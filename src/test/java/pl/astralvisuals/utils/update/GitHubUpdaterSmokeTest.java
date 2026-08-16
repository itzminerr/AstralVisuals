package pl.astralvisuals.utils.update;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.lang.reflect.Method;
import java.net.URI;
import java.net.http.HttpClient;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.Comparator;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

/** A dependency-free integration check that can be run with ./gradlew updaterSmokeTest. */
public final class GitHubUpdaterSmokeTest {
   private GitHubUpdaterSmokeTest() {
   }

   public static void main(String[] args) throws Exception {
      Path testDirectory = Files.createTempDirectory("astralvisuals-updater-test-");
      try {
         testVersionComparison();
         testLiveGitHubRelease(testDirectory);
         testReplacementHelper(testDirectory);
         System.out.println("UPDATER SMOKE TEST PASSED");
      } finally {
         deleteRecursively(testDirectory);
      }
   }

   private static void testVersionComparison() throws Exception {
      Method compare = method("compareVersions", String.class, String.class);
      require((int)compare.invoke(null, "v1.3", "1.2") > 0, "v1.3 must be newer than 1.2");
      require((int)compare.invoke(null, "1.2", "v1.2") == 0, "v prefix must not affect equality");
      require((int)compare.invoke(null, "1.2.1", "1.2") > 0, "patch version comparison failed");
      require((int)compare.invoke(null, "1.2-beta", "1.2") < 0, "pre-release must be older than stable");
      System.out.println("[OK] Semantic version comparison");
   }

   private static void testLiveGitHubRelease(Path testDirectory) throws Exception {
      HttpClient client = HttpClient.newBuilder()
         .connectTimeout(Duration.ofSeconds(10))
         .followRedirects(HttpClient.Redirect.NORMAL)
         .build();
      JsonObject release = (JsonObject)method("requestLatestRelease", HttpClient.class).invoke(null, client);
      String tag = release.get("tag_name").getAsString();
      Object asset = method("findJarAsset", JsonArray.class, String.class).invoke(null, release.getAsJsonArray("assets"), tag);
      require(asset != null, "latest GitHub Release has no matching AstralVisuals JAR");

      Method nameAccessor = asset.getClass().getDeclaredMethod("name");
      Method urlAccessor = asset.getClass().getDeclaredMethod("url");
      Method digestAccessor = asset.getClass().getDeclaredMethod("digest");
      nameAccessor.setAccessible(true);
      urlAccessor.setAccessible(true);
      digestAccessor.setAccessible(true);
      String assetName = (String)nameAccessor.invoke(asset);
      URI assetUrl = (URI)urlAccessor.invoke(asset);
      String digest = (String)digestAccessor.invoke(asset);

      Path downloadedJar = testDirectory.resolve("downloaded-release.jar");
      method("download", HttpClient.class, URI.class, Path.class).invoke(null, client, assetUrl, downloadedJar);
      method("verifyJar", Path.class, String.class).invoke(null, downloadedJar, tag);
      method("verifyDigestIfPresent", Path.class, String.class).invoke(null, downloadedJar, digest);
      require(Files.size(downloadedJar) > 0, "downloaded release JAR is empty");
      System.out.println("[OK] GitHub Release " + tag + ": " + assetName + " (" + Files.size(downloadedJar) + " bytes)");
   }

   private static void testReplacementHelper(Path testDirectory) throws Exception {
      Path current = testDirectory.resolve("installed.jar");
      Path pending = testDirectory.resolve("installed.jar.update-pending");
      Files.writeString(current, "old-version", StandardCharsets.UTF_8);
      Files.writeString(pending, "new-version", StandardCharsets.UTF_8);

      Process sleeper;
      Process updater;
      if (isWindows()) {
         sleeper = new ProcessBuilder("powershell.exe", "-NoProfile", "-NonInteractive", "-Command", "Start-Sleep -Milliseconds 800").start();
         String scriptBody = (String)method("windowsUpdateScript").invoke(null);
         Path script = testDirectory.resolve("replace-test.ps1");
         Files.writeString(script, scriptBody, StandardCharsets.UTF_8);
         updater = new ProcessBuilder(
            "powershell.exe", "-NoProfile", "-NonInteractive", "-ExecutionPolicy", "Bypass", "-File", script.toString(),
            "-ProcessId", Long.toString(sleeper.pid()), "-CurrentJar", current.toString(), "-PendingJar", pending.toString()
         ).redirectErrorStream(true).start();
      } else {
         sleeper = new ProcessBuilder("sh", "-c", "sleep 1").start();
         String scriptBody = (String)method("unixUpdateScript").invoke(null);
         Path script = testDirectory.resolve("replace-test.sh");
         Files.writeString(script, scriptBody, StandardCharsets.UTF_8);
         updater = new ProcessBuilder("sh", script.toString(), Long.toString(sleeper.pid()), current.toString(), pending.toString())
            .redirectErrorStream(true).start();
      }

      require(updater.waitFor(15, TimeUnit.SECONDS), "replacement helper timed out");
      String helperOutput = new String(updater.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
      require(updater.exitValue() == 0, "replacement helper failed: " + helperOutput);
      require("new-version".equals(Files.readString(current, StandardCharsets.UTF_8)), "replacement helper did not install the pending file");
      require(!Files.exists(pending), "pending file was not moved");
      require(!Files.exists(current.resolveSibling(current.getFileName() + ".update-backup")), "backup was not cleaned up");
      System.out.println("[OK] Post-exit JAR replacement on " + System.getProperty("os.name"));
   }

   private static Method method(String name, Class<?>... parameterTypes) throws Exception {
      Method method = GitHubUpdater.class.getDeclaredMethod(name, parameterTypes);
      method.setAccessible(true);
      return method;
   }

   private static boolean isWindows() {
      return System.getProperty("os.name", "").toLowerCase(Locale.ROOT).contains("win");
   }

   private static void require(boolean condition, String message) {
      if (!condition) {
         throw new AssertionError(message);
      }
   }

   private static void deleteRecursively(Path directory) throws Exception {
      if (!Files.exists(directory)) {
         return;
      }
      try (var paths = Files.walk(directory)) {
         for (Path path : paths.sorted(Comparator.reverseOrder()).toList()) {
            Files.deleteIfExists(path);
         }
      }
   }
}
