package pl.astralvisuals.display.screens.clickgui.components.implement.settings.multiselect;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.class_332;
import net.minecraft.class_4587;
import pl.astralvisuals.Force;
import pl.astralvisuals.common.animation.Animation;
import pl.astralvisuals.common.animation.Direction;
import pl.astralvisuals.common.animation.implement.Decelerate;
import pl.astralvisuals.display.screens.clickgui.components.implement.settings.AbstractSettingComponent;
import pl.astralvisuals.features.module.setting.implement.MultiSelectSetting;
import pl.astralvisuals.utils.display.font.FontRenderer;
import pl.astralvisuals.utils.display.font.Fonts;
import pl.astralvisuals.utils.display.scissor.ScissorAssist;
import pl.astralvisuals.utils.display.shape.ShapeProperties;
import pl.astralvisuals.utils.math.calc.Calculate;

public class MultiSelectComponent extends AbstractSettingComponent {
   private final List<MultiSelectedButton> multiSelectedButtons = new ArrayList<>();
   private final MultiSelectSetting setting;
   private boolean open;
   private float dropdownListX;
   private float dropDownListY;
   private float dropDownListWidth;
   private float dropDownListHeight;
   private final Animation alphaAnimation = new Decelerate().setMs(300).setValue(1.0);
   private final Animation heightAnimation = new Decelerate().setMs(200).setValue(0.0);

   public MultiSelectComponent(MultiSelectSetting setting) {
      super(setting);
      this.setting = setting;
      this.alphaAnimation.setDirection(Direction.BACKWARDS);
      this.heightAnimation.setDirection(Direction.BACKWARDS);

      for (String s : setting.getList()) {
         this.multiSelectedButtons.add(new MultiSelectedButton(setting, s));
      }
   }

   @Override
   public void render(class_332 context, int mouseX, int mouseY, float delta) {
      class_4587 matrices = context.method_51448();
      float baseHeight = 20.0F;
      List<String> fullSettingsList = this.setting.getList();
      this.dropdownListX = this.x + this.width - 75.0F;
      this.dropDownListY = this.y + 24.0F;
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

      Fonts.getSize(20, Fonts.Type.GUIICONS).drawString(matrices, "I", this.x + 6.0F, this.y + 12.0F, new Color(128, 128, 128, 64).getRGB());
      Fonts.getSize(12, Fonts.Type.DEFAULT).drawString(matrices, this.setting.getName(), this.x + 19.0F, this.y + 13.0F, -2828575);
   }

   @Override
   public boolean mouseClicked(double mouseX, double mouseY, int button) {
      if (button == 0) {
         if (Calculate.isHovered(mouseX, mouseY, this.x + this.width - 75.0F, this.y + 8.0F, 66.0, 14.0)) {
            this.open = !this.open;
         } else if (this.open && !this.isHoveredList(mouseX, mouseY)) {
            this.open = false;
         }

         if (this.open) {
            this.multiSelectedButtons.forEach(selectedButton -> selectedButton.mouseClicked(mouseX, mouseY, button));
         }
      }

      return super.mouseClicked(mouseX, mouseY, button);
   }

   @Override
   public boolean isHover(double mouseX, double mouseY) {
      return this.open && this.isHoveredList(mouseX, mouseY);
   }

   private void renderSelected(class_4587 matrix) {
      FontRenderer font = Fonts.getSize(12);
      int x1 = (int)(this.x + this.width - 72.0F);
      rectangle.render(
         ShapeProperties.create(matrix, this.x + this.width - 75.0F, this.y + 7.0F, 66.0, 14.0)
            .round(3.0F)
            .thickness(2.0F)
            .outlineColor(new Color(35, 52, 55, 155).getRGB())
            .color(new Color(15, 15, 15, 0).getRGB(), new Color(15, 15, 15, 0).getRGB(), new Color(15, 15, 15, 0).getRGB(), new Color(15, 15, 15, 0).getRGB())
            .build()
      );
      String selectedName = String.join(", ", this.setting.getSelected());
      float offset = 64.0F;
      ScissorAssist scissor = Force.getInstance().getScissorManager();
      scissor.push(matrix.method_23760().method_23761(), x1 - 2, this.y + 4.0F, 64.0F, 14.0F);
      font.drawStringWithScroll(matrix, selectedName, x1, this.y + 13.0F, offset, new Color(225, 225, 225, 225).getRGB());
      scissor.pop();
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

      for (MultiSelectedButton button : this.multiSelectedButtons) {
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
