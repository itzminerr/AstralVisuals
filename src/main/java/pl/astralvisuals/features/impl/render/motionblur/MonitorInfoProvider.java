package pl.astralvisuals.features.impl.render.motionblur;

import net.minecraft.class_310;
import org.lwjgl.PointerBuffer;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWVidMode;

// Порт MonitorInfoProvider из EvaWare: частота обновления монитора, на котором сейчас окно.
public class MonitorInfoProvider {
   private static long lastMonitorHandle = 0L;
   private static int lastRefreshRate = 60;
   private static long lastCheckTime = 0L;
   private static final long CHECK_INTERVAL_NS = 1_000_000_000L;

   public static void updateDisplayInfo() {
      long now = System.nanoTime();
      if (now - lastCheckTime < CHECK_INTERVAL_NS) {
         return;
      }

      lastCheckTime = now;
      class_310 client = class_310.method_1551();
      if (client == null || client.method_22683() == null) {
         return;
      }

      long window = client.method_22683().method_4490();
      long monitor = GLFW.glfwGetWindowMonitor(window);
      if (monitor == 0L) {
         monitor = getMonitorFromWindowPosition(window);
      }

      if (monitor != lastMonitorHandle) {
         lastRefreshRate = detectRefreshRateFromMonitor(monitor);
         lastMonitorHandle = monitor;
      }
   }

   public static int getRefreshRate() {
      return lastRefreshRate;
   }

   private static long getMonitorFromWindowPosition(long window) {
      int[] winX = new int[1];
      int[] winY = new int[1];
      int[] winW = new int[1];
      int[] winH = new int[1];
      GLFW.glfwGetWindowPos(window, winX, winY);
      GLFW.glfwGetWindowSize(window, winW, winH);
      int centerX = winX[0] + winW[0] / 2;
      int centerY = winY[0] + winH[0] / 2;

      long result = GLFW.glfwGetPrimaryMonitor();
      PointerBuffer monitors = GLFW.glfwGetMonitors();
      if (monitors != null) {
         for (int i = 0; i < monitors.limit(); i++) {
            long m = monitors.get(i);
            int[] mx = new int[1];
            int[] my = new int[1];
            GLFW.glfwGetMonitorPos(m, mx, my);
            GLFWVidMode mode = GLFW.glfwGetVideoMode(m);
            if (mode == null) {
               continue;
            }

            if (centerX >= mx[0] && centerX < mx[0] + mode.width() && centerY >= my[0] && centerY < my[0] + mode.height()) {
               result = m;
               break;
            }
         }
      }

      return result;
   }

   private static int detectRefreshRateFromMonitor(long monitor) {
      GLFWVidMode vidMode = GLFW.glfwGetVideoMode(monitor);
      return vidMode != null ? vidMode.refreshRate() : 60;
   }
}
