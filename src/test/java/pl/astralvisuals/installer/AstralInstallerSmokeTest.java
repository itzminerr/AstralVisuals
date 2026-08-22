package pl.astralvisuals.installer;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.JarOutputStream;

/** Integration checks for the Fabric pre-launch installer and its live GitHub update path. */
public final class AstralInstallerSmokeTest {
   private AstralInstallerSmokeTest() {
   }

   public static void main(String[] args) throws Exception {
      Path testDirectory = Files.createTempDirectory("astralinstaller-test-");
      try {
         testVersionComparison();
         testReleaseParsing();
         testFabricArtifact(Path.of("astralinstaller.jar").toAbsolutePath().normalize());
         testLiveInstallAndNoUpdate(testDirectory);
         testOfflineCacheSafety(testDirectory);
         System.out.println("ASTRAL INSTALLER SMOKE TEST PASSED");
      } finally {
         deleteRecursively(testDirectory);
      }
   }

   private static void testVersionComparison() {
      require(AstralInstaller.compareVersions("v2.5", "2.4") > 0, "version increment was not detected");
      require(AstralInstaller.compareVersions("2.2", "v2.2") == 0, "v prefix changed equality");
      require(AstralInstaller.compareVersions("2.2.1", "2.2") > 0, "patch comparison failed");
      require(AstralInstaller.compareVersions("2.2-beta", "2.2") < 0, "pre-release must be older than stable");
      System.out.println("[OK] Semantic version comparison");
   }

   private static void testReleaseParsing() throws Exception {
      String json = """
         {
           "tag_name": "v9.4",
           "assets": [
             {"name":"astralinstaller.jar","browser_download_url":"https://github.com/itzminerr/AstralVisuals/releases/download/v9.4/astralinstaller.jar"},
             {"digest":"sha256:aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa","name":"astralvisuals-9.4.jar","browser_download_url":"https://github.com/itzminerr/AstralVisuals/releases/download/v9.4/astralvisuals-9.4.jar"}
           ]
         }
         """;
      AstralInstaller.Release release = AstralInstaller.parseRelease(json);
      require("v9.4".equals(release.version()), "release tag was not parsed");
      require(release.assets().size() == 2, "release assets were not parsed");
      require(release.assets().get(1).digest().startsWith("sha256:"), "asset digest was not parsed");
      System.out.println("[OK] Dependency-free GitHub JSON parsing");
   }

   private static void testFabricArtifact(Path installer) throws Exception {
      require(Files.isRegularFile(installer), "astralinstaller.jar was not built");
      try (JarFile jar = new JarFile(installer.toFile())) {
         String metadata = readEntry(jar, "fabric.mod.json");
         require(metadata.contains("\"id\": \"astralinstaller\""), "installer has the wrong Fabric mod id");
         require(metadata.contains("\"preLaunch\""), "installer has no preLaunch entrypoint");
         require(metadata.contains("\"main\""), "installer has no normal initialization entrypoint");
         require(readEntry(jar, "accesswidener").startsWith("accessWidener\tv1\tintermediary"), "access widener was not remapped");
         require(jar.getJarEntry("META-INF/jars/satin-3.0.0-alpha.1.jar") != null, "Satin runtime is missing");
         require(jar.getJarEntry("META-INF/jars/jna-5.13.0.jar") == null, "obsolete JNA conflicts with Minecraft 1.21.4");
         require(jar.getJarEntry("pl/astralvisuals/Force.class") == null, "installer accidentally contains the client payload");
      }
      System.out.println("[OK] astralinstaller.jar is a self-contained Fabric pre-launch mod");
   }

   private static void testLiveInstallAndNoUpdate(Path testDirectory) throws Exception {
      Path cache = testDirectory.resolve("live-cache");
      Files.createDirectories(cache);
      createClientJar(cache.resolve("client.jar"), "0.1");

      AstralInstaller.UpdateResult installed = AstralInstaller.update(cache, AstralInstaller.LATEST_RELEASE);
      require(installed.updated(), "live GitHub client was not downloaded");
      require(Files.isRegularFile(installed.client().path()), "downloaded client is missing");
      AstralInstaller.verifyClientJar(installed.client().path(), installed.client().version());

      AstralInstaller.UpdateResult repeated = AstralInstaller.update(cache, AstralInstaller.LATEST_RELEASE);
      require(!repeated.updated(), "current release was downloaded twice");
      require(installed.client().version().equals(repeated.client().version()), "cached version changed during the second check");
      System.out.println("[OK] Live GitHub update and same-launch cache: " + installed.client().version());
   }

   private static void testOfflineCacheSafety(Path testDirectory) throws Exception {
      Path cache = testDirectory.resolve("offline-cache");
      Files.createDirectories(cache);
      Path client = cache.resolve("client.jar");
      createClientJar(client, "1.0");
      boolean failed = false;
      try {
         AstralInstaller.update(cache, URI.create("http://127.0.0.1:1/releases/latest"));
      } catch (Exception expected) {
         failed = true;
      }
      require(failed, "offline update unexpectedly succeeded");
      require("1.0".equals(AstralInstaller.verifyClientJar(client, null).version()), "offline check damaged the cache");
      System.out.println("[OK] Existing cached client survives an unavailable GitHub API");
   }

   private static String readEntry(JarFile jar, String name) throws Exception {
      JarEntry entry = jar.getJarEntry(name);
      require(entry != null, name + " is missing");
      try (var input = jar.getInputStream(entry)) {
         return new String(input.readAllBytes(), StandardCharsets.UTF_8);
      }
   }

   private static void createClientJar(Path path, String version) throws Exception {
      try (JarOutputStream output = new JarOutputStream(Files.newOutputStream(path))) {
         writeEntry(output, "fabric.mod.json", "{\"schemaVersion\":1,\"id\":\"astralvisuals\",\"version\":\"" + version + "\"}");
         writeEntry(output, "mixins.json", "{}");
         writeEntry(output, "pl/astralvisuals/Force.class", "test");
      }
   }

   private static void writeEntry(JarOutputStream output, String name, String contents) throws Exception {
      output.putNextEntry(new JarEntry(name));
      output.write(contents.getBytes(StandardCharsets.UTF_8));
      output.closeEntry();
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
