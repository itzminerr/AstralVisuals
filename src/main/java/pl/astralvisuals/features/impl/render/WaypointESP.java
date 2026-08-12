package pl.astralvisuals.features.impl.render;

import java.awt.Color;
import net.minecraft.class_2338;
import net.minecraft.class_238;
import net.minecraft.class_243;
import net.minecraft.class_332;
import net.minecraft.class_4184;
import net.minecraft.class_4587;
import pl.astralvisuals.Force;
import pl.astralvisuals.common.repository.way.Way;
import pl.astralvisuals.common.repository.way.WayRepository;
import pl.astralvisuals.events.render.DrawEvent;
import pl.astralvisuals.events.render.WorldRenderEvent;
import pl.astralvisuals.features.module.Module;
import pl.astralvisuals.features.module.ModuleCategory;
import pl.astralvisuals.features.module.setting.implement.BooleanSetting;
import pl.astralvisuals.features.module.setting.implement.ColorSetting;
import pl.astralvisuals.features.module.setting.implement.SliderSettings;
import pl.astralvisuals.utils.client.Instance;
import pl.astralvisuals.utils.client.managers.event.EventHandler;
import pl.astralvisuals.utils.display.font.FontRenderer;
import pl.astralvisuals.utils.display.font.Fonts;
import pl.astralvisuals.utils.display.geometry.Render3D;
import pl.astralvisuals.utils.math.projection.Projection;

// 3D-визуализация вейпоинтов (.way): из низа мира до самого верха идёт луч в координатах
// вейпоинта, на его высоте — выделенный маркер-куб, а надпись с названием рисуется на HUD-слое
// (через проекцию мир→экран), поэтому всегда видна поверх луча.
public class WaypointESP extends Module {
   private final ColorSetting beamColor = new ColorSetting("Цвет луча", "Цвет вертикального луча")
      .value(new Color(90, 190, 255).getRGB())
      .presets(new Color(90, 190, 255).getRGB(), -1, -65536, -16711936, -23178, -7722014);

   private final SliderSettings beamWidth = new SliderSettings("Толщина луча", "Ширина столба луча")
      .setValue(0.2F).range(0.05F, 0.5F);

   private final ColorSetting markerColor = new ColorSetting("Цвет маркера", "Цвет куба на высоте вейпоинта")
      .value(new Color(255, 205, 70).getRGB())
      .presets(new Color(255, 205, 70).getRGB(), -1, -65536, -16711936, -16776961);

   private final SliderSettings beamDistance = new SliderSettings("Дальность луча", "Луч виден только в пределах этой дистанции (надпись — всегда)")
      .setValue(80.0F).range(8.0F, 512.0F);

   private final BooleanSetting showName = new BooleanSetting("Название", "Показывать надпись с названием").setValue(true);
   private final BooleanSetting showDistance = new BooleanSetting("Расстояние", "Добавлять расстояние к надписи").setValue(true);
   private final SliderSettings textScale = new SliderSettings("Размер текста", "Масштаб надписи на экране").setValue(2.5F).range(1.0F, 5.0F);

   public static WaypointESP getInstance() {
      return Instance.get(WaypointESP.class);
   }

   public WaypointESP() {
      super("WaypointEsp", "Waypoint ESP", ModuleCategory.RENDER);
      this.setup(this.beamColor, this.beamWidth, this.markerColor, this.beamDistance, this.showName, this.showDistance, this.textScale);
   }

   @EventHandler
   public void onWorldRender(WorldRenderEvent event) {
      if (mc.field_1724 == null || mc.field_1687 == null) {
         return;
      }
      WayRepository repo = this.repo();
      if (repo == null) {
         return;
      }

      String server = repo.getCurrentServerKey();
      double bottomY = mc.field_1687.method_31607();
      double topY = mc.field_1687.method_31600() + 1.0;
      int beamC = this.beamColor.getColor();
      int markerC = this.markerColor.getColor();
      double half = this.beamWidth.getValue() / 2.0;
      double maxDist = this.beamDistance.getValue();
      double maxDistSq = maxDist * maxDist;
      double px = mc.field_1724.method_23317();
      double pz = mc.field_1724.method_23321();

      for (Way way : repo.wayList) {
         if (!server.equalsIgnoreCase(way.server())) {
            continue;
         }

         class_2338 pos = way.pos();
         double cx = pos.method_10263() + 0.5;
         double cz = pos.method_10260() + 0.5;

         // Луч виден только вблизи: за пределами дальности рисуем только надпись (в onDraw).
         double dx = cx - px;
         double dz = cz - pz;
         if (dx * dx + dz * dz > maxDistSq) {
            continue;
         }

         // Луч: тонкий столб от низа до верха мира — яркие вертикальные рёбра + лёгкая заливка,
         // depth=false → виден сквозь стены, как маяк-навигатор.
         class_238 beam = new class_238(cx - half, bottomY, cz - half, cx + half, topY, cz + half);
         Render3D.drawBox(beam, beamC, 1.5F, true, true, false);

         // Маркер на высоте вейпоинта: выделенный куб (контур + заливка).
         class_238 marker = new class_238(pos).method_1014(0.06);
         Render3D.drawBox(marker, markerC, 2.0F, true, true, false);
      }
   }

   // Надписи рисуем на HUD-слое — поверх всего мира (луч их больше не перекрывает).
   @EventHandler
   public void onDraw(DrawEvent event) {
      if (!this.showName.isValue() || mc.field_1724 == null || mc.field_1687 == null) {
         return;
      }
      WayRepository repo = this.repo();
      if (repo == null) {
         return;
      }

      String server = repo.getCurrentServerKey();
      class_4587 matrix = event.getDrawContext().method_51448();
      FontRenderer font = Fonts.getSize((int) (15.0F * this.textScale.getValue()), Fonts.Type.SEMI);

      // Вектор направления камеры — для надёжной проверки «перед камерой» (без мигания у плоскости).
      class_4184 camera = mc.field_1773.method_19418();
      class_243 camPos = camera.method_19326();
      double yawR = Math.toRadians(camera.method_19330());
      double pitchR = Math.toRadians(camera.method_19329());
      double cosP = Math.cos(pitchR);
      double fx = -Math.sin(yawR) * cosP;
      double fy = -Math.sin(pitchR);
      double fz = Math.cos(yawR) * cosP;

      for (Way way : repo.wayList) {
         if (!server.equalsIgnoreCase(way.server())) {
            continue;
         }

         class_2338 pos = way.pos();
         double cx = pos.method_10263() + 0.5;
         double cy = pos.method_10264() + 1.2;
         double cz = pos.method_10260() + 0.5;

         // Рисуем только если вейпоинт строго перед камерой (dot > 0). Это убирает мигание,
         // когда стоишь рядом и крутишь камеру — проекция за камерой больше не «прыгает».
         double dot = (cx - camPos.field_1352) * fx + (cy - camPos.field_1351) * fy + (cz - camPos.field_1350) * fz;
         if (dot <= 0.0) {
            continue;
         }

         class_243 screen = Projection.worldSpaceToScreenSpace(new class_243(cx, cy, cz));

         String text = way.name();
         if (this.showDistance.isValue()) {
            double dx = cx - mc.field_1724.method_23317();
            double dy = pos.method_10264() + 0.5 - mc.field_1724.method_23318();
            double dz = cz - mc.field_1724.method_23321();
            int dist = (int) Math.round(Math.sqrt(dx * dx + dy * dy + dz * dz));
            text = text + " (" + dist + " м)";
         }

         font.drawCenteredString(matrix, text, screen.field_1352, screen.field_1351, -1);
      }
   }

   private WayRepository repo() {
      Force force = Force.getInstance();
      return force != null ? force.getWayRepository() : null;
   }
}
