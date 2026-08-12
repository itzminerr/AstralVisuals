package pl.astralvisuals.display.screens.mainmenu;

import java.awt.Color;
import net.minecraft.class_2561;
import net.minecraft.class_332;
import net.minecraft.class_437;
import pl.astralvisuals.display.screens.mainmenu.altmanager.AltManager;
import pl.astralvisuals.utils.display.color.ColorAssist;
import pl.astralvisuals.utils.display.interfaces.QuickImports;

public class AltManagerHost extends class_437 implements QuickImports {
   private final class_437 parent;
   private AltManager altManager;

   public AltManagerHost(class_437 parent) {
      super(class_2561.method_30163("Смена аккаунта"));
      this.parent = parent;
   }

   protected void method_25426() {
      super.method_25426();
      this.updateAltPosition();
   }

   public void method_25393() {
      super.method_25393();
      if (this.altManager != null) {
         this.altManager.tick();
      }
   }

   public void method_25394(class_332 context, int mouseX, int mouseY, float delta) {
      this.method_57728(context, delta);
      context.method_25294(0, 0, this.field_22789, this.field_22790, -1442840576);
      this.updateAltPosition();
      Color buttonColor = new Color(ColorAssist.getGuiRectColor(1.0F), true);
      Color outlineColor = new Color(ColorAssist.getOutline(), true);
      Color gradientColor = new Color(ColorAssist.getGuiRectColor2(1.0F), true);
      Color textColor = new Color(ColorAssist.getText(), true);
      Color bgColor = new Color(ColorAssist.getRect(0.9F), true);
      this.altManager.render(context, buttonColor, outlineColor, gradientColor, textColor, bgColor);
   }

   public boolean method_25402(double mouseX, double mouseY, int button) {
      return this.altManager != null && this.altManager.mouseClicked(mouseX, mouseY, button) ? true : super.method_25402(mouseX, mouseY, button);
   }

   public boolean method_25401(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
      return this.altManager != null && this.altManager.mouseScrolled(mouseX, mouseY, verticalAmount)
         ? true
         : super.method_25401(mouseX, mouseY, horizontalAmount, verticalAmount);
   }

   public boolean method_25403(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
      return this.altManager != null && this.altManager.mouseDragged(mouseX, mouseY, button)
         ? true
         : super.method_25403(mouseX, mouseY, button, deltaX, deltaY);
   }

   public boolean method_25406(double mouseX, double mouseY, int button) {
      if (this.altManager != null) {
         this.altManager.mouseReleased();
      }

      return super.method_25406(mouseX, mouseY, button);
   }

   public boolean method_25400(char chr, int modifiers) {
      return this.altManager != null && this.altManager.charTyped(chr) ? true : super.method_25400(chr, modifiers);
   }

   public boolean method_25404(int keyCode, int scanCode, int modifiers) {
      if (keyCode == 256) {
         this.method_25419();
         return true;
      } else {
         return this.altManager != null && this.altManager.keyPressed(keyCode) ? true : super.method_25404(keyCode, scanCode, modifiers);
      }
   }

   public void method_25419() {
      mc.method_1507(this.parent);
   }

   private void updateAltPosition() {
      float panelWidth = this.altManager == null ? 160.0F : this.altManager.getPanelWidth();
      float panelHeight = this.altManager == null ? 210.0F : this.altManager.getPanelHeight();
      float panelX = this.field_22789 / 2.0F - panelWidth / 2.0F;
      float panelY = this.field_22790 / 2.0F - panelHeight / 2.0F;
      if (this.altManager == null) {
         this.altManager = new AltManager(panelX, panelY);
      } else {
         this.altManager.updatePosition(panelX, panelY);
      }
   }
}
