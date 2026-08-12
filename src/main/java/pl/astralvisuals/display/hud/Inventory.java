package pl.astralvisuals.display.hud;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.class_1799;
import net.minecraft.class_332;
import net.minecraft.class_4587;
import org.joml.Vector4f;
import pl.astralvisuals.utils.client.managers.api.draggable.AbstractDraggable;
import pl.astralvisuals.utils.display.color.ColorAssist;
import pl.astralvisuals.utils.display.font.FontRenderer;
import pl.astralvisuals.utils.display.font.Fonts;
import pl.astralvisuals.utils.display.geometry.Render2D;
import pl.astralvisuals.utils.display.shape.ShapeProperties;
import pl.astralvisuals.utils.display.style.GlassStyle;
import pl.astralvisuals.utils.interactions.interact.PlayerInteractionHelper;

public class Inventory extends AbstractDraggable {
   List<class_1799> stacks = new ArrayList<>(27);

   public Inventory() {
      super("Инвентарь", 385, 40, 123, 60, true);
   }

   @Override
   public boolean visible() {
      for (class_1799 stack : this.stacks) {
         if (!stack.method_7960()) {
            return true;
         }
      }

      return PlayerInteractionHelper.isChat(mc.field_1755);
   }

   @Override
   public void tick() {
      this.stacks.clear();
      if (mc.field_1724 != null) {
         for (int i = 9; i < 36; i++) {
            this.stacks.add(mc.field_1724.field_7514.method_5438(i));
         }
      }
   }

   @Override
   public void drawDraggable(class_332 context) {
      class_4587 matrix = context.method_51448();
      FontRenderer font = Fonts.getSize(14, Fonts.Type.DEFAULT);
      FontRenderer items = Fonts.getSize(12, Fonts.Type.DEFAULT);
      FontRenderer icon = Fonts.getSize(20, Fonts.Type.ICONS);
      GlassStyle.backdrop(matrix, this.getX(), this.getY(), this.getWidth(), 15.5F, new Vector4f(9.0F, 0.0F, 9.0F, 0.0F));
      icon.drawString(matrix, "F", this.getX() + 4.5F, this.getY() + 6, new Color(225, 225, 255, 255).getRGB());
      font.drawString(matrix, this.getName(), this.getX() + 22, this.getY() + 6.5F, ColorAssist.getText());
      GlassStyle.backdrop(matrix, this.getX(), this.getY() + 16.4F, this.getWidth(), this.getHeight() - 15, new Vector4f(0.0F, 9.0F, 0.0F, 9.0F));
      int offsetY = 20;
      int offsetX = 4;
      int itemsPerRow = 9;
      int itemIndex = 0;

      for (class_1799 stack : this.stacks) {
         float itemX = this.getX() + offsetX + 1;
         float itemY = this.getY() + offsetY + 1.0F;
         if (itemIndex % itemsPerRow != itemsPerRow - 1) {
            rectangle.render(ShapeProperties.create(matrix, itemX + 10.0F, itemY, 0.5, 9.0).color(ColorAssist.getText(0.1F)).round(0.0F).build());
         }

         if (itemIndex < this.stacks.size() - itemsPerRow) {
            rectangle.render(ShapeProperties.create(matrix, itemX - 0.5F, itemY + 10.0F, 9.0, 0.5).color(ColorAssist.getText(0.1F)).round(0.0F).build());
         }

         Render2D.defaultDrawStack(context, stack, itemX - 1.0F, itemY - 1.0F, false, true, 0.5F);
         offsetX += 13;
         if (++itemIndex % itemsPerRow == 0) {
            offsetY += 13;
            offsetX = 4;
         }
      }
   }
}
