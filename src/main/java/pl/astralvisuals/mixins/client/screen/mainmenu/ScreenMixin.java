package pl.astralvisuals.mixins.client.screen.mainmenu;

import net.minecraft.class_2558;
import net.minecraft.class_2583;
import net.minecraft.class_332;
import net.minecraft.class_433;
import net.minecraft.class_437;
import net.minecraft.class_442;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import pl.astralvisuals.display.screens.clickgui.MenuScreen;
import pl.astralvisuals.events.chat.ChatEvent;
import pl.astralvisuals.utils.client.managers.event.EventManager;
import pl.astralvisuals.utils.display.interfaces.QuickImports;

@Mixin(class_437.class)
public class ScreenMixin implements QuickImports {

   // Захватываем фон для стеклянных элементов перед рендером ЛЮБОГО экрана.
   // При открытом экране HUD не рисуется, поэтому InGameHudMixin тут не помогает —
   // делаем свежий захват кадра (мир/меню за экраном) прямо здесь.
   @Inject(method = "method_25394", at = @At("HEAD"), remap = false)
   private void astral$setupBlurForScreens(class_332 context, int mouseX, int mouseY, float delta, CallbackInfo ci) {
      blur.setup();
   }

   @Inject(
      at = @At(value = "INVOKE", target = "Lorg/slf4j/Logger;error(Ljava/lang/String;Ljava/lang/Object;)V", remap = false, ordinal = 1),
      method = "handleTextClick",
      cancellable = true
   )
   public void handleCustomClickEvent(class_2583 style, CallbackInfoReturnable<Boolean> cir) {
      class_2558 clickEvent = style.method_10970();
      if (clickEvent != null) {
         EventManager.callEvent(new ChatEvent(clickEvent.method_10844()));
         cir.setReturnValue(true);
         cir.cancel();
      }
   }

   @Inject(method = "renderBackground", at = @At("HEAD"), cancellable = true)
   private void disableBackgroundBlurAndDimming(class_332 context, int mouseX, int mouseY, float delta, CallbackInfo ci) {
      // Убираем затемнение/блюр на нашем меню, ванильном Game Menu (Esc) и главном меню.
      // Для главного меню renderBackground рисует градиент/текстуру поверх панорамы — отменяем.
      if ((Object)this instanceof MenuScreen || (Object)this instanceof class_433 || (Object)this instanceof class_442) {
         ci.cancel();
      }
   }

}
