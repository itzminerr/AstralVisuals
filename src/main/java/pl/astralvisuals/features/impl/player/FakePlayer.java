package pl.astralvisuals.features.impl.player;

import com.mojang.authlib.GameProfile;
import java.util.UUID;
import net.minecraft.class_1268;
import net.minecraft.class_1294;
import net.minecraft.class_1297;
import net.minecraft.class_1304;
import net.minecraft.class_1324;
import net.minecraft.class_1799;
import net.minecraft.class_1802;
import net.minecraft.class_243;
import net.minecraft.class_2561;
import net.minecraft.class_3414;
import net.minecraft.class_3417;
import net.minecraft.class_3419;
import net.minecraft.class_3532;
import net.minecraft.class_4050;
import net.minecraft.class_5134;
import net.minecraft.class_638;
import net.minecraft.class_745;
import net.minecraft.class_746;
import net.minecraft.class_1297.class_5529;
import pl.astralvisuals.events.player.AttackEvent;
import pl.astralvisuals.events.player.TickEvent;
import pl.astralvisuals.features.impl.render.KillEffect;
import pl.astralvisuals.features.module.Module;
import pl.astralvisuals.features.module.ModuleCategory;
import pl.astralvisuals.features.module.setting.implement.SliderSettings;
import pl.astralvisuals.utils.client.managers.event.EventHandler;

public class FakePlayer extends Module {
   static final int FAKE_PLAYER_ENTITY_ID = -90621;
   static final float HIT_DAMAGE_HP = 2.0F;
   static final float LOOK_SMOOTH = 0.35F;
   private final SliderSettings distance = new SliderSettings("Дистанция", "Расстояние до фейк-игрока").setValue(3.0F).range(1, 6);
   private FakePlayer.PracticePlayerEntity fakePlayer;
   private class_638 spawnedWorld;
   private class_243 spawnPos;
   private long lastAttackTime;

   public FakePlayer() {
      super("FakePlayer", "Fake Player", ModuleCategory.PLAYER);
      this.setup(this.distance);
   }

   @Override
   public void activate() {
      this.spawnPos = null;
      this.trySpawnFakePlayer();
   }

   @Override
   public void deactivate() {
      this.removeFakePlayer();
   }

   @EventHandler
   public void onTick(TickEvent event) {
      if (mc.field_1724 != null && mc.field_1687 != null) {
         if (this.spawnedWorld != null && this.spawnedWorld != mc.field_1687) {
            this.removeFakePlayer();
         }

         if (this.fakePlayer != null && mc.field_1687.method_8469(-90621) == this.fakePlayer) {
            this.syncFakePlayer();
            this.lookAtPlayer();
         } else {
            this.trySpawnFakePlayer();
         }
      } else {
         this.removeFakePlayer();
      }
   }

   @EventHandler
   public void onAttack(AttackEvent event) {
      if (mc.field_1724 != null && this.fakePlayer != null && event.getEntity() == this.fakePlayer) {
         event.cancel();
         float cooldown = mc.field_1724.method_7261(0.5F);
         boolean criticalHit = cooldown >= 0.95F && this.isCriticalHit();
         if (criticalHit) {
            mc.field_1724.method_7277(this.fakePlayer);
            this.playSoundAt(this.fakePlayer, class_3417.field_15016);
         } else if (cooldown >= 0.95F) {
            this.playSoundAt(this.fakePlayer, class_3417.field_14840);
         } else {
            this.playSoundAt(this.fakePlayer, class_3417.field_14625);
         }

         this.playSoundAt(this.fakePlayer, class_3417.field_15115);
         this.fakePlayer.setHurtFlash(criticalHit ? 8 : 6);
         float nextHealth = this.fakePlayer.method_6032() - 2.0F;
         if (nextHealth > 0.0F) {
            this.fakePlayer.method_6033(nextHealth);
         } else if (!this.fakePlayer.popTotem()) {
            this.fakePlayer.method_6033(this.fakePlayer.method_6063());
         } else {
            this.playSoundAt(this.fakePlayer, class_3417.field_14931);
            KillEffect killEffect = KillEffect.getInstance();
            if (killEffect != null) {
               killEffect.spawnTotemEffect(this.fakePlayer);
            }
         }

         this.fakePlayer.stabilize(this.spawnPos);
         mc.field_1724.method_7350();
         this.lastAttackTime = System.currentTimeMillis();
      }
   }

   private void trySpawnFakePlayer() {
      class_746 player = mc.field_1724;
      class_638 world = mc.field_1687;
      if (player != null && world != null) {
         this.removeFakePlayer();
         if (this.spawnPos == null || this.spawnedWorld != world) {
            this.spawnPos = this.calculateSpawnPos(player);
         }

         FakePlayer.PracticePlayerEntity entity = new FakePlayer.PracticePlayerEntity(world, new GameProfile(UUID.randomUUID(), "AstralVisuals"));
         entity.method_5838(-90621);
         entity.method_18380(class_4050.field_18076);
         entity.method_5665(class_2561.method_43470("AstralVisuals"));
         entity.method_5880(true);
         entity.method_5875(true);
         entity.method_5673(class_1304.field_6169, new class_1799(class_1802.field_22027));
         entity.method_5673(class_1304.field_6174, new class_1799(class_1802.field_22028));
         entity.method_5673(class_1304.field_6172, new class_1799(class_1802.field_22029));
         entity.method_5673(class_1304.field_6166, new class_1799(class_1802.field_22030));
         entity.method_5673(class_1304.field_6173, new class_1799(class_1802.field_22022));
         entity.method_5673(class_1304.field_6171, new class_1799(class_1802.field_8288));
         entity.method_6033(entity.method_6063());
         entity.method_6073(0.0F);
         class_1324 knockbackResistance = entity.method_5996(class_5134.field_23718);
         if (knockbackResistance != null) {
            knockbackResistance.method_6192(1.0);
         }

         this.fakePlayer = entity;
         this.spawnedWorld = world;
         this.lastAttackTime = 0L;
         this.syncFakePlayer();
         world.method_53875(entity);
         entity.syncPosition(this.spawnPos, player.method_36454(), player.method_36455());
         this.lookAtPlayer();
      }
   }

   private void syncFakePlayer() {
      if (this.fakePlayer != null && this.spawnPos != null) {
         this.fakePlayer.field_6008 = 0;
         this.fakePlayer.field_6213 = 0;
         this.fakePlayer.method_24830(true);
         this.fakePlayer.method_5728(false);
         this.fakePlayer.method_5660(false);
         this.fakePlayer.method_6073(0.0F);
         this.fakePlayer.stabilize(this.spawnPos);
      }
   }

   private void lookAtPlayer() {
      if (this.fakePlayer != null && mc.field_1724 != null) {
         class_243 from = new class_243(this.fakePlayer.method_23317(), this.fakePlayer.method_23320(), this.fakePlayer.method_23321());
         class_243 to = new class_243(mc.field_1724.method_23317(), mc.field_1724.method_23320(), mc.field_1724.method_23321());
         class_243 delta = to.method_1020(from);
         double distXZ = Math.sqrt(delta.field_1352 * delta.field_1352 + delta.field_1350 * delta.field_1350);
         if (!(distXZ < 1.0E-4)) {
            float targetYaw = (float)(class_3532.method_15349(delta.field_1350, delta.field_1352) * (180.0 / Math.PI)) - 90.0F;
            float targetPitch = (float)(-(class_3532.method_15349(delta.field_1351, distXZ) * (180.0 / Math.PI)));
            float yaw = this.fakePlayer.method_36454() + class_3532.method_15393(targetYaw - this.fakePlayer.method_36454()) * 0.35F;
            float pitch = this.fakePlayer.method_36455() + (targetPitch - this.fakePlayer.method_36455()) * 0.35F;
            this.fakePlayer.syncPosition(this.spawnPos, yaw, class_3532.method_15363(pitch, -89.9F, 89.9F));
         }
      }
   }

   private boolean isCriticalHit() {
      if (mc.field_1724 == null) {
         return false;
      } else if (mc.field_1724.method_24828()) {
         return false;
      } else if (mc.field_1724.method_6101()) {
         return false;
      } else if (mc.field_1724.method_5799() || mc.field_1724.method_5869()) {
         return false;
      } else if (mc.field_1724.method_5771()) {
         return false;
      } else if (mc.field_1724.method_5765()) {
         return false;
      } else if (mc.field_1724.method_5624()) {
         return false;
      } else if (mc.field_1724.method_31549().field_7479 || mc.field_1724.method_6128()) {
         return false;
      } else {
         return mc.field_1724.method_6059(class_1294.field_5919) ? false : mc.field_1724.field_6017 > 0.0F;
      }
   }

   private void playSoundAt(class_1297 entity, class_3414 sound) {
      if (mc.field_1687 != null && entity != null && sound != null) {
         mc.field_1687.method_55116(entity, sound, class_3419.field_15248, 1.0F, 1.0F);
      }
   }

   private class_243 calculateSpawnPos(class_746 player) {
      double yaw = Math.toRadians(player.method_36454());
      class_243 forward = new class_243(-Math.sin(yaw), 0.0, Math.cos(yaw));
      return player.method_19538().method_1019(forward.method_1021(this.distance.getValue()));
   }

   private void removeFakePlayer() {
      if (this.spawnedWorld != null && this.fakePlayer != null) {
         this.spawnedWorld.method_2945(-90621, class_5529.field_26999);
      }

      this.fakePlayer = null;
      this.spawnedWorld = null;
      this.spawnPos = null;
      this.lastAttackTime = 0L;
   }

   private static class PracticePlayerEntity extends class_745 {
      public PracticePlayerEntity(class_638 world, GameProfile profile) {
         super(world, profile);
      }


      public void setHurtFlash(int ticks) {
         this.field_6235 = Math.max(1, ticks);
         this.field_6254 = Math.max(1, ticks);
         this.field_6008 = 0;
         this.field_6213 = 0;
         this.field_6253 = 0.0F;
      }

      public void stabilize(class_243 pos) {
         this.field_6008 = 0;
         this.field_6213 = 0;
         this.method_18800(0.0, 0.0, 0.0);
         this.syncPosition(pos, this.method_36454(), this.method_36455());
      }

      public void syncPosition(class_243 pos, float yaw, float pitch) {
         this.method_5808(pos.field_1352, pos.field_1351, pos.field_1350, yaw, pitch);
         this.method_43391(pos.field_1352, pos.field_1351, pos.field_1350);
         this.method_5759(pos.field_1352, pos.field_1351, pos.field_1350, yaw, pitch, 0);
         this.field_6014 = pos.field_1352;
         this.field_6036 = pos.field_1351;
         this.field_5969 = pos.field_1350;
         this.field_6038 = pos.field_1352;
         this.field_5971 = pos.field_1351;
         this.field_5989 = pos.field_1350;
         this.method_36456(yaw);
         this.method_36457(pitch);
         this.field_6241 = yaw;
         this.field_6283 = yaw;
      }

      public boolean popTotem() {
         class_1799 offHand = this.method_6079();
         class_1799 mainHand = this.method_6047();
         boolean offTotem = !offHand.method_7960() && offHand.method_31574(class_1802.field_8288);
         boolean mainTotem = !mainHand.method_7960() && mainHand.method_31574(class_1802.field_8288);
         if (!offTotem && !mainTotem) {
            return false;
         } else {
            if (offTotem) {
               this.method_6122(class_1268.field_5810, class_1799.field_8037);
            } else {
               this.method_6122(class_1268.field_5808, class_1799.field_8037);
            }

            this.method_6033(this.method_6063());
            this.method_6073(0.0F);
            this.method_6122(class_1268.field_5810, new class_1799(class_1802.field_8288));
            this.setHurtFlash(10);
            return true;
         }
      }
   }
}
