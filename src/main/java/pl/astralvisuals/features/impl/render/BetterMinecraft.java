package pl.astralvisuals.features.impl.render;

import pl.astralvisuals.features.module.Module;
import pl.astralvisuals.features.module.ModuleCategory;
import pl.astralvisuals.features.module.setting.implement.BooleanSetting;
import pl.astralvisuals.utils.client.Instance;

public class BetterMinecraft extends Module {
   private final BooleanSetting betterButton = new BooleanSetting("Кастомные клавиши", "Использовать кастомные клавиши").setValue(true);

   public static BetterMinecraft getInstance() {
      return Instance.get(BetterMinecraft.class);
   }

   public BetterMinecraft() {
      super("BetterMinecraft", "Better Minecraft", ModuleCategory.RENDER);
      this.setup(this.betterButton);
   }

   public BooleanSetting getBetterButton() {
      return this.betterButton;
   }
}
