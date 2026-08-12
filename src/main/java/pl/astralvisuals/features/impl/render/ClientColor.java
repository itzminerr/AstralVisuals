package pl.astralvisuals.features.impl.render;

import java.awt.Color;
import pl.astralvisuals.features.module.Module;
import pl.astralvisuals.features.module.ModuleCategory;
import pl.astralvisuals.features.module.setting.implement.ColorSetting;
import pl.astralvisuals.features.module.setting.implement.SelectSetting;
import pl.astralvisuals.features.module.setting.implement.SliderSettings;
import pl.astralvisuals.utils.client.Instance;
import pl.astralvisuals.utils.display.color.ColorAssist;

// Основной цвет клиента. Задаёт акцент GUI (неоновые кнопки) и используется любыми
// настройками цвета с включённой "Синхронизацией". Режимы: статичный цвет, градиент из 2
// или из 3 цветов (анимированный перелив по времени).
public class ClientColor extends Module {
   public static final String STATIC = "Статичный";
   public static final String GRADIENT2 = "Градиент из 2";
   public static final String GRADIENT3 = "Градиент из 3";

   private final SelectSetting mode = new SelectSetting("Режим", "Тип основного цвета")
      .value(STATIC, GRADIENT2, GRADIENT3)
      .selected(GRADIENT2);
   private final ColorSetting color1 = new ColorSetting("Цвет 1", "Первый цвет").setColor(new Color(110, 139, 255).getRGB());
   private final ColorSetting color2 = new ColorSetting("Цвет 2", "Второй цвет").setColor(new Color(179, 110, 255).getRGB())
      .visible(() -> !this.mode.isSelected(STATIC));
   private final ColorSetting color3 = new ColorSetting("Цвет 3", "Третий цвет").setColor(new Color(110, 230, 255).getRGB())
      .visible(() -> this.mode.isSelected(GRADIENT3));
   private final SliderSettings speed = new SliderSettings("Скорость", "Скорость перелива градиента (сек.)")
      .setValue(4.0F)
      .range(1, 20)
      .visible(() -> !this.mode.isSelected(STATIC));

   public static ClientColor getInstance() {
      return Instance.get(ClientColor.class);
   }

   public ClientColor() {
      super("ClientColor", "Client Color", ModuleCategory.RENDER);
      this.setup(this.mode, this.color1, this.color2, this.color3, this.speed);
   }

   // Опорные цвета градиента (raw — без синхронизации, чтобы не было рекурсии).
   private int[] stops() {
      if (this.mode.isSelected(STATIC)) {
         return new int[]{this.color1.rawColor()};
      } else if (this.mode.isSelected(GRADIENT3)) {
         return new int[]{this.color1.rawColor(), this.color2.rawColor(), this.color3.rawColor()};
      } else {
         return new int[]{this.color1.rawColor(), this.color2.rawColor()};
      }
   }

   // «Живой» цвет в точке фазы (0..1) цикла. Чем выше «Скорость», тем быстрее перелив.
   // phaseOffset сдвигает выборку, чтобы start/mid/end давали текущий проточный градиент.
   public int colorAt(float phaseOffset) {
      int[] stops = this.stops();
      if (stops.length == 1) {
         return stops[0];
      }

      // секунд на один сегмент: speed=1 -> 6с (медленно), speed=20 -> 0.3с (быстро).
      double secondsPerSegment = 6.0 / Math.max(1.0F, this.speed.getValue());
      double cycleMs = secondsPerSegment * 1000.0 * stops.length;
      double t = System.currentTimeMillis() % (long)cycleMs / cycleMs * stops.length;
      t = (t + phaseOffset * stops.length) % stops.length;
      int i = (int)t % stops.length;
      int j = (i + 1) % stops.length;
      float f = (float)(t - Math.floor(t));
      return ColorAssist.interpolateColor(stops[i], stops[j], f);
   }

   // Текущий «живой» цвет (для синхронизации настроек цвета).
   public int getColor() {
      return this.colorAt(0.0F);
   }

   // Для GUI-градиента: start/mid/end — это анимированные точки одного перелива (видно скорость).
   public int getStart() {
      return this.colorAt(0.0F);
   }

   public int getMid() {
      return this.colorAt(0.15F);
   }

   public int getEnd() {
      return this.colorAt(0.3F);
   }
}
