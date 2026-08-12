package pl.astralvisuals.utils.client.window;

import com.sun.jna.Memory;
import com.sun.jna.Native;
import com.sun.jna.Pointer;
import com.sun.jna.platform.win32.WinDef.HWND;
import com.sun.jna.win32.StdCallLibrary;
import java.io.InputStream;
import net.minecraft.class_1011;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWImage;
import org.lwjgl.glfw.GLFWNativeWin32;
import org.lwjgl.glfw.GLFWImage.Buffer;
import org.lwjgl.system.MemoryUtil;

public class WindowStyle {
   public static void setDarkMode(long windowHandle) {
      String os = System.getProperty("os.name").toLowerCase();
      if (!os.contains("linux")) {
         long hwnd = GLFWNativeWin32.glfwGetWin32Window(windowHandle);
         HWND hwndJna = new HWND(new Pointer(hwnd));
         int darkModeAttribute = 20;
         Memory darkModeEnabled = new Memory(4L);
         darkModeEnabled.setInt(0L, 1);
         WindowStyle.DwmApi.INSTANCE.DwmSetWindowAttribute(hwndJna, darkModeAttribute, darkModeEnabled, 4);
      }
   }

   public static void setMinecraftIcon(long windowHandle) {
      try {
         try (InputStream stream = WindowStyle.class.getClassLoader().getResourceAsStream("assets/minecraft/icon.png")) {
            if (stream != null) {
               class_1011 image = class_1011.method_4309(stream);

               try {
                  Buffer icons = GLFWImage.malloc(1);

                  try {
                     icons.position(0);
                     icons.width(image.method_4307());
                     icons.height(image.method_4323());
                     icons.pixels(MemoryUtil.memByteBuffer(image.field_4988, image.method_4307() * image.method_4323() * 4));
                     GLFW.glfwSetWindowIcon(windowHandle, icons);
                  } finally {
                     icons.free();
                  }
               } catch (Throwable var15) {
                  if (image != null) {
                     try {
                        image.close();
                     } catch (Throwable var13) {
                        var15.addSuppressed(var13);
                     }
                  }

                  throw var15;
               }

               if (image != null) {
                  image.close();
               }

               return;
            }
         }
      } catch (Exception var17) {
      }
   }

   public interface DwmApi extends StdCallLibrary {
      WindowStyle.DwmApi INSTANCE = (WindowStyle.DwmApi)Native.loadLibrary("dwmapi", WindowStyle.DwmApi.class);

      int DwmSetWindowAttribute(HWND var1, int var2, Pointer var3, int var4);
   }
}
