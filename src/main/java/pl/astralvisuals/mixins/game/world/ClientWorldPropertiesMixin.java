package pl.astralvisuals.mixins.game.world;

import net.minecraft.class_638.class_5271;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import pl.astralvisuals.features.impl.render.WorldTweaks;
import pl.astralvisuals.utils.display.interfaces.QuickImports;

@Mixin(class_5271.class)
public class ClientWorldPropertiesMixin implements QuickImports {
   @Shadow
   private long field_24439;

   @Inject(method = "setTimeOfDay", at = @At("HEAD"), cancellable = true)
   public void setTimeOfDayHook(long timeOfDay, CallbackInfo ci) {
      WorldTweaks tweaks = WorldTweaks.getInstance();
      if (tweaks.state && tweaks.modeSetting.isSelected("Время")) {
         this.field_24439 = tweaks.timeSetting.getInt() * 1000L;
         ci.cancel();
      }
   }
}
