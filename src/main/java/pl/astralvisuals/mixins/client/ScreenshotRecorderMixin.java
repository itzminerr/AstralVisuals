package pl.astralvisuals.mixins.client;

import java.io.File;
import java.util.function.Consumer;
import net.minecraft.class_1011;
import net.minecraft.class_2561;
import net.minecraft.class_318;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import pl.astralvisuals.Force;
import pl.astralvisuals.features.impl.movement.ExtraScreenshot;
import pl.astralvisuals.utils.client.chat.ChatMessage;

@Mixin(class_318.class)
public abstract class ScreenshotRecorderMixin {
   @Inject(method = "method_1661", at = @At("HEAD"), cancellable = true)
   private static void handleExtraScreenshot(class_1011 image, File file, Consumer<class_2561> messageReceiver, CallbackInfo ci) {
      Force force = Force.getInstance();
      if (force == null || !force.isInitialized()) {
         return;
      }

      ExtraScreenshot extraScreenshot = ExtraScreenshot.getInstance();
      if (extraScreenshot == null || !extraScreenshot.isState()) {
         return;
      }

      try {
         image.method_4325(file);
         if (extraScreenshot.copyToClipboard(file)) {
            messageReceiver.accept(ChatMessage.brandmessageText("copied screenshot"));
         } else {
            messageReceiver.accept(ChatMessage.brandmessageText("failed to copy screenshot"));
         }
      } catch (Exception exception) {
         messageReceiver.accept(class_2561.method_43469("screenshot.failure", exception.getMessage()));
      } finally {
         image.close();
      }
      ci.cancel();
   }
}
