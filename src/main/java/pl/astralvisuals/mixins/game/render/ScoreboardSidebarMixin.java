package pl.astralvisuals.mixins.game.render;

import net.minecraft.class_329;
import net.minecraft.class_332;
import net.minecraft.class_9779;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import pl.astralvisuals.features.impl.render.NoRender;

// Скрытие скорборда (боковое табло) в NoRender. class_329 = InGameHud.
@Mixin(class_329.class)
public class ScoreboardSidebarMixin {
   // method_55803 = renderScoreboardSidebar(DrawContext, RenderTickCounter) — точка входа HUD.
   @Inject(
      method = "method_55803(Lnet/minecraft/class_332;Lnet/minecraft/class_9779;)V",
      at = @At("HEAD"), cancellable = true, remap = false
   )
   private void astral$noScoreboard(class_332 context, class_9779 tickCounter, CallbackInfo ci) {
      NoRender noRender = NoRender.getInstance();
      if (noRender != null && noRender.isState() && noRender.modeSetting.isSelected(NoRender.SCOREBOARD)) {
         ci.cancel();
      }
   }
}
