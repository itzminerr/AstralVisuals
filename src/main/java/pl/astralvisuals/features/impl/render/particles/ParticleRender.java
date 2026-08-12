package pl.astralvisuals.features.impl.render.particles;

import com.mojang.blaze3d.systems.RenderSystem;
import java.util.ArrayDeque;
import java.util.Deque;
import net.minecraft.class_10142;
import net.minecraft.class_2338;
import net.minecraft.class_243;
import net.minecraft.class_265;
import net.minecraft.class_286;
import net.minecraft.class_287;
import net.minecraft.class_289;
import net.minecraft.class_290;
import net.minecraft.class_293;
import net.minecraft.class_2960;
import net.minecraft.class_4184;
import net.minecraft.class_4587;
import net.minecraft.class_7833;
import org.joml.Matrix4f;
import pl.astralvisuals.utils.display.color.ColorAssist;
import pl.astralvisuals.utils.display.geometry.Render3D;
import pl.astralvisuals.utils.display.interfaces.QuickImports;
import pl.astralvisuals.utils.math.calc.Calculate;

// Порт частиц из EvaWare (ParticleRender). Физика и биллбордовый рендер адаптированы под
// intermediary-имена и батч-рендер моего Render3D (drawTexture / drawLine).
public class ParticleRender implements QuickImports {
   public float prevX;
   public float prevY;
   public float prevZ;
   public float x;
   public float y;
   public float z;
   public float motionX;
   public float motionY;
   public float motionZ;
   public float rotation;
   public float prevRotation;
   public float rotateSpeed = 20.0F;
   public float size = 0.2F;
   public class_2960 identifier;
   public boolean gravityFalls;
   public boolean dropPhysics;
   public boolean rotating;
   public boolean trail;
   public int trailLength = 5;
   public int baseColor = -1;

   private int age;
   private final int maxLife;
   private final int spawnDuration;
   private final int dyingDuration;
   private final Deque<class_243> trailPoints = new ArrayDeque<>();

   public ParticleRender(float x, float y, float z, int lifeTime, int spawnDuration, int dyingDuration) {
      this.prevX = this.x = x;
      this.prevY = this.y = y;
      this.prevZ = this.z = z;
      this.maxLife = Calculate.getRandom(Math.max(lifeTime / 2, 1), Math.max(lifeTime, 2));
      this.spawnDuration = spawnDuration;
      this.dyingDuration = dyingDuration;
      this.rotation = Calculate.getRandom(-180.0F, 180.0F);
      this.prevRotation = this.rotation;
   }

   // Клиентский тик: физика + старение. true = частица мертва и должна быть удалена.
   public boolean update() {
      this.age++;
      float gravity = this.gravityFalls ? 0.3F : 1.0F;
      this.prevX = this.x;
      this.prevY = this.y;
      this.prevZ = this.z;
      this.x = this.x + this.motionX;
      this.y = this.y + this.motionY * gravity;
      this.z = this.z + this.motionZ;

      double speed = Math.sqrt(this.motionX * this.motionX + this.motionZ * this.motionZ);
      float half = this.size / 2.0F;
      if (this.solidAt(this.x, this.y - half - 0.05F, this.z)) {
         this.motionY = -this.motionY / 1.1F;
         this.motionX /= 1.1F;
         this.motionZ /= 1.1F;
      } else if (this.solidAt(this.x - (float)speed - half, this.y, this.z)
         || this.solidAt(this.x + (float)speed + half, this.y, this.z)
         || this.solidAt(this.x, this.y, this.z - (float)speed - half)
         || this.solidAt(this.x, this.y, this.z + (float)speed + half)) {
         this.motionX = -this.motionX;
         this.motionZ = -this.motionZ;
      } else if (this.dropPhysics) {
         this.motionY -= 0.02F;
      }

      this.prevRotation = this.rotation;
      this.rotation += this.rotateSpeed;

      if (!this.gravityFalls) {
         this.motionX /= 1.1F;
         this.motionY /= 1.1F;
         this.motionZ /= 1.1F;
      }

      if (this.trail) {
         this.trailPoints.addFirst(new class_243(this.x, this.y, this.z));
         while (this.trailPoints.size() > this.trailLength) {
            this.trailPoints.removeLast();
         }
      }

      boolean tooFar = mc.field_1724 != null && mc.field_1724.method_19538().method_1022(new class_243(this.x, this.y, this.z)) >= 80.0;
      return tooFar || this.age >= this.spawnDuration + this.maxLife + this.dyingDuration;
   }

   public void render() {
      if (mc.field_1687 == null) {
         return;
      }

      class_4184 camera = mc.method_1561().field_4686;
      float ix = Calculate.interpolate(this.prevX, this.x);
      float iy = Calculate.interpolate(this.prevY, this.y);
      float iz = Calculate.interpolate(this.prevZ, this.z);
      class_243 cam = camera.method_19326();
      int color = ColorAssist.setAlpha(this.baseColor, (int)(255.0F * this.alpha()));

      if (this.identifier == null) {
         return;
      }

      class_4587 matrix = new class_4587();
      matrix.method_22903();
      matrix.method_22907(class_7833.field_40714.rotationDegrees(camera.method_19329()));
      matrix.method_22907(class_7833.field_40716.rotationDegrees(camera.method_19330() + 180.0F));
      matrix.method_22904(ix - cam.field_1352, iy - cam.field_1351, iz - cam.field_1350);
      matrix.method_22907(class_7833.field_40716.rotationDegrees(-camera.method_19330()));
      matrix.method_22907(class_7833.field_40714.rotationDegrees(camera.method_19329()));
      if (this.rotating) {
         matrix.method_22907(class_7833.field_40718.rotationDegrees(Calculate.interpolate(this.prevRotation, this.rotation)));
      }

      // Прямой рендер биллборда со стандартным альфа-смешиванием (надёжнее очереди Render3D,
      // у которой нестандартный ONE_MINUS_CONSTANT_ALPHA блендинг — частицы были невидимы).
      Matrix4f m = matrix.method_23760().method_23761();
      float half = this.size / 2.0F;

      RenderSystem.enableBlend();
      RenderSystem.defaultBlendFunc();
      RenderSystem.disableCull();
      RenderSystem.disableDepthTest();
      RenderSystem.depthMask(false);
      RenderSystem.setShader(class_10142.field_53880);
      RenderSystem.setShaderTexture(0, this.identifier);

      class_287 buffer = class_289.method_1348().method_60827(class_293.class_5596.field_27382, class_290.field_1575);
      buffer.method_22918(m, -half, half, 0.0F).method_22913(0.0F, 0.0F).method_39415(color);
      buffer.method_22918(m, half, half, 0.0F).method_22913(0.0F, 1.0F).method_39415(color);
      buffer.method_22918(m, half, -half, 0.0F).method_22913(1.0F, 1.0F).method_39415(color);
      buffer.method_22918(m, -half, -half, 0.0F).method_22913(1.0F, 0.0F).method_39415(color);
      class_286.method_43433(buffer.method_60800());

      RenderSystem.depthMask(true);
      RenderSystem.enableDepthTest();
      RenderSystem.enableCull();
      RenderSystem.disableBlend();
      matrix.method_22909();

      if (this.trail && this.trailPoints.size() > 1) {
         int trailColor = ColorAssist.setAlpha(this.baseColor, (int)(120.0F * this.alpha()));
         class_243 last = null;
         for (class_243 point : this.trailPoints) {
            if (last != null) {
               Render3D.drawLine(last, point, trailColor, 1.5F, true);
            }

            last = point;
         }
      }
   }

   private float alpha() {
      if (this.age < this.spawnDuration) {
         return this.spawnDuration <= 0 ? 1.0F : (float)this.age / this.spawnDuration;
      }

      int dyingStart = this.spawnDuration + this.maxLife;
      if (this.age < dyingStart) {
         return 1.0F;
      }

      if (this.dyingDuration <= 0) {
         return 0.0F;
      }

      return Math.max(0.0F, 1.0F - (float)(this.age - dyingStart) / this.dyingDuration);
   }

   private boolean solidAt(float x, float y, float z) {
      if (mc.field_1687 == null) {
         return false;
      }

      class_2338 pos = class_2338.method_49637(x, y, z);
      class_265 shape = mc.field_1687.method_8320(pos).method_26218(mc.field_1687, pos);
      return !shape.method_1110();
   }

   public static class_2960 getTexture(String mode) {
      String name = switch (mode) {
         case "Spark" -> "spark_" + Calculate.getRandom(1, 4);
         case "Star" -> "star";
         case "Heart" -> "heart";
         case "Dollar" -> "dollar";
         case "Snowflake" -> "snowflake";
         case "Ball" -> "ball";
         default -> "glow";
      };
      return class_2960.method_60654("textures/features/particles/eva/" + name + ".png");
   }
}
