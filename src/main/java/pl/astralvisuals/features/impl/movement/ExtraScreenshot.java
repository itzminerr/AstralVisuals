package pl.astralvisuals.features.impl.movement;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Locale;
import java.util.concurrent.locks.LockSupport;
import javax.imageio.ImageIO;
import com.sun.jna.Native;
import com.sun.jna.Pointer;
import com.sun.jna.platform.win32.BaseTSD;
import com.sun.jna.win32.StdCallLibrary;
import pl.astralvisuals.features.module.Module;
import pl.astralvisuals.features.module.ModuleCategory;
import pl.astralvisuals.features.module.setting.implement.BooleanSetting;
import pl.astralvisuals.utils.client.Instance;
import pl.astralvisuals.utils.client.logs.Logger;

public final class ExtraScreenshot extends Module {
   private final BooleanSetting autoDelete = new BooleanSetting(
      "Автоматически удалять скриншот",
      "Удалять файл скриншота после копирования в буфер обмена"
   ).setValue(false);

   public ExtraScreenshot() {
      super("ExtraScreenshot", "Extra Screenshot", ModuleCategory.PLAYER);
      this.setup(this.autoDelete);
   }

   public static ExtraScreenshot getInstance() {
      return Instance.get(ExtraScreenshot.class);
   }

   public boolean copyToClipboard(File screenshot) {
      if (!this.isState() || screenshot == null || !screenshot.isFile()) {
         return false;
      }

      try {
         BufferedImage image = ImageIO.read(screenshot);
         if (image == null) {
            Logger.warn("ExtraScreenshot could not decode " + screenshot.getAbsolutePath());
            return false;
         }

         if (!System.getProperty("os.name", "").toLowerCase(Locale.ROOT).contains("win")) {
            Logger.warn("ExtraScreenshot image clipboard is currently supported only on Windows");
            return false;
         }

         WindowsClipboard.copy(image);
         if (this.autoDelete.isValue()) {
            Files.deleteIfExists(screenshot.toPath());
         }
         return true;
      } catch (Exception exception) {
         Logger.warn("ExtraScreenshot could not copy the screenshot", exception);
         return false;
      }
   }

   private static final class WindowsClipboard {
      private static final int CF_DIB = 8;
      private static final int GMEM_MOVEABLE = 0x0002;
      private static final int BITMAP_INFO_HEADER_SIZE = 40;
      private static final int OPEN_ATTEMPTS = 10;
      private static final long OPEN_RETRY_DELAY_NANOS = 10_000_000L;
      private static final User32Clipboard USER32 = Native.load("user32", User32Clipboard.class);
      private static final Kernel32Clipboard KERNEL32 = Native.load("kernel32", Kernel32Clipboard.class);

      private static void copy(BufferedImage image) throws IOException {
         boolean clipboardOpen = false;
         Pointer globalMemory = null;
         boolean ownershipTransferred = false;

         try {
            for (int attempt = 0; attempt < OPEN_ATTEMPTS && !clipboardOpen; attempt++) {
               clipboardOpen = USER32.OpenClipboard(null);
               if (!clipboardOpen) {
                  LockSupport.parkNanos(OPEN_RETRY_DELAY_NANOS);
               }
            }

            if (!clipboardOpen) {
               throw new IOException("OpenClipboard failed with error " + Native.getLastError());
            }
            if (!USER32.EmptyClipboard()) {
               throw new IOException("EmptyClipboard failed with error " + Native.getLastError());
            }

            int width = image.getWidth();
            int height = image.getHeight();
            long pixelBytes = Math.multiplyExact(Math.multiplyExact((long)width, (long)height), 4L);
            long allocationSize = Math.addExact(BITMAP_INFO_HEADER_SIZE, pixelBytes);
            globalMemory = KERNEL32.GlobalAlloc(GMEM_MOVEABLE, new BaseTSD.SIZE_T(allocationSize));
            if (globalMemory == null) {
               throw new IOException("GlobalAlloc failed with error " + Native.getLastError());
            }

            Pointer memory = KERNEL32.GlobalLock(globalMemory);
            if (memory == null) {
               throw new IOException("GlobalLock failed with error " + Native.getLastError());
            }

            try {
               writeDib(memory, image, width, height, pixelBytes);
            } finally {
               KERNEL32.GlobalUnlock(globalMemory);
            }

            if (USER32.SetClipboardData(CF_DIB, globalMemory) == null) {
               throw new IOException("SetClipboardData failed with error " + Native.getLastError());
            }
            ownershipTransferred = true;
         } finally {
            if (!ownershipTransferred && globalMemory != null) {
               KERNEL32.GlobalFree(globalMemory);
            }
            if (clipboardOpen) {
               USER32.CloseClipboard();
            }
         }
      }

      private static void writeDib(Pointer memory, BufferedImage image, int width, int height, long pixelBytes) {
         memory.setInt(0L, BITMAP_INFO_HEADER_SIZE);
         memory.setInt(4L, width);
         memory.setInt(8L, height);
         memory.setShort(12L, (short)1);
         memory.setShort(14L, (short)32);
         memory.setInt(16L, 0);
         memory.setInt(20L, Math.toIntExact(pixelBytes));
         memory.setInt(24L, 0);
         memory.setInt(28L, 0);
         memory.setInt(32L, 0);
         memory.setInt(36L, 0);

         int[] rgbRow = new int[width];
         byte[] dibRow = new byte[Math.multiplyExact(width, 4)];
         for (int row = 0; row < height; row++) {
            image.getRGB(0, height - row - 1, width, 1, rgbRow, 0, width);
            for (int x = 0; x < width; x++) {
               int rgb = rgbRow[x];
               int offset = x * 4;
               dibRow[offset] = (byte)rgb;
               dibRow[offset + 1] = (byte)(rgb >>> 8);
               dibRow[offset + 2] = (byte)(rgb >>> 16);
               dibRow[offset + 3] = 0;
            }
            memory.write(BITMAP_INFO_HEADER_SIZE + (long)row * dibRow.length, dibRow, 0, dibRow.length);
         }
      }

      private WindowsClipboard() {
      }

      private interface User32Clipboard extends StdCallLibrary {
         boolean OpenClipboard(Pointer owner);

         boolean EmptyClipboard();

         Pointer SetClipboardData(int format, Pointer memory);

         boolean CloseClipboard();
      }

      private interface Kernel32Clipboard extends StdCallLibrary {
         Pointer GlobalAlloc(int flags, BaseTSD.SIZE_T bytes);

         Pointer GlobalLock(Pointer memory);

         boolean GlobalUnlock(Pointer memory);

         Pointer GlobalFree(Pointer memory);
      }
   }
}
