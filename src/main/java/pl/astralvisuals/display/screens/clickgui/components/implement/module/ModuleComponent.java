package pl.astralvisuals.display.screens.clickgui.components.implement.module;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import net.minecraft.class_332;
import pl.astralvisuals.common.animation.Animation;
import pl.astralvisuals.common.animation.Direction;
import pl.astralvisuals.common.animation.implement.Decelerate;
import pl.astralvisuals.display.screens.clickgui.MenuScreen;
import pl.astralvisuals.display.screens.clickgui.components.AbstractComponent;
import pl.astralvisuals.display.screens.clickgui.components.implement.other.ToggleComponent;
import pl.astralvisuals.display.screens.clickgui.components.implement.settings.AbstractSettingComponent;
import pl.astralvisuals.features.module.Module;
import pl.astralvisuals.features.module.setting.SettingComponentAdder;
import pl.astralvisuals.utils.client.chat.StringHelper;
import pl.astralvisuals.utils.client.sound.SoundManager;
import pl.astralvisuals.utils.display.color.ColorAssist;
import pl.astralvisuals.utils.display.font.Fonts;
import pl.astralvisuals.utils.display.shape.ShapeProperties;
import pl.astralvisuals.utils.display.style.GlassStyle;
import pl.astralvisuals.utils.math.calc.Calculate;

public class ModuleComponent extends AbstractComponent {
   public static boolean anyBinding = false;
   private final List<AbstractSettingComponent> components = new ArrayList<>();
   private final ToggleComponent toggleComponent = new ToggleComponent();
   private final Module module;
   private boolean binding;
   private final Animation alphaAnimation = new Decelerate().setMs(400).setValue(105.0);

   public ModuleComponent(Module module) {
      this.module = module;
      new SettingComponentAdder().addSettingComponent(module.settings(), this.components);
      this.alphaAnimation.setDirection(module.isState() ? Direction.FORWARDS : Direction.BACKWARDS);
      this.alphaAnimation.reset();
   }

   @Override
   public void render(class_332 context, int mouseX, int mouseY, float delta) {
      this.alphaAnimation.setDirection(this.module.isState() ? Direction.FORWARDS : Direction.BACKWARDS);
      int alphaOffset = 150 + this.alphaAnimation.getOutput().intValue();
      this.height = 30.0F;
      boolean hovered = Calculate.isHovered(mouseX, mouseY, this.x, this.y, this.width, this.height);
      GlassStyle.surface(context.method_51448(), this.x, this.y, this.width, this.height, 11.0F, hovered);
      // Неоновое свечение на включённых модулях (интенсивность по анимации).
      float accentIntensity = (float)(this.alphaAnimation.getOutput().doubleValue() / 105.0);
      GlassStyle.accent(context.method_51448(), this.x, this.y, this.width, this.height, 11.0F, accentIntensity);
      this.toggleComponent.x = this.x + this.width - 29.0F;
      this.toggleComponent.y = this.y + 9.0F;
      this.toggleComponent.setSize(24.0F, 12.0F);
      this.toggleComponent.setRunnable(this.module::switchState);
      this.toggleComponent.setState(this.module.isState());
      this.toggleComponent.render(context, mouseX, mouseY, delta);
      String displayName = this.binding ? "Нажмите любую клавишу..." : this.module.getVisibleName();
      String bindName = StringHelper.getBindName(this.module.getKey());
      Fonts.getSize(16, Fonts.Type.DEFAULT)
         .drawString(context.method_51448(), displayName, this.x + 7.0F, this.y + 7.5F, new Color(255, 255, 255, alphaOffset).getRGB());
      Fonts.getSize(16, Fonts.Type.DEFAULT)
         .drawString(context.method_51448(), "Bind: " + bindName, this.x + 7.0F, this.y + 17.5F, new Color(255, 255, 255, alphaOffset).getRGB());
   }

   @Override
   public boolean mouseClicked(double mouseX, double mouseY, int button) {
      float bindTextX = this.x + 7.0F;
      float bindTextY = this.y + 17.5F;
      float bindTextWidth = this.width - 40.0F;
      float bindTextHeight = 12.0F;
      if (Calculate.isHovered(mouseX, mouseY, bindTextX, bindTextY, bindTextWidth, bindTextHeight) && button == 0) {
         this.binding = !this.binding;
         anyBinding = this.binding;
         return true;
      } else if (Calculate.isHovered(mouseX, mouseY, this.x, this.y, this.width, this.height) && button == 2) {
         this.binding = !this.binding;
         anyBinding = this.binding;
         return true;
      } else if (Calculate.isHovered(mouseX, mouseY, this.x, this.y, this.width, this.height) && button == 1 && !this.module.settings().isEmpty()) {
         ModuleSettingsWindow settingsWindow = windowManager.getWindows()
            .stream()
            .filter(window -> window instanceof ModuleSettingsWindow existingWindow && existingWindow.module.equals(this.module))
            .map(ModuleSettingsWindow.class::cast)
            .findFirst()
            .orElseGet(() -> {
               ModuleSettingsWindow newWindow = new ModuleSettingsWindow(this.module);
               windowManager.add(newWindow);
               return newWindow;
            });
         MenuScreen menu = MenuScreen.INSTANCE;
         float windowHeight = Math.min(settingsWindow.getComponentHeight(), 200);
         float windowX = menu.x + menu.width + 10;
         float windowY = menu.y - 5;
         settingsWindow.position(windowX, windowY).size(160.0F, (float)settingsWindow.getComponentHeight());
         return true;
      } else if (this.binding && button != 2) {
         this.module.setKey(button);
         this.binding = false;
         anyBinding = false;
         return true;
      } else {
         this.toggleComponent.mouseClicked(mouseX, mouseY, button);
         return super.mouseClicked(mouseX, mouseY, button);
      }
   }

   @Override
   public boolean isHover(double mouseX, double mouseY) {
      return Calculate.isHovered(mouseX, mouseY, this.x, this.y, this.width, this.height);
   }

   @Override
   public void tick() {
      this.components.forEach(AbstractComponent::tick);
      super.tick();
   }

   @Override
   public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
      if (this.binding) {
         if (keyCode == 261) {
            this.module.setKey(-1);
            this.binding = false;
            anyBinding = false;
            SoundManager.playSound(SoundManager.ENABLE_MODULE, 1.0F, 1.0F);
            return true;
         } else if (keyCode == 256) {
            this.binding = false;
            anyBinding = false;
            SoundManager.playSound(SoundManager.ENABLE_MODULE, 1.0F, 1.0F);
            return true;
         } else {
            this.module.setKey(keyCode);
            this.binding = false;
            anyBinding = false;
            SoundManager.playSound(SoundManager.ENABLE_MODULE, 1.0F, 1.0F);
            return true;
         }
      } else {
         return super.keyPressed(keyCode, scanCode, modifiers);
      }
   }

   public int getComponentHeight() {
      return 30;
   }

   @Override
   public boolean equals(Object o) {
      if (this == o) {
         return true;
      } else if (o != null && this.getClass() == o.getClass()) {
         ModuleComponent that = (ModuleComponent)o;
         return this.module.equals(that.module);
      } else {
         return false;
      }
   }

   @Override
   public int hashCode() {
      return Objects.hash(this.module);
   }

   public List<AbstractSettingComponent> getComponents() {
      return this.components;
   }

   public ToggleComponent getToggleComponent() {
      return this.toggleComponent;
   }

   public Module getModule() {
      return this.module;
   }

   public boolean isBinding() {
      return this.binding;
   }

   public Animation getAlphaAnimation() {
      return this.alphaAnimation;
   }
}
