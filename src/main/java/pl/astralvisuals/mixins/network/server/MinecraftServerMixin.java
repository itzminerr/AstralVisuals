package pl.astralvisuals.mixins.network.server;

import net.minecraft.server.MinecraftServer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import pl.astralvisuals.Force;
import pl.astralvisuals.utils.client.logs.Logger;
import pl.astralvisuals.utils.client.managers.file.exception.FileProcessingException;

@Mixin(MinecraftServer.class)
public class MinecraftServerMixin {
   @Inject(method = "shutdown", at = @At("HEAD"))
   public void shutdown(CallbackInfo ci) {
      if (Force.getInstance().isInitialized()) {
         try {
            Force.getInstance().getFileController().saveFiles();
         } catch (FileProcessingException var3) {
            Logger.error("Error occurred while saving files: " + var3.getMessage());
         }
      }
   }
}
