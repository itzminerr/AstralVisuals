package pl.astralvisuals.utils.update;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.fabricmc.loader.api.FabricLoader;
import pl.astralvisuals.utils.client.logs.Logger;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.PosixFilePermission;
import java.security.MessageDigest;
import java.time.Duration;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.HexFormat;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.jar.JarFile;

/** Downloads the newest GitHub Release and replaces this mod after Minecraft exits. */
public final class GitHubUpdater {
   private static final String MOD_ID = "astralvisuals";
   private static final String REPOSITORY = "itzminerr/AstralVisuals";
   private static final URI LATEST_RELEASE = URI.create("https://api.github.com/repos/" + REPOSITORY + "/releases/latest");
   private static final long MAX_DOWNLOAD_BYTES = 250L * 1024L * 1024L;
   private static final AtomicBoolean STARTED = new AtomicBoolean();

   private GitHubUpdater() {
   }

   public static void checkForUpdatesAsync() {
      if (!Boolean.parseBoolean(System.getProperty("astralvisuals.autoUpdate", "true")) || !STARTED.compareAndSet(false, true)) {
         return;
      }

      Thread.ofPlatform().daemon(true).name("AstralVisuals GitHub updater").start(GitHubUpdater::checkForUpdates);
   }

   private static void checkForUpdates() {
      try {
         String currentVersion = FabricLoader.getInstance()
            .getModContainer(MOD_ID)
            .orElseThrow(() -> new IllegalStateException("Mod container was not found"))
            .getMetadata()
            .getVersion()
            .getFriendlyString();
         Path currentJar = locateCurrentJar();
         if (currentJar == null) {
            Logger.info("Auto-update skipped in the development environment.");
            return;
         }

         HttpClient client = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .followRedirects(HttpClient.Redirect.NORMAL)
            .build();
         JsonObject release = requestLatestRelease(client);
         String latestVersion = release.get("tag_name").getAsString();
         if (compareVersions(latestVersion, currentVersion) <= 0) {
            Logger.info("AstralVisuals is up to date (" + currentVersion + ").");
            return;
         }

         ReleaseAsset asset = findJarAsset(release.getAsJsonArray("assets"), latestVersion);
         if (asset == null) {
            Logger.warn("GitHub Release " + latestVersion + " has no suitable client JAR asset.");
            return;
         }

         Logger.info("AstralVisuals " + latestVersion + " is available. Downloading update...");
         Path pendingJar = currentJar.resolveSibling(currentJar.getFileName() + ".update-pending");
         download(client, asset.url(), pendingJar);
         try {
            verifyJar(pendingJar, latestVersion);
            verifyDigestIfPresent(pendingJar, asset.digest());
            startReplacementHelper(currentJar, pendingJar);
         } catch (Exception exception) {
            Files.deleteIfExists(pendingJar);
            throw exception;
         }
         Logger.info("Update " + latestVersion + " is ready and will be installed after Minecraft closes.");
      } catch (Exception exception) {
         Logger.warn("Could not check GitHub for AstralVisuals updates: " + exception.getMessage());
      }
   }

   private static JsonObject requestLatestRelease(HttpClient client) throws IOException, InterruptedException {
      HttpRequest request = HttpRequest.newBuilder(LATEST_RELEASE)
         .timeout(Duration.ofSeconds(20))
         .header("Accept", "application/vnd.github+json")
         .header("X-GitHub-Api-Version", "2022-11-28")
         .header("User-Agent", "AstralVisuals-AutoUpdater")
         .GET()
         .build();
      HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
      if (response.statusCode() != 200) {
         throw new IOException("GitHub API returned HTTP " + response.statusCode());
      }
      return JsonParser.parseString(response.body()).getAsJsonObject();
   }

   private static ReleaseAsset findJarAsset(JsonArray assets, String version) {
      if (assets == null) {
         return null;
      }

      String normalizedVersion = normalizeVersion(version).toLowerCase(Locale.ROOT);
      return assets.asList().stream()
         .map(JsonElement::getAsJsonObject)
         .filter(asset -> asset.has("name") && asset.has("browser_download_url"))
         .map(asset -> new ReleaseAsset(
            asset.get("name").getAsString(),
            URI.create(asset.get("browser_download_url").getAsString()),
            asset.has("digest") && !asset.get("digest").isJsonNull() ? asset.get("digest").getAsString() : null
         ))
         .filter(asset -> isClientJar(asset.name()))
         .min(Comparator.comparingInt(asset -> asset.name().toLowerCase(Locale.ROOT).contains(normalizedVersion) ? 0 : 1))
         .orElse(null);
   }

   private static boolean isClientJar(String name) {
      String lower = name.toLowerCase(Locale.ROOT);
      return lower.endsWith(".jar")
         && lower.contains("astralvisuals")
         && !lower.contains("sources")
         && !lower.contains("javadoc")
         && !lower.contains("dev");
   }

   private static void download(HttpClient client, URI url, Path destination) throws IOException, InterruptedException {
      Files.deleteIfExists(destination);
      HttpRequest request = HttpRequest.newBuilder(url)
         .timeout(Duration.ofMinutes(2))
         .header("Accept", "application/octet-stream")
         .header("User-Agent", "AstralVisuals-AutoUpdater")
         .GET()
         .build();
      HttpResponse<Path> response = client.send(request, HttpResponse.BodyHandlers.ofFile(destination));
      if (response.statusCode() != 200) {
         Files.deleteIfExists(destination);
         throw new IOException("update download returned HTTP " + response.statusCode());
      }
      long size = Files.size(destination);
      if (size == 0 || size > MAX_DOWNLOAD_BYTES) {
         Files.deleteIfExists(destination);
         throw new IOException("invalid update size: " + size + " bytes");
      }
   }

   private static void verifyJar(Path jarPath, String expectedVersion) throws IOException {
      try (JarFile jar = new JarFile(jarPath.toFile())) {
         if (jar.getJarEntry("fabric.mod.json") == null) {
            throw new IOException("downloaded file is not an AstralVisuals Fabric mod");
         }
         String metadata = new String(jar.getInputStream(jar.getJarEntry("fabric.mod.json")).readAllBytes(), StandardCharsets.UTF_8);
         JsonObject json = JsonParser.parseString(metadata).getAsJsonObject();
         if (!json.has("id") || !MOD_ID.equals(json.get("id").getAsString())) {
            throw new IOException("downloaded JAR has an unexpected mod id");
         }
         if (!json.has("version") || !normalizeVersion(expectedVersion).equals(normalizeVersion(json.get("version").getAsString()))) {
            throw new IOException("downloaded JAR version does not match the GitHub Release tag");
         }
      }
   }

   private static void verifyDigestIfPresent(Path jarPath, String digest) throws Exception {
      if (digest == null || !digest.toLowerCase(Locale.ROOT).startsWith("sha256:")) {
         return;
      }
      String expected = digest.substring("sha256:".length());
      MessageDigest sha256 = MessageDigest.getInstance("SHA-256");
      try (var input = Files.newInputStream(jarPath)) {
         byte[] buffer = new byte[8192];
         int read;
         while ((read = input.read(buffer)) >= 0) {
            sha256.update(buffer, 0, read);
         }
      }
      String actual = HexFormat.of().formatHex(sha256.digest());
      if (!MessageDigest.isEqual(expected.getBytes(StandardCharsets.US_ASCII), actual.getBytes(StandardCharsets.US_ASCII))) {
         throw new IOException("GitHub SHA-256 verification failed");
      }
   }

   private static Path locateCurrentJar() throws Exception {
      Path location = Path.of(GitHubUpdater.class.getProtectionDomain().getCodeSource().getLocation().toURI()).toAbsolutePath().normalize();
      return Files.isRegularFile(location) && location.getFileName().toString().toLowerCase(Locale.ROOT).endsWith(".jar") ? location : null;
   }

   private static void startReplacementHelper(Path currentJar, Path pendingJar) throws IOException {
      long pid = ProcessHandle.current().pid();
      if (isWindows()) {
         Path script = Files.createTempFile("astralvisuals-update-", ".ps1");
         Files.writeString(script, windowsUpdateScript(), StandardCharsets.UTF_8);
         new ProcessBuilder(
            "powershell.exe", "-NoProfile", "-NonInteractive", "-WindowStyle", "Hidden", "-ExecutionPolicy", "Bypass",
            "-File", script.toString(), "-ProcessId", Long.toString(pid), "-CurrentJar", currentJar.toString(), "-PendingJar", pendingJar.toString()
         ).redirectOutput(ProcessBuilder.Redirect.DISCARD).redirectError(ProcessBuilder.Redirect.DISCARD).start();
      } else {
         Path script = Files.createTempFile("astralvisuals-update-", ".sh");
         Files.writeString(script, unixUpdateScript(), StandardCharsets.UTF_8);
         try {
            Files.setPosixFilePermissions(script, EnumSet.of(PosixFilePermission.OWNER_READ, PosixFilePermission.OWNER_WRITE, PosixFilePermission.OWNER_EXECUTE));
         } catch (UnsupportedOperationException ignored) {
         }
         new ProcessBuilder("sh", script.toString(), Long.toString(pid), currentJar.toString(), pendingJar.toString())
            .redirectOutput(ProcessBuilder.Redirect.DISCARD).redirectError(ProcessBuilder.Redirect.DISCARD).start();
      }
   }

   private static String windowsUpdateScript() {
      return """
         param([long]$ProcessId, [string]$CurrentJar, [string]$PendingJar)
         $backup = $CurrentJar + '.update-backup'
         try {
           Wait-Process -Id $ProcessId -ErrorAction SilentlyContinue
           Start-Sleep -Milliseconds 500
           Remove-Item -LiteralPath $backup -Force -ErrorAction SilentlyContinue
           Move-Item -LiteralPath $CurrentJar -Destination $backup -Force
           try {
             Move-Item -LiteralPath $PendingJar -Destination $CurrentJar -Force
             Remove-Item -LiteralPath $backup -Force -ErrorAction SilentlyContinue
           } catch {
             Move-Item -LiteralPath $backup -Destination $CurrentJar -Force -ErrorAction SilentlyContinue
           }
         } finally {
           Remove-Item -LiteralPath $PSCommandPath -Force -ErrorAction SilentlyContinue
         }
         """;
   }

   private static String unixUpdateScript() {
      return """
         #!/bin/sh
         pid="$1"
         current="$2"
         pending="$3"
         backup="${current}.update-backup"
         while kill -0 "$pid" 2>/dev/null; do sleep 1; done
         rm -f -- "$backup"
         if mv -- "$current" "$backup" && mv -- "$pending" "$current"; then
           rm -f -- "$backup"
         else
           mv -- "$backup" "$current" 2>/dev/null || true
         fi
         rm -f -- "$0"
         """;
   }

   private static boolean isWindows() {
      return System.getProperty("os.name", "").toLowerCase(Locale.ROOT).contains("win");
   }

   static int compareVersions(String left, String right) {
      String[] leftParts = normalizeVersion(left).split("[-+]", 2)[0].split("\\.");
      String[] rightParts = normalizeVersion(right).split("[-+]", 2)[0].split("\\.");
      int length = Math.max(leftParts.length, rightParts.length);
      for (int index = 0; index < length; index++) {
         int leftNumber = numericPart(leftParts, index);
         int rightNumber = numericPart(rightParts, index);
         if (leftNumber != rightNumber) {
            return Integer.compare(leftNumber, rightNumber);
         }
      }
      boolean leftPrerelease = normalizeVersion(left).contains("-");
      boolean rightPrerelease = normalizeVersion(right).contains("-");
      return Boolean.compare(rightPrerelease, leftPrerelease);
   }

   private static int numericPart(String[] parts, int index) {
      if (index >= parts.length) {
         return 0;
      }
      String digits = parts[index].replaceFirst("[^0-9].*$", "");
      if (digits.isEmpty()) {
         return 0;
      }
      try {
         return Integer.parseInt(digits);
      } catch (NumberFormatException ignored) {
         return Integer.MAX_VALUE;
      }
   }

   private static String normalizeVersion(String version) {
      String normalized = version == null ? "0" : version.trim();
      return normalized.startsWith("v") || normalized.startsWith("V") ? normalized.substring(1) : normalized;
   }

   private record ReleaseAsset(String name, URI url, String digest) {
   }
}
