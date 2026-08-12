package pl.astralvisuals.display.hud;

import java.awt.Color;
import java.util.Comparator;
import java.util.List;
import net.minecraft.class_2338;
import net.minecraft.class_332;
import net.minecraft.class_4587;
import org.joml.Vector4f;
import pl.astralvisuals.Force;
import pl.astralvisuals.common.repository.way.Way;
import pl.astralvisuals.common.repository.way.WayRepository;
import pl.astralvisuals.utils.client.managers.api.draggable.AbstractDraggable;
import pl.astralvisuals.utils.display.color.ColorAssist;
import pl.astralvisuals.utils.display.font.FontRenderer;
import pl.astralvisuals.utils.display.font.Fonts;
import pl.astralvisuals.utils.display.style.GlassStyle;
import pl.astralvisuals.utils.interactions.interact.PlayerInteractionHelper;

// HUD-навигатор: показывает ближайший вейпоинт текущего сервера (из WayRepository)
// с координатами, расстоянием и стрелкой направления к нему.
public class Waypoints extends AbstractDraggable {
   private static final double MAX_DISTANCE = 1_000_000.0;

   public Waypoints() {
      super("Вейпоинты", 10, 170, 120, 48, true);
   }

   @Override
   public boolean visible() {
      WayRepository repo = this.repo();
      // Виден, если есть хотя бы один вейпоинт на текущем сервере, либо в редакторе HUD.
      boolean has = repo != null && mc.field_1724 != null && this.currentWays(repo).size() > 0;
      return has || PlayerInteractionHelper.isChat(mc.field_1755);
   }

   private WayRepository repo() {
      Force instance = Force.getInstance();
      return instance != null ? instance.getWayRepository() : null;
   }

   // Вейпоинты, привязанные к текущему серверу.
   private List<Way> currentWays(WayRepository repo) {
      String server = repo.getCurrentServerKey();
      return repo.wayList.stream().filter(w -> server.equalsIgnoreCase(w.server())).toList();
   }

   // Ближайший вейпоинт к игроку.
   private Way nearestWay(WayRepository repo) {
      if (mc.field_1724 == null) {
         return null;
      }
      double px = mc.field_1724.method_23317();
      double pz = mc.field_1724.method_23321();
      String server = repo.getCurrentServerKey();
      return repo.wayList
         .stream()
         .filter(w -> server.equalsIgnoreCase(w.server()))
         .min(Comparator.comparingDouble(w -> this.distSq(w, px, pz)))
         .orElse(null);
   }

   private double distSq(Way w, double px, double pz) {
      double dx = w.pos().method_10263() + 0.5 - px;
      double dz = w.pos().method_10260() + 0.5 - pz;
      return dx * dx + dz * dz;
   }

   @Override
   public void drawDraggable(class_332 context) {
      class_4587 matrix = context.method_51448();
      FontRenderer font = Fonts.getSize(13, Fonts.Type.DEFAULT);
      FontRenderer icon = Fonts.getSize(17, Fonts.Type.ICONS);
      FontRenderer valueFont = Fonts.getSize(13, Fonts.Type.SEMI);
      FontRenderer smallFont = Fonts.getSize(11, Fonts.Type.DEFAULT);

      WayRepository repo = this.repo();
      // Образец для предпросмотра в редакторе HUD.
      boolean preview = (repo == null || mc.field_1724 == null) && PlayerInteractionHelper.isChat(mc.field_1755);
      String name;
      String coords;
      String distance;
      String arrow;
      if (repo != null && mc.field_1724 != null) {
         Way w = this.nearestWay(repo);
         if (w != null) {
            name = w.name();
            coords = w.pos().method_10263() + ", " + w.pos().method_10264() + ", " + w.pos().method_10260();
            double px = mc.field_1724.method_23317();
            double py = mc.field_1724.method_23318();
            double pz = mc.field_1724.method_23321();
            double dx = w.pos().method_10263() + 0.5 - px;
            double dy = w.pos().method_10264() + 0.5 - py;
            double dz = w.pos().method_10260() + 0.5 - pz;
            double flatDist = Math.sqrt(dx * dx + dz * dz);
            double fullDist = Math.sqrt(dx * dx + dy * dy + dz * dz);
            distance = (int)Math.round(flatDist) + " м (" + (int)Math.round(fullDist) + " 3D)";
            arrow = this.directionArrow(mc.field_1724.method_36454(), dx, dz);
         } else {
            name = "Нет вейпоинтов"; coords = "—"; distance = "—"; arrow = "";
         }
      } else {
         name = "База"; coords = "120, 64, -450"; distance = "53 м (61 3D)"; arrow = "↑";
      }

      float nameW = valueFont.getStringWidth(name);
      float coordsW = smallFont.getStringWidth(coords);
      float distW = smallFont.getStringWidth(distance);
      float width = Math.max(120.0F, Math.max(Math.max(nameW, coordsW), distW) + 40.0F);
      float height = 48.0F;
      this.setWidth((int)width);
      this.setHeight((int)height);

      int x = this.getX();
      int y = this.getY();

      // Шапка с иконкой и названием.
      GlassStyle.backdrop(matrix, (float)x, (float)y, width, 15.5F, new Vector4f(9.0F, 0.0F, 9.0F, 0.0F));
      icon.drawString(matrix, "X", (float)x + 5.0F, (float)y + 6.5F, new Color(225, 225, 255, 255).getRGB());
      font.drawString(matrix, this.getName(), (float)x + 22, (float)y + 6.5F, ColorAssist.getText());

      // Тело с данными ближайшего вейпоинта.
      GlassStyle.backdrop(matrix, (float)x, (float)y + 16.5F, width, height - 17.0F, new Vector4f(0.0F, 9.0F, 0.0F, 9.0F));
      int accent = GlassStyle.accentMid();
      int labelColor = ColorAssist.getText(0.55F);
      int valueColor = ColorAssist.getText();
      float cursorY = (float)y + 21.0F;

      // Стрелка направления крупно слева, имя вейпоинта справа.
      FontRenderer arrowFont = Fonts.getSize(18, Fonts.Type.BOLD);
      arrowFont.drawString(matrix, arrow, (float)x + 9.0F, cursorY - 1.0F, accent);
      valueFont.drawString(matrix, name, (float)x + 30.0F, cursorY, valueColor);
      smallFont.drawString(matrix, coords, (float)x + 30.0F, cursorY + 11.0F, labelColor);
      smallFont.drawString(matrix, distance, (float)x + 30.0F, cursorY + 22.0F, labelColor);
   }

   // Вычисляет стрелку направления от игрока к вейпоинту в системе обзора игрока:
   // ↑ — цель прямо по курсу, → — справа, ↓ — позади, ← — слева.
   private String directionArrow(float yaw, double dx, double dz) {
      if (dx == 0.0 && dz == 0.0) {
         return "●";
      }
      double rad = Math.toRadians(yaw);
      double sin = Math.sin(rad);
      double cos = Math.cos(rad);
      // Проекция направления на вектор «вперёд» и «вправо» игрока (forward = (-sin, cos)).
      double forward = -dx * sin + dz * cos;
      double right = -dx * cos - dz * sin;
      // Угол по часовой стрелке от «вперёд» (0° = ↑).
      double phi = Math.toDegrees(Math.atan2(right, forward));
      int idx = (int)Math.floor(((phi % 360.0) + 360.0) / 45.0 + 0.5) & 7;
      String[] arrows = {"↑", "↗", "→", "↘", "↓", "↙", "←", "↖"};
      return arrows[idx];
   }
}
