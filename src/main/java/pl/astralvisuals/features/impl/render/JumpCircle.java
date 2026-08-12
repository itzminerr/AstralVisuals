package pl.astralvisuals.features.impl.render;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.class_243;
import net.minecraft.class_2960;
import net.minecraft.class_4184;
import net.minecraft.class_4587;
import net.minecraft.class_7833;
import net.minecraft.class_4587.class_4665;
import org.joml.Vector4i;
import pl.astralvisuals.events.player.JumpEvent;
import pl.astralvisuals.events.render.WorldRenderEvent;
import pl.astralvisuals.features.module.Module;
import pl.astralvisuals.features.module.ModuleCategory;
import pl.astralvisuals.features.module.setting.implement.ColorSetting;
import pl.astralvisuals.features.module.setting.implement.SliderSettings;
import pl.astralvisuals.utils.client.managers.event.EventHandler;
import pl.astralvisuals.utils.display.color.ColorAssist;
import pl.astralvisuals.utils.display.geometry.Render3D;
import pl.astralvisuals.utils.math.Counter;

public class JumpCircle extends Module {
   private final List<JumpCircle.Circle> circles = new ArrayList<>();
   private final class_2960 circleTexture = class_2960.method_60654("textures/circle2.png");
   private final ColorSetting colorSetting = new ColorSetting("Цвет", "Цвет круга").setColor(-1).presets(-1, -9659651, -7569409, -23178, -33925);
   private final SliderSettings maxSize = new SliderSettings("Макс размер", "Максимальный размер круга").setValue(2.5F).range(1.0F, 3.0F);
   private final SliderSettings speed = new SliderSettings("Скорость", "Скорость анимации").setValue(1000.0F).range(500.0F, 5000.0F);

   public JumpCircle() {
      super("JumpCircle", "Jump Circle", ModuleCategory.RENDER);
      this.setup(this.colorSetting, this.maxSize, this.speed);
   }

   @EventHandler
   public void onJump(JumpEvent event) {
      if (mc.field_1724 != null && event.getPlayer() == mc.field_1724) {
         class_243 pos = new class_243(mc.field_1724.method_23317(), Math.floor(mc.field_1724.method_23318()) + 0.001, mc.field_1724.method_23321());
         this.circles.add(new JumpCircle.Circle(pos, new Counter()));
      }
   }

   @EventHandler
   public void onWorldRender(WorldRenderEvent e) {
      this.circles.removeIf(circle -> circle.timer.passedMs((long)this.speed.getValue()));
      this.renderCircles();
   }

   private void renderCircles() {
      if (!this.circles.isEmpty()) {
         for (JumpCircle.Circle circle : this.circles) {
            this.renderSingleCircle(circle);
         }
      }
   }

   private void renderSingleCircle(JumpCircle.Circle circle) {
      float lifeTime = (float)circle.timer.getPassedTimeMs();
      float maxTime = this.speed.getValue();
      float progress = Math.min(lifeTime / maxTime, 1.0F);
      if (!(progress >= 1.0F)) {
         float easedProgress = this.bounceOut(progress);
         float scale = easedProgress * this.maxSize.getValue();
         float fadeInDuration = 0.15F;
         float glowStart = 0.65F;
         float fadeOutStart = 0.85F;
         float alpha;
         if (progress < fadeInDuration) {
            alpha = progress / fadeInDuration;
         } else if (progress >= fadeOutStart) {
            float fadeOutProgress = (progress - fadeOutStart) / (1.0F - fadeOutStart);
            alpha = 1.0F - fadeOutProgress;
            if (progress > glowStart) {
               float glowProgress = (progress - glowStart) / (fadeOutStart - glowStart);
               float glowPulse = (float)(Math.sin(glowProgress * Math.PI * 3.0) * 0.3 + 0.3);
               alpha += glowPulse * (1.0F - fadeOutProgress);
            }
         } else if (progress > glowStart) {
            float glowProgress = (progress - glowStart) / (fadeOutStart - glowStart);
            float glowPulse = (float)(Math.sin(glowProgress * Math.PI * 3.0) * 0.3 + 0.3);
            alpha = 1.0F + glowPulse;
         } else {
            alpha = 1.0F;
         }

         alpha = Math.max(0.0F, Math.min(1.0F, alpha));
         class_4184 camera = mc.method_1561().field_4686;
         class_243 cameraPos = camera.method_19326();
         class_243 circlePos = circle.pos();
         class_4587 matrixStack = new class_4587();
         matrixStack.method_22907(class_7833.field_40714.rotationDegrees(camera.method_19329()));
         matrixStack.method_22907(class_7833.field_40716.rotationDegrees(camera.method_19330() + 180.0F));
         matrixStack.method_22904(
            circlePos.field_1352 - cameraPos.field_1352, circlePos.field_1351 - cameraPos.field_1351, circlePos.field_1350 - cameraPos.field_1350
         );
         matrixStack.method_22907(class_7833.field_40716.rotationDegrees(-camera.method_19330()));
         matrixStack.method_22907(class_7833.field_40714.rotationDegrees(90.0F));
         class_4665 entry = matrixStack.method_23760();
         int color = ColorAssist.multAlpha(this.colorSetting.getColor(), alpha);
         Vector4i colors = new Vector4i(color, color, color, color);
         Render3D.drawTexture(entry, this.circleTexture, -scale / 2.0F, -scale / 2.0F, scale, scale, colors, true);
      }
   }

   private float bounceOut(float value) {
      float n1 = 7.5625F;
      float d1 = 2.75F;
      if (value < 1.0F / d1) {
         return n1 * value * value;
      } else if (value < 2.0F / d1) {
         float var6;
         return n1 * (var6 = value - 1.5F / d1) * var6 + 0.75F;
      } else {
         float var4;
         float var5;
         return value < 2.5F / d1 ? n1 * (var4 = value - 2.25F / d1) * var4 + 0.9375F : n1 * (var5 = value - 2.625F / d1) * var5 + 0.984375F;
      }
   }

   public record Circle(class_243 pos, Counter timer) {
   }
}
