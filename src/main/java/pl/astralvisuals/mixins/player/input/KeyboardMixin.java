package pl.astralvisuals.mixins.player.input;

import net.minecraft.class_309;
import net.minecraft.class_310;
import net.minecraft.class_3675.class_307;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import pl.astralvisuals.commands.defaults.BindCommand;
import pl.astralvisuals.display.screens.clickgui.MenuScreen;
import pl.astralvisuals.events.keyboard.KeyEvent;
import pl.astralvisuals.utils.client.managers.event.EventManager;

@Mixin(class_309.class)
public class KeyboardMixin {
   @Final
   @Shadow
   private class_310 field_1678;

   @Inject(method = "onKey", at = @At("HEAD"))
   private void onKey(long window, int key, int scanCode, int action, int modifiers, CallbackInfo ci) {
      if (key != -1 && window == this.field_1678.method_22683().method_4490()) {
         if (action == 0 && key == BindCommand.ClickGuiManager.getClickGuiKey() && this.field_1678.field_1755 == null) {
            MenuScreen.INSTANCE.openGui();
         }

         EventManager.callEvent(new KeyEvent(this.field_1678.field_1755, class_307.field_1668, key, action));
      }
   }
}
