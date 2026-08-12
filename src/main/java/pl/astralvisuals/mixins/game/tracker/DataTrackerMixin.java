package pl.astralvisuals.mixins.game.tracker;

import net.minecraft.class_2379;
import net.minecraft.class_2940;
import net.minecraft.class_2941;
import net.minecraft.class_2943;
import net.minecraft.class_2945;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(class_2945.class)
public abstract class DataTrackerMixin {
   @Inject(method = "set(Lnet/minecraft/entity/data/TrackedData;Ljava/lang/Object;)V", at = @At("HEAD"), cancellable = true)
   private <T> void onSet(class_2940<T> data, T value, CallbackInfo ci) {
      class_2941<T> serializer = data.comp_2328();
      if (serializer == class_2943.field_13319 && !(value instanceof Byte)) {
         ci.cancel();
      }

      if (serializer == class_2943.field_13316 && !(value instanceof class_2379)) {
         ci.cancel();
      }
   }
}
