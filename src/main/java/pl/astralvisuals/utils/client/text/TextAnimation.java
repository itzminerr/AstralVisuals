package pl.astralvisuals.utils.client.text;

import java.util.Arrays;
import java.util.List;

public class TextAnimation {
   private final List<String> messages;
   private String currentText = "";
   private int currentMessageIndex = 0;
   private int animationTick = 0;
   private boolean isRemoving = false;
   private boolean showUnderscore = true;
   private int underscoreTick = 0;
   private final int delayTicks = 2;
   private final int pauseTicksMax = 60;
   private int pauseTicks = 0;
   private final int underscoreBlinkTicks = 10;

   public TextAnimation() {
      this.messages = Arrays.asList(
         "С возвращением в AstralVisuals.", "Клиент готов к запуску.", "Все лишнее вычищено, можно продолжать.", "Настрой интерфейс и заходи в игру."
      );
   }

   public void updateText() {
      if (this.pauseTicks > 0) {
         this.pauseTicks--;
         this.updateUnderscore();
      } else {
         if (this.animationTick >= 2) {
            String fullText = this.messages.get(this.currentMessageIndex);
            if (this.isRemoving) {
               if (!this.currentText.isEmpty()) {
                  this.currentText = this.currentText.substring(0, this.currentText.length() - 1);
               } else {
                  this.isRemoving = false;
                  this.currentMessageIndex = (this.currentMessageIndex + 1) % this.messages.size();
                  this.pauseTicks = 60;
               }
            } else if (this.currentText.length() < fullText.length()) {
               this.currentText = fullText.substring(0, this.currentText.length() + 1);
            } else {
               this.isRemoving = true;
               this.pauseTicks = 60;
            }

            this.animationTick = 0;
         }

         this.animationTick++;
         this.updateUnderscore();
      }
   }

   private void updateUnderscore() {
      this.underscoreTick++;
      if (this.underscoreTick >= 10) {
         this.showUnderscore = !this.showUnderscore;
         this.underscoreTick = 0;
      }
   }

   public String getCurrentText() {
      return this.currentText + (this.showUnderscore ? "_" : "");
   }
}
