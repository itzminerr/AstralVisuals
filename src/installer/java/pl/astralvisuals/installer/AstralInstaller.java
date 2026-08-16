package pl.astralvisuals.installer;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
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
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.entrypoint.PreLaunchEntrypoint;
import net.fabricmc.loader.impl.launch.knot.Knot;
import org.spongepowered.asm.mixin.Mixins;

/**
 * Zero-configuration Fabric loader for AstralVisuals.
 *
 * <p>The loader is the only file users put in {@code mods}. During Fabric's
 * pre-launch phase it synchronously checks GitHub, caches the current client
 * outside {@code mods}, adds that client to Knot's class path and registers its
 * mixins before Minecraft classes are loaded.</p>
 */
public final class AstralInstaller implements PreLaunchEntrypoint, ModInitializer {
   static final URI LATEST_RELEASE = URI.create("https://api.github.com/repos/itzminerr/AstralVisuals/releases/latest");
   static final String RELEASE_API_PROPERTY = "astralinstaller.releaseApi";
   static final String CLIENT_VERSION_PROPERTY = "astralvisuals.client.version";

   private static final String MOD_ID = "astralvisuals";
   private static final String CACHE_DIRECTORY = ".astralinstaller";
   private static final String CLIENT_FILE = "client.jar";
   private static final String CLIENT_ENTRYPOINT = "pl.astralvisuals.Force";
   private static final String MIXIN_CONFIGURATION = "mixins.json";
   private static final String REPOSITORY_RELEASE_PATH = "/itzminerr/AstralVisuals/releases/download/";
   private static final long MAX_DOWNLOAD_BYTES = 250L * 1024L * 1024L;
   private static final int MAX_METADATA_BYTES = 1024 * 1024;

   private static volatile boolean payloadReady;

   @Override
   public void onPreLaunch() {
      if (FabricLoader.getInstance().isModLoaded(MOD_ID)) {
         info("a regular AstralVisuals mod is already installed; dynamic loading is disabled");
         return;
      }

      Path cacheDirectory = FabricLoader.getInstance().getGameDir().resolve(CACHE_DIRECTORY).toAbsolutePath().normalize();
      Path clientPath;
      try {
         UpdateResult result = update(cacheDirectory, releaseUri());
         clientPath = result.client().path();
         if (result.updated()) {
            info("downloaded AstralVisuals " + result.client().version());
         } else {
            info("AstralVisuals " + result.client().version() + " is up to date");
         }
      } catch (Exception updateFailure) {
         Optional<ClientJar> cached = readClientJar(cacheDirectory.resolve(CLIENT_FILE));
         if (cached.isEmpty()) {
            throw new IllegalStateException(
               "AstralInstaller could not download AstralVisuals and no valid cached client exists",
               updateFailure
            );
         }
         clientPath = cached.get().path();
         warn("GitHub check failed; using cached AstralVisuals " + cached.get().version() + ": " + message(updateFailure));
      }

      try {
         ClientJar client = verifyClientJar(clientPath, null);
         Knot.getLauncher().addToClassPath(client.path());
         attachPayloadResources(client.path());
         Mixins.addConfiguration(MIXIN_CONFIGURATION);
         reloadMixinTransformer();
         System.setProperty(CLIENT_VERSION_PROPERTY, client.version());
         payloadReady = true;
         info("AstralVisuals " + client.version() + " attached to the current Minecraft launch");
      } catch (Exception exception) {
         throw new IllegalStateException("AstralInstaller could not attach the downloaded client", exception);
      }
   }

   /** Runs at Fabric's normal mod-initialization point, after the payload was attached in pre-launch. */
   @Override
   public void onInitialize() {
      if (!payloadReady) {
         return;
      }

      try {
         Class<?> entrypointClass = getClass().getClassLoader().loadClass(CLIENT_ENTRYPOINT);
         Object entrypoint = entrypointClass.getDeclaredConstructor().newInstance();
         if (!(entrypoint instanceof ModInitializer initializer)) {
            throw new IllegalStateException(CLIENT_ENTRYPOINT + " does not implement ModInitializer");
         }
         initializer.onInitialize();
      } catch (Exception exception) {
         throw new IllegalStateException("AstralInstaller could not initialize AstralVisuals", exception);
      }
   }

   private static URI releaseUri() {
      String override = System.getProperty(RELEASE_API_PROPERTY);
      return override == null || override.isBlank() ? LATEST_RELEASE : URI.create(override);
   }

   static UpdateResult update(Path cacheDirectory, URI releaseUri) throws Exception {
      Path normalizedCache = cacheDirectory.toAbsolutePath().normalize();
      Files.createDirectories(normalizedCache);
      Path target = normalizedCache.resolve(CLIENT_FILE);
      Optional<ClientJar> current = readClientJar(target);

      HttpClient client = HttpClient.newBuilder()
         .connectTimeout(Duration.ofSeconds(10))
         .followRedirects(HttpClient.Redirect.NORMAL)
         .build();
      Release release = requestLatestRelease(client, releaseUri);
      ReleaseAsset asset = selectClientAsset(release);
      validateAsset(asset);

      if (current.isPresent() && compareVersions(current.get().version(), release.version()) >= 0) {
         return new UpdateResult(false, current.get());
      }

      Path pending = Files.createTempFile(normalizedCache, ".client-download-", ".jar.part");
      try {
         download(client, asset.url(), pending);
         ClientJar downloaded = verifyClientJar(pending, release.version());
         verifyDigestIfPresent(pending, asset.digest());
         installDownloadedJar(pending, target);
         return new UpdateResult(true, new ClientJar(target, downloaded.version()));
      } finally {
         Files.deleteIfExists(pending);
      }
   }

   private static Optional<ClientJar> readClientJar(Path path) {
      if (!Files.isRegularFile(path)) {
         return Optional.empty();
      }
      try {
         return Optional.of(verifyClientJar(path, null));
      } catch (Exception ignored) {
         return Optional.empty();
      }
   }

   static ClientJar verifyClientJar(Path path, String expectedVersion) throws IOException {
      Path normalized = path.toAbsolutePath().normalize();
      try (JarFile jar = new JarFile(normalized.toFile())) {
         String metadata = readSmallEntry(jar, "fabric.mod.json");
         String id = jsonStringField(metadata, "id");
         String version = jsonStringField(metadata, "version");
         if (!MOD_ID.equals(id) || version == null || version.isBlank()) {
            throw new IOException("downloaded file is not an AstralVisuals client");
         }
         String normalizedVersion = normalizeVersion(version);
         if (expectedVersion != null && !normalizeVersion(expectedVersion).equals(normalizedVersion)) {
            throw new IOException("client version does not match the GitHub Release tag");
         }
         requireEntry(jar, "pl/astralvisuals/Force.class");
         requireEntry(jar, MIXIN_CONFIGURATION);
         return new ClientJar(normalized, normalizedVersion);
      }
   }

   private static String readSmallEntry(JarFile jar, String name) throws IOException {
      JarEntry entry = jar.getJarEntry(name);
      if (entry == null || entry.getSize() > MAX_METADATA_BYTES) {
         throw new IOException("client is missing " + name);
      }
      try (InputStream input = jar.getInputStream(entry)) {
         byte[] bytes = input.readNBytes(MAX_METADATA_BYTES + 1);
         if (bytes.length > MAX_METADATA_BYTES) {
            throw new IOException(name + " is unexpectedly large");
         }
         return new String(bytes, StandardCharsets.UTF_8);
      }
   }

   private static void requireEntry(JarFile jar, String name) throws IOException {
      if (jar.getJarEntry(name) == null) {
         throw new IOException("client is missing " + name);
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
      String path = asset.url().getPath();
      if (!"https".equalsIgnoreCase(asset.url().getScheme())
         || !"github.com".equalsIgnoreCase(asset.url().getHost())
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

   static void verifyDigestIfPresent(Path path, String digest) throws Exception {
      if (digest == null || digest.isBlank()) {
         return;
      }
      String lowerDigest = digest.toLowerCase(Locale.ROOT);
      if (!lowerDigest.matches("sha256:[0-9a-f]{64}")) {
         throw new IOException("GitHub supplied an invalid SHA-256 digest");
      }
      MessageDigest sha256 = MessageDigest.getInstance("SHA-256");
      try (InputStream input = Files.newInputStream(path)) {
         byte[] buffer = new byte[64 * 1024];
         int read;
         while ((read = input.read(buffer)) >= 0) {
            sha256.update(buffer, 0, read);
         }
      }
      String actual = HexFormat.of().formatHex(sha256.digest());
      String expected = lowerDigest.substring("sha256:".length());
      if (!MessageDigest.isEqual(expected.getBytes(StandardCharsets.US_ASCII), actual.getBytes(StandardCharsets.US_ASCII))) {
         throw new IOException("GitHub SHA-256 verification failed");
      }
   }

   private static void installDownloadedJar(Path pending, Path target) throws IOException {
      try {
         Files.move(pending, target, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
      } catch (AtomicMoveNotSupportedException ignored) {
         Files.move(pending, target, StandardCopyOption.REPLACE_EXISTING);
      }
   }

   /**
    * Adds the payload as a second root of the installer mod so Fabric Resource Loader
    * exposes its assets, shaders and sounds in the normal {@code fabric} resource pack.
    */
   private static void attachPayloadResources(Path clientPath) throws ReflectiveOperationException {
      Object container = FabricLoader.getInstance()
         .getModContainer("astralinstaller")
         .orElseThrow(() -> new IllegalStateException("AstralInstaller mod container is missing"));
      Field codeSourcePaths = container.getClass().getDeclaredField("codeSourcePaths");
      codeSourcePaths.setAccessible(true);
      @SuppressWarnings("unchecked")
      List<Path> existing = (List<Path>)codeSourcePaths.get(container);
      List<Path> combined = new ArrayList<>(existing);
      if (!combined.contains(clientPath)) {
         combined.add(clientPath);
      }
      codeSourcePaths.set(container, List.copyOf(combined));

      Field roots = container.getClass().getDeclaredField("roots");
      roots.setAccessible(true);
      roots.set(container, null);
   }

   /** Makes a configuration added after Mixin bootstrap visible before the first game class is transformed. */
   private static void reloadMixinTransformer() throws ReflectiveOperationException {
      ClassLoader classLoader = AstralInstaller.class.getClassLoader();
      Object delegate = readField(classLoader, "delegate");
      Object transformer = readField(delegate, "mixinTransformer");
      if (transformer == null || !"org.spongepowered.asm.mixin.transformer.MixinTransformer".equals(transformer.getClass().getName())) {
         throw new IllegalStateException("unsupported Mixin transformer: " + (transformer == null ? "null" : transformer.getClass().getName()));
      }
      Object processor = readField(transformer, "processor");
      Field transformedCount = processor.getClass().getDeclaredField("transformedCount");
      transformedCount.setAccessible(true);
      transformedCount.setInt(processor, 0);
   }

   private static Object readField(Object owner, String name) throws ReflectiveOperationException {
      Field field = owner.getClass().getDeclaredField(name);
      field.setAccessible(true);
      return field.get(owner);
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
      Pattern pattern = Pattern.compile("\\\"" + Pattern.quote(field) + "\\\"\\s*:\\s*\\\"((?:\\\\.|[^\\\"\\\\])*)\\\"");
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
      int end = start < 0 ? -1 : findMatching(json, start, '[', ']');
      if (end < 0) {
         throw new IOException("GitHub Release has an invalid " + field + " array");
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
         } else if (value == '"') {
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

   private static void info(String message) {
      System.out.println("[AstralInstaller] " + message);
   }

   private static void warn(String message) {
      System.err.println("[AstralInstaller] " + message);
   }

   record ClientJar(Path path, String version) {
   }

   record Release(String version, List<ReleaseAsset> assets) {
   }

   record ReleaseAsset(String name, URI url, String digest) {
   }

   record UpdateResult(boolean updated, ClientJar client) {
   }
}
