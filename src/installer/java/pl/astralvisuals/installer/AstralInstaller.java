package pl.astralvisuals.installer;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.security.MessageDigest;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HexFormat;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** Standalone pre-launch updater for AstralVisuals. Uses only the Java runtime. */
public final class AstralInstaller {
   static final URI LATEST_RELEASE = URI.create("https://api.github.com/repos/itzminerr/AstralVisuals/releases/latest");
   private static final String MOD_ID = "astralvisuals";
   private static final String REPOSITORY_RELEASE_PATH = "/itzminerr/AstralVisuals/releases/download/";
   private static final long MAX_DOWNLOAD_BYTES = 250L * 1024L * 1024L;
   private static final int MAX_METADATA_BYTES = 1024 * 1024;

   private AstralInstaller() {
   }

   public static void main(String[] args) {
      int exitCode = run(args, System.getenv(), Path.of("").toAbsolutePath().normalize(), System.out, System.err);
      if (exitCode != 0) {
         System.exit(exitCode);
      }
   }

   static int run(String[] args, Map<String, String> environment, Path workingDirectory, PrintStream out, PrintStream err) {
      Options options;
      try {
         options = Options.parse(args);
      } catch (IllegalArgumentException exception) {
         err.println("AstralInstaller: " + exception.getMessage());
         printUsage(err);
         return 2;
      }
      if (options.help()) {
         printUsage(out);
         return 0;
      }

      Path modsDirectory = resolveModsDirectory(options.modsDirectory(), environment, workingDirectory);
      List<ClientJar> installed;
      try {
         Files.createDirectories(modsDirectory);
         if (!Files.isDirectory(modsDirectory)) {
            throw new IOException("mods path is not a directory");
         }
         installed = discoverClientJars(modsDirectory);
      } catch (Exception exception) {
         err.println("AstralInstaller: cannot access " + modsDirectory + ": " + message(exception));
         return 1;
      }

      out.println("AstralInstaller: checking GitHub before Minecraft starts...");
      try {
         URI releaseUri = releaseUri(environment);
         UpdateResult result = update(modsDirectory, releaseUri);
         if (result.updated()) {
            String previous = result.previousVersion() == null ? "not installed" : result.previousVersion();
            out.println("AstralInstaller: installed " + result.version() + " (previous: " + previous + ").");
         } else {
            out.println("AstralInstaller: AstralVisuals " + result.version() + " is already up to date.");
         }
         return 0;
      } catch (InstallationException exception) {
         err.println("AstralInstaller: local installation failed; Minecraft launch was stopped: " + message(exception));
         return 1;
      } catch (Exception exception) {
         if (!installed.isEmpty()) {
            ClientJar current = newest(installed);
            err.println(
               "AstralInstaller: update check failed (" + message(exception) + "). Continuing with installed " + current.version() + "."
            );
            return 0;
         }
         err.println("AstralInstaller: no client is installed and GitHub could not be reached: " + message(exception));
         return 1;
      }
   }

   private static URI releaseUri(Map<String, String> environment) {
      String override = environment.get("ASTRAL_RELEASE_API");
      return override == null || override.isBlank() ? LATEST_RELEASE : URI.create(override);
   }

   static UpdateResult update(Path modsDirectory, URI releaseUri) throws Exception {
      Path normalizedMods = modsDirectory.toAbsolutePath().normalize();
      Files.createDirectories(normalizedMods);
      List<ClientJar> installed = discoverClientJars(normalizedMods);
      ClientJar current = installed.isEmpty() ? null : newest(installed);

      HttpClient client = HttpClient.newBuilder()
         .connectTimeout(Duration.ofSeconds(10))
         .followRedirects(HttpClient.Redirect.NORMAL)
         .build();
      Release release = requestLatestRelease(client, releaseUri);
      ReleaseAsset asset = selectClientAsset(release);
      validateAsset(asset);

      if (current != null && compareVersions(current.version(), release.version()) >= 0) {
         cleanupDuplicates(installed, current.path());
         return new UpdateResult(false, current.version(), current.version(), current.path());
      }

      Path pending = Files.createTempFile(normalizedMods, ".astralvisuals-download-", ".jar.part");
      try {
         download(client, asset.url(), pending);
         verifyClientJar(pending, release.version());
         verifyDigestIfPresent(pending, asset.digest());
         Path installedPath = installDownloadedJar(normalizedMods, pending, asset.name(), installed);
         return new UpdateResult(true, normalizeVersion(release.version()), current == null ? null : current.version(), installedPath);
      } finally {
         Files.deleteIfExists(pending);
      }
   }

   static Path resolveModsDirectory(Path explicit, Map<String, String> environment, Path workingDirectory) {
      if (explicit != null) {
         return explicit.toAbsolutePath().normalize();
      }
      String configured = environment.get("ASTRAL_MODS_DIR");
      if (configured != null && !configured.isBlank()) {
         return Path.of(configured).toAbsolutePath().normalize();
      }
      String prismMinecraftDirectory = environment.get("INST_MC_DIR");
      if (prismMinecraftDirectory != null && !prismMinecraftDirectory.isBlank()) {
         return Path.of(prismMinecraftDirectory).resolve("mods").toAbsolutePath().normalize();
      }
      Path normalizedWorkingDirectory = workingDirectory.toAbsolutePath().normalize();
      Path fileName = normalizedWorkingDirectory.getFileName();
      return fileName != null && fileName.toString().equalsIgnoreCase("mods")
         ? normalizedWorkingDirectory
         : normalizedWorkingDirectory.resolve("mods");
   }

   static List<ClientJar> discoverClientJars(Path modsDirectory) throws IOException {
      if (!Files.isDirectory(modsDirectory)) {
         return List.of();
      }
      List<ClientJar> clients = new ArrayList<>();
      try (var paths = Files.list(modsDirectory)) {
         for (Path path : paths.filter(Files::isRegularFile).sorted().toList()) {
            if (!path.getFileName().toString().toLowerCase(Locale.ROOT).endsWith(".jar")) {
               continue;
            }
            readClientJar(path).ifPresent(clients::add);
         }
      }
      return clients;
   }

   private static Optional<ClientJar> readClientJar(Path path) {
      try (JarFile jar = new JarFile(path.toFile())) {
         JarEntry metadataEntry = jar.getJarEntry("fabric.mod.json");
         if (metadataEntry == null || metadataEntry.getSize() > MAX_METADATA_BYTES) {
            return Optional.empty();
         }
         byte[] metadataBytes;
         try (InputStream input = jar.getInputStream(metadataEntry)) {
            metadataBytes = input.readNBytes(MAX_METADATA_BYTES + 1);
         }
         if (metadataBytes.length > MAX_METADATA_BYTES) {
            return Optional.empty();
         }
         String metadata = new String(metadataBytes, StandardCharsets.UTF_8);
         String id = jsonStringField(metadata, "id");
         String version = jsonStringField(metadata, "version");
         if (!MOD_ID.equals(id) || version == null || version.isBlank()) {
            return Optional.empty();
         }
         return Optional.of(new ClientJar(path.toAbsolutePath().normalize(), normalizeVersion(version)));
      } catch (Exception ignored) {
         return Optional.empty();
      }
   }

   static Release requestLatestRelease(HttpClient client, URI releaseUri) throws IOException, InterruptedException {
      HttpRequest request = HttpRequest.newBuilder(releaseUri)
         .timeout(Duration.ofSeconds(20))
         .header("Accept", "application/vnd.github+json")
         .header("X-GitHub-Api-Version", "2022-11-28")
         .header("User-Agent", "AstralVisuals-Installer")
         .GET()
         .build();
      HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
      if (response.statusCode() != 200) {
         throw new IOException("GitHub API returned HTTP " + response.statusCode());
      }
      if (response.body().length() > 2_000_000) {
         throw new IOException("GitHub response is unexpectedly large");
      }
      return parseRelease(response.body());
   }

   static Release parseRelease(String json) throws IOException {
      String version = jsonStringField(json, "tag_name");
      if (version == null || version.isBlank()) {
         throw new IOException("GitHub Release has no tag_name");
      }
      String assetsJson = jsonArray(json, "assets");
      List<ReleaseAsset> assets = new ArrayList<>();
      for (String assetJson : topLevelObjects(assetsJson)) {
         String name = jsonStringField(assetJson, "name");
         String downloadUrl = jsonStringField(assetJson, "browser_download_url");
         String digest = jsonStringField(assetJson, "digest");
         if (name != null && downloadUrl != null) {
            try {
               assets.add(new ReleaseAsset(name, URI.create(downloadUrl), digest));
            } catch (IllegalArgumentException ignored) {
            }
         }
      }
      return new Release(version, List.copyOf(assets));
   }

   private static ReleaseAsset selectClientAsset(Release release) throws IOException {
      String normalizedVersion = normalizeVersion(release.version()).toLowerCase(Locale.ROOT);
      return release.assets().stream()
         .filter(asset -> isClientJarName(asset.name()))
         .min(Comparator.comparingInt(asset -> asset.name().toLowerCase(Locale.ROOT).contains(normalizedVersion) ? 0 : 1))
         .orElseThrow(() -> new IOException("GitHub Release " + release.version() + " has no AstralVisuals client JAR"));
   }

   private static void validateAsset(ReleaseAsset asset) throws IOException {
      if (!isSafeFileName(asset.name()) || !isClientJarName(asset.name())) {
         throw new IOException("GitHub asset has an unsafe file name");
      }
      String scheme = asset.url().getScheme();
      String host = asset.url().getHost();
      String path = asset.url().getPath();
      if (!"https".equalsIgnoreCase(scheme)
         || host == null
         || !host.equalsIgnoreCase("github.com")
         || path == null
         || !path.toLowerCase(Locale.ROOT).startsWith(REPOSITORY_RELEASE_PATH.toLowerCase(Locale.ROOT))) {
         throw new IOException("GitHub asset URL is outside the AstralVisuals repository");
      }
   }

   static void download(HttpClient client, URI url, Path destination) throws IOException, InterruptedException {
      HttpRequest request = HttpRequest.newBuilder(url)
         .timeout(Duration.ofMinutes(3))
         .header("Accept", "application/octet-stream")
         .header("User-Agent", "AstralVisuals-Installer")
         .GET()
         .build();
      HttpResponse<InputStream> response = client.send(request, HttpResponse.BodyHandlers.ofInputStream());
      try (InputStream input = response.body()) {
         if (response.statusCode() != 200) {
            throw new IOException("client download returned HTTP " + response.statusCode());
         }
         long declaredSize = response.headers().firstValueAsLong("Content-Length").orElse(-1L);
         if (declaredSize == 0L || declaredSize > MAX_DOWNLOAD_BYTES) {
            throw new IOException("invalid client download size: " + declaredSize + " bytes");
         }
         try (var output = Files.newOutputStream(destination, StandardOpenOption.TRUNCATE_EXISTING)) {
            byte[] buffer = new byte[64 * 1024];
            long total = 0L;
            int read;
            while ((read = input.read(buffer)) >= 0) {
               total += read;
               if (total > MAX_DOWNLOAD_BYTES) {
                  throw new IOException("client download exceeds " + MAX_DOWNLOAD_BYTES + " bytes");
               }
               output.write(buffer, 0, read);
            }
            if (total == 0L) {
               throw new IOException("downloaded client is empty");
            }
         }
      }
   }

   static void verifyClientJar(Path jarPath, String expectedVersion) throws IOException {
      ClientJar client = readClientJar(jarPath).orElseThrow(() -> new IOException("downloaded file is not an AstralVisuals Fabric mod"));
      if (!normalizeVersion(expectedVersion).equals(client.version())) {
         throw new IOException("downloaded JAR version does not match the GitHub Release tag");
      }
   }

   static void verifyDigestIfPresent(Path jarPath, String digest) throws Exception {
      if (digest == null || digest.isBlank()) {
         return;
      }
      String lowerDigest = digest.toLowerCase(Locale.ROOT);
      if (!lowerDigest.matches("sha256:[0-9a-f]{64}")) {
         throw new IOException("GitHub supplied an invalid SHA-256 digest");
      }
      String expected = lowerDigest.substring("sha256:".length());
      MessageDigest sha256 = MessageDigest.getInstance("SHA-256");
      try (InputStream input = Files.newInputStream(jarPath)) {
         byte[] buffer = new byte[64 * 1024];
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

   private static Path installDownloadedJar(Path modsDirectory, Path pending, String assetName, List<ClientJar> installed)
      throws InstallationException {
      Path target = modsDirectory.resolve(assetName).toAbsolutePath().normalize();
      if (!target.getParent().equals(modsDirectory.toAbsolutePath().normalize())) {
         throw new InstallationException("release asset escapes the mods directory");
      }
      if (Files.exists(target) && installed.stream().noneMatch(client -> client.path().equals(target))) {
         throw new InstallationException("refusing to overwrite an unrelated file: " + target.getFileName());
      }

      Map<Path, Path> backups = new LinkedHashMap<>();
      boolean newJarInstalled = false;
      try {
         for (ClientJar client : installed) {
            Path backup = client.path().resolveSibling(
               client.path().getFileName() + ".astralinstaller-backup-" + UUID.randomUUID()
            );
            move(client.path(), backup, false);
            backups.put(client.path(), backup);
         }
         move(pending, target, false);
         newJarInstalled = true;
      } catch (Exception exception) {
         if (newJarInstalled) {
            try {
               Files.deleteIfExists(target);
            } catch (IOException ignored) {
            }
         }
         restoreBackups(backups);
         throw new InstallationException(message(exception), exception);
      }

      for (Path backup : backups.values()) {
         try {
            Files.deleteIfExists(backup);
         } catch (IOException ignored) {
            backup.toFile().deleteOnExit();
         }
      }
      return target;
   }

   private static void cleanupDuplicates(List<ClientJar> installed, Path keep) throws InstallationException {
      for (ClientJar client : installed) {
         if (!client.path().equals(keep)) {
            try {
               Files.deleteIfExists(client.path());
            } catch (IOException exception) {
               throw new InstallationException("could not remove duplicate " + client.path().getFileName(), exception);
            }
         }
      }
   }

   private static void restoreBackups(Map<Path, Path> backups) {
      List<Map.Entry<Path, Path>> entries = new ArrayList<>(backups.entrySet());
      for (int index = entries.size() - 1; index >= 0; index--) {
         Map.Entry<Path, Path> entry = entries.get(index);
         if (!Files.exists(entry.getValue())) {
            continue;
         }
         try {
            move(entry.getValue(), entry.getKey(), true);
         } catch (IOException ignored) {
         }
      }
   }

   private static void move(Path source, Path target, boolean replace) throws IOException {
      StandardCopyOption[] atomicOptions = replace
         ? new StandardCopyOption[] {StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING}
         : new StandardCopyOption[] {StandardCopyOption.ATOMIC_MOVE};
      StandardCopyOption[] fallbackOptions = replace
         ? new StandardCopyOption[] {StandardCopyOption.REPLACE_EXISTING}
         : new StandardCopyOption[0];
      try {
         Files.move(source, target, atomicOptions);
      } catch (AtomicMoveNotSupportedException exception) {
         Files.move(source, target, fallbackOptions);
      }
   }

   private static ClientJar newest(List<ClientJar> clients) {
      return clients.stream()
         .max((left, right) -> {
            int version = compareVersions(left.version(), right.version());
            return version != 0 ? version : right.path().toString().compareTo(left.path().toString());
         })
         .orElseThrow();
   }

   private static boolean isClientJarName(String name) {
      String lower = name.toLowerCase(Locale.ROOT);
      return lower.endsWith(".jar")
         && lower.contains("astralvisuals")
         && !lower.contains("sources")
         && !lower.contains("javadoc")
         && !lower.contains("installer")
         && !lower.contains("dev");
   }

   private static boolean isSafeFileName(String name) {
      return name != null && !name.isBlank() && !name.contains("/") && !name.contains("\\") && !name.equals(".") && !name.equals("..");
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

   static String normalizeVersion(String version) {
      String normalized = version == null ? "0" : version.trim();
      return normalized.startsWith("v") || normalized.startsWith("V") ? normalized.substring(1) : normalized;
   }

   private static String jsonStringField(String json, String field) {
      Pattern pattern = Pattern.compile(
         "\\\"" + Pattern.quote(field) + "\\\"\\s*:\\s*\\\"((?:\\\\.|[^\\\"\\\\])*)\\\""
      );
      Matcher matcher = pattern.matcher(json);
      return matcher.find() ? unescapeJson(matcher.group(1)) : null;
   }

   private static String jsonArray(String json, String field) throws IOException {
      Pattern pattern = Pattern.compile("\\\"" + Pattern.quote(field) + "\\\"\\s*:");
      Matcher matcher = pattern.matcher(json);
      if (!matcher.find()) {
         throw new IOException("GitHub Release has no " + field + " array");
      }
      int start = json.indexOf('[', matcher.end());
      if (start < 0) {
         throw new IOException("GitHub Release has an invalid " + field + " array");
      }
      int end = findMatching(json, start, '[', ']');
      if (end < 0) {
         throw new IOException("GitHub Release has an unterminated " + field + " array");
      }
      return json.substring(start + 1, end);
   }

   private static List<String> topLevelObjects(String json) {
      List<String> objects = new ArrayList<>();
      boolean inString = false;
      boolean escaped = false;
      int depth = 0;
      int start = -1;
      for (int index = 0; index < json.length(); index++) {
         char value = json.charAt(index);
         if (inString) {
            if (escaped) {
               escaped = false;
            } else if (value == '\\') {
               escaped = true;
            } else if (value == '"') {
               inString = false;
            }
            continue;
         }
         if (value == '"') {
            inString = true;
         } else if (value == '{') {
            if (depth++ == 0) {
               start = index;
            }
         } else if (value == '}' && depth > 0 && --depth == 0 && start >= 0) {
            objects.add(json.substring(start, index + 1));
            start = -1;
         }
      }
      return objects;
   }

   private static int findMatching(String json, int start, char open, char close) {
      boolean inString = false;
      boolean escaped = false;
      int depth = 0;
      for (int index = start; index < json.length(); index++) {
         char value = json.charAt(index);
         if (inString) {
            if (escaped) {
               escaped = false;
            } else if (value == '\\') {
               escaped = true;
            } else if (value == '"') {
               inString = false;
            }
         } else if (value == '"') {
            inString = true;
         } else if (value == open) {
            depth++;
         } else if (value == close && --depth == 0) {
            return index;
         }
      }
      return -1;
   }

   private static String unescapeJson(String value) {
      StringBuilder result = new StringBuilder(value.length());
      for (int index = 0; index < value.length(); index++) {
         char current = value.charAt(index);
         if (current != '\\' || index + 1 >= value.length()) {
            result.append(current);
            continue;
         }
         char escaped = value.charAt(++index);
         switch (escaped) {
            case '"', '\\', '/' -> result.append(escaped);
            case 'b' -> result.append('\b');
            case 'f' -> result.append('\f');
            case 'n' -> result.append('\n');
            case 'r' -> result.append('\r');
            case 't' -> result.append('\t');
            case 'u' -> {
               if (index + 4 >= value.length()) {
                  throw new IllegalArgumentException("invalid JSON unicode escape");
               }
               result.append((char)Integer.parseInt(value.substring(index + 1, index + 5), 16));
               index += 4;
            }
            default -> throw new IllegalArgumentException("invalid JSON escape");
         }
      }
      return result.toString();
   }

   private static String message(Throwable throwable) {
      String message = throwable.getMessage();
      return message == null || message.isBlank() ? throwable.getClass().getSimpleName() : message;
   }

   private static void printUsage(PrintStream output) {
      output.println("Usage: java -jar astralinstaller.jar [--mods-dir <path>]");
      output.println("PrismLauncher: INST_MC_DIR is detected automatically by the pre-launch command.");
   }

   record ClientJar(Path path, String version) {
   }

   record Release(String version, List<ReleaseAsset> assets) {
   }

   record ReleaseAsset(String name, URI url, String digest) {
   }

   record UpdateResult(boolean updated, String version, String previousVersion, Path installedPath) {
   }

   private record Options(Path modsDirectory, boolean help) {
      private static Options parse(String[] args) {
         Path modsDirectory = null;
         boolean help = false;
         for (int index = 0; index < args.length; index++) {
            String argument = args[index];
            if (argument.equals("--help") || argument.equals("-h")) {
               help = true;
            } else if (argument.equals("--mods-dir")) {
               if (++index >= args.length) {
                  throw new IllegalArgumentException("--mods-dir requires a path");
               }
               modsDirectory = Path.of(args[index]);
            } else if (argument.startsWith("--mods-dir=")) {
               modsDirectory = Path.of(argument.substring("--mods-dir=".length()));
            } else if (!argument.startsWith("-") && modsDirectory == null) {
               modsDirectory = Path.of(argument);
            } else {
               throw new IllegalArgumentException("unknown argument: " + argument);
            }
         }
         return new Options(modsDirectory, help);
      }
   }

   static final class InstallationException extends Exception {
      private InstallationException(String message) {
         super(message);
      }

      private InstallationException(String message, Throwable cause) {
         super(message, cause);
      }
   }
}
