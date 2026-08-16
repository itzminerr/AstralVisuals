package pl.astralvisuals.mixins.game.render;

import java.util.ArrayDeque;
import net.minecraft.class_10042;
import net.minecraft.class_1309;
import net.minecraft.class_4587;
import net.minecraft.class_4597;
import net.minecraft.class_922;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.Unique;
import pl.astralvisuals.features.impl.movement.EggMan;
import pl.astralvisuals.features.impl.render.BabyMod;
import pl.astralvisuals.features.impl.render.SelfNametag;
import pl.astralvisuals.utils.client.render.RenderStateEntityCache;
import pl.astralvisuals.utils.display.interfaces.QuickImports;

// class_922 = LivingEntityRenderer, method_4055(class_1309, double) = hasLabel.
// Форсим показ лейбла для собственного игрока, когда включён SelfNametag (виден от 3-го лица).
@Mixin(class_922.class)
public class LivingEntityRendererMixin implements QuickImports {
   @Unique
   private static final ThreadLocal<ArrayDeque<Boolean>> astral$scaleStack = ThreadLocal.withInitial(ArrayDeque::new);

   @Inject(method = "method_62355", at = @At("TAIL"), remap = false)
   private void astral$rememberEntity(class_1309 entity, class_10042 state, float tickDelta, CallbackInfo ci) {
      RenderStateEntityCache.put(state, entity);
   }

   @Inject(method = "method_4054", at = @At("HEAD"), remap = false)
   private void astral$applyScale(class_10042 state, class_4587 matrices, class_4597 consumers, int light, CallbackInfo ci) {
      class_1309 entity = RenderStateEntityCache.get(state);
      EggMan eggMan = EggMan.getInstance();
      BabyMod babyMod = BabyMod.getInstance();
      boolean wobble = entity != null && eggMan != null && eggMan.shouldWobble(entity);
      boolean baby = entity != null && babyMod != null && babyMod.shouldApply(entity);
      boolean changed = wobble || baby;
      astral$scaleStack.get().push(changed);
      if (changed) {
         matrices.method_22903();
         if (wobble) {
            eggMan.applyWobble(entity, matrices);
         }
         if (baby) {
            babyMod.applyScale(matrices);
         }
      }
   }

   @Inject(method = "method_4054", at = @At("RETURN"), remap = false)
   private void astral$restoreScale(class_10042 state, class_4587 matrices, class_4597 consumers, int light, CallbackInfo ci) {
      ArrayDeque<Boolean> stack = astral$scaleStack.get();
      if (!stack.isEmpty() && stack.pop()) {
         matrices.method_22909();
      }
   }

   @Inject(method = "method_4055(Lnet/minecraft/class_1309;D)Z", at = @At("HEAD"), cancellable = true, remap = false)
   private void astral$selfNametag(class_1309 entity, double dist, CallbackInfoReturnable<Boolean> cir) {
      SelfNametag module = SelfNametag.getInstance();
      if (module != null && module.isState() && mc.field_1724 != null && entity == mc.field_1724) {
         cir.setReturnValue(true);
      }
   }
}
