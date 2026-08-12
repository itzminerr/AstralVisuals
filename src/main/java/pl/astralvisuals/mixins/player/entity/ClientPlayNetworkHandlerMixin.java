package pl.astralvisuals.mixins.player.entity;

import net.minecraft.class_2396;
import net.minecraft.class_2398;
import net.minecraft.class_2675;
import net.minecraft.class_634;
import net.minecraft.class_7439;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import pl.astralvisuals.events.chat.ChatEvent;
import pl.astralvisuals.features.impl.render.NoRender;
import pl.astralvisuals.utils.client.managers.event.EventManager;
import pl.astralvisuals.utils.display.interfaces.QuickImports;

@Mixin(class_634.class)
public class ClientPlayNetworkHandlerMixin implements QuickImports {
   @Inject(method = "sendChatMessage(Ljava/lang/String;)V", at = @At("HEAD"), cancellable = true)
   private void sendChatMessage(String string, CallbackInfo ci) {
      ChatEvent event = new ChatEvent(string);
      EventManager.callEvent(event);
      if (event.isCancelled()) {
         ci.cancel();
      }
   }

   @Inject(method = "onGameMessage", at = @At("HEAD"), cancellable = true)
   private void onGameMessage(class_7439 packet, CallbackInfo ci) {
      String message = packet.comp_763().getString();
      ChatEvent event = new ChatEvent(message);
      EventManager.callEvent(event);
      if (event.isCancelled()) {
         ci.cancel();
      }
   }

   @Inject(method = "method_11077", at = @At("HEAD"), cancellable = true, remap = false)
   private void astral$onParticle(class_2675 packet, CallbackInfo ci) {
      NoRender noRender = NoRender.getInstance();
      if (noRender != null && noRender.isState()) {
         class_2396<?> type = packet.method_11551().method_10295();
         boolean crit = (type == class_2398.field_11205 || type == class_2398.field_11208) && noRender.modeSetting.isSelected("Партиклы крита");
         boolean sweep = type == class_2398.field_11227 && noRender.modeSetting.isSelected("Партиклы разящего удара");
         if (crit || sweep) {
            ci.cancel();
         }
      }
   }
}
