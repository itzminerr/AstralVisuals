package pl.astralvisuals.display.screens.clickgui.components.implement.settings;

import java.awt.Color;
import net.minecraft.class_332;
import pl.astralvisuals.display.screens.clickgui.components.implement.other.CheckComponent;
import pl.astralvisuals.features.module.setting.implement.BooleanSetting;
import pl.astralvisuals.utils.display.font.Fonts;

public class CheckboxComponent extends AbstractSettingComponent {
   private final CheckComponent checkComponent = new CheckComponent();
   private final BooleanSetting setting;

   public CheckboxComponent(BooleanSetting setting) {
      super(setting);
      this.setting = setting;
   }

   @Override
   public void render(class_332 context, int mouseX, int mouseY, float delta) {
      this.height = 18.0F;
      Fonts.getSize(12, Fonts.Type.DEFAULT)
         .drawString(context.method_51448(), this.setting.getName(), this.x + 8.0F, this.y + 10.0F, new Color(210, 210, 220, 220).getRGB());
      this.checkComponent
         .position(this.x + this.width - 24.0F, this.y + 3.5F)
         .setRunnable(() -> this.setting.setValue(!this.setting.isValue()))
         .setState(this.setting.isValue())
         .render(context, mouseX, mouseY, delta);
   }

   @Override
   public boolean mouseClicked(double mouseX, double mouseY, int button) {
      this.checkComponent.mouseClicked(mouseX, mouseY, button);
      return super.mouseClicked(mouseX, mouseY, button);
   }
}
