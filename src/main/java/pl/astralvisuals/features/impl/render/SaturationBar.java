package pl.astralvisuals.features.impl.render;

import pl.astralvisuals.features.module.Module;
import pl.astralvisuals.features.module.ModuleCategory;
import pl.astralvisuals.utils.client.Instance;

public final class SaturationBar extends Module {
   public SaturationBar() {
      super("SaturationBar", "Saturation Bar", ModuleCategory.RENDER);
   }

   public static SaturationBar getInstance() {
      return Instance.get(SaturationBar.class);
   }
}
