package pl.astralvisuals.display.screens.clickgui.components.implement.settings;

import java.awt.Color;
import net.minecraft.class_332;
import net.minecraft.class_4587;
import net.minecraft.class_9848;
import pl.astralvisuals.features.module.setting.implement.BindSetting;
import pl.astralvisuals.utils.client.chat.StringHelper;
import pl.astralvisuals.utils.display.font.Fonts;
import pl.astralvisuals.utils.display.shape.ShapeProperties;
import pl.astralvisuals.utils.math.calc.Calculate;

public class IconBindComponent extends AbstractSettingComponent {
   private final BindSetting setting;
   private boolean binding;

   public IconBindComponent(BindSetting setting) {
      super(setting);
      this.setting = setting;
   }

   @Override
   public void render(class_332 context, int mouseX, int mouseY, float delta) {
      class_4587 matrix = context.method_51448();
      String bindName = StringHelper.getBindName(this.setting.getKey());
      String name = this.binding ? "(" + bindName + ") ..." : bindName;
      float stringWidth = Fonts.getSize(11, Fonts.Type.SEMI).getStringWidth(name) - 2.0F;
      this.height = 20.0F;
      rectangle.render(
         ShapeProperties.create(matrix, this.x + this.width - stringWidth - 17.0F, this.y + 5.5F, stringWidth + 10.0F, 12.0)
            .round(3.0F)
            .outlineColor(new Color(200, 200, 200, 255).getRGB())
            .color(
               new Color(61, 67, 71, 80).getRGB(), new Color(71, 77, 81, 80).getRGB(), new Color(81, 87, 91, 80).getRGB(), new Color(91, 97, 101, 80).getRGB()
            )
            .build()
      );
      int bindingColor = class_9848.method_61324(255, 135, 136, 148);
      Fonts.getSize(11, Fonts.Type.SEMI).drawString(matrix, name, this.x + this.width - 12.0F - stringWidth - 1.0F, this.y + 11.0F, bindingColor);
      Fonts.getSize(14, Fonts.Type.GUIICONS).drawString(context.method_51448(), "L", this.x + 6.0F, this.y + 11.0F, new Color(128, 128, 128, 64).getRGB());
      Fonts.getSize(12, Fonts.Type.DEFAULT).drawString(context.method_51448(), this.setting.getName(), this.x + 17.0F, this.y + 10.0F, -2828575);
   }

   @Override
   public boolean mouseClicked(double mouseX, double mouseY, int button) {
      if (button == 0) {
         if (Calculate.isHovered(mouseX, mouseY, this.x, this.y, this.width, this.height)) {
            this.binding = !this.binding;
         } else {
            this.binding = false;
         }
      }

      if (this.binding && button > 1) {
         this.setting.setKey(button);
         this.binding = false;
      }

      return super.mouseClicked(mouseX, mouseY, button);
   }

   @Override
   public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
      int key = keyCode == 261 ? -1 : keyCode;
      if (this.binding) {
         this.setting.setKey(key);
         this.binding = false;
      }

      return super.keyPressed(keyCode, scanCode, modifiers);
   }
}
