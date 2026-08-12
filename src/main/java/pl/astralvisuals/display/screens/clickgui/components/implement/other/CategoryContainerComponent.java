package pl.astralvisuals.display.screens.clickgui.components.implement.other;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.class_332;
import pl.astralvisuals.Force;
import pl.astralvisuals.display.screens.clickgui.components.AbstractComponent;
import pl.astralvisuals.display.screens.clickgui.components.implement.category.CategoryComponent;
import pl.astralvisuals.features.module.ModuleCategory;

public class CategoryContainerComponent extends AbstractComponent {
   private final List<CategoryComponent> categoryComponents = new ArrayList<>();
   public static final float ICON_SIZE = 25.0F;
   public static final float ICON_GAP = 10.0F;

   public void initializeCategoryComponents() {
      this.categoryComponents.clear();

      for (ModuleCategory category : ModuleCategory.values()) {
         if (this.shouldDisplayCategory(category)) {
            this.categoryComponents.add(new CategoryComponent(category));
         }
      }
   }

   private boolean shouldDisplayCategory(ModuleCategory category) {
      if (category == ModuleCategory.CONFIGS) {
         return true;
      } else {
         Force force = Force.getInstance();
         return force != null
            && force.getModuleRepository() != null
            && force.getModuleRepository().modules().stream().anyMatch(m -> m.getCategory() == category);
      }
   }

   @Override
   public void render(class_332 context, int mouseX, int mouseY, float delta) {
      float offset = 0.0F;

      for (CategoryComponent component : this.categoryComponents) {
         component.x = this.x + 7.4F;
         component.y = this.y + 44.0F + offset;
         component.width = 25.0F;
         component.height = 17.0F;
         component.render(context, mouseX, mouseY, delta);
         offset += 35.0F;
      }
   }

   @Override
   public void tick() {
      this.categoryComponents.forEach(AbstractComponent::tick);
      super.tick();
   }

   @Override
   public boolean mouseClicked(double mx, double my, int btn) {
      this.categoryComponents.forEach(c -> c.mouseClicked(mx, my, btn));
      return super.mouseClicked(mx, my, btn);
   }

   @Override
   public boolean mouseReleased(double mx, double my, int btn) {
      this.categoryComponents.forEach(c -> c.mouseReleased(mx, my, btn));
      return super.mouseReleased(mx, my, btn);
   }

   @Override
   public boolean mouseDragged(double mx, double my, int btn, double dx, double dy) {
      this.categoryComponents.forEach(c -> c.mouseDragged(mx, my, btn, dx, dy));
      return super.mouseDragged(mx, my, btn, dx, dy);
   }

   @Override
   public boolean mouseScrolled(double mx, double my, double amt) {
      this.categoryComponents.forEach(c -> c.mouseScrolled(mx, my, amt));
      return super.mouseScrolled(mx, my, amt);
   }

   @Override
   public boolean keyPressed(int kc, int sc, int mod) {
      this.categoryComponents.forEach(c -> c.keyPressed(kc, sc, mod));
      return super.keyPressed(kc, sc, mod);
   }

   @Override
   public boolean charTyped(char chr, int mod) {
      this.categoryComponents.forEach(c -> c.charTyped(chr, mod));
      return super.charTyped(chr, mod);
   }
}
