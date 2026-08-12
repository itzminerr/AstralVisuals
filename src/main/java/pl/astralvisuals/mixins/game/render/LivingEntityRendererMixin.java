package pl.astralvisuals.mixins.game.render;

import net.minecraft.class_1309;
import net.minecraft.class_922;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import pl.astralvisuals.features.impl.render.SelfNametag;
import pl.astralvisuals.utils.display.interfaces.QuickImports;

// class_922 = LivingEntityRenderer, method_4055(class_1309, double) = hasLabel.
// Форсим показ лейбла для собственного игрока, когда включён SelfNametag (виден от 3-го лица).
@Mixin(class_922.class)
public class LivingEntityRendererMixin implements QuickImports {
   @Inject(method = "method_4055(Lnet/minecraft/class_1309;D)Z", at = @At("HEAD"), cancellable = true, remap = false)
   private void astral$selfNametag(class_1309 entity, double dist, CallbackInfoReturnable<Boolean> cir) {
      SelfNametag module = SelfNametag.getInstance();
      if (module != null && module.isState() && mc.field_1724 != null && entity == mc.field_1724) {
         cir.setReturnValue(true);
      }
   }
}
