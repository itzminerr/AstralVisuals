package pl.astralvisuals.display.screens.clickgui.components.implement.window.implement.settings.color;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import net.minecraft.class_332;
import net.minecraft.class_4587;
import pl.astralvisuals.display.screens.clickgui.components.AbstractComponent;
import pl.astralvisuals.display.screens.clickgui.components.implement.other.ToggleComponent;
import pl.astralvisuals.display.screens.clickgui.components.implement.window.AbstractWindow;
import pl.astralvisuals.display.screens.clickgui.components.implement.window.implement.settings.color.component.AlphaComponent;
import pl.astralvisuals.display.screens.clickgui.components.implement.window.implement.settings.color.component.ColorEditorComponent;
import pl.astralvisuals.display.screens.clickgui.components.implement.window.implement.settings.color.component.ColorPresetComponent;
import pl.astralvisuals.display.screens.clickgui.components.implement.window.implement.settings.color.component.HueComponent;
import pl.astralvisuals.display.screens.clickgui.components.implement.window.implement.settings.color.component.SaturationComponent;
import pl.astralvisuals.features.module.setting.implement.ColorSetting;
import pl.astralvisuals.utils.display.color.ColorAssist;
import pl.astralvisuals.utils.display.font.Fonts;
import pl.astralvisuals.utils.display.style.GlassStyle;
import pl.astralvisuals.utils.math.calc.Calculate;

public class ColorWindow extends AbstractWindow {
   private final List<AbstractComponent> components = new ArrayList<>();
   private final ColorSetting setting;
   private final HueComponent hueComponent;
   private final SaturationComponent saturationComponent;
   private final AlphaComponent alphaComponent;
   private final ColorEditorComponent colorEditorComponent;
   private final ColorPresetComponent colorPresetComponent;
   private final ToggleComponent syncToggle = new ToggleComponent();

   public ColorWindow(ColorSetting setting) {
      this.setting = setting;
      this.components
         .addAll(
            Arrays.asList(
               this.hueComponent = new HueComponent(setting),
               this.saturationComponent = new SaturationComponent(setting),
               this.alphaComponent = new AlphaComponent(setting),
               this.colorEditorComponent = new ColorEditorComponent(setting),
               this.colorPresetComponent = new ColorPresetComponent(setting)
            )
         );
      this.syncToggle.setRunnable(() -> this.setting.setSync(!this.setting.isSync()));
   }

   public static float getPreferredHeight(ColorSetting setting) {
      return (setting.getPresets().length == 0 ? 124.0F : 152.0F) + 22.0F;
   }

   @Override
   public void drawWindow(class_332 context, int mouseX, int mouseY, float delta) {
      class_4587 matrix = context.method_51448();
      this.height = getPreferredHeight(this.setting);
      GlassStyle.strongBackdrop(matrix, this.x, this.y, this.width, this.height, 9.0F);
      Fonts.getSize(13, Fonts.Type.DEFAULT).drawString(matrix, this.setting.getName(), this.x + 8.0F, this.y + 13.0F, ColorAssist.getText(0.85F));
      this.components.forEach(component -> {
         component.x = this.x;
         component.y = this.y;
         component.width = this.width;
         component.height = this.height;
         component.render(context, mouseX, mouseY, delta);
      });

      // Строка "Синхронизация" внизу окна — цвет берётся из Client Color.
      float rowY = this.y + this.height - 17.0F;
      Fonts.getSize(12, Fonts.Type.DEFAULT).drawString(matrix, "Синхронизация", this.x + 8.0F, rowY + 2.0F, ColorAssist.getText(0.85F));
      this.syncToggle.setSize(24.0F, 12.0F);
      this.syncToggle.x = this.x + this.width - 32.0F;
      this.syncToggle.y = rowY;
      this.syncToggle.setState(this.setting.isSync());
      this.syncToggle.render(context, mouseX, mouseY, delta);
   }

   @Override
   public boolean mouseClicked(double mouseX, double mouseY, int button) {
      if (button == 1) {
         this.startCloseAnimation();
         return true;
      } else if (button == 0 && Calculate.isHovered(mouseX, mouseY, this.x, this.y, this.width, 20.0)) {
         this.dragging = true;
         this.dragX = (int)(this.x - mouseX);
         this.dragY = (int)(this.y - mouseY);
         return true;
      } else {
         this.syncToggle.mouseClicked(mouseX, mouseY, button);

         for (AbstractComponent component : this.components) {
            component.mouseClicked(mouseX, mouseY, button);
         }

         return true;
      }
   }

   @Override
   public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
      boolean handled = false;

      for (AbstractComponent component : this.components) {
         handled |= component.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
      }

      return this.dragging || handled || super.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
   }

   @Override
   public boolean mouseScrolled(double mouseX, double mouseY, double amount) {
      this.components.forEach(component -> component.mouseScrolled(mouseX, mouseY, amount));
      return super.mouseScrolled(mouseX, mouseY, amount);
   }

   @Override
   public boolean mouseReleased(double mouseX, double mouseY, int button) {
      this.components.forEach(component -> component.mouseReleased(mouseX, mouseY, button));
      return super.mouseReleased(mouseX, mouseY, button);
   }

   public ColorSetting getSetting() {
      return this.setting;
   }
}
