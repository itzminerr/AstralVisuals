package pl.astralvisuals.mixins.game.render;

import net.minecraft.class_1937;
import net.minecraft.class_243;
import net.minecraft.class_4066;
import net.minecraft.class_4184;
import net.minecraft.class_4597;
import net.minecraft.class_638;
import net.minecraft.class_9976;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import pl.astralvisuals.features.impl.render.NoRender;

// Скрытие погоды (дождь/снег) в NoRender. class_9976 = WeatherRendering.
@Mixin(class_9976.class)
public class WeatherRenderingMixin {
   // method_62316 — отрисовка осадков (струи дождя/снега).
   @Inject(method = "method_62316", at = @At("HEAD"), cancellable = true, remap = false)
   private void astral$noPrecipitation(
      class_1937 world, class_4597 vertexConsumers, int ticks, float tickDelta, class_243 pos, CallbackInfo ci
   ) {
      if (isWeatherHidden()) {
         ci.cancel();
      }
   }

   // method_62319 — брызги дождя по земле.
   @Inject(method = "method_62319", at = @At("HEAD"), cancellable = true, remap = false)
   private void astral$noSplashes(class_638 world, class_4184 camera, int ticks, class_4066 particlesMode, CallbackInfo ci) {
      if (isWeatherHidden()) {
         ci.cancel();
      }
   }

   private static boolean isWeatherHidden() {
      NoRender noRender = NoRender.getInstance();
      return noRender != null && noRender.isState() && noRender.modeSetting.isSelected(NoRender.WEATHER);
   }
}
