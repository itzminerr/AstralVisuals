package pl.astralvisuals.display.screens.clickgui.components.implement.settings;

import net.minecraft.class_332;
import pl.astralvisuals.display.screens.clickgui.components.implement.other.ButtonComponent;
import pl.astralvisuals.features.module.setting.implement.ButtonSetting;
import pl.astralvisuals.utils.display.font.Fonts;

public class SButtonComponent extends AbstractSettingComponent {
   private final ButtonComponent buttonComponent = new ButtonComponent();
   private final ButtonSetting setting;

   public SButtonComponent(ButtonSetting setting) {
      super(setting);
      this.setting = setting;
   }

   @Override
   public void render(class_332 context, int mouseX, int mouseY, float delta) {
      this.height = 20.0F;
      Fonts.getSize(14, Fonts.Type.BOLD).drawString(context.method_51448(), this.setting.getName(), this.x + 9.0F, this.y + 6.0F, -2828575);
      ((ButtonComponent)this.buttonComponent
            .setText("Click on me")
            .setRunnable(this.setting.getRunnable())
            .position(this.x + this.width - 9.0F - this.buttonComponent.width, this.y + 5.0F))
         .render(context, mouseX, mouseY, delta);
   }

   @Override
   public boolean mouseClicked(double mouseX, double mouseY, int button) {
      this.buttonComponent.mouseClicked(mouseX, mouseY, button);
      return super.mouseClicked(mouseX, mouseY, button);
   }
}
