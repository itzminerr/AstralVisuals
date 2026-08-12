package pl.astralvisuals.utils.client.sound;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.util.Locale;
import java.util.concurrent.CompletableFuture;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.FloatControl;
import javax.sound.sampled.LineEvent;
import net.minecraft.class_4228;
import net.minecraft.class_2378;
import net.minecraft.class_2960;
import net.minecraft.class_3414;
import net.minecraft.class_3419;
import net.minecraft.class_7923;
import pl.astralvisuals.utils.display.interfaces.QuickImports;
import pl.astralvisuals.utils.interactions.interact.PlayerInteractionHelper;
import org.lwjgl.system.MemoryUtil;

public final class SoundManager implements QuickImports {
   public static class_3414 OPEN_GUI = class_3414.method_47908(class_2960.method_60654("minecraft:gui_open"));
   public static class_3414 CLOSE_GUI = class_3414.method_47908(class_2960.method_60654("minecraft:gui_close"));
   public static class_3414 ENABLE_MODULE = class_3414.method_47908(class_2960.method_60654("minecraft:module_enable"));
   public static class_3414 DISABLE_MODULE = class_3414.method_47908(class_2960.method_60654("minecraft:module_disable"));
   public static class_3414 CATEGORY_CLICK = class_3414.method_47908(class_2960.method_60654("minecraft:category_click"));
   public static class_3414 ORTHODOX = class_3414.method_47908(class_2960.method_60654("minecraft:kolokolnia_kill"));

   public static void init() {
      class_2378.method_10230(class_7923.field_41172, OPEN_GUI.comp_3319(), OPEN_GUI);
      class_2378.method_10230(class_7923.field_41172, CLOSE_GUI.comp_3319(), CLOSE_GUI);
      class_2378.method_10230(class_7923.field_41172, ENABLE_MODULE.comp_3319(), ENABLE_MODULE);
      class_2378.method_10230(class_7923.field_41172, DISABLE_MODULE.comp_3319(), DISABLE_MODULE);
      class_2378.method_10230(class_7923.field_41172, CATEGORY_CLICK.comp_3319(), CATEGORY_CLICK);
      class_2378.method_10230(class_7923.field_41172, ORTHODOX.comp_3319(), ORTHODOX);
   }

   public static void playSound(class_3414 sound) {
      playSound(sound, 1.0F, 1.0F);
   }

   public static void playSound(class_3414 sound, float volume, float pitch) {
      if (!PlayerInteractionHelper.nullCheck()) {
         mc.field_1687.method_8396(mc.field_1724, mc.field_1724.method_24515(), sound, class_3419.field_15245, volume, pitch);
      }
   }

   /** Асинхронно проигрывает пользовательский WAV/AIFF/AU или OGG без ресурс-пака. */
   public static void playFile(File file, float volume) {
      if (file == null || !file.isFile() || volume <= 0.0F) {
         return;
      }

      CompletableFuture.runAsync(() -> {
         try {
            String name = file.getName().toLowerCase(Locale.ROOT);
            if (name.endsWith(".ogg")) {
               playOgg(file, volume);
            } else if (name.endsWith(".wav") || name.endsWith(".aiff") || name.endsWith(".aif") || name.endsWith(".au")) {
               playJavaSound(file, volume);
            }
         } catch (Exception ignored) {
         }
      });
   }

   private static void playOgg(File file, float volume) throws Exception {
      AudioFormat format;
      byte[] pcm;
      try (InputStream input = Files.newInputStream(file.toPath()); class_4228 ogg = new class_4228(input); ByteArrayOutputStream output = new ByteArrayOutputStream()) {
         format = ogg.method_19719();
         while (true) {
            ByteBuffer buffer = ogg.method_19720(64 * 1024);
            int length = buffer.remaining();
            if (length > 0) {
               byte[] chunk = new byte[length];
               buffer.get(chunk);
               output.write(chunk);
            }

            MemoryUtil.memFree(buffer);
            if (length == 0) {
               break;
            }
         }

         pcm = output.toByteArray();
      }

      playPcm(format, pcm, volume);
   }

   private static void playJavaSound(File file, float volume) throws Exception {
      try (AudioInputStream source = AudioSystem.getAudioInputStream(file)) {
         AudioFormat original = source.getFormat();
         AudioFormat decoded = new AudioFormat(
            AudioFormat.Encoding.PCM_SIGNED,
            original.getSampleRate(),
            16,
            original.getChannels(),
            original.getChannels() * 2,
            original.getSampleRate(),
            false
         );
         try (AudioInputStream pcm = AudioSystem.getAudioInputStream(decoded, source)) {
            playPcm(decoded, pcm.readAllBytes(), volume);
         }
      }
   }

   private static void playPcm(AudioFormat format, byte[] data, float volume) throws Exception {
      if (data.length == 0) {
         return;
      }

      Clip clip = AudioSystem.getClip();
      clip.open(new AudioInputStream(new ByteArrayInputStream(data), format, data.length / Math.max(1, format.getFrameSize())));
      if (clip.isControlSupported(FloatControl.Type.MASTER_GAIN)) {
         FloatControl gain = (FloatControl)clip.getControl(FloatControl.Type.MASTER_GAIN);
         float db = (float)(20.0 * Math.log10(Math.max(0.0001F, Math.min(1.0F, volume))));
         gain.setValue(Math.max(gain.getMinimum(), Math.min(gain.getMaximum(), db)));
      }

      clip.addLineListener(event -> {
         if (event.getType() == LineEvent.Type.STOP) {
            event.getLine().close();
         }
      });
      clip.start();
   }

   private SoundManager() {
      throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
   }
}
