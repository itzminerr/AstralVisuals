package pl.astralvisuals.mixins.player.item;

import net.minecraft.class_1738;
import net.minecraft.class_1741;
import net.minecraft.class_8051;
import net.minecraft.class_1792.class_1793;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import pl.astralvisuals.utils.display.interfaces.IArmorItem;

@Mixin(class_1738.class)
public abstract class ArmorItemMixin implements IArmorItem {
   @Unique
   private class_1741 armorMaterial;
   @Unique
   private class_8051 type;

   @Inject(method = "<init>", at = @At("RETURN"))
   public void hookCatchArgs(class_1741 material, class_8051 type, class_1793 settings, CallbackInfo ci) {
      this.armorMaterial = material;
      this.type = type;
   }

   @Override
   public class_1741 zov_pidarok$getMaterial() {
      return this.armorMaterial;
   }

   @Override
   public class_8051 zov_pidarok$getType() {
      return this.type;
   }
}
