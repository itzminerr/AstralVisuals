package pl.astralvisuals.display.hud;

import java.awt.Color;
import net.minecraft.class_1937;
import net.minecraft.class_332;
import net.minecraft.class_4587;
import org.joml.Vector4f;
import pl.astralvisuals.utils.client.managers.api.draggable.AbstractDraggable;
import pl.astralvisuals.utils.display.color.ColorAssist;
import pl.astralvisuals.utils.display.font.FontRenderer;
import pl.astralvisuals.utils.display.font.Fonts;
import pl.astralvisuals.utils.display.style.GlassStyle;
import pl.astralvisuals.utils.interactions.interact.PlayerInteractionHelper;

// Компактный HUD с координатами, направлением и парными координатами (оверworld ↔ nether).
// Использует только стабильные методы доступа (позиция double + yaw), подтверждённые в проекте.
public class Coordinates extends AbstractDraggable {
   private static final String[] DIRECTIONS = new String[]{"Юг", "Юго-Запад", "Запад", "Северо-Запад", "Север", "Северо-Восток", "Восток", "Юго-Восток"};

   public Coordinates() {
      super("Координаты", 10, 110, 130, 68, true);
   }

   @Override
   public boolean visible() {
      return mc.field_1724 != null || PlayerInteractionHelper.isChat(mc.field_1755);
   }

   @Override
   public void drawDraggable(class_332 context) {
      class_4587 matrix = context.method_51448();
      FontRenderer font = Fonts.getSize(13, Fonts.Type.DEFAULT);
      FontRenderer icon = Fonts.getSize(17, Fonts.Type.ICONS);
      FontRenderer valueFont = Fonts.getSize(13, Fonts.Type.SEMI);
      int x = this.getX();
      int y = this.getY();

      // Образец данных для предпросмотра в редакторе HUD (когда открыт чат и нет игрока).
      int playerX;
      int playerY;
      int playerZ;
      float yaw;
      if (mc.field_1724 != null) {
         // method_23317/23318/23321 = getX/getY/getZ (double) — подтверждено в FakePlayer/Particles.
         playerX = (int)Math.floor(mc.field_1724.method_23317());
         playerY = (int)Math.floor(mc.field_1724.method_23318());
         playerZ = (int)Math.floor(mc.field_1724.method_23321());
         // method_36454 = getYaw (подтверждено в MathAngle).
         yaw = mc.field_1724.method_36454();
      } else {
         playerX = 123; playerY = 64; playerZ = -456; yaw = 90.0F;
      }

      int facingIndex = (int)Math.floor(((double)(yaw % 360.0F) + 360.0) / 45.0) & 7;
      String direction = DIRECTIONS[facingIndex];
      String facing = direction + " (" + Math.round(Math.floor(yaw % 360.0F)) + "°)";
      String coords = "XYZ: " + playerX + ", " + playerY + ", " + playerZ;
      // Парные координаты зависят от текущего измерения: в «своём» измерении показываем
      // реальные координаты, в «чужом» — пересчёт (незер = оверворлд / 8, оверворлд = незер * 8).
      boolean inNether = mc.field_1687 != null && mc.field_1687.method_27983() == class_1937.field_25180;
      String nether;
      String overworld;
      if (inNether) {
         nether = "Незер: " + playerX + ", " + playerZ;
         overworld = "Оверворлд: " + playerX * 8 + ", " + playerZ * 8;
      } else {
         nether = "Незер: " + playerX / 8 + ", " + playerZ / 8;
         overworld = "Оверворлд: " + playerX + ", " + playerZ;
      }

      // Подбираем ширину по самому широкому тексту.
      float coordsW = valueFont.getStringWidth(coords);
      float facingW = valueFont.getStringWidth(facing);
      float netherW = valueFont.getStringWidth(nether);
      float overworldW = valueFont.getStringWidth(overworld);
      float maxWidth = Math.max(Math.max(Math.max(coordsW, facingW), netherW), overworldW);
      float width = maxWidth + 32.0F;
      float height = 68.0F;
      this.setWidth((int)width);
      this.setHeight((int)height);

      // Шапка с иконкой и названием.
      GlassStyle.backdrop(matrix, (float)x, (float)y, width, 15.5F, new Vector4f(9.0F, 0.0F, 9.0F, 0.0F));
      icon.drawString(matrix, "X", (float)x + 5.0F, (float)y + 6.5F, new Color(225, 225, 255, 255).getRGB());
      font.drawString(matrix, this.getName(), (float)x + 22, (float)y + 6.5F, ColorAssist.getText());

      // Тело с данными.
      GlassStyle.backdrop(matrix, (float)x, (float)y + 16.5F, width, height - 17.0F, new Vector4f(0.0F, 9.0F, 0.0F, 9.0F));
      int accent = GlassStyle.accentMid();
      int labelColor = ColorAssist.getText(0.5F);
      int valueColor = ColorAssist.getText();
      float cursorY = (float)y + 21.0F;
      valueFont.drawString(matrix, coords, (float)x + 10.0F, cursorY, valueColor);
      valueFont.drawString(matrix, facing, (float)x + 10.0F, cursorY + 11.0F, accent);
      valueFont.drawString(matrix, nether, (float)x + 10.0F, cursorY + 22.0F, labelColor);
      valueFont.drawString(matrix, overworld, (float)x + 10.0F, cursorY + 33.0F, labelColor);
   }
}
