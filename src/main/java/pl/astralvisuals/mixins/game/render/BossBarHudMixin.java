package pl.astralvisuals.mixins.game.render;

import net.minecraft.class_332;
import net.minecraft.class_337;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import pl.astralvisuals.features.impl.render.NoRender;

// Скрытие боссбара в NoRender. class_337 = BossBarHud.
@Mixin(class_337.class)
public class BossBarHudMixin {
   // method_1796 = render(DrawContext) — отрисовка всех боссбаров сверху экрана.
   @Inject(method = "method_1796(Lnet/minecraft/class_332;)V", at = @At("HEAD"), cancellable = true, remap = false)
   private void astral$noBossBar(class_332 context, CallbackInfo ci) {
      NoRender noRender = NoRender.getInstance();
      if (noRender != null && noRender.isState() && noRender.modeSetting.isSelected(NoRender.BOSSBAR)) {
         ci.cancel();
      }
   }
}
