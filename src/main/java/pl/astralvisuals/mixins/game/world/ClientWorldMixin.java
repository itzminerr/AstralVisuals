package pl.astralvisuals.mixins.game.world;

import net.minecraft.class_1297;
import net.minecraft.class_1297.class_5529;
import net.minecraft.class_1511;
import net.minecraft.class_2338;
import net.minecraft.class_2394;
import net.minecraft.class_2396;
import net.minecraft.class_2398;
import net.minecraft.class_243;
import net.minecraft.class_638;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import pl.astralvisuals.events.player.EntitySpawnEvent;
import pl.astralvisuals.events.render.WorldLoadEvent;
import pl.astralvisuals.features.impl.render.HitEffect;
import pl.astralvisuals.features.impl.render.NoRender;
import pl.astralvisuals.utils.client.managers.event.EventManager;
import pl.astralvisuals.utils.display.interfaces.QuickImports;
import pl.astralvisuals.utils.interactions.interact.PlayerInteractionHelper;

@Mixin(class_638.class)
public class ClientWorldMixin implements QuickImports {
   @Inject(method = "<init>", at = @At("RETURN"))
   public void initHook(CallbackInfo info) {
      EventManager.callEvent(new WorldLoadEvent());
   }

   // addParticle / addImportantParticle — единая точка спавна крит/разящих/взрывных партиклов
   // (крит приходит через EntityStatus, а не через ParticleS2CPacket, поэтому ловим именно здесь)
   @Inject(method = {"method_8406", "method_8494"}, at = @At("HEAD"), cancellable = true, remap = false)
   private void astral$cancelParticle(
      class_2394 parameters, double x, double y, double z, double vx, double vy, double vz, CallbackInfo ci
   ) {
      NoRender noRender = NoRender.getInstance();
      if (noRender == null || !noRender.isState()) {
         return;
      }

      class_2396<?> type = parameters.method_10295();
      boolean crit = (type == class_2398.field_11205 || type == class_2398.field_11208) && noRender.modeSetting.isSelected("Партиклы крита");
      boolean sweep = type == class_2398.field_11227 && noRender.modeSetting.isSelected("Партиклы разящего удара");
      boolean explosion = (type == class_2398.field_11221 || type == class_2398.field_11236)
         && noRender.modeSetting.isSelected("Партиклы взрыва кристалла");
      if (crit || sweep || explosion) {
         ci.cancel();
      }
   }

   // removeEntity(id, reason) — клиент удаляет сущность (в т.ч. взрыв энд-кристалла).
   // Событийный детект надёжнее поллинга по тикам: не пропускает быстрые кристаллы.
   @Inject(method = "method_2945", at = @At("HEAD"), remap = false)
   private void astral$crystalExplode(int id, class_5529 reason, CallbackInfo ci) {
      HitEffect hit = HitEffect.getInstance();
      if (hit == null || !hit.isState() || !hit.isCrystalWaveEnabled() || mc.field_1724 == null) {
         return;
      }

      class_1297 entity = ((class_638) (Object) this).method_8469(id);
      if (!(entity instanceof class_1511)) {
         return;
      }

      class_243 pos = entity.method_19538();
      if (mc.field_1724.method_19538().method_1022(pos) <= 24.0) {
         hit.addWave(class_2338.method_49637(pos.field_1352, pos.field_1351 - 0.1, pos.field_1350));
      }
   }

   @Inject(method = "addEntity", at = @At("HEAD"), cancellable = true)
   public void addEntityHook(class_1297 entity, CallbackInfo ci) {
      if (!PlayerInteractionHelper.nullCheck()) {
         EntitySpawnEvent event = new EntitySpawnEvent(entity);
         EventManager.callEvent(event);
         if (event.isCancelled()) {
            ci.cancel();
         }
      }
   }
}
