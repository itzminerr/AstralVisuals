package pl.astralvisuals.display.screens.clickgui.components.implement.category;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.class_332;
import net.minecraft.class_3532;
import net.minecraft.class_4587;
import org.joml.Matrix4f;
import pl.astralvisuals.Force;
import pl.astralvisuals.common.animation.Animation;
import pl.astralvisuals.common.animation.Direction;
import pl.astralvisuals.common.animation.implement.Decelerate;
import pl.astralvisuals.display.screens.clickgui.MenuScreen;
import pl.astralvisuals.display.screens.clickgui.components.AbstractComponent;
import pl.astralvisuals.display.screens.clickgui.components.implement.module.ModuleComponent;
import pl.astralvisuals.features.module.Module;
import pl.astralvisuals.features.module.ModuleCategory;
import pl.astralvisuals.utils.display.color.ColorAssist;
import pl.astralvisuals.utils.display.font.Fonts;
import pl.astralvisuals.utils.display.scissor.ScissorAssist;
import pl.astralvisuals.utils.display.shape.ShapeProperties;
import pl.astralvisuals.utils.math.calc.Calculate;

public class CategoryComponent extends AbstractComponent {
   private static final float CONTENT_X = 84.0F;
   private static final float CONTENT_Y = 39.0F;
   private static final float CONTENT_BOTTOM_PADDING = 4.0F;
   // Свечение активных модулей уходит вверх за их границы (~4.5px). Расширяем scissor
   // вверх на эту величину, чтобы glow верхних модулей не обрезался рамкой списка.
   private static final float GLOW_MARGIN = 6.0F;
   private final List<ModuleComponent> moduleComponents = new ArrayList<>();
   private final ModuleCategory category;
   private final Animation alphaAnimation = new Decelerate().setMs(300).setValue(1.0);
   private double scrollBarAnimation = 0.0;

   public CategoryComponent(ModuleCategory category) {
      this.category = category;
      this.initializeModules();
   }

   private void initializeModules() {
      for (Module module : Force.getInstance().getModuleRepository().modules()) {
         this.moduleComponents.add(new ModuleComponent(module));
      }
   }

   @Override
   public void render(class_332 context, int mouseX, int mouseY, float delta) {
      MenuScreen menu = MenuScreen.INSTANCE;
      Matrix4f positionMatrix = context.method_51448().method_23760().method_23761();
      ScissorAssist scissorManager = Force.getInstance().getScissorManager();
      this.drawCategoryTab(context.method_51448());
      if (menu.getCategory().equals(this.category)) {
         int columnWidth = 165;
         float columnGap = 13.0F;
         int maxScroll = 0;
         float listY = menu.y + 39.0F;
         float listHeight = this.getModuleListHeight(menu);
         int[] columnOffsets = new int[2];
         int componentIndex = 0;
         // Расширяем scissor вверх на GLOW_MARGIN, чтобы свечение верхних модулей не обрезалось.
         scissorManager.push(positionMatrix, menu.x, listY - GLOW_MARGIN, menu.width, listHeight + GLOW_MARGIN);

         for (int i = this.moduleComponents.size() - 1; i >= 0; i--) {
            ModuleComponent component = this.moduleComponents.get(i);
            if (this.shouldRenderComponent(component)) {
               int componentHeight = component.getComponentHeight() + 9;
               int column = componentIndex % 2;
               component.x = menu.x + 48 + column * (columnWidth + columnGap);
               component.y = (float)(listY + columnOffsets[column] + this.smoothedScroll);
               component.width = columnWidth;
               if (component.y > listY - componentHeight && listY + listHeight > component.y) {
                  component.render(context, mouseX, mouseY, delta);
               }

               columnOffsets[column] += componentHeight;
               componentIndex++;
               maxScroll = Math.max(maxScroll, Math.max(columnOffsets[0], columnOffsets[1]));
            }
         }

         scissorManager.pop();
         int clamped = class_3532.method_15340((int)(maxScroll - listHeight), 0, maxScroll);
         this.scroll = class_3532.method_15350(this.scroll, -clamped, 0.0);
         this.smoothedScroll = Calculate.interpolateSmooth(2.0, (float)this.smoothedScroll, (float)this.scroll);
         if (clamped > 0) {
            float scrollBarHeight = listHeight - 8.0F;
            float scrollBarWidth = 2.0F;
            float scrollBarX = menu.x + menu.width - 4;
            float scrollBarY = listY + 4.0F;
            float thumbHeight = 35.0F;
            float scrollProgress = (float)(Math.abs(this.scroll) / clamped);
            float targetThumbY = scrollBarY + (scrollBarHeight - thumbHeight) * scrollProgress;
            this.scrollBarAnimation = Calculate.interpolateSmooth(5.0, (float)this.scrollBarAnimation, targetThumbY);
            rectangle.render(
               ShapeProperties.create(context.method_51448(), scrollBarX, (float)this.scrollBarAnimation, scrollBarWidth, thumbHeight)
                  .round(1.0F)
                  .color(ColorAssist.getColor(255, 255, 255, 25))
                  .build()
            );
         }
      }
   }

   private void drawCategoryTab(class_4587 matrix) {
      boolean active = MenuScreen.INSTANCE.getCategory().equals(this.category);
      this.alphaAnimation.setDirection(active ? Direction.FORWARDS : Direction.BACKWARDS);
      int selectColor = active ? ColorAssist.getColor(255, 255, 255, 255) : ColorAssist.getColor(255, 255, 255, 120);
      Fonts.getSize(18, Fonts.Type.ICONSCATEGORY).drawString(matrix, this.getCategoryIcon(this.category), this.x + 8.0F, this.y + 10.5F, selectColor);
   }

   @Override
   public boolean mouseClicked(double mouseX, double mouseY, int button) {
      MenuScreen menu = MenuScreen.INSTANCE;
      if (Calculate.isHovered(mouseX, mouseY, this.x, this.y, this.width, this.height) && button == 0) {
         menu.setCategory(this.category);
      }

      if (this.isModuleListHovered(menu, mouseX, mouseY)) {
         for (int i = this.moduleComponents.size() - 1; i >= 0; i--) {
            ModuleComponent moduleComponent = this.moduleComponents.get(i);
            if (this.shouldRenderComponent(moduleComponent) && moduleComponent.isHover(mouseX, mouseY)) {
               moduleComponent.mouseClicked(mouseX, mouseY, button);
               return super.mouseClicked(mouseX, mouseY, button);
            }
         }
      }

      return super.mouseClicked(mouseX, mouseY, button);
   }

   @Override
   public boolean isHover(double mouseX, double mouseY) {
      for (ModuleComponent moduleComponent : this.moduleComponents) {
         if (moduleComponent.isHover(mouseX, mouseY)) {
            return true;
         }
      }

      return super.isHover(mouseX, mouseY);
   }

   @Override
   public boolean mouseReleased(double mouseX, double mouseY, int button) {
      this.moduleComponents.forEach(moduleComponent -> moduleComponent.mouseReleased(mouseX, mouseY, button));
      return super.mouseReleased(mouseX, mouseY, button);
   }

   @Override
   public boolean mouseScrolled(double mouseX, double mouseY, double amount) {
      MenuScreen menu = MenuScreen.INSTANCE;
      if (menu.getCategory().equals(this.category) && this.isModuleListHovered(menu, mouseX, mouseY)) {
         this.scroll += amount * 20.0;
      }

      this.moduleComponents.forEach(moduleComponent -> {
         if (this.shouldRenderComponent(moduleComponent)) {
            moduleComponent.mouseScrolled(mouseX, mouseY, amount);
         }
      });
      return super.mouseScrolled(mouseX, mouseY, amount);
   }

   @Override
   public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
      this.moduleComponents.forEach(moduleComponent -> {
         if (this.shouldRenderComponent(moduleComponent)) {
            moduleComponent.keyPressed(keyCode, scanCode, modifiers);
         }
      });
      return super.keyPressed(keyCode, scanCode, modifiers);
   }

   @Override
   public boolean charTyped(char chr, int modifiers) {
      this.moduleComponents.forEach(moduleComponent -> {
         if (this.shouldRenderComponent(moduleComponent)) {
            moduleComponent.charTyped(chr, modifiers);
         }
      });
      return super.charTyped(chr, modifiers);
   }

   private boolean shouldRenderComponent(ModuleComponent component) {
      MenuScreen menu = MenuScreen.INSTANCE;
      String text = menu.getSearchComponent().getText().toLowerCase();
      String moduleName = component.getModule().getVisibleName().toLowerCase();
      return text.equalsIgnoreCase("") ? component.getModule().getCategory().equals(menu.getCategory()) : moduleName.contains(text);
   }

   private float getModuleListHeight(MenuScreen menu) {
      return menu.height - 39.0F - 4.0F;
   }

   private boolean isModuleListHovered(MenuScreen menu, double mouseX, double mouseY) {
      return Calculate.isHovered(mouseX, mouseY, menu.x + 84.0F, menu.y + 39.0F, menu.width - 84.0F, this.getModuleListHeight(menu));
   }

   private String getCategoryIcon(ModuleCategory category) {
      return switch (category) {
         case COMBAT -> "A";
         case RENDER -> "C";
         case PLAYER -> "D";
         case CONFIGS -> "F";
      };
   }
}
