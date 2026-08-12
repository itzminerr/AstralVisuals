package pl.astralvisuals.mixins.client.screen.mainmenu;

import net.minecraft.class_2960;
import net.minecraft.class_310;
import net.minecraft.class_332;
import net.minecraft.class_751;
import net.minecraft.class_766;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(class_766.class)
public class RotatingCubeMapRendererMixin {
   private static final class_751 CUSTOM_PANORAMA_RENDERER = new class_751(class_2960.method_60655("minecraft", "panorama/panorama"));
   private static float customPitch = 0.0F;

   @Inject(method = "render", at = @At("HEAD"), cancellable = true)
   private void renderCustomPanorama(class_332 context, int width, int height, float alpha, float tickDelta, CallbackInfo ci) {
      class_310 client = class_310.method_1551();
      float f = client.method_61966().method_60638();
      float g = (float)(f * (Double)client.field_1690.method_45581().method_41753());
      customPitch = wrapOnce(customPitch + g * 0.1F, 360.0F);
      context.method_51452();
      CUSTOM_PANORAMA_RENDERER.method_3156(client, 10.0F, -customPitch, alpha);
      context.method_51452();
      ci.cancel();
   }

   private static float wrapOnce(float a, float b) {
      return a > b ? a - b : a;
   }
}
