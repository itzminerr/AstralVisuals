package pl.astralvisuals.features.impl.render;

import pl.astralvisuals.features.module.Module;
import pl.astralvisuals.features.module.ModuleCategory;
import pl.astralvisuals.features.module.setting.implement.MultiSelectSetting;
import pl.astralvisuals.utils.client.Instance;

public class NoRender extends Module {
   public static final String FIRE = "Огонь";
   public static final String BAD_EFFECTS = "Негативные эффекты";
   public static final String BLOCK_OVERLAY = "Подсветка блока";
   public static final String DARKNESS = "Темнота";
   public static final String DAMAGE = "Урон";
   public static final String CRIT_PARTICLES = "Партиклы крита";
   public static final String SWEEP_PARTICLES = "Партиклы разящего удара";
   public static final String EXPLOSION_PARTICLES = "Партиклы взрыва кристалла";
   public static final String BLOCK_BREAK_PARTICLES = "Партиклы ломания блоков";
   public static final String EFFECT_PARTICLES = "Партиклы эффектов";
   public static final String WEATHER = "Погода";
   public static final String CLOUDS = "Облака";
   public static final String SCOREBOARD = "Скорборд";
   public static final String BOSSBAR = "Боссбар";
   public static final String PLAYER_GLOW = "Свечение игроков";
   public final MultiSelectSetting modeSetting = new MultiSelectSetting("Элементы", "Эффекты, которые нужно скрывать")
      .value(
         "Огонь",
         "Негативные эффекты",
         "Подсветка блока",
         "Темнота",
         "Урон",
         "Погода",
         "Облака",
         "Скорборд",
         "Боссбар",
         PLAYER_GLOW,
         "Партиклы крита",
         "Партиклы разящего удара",
         "Партиклы взрыва кристалла",
         "Партиклы ломания блоков",
         "Партиклы эффектов"
      )
      .selected("Огонь", "Негативные эффекты", "Подсветка блока", "Темнота", "Урон", "Партиклы крита", "Партиклы разящего удара");

   public static NoRender getInstance() {
      return Instance.get(NoRender.class);
   }

   public NoRender() {
      super("NoRender", "No Render", ModuleCategory.RENDER);
      this.setup(this.modeSetting);
   }
}
