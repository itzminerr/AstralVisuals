package pl.astralvisuals.display.screens.clickgui;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import net.minecraft.class_2561;
import net.minecraft.class_332;
import net.minecraft.class_437;
import pl.astralvisuals.common.animation.Easy.Direction;
import pl.astralvisuals.common.animation.Easy.EaseBackIn;
import pl.astralvisuals.display.screens.clickgui.components.AbstractComponent;
import pl.astralvisuals.display.screens.clickgui.components.implement.module.ModuleComponent;
import pl.astralvisuals.display.screens.clickgui.components.implement.other.BackgroundComponent;
import pl.astralvisuals.display.screens.clickgui.components.implement.other.CategoryContainerComponent;
import pl.astralvisuals.display.screens.clickgui.components.implement.other.SearchComponent;
import pl.astralvisuals.display.screens.clickgui.components.implement.other.UserComponent;
import pl.astralvisuals.display.screens.clickgui.components.implement.settings.TextComponent;
import pl.astralvisuals.features.impl.render.Interface;
import pl.astralvisuals.features.module.ModuleCategory;
import pl.astralvisuals.utils.client.sound.SoundManager;
import pl.astralvisuals.utils.display.interfaces.QuickImports;
import pl.astralvisuals.utils.display.scale.UiScale;
import pl.astralvisuals.utils.display.shape.ShapeProperties;
import pl.astralvisuals.utils.math.calc.Calculate;

public class MenuScreen extends class_437 implements QuickImports {
   public static MenuScreen INSTANCE = new MenuScreen();
   private final List<AbstractComponent> components = new ArrayList<>();
   private final BackgroundComponent backgroundComponent = new BackgroundComponent();
   private final UserComponent userComponent = new UserComponent();
   private final SearchComponent searchComponent = new SearchComponent();
   private final CategoryContainerComponent categoryContainerComponent = new CategoryContainerComponent();
   public final EaseBackIn animation = new EaseBackIn(325, 1.0, 1.5F);
   public ModuleCategory category = ModuleCategory.RENDER;
   public int x;
   public int y;
   public int width;
   public int height;
   private double lastTransformedMouseX = 0.0;
   private double lastTransformedMouseY = 0.0;
   private boolean pointerCaptured;
   private float capturedGuiScale = 1.0F;

   public MenuScreen() {
      super(class_2561.method_30163("MenuScreen"));
      this.initialize();
   }

   public void initialize() {
      this.animation.setDirection(Direction.FORWARDS);
      this.pointerCaptured = false;
      this.capturedGuiScale = 1.0F;
      this.categoryContainerComponent.initializeCategoryComponents();
      this.components.clear();
      this.components.addAll(Arrays.asList(this.backgroundComponent, this.userComponent, this.searchComponent, this.categoryContainerComponent));
   }

   public void method_25393() {
      this.method_25419();
      this.components.forEach(AbstractComponent::tick);
      super.method_25393();
   }

   private double[] transformMouseCoords(double mouseX, double mouseY) {
      float guiScale = this.pointerCaptured ? this.capturedGuiScale : this.getGuiScale();
      float scale = guiScale * this.getRenderAnimationScale();
      if (scale <= 0.01F) {
         scale = 1.0F;
      }

      float centerX = this.x + this.width / 2.0F - 5.0F;
      float centerY = this.y + this.height / 2.0F;
      double transformedX = (mouseX - centerX) / scale + centerX;
      double transformedY = (mouseY - centerY) / scale + centerY;
      return new double[]{transformedX, transformedY};
   }

   public void method_25394(class_332 context, int mouseX, int mouseY, float delta) {
      this.x = window.method_4486() / 2 - 200 + 4;
      this.y = window.method_4502() / 2 - 125 + 2;
      this.width = 400;
      this.height = 250;
      double[] transformed = this.transformMouseCoords(mouseX, mouseY);
      this.lastTransformedMouseX = transformed[0];
      this.lastTransformedMouseY = transformed[1];
      // Глубокий затемняющий оверлей всего экрана с лёгким неоновым отливом (cyber-атмосфера):
      // верх — чёрный с синим тоном, низ — чистый чёрный, прозрачность растёт вместе с анимацией.
      float dimAlpha = 120.0F * this.getScaleAnimation();
      rectangle.render(
         ShapeProperties.create(context.method_51448(), 0.0, 0.0, window.method_4486(), window.method_4502())
            .color(
               Calculate.applyOpacity(new java.awt.Color(6, 8, 16).getRGB(), dimAlpha),
               Calculate.applyOpacity(new java.awt.Color(0, 0, 0).getRGB(), dimAlpha),
               Calculate.applyOpacity(new java.awt.Color(10, 6, 18).getRGB(), dimAlpha),
               Calculate.applyOpacity(new java.awt.Color(0, 0, 0).getRGB(), dimAlpha)
            )
            .build()
      );
      this.backgroundComponent.position(this.x, this.y).size(this.width, this.height);
      this.userComponent.position(this.x, this.y + this.height);
      if (this.category != ModuleCategory.COMBAT
         && this.category != ModuleCategory.RENDER
         && this.category != ModuleCategory.PLAYER) {
         this.searchComponent.position(this.x + this.width + 9999.0F, this.y - 9999.0F);
         this.searchComponent.setText("");
      } else {
         this.searchComponent.position(this.x + 276.0F, this.y + 10.0F);
      }

      this.categoryContainerComponent.position(this.x, this.y);
      float centerX = this.x + this.width / 2.0F - 5.0F;
      float centerY = this.y + this.height / 2.0F;
      UiScale.render(
         this.getGuiScale(),
         centerX,
         centerY,
         () -> Calculate.scale(context.method_51448(), centerX, centerY, this.getScaleAnimation(), () -> {
            this.components.forEach(component -> component.render(context, (int)this.lastTransformedMouseX, (int)this.lastTransformedMouseY, delta));
            windowManager.render(context, (int)this.lastTransformedMouseX, (int)this.lastTransformedMouseY, delta);
         })
      );
      super.method_25394(context, mouseX, mouseY, delta);
   }

   public void openGui() {
      this.animation.setDirection(Direction.FORWARDS);
      this.animation.reset();
      this.pointerCaptured = false;
      this.capturedGuiScale = this.getGuiScale();
      mc.method_1507(this);
      SoundManager.playSound(SoundManager.OPEN_GUI);
   }

   public float getScaleAnimation() {
      return (float)this.animation.getOutput();
   }

   public float getGuiScale() {
      Interface interfaceModule = Interface.getInstance();
      return interfaceModule == null ? 1.0F : interfaceModule.getGuiScale();
   }

   private float getRenderAnimationScale() {
      float scale = this.getScaleAnimation();
      return scale == 1.0F ? 1.0F : 0.5F + scale / 2.0F;
   }

   public boolean method_25402(double mouseX, double mouseY, int button) {
      if (button == 0 && !this.pointerCaptured) {
         this.capturedGuiScale = this.getGuiScale();
         this.pointerCaptured = true;
      }

      double[] transformed = this.transformMouseCoords(mouseX, mouseY);
      boolean windowHandled = windowManager.mouseClicked(transformed[0], transformed[1], button);
      if (!windowHandled) {
         for (AbstractComponent component : this.components) {
            component.mouseClicked(transformed[0], transformed[1], button);
         }
      }

      return super.method_25402(mouseX, mouseY, button);
   }

   public boolean method_25406(double mouseX, double mouseY, int button) {
      double[] transformed = this.transformMouseCoords(mouseX, mouseY);

      for (AbstractComponent component : this.components) {
         component.mouseReleased(transformed[0], transformed[1], button);
      }

      windowManager.mouseReleased(transformed[0], transformed[1], button);
      if (button == 0) {
         this.pointerCaptured = false;
      }

      return super.method_25406(mouseX, mouseY, button);
   }

   public boolean method_25403(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
      double[] transformed = this.transformMouseCoords(mouseX, mouseY);
      boolean windowHandled = windowManager.mouseDragged(transformed[0], transformed[1], button, deltaX, deltaY);
      if (!windowHandled) {
         for (AbstractComponent component : this.components) {
            component.mouseDragged(transformed[0], transformed[1], button, deltaX, deltaY);
         }
      }

      return super.method_25403(mouseX, mouseY, button, deltaX, deltaY);
   }

   public boolean method_25401(double mouseX, double mouseY, double horizontal, double vertical) {
      double[] transformed = this.transformMouseCoords(mouseX, mouseY);
      boolean windowHandled = windowManager.mouseScrolled(transformed[0], transformed[1], vertical);
      if (!windowHandled) {
         for (AbstractComponent component : this.components) {
            component.mouseScrolled(transformed[0], transformed[1], vertical);
         }
      }

      return super.method_25401(mouseX, mouseY, horizontal, vertical);
   }

   public boolean method_25404(int keyCode, int scanCode, int modifiers) {
      if (!windowManager.keyPressed(keyCode, scanCode, modifiers)) {
         for (AbstractComponent component : this.components) {
            if (component.keyPressed(keyCode, scanCode, modifiers)) {
               return true;
            }
         }
      }

      if (keyCode == 256 && this.method_25422() && !ModuleComponent.anyBinding) {
         SoundManager.playSound(SoundManager.CLOSE_GUI);
         this.animation.setDirection(Direction.BACKWARDS);
         return true;
      } else {
         return super.method_25404(keyCode, scanCode, modifiers);
      }
   }

   public boolean method_25400(char chr, int modifiers) {
      if (!windowManager.charTyped(chr, modifiers)) {
         for (AbstractComponent component : this.components) {
            component.charTyped(chr, modifiers);
         }
      }

      return super.method_25400(chr, modifiers);
   }

   public boolean method_25421() {
      return false;
   }

   public void method_25419() {
      if (this.animation.finished(Direction.BACKWARDS)) {
         TextComponent.typing = false;
         SearchComponent.typing = false;
         super.method_25419();
      }
   }

   public void setCategory(ModuleCategory category) {
      this.category = category;
   }

   public void setX(int x) {
      this.x = x;
   }

   public void setY(int y) {
      this.y = y;
   }

   public void setWidth(int width) {
      this.width = width;
   }

   public void setHeight(int height) {
      this.height = height;
   }

   public void setLastTransformedMouseX(double lastTransformedMouseX) {
      this.lastTransformedMouseX = lastTransformedMouseX;
   }

   public void setLastTransformedMouseY(double lastTransformedMouseY) {
      this.lastTransformedMouseY = lastTransformedMouseY;
   }

   public List<AbstractComponent> getComponents() {
      return this.components;
   }

   public BackgroundComponent getBackgroundComponent() {
      return this.backgroundComponent;
   }

   public UserComponent getUserComponent() {
      return this.userComponent;
   }

   public SearchComponent getSearchComponent() {
      return this.searchComponent;
   }

   public CategoryContainerComponent getCategoryContainerComponent() {
      return this.categoryContainerComponent;
   }

   public EaseBackIn getAnimation() {
      return this.animation;
   }

   public ModuleCategory getCategory() {
      return this.category;
   }

   public int getX() {
      return this.x;
   }

   public int getY() {
      return this.y;
   }

   public int getWidth() {
      return this.width;
   }

   public int getHeight() {
      return this.height;
   }

   public double getLastTransformedMouseX() {
      return this.lastTransformedMouseX;
   }

   public double getLastTransformedMouseY() {
      return this.lastTransformedMouseY;
   }
}
