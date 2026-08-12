package pl.astralvisuals.mixins.client.screen.ingame;

import java.util.ConcurrentModificationException;
import net.minecraft.class_1657;
import net.minecraft.class_1702;
import net.minecraft.class_1921;
import net.minecraft.class_2960;
import net.minecraft.class_310;
import net.minecraft.class_329;
import net.minecraft.class_332;
import net.minecraft.class_9779;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.At.Shift;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import pl.astralvisuals.Force;
import pl.astralvisuals.events.render.DrawEvent;
import pl.astralvisuals.features.impl.render.CrossHair;
import pl.astralvisuals.features.impl.render.Interface;
import pl.astralvisuals.utils.client.managers.event.EventManager;
import pl.astralvisuals.utils.display.geometry.Render2D;
import pl.astralvisuals.utils.display.interfaces.QuickImports;
import pl.astralvisuals.utils.display.scale.UiScale;
import pl.astralvisuals.utils.math.calc.Calculate;

@Mixin(class_329.class)
public abstract class InGameHudMixin implements QuickImports {
   @Unique
   private final Interface interfaceModule = Interface.getInstance();
   @Final
   @Shadow
   private class_310 field_2035;
   @Final
   @Shadow
   private static class_2960 field_45327;
   @Final
   @Shadow
   private static class_2960 field_45328;
   @Final
   @Shadow
   private static class_2960 field_45298;

   @Inject(
      method = "render",
      at = @At(
         value = "INVOKE",
         target = "Lnet/minecraft/client/gui/LayeredDrawer;render(Lnet/minecraft/client/gui/DrawContext;Lnet/minecraft/client/render/RenderTickCounter;)V",
         shift = Shift.AFTER
      )
   )
   public void onRender(class_332 context, class_9779 tickCounter, CallbackInfo ci) {
      blur.setup();
      DrawEvent event = new DrawEvent(context, drawEngine, tickCounter.method_60637(false));
      EventManager.callEvent(event);
      Render2D.onRender(context);
      boolean debugHudVisible = this.field_2035.method_53526().method_53536();
      if (!this.field_2035.field_1690.field_1842 && !debugHudVisible) {
         context.method_51448().method_22903();
         context.method_51448().method_46416(0.0F, 0.0F, 400.0F);
         Force.getInstance().getDraggableRepository().draggable().forEach(draggable -> {
            if (draggable.canDraw(this.interfaceModule, draggable)) {
               draggable.startAnimation();
            } else {
               draggable.stopAnimation();
            }

            float scale = draggable.getScaleAnimation().getOutput().floatValue();
            if (!draggable.isCloseAnimationFinished()) {
               draggable.validPosition();
               float hudScale = this.interfaceModule.getHudScale();
               float centerX = draggable.getX() + draggable.getWidth() / 2.0F;
               float centerY = draggable.getY() + draggable.getHeight() / 2.0F;

               try {
                  context.method_51448().method_22903();
                  UiScale.render(hudScale, centerX, centerY, () -> Calculate.setAlpha(scale, () -> draggable.drawDraggable(context)));
               } catch (ConcurrentModificationException var5x) {
               } finally {
                  context.method_51448().method_22909();
               }
            }
         });
         context.method_51448().method_22909();
      }
   }

   @Inject(
      method = "renderCrosshair",
      at = @At(value = "FIELD", target = "Lnet/minecraft/client/gui/hud/InGameHud;CROSSHAIR_TEXTURE:Lnet/minecraft/util/Identifier;"),
      cancellable = true
   )
   public void renderCrosshairHook(class_332 context, class_9779 tickCounter, CallbackInfo ci) {
      CrossHair crossHair = CrossHair.getInstance();
      if (crossHair.isState()) {
         crossHair.onRenderCrossHair();
         ci.cancel();
      }
   }

   @Inject(at = @At("HEAD"), method = "renderStatusEffectOverlay", cancellable = true)
   public void renderStatusEffectOverlayHook(class_332 context, class_9779 tickCounter, CallbackInfo ci) {
      if (this.interfaceModule.isState() && this.interfaceModule.interfaceSettings.isSelected("Эффекты")) {
         ci.cancel();
      }
   }

   @Inject(method = "renderFood", at = @At("TAIL"))
   private void astralvisual$renderAppleSkin(class_332 context, class_1657 player, int top, int right, CallbackInfo ci) {
      if (player != null) {
         class_1702 hungerManager = player.method_7344();
         float saturation = Math.min(hungerManager.method_7589(), (float)hungerManager.method_7586());
         if (!(saturation <= 0.0F)) {
            for (int index = 0; index < 10; index++) {
               float iconSaturation = Math.max(0.0F, Math.min(2.0F, saturation - index * 2.0F));
               int x = right - index * 8 - 9;
               int y = top - 10;
               context.method_52706(class_1921::method_62277, field_45327, x, y, 9, 9);
               if (iconSaturation >= 2.0F) {
                  context.method_52706(class_1921::method_62277, field_45298, x, y, 9, 9);
               } else if (iconSaturation > 0.0F) {
                  context.method_52706(class_1921::method_62277, field_45328, x, y, 9, 9);
               }
            }
         }
      }
   }
}
