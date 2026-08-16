package pl.astralvisuals.features.impl.render;

import net.minecraft.class_1297;
import net.minecraft.class_4587;
import pl.astralvisuals.features.module.Module;
import pl.astralvisuals.features.module.ModuleCategory;
import pl.astralvisuals.utils.client.Instance;

public final class BabyMod extends Module {
   private static final float MODEL_SCALE = 0.5F;

   public BabyMod() {
      super("BabyMod", "BabyMod", ModuleCategory.RENDER);
   }

   public static BabyMod getInstance() {
      return Instance.get(BabyMod.class);
   }

   public boolean shouldApply(class_1297 entity) {
      return this.isState() && mc.field_1724 != null && entity == mc.field_1724;
   }

   public void applyScale(class_4587 matrices) {
      matrices.method_22905(MODEL_SCALE, MODEL_SCALE, MODEL_SCALE);
   }

   public boolean shouldAdjustCamera(class_1297 focusedEntity, boolean thirdPerson) {
      return thirdPerson && this.shouldApply(focusedEntity);
   }
}
