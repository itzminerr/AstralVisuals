package pl.astralvisuals.features.impl.render;

import java.awt.Color;
import pl.astralvisuals.features.module.Module;
import pl.astralvisuals.features.module.ModuleCategory;
import pl.astralvisuals.features.module.setting.implement.BooleanSetting;
import pl.astralvisuals.features.module.setting.implement.ColorSetting;
import pl.astralvisuals.utils.client.Instance;

public class CustomHitbox extends Module {
   private final ColorSetting colorSetting = new ColorSetting("Цвет", "Цвет хитбокса")
      .setColor(-1)
      .presets(-1, -65536, -23178, -16711936, -16776961, -7722014);
   private final BooleanSetting sightLineSetting = new BooleanSetting("Убрать линию зрения", "Скрыть синюю линию направления взгляда").setValue(true);
   private final BooleanSetting eyeBoxSetting = new BooleanSetting("Убрать квадрат зрения", "Скрыть красный квадрат на уровне глаз").setValue(true);
   private final BooleanSetting onlyPlayersSetting = new BooleanSetting("Только игроки", "Показывать хитбоксы только у игроков").setValue(false);

   private final BooleanSetting friendColorEnabled = new BooleanSetting("Цвет друзей", "Отдельный цвет хитбокса для друзей").setValue(false);
   private final ColorSetting friendColorSetting = new ColorSetting("Цвет друга", "Цвет хитбокса у друзей")
      .setColor(new Color(80, 220, 120).getRGB())
      .presets(new Color(80, 220, 120).getRGB(), -16711936, -16776961, -1, -23178)
      .visible(this.friendColorEnabled::isValue);

   public static CustomHitbox getInstance() {
      return Instance.get(CustomHitbox.class);
   }

   public CustomHitbox() {
      super("CustomHitbox", "Custom Hitbox", ModuleCategory.RENDER);
      this.setup(this.colorSetting, this.sightLineSetting, this.eyeBoxSetting, this.onlyPlayersSetting,
         this.friendColorEnabled, this.friendColorSetting);
   }

   public boolean onlyPlayers() {
      return this.onlyPlayersSetting.isValue();
   }

   public boolean useFriendColor() {
      return this.friendColorEnabled.isValue();
   }

   // ARGB-цвет хитбокса друга.
   public int getFriendColor() {
      return this.friendColorSetting.getColor();
   }

   // ARGB-цвет хитбокса (альфа = прозрачность контура)
   public int getColor() {
      return this.colorSetting.getColor();
   }

   public boolean hideSightLine() {
      return this.sightLineSetting.isValue();
   }

   public boolean hideEyeBox() {
      return this.eyeBoxSetting.isValue();
   }
}
