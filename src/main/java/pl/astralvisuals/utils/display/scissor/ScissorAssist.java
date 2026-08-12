package pl.astralvisuals.utils.display.scissor;

import com.mojang.blaze3d.systems.RenderSystem;
import java.util.Stack;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import pl.astralvisuals.utils.display.interfaces.QuickImports;
import pl.astralvisuals.utils.display.scale.UiScale;

public class ScissorAssist implements QuickImports {
   private final Pool<ScissorAssist.Scissor> scissorPool = new Pool<>(ScissorAssist.Scissor::new);
   private final Stack<ScissorAssist.Scissor> scissorStack = new Stack<>();

   public void push(Matrix4f matrix4f, float x, float y, float width, float height) {
      ScissorAssist.Scissor currentScissor = this.scissorPool.get();
      boolean fullScreen = x <= 0.0F && y <= 0.0F && width >= window.method_4486() && height >= window.method_4502();
      if (!fullScreen) {
         x = UiScale.x(x);
         y = UiScale.y(y);
         width = UiScale.size(width);
         height = UiScale.size(height);
      }

      Vector3f pos = matrix4f.transformPosition(x, y, 0.0F, new Vector3f());
      Vector3f size = matrix4f.getScale(new Vector3f()).mul(width, height, 0.0F);
      currentScissor.set(pos.x, pos.y, size.x, size.y);
      if (!this.scissorStack.isEmpty()) {
         ScissorAssist.Scissor parent = this.scissorStack.peek();
         currentScissor.intersect(parent);
      }

      this.scissorStack.push(currentScissor);
      this.setScissor(currentScissor);
   }

   public void pop() {
      if (!this.scissorStack.isEmpty()) {
         this.scissorPool.free(this.scissorStack.pop());
         if (this.scissorStack.isEmpty()) {
            RenderSystem.disableScissor();
         } else {
            this.setScissor(this.scissorStack.peek());
         }
      }
   }

   private void setScissor(ScissorAssist.Scissor scissor) {
      int scaleFactor = (int)window.method_4495();
      int x = scissor.x * scaleFactor;
      int y = window.method_4507() - (scissor.y * scaleFactor + scissor.height * scaleFactor);
      int width = scissor.width * scaleFactor;
      int height = scissor.height * scaleFactor;
      RenderSystem.enableScissor(x, y, width, height);
   }

   private static class Scissor {
      public int x;
      public int y;
      public int width;
      public int height;

      public void set(double x, double y, double width, double height) {
         this.x = Math.max(0, (int)Math.round(x));
         this.y = Math.max(0, (int)Math.round(y));
         this.width = Math.max(0, (int)Math.round(width));
         this.height = Math.max(0, (int)Math.round(height));
      }

      public void intersect(ScissorAssist.Scissor parent) {
         int x1 = Math.max(this.x, parent.x);
         int y1 = Math.max(this.y, parent.y);
         int x2 = Math.min(this.x + this.width, parent.x + parent.width);
         int y2 = Math.min(this.y + this.height, parent.y + parent.height);
         this.x = x1;
         this.y = y1;
         this.width = Math.max(0, x2 - x1);
         this.height = Math.max(0, y2 - y1);
      }

      ScissorAssist.Scissor copy() {
         ScissorAssist.Scissor newScissor = new ScissorAssist.Scissor();
         newScissor.set(this.x, this.y, this.width, this.height);
         return newScissor;
      }
   }
}
