package pl.astralvisuals.mixins.client.screen.ingame;

import net.minecraft.class_1735;
import net.minecraft.class_310;
import net.minecraft.class_332;
import net.minecraft.class_437;
import net.minecraft.class_465;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.glfw.GLFW;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import pl.astralvisuals.events.container.HandledScreenEvent;
import pl.astralvisuals.features.impl.movement.ItemScroller;
import pl.astralvisuals.utils.client.interfaces.IHandledScreen;
import pl.astralvisuals.utils.client.managers.event.EventManager;

@Mixin(class_465.class)
public abstract class HandledScreenMixin implements IHandledScreen {
   @Shadow
   public int field_2792;
   @Shadow
   public int field_2779;
   @Shadow
   @Nullable
   protected class_1735 field_2787;

   // getSlotAt(mouseX, mouseY)
   @Shadow
   protected abstract class_1735 method_64240(double mouseX, double mouseY);

   @Override
   public class_1735 astral$focusedSlot() {
      return this.field_2787;
   }

   @Inject(method = "render", at = @At("RETURN"))
   public void render(class_332 context, int mouseX, int mouseY, float delta, CallbackInfo ci) {
      EventManager.callEvent(new HandledScreenEvent(context, this.field_2787, this.field_2792, this.field_2779));

      // ItemScroller: пока зажаты Shift+ЛКМ, каждый кадр переносим слот под курсором.
      // Опрос в render (а не только mouseDragged) делает драг устойчивым: он не прерывается,
      // когда курсор выходит за пределы инвентаря и возвращается обратно.
      ItemScroller scroller = ItemScroller.getInstance();
      if (scroller != null && scroller.isState() && class_437.method_25442() && this.astral$isLeftMouseDown()) {
         scroller.onSlotDragged(this.method_64240(mouseX, mouseY));
      }
   }

   @Unique
   private boolean astral$isLeftMouseDown() {
      long window = class_310.method_1551().method_22683().method_4490();
      return GLFW.glfwGetMouseButton(window, GLFW.GLFW_MOUSE_BUTTON_LEFT) == GLFW.GLFW_PRESS;
   }

   // ItemScroller: ЛКМ нажата с Shift -> начинаем новый драг (mouseClicked = method_25402)
   @Inject(method = "method_25402", at = @At("HEAD"), remap = false)
   private void astral$itemScrollerClick(double mouseX, double mouseY, int button, CallbackInfoReturnable<Boolean> cir) {
      ItemScroller scroller = ItemScroller.getInstance();
      if (scroller != null && scroller.isState() && button == 0 && class_437.method_25442()) {
         scroller.onDragStart(this.method_64240(mouseX, mouseY));
      }
   }

   // ItemScroller: каждое событие движения мыши во время драга (mouseDragged = method_25403).
   // Даёт пер-движение детализацию (не пропускает слоты при быстром драге), в дополнение к опросу в render.
   @Inject(method = "method_25403", at = @At("HEAD"), remap = false)
   private void astral$itemScrollerDrag(
      double mouseX, double mouseY, int button, double deltaX, double deltaY, CallbackInfoReturnable<Boolean> cir
   ) {
      ItemScroller scroller = ItemScroller.getInstance();
      if (scroller != null && scroller.isState() && button == 0 && class_437.method_25442()) {
         scroller.onSlotDragged(this.method_64240(mouseX, mouseY));
      }
   }
}
