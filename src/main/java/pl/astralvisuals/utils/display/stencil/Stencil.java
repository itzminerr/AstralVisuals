package pl.astralvisuals.utils.display.stencil;

import net.minecraft.class_276;
import org.lwjgl.opengl.EXTFramebufferObject;
import org.lwjgl.opengl.GL11;
import pl.astralvisuals.utils.display.interfaces.QuickImports;

public final class Stencil implements QuickImports {
   public static void push() {
      class_276 framebuffer = mc.method_1522();
      if (framebuffer.field_1474 > -1) {
         mc.method_1522().method_1235(false);
         EXTFramebufferObject.glDeleteRenderbuffersEXT(framebuffer.field_1474);
         int stencilDepthBufferID = EXTFramebufferObject.glGenRenderbuffersEXT();
         EXTFramebufferObject.glBindRenderbufferEXT(36161, stencilDepthBufferID);
         EXTFramebufferObject.glRenderbufferStorageEXT(36161, 34041, window.method_4480(), window.method_4507());
         EXTFramebufferObject.glFramebufferRenderbufferEXT(36160, 36128, 36161, stencilDepthBufferID);
         EXTFramebufferObject.glFramebufferRenderbufferEXT(36160, 36096, 36161, stencilDepthBufferID);
         framebuffer.field_1474 = -1;
      }

      GL11.glStencilMask(255);
      GL11.glClear(1024);
      GL11.glEnable(2960);
      GL11.glStencilFunc(519, 1, 1);
      GL11.glStencilOp(7681, 7681, 7681);
      GL11.glDisable(2929);
      GL11.glColorMask(false, false, false, false);
   }

   public static void read(int ref) {
      GL11.glColorMask(true, true, true, true);
      GL11.glStencilFunc(514, ref, 1);
      GL11.glStencilOp(7680, 7680, 7680);
   }

   public static void pop() {
      GL11.glDisable(2960);
      GL11.glEnable(2929);
   }

   private Stencil() {
      throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
   }
}
