package pl.astralvisuals.mixins.network;

import net.minecraft.class_2535;
import net.minecraft.class_2596;
import net.minecraft.class_2824;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import pl.astralvisuals.features.impl.movement.CrystalOptimizer;

@Mixin(class_2535.class)
public class ClientConnectionMixin {
   @Inject(method = "method_10743(Lnet/minecraft/class_2596;)V", at = @At("HEAD"), remap = false)
   private void astral$onPacketSend(class_2596<?> packet, CallbackInfo ci) {
      if (packet instanceof class_2824 interactPacket) {
         CrystalOptimizer optimizer = CrystalOptimizer.getInstance();
         if (optimizer != null && optimizer.isState()) {
            optimizer.onAttackPacket(interactPacket);
         }
      }
   }
}
