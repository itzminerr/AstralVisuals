package pl.astralvisuals.mixins.client.mouse;

import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import net.minecraft.class_310;
import net.minecraft.class_312;
import net.minecraft.class_746;
import net.minecraft.class_3675.class_307;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import pl.astralvisuals.events.keyboard.HotBarScrollEvent;
import pl.astralvisuals.events.keyboard.KeyEvent;
import pl.astralvisuals.events.keyboard.MouseRotationEvent;
import pl.astralvisuals.events.render.FovEvent;
import pl.astralvisuals.utils.client.managers.event.EventManager;

@Mixin(class_312.class)
public class MouseMixin {
   @Final
   @Shadow
   private class_310 field_1779;
   @Shadow
   public double field_1789;
   @Shadow
   public double field_1787;
   @Shadow
   private boolean field_1783;
   @Shadow
   private double field_1795;
   @Shadow
   private double field_1794;
   @Unique
   private boolean astral$discardMouseMotionAfterClick;

   @Inject(method = "onMouseButton", at = @At("HEAD"))
   public void onMouseButtonHook(long window, int button, int action, int mods, CallbackInfo ci) {
      if (button != -1 && window == this.field_1779.method_22683().method_4490()) {
         if (action == 1 && this.field_1783 && this.field_1779.field_1755 == null) {
            // GLFW/XWayland may deliver a stale absolute cursor position together with a click.
            // Drop all motion already accumulated in this input batch and rebase any following
            // cursor callbacks until Minecraft consumes the batch in updateMouse().
            this.astral$discardMouseMotionAfterClick = true;
            this.field_1789 = 0.0;
            this.field_1787 = 0.0;
         }

         EventManager.callEvent(new KeyEvent(this.field_1779.field_1755, class_307.field_1672, button, action));
      }
   }

   @Inject(method = "method_1600", at = @At("HEAD"), cancellable = true)
   private void astral$discardStaleCursorPosition(long window, double cursorX, double cursorY, CallbackInfo ci) {
      if (this.astral$discardMouseMotionAfterClick && window == this.field_1779.method_22683().method_4490()) {
         // Keep vanilla's coordinate baseline in sync so the first accepted event cannot include
         // the distance from the rejected (usually absolute) XWayland coordinate.
         this.field_1795 = cursorX;
         this.field_1794 = cursorY;
         this.field_1789 = 0.0;
         this.field_1787 = 0.0;
         ci.cancel();
      }
   }

   @Inject(
      method = "onMouseScroll",
      at = @At(value = "INVOKE", target = "Lnet/minecraft/client/network/ClientPlayerEntity;getInventory()Lnet/minecraft/entity/player/PlayerInventory;"),
      cancellable = true
   )
   public void onMouseScrollHook(long window, double horizontal, double vertical, CallbackInfo ci) {
      HotBarScrollEvent event = new HotBarScrollEvent(horizontal, vertical);
      EventManager.callEvent(event);
      if (event.isCancelled()) {
         ci.cancel();
      }
   }

   @Inject(method = "updateMouse", at = @At("HEAD"))
   private void onUpdateMouse(double timeDelta, CallbackInfo ci) {
      if (this.astral$discardMouseMotionAfterClick) {
         this.field_1789 = 0.0;
         this.field_1787 = 0.0;
         this.astral$discardMouseMotionAfterClick = false;
      }

      FovEvent event = new FovEvent();
      EventManager.callEvent(event);
      if (event.isCancelled()) {
         double slowdown = (double)event.getFov() / ((Integer)this.field_1779.field_1690.method_41808().method_41753()).intValue();
         this.field_1789 *= slowdown;
         this.field_1787 *= slowdown;
      }
   }

   @WrapWithCondition(
      method = "updateMouse",
      at = @At(value = "INVOKE", target = "Lnet/minecraft/client/network/ClientPlayerEntity;changeLookDirection(DD)V"),
      require = 1,
      allow = 1
   )
   private boolean modifyMouseRotationInput(class_746 instance, double cursorDeltaX, double cursorDeltaY) {
      MouseRotationEvent event = new MouseRotationEvent((float)cursorDeltaX, (float)cursorDeltaY);
      EventManager.callEvent(event);
      if (event.isCancelled()) {
         return false;
      } else {
         instance.method_5872(event.getCursorDeltaX(), event.getCursorDeltaY());
         return false;
      }
   }
}
