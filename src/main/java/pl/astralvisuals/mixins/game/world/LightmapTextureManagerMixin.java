package pl.astralvisuals.mixins.game.world;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import net.minecraft.class_765;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import pl.astralvisuals.features.impl.render.NoRender;
import pl.astralvisuals.features.impl.render.WorldTweaks;

@Mixin(class_765.class)
public class LightmapTextureManagerMixin {
   @ModifyExpressionValue(method = "update(F)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/option/SimpleOption;getValue()Ljava/lang/Object;"))
   private Object injectXRayFullBright(Object original) {
      WorldTweaks tweaks = WorldTweaks.getInstance();
      return tweaks.isState() && tweaks.modeSetting.isSelected("Яркость")
         ? Math.max((Double)original, (double)(tweaks.brightSetting.getValue() * 10.0F))
         : original;
   }

   @Inject(method = "getDarknessFactor", at = @At("HEAD"), cancellable = true)
   private void removeDarkness(float delta, CallbackInfoReturnable<Float> cir) {
      NoRender noRender = NoRender.getInstance();
      if (noRender != null && noRender.isState() && noRender.modeSetting.isSelected("Темнота")) {
         cir.setReturnValue(0.0F);
      }
   }

   @Inject(method = "getDarkness", at = @At("HEAD"), cancellable = true)
   private void removeDarknessEffect(CallbackInfoReturnable<Float> cir) {
      NoRender noRender = NoRender.getInstance();
      if (noRender != null && noRender.isState() && noRender.modeSetting.isSelected("Темнота")) {
         cir.setReturnValue(0.0F);
      }
   }

   @Inject(
      method = "update",
      at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/LightmapTextureManager;getDarknessFactor(F)F"),
      cancellable = true
   )
   private void cancelDarknessInUpdate(float delta, CallbackInfo ci) {
      NoRender noRender = NoRender.getInstance();
      if (noRender == null || !noRender.isState() || !noRender.modeSetting.isSelected("Темнота")) {
         ;
      }
   }
}
