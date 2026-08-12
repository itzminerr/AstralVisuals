package pl.astralvisuals.features.impl.render;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import net.minecraft.class_1297;
import net.minecraft.class_1309;
import net.minecraft.class_1665;
import net.minecraft.class_1676;
import net.minecraft.class_1684;
import net.minecraft.class_1685;
import net.minecraft.class_1799;
import net.minecraft.class_1802;
import net.minecraft.class_238;
import net.minecraft.class_239;
import net.minecraft.class_243;
import net.minecraft.class_3959;
import net.minecraft.class_3965;
import net.minecraft.class_4587.class_4665;
import pl.astralvisuals.events.render.DrawEvent;
import pl.astralvisuals.events.render.WorldRenderEvent;
import pl.astralvisuals.features.module.Module;
import pl.astralvisuals.features.module.ModuleCategory;
import pl.astralvisuals.features.module.setting.implement.BooleanSetting;
import pl.astralvisuals.features.module.setting.implement.ColorSetting;
import pl.astralvisuals.features.module.setting.implement.SliderSettings;
import pl.astralvisuals.mixins.player.entity.ProjectileEntityAccessor;
import pl.astralvisuals.utils.client.Instance;
import pl.astralvisuals.utils.client.managers.event.EventHandler;
import pl.astralvisuals.utils.display.color.ColorAssist;
import pl.astralvisuals.utils.display.font.FontRenderer;
import pl.astralvisuals.utils.display.font.Fonts;
import pl.astralvisuals.utils.display.geometry.Render3D;
import pl.astralvisuals.utils.math.projection.Projection;

// Предсказание траектории снарядов (стрелы, жемчуг, трезубец, зелья).
// Логика симуляции портирована из Pulse; рендер — через Render3D (depth=true, как в KillEffect/ChinaHat).
public class Predictions extends Module {
   private static final int MAX_STEPS = 220;

   private final BooleanSetting arrows = new BooleanSetting("Стрелы", "Лук и арбалет").setValue(true);
   private final BooleanSetting enderPearls = new BooleanSetting("Жемчуг", "Эндер-жемчуг").setValue(true);
   private final BooleanSetting tridents = new BooleanSetting("Трезубец", "Трезубец").setValue(true);
   private final BooleanSetting potions = new BooleanSetting("Зелья", "Кидаемые зелья").setValue(true);
   private final SliderSettings lineWidth = new SliderSettings("Толщина", "Толщина линии").setValue(3.0F).range(1, 6);
   private final ColorSetting lineColor = new ColorSetting("Цвет", "Цвет линии").setColor(new Color(120, 80, 255).getRGB());
   private final BooleanSetting gradient = new BooleanSetting("Градиент", "Градиент к концу траектории").setValue(true);
   private final ColorSetting gradientColor = new ColorSetting("Цвет градиента", "Второй цвет").setColor(new Color(80, 180, 255).getRGB()).visible(this::gradientOn);
   private final BooleanSetting impactMarker = new BooleanSetting("Маркер", "Метка в точке падения").setValue(true);
   private final BooleanSetting impactTimer = new BooleanSetting("Таймер", "Показывать время до падения/попадания").setValue(true);

   private final List<class_243> path = new ArrayList<>();
   private final List<FlyingPrediction> flyingPredictions = new ArrayList<>();
   private class_243 impactPoint;
   private int impactTicks = -1;

   public static Predictions getInstance() {
      return Instance.get(Predictions.class);
   }

   public Predictions() {
      super("Predictions", "Predictions", ModuleCategory.RENDER);
      this.setup(
         this.arrows, this.enderPearls, this.tridents, this.potions, this.lineWidth,
         this.lineColor, this.gradient, this.gradientColor, this.impactMarker, this.impactTimer
      );
   }

   private boolean gradientOn() {
      return this.gradient.isValue();
   }

   @EventHandler
   public void onWorldRender(WorldRenderEvent e) {
      if (mc.field_1724 == null || mc.field_1687 == null) {
         this.clearAllPredictions();
         return;
      }

      this.updateFlyingPredictions(e.getPartialTicks());

      class_1799 stack = this.currentProjectileStack(e.getPartialTicks());
      ProjectileType type = ProjectileType.from(stack);
      if (type == null || !this.isEnabled(type)) {
         this.clearHeldPrediction();
         return;
      }

      this.simulate(e.getPartialTicks(), stack, type);
      // Рендер через тот же путь, что у ChinaHat (видимый): передаём entry матрицы события.
      this.render(e.getStack().method_23760());
   }

   private class_1799 currentProjectileStack(float partial) {
      class_1799 main = mc.field_1724.method_6047();
      if (ProjectileType.from(main) != null) {
         return main;
      }

      class_1799 off = mc.field_1724.method_6079();
      return ProjectileType.from(off) != null ? off : class_1799.field_8037;
   }

   private boolean isEnabled(ProjectileType type) {
      return switch (type) {
         case ARROW -> this.arrows.isValue();
         case ENDER_PEARL -> this.enderPearls.isValue();
         case TRIDENT -> this.tridents.isValue();
         case POTION -> this.potions.isValue();
      };
   }


   private void simulate(float tickDelta, class_1799 stack, ProjectileType type) {
      this.clearHeldPrediction();

      class_243 position = mc.field_1724.method_5836(tickDelta);
      class_243 velocity = this.initialVelocity(stack, type);
      this.path.add(position);

      for (int step = 0; step < MAX_STEPS; step++) {
         class_243 next = position.method_1019(velocity);
         boolean collided = false;

         // Столкновения считаем «по возможности»: если raycast/поиск сущностей бросит исключение
         // (изменения API 1.21.4), всё равно строим баллистическую траекторию — линия будет видна.
         try {
            class_3965 blockHit = mc.field_1687.method_17742(
               new class_3959(position, next, class_3959.class_3960.field_17558, class_3959.class_242.field_1348, mc.field_1724)
            );
            if (blockHit != null && blockHit.method_17783() != class_239.class_240.field_1333) {
               next = blockHit.method_17784();
               collided = true;
            }

            class_243 entityHit = this.findEntityHit(position, next);
            if (entityHit != null) {
               next = entityHit;
               collided = true;
            }
         } catch (Throwable ignored) {
            // нет коллизий в этом кадре — продолжаем чистую баллистику
         }

         this.path.add(next);
         if (collided) {
            this.impactPoint = next;
            this.impactTicks = step + 1;
            return;
         }

         if (next.field_1351 < mc.field_1687.method_31607() - 16.0) {
            return;
         }

         position = next;
         velocity = velocity.method_1021(type.drag).method_1031(0.0, -type.gravity, 0.0);
      }
   }

   private void clearHeldPrediction() {
      this.path.clear();
      this.impactPoint = null;
      this.impactTicks = -1;
   }

   private void clearAllPredictions() {
      this.clearHeldPrediction();
      this.flyingPredictions.clear();
   }

   @Override
   public void deactivate() {
      this.clearAllPredictions();
   }

   /** Подписи для предварительного прогноза и уже летящих снарядов. */
   @EventHandler
   public void onDraw(DrawEvent event) {
      if (!this.impactTimer.isValue() || mc.field_1724 == null || mc.field_1687 == null) {
         return;
      }

      FontRenderer font = Fonts.getSize(14, Fonts.Type.SEMI);
      if (this.impactPoint != null && this.impactTicks >= 0) {
         this.drawTimerLabel(event, font, this.impactPoint, this.impactTicks);
      }

      for (FlyingPrediction prediction : this.flyingPredictions) {
         float remainingTicks = Math.max(0.0F, prediction.ticks() - event.getPartialTicks());
         this.drawTimerLabel(event, font, prediction.position(), remainingTicks);
      }
   }

   private void drawTimerLabel(DrawEvent event, FontRenderer font, class_243 worldPosition, float ticks) {
      class_243 screen = Projection.worldSpaceToScreenSpace(worldPosition);
      if (screen.field_1350 <= 0.0 || screen.field_1350 >= 1.0) {
         return;
      }

      String text = String.format(Locale.US, "%.1fс", ticks / 20.0F);
      font.drawCenteredString(
         event.getDrawContext().method_51448(), text, screen.field_1352, screen.field_1351 + 7.0F,
         0xFF000000 | this.pathColor(1.0F) & 0xFFFFFF
      );
   }

   /** После броска/выстрела пересчитываем оставшиееся время от текущей позиции снаряда. */
   private void updateFlyingPredictions(float tickDelta) {
      this.flyingPredictions.clear();
      for (class_1297 entity : mc.field_1687.method_18112()) {
         ProjectileEntityAccessor accessor = entity instanceof class_1676 ? (ProjectileEntityAccessor)entity : null;
         if (!(entity instanceof class_1676 projectile)
            || !projectile.method_5805()
            || projectile.method_7325()
            || accessor.astral$getOwner() != mc.field_1724 && !mc.field_1724.method_5667().equals(accessor.astral$getOwnerUuid())) {
            continue;
         }

         ProjectileType type = this.flyingType(projectile);
         if (type == null || !this.isEnabled(type)) {
            continue;
         }
         class_243 currentVelocity = projectile.method_18798();
         if (currentVelocity.field_1352 * currentVelocity.field_1352
            + currentVelocity.field_1351 * currentVelocity.field_1351
            + currentVelocity.field_1350 * currentVelocity.field_1350 < 1.0E-6) {
            continue;
         }

         int ticks = this.predictFlyingImpact(projectile, type, tickDelta);
         if (ticks > 0) {
            this.flyingPredictions.add(new FlyingPrediction(projectile.method_30950(tickDelta), ticks));
         }
      }
   }

   private ProjectileType flyingType(class_1676 projectile) {
      if (projectile instanceof class_1684) {
         return ProjectileType.ENDER_PEARL;
      }
      if (projectile instanceof class_1685) {
         return ProjectileType.TRIDENT;
      }
      if (projectile instanceof class_1665) {
         return ProjectileType.ARROW;
      }
      return null;
   }

   private int predictFlyingImpact(class_1676 projectile, ProjectileType type, float tickDelta) {
      class_243 position = projectile.method_30950(tickDelta);
      class_243 velocity = projectile.method_18798();
      for (int step = 0; step < MAX_STEPS; step++) {
         class_243 next = position.method_1019(velocity);
         boolean collided = false;
         try {
            class_3965 blockHit = mc.field_1687.method_17742(
               new class_3959(position, next, class_3959.class_3960.field_17558, class_3959.class_242.field_1348, projectile)
            );
            if (blockHit != null && blockHit.method_17783() != class_239.class_240.field_1333) {
               next = blockHit.method_17784();
               collided = true;
            }

            class_243 entityHit = this.findEntityHit(position, next, projectile);
            if (entityHit != null) {
               collided = true;
            }
         } catch (Throwable ignored) {
         }

         if (collided) {
            return step + 1;
         }
         if (next.field_1351 < mc.field_1687.method_31607() - 16.0) {
            return -1;
         }

         position = next;
         velocity = velocity.method_1021(type.drag).method_1031(0.0, -type.gravity, 0.0);
      }
      return -1;
   }

   private class_243 initialVelocity(class_1799 stack, ProjectileType type) {
      class_243 direction = this.lookDirection();
      double speed = type.baseSpeed;
      Object item = stack.method_7909();

      // Если лук/трезубец не натянут — показываем траекторию для полного заряда.
      if (item == class_1802.field_8102) {
         float charge = 1.0F;
         if (mc.field_1724.method_6115() && mc.field_1724.method_6014() > 0) {
            int used = stack.method_7935(mc.field_1724) - mc.field_1724.method_6014();
            charge = Math.min(1.0F, Math.max(0.05F, (used / 20.0F * used / 20.0F + used / 20.0F * 2.0F) / 3.0F));
         }

         speed = 3.0 * charge;
      } else if (item == class_1802.field_8547) {
         float charge = 1.0F;
         if (mc.field_1724.method_6115() && mc.field_1724.method_6014() > 0) {
            int used = stack.method_7935(mc.field_1724) - mc.field_1724.method_6014();
            charge = Math.min(1.0F, Math.max(0.1F, used / 10.0F));
         }

         speed = 2.5 * charge;
      }

      return direction.method_1021(speed).method_1019(mc.field_1724.method_18798().method_1021(0.35));
   }

   private class_243 lookDirection() {
      float yaw = mc.field_1724.method_36454();
      float pitch = mc.field_1724.method_36455();
      // Каноничная формула getRotationVector — без сдвига на -PI (он разворачивал дугу за спину).
      float pitchCos = (float)Math.cos(-pitch * (Math.PI / 180.0));
      float pitchSin = (float)Math.sin(-pitch * (Math.PI / 180.0));
      float yawCos = (float)Math.cos(-yaw * (Math.PI / 180.0));
      float yawSin = (float)Math.sin(-yaw * (Math.PI / 180.0));
      return new class_243(yawSin * pitchCos, pitchSin, yawCos * pitchCos).method_1029();
   }

   private class_243 findEntityHit(class_243 from, class_243 to) {
      return this.findEntityHit(from, to, mc.field_1724);
   }

   private class_243 findEntityHit(class_243 from, class_243 to, class_1297 source) {
      class_243 closest = null;
      double closestDist = Double.MAX_VALUE;
      class_238 search = new class_238(from, to).method_1014(1.0);

      for (class_1297 entity : mc.field_1687.method_8333(source, search, entity -> true)) {
         if (entity == source || entity == mc.field_1724 || entity.method_7325() || !entity.method_5805() || !(entity instanceof class_1309)) {
            continue;
         }

         var hit = entity.method_5829().method_1014(0.3).method_992(from, to);
         if (hit.isPresent()) {
            double dist = from.method_1025(hit.get());
            if (dist < closestDist) {
               closestDist = dist;
               closest = hit.get();
            }
         }
      }

      return closest;
   }

   // Рендер ровно тем же путём, что и видимый ChinaHat: передаём entry матрицы события
   // в Render3D.drawLine(entry, ...) с depth=true (а не no-arg overload через lastWorldSpaceMatrix).
   private void render(class_4665 entry) {
      if (this.path.size() < 2) {
         return;
      }

      float width = this.lineWidth.getValue();
      for (int i = 0; i < this.path.size() - 1; i++) {
         float progress = (float)i / (float)(this.path.size() - 1);
         int color = 0xFF000000 | this.pathColor(progress) & 0xFFFFFF;
         Render3D.drawLine(entry, this.path.get(i), this.path.get(i + 1), color, color, width, true);
      }

      if (this.impactMarker.isValue() && this.impactPoint != null) {
         int color = 0xFF000000 | this.pathColor(1.0F) & 0xFFFFFF;
         double s = 0.3;
         class_238 box = new class_238(
            this.impactPoint.field_1352 - s, this.impactPoint.field_1351 - s, this.impactPoint.field_1350 - s,
            this.impactPoint.field_1352 + s, this.impactPoint.field_1351 + s, this.impactPoint.field_1350 + s
         );
         Render3D.drawBox(entry, box, color, width, true, true, true);
      }
   }

   private int pathColor(float progress) {
      int start = this.lineColor.getColor();
      if (!this.gradient.isValue()) {
         return start;
      }

      int end = this.gradientColor.getColor();
      int r = lerp(ColorAssist.red(start), ColorAssist.red(end), progress);
      int g = lerp(ColorAssist.green(start), ColorAssist.green(end), progress);
      int b = lerp(ColorAssist.blue(start), ColorAssist.blue(end), progress);
      return ColorAssist.getColor(r, g, b, 255);
   }

   private static int lerp(int start, int end, float progress) {
      return Math.round(start + (end - start) * Math.max(0.0F, Math.min(1.0F, progress)));
   }

   private record FlyingPrediction(class_243 position, int ticks) {
   }

   private enum ProjectileType {
      ENDER_PEARL(1.5, 0.99, 0.03),
      TRIDENT(2.5, 0.99, 0.05),
      ARROW(3.0, 0.99, 0.05),
      POTION(0.5, 0.99, 0.05);

      private final double baseSpeed;
      private final double drag;
      private final double gravity;

      ProjectileType(double baseSpeed, double drag, double gravity) {
         this.baseSpeed = baseSpeed;
         this.drag = drag;
         this.gravity = gravity;
      }

      private static ProjectileType from(class_1799 stack) {
         if (stack == null || stack.method_7960()) {
            return null;
         }

         Object item = stack.method_7909();
         if (item == class_1802.field_8634) {
            return ENDER_PEARL;
         }
         if (item == class_1802.field_8547) {
            return TRIDENT;
         }
         if (item == class_1802.field_8102 || item == class_1802.field_8399) {
            return ARROW;
         }
         return item == class_1802.field_8436 || item == class_1802.field_8150 ? POTION : null;
      }
   }
}
