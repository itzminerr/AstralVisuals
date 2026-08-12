package pl.astralvisuals.mixins.client;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_310;
import net.minecraft.class_1297;
import net.minecraft.class_1657;
import net.minecraft.class_437;
import net.minecraft.class_542;
import net.minecraft.class_634;
import net.minecraft.class_636;
import net.minecraft.class_746;
import net.minecraft.class_757;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import pl.astralvisuals.Force;
import pl.astralvisuals.events.container.SetScreenEvent;
import pl.astralvisuals.features.impl.render.NoRender;
import pl.astralvisuals.utils.client.logs.Logger;
import pl.astralvisuals.utils.client.managers.event.EventManager;
import pl.astralvisuals.utils.client.managers.file.exception.FileProcessingException;
import pl.astralvisuals.utils.client.window.WindowStyle;
import pl.astralvisuals.utils.display.font.Fonts;
import pl.astralvisuals.utils.display.interfaces.QuickImports;

@Environment(EnvType.CLIENT)
@Mixin(class_310.class)
public abstract class MinecraftClientMixin implements QuickImports {
   private static final String WINDOW_TITLE = "Astral Visuals - Latest";

   @Shadow
   @Nullable
   public class_636 field_1761;
   @Shadow
   @Nullable
   public class_746 field_1724;
   @Shadow
   @Final
   public class_757 field_1773;
   @Shadow
   @Nullable
   public class_437 field_1755;

   @Shadow
   @Nullable
   public abstract class_634 method_1562();

   @Inject(at = @At("TAIL"), method = "<init>")
   private void onInit(class_542 args, CallbackInfo ci) {
      Fonts.init();
      WindowStyle.setMinecraftIcon(class_310.method_1551().method_22683().method_4490());
   }

   @Inject(at = @At("HEAD"), method = "stop")
   private void stop(CallbackInfo ci) {
      Logger.info("Stopping for MinecraftClient");
      if (Force.getInstance().getDiscordManager() != null) {
         Force.getInstance().getDiscordManager().stopRPC();
      }

      if (Force.getInstance().isInitialized()) {
         try {
            Force.getInstance().getFileController().saveFiles();
         } catch (FileProcessingException var6) {
            Logger.error("Error occurred while saving files: " + var6.getMessage() + " " + var6.getCause());
         } finally {
            Force.getInstance().getFileController().stopAutoSave();
         }
      }
   }

   @Inject(method = "setScreen", at = @At("HEAD"), cancellable = true)
   public void setScreenHook(class_437 screen, CallbackInfo ci) {
      SetScreenEvent event = new SetScreenEvent(screen);
      EventManager.callEvent(event);
      Force.getInstance().getDraggableRepository().draggable().forEach(drag -> drag.setScreen(event));
      class_437 eventScreen = event.getScreen();
      if (screen != eventScreen) {
         mc.method_1507(eventScreen);
         ci.cancel();
      }
   }

   @Inject(method = "onResolutionChanged", at = @At("TAIL"))
   private void applyDarkMode(CallbackInfo ci) {
      String os = System.getProperty("os.name").toLowerCase();
      if (!os.contains("linux")) {
         class_310 client = class_310.method_1551();
         WindowStyle.setDarkMode(client.method_22683().method_4490());
      }
   }

   @Inject(method = "method_24287", at = @At("HEAD"), cancellable = true, remap = false)
   private void getWindowTitle(CallbackInfoReturnable<String> cir) {
      cir.setReturnValue(WINDOW_TITLE);
   }

   @Inject(method = "method_27022(Lnet/minecraft/class_1297;)Z", at = @At("HEAD"), cancellable = true, remap = false)
   private void astral$hidePlayerGlow(class_1297 entity, CallbackInfoReturnable<Boolean> cir) {
      NoRender noRender = NoRender.getInstance();
      if (entity instanceof class_1657 && noRender != null && noRender.isState() && noRender.modeSetting.isSelected(NoRender.PLAYER_GLOW)) {
         cir.setReturnValue(false);
      }
   }
}
