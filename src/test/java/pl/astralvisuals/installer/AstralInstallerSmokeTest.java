package pl.astralvisuals.installer;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.Map;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;

/** Integration check for the standalone pre-launch installer. */
public final class AstralInstallerSmokeTest {
   private AstralInstallerSmokeTest() {
   }

   public static void main(String[] args) throws Exception {
      Path testDirectory = Files.createTempDirectory("astralinstaller-test-");
      try {
         testVersionComparison();
         testReleaseParsing();
         testPrismDirectoryResolution(testDirectory);
         testLiveInstallAndNoUpdate(testDirectory);
         testOfflineFallback(testDirectory);
         System.out.println("ASTRAL INSTALLER SMOKE TEST PASSED");
      } finally {
         deleteRecursively(testDirectory);
      }
   }

   private static void testVersionComparison() {
      require(AstralInstaller.compareVersions("v2.3", "2.2") > 0, "version increment was not detected");
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
             {"uploader":{"login":"test"},"digest":"sha256:aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa","name":"astralvisuals-9.4.jar","browser_download_url":"https://github.com/itzminerr/AstralVisuals/releases/download/v9.4/astralvisuals-9.4.jar"}
           ]
         }
         """;
      AstralInstaller.Release release = AstralInstaller.parseRelease(json);
      require("v9.4".equals(release.version()), "release tag was not parsed");
      require(release.assets().size() == 2, "release assets were not parsed");
      require(release.assets().get(1).digest().startsWith("sha256:"), "asset digest was not parsed");
      System.out.println("[OK] Dependency-free GitHub JSON parsing");
   }

   private static void testPrismDirectoryResolution(Path testDirectory) {
      Path minecraftDirectory = testDirectory.resolve("Prism Instance").resolve(".minecraft");
      Path resolved = AstralInstaller.resolveModsDirectory(null, Map.of("INST_MC_DIR", minecraftDirectory.toString()), testDirectory);
      require(resolved.equals(minecraftDirectory.resolve("mods").toAbsolutePath().normalize()), "INST_MC_DIR was not honored");
      Path explicit = testDirectory.resolve("custom mods");
      require(
         AstralInstaller.resolveModsDirectory(explicit, Map.of("INST_MC_DIR", minecraftDirectory.toString()), testDirectory)
            .equals(explicit.toAbsolutePath().normalize()),
         "explicit mods directory did not take precedence"
      );
      System.out.println("[OK] PrismLauncher and explicit mods directory resolution");
   }

   private static void testLiveInstallAndNoUpdate(Path testDirectory) throws Exception {
      Path modsDirectory = testDirectory.resolve("live").resolve("mods");
      Files.createDirectories(modsDirectory);
      createClientJar(modsDirectory.resolve("astralvisuals-0.1.jar"), "0.1");
      createClientJar(modsDirectory.resolve("astralvisuals-0.2.jar"), "0.2");

      AstralInstaller.UpdateResult installed = AstralInstaller.update(modsDirectory, AstralInstaller.LATEST_RELEASE);
      require(installed.updated(), "live GitHub client was not installed");
      require("0.2".equals(installed.previousVersion()), "newest installed version was not selected");
      require(Files.isRegularFile(installed.installedPath()), "downloaded client is missing");
      require(AstralInstaller.discoverClientJars(modsDirectory).size() == 1, "old client versions were not removed");

      AstralInstaller.UpdateResult repeated = AstralInstaller.update(modsDirectory, AstralInstaller.LATEST_RELEASE);
      require(!repeated.updated(), "current release was downloaded twice");
      require(installed.version().equals(repeated.version()), "installed version changed during the second check");
      System.out.println("[OK] Live GitHub install before launch and no-op second check: " + installed.version());
   }

   private static void testOfflineFallback(Path testDirectory) throws Exception {
      Path modsDirectory = testDirectory.resolve("offline").resolve("mods");
      Files.createDirectories(modsDirectory);
      createClientJar(modsDirectory.resolve("astralvisuals-1.0.jar"), "1.0");
      ByteArrayOutputStream output = new ByteArrayOutputStream();
      ByteArrayOutputStream errors = new ByteArrayOutputStream();
      int exit = AstralInstaller.run(
         new String[] {"--mods-dir", modsDirectory.toString()},
         Map.of("ASTRAL_RELEASE_API", "http://127.0.0.1:1/releases/latest"),
         testDirectory,
         new PrintStream(output, true, StandardCharsets.UTF_8),
         new PrintStream(errors, true, StandardCharsets.UTF_8)
      );
      require(exit == 0, "an installed client must remain launchable when GitHub is unavailable");
      require(AstralInstaller.discoverClientJars(modsDirectory).size() == 1, "offline fallback modified the installed client");
      System.out.println("[OK] Existing client remains safe when an update check cannot replace it");
   }

   private static void createClientJar(Path path, String version) throws Exception {
      try (JarOutputStream output = new JarOutputStream(Files.newOutputStream(path))) {
         output.putNextEntry(new JarEntry("fabric.mod.json"));
         output.write(("{\"schemaVersion\":1,\"id\":\"astralvisuals\",\"version\":\"" + version + "\"}").getBytes(StandardCharsets.UTF_8));
         output.closeEntry();
      }
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
