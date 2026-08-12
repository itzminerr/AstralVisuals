package pl.astralvisuals.features.impl.render;

import com.mojang.blaze3d.platform.GlStateManager.class_4534;
import com.mojang.blaze3d.platform.GlStateManager.class_4535;
import com.mojang.blaze3d.systems.RenderSystem;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.class_10142;
import net.minecraft.class_1297;
import net.minecraft.class_1309;
import net.minecraft.class_243;
import net.minecraft.class_286;
import net.minecraft.class_287;
import net.minecraft.class_289;
import net.minecraft.class_290;
import net.minecraft.class_2960;
import net.minecraft.class_3532;
import net.minecraft.class_4184;
import net.minecraft.class_4587;
import net.minecraft.class_7833;
import net.minecraft.class_293.class_5596;
import org.joml.Matrix4f;
import org.joml.Vector4i;
import pl.astralvisuals.common.animation.Animation;
import pl.astralvisuals.common.animation.Direction;
import pl.astralvisuals.common.animation.implement.Decelerate;
import pl.astralvisuals.events.player.TickEvent;
import pl.astralvisuals.events.render.WorldRenderEvent;
import pl.astralvisuals.features.module.Module;
import pl.astralvisuals.features.module.ModuleCategory;
import pl.astralvisuals.features.module.setting.implement.ColorSetting;
import pl.astralvisuals.features.module.setting.implement.SelectSetting;
import pl.astralvisuals.features.module.setting.implement.SliderSettings;
import pl.astralvisuals.utils.client.Instance;
import pl.astralvisuals.utils.client.managers.event.EventHandler;
import pl.astralvisuals.utils.client.target.TargetTracker;
import pl.astralvisuals.utils.display.color.ColorAssist;
import pl.astralvisuals.utils.display.geometry.Render3D;
import pl.astralvisuals.utils.math.calc.CalcVector;
import pl.astralvisuals.utils.math.calc.Calculate;

public class TargetESP extends Module {
   // Текстуры — статические константы: раньше Identifier парсился десятки раз за кадр (призраки/кристаллы).
   private static final class_2960 BLOOM_TEXTURE = class_2960.method_60654("textures/features/particles/bloom.png");
   private static final int HURT_RED = 0xFFFF0000;

   private final Animation esp_anim = new Decelerate().setMs(400).setValue(1.0);
   private final SelectSetting targetEspType = new SelectSetting("Рендер цели", "Режим подсветки цели")
      .value("Куб", "Кольцо", "Призраки", "Кристаллы")
      .selected("Кольцо");
   private final SelectSetting cubeType = new SelectSetting("Текстура куба", "Вариант текстуры куба").value("1", "2", "3", "4", "5").visible(this::isCubeMode);
   public final ColorSetting colorSetting = new ColorSetting("Цвет", "Цвет подсветки").setColor(-1).presets(-1, -9659651, -7569409, -23178, -33925);
   public final SliderSettings radiusSetting = new SliderSettings("Радиус", "Размер подсветки цели").setValue(1.0F).range(0.5F, 3.0F);
   public final SliderSettings crystalBrightness = new SliderSettings("Яркость кристаллов", "Яркость кристаллов")
      .setValue(1.0F)
      .range(0.5F, 3.0F)
      .visible(() -> "Crystals".equals(this.normalizeMode(this.targetEspType.getSelected())));
   private class_1309 lastTarget;
   private class_1297 lastRenderedTarget;
   private final List<TargetESP.Crystal> crystalList = new ArrayList<>();
   private float rotationAngle;

   public static TargetESP getInstance() {
      return Instance.get(TargetESP.class);
   }

   public TargetESP() {
      super("TargetEsp", "Target ESP", ModuleCategory.RENDER);
      this.setup(this.targetEspType, this.cubeType, this.colorSetting, this.radiusSetting, this.crystalBrightness);
   }

   public float getRadius() {
      return this.radiusSetting.getValue();
   }

   // базовый цвет кристаллов с учётом настройки яркости
   public int getCrystalColor() {
      return applyBrightness(this.colorSetting.getColor(), this.crystalBrightness.getValue());
   }

   public static int applyBrightness(int color, float brightness) {
      int a = color >> 24 & 0xFF;
      int r = Math.min(255, Math.round((color >> 16 & 0xFF) * brightness));
      int g = Math.min(255, Math.round((color >> 8 & 0xFF) * brightness));
      int b = Math.min(255, Math.round((color & 0xFF) * brightness));
      return a << 24 | r << 16 | g << 8 | b;
   }

   @EventHandler
   public void onTick(TickEvent event) {
      // Двигает анимацию вращения ESP (espValue/espSpeed) каждый клиентский тик.
      // Раньше это висело на RotationUpdateEvent, который нигде не вызывается — поэтому ничего не крутилось.
      Render3D.updateTargetEsp();
   }

   @EventHandler
   public void onWorldRender(WorldRenderEvent event) {
      if (mc.field_1724 != null && mc.field_1687 != null) {
         class_1309 currentTarget = TargetTracker.getTarget();
         if (currentTarget != null) {
            this.lastTarget = currentTarget;
            this.esp_anim.setDirection(Direction.FORWARDS);
         } else {
            this.esp_anim.setDirection(Direction.BACKWARDS);
         }

         if (!this.isValid(this.lastTarget)) {
            this.resetTargetState();
         } else {
            float animation = this.esp_anim.getOutput().floatValue();
            if (!this.esp_anim.isFinished(Direction.BACKWARDS) && !(animation <= 0.01F)) {
               float hurtBlend = class_3532.method_15363((this.lastTarget.field_6235 - tickCounter.method_60637(false)) / 20.0F, 0.0F, 1.0F);
               String mode = this.normalizeMode(this.targetEspType.getSelected());
               switch (mode) {
                  case "Cube":
                     Render3D.drawCube(this.lastTarget, animation, hurtBlend, this.cubeType.getSelected());
                     break;
                  case "Circle":
                     Render3D.drawCircle(event.getStack(), this.lastTarget, animation, hurtBlend);
                     break;
                  case "Ghosts":
                     this.renderGhosts(this.lastTarget, animation, hurtBlend, 0.62F);
                     break;
                  case "Crystals":
                     if (this.crystalList.isEmpty() || this.lastTarget != this.lastRenderedTarget) {
                        this.createCrystals(this.lastTarget);
                        this.lastRenderedTarget = this.lastTarget;
                     }

                     this.renderCrystals(event.getStack(), this.lastTarget, animation, hurtBlend);
                     break;
                  default:
                     Render3D.drawCircle(event.getStack(), this.lastTarget, animation, hurtBlend);
               }
            } else {
               if (currentTarget == null) {
                  this.resetTargetState();
               }
            }
         }
      }
   }

   private void resetTargetState() {
      this.lastTarget = null;
      this.lastRenderedTarget = null;
      this.crystalList.clear();
   }

   private boolean isValid(class_1309 entity) {
      return entity != null && entity.method_5805() && !entity.method_31481() && entity.method_37908() == mc.field_1687;
   }

   private boolean isCubeMode() {
      return "Cube".equals(this.normalizeMode(this.targetEspType.getSelected()));
   }

   private String normalizeMode(String mode) {
      if (mode == null) {
         return "Circle";
      } else {
         return switch (mode) {
            case "Куб" -> "Cube";
            case "Кольцо" -> "Circle";
            case "Призраки" -> "Ghosts";
            case "Кристаллы" -> "Crystals";
            default -> mode;
         };
      }
   }

   private void renderGhosts(class_1309 target, float animation, float hurtBlend, float speed) {
      class_4184 camera = mc.method_1561().field_4686;
      class_243 targetPos = Calculate.interpolate(target).method_1020(camera.method_19326());
      boolean canSee = mc.field_1724.method_6057(target);
      double interpolatedAge = Calculate.interpolate((float)(mc.field_1724.field_6012 - 1), (float)mc.field_1724.field_6012);
      float halfHeight = target.method_17682() / 2.0F + 0.2F;
      float baseWidth = (target.method_17681() + 0.2F) * this.radiusSetting.getValue();
      float minY = 0.2F;
      float maxY = target.method_17682() - 0.2F;
      float hitEffect = Math.min(hurtBlend * 2.0F, 2.0F);
      float acceleration = (float)Math.sin(hitEffect * Math.PI) * 0.18F;
      float verticalShift = (float)Math.sin(hitEffect * Math.PI) * -0.04F;
      // Инварианты кадра наружу из цикла (раньше пересчитывались/аллоцировались 44× за кадр):
      float camPitch = camera.method_19329();
      float camYaw = camera.method_19330();
      double finalWidth = baseWidth * (1.0F + acceleration) * 1.04F;
      int ghostColor = this.colorSetting.getColor();
      float hurtMult = 1.0F + hurtBlend * 10.0F;
      class_4587 matrices = new class_4587(); // один стек на все 44 итерации вместо new каждый раз

      for (int layer = 0; layer < 4; layer++) {
         int i = 0;

         for (int length = 10; i <= length; i++) {
            double baseAngle = ((i / 2.0F + interpolatedAge * speed * 2.0) * length + layer * 90) % (length * 180);
            double radians = Math.toRadians(baseAngle);
            double sinQuad = Math.sin(Math.toRadians(interpolatedAge * 0.7 + i * (layer + halfHeight)) * 1.1) / 2.0;
            double adjustedSin = layer % 2 == 0 ? sinQuad : -sinQuad;
            double yOffset = minY + (adjustedSin + 0.5) * (maxY - minY);
            float offset = (float)(i + length) / (length + length);
            matrices.method_22903();
            matrices.method_22907(class_7833.field_40714.rotationDegrees(camPitch));
            matrices.method_22907(class_7833.field_40716.rotationDegrees(camYaw + 180.0F));
            matrices.method_22904(
               targetPos.field_1352 + Math.cos(radians) * finalWidth, targetPos.field_1351 + yOffset, targetPos.field_1350 + Math.sin(radians) * finalWidth
            );
            matrices.method_22907(class_7833.field_40716.rotationDegrees(-camYaw));
            matrices.method_22907(class_7833.field_40714.rotationDegrees(camPitch));
            int color = ColorAssist.multRedAndAlpha(ghostColor, hurtMult, offset * animation);
            float scale = 0.6F * offset * (0.6F + speed * 0.1F) + verticalShift;
            Render3D.drawTexture(
               matrices.method_23760().method_56822(),
               BLOOM_TEXTURE,
               -scale / 2.0F,
               -scale / 2.0F,
               scale,
               scale,
               new Vector4i(color),
               canSee
            );
            matrices.method_22909();
         }
      }
   }

   private void createCrystals(class_1297 target) {
      this.crystalList.clear();
      this.crystalList.add(new TargetESP.Crystal(target, new class_243(0.0, 0.85, 0.8), new class_243(-49.0, 0.0, 40.0)));
      this.crystalList.add(new TargetESP.Crystal(target, new class_243(0.2, 0.85, -0.675), new class_243(35.0, 0.0, -30.0)));
      this.crystalList.add(new TargetESP.Crystal(target, new class_243(0.6, 1.35, 0.6), new class_243(-30.0, 0.0, 35.0)));
      this.crystalList.add(new TargetESP.Crystal(target, new class_243(-0.74, 1.05, 0.4), new class_243(-25.0, 0.0, -30.0)));
      this.crystalList.add(new TargetESP.Crystal(target, new class_243(0.74, 0.95, -0.4), new class_243(0.0, 0.0, 0.0)));
      this.crystalList.add(new TargetESP.Crystal(target, new class_243(-0.475, 0.85, -0.375), new class_243(30.0, 0.0, -25.0)));
      this.crystalList.add(new TargetESP.Crystal(target, new class_243(0.0, 1.35, -0.6), new class_243(45.0, 0.0, 0.0)));
      this.crystalList.add(new TargetESP.Crystal(target, new class_243(0.85, 0.7, 0.1), new class_243(-30.0, 0.0, 30.0)));
      this.crystalList.add(new TargetESP.Crystal(target, new class_243(-0.7, 1.35, -0.3), new class_243(0.0, 0.0, 0.0)));
      this.crystalList.add(new TargetESP.Crystal(target, new class_243(-0.3, 1.35, 0.55), new class_243(0.0, 0.0, 0.0)));
      this.crystalList.add(new TargetESP.Crystal(target, new class_243(-0.5, 0.7, 0.7), new class_243(0.0, 0.0, 0.0)));
      this.crystalList.add(new TargetESP.Crystal(target, new class_243(0.5, 0.7, 0.7), new class_243(0.0, 0.0, 0.0)));
      this.crystalList.add(new TargetESP.Crystal(target, new class_243(-0.7, 0.75, 0.0), new class_243(0.0, 0.0, 0.0)));
      this.crystalList.add(new TargetESP.Crystal(target, new class_243(-0.2, 0.65, -0.7), new class_243(0.0, 0.0, 0.0)));
   }

   private void renderCrystals(class_4587 matrices, class_1297 target, float animation, float hurtBlend) {
      if (target != null && !this.crystalList.isEmpty()) {
         RenderSystem.enableDepthTest();
         class_243 targetPos = CalcVector.lerpPosition(target);
         this.rotationAngle = (this.rotationAngle + 0.5F) % 360.0F;
         matrices.method_22903();
         matrices.method_22904(targetPos.field_1352, targetPos.field_1351, targetPos.field_1350);
         // масштабируем вокруг центра хитбокса, чтобы при смене радиуса всё оставалось центрированным
         double centerY = target.method_17682() / 2.0;
         float crystalRadius = this.radiusSetting.getValue();
         matrices.method_22904(0.0, centerY, 0.0);
         matrices.method_22905(crystalRadius, crystalRadius, crystalRadius);
         matrices.method_22904(0.0, -centerY, 0.0);
         matrices.method_22907(class_7833.field_40716.rotationDegrees(this.rotationAngle));
         class_4184 camera = mc.field_1773.method_19418();

         for (TargetESP.Crystal crystal : this.crystalList) {
            crystal.render(matrices, animation, hurtBlend, camera);
         }

         matrices.method_22909();
         RenderSystem.enableDepthTest();
      }
   }

   private static class Crystal {
      // Силуэт кристалла постоянный (size=0.05, 8 граней) — считаем один раз, а не 14×5 раз за кадр.
      private static final float SIZE = 0.05F;
      private static final int SIDES = 8;
      private static final class_243[] TOP = new class_243[SIDES];
      private static final class_243[] BOTTOM = new class_243[SIDES];
      private static final class_243 APEX_TOP;
      private static final class_243 APEX_BOTTOM;

      static {
         float prismHeight = SIZE;
         for (int i = 0; i < SIDES; i++) {
            float angle = (float) ((Math.PI * 2) * i / SIDES);
            float x = (float) (SIZE * Math.cos(angle));
            float z = (float) (SIZE * Math.sin(angle));
            TOP[i] = new class_243(x, prismHeight / 2.0F, z);
            BOTTOM[i] = new class_243(x, -prismHeight / 2.0F, z);
         }
         float pyramidHeight = SIZE * 1.5F;
         APEX_TOP = new class_243(0.0, prismHeight / 2.0F + pyramidHeight, 0.0);
         APEX_BOTTOM = new class_243(0.0, -prismHeight / 2.0F - pyramidHeight, 0.0);
      }

      final class_1297 entity;
      final class_243 position;
      final class_243 rotation;
      final float size;
      final float rotationSpeed;

      Crystal(class_1297 entity, class_243 position, class_243 rotation) {
         this.entity = entity;
         this.position = position;
         this.rotation = rotation;
         this.size = 0.05F;
         this.rotationSpeed = 0.5F + (float)(Math.random() * 1.5);
      }

      void render(class_4587 matrices, float animation, float hurtBlend, class_4184 camera) {
         matrices.method_22903();
         matrices.method_22904(this.position.field_1352, this.position.field_1351, this.position.field_1350);
         float pulsation = 1.0F + (float)(Math.sin(System.currentTimeMillis() / 500.0) * 0.1F);
         matrices.method_22905(pulsation, pulsation, pulsation);
         float selfRotation = (float)(System.currentTimeMillis() % 36000L) / 100.0F * this.rotationSpeed;
         matrices.method_22907(class_7833.field_40714.rotationDegrees((float)this.rotation.field_1352));
         matrices.method_22907(class_7833.field_40716.rotationDegrees((float)this.rotation.field_1351 + selfRotation));
         matrices.method_22907(class_7833.field_40718.rotationDegrees((float)this.rotation.field_1350));
         RenderSystem.disableCull();
         RenderSystem.enableBlend();
         RenderSystem.blendFunc(class_4535.SRC_ALPHA, class_4534.ONE_MINUS_SRC_ALPHA);
         RenderSystem.setShader(class_10142.field_53876);
         int baseColor = ColorAssist.interpolateColor(TargetESP.getInstance().getCrystalColor(), HURT_RED, hurtBlend);
         RenderSystem.blendFunc(class_4535.SRC_ALPHA, class_4534.ONE);
         this.drawCrystal(matrices, baseColor, 0.2F, true, animation);
         RenderSystem.blendFunc(class_4535.SRC_ALPHA, class_4534.ONE_MINUS_SRC_ALPHA);
         this.drawCrystal(matrices, baseColor, 0.3F, true, animation);
         this.drawCrystal(matrices, baseColor, 0.8F, false, animation);
         RenderSystem.depthMask(false);
         RenderSystem.blendFunc(class_4535.SRC_ALPHA, class_4534.ONE);
         matrices.method_22903();
         matrices.method_22905(1.2F, 1.2F, 1.2F);
         this.drawCrystal(matrices, baseColor, 0.3F, true, animation);
         matrices.method_22909();
         this.drawBloomSphere(matrices, baseColor, animation, camera);
         RenderSystem.depthMask(true);
         RenderSystem.disableBlend();
         RenderSystem.enableCull();
         matrices.method_22909();
      }

      private void drawBloomSphere(class_4587 matrices, int baseColor, float animation, class_4184 camera) {
         RenderSystem.setShader(class_10142.field_53880);
         RenderSystem.setShaderTexture(0, BLOOM_TEXTURE);
         RenderSystem.enableBlend();
         RenderSystem.blendFunc(class_4535.SRC_ALPHA, class_4534.ONE);
         RenderSystem.depthMask(false);
         int bloomColor = ColorAssist.setAlpha(baseColor, (int)(10.0F * animation));
         float bloomSize = this.size * 13.0F;
         float pitch = camera.method_19329();
         float yaw = camera.method_19330();
         int segments = 6;

         // Все 12 квадов блума пишем в один буфер и делаем один draw-call (было 12 отдельных).
         class_287 bufferBuilder = class_289.method_1348().method_60827(class_5596.field_27382, class_290.field_1575);

         for (int i = 0; i < segments; i++) {
            matrices.method_22903();
            float angle = 360.0F / segments * i;
            matrices.method_22907(class_7833.field_40716.rotationDegrees(angle));
            matrices.method_22907(class_7833.field_40716.rotationDegrees(-yaw));
            matrices.method_22907(class_7833.field_40714.rotationDegrees(pitch));
            bloomQuad(bufferBuilder, matrices.method_23760().method_23761(), bloomSize, bloomColor);
            matrices.method_22909();
         }

         for (int i = 0; i < segments; i++) {
            matrices.method_22903();
            float angle = 360.0F / segments * i;
            matrices.method_22907(class_7833.field_40714.rotationDegrees(90.0F));
            matrices.method_22907(class_7833.field_40716.rotationDegrees(angle));
            matrices.method_22907(class_7833.field_40716.rotationDegrees(-yaw));
            matrices.method_22907(class_7833.field_40714.rotationDegrees(pitch));
            bloomQuad(bufferBuilder, matrices.method_23760().method_23761(), bloomSize, bloomColor);
            matrices.method_22909();
         }

         class_286.method_43433(bufferBuilder.method_60800());
         RenderSystem.depthMask(true);
         RenderSystem.blendFunc(class_4535.SRC_ALPHA, class_4534.ONE_MINUS_SRC_ALPHA);
      }

      private static void bloomQuad(class_287 bufferBuilder, Matrix4f matrix, float size, int color) {
         bufferBuilder.method_22918(matrix, -size / 2.0F, -size / 2.0F, 0.0F).method_22913(0.0F, 1.0F).method_39415(color);
         bufferBuilder.method_22918(matrix, size / 2.0F, -size / 2.0F, 0.0F).method_22913(1.0F, 1.0F).method_39415(color);
         bufferBuilder.method_22918(matrix, size / 2.0F, size / 2.0F, 0.0F).method_22913(1.0F, 0.0F).method_39415(color);
         bufferBuilder.method_22918(matrix, -size / 2.0F, size / 2.0F, 0.0F).method_22913(0.0F, 0.0F).method_39415(color);
      }

      private void drawCrystal(class_4587 matrices, int baseColor, float alpha, boolean filled, float animation) {
         class_287 bufferBuilder = class_289.method_1348().method_60827(filled ? class_5596.field_27379 : class_5596.field_29344, class_290.field_1576);
         int finalColor = ColorAssist.setAlpha(baseColor, (int)(alpha * 255.0F * animation));

         for (int i = 0; i < SIDES; i++) {
            class_243 v1 = BOTTOM[i];
            class_243 v2 = BOTTOM[(i + 1) % SIDES];
            class_243 v3 = TOP[(i + 1) % SIDES];
            class_243 v4 = TOP[i];
            this.drawQuad(matrices, bufferBuilder, v1, v2, v3, v4, finalColor, filled);
         }

         for (int i = 0; i < SIDES; i++) {
            this.drawTriangle(matrices, bufferBuilder, APEX_TOP, TOP[i], TOP[(i + 1) % SIDES], finalColor, filled);
         }

         for (int i = 0; i < SIDES; i++) {
            this.drawTriangle(matrices, bufferBuilder, APEX_BOTTOM, BOTTOM[(i + 1) % SIDES], BOTTOM[i], finalColor, filled);
         }

         class_286.method_43433(bufferBuilder.method_60800());
      }

      private void drawTriangle(class_4587 matrices, class_287 bufferBuilder, class_243 v1, class_243 v2, class_243 v3, int color, boolean filled) {
         if (filled) {
            bufferBuilder.method_22918(matrices.method_23760().method_23761(), (float)v1.field_1352, (float)v1.field_1351, (float)v1.field_1350)
               .method_39415(color);
            bufferBuilder.method_22918(matrices.method_23760().method_23761(), (float)v2.field_1352, (float)v2.field_1351, (float)v2.field_1350)
               .method_39415(color);
            bufferBuilder.method_22918(matrices.method_23760().method_23761(), (float)v3.field_1352, (float)v3.field_1351, (float)v3.field_1350)
               .method_39415(color);
         }
      }

      private void drawQuad(class_4587 matrices, class_287 bufferBuilder, class_243 v1, class_243 v2, class_243 v3, class_243 v4, int color, boolean filled) {
         if (filled) {
            this.drawTriangle(matrices, bufferBuilder, v1, v2, v3, color, true);
            this.drawTriangle(matrices, bufferBuilder, v1, v3, v4, color, true);
         } else {
            bufferBuilder.method_22918(matrices.method_23760().method_23761(), 100.0F, 100.0F, (float)v1.field_1350).method_39415(color);
         }
      }
   }
}
