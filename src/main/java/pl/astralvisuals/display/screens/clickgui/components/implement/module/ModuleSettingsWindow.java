package pl.astralvisuals.display.screens.clickgui.components.implement.module;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;
import net.minecraft.class_332;
import net.minecraft.class_3532;
import net.minecraft.class_4587;
import org.joml.Vector4f;
import pl.astralvisuals.Force;
import pl.astralvisuals.display.screens.clickgui.components.AbstractComponent;
import pl.astralvisuals.display.screens.clickgui.components.implement.settings.AbstractSettingComponent;
import pl.astralvisuals.display.screens.clickgui.components.implement.window.AbstractWindow;
import pl.astralvisuals.features.module.Module;
import pl.astralvisuals.features.module.setting.SettingComponentAdder;
import pl.astralvisuals.utils.display.color.ColorAssist;
import pl.astralvisuals.utils.display.font.Fonts;
import pl.astralvisuals.utils.display.scissor.ScissorAssist;
import pl.astralvisuals.utils.display.shape.ShapeProperties;
import pl.astralvisuals.utils.display.style.GlassStyle;
import pl.astralvisuals.utils.math.calc.Calculate;

public class ModuleSettingsWindow extends AbstractWindow {
   private final List<AbstractSettingComponent> components = new ArrayList<>();
   public final Module module;

   public ModuleSettingsWindow(Module module) {
      this.module = module;
      new SettingComponentAdder().addSettingComponent(module.settings(), this.components);
      this.draggable(true);
   }

   @Override
   public void drawWindow(class_332 context, int mouseX, int mouseY, float delta) {
      class_4587 matrix = context.method_51448();
      ScissorAssist scissorManager = Force.getInstance().getScissorManager();
      this.height = class_3532.method_15340(this.getComponentHeight() + 5, 0, 200);
      GlassStyle.strongBackdrop(matrix, this.x, this.y, this.width + 1.5F, this.height, 12.0F);
      GlassStyle.surface(matrix, this.x, this.y, this.width + 1.5F, 22.0F, new Vector4f(12.0F, 0.0F, 12.0F, 0.0F), false);
      Fonts.getSize(15, Fonts.Type.DEFAULT).drawString(matrix, this.module.getVisibleName(), this.x + 19.0F, this.y + 10.0F, ColorAssist.getText());
      Fonts.getSize(15, Fonts.Type.ICONS).drawString(matrix, "H", this.x + 8.0F, this.y + 11.0F, ColorAssist.getText());
      Fonts.getSize(20, Fonts.Type.BOLD).drawString(matrix, "x", this.x + 146.0F, this.y + 8.5F, ColorAssist.getText());
      boolean isLimitedHeight = class_3532.method_15363(this.height, 0.0F, 200.0F) == 200.0F;
      if (isLimitedHeight) {
         scissorManager.push(matrix.method_23760().method_23761(), this.x, this.y + 23.0F, this.width, this.height - 24.0F);
      }

      float offset = 0.0F;
      int totalHeight = 0;

      for (int i = this.components.size() - 1; i >= 0; i--) {
         AbstractSettingComponent component = this.components.get(i);
         Supplier<Boolean> visible = component.getSetting().getVisible();
         if (visible == null || visible.get()) {
            component.x = this.x;
            component.y = (float)(this.y + 22.0F + offset + (this.getComponentHeight() - 25 - component.height) + this.smoothedScroll);
            component.width = this.width;
            component.render(context, mouseX, mouseY, delta);
            offset -= component.height;
            totalHeight += (int)component.height;
         }
      }

      if (isLimitedHeight) {
         scissorManager.pop();
      }

      int maxScroll = (int)Math.max(0.0F, totalHeight - (this.height - 28.0F));
      this.scroll = class_3532.method_15350(this.scroll, -maxScroll, 0.0);
      this.smoothedScroll = class_3532.method_16436(0.1F, this.smoothedScroll, this.scroll);
      if (isLimitedHeight) {
         float viewableHeight = this.height - 30.0F;
         float scrollbarHeight = Math.max(20.0F, viewableHeight / totalHeight * viewableHeight);
         float scrollPercent = (float)(-this.smoothedScroll / maxScroll);
         float scrollbarY = this.y + 30.0F + scrollPercent * (viewableHeight - scrollbarHeight);
         float scrollbarX = this.x + this.width - 6.0F;
         float scrollbarWidth = 3.0F;
         rectangle.render(
            ShapeProperties.create(matrix, scrollbarX, this.y + 30.0F, scrollbarWidth, viewableHeight - 6.0F)
               .round(1.0F)
               .color(new Color(30, 30, 30, 100).getRGB())
               .build()
         );
         rectangle.render(
            ShapeProperties.create(matrix, scrollbarX, scrollbarY, scrollbarWidth, scrollbarHeight - 6.0F)
               .round(1.5F)
               .color(new Color(100, 100, 100, 180).getRGB())
               .build()
         );
      }
   }

   @Override
   public boolean mouseClicked(double mouseX, double mouseY, int button) {
      if (button == 0) {
         if (Calculate.isHovered(mouseX, mouseY, this.x + 145.0F, this.y + 5.0F, 15.0, 15.0)) {
            this.startCloseAnimation();
            return true;
         }

         if (Calculate.isHovered(mouseX, mouseY, this.x, this.y, this.width, 19.0)) {
            this.dragging = true;
            this.dragX = (int)(this.x - mouseX);
            this.dragY = (int)(this.y - mouseY);
            return true;
         }
      }

      boolean isAnyComponentHovered = this.components.stream().anyMatch(abstractComponent -> abstractComponent.isHover(mouseX, mouseY));
      if (isAnyComponentHovered) {
         this.components.forEach(abstractComponent -> {
            if (abstractComponent.isHover(mouseX, mouseY)) {
               abstractComponent.mouseClicked(mouseX, mouseY, button);
            }
         });
         return true;
      } else {
         this.components.forEach(abstractComponent -> abstractComponent.mouseClicked(mouseX, mouseY, button));
         return true;
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

      return super.isHovered(mouseX, mouseY);
   }

   @Override
   public boolean mouseReleased(double mouseX, double mouseY, int button) {
      this.dragging = false;
      this.components.forEach(abstractComponent -> abstractComponent.mouseReleased(mouseX, mouseY, button));
      return super.mouseReleased(mouseX, mouseY, button);
   }

   @Override
   public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
      boolean handled = false;

      for (AbstractSettingComponent component : this.components) {
         handled |= component.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
      }

      return this.dragging || handled || super.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
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
}
