package pl.astralvisuals.features.impl.render;

import com.mojang.authlib.GameProfile;
import java.awt.Color;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.class_1297;
import net.minecraft.class_1304;
import net.minecraft.class_1309;
import net.minecraft.class_1657;
import net.minecraft.class_243;
import net.minecraft.class_2561;
import net.minecraft.class_2663;
import net.minecraft.class_3730;
import net.minecraft.class_4050;
import net.minecraft.class_4587;
import net.minecraft.class_745;
import pl.astralvisuals.events.packet.PacketEvent;
import pl.astralvisuals.events.player.EntityDeathEvent;
import pl.astralvisuals.events.render.WorldRenderEvent;
import pl.astralvisuals.features.module.Module;
import pl.astralvisuals.features.module.ModuleCategory;
import pl.astralvisuals.features.module.setting.implement.BooleanSetting;
import pl.astralvisuals.features.module.setting.implement.ColorSetting;
import pl.astralvisuals.features.module.setting.implement.SelectSetting;
import pl.astralvisuals.features.module.setting.implement.SliderSettings;
import pl.astralvisuals.features.module.setting.implement.TextSetting;
import pl.astralvisuals.utils.client.Instance;
import pl.astralvisuals.utils.client.managers.event.EventHandler;
import pl.astralvisuals.utils.client.sound.SoundManager;
import pl.astralvisuals.utils.display.color.ColorAssist;
import pl.astralvisuals.utils.display.geometry.Render3D;

public class KillEffect extends Module {
   private static final long DEATH_SOUL_DURATION_MS = 3000L;
   private static final long TOTEM_SOUL_DURATION_MS = 2400L;
   private final BooleanSetting mobs = new BooleanSetting("Мобы", "Применять эффект к мобам").setValue(false);
   private final SelectSetting effectType = new SelectSetting("Тип эффекта", "Вид эффекта")
      .value("Крест", "Душа", "Молния", "Вспышка", "Кольца")
      .selected("Душа");
   private final ColorSetting effectColor = new ColorSetting("Цвет эффекта", "Цвет креста, вспышки и колец")
      .setColor(new Color(150, 100, 255).getRGB());
   private final BooleanSetting killSound = new BooleanSetting("Звук после килла", "Проигрывать звук после смерти цели").setValue(false);
   private final SelectSetting soundMode = new SelectSetting("Источник звука", "Встроенный или свой файл")
      .value("Встроенный", "Свой файл")
      .selected("Встроенный")
      .visible(this.killSound::isValue);
   private final TextSetting soundFile = new TextSetting("Файл звука", "Имя OGG/WAV в AstralVisuals/Custom/KillSounds")
      .setText("kill.ogg").setMin(1).setMax(96)
      .visible(() -> this.killSound.isValue() && this.soundMode.isSelected("Свой файл"));
   private final SliderSettings soundVolume = new SliderSettings("Громкость килла", "Громкость звука после килла")
      .range(0.0F, 1.0F).setValue(1.0F).visible(this.killSound::isValue);
   private final Map<class_1297, KillEffect.EntityRenderData> renderEntities = new ConcurrentHashMap<>();
   private final Map<class_1297, KillEffect.EntityRenderData> totemEntities = new ConcurrentHashMap<>();

   public KillEffect() {
      super("KillEffect", "Kill Effect", ModuleCategory.RENDER);
      this.setup(this.mobs, this.effectType, this.effectColor, this.killSound, this.soundMode, this.soundFile, this.soundVolume);
   }

   public static KillEffect getInstance() {
      return Instance.get(KillEffect.class);
   }

   @EventHandler
   public void onEntityDeath(EntityDeathEvent event) {
      if (mc.field_1687 != null && mc.field_1724 != null) {
         class_1297 entity = event.getEntity();
         if (entity instanceof class_1309) {
            if (this.mobs.isValue() || entity instanceof class_1657) {
               if (entity != mc.field_1724 && !this.renderEntities.containsKey(entity)) {
                  this.spawnGhost(entity, 3000L, false);
                  this.playKillSound();
               }
            }
         }
      }
   }

   @EventHandler
   public void onPacket(PacketEvent event) {
      if (mc.field_1687 != null && mc.field_1724 != null) {
         if (this.isState()) {
            if (event.getPacket() instanceof class_2663 packet && packet.method_11470() == 35) {
               class_1297 entity = packet.method_11469(mc.field_1687);
               if (entity instanceof class_1657 player && player != mc.field_1724) {
                  if (!this.mobs.isValue() && !(entity instanceof class_1657)) {
                     return;
                  }

                  if (this.totemEntities.containsKey(entity)) {
                     return;
                  }

                   this.spawnGhost(entity, 2400L, true);
               }
            }
         }
      }
   }

   public void spawnTotemEffect(class_1297 entity) {
      if (this.isState() && entity != null && mc.field_1687 != null && mc.field_1724 != null) {
         if (!this.totemEntities.containsKey(entity)) {
            this.spawnGhost(entity, 2400L, true);
         }
      }
   }

   private void spawnGhost(class_1297 entity, long durationMs, boolean isTotem) {
      class_1309 ghostEntity = null;
      if (this.isGhostEffect() && entity instanceof class_1309 livingEntity) {
         ghostEntity = this.createGhostEntity(livingEntity, isTotem);
      }

      KillEffect.EntityRenderData data = new KillEffect.EntityRenderData(
         System.currentTimeMillis(), durationMs, entity.method_36454(), entity.method_19538(), entity, ghostEntity, isTotem
      );
      if (isTotem) {
         this.totemEntities.put(entity, data);
      } else {
         this.renderEntities.put(entity, data);
      }
   }

   private class_1309 createGhostEntity(class_1309 source, boolean isTotem) {
      if (source instanceof class_1657 player) {
         return this.createGhostPlayer(player, isTotem);
      } else if (!(source.method_5864().method_5883(mc.field_1687, class_3730.field_16462) instanceof class_1309 ghost)) {
         return null;
      } else {
         ghost.method_18380(class_4050.field_18076);
         ghost.method_36457(0.0F);
         ghost.method_36456(0.0F);
         ghost.field_6241 = 0.0F;
         ghost.field_6283 = 0.0F;
         ghost.method_6033(Math.max(source.method_6063(), 1.0F));
         ghost.method_6073(0.0F);
         ghost.method_18799(class_243.field_1353);
         ghost.method_5875(true);
         ghost.method_5880(false);

         for (class_1304 slot : class_1304.values()) {
            ghost.method_5673(slot, source.method_6118(slot).method_7972());
         }

         return ghost;
      }
   }

   private class_745 createGhostPlayer(class_1657 source, boolean isTotem) {
      GameProfile profile = source.method_7334();
      class_745 ghost = new class_745(mc.field_1687, profile);
      ghost.method_5826(UUID.randomUUID());
      ghost.method_18380(class_4050.field_18076);
      ghost.method_36457(0.0F);
      ghost.method_36456(0.0F);
      ghost.field_6241 = 0.0F;
      ghost.field_6283 = 0.0F;
      ghost.method_6033(Math.max(source.method_6032(), 1.0F));
      ghost.method_6073(0.0F);
      ghost.method_18799(class_243.field_1353);
      ghost.method_5875(true);
      ghost.method_5880(false);
      ghost.method_5665(class_2561.method_43470((isTotem ? "TotemGhost_" : "Ghost_") + profile.getId()));

      for (class_1304 slot : class_1304.values()) {
         ghost.method_5673(slot, source.method_6118(slot).method_7972());
      }

      return ghost;
   }

   @EventHandler
   public void onWorldRender(WorldRenderEvent e) {
      if (mc.field_1687 != null && mc.field_1724 != null) {
         class_4587 stack = e.getStack();
         float tickDelta = e.getPartialTicks();
         List<class_1297> toRemove = new ArrayList<>();
         this.renderEntities.forEach((entity, data) -> {
            if (System.currentTimeMillis() - data.getTimestamp() > data.getDurationMs()) {
               toRemove.add(entity);
            } else {
               this.renderSoul(stack, data, tickDelta);
            }
         });
         toRemove.forEach(this.renderEntities::remove);
         List<class_1297> totemRemove = new ArrayList<>();
         this.totemEntities.forEach((entity, data) -> {
            if (System.currentTimeMillis() - data.getTimestamp() > data.getDurationMs()) {
               totemRemove.add(entity);
            } else {
               this.renderSoul(stack, data, tickDelta);
            }
         });
         totemRemove.forEach(this.totemEntities::remove);
      }
   }

   private void renderSoul(class_4587 stack, KillEffect.EntityRenderData data, float tickDelta) {
      float timeProgress = Math.min(1.0F, (float)(System.currentTimeMillis() - data.getTimestamp()) / (float)data.getDurationMs());
      if (!(timeProgress >= 1.0F)) {
         if (this.isCrossEffect()) {
            int color = new Color(255, 255, 255, (int)(150.0F * (1.0F - timeProgress))).getRGB();
            float yaw = (float)Math.toRadians(data.getYaw() + 95.0F);
            class_243 pos = data.getStartPos();
            Render3D.drawLine(pos.method_1031(0.0, 0.0, 0.0), pos.method_1031(0.0, 3.0, 0.0), color, 5.0F, true);
            float armLength = 1.0F;
            float yOffset = 2.3F;
            class_243 start = pos.method_1031(-armLength * Math.sin(yaw), yOffset, armLength * Math.cos(yaw));
            class_243 end = pos.method_1031(armLength * Math.sin(yaw), yOffset, -armLength * Math.cos(yaw));
            Render3D.drawLine(start, end, color, 5.0F, true);
         } else if (this.isLightningEffect()) {
            this.renderLightning(data, timeProgress);
         } else if (this.isBurstEffect()) {
            this.renderBurst(data, timeProgress);
         } else if (this.isRingsEffect()) {
            this.renderRings(data, timeProgress);
         } else if (this.isGhostEffect()) {
            float easedProgress = this.smoothStep(timeProgress);
            float yOffset = easedProgress * (data.isTotem() ? 2.4F : 3.0F);
            int alpha = (int)(255.0F * (1.0F - easedProgress));
            float yaw = data.getYaw() + easedProgress * (data.isTotem() ? 360.0F : 420.0F);
            class_243 soulPos = data.getStartPos().method_1031(0.0, yOffset, 0.0);
            class_1309 ghostEntity = data.getGhostEntity();
            if (ghostEntity != null) {
               this.prepareGhostFrame(ghostEntity, soulPos);
               Render3D.drawEntity(ghostEntity, soulPos, yaw, alpha, stack, tickDelta);
            } else {
               Render3D.drawEntity(data.getEntity(), soulPos, yaw, alpha, stack, tickDelta);
            }
         }
      }
   }

   private void prepareGhostFrame(class_1309 ghost, class_243 soulPos) {
      ghost.method_23327(soulPos.field_1352, soulPos.field_1351, soulPos.field_1350);
      ghost.method_18380(class_4050.field_18076);
      ghost.method_36457(0.0F);
      ghost.method_36456(0.0F);
      ghost.field_6241 = 0.0F;
      ghost.field_6283 = 0.0F;
      ghost.method_18799(class_243.field_1353);
      ghost.method_24830(false);
   }

   // Процедурная молния: рваный ствол сверху вниз в точку смерти, с ответвлениями,
   // короткая яркая вспышка с мерцанием, гаснет в первые ~45% длительности.
   private void renderLightning(KillEffect.EntityRenderData data, float timeProgress) {
      float life = Math.min(1.0F, timeProgress / 0.45F);
      if (life >= 1.0F) {
         return;
      }

      float flicker = 0.65F + 0.35F * (float)Math.abs(Math.sin(System.currentTimeMillis() / 40.0));
      int alpha = (int)(255.0F * (1.0F - life) * flicker);
      if (alpha <= 4) {
         return;
      }

      int color = new Color(210, 230, 255, alpha).getRGB();
      class_243 base = data.getStartPos();
      double height = 14.0;
      int segments = 12;
      java.util.Random rnd = new java.util.Random(data.getEntity().method_5628());
      class_243 prev = base.method_1031(0.0, height, 0.0);

      for (int i = 1; i <= segments; i++) {
         double t = (double)i / segments;
         double jitter = (1.0 - t) * 0.6;
         double jx = (rnd.nextDouble() * 2.0 - 1.0) * jitter;
         double jz = (rnd.nextDouble() * 2.0 - 1.0) * jitter;
         class_243 cur = base.method_1031(jx, height * (1.0 - t), jz);
         Render3D.drawLine(prev, cur, color, 4.0F, true);
         if (i % 4 == 0 && i < segments) {
            double bx = jx + (rnd.nextDouble() * 2.0 - 1.0) * 1.2;
            double bz = jz + (rnd.nextDouble() * 2.0 - 1.0) * 1.2;
            class_243 branch = base.method_1031(bx, height * (1.0 - t) - 1.2, bz);
            Render3D.drawLine(cur, branch, color, 2.5F, true);
         }

         prev = cur;
      }
   }

   private boolean isCrossEffect() {
      return this.effectType.isSelected("Крест") || this.effectType.isSelected("Cross");
   }

   private boolean isGhostEffect() {
      return this.effectType.isSelected("Душа") || this.effectType.isSelected("Ghost");
   }

   private boolean isLightningEffect() {
      return this.effectType.isSelected("Молния") || this.effectType.isSelected("Lightning");
   }

   private boolean isBurstEffect() {
      return this.effectType.isSelected("Вспышка");
   }

   private boolean isRingsEffect() {
      return this.effectType.isSelected("Кольца");
   }

   private void renderBurst(KillEffect.EntityRenderData data, float progress) {
      float eased = this.smoothStep(progress);
      int alpha = (int)(255.0F * (1.0F - eased) * (1.0F - eased));
      int color = ColorAssist.setAlpha(this.effectColor.getColor(), alpha);
      double radius = 0.25 + eased * 4.5;
      class_243 center = data.getStartPos().method_1031(0.0, 1.0, 0.0);
      java.util.Random random = new java.util.Random(data.getEntity().method_5628() * 31L + 17L);
      for (int i = 0; i < 30; i++) {
         class_243 direction = new class_243(
            random.nextDouble() * 2.0 - 1.0,
            random.nextDouble() * 1.7 - 0.35,
            random.nextDouble() * 2.0 - 1.0
         ).method_1029();
         class_243 start = center.method_1019(direction.method_1021(radius * 0.35));
         class_243 end = center.method_1019(direction.method_1021(radius));
         Render3D.drawLine(start, end, color, 2.5F, true);
      }
   }

   private void renderRings(KillEffect.EntityRenderData data, float progress) {
      class_243 base = data.getStartPos();
      int segments = 56;
      for (int ring = 0; ring < 4; ring++) {
         float local = Math.max(0.0F, Math.min(1.0F, progress * 1.35F - ring * 0.12F));
         int alpha = (int)(220.0F * (1.0F - local));
         int color = ColorAssist.setAlpha(this.effectColor.getColor(), alpha);
         double radius = 0.35 + local * (2.5 + ring * 0.25);
         double y = 0.15 + ring * 0.42 + local * 1.2;
         class_243 previous = base.method_1031(radius, y, 0.0);
         for (int i = 1; i <= segments; i++) {
            double angle = Math.PI * 2.0 * i / segments;
            class_243 point = base.method_1031(Math.cos(angle) * radius, y, Math.sin(angle) * radius);
            Render3D.drawLine(previous, point, color, 2.5F, true);
            previous = point;
         }
      }
   }

   private void playKillSound() {
      if (!this.killSound.isValue()) {
         return;
      }

      if (this.soundMode.isSelected("Свой файл")) {
         File directory = killSoundDirectory();
         directory.mkdirs();
         String configured = this.soundFile.getText();
         String safeName = new File(configured == null || configured.isBlank() ? "kill.ogg" : configured).getName();
         SoundManager.playFile(new File(directory, safeName), this.soundVolume.getValue());
      } else {
         SoundManager.playSound(SoundManager.ORTHODOX, this.soundVolume.getValue(), 1.0F);
      }
   }

   public static File killSoundDirectory() {
      return new File(new File(mc.field_1697, "AstralVisuals/Custom"), "KillSounds");
   }

   private float smoothStep(float value) {
      return value * value * (3.0F - 2.0F * value);
   }

   private static class EntityRenderData {
      private final long timestamp;
      private final long durationMs;
      private final float yaw;
      private final class_243 startPos;
      private final class_1297 entity;
      private final class_1309 ghostEntity;
      private final boolean totem;

      public EntityRenderData(long timestamp, long durationMs, float yaw, class_243 startPos, class_1297 entity, class_1309 ghostEntity, boolean totem) {
         this.timestamp = timestamp;
         this.durationMs = durationMs;
         this.yaw = yaw;
         this.startPos = startPos;
         this.entity = entity;
         this.ghostEntity = ghostEntity;
         this.totem = totem;
      }

      public long getTimestamp() {
         return this.timestamp;
      }

      public long getDurationMs() {
         return this.durationMs;
      }

      public float getYaw() {
         return this.yaw;
      }

      public class_243 getStartPos() {
         return this.startPos;
      }

      public class_1297 getEntity() {
         return this.entity;
      }

      public class_1309 getGhostEntity() {
         return this.ghostEntity;
      }

      public boolean isTotem() {
         return this.totem;
      }
   }
}
