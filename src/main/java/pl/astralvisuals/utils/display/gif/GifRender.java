package pl.astralvisuals.utils.display.gif;

import net.minecraft.class_2960;
import net.minecraft.class_310;
import net.minecraft.class_4587;
import pl.astralvisuals.utils.display.shape.ShapeProperties;
import pl.astralvisuals.utils.display.shape.implement.Image;
import pl.astralvisuals.utils.math.frame.FrameRateCounter;

public class GifRender {
   private int currentFrame = 0;
   private float frameTime = 0.0F;
   private final float frameDuration = 0.0015F;
   private final String[] frames;
   private final Image image;

   public GifRender(String path, int frameCount) {
      this.frames = new String[frameCount];

      for (int i = 0; i < frameCount; i++) {
         this.frames[i] = String.format("%s/%05d.png", path, i + 1);
      }

      this.image = new Image();
   }

   public void render(class_4587 matrix, float x, float y, float width, float height) {
      if (class_310.method_1551().method_1569()) {
         this.frameTime = this.frameTime + (FrameRateCounter.INSTANCE.getFps() > 0 ? 1.0F / FrameRateCounter.INSTANCE.getFps() : 0.006F);
         if (this.frameTime >= 0.0015F) {
            this.currentFrame = (this.currentFrame + 1) % this.frames.length;
            this.frameTime = 0.0F;
         }

         class_2960 frameId = class_2960.method_60654(this.frames[this.currentFrame]);
         if (!class_310.method_1551().method_1478().method_14486(frameId).isEmpty()) {
            this.image.setTexture(this.frames[this.currentFrame]).render(ShapeProperties.create(matrix, x, y, width, height).color(-1).build());
         }
      }
   }
}
