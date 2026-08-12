package pl.astralvisuals.mixins.player.item;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.class_1268;
import net.minecraft.class_1306;
import net.minecraft.class_1309;
import net.minecraft.class_1799;
import net.minecraft.class_4587;
import net.minecraft.class_4597;
import net.minecraft.class_742;
import net.minecraft.class_746;
import net.minecraft.class_759;
import net.minecraft.class_811;
import net.minecraft.class_4597.class_4598;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.At.Shift;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import pl.astralvisuals.events.item.HandAnimationEvent;
import pl.astralvisuals.events.item.HandOffsetEvent;
import pl.astralvisuals.events.render.ItemRendererEvent;
import pl.astralvisuals.features.impl.render.HandShader;
import pl.astralvisuals.features.impl.render.ViewModel;
import pl.astralvisuals.features.impl.render.handshader.HandShaderRenderer;
import pl.astralvisuals.utils.client.managers.event.EventManager;

@Mixin(class_759.class)
public abstract class HeldItemRendererMixin {
   @Inject(
      method = "renderItem(FLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider$Immediate;Lnet/minecraft/client/network/ClientPlayerEntity;I)V",
      at = @At("HEAD")
   )
   private void captureBeforeHands(float tickDelta, class_4587 matrices, class_4598 vertexConsumers, class_746 player, int light, CallbackInfo ci) {
      HandShader shader = HandShader.getInstance();
      if (shader != null) {
         HandShaderRenderer.INSTANCE.captureBeforeHands(shader);
      }
   }

   @Inject(
      method = "renderItem(FLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider$Immediate;Lnet/minecraft/client/network/ClientPlayerEntity;I)V",
      at = @At("TAIL")
   )
   private void captureAfterHands(float tickDelta, class_4587 matrices, class_4598 vertexConsumers, class_746 player, int light, CallbackInfo ci) {
      HandShader shader = HandShader.getInstance();
      if (shader != null) {
         HandShaderRenderer.INSTANCE.captureAfterHands(shader);
      }
   }

   @Inject(method = "renderFirstPersonItem", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/util/math/MatrixStack;push()V", shift = Shift.AFTER))
   private void renderFirstPersonItemHook(
      class_742 player,
      float tickDelta,
      float pitch,
      class_1268 hand,
      float swingProgress,
      class_1799 stack,
      float equipProgress,
      class_4587 matrices,
      class_4597 vertexConsumers,
      int light,
      CallbackInfo ci
   ) {
      HandOffsetEvent event = new HandOffsetEvent(matrices, stack, hand);
      EventManager.callEvent(event);
   }

   @WrapOperation(
      method = "renderItem(FLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider$Immediate;Lnet/minecraft/client/network/ClientPlayerEntity;I)V",
      at = @At(
         value = "INVOKE",
         target = "Lnet/minecraft/client/render/item/HeldItemRenderer;renderFirstPersonItem(Lnet/minecraft/client/network/AbstractClientPlayerEntity;FFLnet/minecraft/util/Hand;FLnet/minecraft/item/ItemStack;FLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;I)V"
      )
   )
   private void itemRenderHook(
      class_759 instance,
      class_742 player,
      float tickDelta,
      float pitch,
      class_1268 hand,
      float swingProgress,
      class_1799 item,
      float equipProgress,
      class_4587 matrices,
      class_4597 vertexConsumers,
      int light,
      Operation<Void> original
   ) {
      ItemRendererEvent event = new ItemRendererEvent(player, item, hand);
      EventManager.callEvent(event);
      original.call(
         new Object[]{
            instance, event.getPlayer(), tickDelta, pitch, event.getHand(), swingProgress, event.getStack(), equipProgress, matrices, vertexConsumers, light
         }
      );
   }

   @WrapOperation(
      method = "renderFirstPersonItem",
      at = @At(
         value = "INVOKE",
         target = "Lnet/minecraft/client/render/item/HeldItemRenderer;swingArm(FFLnet/minecraft/client/util/math/MatrixStack;ILnet/minecraft/util/Arm;)V",
         ordinal = 2
      )
   )
   private void handAnimationHook(
      class_759 instance,
      float swingProgress,
      float equipProgress,
      class_4587 matrices,
      int armX,
      class_1306 arm,
      Operation<Void> original,
      @Local(ordinal = 0, argsOnly = true) class_742 player,
      @Local(ordinal = 0, argsOnly = true) class_1268 hand
   ) {
      HandAnimationEvent event = new HandAnimationEvent(matrices, hand, swingProgress);
      EventManager.callEvent(event);
      if (!event.isCancelled()) {
         original.call(new Object[]{instance, swingProgress, equipProgress, matrices, armX, arm});
      }
   }

   // Оборачиваем РОВНО вызов отрисовки модели предмета внутри renderFirstPersonItem.
   // Здесь матрица уже спозиционирована ванилллой на месте предмета, поэтому масштаб View Model
   // применяется только к предмету (без улёта).
   @WrapOperation(
      method = "method_3228",
      at = @At(
         value = "INVOKE",
         target = "Lnet/minecraft/class_759;method_3233(Lnet/minecraft/class_1309;Lnet/minecraft/class_1799;Lnet/minecraft/class_811;ZLnet/minecraft/class_4587;Lnet/minecraft/class_4597;I)V",
         remap = false
      ),
      remap = false
   )
   private void astral$wrapHeldItemModel(
      class_759 instance,
      class_1309 entity,
      class_1799 stack,
      class_811 mode,
      boolean leftHanded,
      class_4587 matrices,
      class_4597 vertexConsumers,
      int light,
      Operation<Void> original,
      @Local(ordinal = 0, argsOnly = true) class_1268 hand
   ) {
      ViewModel viewModel = ViewModel.getInstance();
      if (viewModel != null && viewModel.isState()) {
         viewModel.applyItemScale(matrices, hand);
      }

      original.call(instance, entity, stack, mode, leftHanded, matrices, vertexConsumers, light);
   }
}
