package pl.astralvisuals.display.hud;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import net.minecraft.class_332;
import net.minecraft.class_4587;
import net.minecraft.class_640;
import pl.astralvisuals.utils.client.managers.api.draggable.AbstractDraggable;
import pl.astralvisuals.utils.display.color.ColorAssist;
import pl.astralvisuals.utils.display.font.FontRenderer;
import pl.astralvisuals.utils.display.font.Fonts;
import pl.astralvisuals.utils.display.shape.ShapeProperties;
import pl.astralvisuals.utils.display.style.GlassStyle;
import pl.astralvisuals.utils.math.calc.Calculate;

public class Watermark extends AbstractDraggable {
   private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");
   private int fpsCount = 0;

   public Watermark() {
      super("Водяной знак", 10, 10, 92, 21, true);
   }

   @Override
   public void tick() {
      this.fpsCount = (int)Calculate.interpolate(this.fpsCount, mc.method_47599(), 0.35F);
   }

   @Override
   public void drawDraggable(class_332 e) {
      if (mc.field_1724 != null) {
         class_4587 matrix = e.method_51448();
         FontRenderer font = Fonts.getSize(15, Fonts.Type.DEFAULT);
         FontRenderer logoFont = Fonts.getSize(18, Fonts.Type.BOLD);
         FontRenderer userIconFont = Fonts.getSize(12, Fonts.Type.GUI);
         FontRenderer statIconFont = Fonts.getSize(14, Fonts.Type.HUD);
         FontRenderer timeIconFont = Fonts.getSize(14, Fonts.Type.ICONSTYPENEW);
         FontRenderer dotFont = Fonts.getSize(23, Fonts.Type.BOLD);
         FontRenderer serverIconFont = Fonts.getSize(14, Fonts.Type.HUD);
         String serverIcon = "f";
         String fps = String.valueOf(this.fpsCount);
         String ping = this.getPing();
         String time = LocalTime.now().format(TIME_FORMATTER);
         String server = this.getServerName();
         String playerName = mc.field_1724.method_5477().getString();
         float iconWidth = 18.0F;
         float panelGap = 5.0F;
         float padding = 4.0F;
         float innerPadding = 9.0F;
         float itemGap = 5.0F;
         float separatorGap = 8.0F;
         float dotWidth = dotFont.getStringWidth(".");
         float nameWidth = innerPadding + userIconFont.getStringWidth("B") + itemGap + font.getStringWidth(playerName) + innerPadding;
         float infoWidth = innerPadding
            + statIconFont.getStringWidth("a")
            + itemGap
            + font.getStringWidth(fps)
            + separatorGap
            + dotWidth
            + separatorGap
            + statIconFont.getStringWidth("h")
            + itemGap
            + font.getStringWidth(ping)
            + separatorGap
            + dotWidth
            + separatorGap
            + timeIconFont.getStringWidth("n")
            + itemGap
            + font.getStringWidth(time)
            + innerPadding;
         float serverWidth = innerPadding + serverIconFont.getStringWidth(serverIcon) + itemGap + font.getStringWidth(server) + innerPadding;
         float iconX = this.getX() + padding;
         float nameX = iconX + iconWidth + panelGap;
         float infoX = nameX + nameWidth + panelGap;
         float serverX = infoX + infoWidth + panelGap;
         float totalWidth = padding + iconWidth + panelGap + nameWidth + panelGap + infoWidth + panelGap + serverWidth + padding;
         this.setWidth((int)totalWidth);
         this.setHeight(21);
         this.drawPanel(matrix, this.getX(), this.getY(), totalWidth, 21.0F, 9.0F);
         this.drawPanel(matrix, iconX, this.getY() + 2.5F, iconWidth, 16.0F, 7.0F);
         this.drawPanel(matrix, nameX, this.getY() + 2.5F, nameWidth, 16.0F, 7.0F);
         this.drawPanel(matrix, infoX, this.getY() + 2.5F, infoWidth, 16.0F, 7.0F);
         this.drawPanel(matrix, serverX, this.getY() + 2.5F, serverWidth, 16.0F, 7.0F);
         int textColor = ColorAssist.getColor(255, 255, 255, 220);
         int iconColor = ColorAssist.getColor(255, 255, 255, 255);
         float panelCenterX = iconX + iconWidth / 2.0F;
         float panelCenterY = this.getY() + 2.5F + 8.0F;
         float logoW = logoFont.getStringWidth("A");
         logoFont.drawString(matrix, "A", panelCenterX - logoW / 2.0F, panelCenterY - 3.5F, iconColor);
         userIconFont.drawString(matrix, "B", nameX + innerPadding, this.getY() + 10, iconColor);
         font.drawString(matrix, playerName, nameX + innerPadding + userIconFont.getStringWidth("B") + itemGap, this.getY() + 8.5F, textColor);
         float infoCursor = infoX + innerPadding;
         statIconFont.drawString(matrix, "a", infoCursor, this.getY() + 9.5F, iconColor);
         infoCursor += statIconFont.getStringWidth("a") + itemGap;
         font.drawString(matrix, fps, infoCursor, this.getY() + 8.5F, textColor);
         infoCursor += font.getStringWidth(fps) + separatorGap;
         dotFont.drawString(matrix, ".", infoCursor, this.getY() + 3.5F, textColor);
         infoCursor += dotWidth + separatorGap;
         statIconFont.drawString(matrix, "h", infoCursor, this.getY() + 9.5F, iconColor);
         infoCursor += statIconFont.getStringWidth("h") + itemGap;
         font.drawString(matrix, ping, infoCursor, this.getY() + 8.5F, textColor);
         infoCursor += font.getStringWidth(ping) + separatorGap;
         dotFont.drawString(matrix, ".", infoCursor, this.getY() + 3.5F, textColor);
         infoCursor += dotWidth + separatorGap;
         timeIconFont.drawString(matrix, "n", infoCursor, this.getY() + 9.5F, iconColor);
         infoCursor += timeIconFont.getStringWidth("n") + itemGap;
         font.drawString(matrix, time, infoCursor, this.getY() + 8.5F, textColor);
         serverIconFont.drawString(matrix, serverIcon, serverX + innerPadding, this.getY() + 9.5F, iconColor);
         font.drawString(matrix, server, serverX + innerPadding + serverIconFont.getStringWidth(serverIcon) + itemGap, this.getY() + 8.5F, textColor);
      }
   }

   private String getPing() {
      if (mc.field_1724 != null && mc.method_1562() != null) {
         class_640 entry = mc.method_1562().method_2871(mc.field_1724.method_5667());
         return entry != null ? String.valueOf(entry.method_2959()) : "0";
      } else {
         return "0";
      }
   }

   private String getServerName() {
      if (mc.method_1562() != null && mc.method_1562().method_45734() != null) {
         String address = mc.method_1562().method_45734().field_3761;
         if (address != null && !address.isBlank()) {
            return address.length() > 18 ? address.substring(0, 15) + "..." : address;
         } else {
            return "Unknown";
         }
      } else {
         return "Singleplayer";
      }
   }

   private void drawPanel(class_4587 matrix, float x, float y, float width, float height, float radius) {
      GlassStyle.backdrop(matrix, x, y, width, height, radius);
   }
}
