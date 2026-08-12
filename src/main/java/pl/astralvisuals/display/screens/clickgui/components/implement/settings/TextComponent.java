package pl.astralvisuals.display.screens.clickgui.components.implement.settings;

import net.minecraft.class_332;
import net.minecraft.class_437;
import net.minecraft.class_4587;
import org.lwjgl.glfw.GLFW;
import pl.astralvisuals.Force;
import pl.astralvisuals.features.module.setting.implement.TextSetting;
import pl.astralvisuals.utils.client.chat.StringHelper;
import pl.astralvisuals.utils.display.color.ColorAssist;
import pl.astralvisuals.utils.display.font.FontRenderer;
import pl.astralvisuals.utils.display.font.Fonts;
import pl.astralvisuals.utils.display.scissor.ScissorAssist;
import pl.astralvisuals.utils.display.shape.ShapeProperties;
import pl.astralvisuals.utils.math.calc.Calculate;

public class TextComponent extends AbstractSettingComponent {
   public static boolean typing;
   private final TextSetting setting;
   private float rectX;
   private float rectY;
   private float rectWidth;
   private float rectHeight;
   private boolean dragging;
   private int cursorPosition = 0;
   private int selectionStart = -1;
   private int selectionEnd = -1;
   private long lastClickTime = 0L;
   private float xOffset = 0.0F;
   private String text = "";

   public TextComponent(TextSetting setting) {
      super(setting);
      this.setting = setting;
   }

   @Override
   public void render(class_332 context, int mouseX, int mouseY, float delta) {
      class_4587 matrix = context.method_51448();
      String wrapped = StringHelper.wrap(this.setting.getDescription(), 70, 12);
      FontRenderer font = Fonts.getSize(12);
      this.height = (int)(18.0F + font.getStringHeight(wrapped) / 3.0F);
      this.rectX = this.x + this.width - 61.5F;
      this.rectY = this.y + 6.0F;
      this.rectWidth = 53.0F;
      this.rectHeight = 12.0F;
      rectangle.render(
         ShapeProperties.create(matrix, this.rectX, this.rectY, this.rectWidth, this.rectHeight)
            .round(2.0F)
            .thickness(2.0F)
            .outlineColor(ColorAssist.getOutline())
            .color(ColorAssist.getGuiRectColor(1.0F))
            .build()
      );
      Fonts.getSize(14, Fonts.Type.BOLD).drawString(context.method_51448(), this.setting.getName(), this.x + 9.0F, this.y + 6.0F, -2828575);
      font.drawString(context.method_51448(), wrapped, this.x + 9.0F, this.y + 15.0F, -7894892);
      this.updateXOffset(font, this.cursorPosition);
      ScissorAssist scissor = Force.getInstance().getScissorManager();
      scissor.push(matrix.method_23760().method_23761(), this.rectX + 1.0F, window.method_4502() / 2.0F - 96.0F, this.rectWidth - 3.0F, 220.0F);
      if (typing && this.selectionStart != -1 && this.selectionEnd != -1 && this.selectionStart != this.selectionEnd) {
         int start = Math.max(0, Math.min(this.getStartOfSelection(), this.text.length()));
         int end = Math.max(0, Math.min(this.getEndOfSelection(), this.text.length()));
         if (start < end) {
            float selectionXStart = this.rectX + 3.0F - this.xOffset + font.getStringWidth(this.text.substring(0, start));
            float selectionXEnd = this.rectX + 3.0F - this.xOffset + font.getStringWidth(this.text.substring(0, end));
            float selectionWidth = selectionXEnd - selectionXStart;
            rectangle.render(
               ShapeProperties.create(matrix, selectionXStart, this.rectY + this.rectHeight / 2.0F - 4.0F, selectionWidth, 8.0).color(-12961216).build()
            );
         }
      }

      font.drawString(context.method_51448(), this.text, this.rectX + 3.0F - this.xOffset, this.rectY + this.rectHeight / 2.0F - 1.0F, typing ? -1 : -7894892);
      if (!typing && this.text.isEmpty()) {
         font.drawString(context.method_51448(), this.text = this.setting.getText(), this.rectX + 3.0F, this.rectY + this.rectHeight / 2.0F - 1.0F, -7894892);
      }

      scissor.pop();
      long currentTime = System.currentTimeMillis();
      boolean focused = typing && currentTime % 1000L < 500L;
      if (focused && (this.selectionStart == -1 || this.selectionStart == this.selectionEnd)) {
         float cursorX = font.getStringWidth(this.text.substring(0, this.cursorPosition));
         rectangle.render(
            ShapeProperties.create(matrix, this.rectX + 3.0F - this.xOffset + cursorX, this.rectY + this.rectHeight / 2.0F - 3.5F, 0.5, 7.0).color(-1).build()
         );
      }

      if (this.dragging) {
         this.cursorPosition = this.getCursorIndexAt(mouseX);
         if (this.selectionStart == -1) {
            this.selectionStart = this.cursorPosition + 1;
         }

         this.selectionEnd = this.cursorPosition;
      }
   }

   @Override
   public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
      this.dragging = true;
      return super.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
   }

   @Override
   public boolean mouseClicked(double mouseX, double mouseY, int button) {
      if (Calculate.isHovered(mouseX, mouseY, this.rectX, this.rectY, this.rectWidth, this.rectHeight) && button == 0) {
         long currentTime = System.currentTimeMillis();
         if (currentTime - this.lastClickTime < 250L) {
            this.selectionStart = 0;
            this.selectionEnd = this.text.length();
         } else {
            typing = true;
            this.dragging = true;
            this.lastClickTime = currentTime;
            this.cursorPosition = this.getCursorIndexAt(mouseX);
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
      this.dragging = false;
      return super.mouseReleased(mouseX, mouseY, button);
   }

   @Override
   public boolean charTyped(char chr, int modifiers) {
      if (typing && this.text.length() < this.setting.getMax()) {
         this.deleteSelectedText();
         this.text = this.text.substring(0, this.cursorPosition) + chr + this.text.substring(this.cursorPosition);
         this.cursorPosition++;
         this.clearSelection();
      }

      return super.charTyped(chr, modifiers);
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
      }

      return super.keyPressed(keyCode, scanCode, modifiers);
   }

   private void pasteFromClipboard() {
      String clipboardText = GLFW.glfwGetClipboardString(window.method_4490());
      if (clipboardText != null) {
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
   }

   private void handleTextModification(int keyCode) {
      if (keyCode == 259) {
         if (this.hasSelection()) {
            this.replaceText(this.getStartOfSelection(), this.getEndOfSelection(), "");
         } else if (this.cursorPosition > 0) {
            this.replaceText(this.cursorPosition - 1, this.cursorPosition, "");
         }
      } else if (keyCode == 257 && this.text.length() >= this.setting.getMin() && this.text.length() <= this.setting.getMax()) {
         this.setting.setText(this.text);
         typing = false;
      }
   }

   private void moveCursor(int keyCode) {
      if (keyCode == 263 && this.cursorPosition > 0) {
         this.cursorPosition--;
      } else if (keyCode == 262 && this.cursorPosition < this.text.length()) {
         this.cursorPosition++;
      }

      this.updateSelectionAfterCursorMove();
   }

   private void updateSelectionAfterCursorMove() {
      if (class_437.method_25442()) {
         if (this.selectionStart == -1) {
            this.selectionStart = this.cursorPosition;
         }

         this.selectionEnd = this.cursorPosition;
      } else {
         this.clearSelection();
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
         start = end;
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

   private int getCursorIndexAt(double mouseX) {
      FontRenderer font = Fonts.getSize(12, Fonts.Type.BOLD);
      float relativeX = (float)mouseX - this.rectX - 3.0F + this.xOffset;

      int position;
      for (position = 0; position < this.text.length(); position++) {
         float textWidth = font.getStringWidth(this.text.substring(0, position + 1));
         if (textWidth > relativeX) {
            break;
         }
      }

      return position;
   }

   private void updateXOffset(FontRenderer font, int cursorPosition) {
      float cursorX = font.getStringWidth(this.text.substring(0, cursorPosition));
      if (cursorX < this.xOffset) {
         this.xOffset = cursorX;
      } else if (cursorX - this.xOffset > this.rectWidth - 7.0F) {
         this.xOffset = cursorX - (this.rectWidth - 7.0F);
      }
   }

   private void deleteSelectedText() {
      if (this.hasSelection()) {
         this.replaceText(this.getStartOfSelection(), this.getEndOfSelection(), "");
      }
   }
}
