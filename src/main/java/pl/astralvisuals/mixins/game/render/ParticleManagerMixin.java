package pl.astralvisuals.mixins.game.render;

import net.minecraft.class_2338;
import net.minecraft.class_2350;
import net.minecraft.class_2680;
import net.minecraft.class_702;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import pl.astralvisuals.features.impl.render.NoRender;

@Mixin(class_702.class)
public class ParticleManagerMixin {
   // addBlockBreakParticles(pos, state) — поф при полном разрушении блока
   @Inject(method = "method_3046", at = @At("HEAD"), cancellable = true, remap = false)
   private void astral$cancelBlockBreak(class_2338 pos, class_2680 state, CallbackInfo ci) {
      if (this.astral$hideBlockBreak()) {
         ci.cancel();
      }
   }

   // addBlockBreakingParticles(pos, side) — крошки при копании блока
   @Inject(method = "method_3054", at = @At("HEAD"), cancellable = true, remap = false)
   private void astral$cancelBlockBreaking(class_2338 pos, class_2350 side, CallbackInfo ci) {
      if (this.astral$hideBlockBreak()) {
         ci.cancel();
      }
   }

   private boolean astral$hideBlockBreak() {
      NoRender noRender = NoRender.getInstance();
      return noRender != null && noRender.isState() && noRender.modeSetting.isSelected("Партиклы ломания блоков");
   }
}
