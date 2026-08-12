package pl.astralvisuals.mixins.game.render;

import net.minecraft.class_2561;
import net.minecraft.class_332;
import net.minecraft.class_339;
import net.minecraft.class_357;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import pl.astralvisuals.common.animation.Animation;
import pl.astralvisuals.common.animation.Direction;
import pl.astralvisuals.common.animation.implement.Decelerate;
import pl.astralvisuals.features.impl.render.BetterMinecraft;
import pl.astralvisuals.utils.display.widget.SliderRender;

@Mixin(class_357.class)
public abstract class SliderWidgetMixin extends class_339 {
   @Shadow
   protected double field_22753;
   @Unique
   private final Animation astral$hover = new Decelerate().setMs(220).setValue(1.0);

   public SliderWidgetMixin(int x, int y, int width, int height, class_2561 message) {
      super(x, y, width, height, message);
   }

   @Inject(method = "renderWidget", at = @At("HEAD"), cancellable = true)
   private void onRenderWidget(class_332 context, int mouseX, int mouseY, float delta, CallbackInfo ci) {
      if (BetterMinecraft.getInstance().isState() && BetterMinecraft.getInstance().getBetterButton().isValue()) {
         ci.cancel();
         boolean hovered = mouseX >= this.method_46426()
            && mouseX < this.method_46426() + this.method_25368()
            && mouseY >= this.method_46427()
            && mouseY < this.method_46427() + this.method_25364();
         this.astral$hover.setDirection(hovered ? Direction.FORWARDS : Direction.BACKWARDS);
         SliderRender.renderSlider(
            context,
            this.method_46426(),
            this.method_46427(),
            this.method_25368(),
            this.method_25364(),
            this.field_22753,
            this.field_22763,
            this.method_25369() != null ? this.method_25369().getString() : "",
            this.astral$hover.getOutput().floatValue()
         );
      }
   }
}
