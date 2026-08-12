package pl.astralvisuals.mixins.client.display.background;

import net.minecraft.class_1297;
import net.minecraft.class_1309;
import net.minecraft.class_4184;
import net.minecraft.class_638;
import net.minecraft.class_6854;
import net.minecraft.class_758;
import net.minecraft.class_9958;
import net.minecraft.class_758.class_4596;
import org.joml.Vector4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import pl.astralvisuals.events.render.FogEvent;
import pl.astralvisuals.features.impl.render.NoRender;
import pl.astralvisuals.utils.client.managers.event.EventManager;
import pl.astralvisuals.utils.display.color.ColorAssist;

@Mixin(class_758.class)
public class BackGroundRendererMixin {
   @Inject(
      method = "getFogModifier(Lnet/minecraft/entity/Entity;F)Lnet/minecraft/client/render/BackgroundRenderer$StatusEffectFogModifier;",
      at = @At("HEAD"),
      cancellable = true
   )
   private static void onGetFogModifier(class_1297 entity, float tickDelta, CallbackInfoReturnable<Object> info) {
      NoRender noRender = NoRender.getInstance();
      if (noRender.isState() && noRender.modeSetting.isSelected("Негативные эффекты")) {
         info.setReturnValue(null);
      }

      if (noRender.isState() && noRender.modeSetting.isSelected("Темнота") && entity instanceof class_1309) {
         info.setReturnValue(null);
      }
   }

   @Inject(method = "getFogColor", at = @At("HEAD"), cancellable = true)
   private static void getFogColorHook(
      class_4184 camera, float tickDelta, class_638 world, int clampedViewDistance, float skyDarkness, CallbackInfoReturnable<Vector4f> cir
   ) {
      FogEvent event = new FogEvent();
      EventManager.callEvent(event);
      if (event.isCancelled()) {
         int color = event.getColor();
         cir.setReturnValue(new Vector4f(ColorAssist.redf(color), ColorAssist.greenf(color), ColorAssist.bluef(color), ColorAssist.alphaf(color)));
      }
   }

   @Inject(method = "applyFog", at = @At("HEAD"), cancellable = true)
   private static void modifyFog(
      class_4184 camera, class_4596 fogType, Vector4f vector4f, float viewDistance, boolean thickenFog, float tickDelta, CallbackInfoReturnable<class_9958> cir
   ) {
      FogEvent event = new FogEvent();
      EventManager.callEvent(event);
      if (event.isCancelled()) {
         int color = event.getColor();
         cir.setReturnValue(
            new class_9958(
               2.0F,
               event.getDistance(),
               class_6854.field_36351,
               ColorAssist.redf(color),
               ColorAssist.greenf(color),
               ColorAssist.bluef(color),
               ColorAssist.alphaf(color)
            )
         );
      }
   }
}
