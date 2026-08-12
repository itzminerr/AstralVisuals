package pl.astralvisuals.mixins.client.screen.chat;

import java.util.List;
import net.minecraft.class_2561;
import net.minecraft.class_332;
import net.minecraft.class_408;
import net.minecraft.class_437;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import pl.astralvisuals.Force;
import pl.astralvisuals.features.impl.render.Interface;
import pl.astralvisuals.utils.client.managers.api.draggable.AbstractDraggable;
import pl.astralvisuals.utils.display.interfaces.QuickImports;

@Mixin(class_408.class)
public class ChatScreenMixin extends class_437 implements QuickImports {
   @Unique
   List<AbstractDraggable> draggables = Force.getInstance().getDraggableRepository().draggable();

   protected ChatScreenMixin() {
      super(class_2561.method_43473());
   }

   @Inject(method = "render", at = @At("HEAD"))
   private void onRender(class_332 context, int mouseX, int mouseY, float delta, CallbackInfo ci) {
      Interface hud = Interface.getInstance();
      this.draggables
         .stream()
         .filter(draggable -> draggable.canDraw(hud, draggable) && draggable.isDragging())
         .reduce((first, second) -> second)
         .ifPresent(active -> this.draggables.forEach(draggable -> {
            if (active == draggable) {
               draggable.render(context, mouseX, mouseY, delta);
            }
         }));
   }

   @Inject(method = "mouseClicked", at = @At("TAIL"))
   private void onMouseClicked(double mouseX, double mouseY, int button, CallbackInfoReturnable<Boolean> cir) {
      this.draggables.forEach(draggable -> draggable.mouseClicked(mouseX, mouseY, button));
   }

   public boolean method_25406(double mouseX, double mouseY, int button) {
      this.draggables.forEach(draggable -> draggable.mouseReleased(mouseX, mouseY, button));
      return super.method_25406(mouseX, mouseY, button);
   }
}
