package pl.astralvisuals.mixins.player.entity;

import net.minecraft.class_10034;
import net.minecraft.class_10055;
import net.minecraft.class_4587;
import net.minecraft.class_4597;
import net.minecraft.class_591;
import net.minecraft.class_970;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import pl.astralvisuals.features.impl.render.custommodels.CustomPlayerModelRenderer;
import pl.astralvisuals.utils.client.interfaces.ICustomPlayerModelState;

@Mixin(class_970.class)
public class ArmorFeatureRendererCustomModelsMixin {
   // прячем броню, когда у игрока активна кастомная модель
   @Inject(
      method = "method_17157(Lnet/minecraft/class_4587;Lnet/minecraft/class_4597;ILnet/minecraft/class_10034;FF)V",
      at = @At("HEAD"),
      cancellable = true,
      remap = false
   )
   private void astral$hideArmorForCustomModels(
      class_4587 matrices, class_4597 vertexConsumers, int light, class_10034 state, float limbAngle, float limbDistance, CallbackInfo ci
   ) {
      if (state instanceof ICustomPlayerModelState customState && customState.astral$hasCustomModel()) {
         ci.cancel();
         class_10055 renderState = PlayerEntityRendererCustomModelsMixin.astral$getState();
         class_591 model = PlayerEntityRendererCustomModelsMixin.astral$getModel();
         if (renderState != null && model != null) {
            CustomPlayerModelRenderer.render(customState.astral$getCustomModel(), model, renderState, matrices, vertexConsumers, light, 0, 0xFFFFFFFF);
         }
      }
   }
}
