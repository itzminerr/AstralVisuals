package pl.astralvisuals.mixins.player.entity;

import net.minecraft.class_10055;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import pl.astralvisuals.utils.client.interfaces.ICustomPlayerModelState;

@Mixin(class_10055.class)
public class PlayerEntityRenderStateCustomModelsMixin implements ICustomPlayerModelState {
   @Unique
   private boolean astral$hasCustomModel;
   @Unique
   private String astral$customModel = "";

   @Override
   public boolean astral$hasCustomModel() {
      return this.astral$hasCustomModel;
   }

   @Override
   public String astral$getCustomModel() {
      return this.astral$customModel;
   }

   @Override
   public void astral$setCustomModel(boolean enabled, String model) {
      this.astral$hasCustomModel = enabled;
      this.astral$customModel = model == null ? "" : model;
   }
}
