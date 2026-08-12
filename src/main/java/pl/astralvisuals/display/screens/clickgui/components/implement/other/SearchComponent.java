package pl.astralvisuals.display.screens.clickgui.components.implement.other;

import java.awt.Color;
import net.minecraft.class_332;
import net.minecraft.class_437;
import net.minecraft.class_4587;
import org.lwjgl.glfw.GLFW;
import pl.astralvisuals.Force;
import pl.astralvisuals.display.screens.clickgui.MenuScreen;
import pl.astralvisuals.display.screens.clickgui.components.AbstractComponent;
import pl.astralvisuals.utils.display.font.FontRenderer;
import pl.astralvisuals.utils.display.font.Fonts;
import pl.astralvisuals.utils.display.scissor.ScissorAssist;
import pl.astralvisuals.utils.display.shape.ShapeProperties;
import pl.astralvisuals.utils.math.calc.Calculate;

public class SearchComponent extends AbstractComponent {
   public static boolean typing = false;
   private boolean dragging;
   private int cursorPosition = 0;
   private int selectionStart = -1;
   private int selectionEnd = -1;
   private long lastClickTime = 0L;
   private float xOffset = 0.0F;
   private String text = "";

   public void setText(String text) {
      this.text = text;
      this.cursorPosition = 0;
      this.clearSelection();
   }

   @Override
   public void render(class_332 context, int mouseX, int mouseY, float delta) {
      class_4587 matrix = context.method_51448();
      FontRenderer font = Fonts.getSize(16, Fonts.Type.DEFAULT);
      this.updateXOffset(font, this.cursorPosition);
      this.width = 102.0F;
      this.height = 15.0F;
      String displayText = this.text.isEmpty() && !typing ? "Поиск функции..." : this.text;
      ScissorAssist scissor = Force.getInstance().getScissorManager();
      scissor.push(matrix.method_23760().method_23761(), this.x, this.y, this.width, this.height);
      if (typing && this.selectionStart != -1 && this.selectionEnd != -1 && this.selectionStart != this.selectionEnd) {
         int start = Math.max(0, Math.min(this.getStartOfSelection(), this.text.length()));
         int end = Math.max(0, Math.min(this.getEndOfSelection(), this.text.length()));
         if (start < end) {
            float selectionXStart = this.x - this.xOffset + font.getStringWidth(this.text.substring(0, start));
            float selectionXEnd = this.x - this.xOffset + font.getStringWidth(this.text.substring(0, end));
            rectangle.render(
               ShapeProperties.create(matrix, selectionXStart, this.y, selectionXEnd - selectionXStart, this.height)
                  .color(new Color(85, 133, 232, 155).getRGB())
                  .build()
            );
         }
      }

      font.drawString(
         context.method_51448(),
         displayText,
         this.x - this.xOffset,
         this.y + 4.5F,
         typing ? new Color(255, 255, 255, 235).getRGB() : new Color(255, 255, 255, 200).getRGB()
      );
      scissor.pop();
      if (typing && System.currentTimeMillis() % 1000L < 500L && (this.selectionStart == -1 || this.selectionStart == this.selectionEnd)) {
         float cursorX = font.getStringWidth(this.text.substring(0, this.cursorPosition));
         rectangle.render(
            ShapeProperties.create(matrix, this.x - this.xOffset + cursorX, this.y + 3.0F, 0.5, this.height - 6.0F)
               .color(new Color(255, 255, 255, 255).getRGB())
               .build()
         );
      }

      if (this.dragging) {
         double[] transformed = this.transformMouseCoords(mouseX, mouseY);
         this.cursorPosition = this.getCursorIndexAt(transformed[0]);
         if (this.selectionStart == -1) {
            this.selectionStart = this.cursorPosition;
         }

         this.selectionEnd = this.cursorPosition;
      }
   }

   @Override
   public boolean mouseClicked(double mouseX, double mouseY, int button) {
      double[] transformed = this.transformMouseCoords(mouseX, mouseY);
      if (Calculate.isHovered(transformed[0], transformed[1], this.x, this.y, this.width, this.height) && button == 0) {
         long currentTime = System.currentTimeMillis();
         if (currentTime - this.lastClickTime < 250L) {
            this.selectionStart = 0;
            this.selectionEnd = this.text.length();
         } else {
            typing = true;
            this.dragging = true;
            this.lastClickTime = currentTime;
            this.cursorPosition = this.getCursorIndexAt(transformed[0]);
            this.selectionStart = this.cursorPosition;
            this.selectionEnd = this.cursorPosition;
         }
      } else {
         typing = false;
         this.clearSelection();
      }

      return super.mouseClicked(mouseX, mouseY, button);
   }

   @Override
   public boolean mouseReleased(double mouseX, double mouseY, int button) {
      if (button == 0) {
         this.dragging = false;
      }

      return super.mouseReleased(mouseX, mouseY, button);
   }

   @Override
   public boolean charTyped(char chr, int modifiers) {
      if (typing && Fonts.getSize(16, Fonts.Type.DEFAULT).getStringWidth(this.text) < this.width - 12.0F) {
         this.deleteSelectedText();
         this.text = this.text.substring(0, this.cursorPosition) + chr + this.text.substring(this.cursorPosition);
         this.cursorPosition++;
         this.clearSelection();
         return true;
      } else {
         return false;
      }
   }

   @Override
   public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
      if (typing) {
         if (class_437.method_25441()) {
            switch (keyCode) {
               case 65:
                  this.selectAllText();
                  break;
               case 67:
                  this.copyToClipboard();
                  break;
               case 86:
                  this.pasteFromClipboard();
            }
         } else {
            switch (keyCode) {
               case 257:
               case 259:
                  this.handleTextModification(keyCode);
               case 258:
               case 260:
               case 261:
               default:
                  break;
               case 262:
               case 263:
                  this.moveCursor(keyCode);
            }
         }

         return true;
      } else {
         return super.keyPressed(keyCode, scanCode, modifiers);
      }
   }

   private double[] transformMouseCoords(double mouseX, double mouseY) {
      MenuScreen menu = MenuScreen.INSTANCE;
      float scale = Math.max(0.01F, menu.getScaleAnimation());
      float centerX = menu.x + menu.width / 2.0F - 5.0F;
      float centerY = menu.y + menu.height / 2.0F;
      return new double[]{(mouseX - centerX) / scale + centerX, (mouseY - centerY) / scale + centerY};
   }

   private void pasteFromClipboard() {
      String clipboardText = GLFW.glfwGetClipboardString(window.method_4490());
      if (clipboardText != null) {
         this.deleteSelectedText();
         this.replaceText(this.cursorPosition, this.cursorPosition, clipboardText);
      }
   }

   private void copyToClipboard() {
      if (this.hasSelection()) {
         GLFW.glfwSetClipboardString(window.method_4490(), this.getSelectedText());
      }
   }

   private void selectAllText() {
      this.selectionStart = 0;
      this.selectionEnd = this.text.length();
      this.cursorPosition = this.text.length();
   }

   private void handleTextModification(int keyCode) {
      if (keyCode == 259) {
         if (this.hasSelection()) {
            this.replaceText(this.getStartOfSelection(), this.getEndOfSelection(), "");
         } else if (this.cursorPosition > 0) {
            this.replaceText(this.cursorPosition - 1, this.cursorPosition, "");
         }
      } else {
         typing = false;
         this.clearSelection();
      }
   }

   private void moveCursor(int keyCode) {
      if (class_437.method_25442()) {
         if (this.selectionStart == -1) {
            this.selectionStart = this.cursorPosition;
         }
      } else {
         this.clearSelection();
      }

      if (keyCode == 263 && this.cursorPosition > 0) {
         this.cursorPosition--;
      }

      if (keyCode == 262 && this.cursorPosition < this.text.length()) {
         this.cursorPosition++;
      }

      if (class_437.method_25442()) {
         this.selectionEnd = this.cursorPosition;
      }
   }

   private void replaceText(int start, int end, String replacement) {
      if (start < 0) {
         start = 0;
      }

      if (end > this.text.length()) {
         end = this.text.length();
      }

      if (start > end) {
         int tmp = start;
         start = end;
         end = tmp;
      }

      this.text = this.text.substring(0, start) + replacement + this.text.substring(end);
      this.cursorPosition = start + replacement.length();
      this.clearSelection();
   }

   private boolean hasSelection() {
      return this.selectionStart != -1 && this.selectionEnd != -1 && this.selectionStart != this.selectionEnd;
   }

   private String getSelectedText() {
      return this.text.substring(this.getStartOfSelection(), this.getEndOfSelection());
   }

   private int getStartOfSelection() {
      return Math.min(this.selectionStart, this.selectionEnd);
   }

   private int getEndOfSelection() {
      return Math.max(this.selectionStart, this.selectionEnd);
   }

   private void clearSelection() {
      this.selectionStart = -1;
      this.selectionEnd = -1;
   }

   private void deleteSelectedText() {
      if (this.hasSelection()) {
         this.replaceText(this.getStartOfSelection(), this.getEndOfSelection(), "");
      }
   }

   private int getCursorIndexAt(double mouseX) {
      FontRenderer font = Fonts.getSize(16, Fonts.Type.DEFAULT);
      float relativeX = (float)mouseX - this.x + this.xOffset;

      int position;
      for (position = 0; position < this.text.length(); position++) {
         float textWidth = font.getStringWidth(this.text.substring(0, position));
         float charWidth = font.getStringWidth(this.text.substring(position, position + 1));
         if (textWidth + charWidth / 2.0F > relativeX) {
            break;
         }
      }

      return Math.max(0, Math.min(position, this.text.length()));
   }

   private void updateXOffset(FontRenderer font, int cursorPosition) {
      float cursorX = font.getStringWidth(this.text.substring(0, Math.min(cursorPosition, this.text.length())));
      float visibleWidth = this.width - 8.0F;
      if (cursorX < this.xOffset) {
         this.xOffset = cursorX;
      } else if (cursorX - this.xOffset > visibleWidth) {
         this.xOffset = cursorX - visibleWidth;
      }

      if (this.xOffset < 0.0F) {
         this.xOffset = 0.0F;
      }
   }

   public String getText() {
      return this.text;
   }
}
