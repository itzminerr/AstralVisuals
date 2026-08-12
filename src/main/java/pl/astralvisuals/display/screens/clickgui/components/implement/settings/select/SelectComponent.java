package pl.astralvisuals.display.screens.clickgui.components.implement.settings.select;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.class_332;
import net.minecraft.class_4587;
import pl.astralvisuals.common.animation.Animation;
import pl.astralvisuals.common.animation.Direction;
import pl.astralvisuals.common.animation.implement.Decelerate;
import pl.astralvisuals.display.screens.clickgui.components.implement.settings.AbstractSettingComponent;
import pl.astralvisuals.features.module.setting.implement.SelectSetting;
import pl.astralvisuals.utils.display.font.FontRenderer;
import pl.astralvisuals.utils.display.font.Fonts;
import pl.astralvisuals.utils.display.shape.ShapeProperties;
import pl.astralvisuals.utils.math.calc.Calculate;

public class SelectComponent extends AbstractSettingComponent {
   private final List<SelectedButton> selectedButtons = new ArrayList<>();
   private final SelectSetting setting;
   private boolean open;
   private float dropdownListX;
   private float dropDownListY;
   private float dropDownListWidth;
   private float dropDownListHeight;
   private final Animation alphaAnimation = new Decelerate().setMs(300).setValue(1.0);
   private final Animation heightAnimation = new Decelerate().setMs(200).setValue(0.0);

   public SelectComponent(SelectSetting setting) {
      super(setting);
      this.setting = setting;
      this.alphaAnimation.setDirection(Direction.BACKWARDS);
      this.heightAnimation.setDirection(Direction.BACKWARDS);

      for (String s : setting.getList()) {
         this.selectedButtons.add(new SelectedButton(setting, s));
      }
   }

   @Override
   public void render(class_332 context, int mouseX, int mouseY, float delta) {
      class_4587 matrices = context.method_51448();
      float baseHeight = 20.0F;
      List<String> fullSettingsList = this.setting.getList();
      this.dropdownListX = this.x + this.width - 75.0F;
      this.dropDownListY = this.y + 23.0F;
      this.dropDownListWidth = 66.0F;
      this.dropDownListHeight = fullSettingsList.size() * 12;
      if (this.open) {
         this.alphaAnimation.setDirection(Direction.FORWARDS);
         this.heightAnimation.setDirection(Direction.FORWARDS);
         this.heightAnimation.setValue(this.dropDownListHeight);
      } else {
         this.alphaAnimation.setDirection(Direction.BACKWARDS);
         this.heightAnimation.setDirection(Direction.BACKWARDS);
      }

      this.height = (int)(baseHeight + this.heightAnimation.getOutput().floatValue() + (this.open ? 5 : 0));
      this.renderSelected(matrices);
      if (!this.alphaAnimation.isFinished(Direction.BACKWARDS)) {
         this.renderSelectList(context, mouseX, mouseY, delta);
      }

      Fonts.getSize(21, Fonts.Type.GUIICONS).drawString(matrices, "J", this.x + 6.0F, this.y + 11.0F, new Color(128, 128, 128, 64).getRGB());
      Fonts.getSize(12, Fonts.Type.DEFAULT).drawString(matrices, this.setting.getName(), this.x + 8.0F, this.y + 13.0F, -2828575);
   }

   @Override
   public boolean mouseClicked(double mouseX, double mouseY, int button) {
      if (button == 0) {
         if (Calculate.isHovered(mouseX, mouseY, this.x + this.width - 75.0F, this.y + 4.0F, 66.0, 17.0)) {
            this.open = !this.open;
            return true;
         }

         if (this.open && !this.isHoveredList(mouseX, mouseY)) {
            this.open = false;
            return true;
         }

         if (this.open) {
            for (SelectedButton selectedButton : this.selectedButtons) {
               if (selectedButton.mouseClicked(mouseX, mouseY, button)) {
                  return true;
               }
            }
         }
      }

      return super.mouseClicked(mouseX, mouseY, button);
   }

   @Override
   public boolean isHover(double mouseX, double mouseY) {
      return this.open && this.isHoveredList(mouseX, mouseY);
   }

   private void renderSelected(class_4587 matrices) {
      FontRenderer font = Fonts.getSize(12);
      int x1 = (int)(this.x + this.width - 72.0F);
      float offset = 64.0F;
      rectangle.render(
         ShapeProperties.create(matrices, this.x + this.width - 75.0F, this.y + 7.0F, 66.0, 14.0)
            .round(3.0F)
            .thickness(2.0F)
            .outlineColor(new Color(35, 52, 55, 155).getRGB())
            .color(new Color(15, 15, 15, 0).getRGB(), new Color(15, 15, 15, 0).getRGB(), new Color(15, 15, 15, 0).getRGB(), new Color(15, 15, 15, 0).getRGB())
            .build()
      );
      String selectedName = String.join(", ", this.setting.getSelected());
      Fonts.getSize(12, Fonts.Type.BOLD)
         .drawString(matrices, selectedName, this.x + this.width - 75.0F + 3.0F, this.y + 13.0F, new Color(225, 225, 225, 225).getRGB());
   }

   private void renderSelectList(class_332 context, int mouseX, int mouseY, float delta) {
      float opacity = this.alphaAnimation.getOutput().floatValue();
      int alpha = (int)(opacity * 0.0F);
      float animatedHeight = this.heightAnimation.getOutput().floatValue();
      rectangle.render(
         ShapeProperties.create(context.method_51448(), this.dropdownListX, this.dropDownListY, this.dropDownListWidth, animatedHeight)
            .round(3.0F)
            .thickness(2.0F)
            .outlineColor(new Color(55, 52, 55, 155).getRGB())
            .color(
               new Color(15, 15, 15, alpha).getRGB(),
               new Color(15, 15, 15, alpha).getRGB(),
               new Color(15, 15, 15, alpha).getRGB(),
               new Color(15, 15, 15, alpha).getRGB()
            )
            .build()
      );
      float offset = this.dropDownListY;

      for (SelectedButton button : this.selectedButtons) {
         button.x = this.dropdownListX;
         button.y = offset;
         button.width = this.dropDownListWidth;
         button.height = 12.0F;
         button.setAlpha(opacity);
         if (offset - this.dropDownListY < animatedHeight) {
            button.render(context, mouseX, mouseY, delta);
         }

         offset += 12.0F;
      }
   }

   private boolean isHoveredList(double mouseX, double mouseY) {
      return Calculate.isHovered(mouseX, mouseY, this.dropdownListX, this.dropDownListY - 16.0F, this.dropDownListWidth, this.dropDownListHeight + 16.0F);
   }
}
