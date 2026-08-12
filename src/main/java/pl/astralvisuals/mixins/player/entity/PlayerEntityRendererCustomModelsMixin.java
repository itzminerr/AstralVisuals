package pl.astralvisuals.mixins.player.entity;

import net.minecraft.class_10055;
import net.minecraft.class_1007;
import net.minecraft.class_4587;
import net.minecraft.class_4597;
import net.minecraft.class_591;
import net.minecraft.class_742;
import net.minecraft.class_8685;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import pl.astralvisuals.features.impl.render.CustomModels;
import pl.astralvisuals.features.impl.render.Cosmetic;
import pl.astralvisuals.features.impl.render.custommodels.CustomPlayerModelRenderer;
import pl.astralvisuals.utils.client.ReflectionUtils;
import pl.astralvisuals.utils.client.interfaces.ICustomPlayerModelState;

@Mixin(class_1007.class)
public class PlayerEntityRendererCustomModelsMixin {
   private static final ThreadLocal<class_10055> STATE = new ThreadLocal<>();
   private static final ThreadLocal<class_591> MODEL = new ThreadLocal<>();

   @Inject(method = "method_62604", at = @At("TAIL"), remap = false)
   private void astral$updateCustomModelState(class_742 player, class_10055 state, float tickProgress, CallbackInfo ci) {
      CustomModels customModels = CustomModels.getInstance();
      boolean enabled = customModels != null && customModels.shouldApplyTo(player);
      ((ICustomPlayerModelState)state).astral$setCustomModel(enabled, enabled ? customModels.getModelName() : "");

      Cosmetic cosmetic = Cosmetic.getInstance();
      if (cosmetic != null && state.field_53520 != null) {
         var cape = cosmetic.getCapeTexture(player);
         if (cape != null) {
            class_8685 skin = state.field_53520;
            state.field_53520 = new class_8685(
               skin.comp_1626(), skin.comp_1911(), cape, skin.comp_1628(), skin.comp_1629(), skin.comp_1630()
            );
            state.field_53532 = true;
         }
      }
      STATE.set(state);
      MODEL.set(ReflectionUtils.getPlayerModel(this));
   }

   static class_10055 astral$getState() {
      return STATE.get();
   }

   static class_591 astral$getModel() {
      return MODEL.get();
   }
}
