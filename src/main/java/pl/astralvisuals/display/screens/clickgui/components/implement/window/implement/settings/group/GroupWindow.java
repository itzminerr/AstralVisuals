package pl.astralvisuals.display.screens.clickgui.components.implement.window.implement.settings.group;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;
import net.minecraft.class_332;
import net.minecraft.class_3532;
import net.minecraft.class_4587;
import pl.astralvisuals.Force;
import pl.astralvisuals.display.screens.clickgui.components.AbstractComponent;
import pl.astralvisuals.display.screens.clickgui.components.implement.settings.AbstractSettingComponent;
import pl.astralvisuals.display.screens.clickgui.components.implement.window.AbstractWindow;
import pl.astralvisuals.features.module.setting.SettingComponentAdder;
import pl.astralvisuals.features.module.setting.implement.GroupSetting;
import pl.astralvisuals.utils.display.color.ColorAssist;
import pl.astralvisuals.utils.display.font.Fonts;
import pl.astralvisuals.utils.display.scissor.ScissorAssist;
import pl.astralvisuals.utils.display.style.GlassStyle;
import pl.astralvisuals.utils.math.calc.Calculate;

public class GroupWindow extends AbstractWindow {
   private final List<AbstractSettingComponent> components = new ArrayList<>();
   private final GroupSetting setting;

   public GroupWindow(GroupSetting setting) {
      this.setting = setting;
      new SettingComponentAdder().addSettingComponent(setting.getSubSettings(), this.components);
   }

   @Override
   public void drawWindow(class_332 context, int mouseX, int mouseY, float delta) {
      class_4587 matrix = context.method_51448();
      ScissorAssist scissorManager = Force.getInstance().getScissorManager();
      this.height = class_3532.method_15340(this.getComponentHeight(), 0, 200);
      GlassStyle.strongBackdrop(matrix, this.x, this.y, this.width + 30.0F, this.height, 9.0F);
      Fonts.getSize(15, Fonts.Type.SEMI)
         .drawGradientString(
            context.method_51448(),
            this.setting.getName() + " Settings",
            this.x + 10.0F,
            this.y + 10.0F,
            ColorAssist.getText(),
            new Color(165, 165, 165, 255).getRGB()
         );
      boolean isLimitedHeight = class_3532.method_15363(this.height, 0.0F, 200.0F) == 200.0F;
      if (isLimitedHeight) {
         scissorManager.push(matrix.method_23760().method_23761(), this.x, this.y + 23.0F, this.width, this.height - 28.0F);
      }

      float offset = 0.0F;
      int totalHeight = 0;

      for (int i = this.components.size() - 1; i >= 0; i--) {
         AbstractSettingComponent component = this.components.get(i);
         Supplier<Boolean> visible = component.getSetting().getVisible();
         if (visible == null || visible.get()) {
            component.x = this.x;
            component.y = (float)(this.y + 19.0F + offset + (this.getComponentHeight() - 25 - component.height) + this.smoothedScroll);
            component.width = this.width + 30.0F;
            component.render(context, mouseX, mouseY, delta);
            offset -= component.height;
            totalHeight += (int)component.height;
         }
      }

      if (isLimitedHeight) {
         scissorManager.pop();
      }

      int maxScroll = (int)Math.max(0.0F, totalHeight - (this.height - 23.0F));
      this.scroll = class_3532.method_15350(this.scroll, -maxScroll, 0.0);
      this.smoothedScroll = class_3532.method_16436(0.1F, this.smoothedScroll, this.scroll);
   }

   @Override
   public boolean mouseClicked(double mouseX, double mouseY, int button) {
      this.draggable(Calculate.isHovered(mouseX, mouseY, this.x, this.y, this.width, 19.0) && button == 0);
      boolean isAnyComponentHovered = this.components.stream().anyMatch(abstractComponent -> abstractComponent.isHover(mouseX, mouseY));
      if (isAnyComponentHovered) {
         this.components.forEach(abstractComponent -> {
            if (abstractComponent.isHover(mouseX, mouseY)) {
               abstractComponent.mouseClicked(mouseX, mouseY, button);
            }
         });
         return super.mouseClicked(mouseX, mouseY, button);
      } else {
         this.components.forEach(abstractComponent -> abstractComponent.mouseClicked(mouseX, mouseY, button));
         return super.mouseClicked(mouseX, mouseY, button);
      }
   }

   @Override
   public boolean isHover(double mouseX, double mouseY) {
      this.components.forEach(abstractComponentx -> abstractComponentx.isHover(mouseX, mouseY));

      for (AbstractComponent abstractComponent : this.components) {
         if (abstractComponent.isHover(mouseX, mouseY)) {
            return true;
         }
      }

      return super.isHover(mouseX, mouseY);
   }

   @Override
   public boolean mouseReleased(double mouseX, double mouseY, int button) {
      this.components.forEach(abstractComponent -> abstractComponent.mouseReleased(mouseX, mouseY, button));
      return super.mouseReleased(mouseX, mouseY, button);
   }

   @Override
   public boolean mouseScrolled(double mouseX, double mouseY, double amount) {
      boolean scrolled = class_3532.method_15363(this.height, 0.0F, 200.0F) == 200.0F
         && Calculate.isHovered(mouseX, mouseY, this.x, this.y, this.width, this.height);
      if (scrolled) {
         this.scroll += amount * 20.0;
      }

      this.components.forEach(abstractComponent -> abstractComponent.mouseScrolled(mouseX, mouseY, amount));
      return scrolled;
   }

   @Override
   public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
      this.components.forEach(abstractComponent -> abstractComponent.keyPressed(keyCode, scanCode, modifiers));
      return super.keyPressed(keyCode, scanCode, modifiers);
   }

   @Override
   public boolean charTyped(char chr, int modifiers) {
      this.components.forEach(abstractComponent -> abstractComponent.charTyped(chr, modifiers));
      return super.charTyped(chr, modifiers);
   }

   public int getComponentHeight() {
      float offsetY = 0.0F;

      for (AbstractSettingComponent component : this.components) {
         Supplier<Boolean> visible = component.getSetting().getVisible();
         if (visible == null || visible.get()) {
            offsetY += component.height;
         }
      }

      return (int)(offsetY + 25.0F);
   }

   public List<AbstractSettingComponent> getComponents() {
      return this.components;
   }

   public GroupSetting getSetting() {
      return this.setting;
   }
}
