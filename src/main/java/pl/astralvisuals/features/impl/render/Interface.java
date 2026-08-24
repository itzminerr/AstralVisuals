package pl.astralvisuals.features.impl.render;

import java.awt.Color;
import pl.astralvisuals.features.module.Module;
import pl.astralvisuals.features.module.ModuleCategory;
import pl.astralvisuals.features.module.setting.implement.BooleanSetting;
import pl.astralvisuals.features.module.setting.implement.ColorSetting;
import pl.astralvisuals.features.module.setting.implement.MultiSelectSetting;
import pl.astralvisuals.features.module.setting.implement.SliderSettings;
import pl.astralvisuals.utils.client.Instance;

public class Interface extends Module {
   public static final String ELEMENT_WATERMARK = "Водяной знак";
   public static final String ELEMENT_HOTKEYS = "Горячие клавиши";
   public static final String ELEMENT_EFFECTS = "Эффекты";
   public static final String ELEMENT_INVENTORY = "Инвентарь";
   public static final String ELEMENT_TARGET_HUD = "Таргет худ";
   public static final String ELEMENT_NOTIFICATIONS = "Уведомления";
   public static final String ELEMENT_COORDINATES = "Координаты";
   public static final String ELEMENT_WAYPOINTS = "Вейпоинты";
   public static final String ELEMENT_MEDIA_PLAYER = "Медиа-плеер";
   public static final String NOTIFICATION_MODULES = "Переключение модулей";

   public final MultiSelectSetting interfaceSettings = new MultiSelectSetting("Элементы", "Элементы интерфейса")
      .value(ELEMENT_WATERMARK, ELEMENT_HOTKEYS, ELEMENT_EFFECTS, ELEMENT_INVENTORY, ELEMENT_TARGET_HUD,
         ELEMENT_NOTIFICATIONS, ELEMENT_COORDINATES, ELEMENT_WAYPOINTS, ELEMENT_MEDIA_PLAYER)
      .selected(ELEMENT_WATERMARK, ELEMENT_HOTKEYS, ELEMENT_EFFECTS, ELEMENT_INVENTORY,
         ELEMENT_NOTIFICATIONS, ELEMENT_COORDINATES, ELEMENT_WAYPOINTS);
   public final MultiSelectSetting notificationSettings = new MultiSelectSetting("Уведомления", "Когда показывать уведомления")
      .value(NOTIFICATION_MODULES)
      .selected(NOTIFICATION_MODULES)
      .visible(() -> this.interfaceSettings.isSelected(ELEMENT_NOTIFICATIONS));
   public final ColorSetting colorSetting = new ColorSetting("Цвет интерфейса", "Основной цвет стекла интерфейса")
      .setColor(new Color(221, 231, 255).getRGB())
      .presets(-2298371, -1, -4934476, -3342337);
   public final BooleanSetting secondColorSetting = new BooleanSetting("Второй цвет", "Включает второй цвет для углового градиента стекла");
   public final ColorSetting secondGlassColor = new ColorSetting("Цвет 2", "Второй цвет градиента стекла")
      .setColor(new Color(255, 50, 150).getRGB())
      .visible(() -> this.secondColorSetting.isValue());
    public final SliderSettings roundScale = new SliderSettings("Скругление углов", "Множитель скругления углов панелей")
       .range(0.0F, 1.0F)
       .setValue(0.5F);
   public final SliderSettings backdropBlur = new SliderSettings("Размытие фона", "Сила размытия мира за стеклом")
      .range(0, 64)
      .setValue(18.0F);
   public final SliderSettings glassDensity = new SliderSettings("Плотность стекла", "Насколько плотно стекло перекрывает фон")
      .range(0.25F, 1.0F)
      .setValue(0.78F);
   public final BooleanSetting glowEnabled = new BooleanSetting("Свечение", "Рисует мягкое свечение вокруг стеклянных панелей");
   public final SliderSettings glowIntensity = new SliderSettings("Яркость свечения", "Яркость свечения панелей")
      .range(0.05F, 1.0F)
      .setValue(0.4F)
      .visible(() -> this.glowEnabled.isValue());
   public final SliderSettings scaleSetting = new SliderSettings("Размер HUD", "Общий масштаб элементов HUD")
      .range(0.5F, 2.0F)
      .setValue(1.0F);
   public final SliderSettings guiScaleSetting = new SliderSettings("Размер GUI", "Общий масштаб интерфейса ClickGUI")
      .range(0.5F, 2.0F)
      .setValue(1.0F);
   public final SliderSettings soundVolumeSetting = new SliderSettings("Громкость звуков", "Громкость звуков переключения модулей")
      .range(0.0F, 1.0F)
      .setValue(1.0F)
      .visible(() -> this.interfaceSettings.isSelected(ELEMENT_NOTIFICATIONS));

   public static Interface getInstance() {
      return Instance.get(Interface.class);
   }

   public Interface() {
      super("Interface", "Interface", ModuleCategory.RENDER);
      this.setup(this.colorSetting, this.secondColorSetting, this.secondGlassColor, this.roundScale,
         this.backdropBlur, this.glassDensity, this.glowEnabled, this.glowIntensity,
         this.scaleSetting, this.guiScaleSetting, this.interfaceSettings, this.notificationSettings, this.soundVolumeSetting);
   }

   public float getRoundScale() {
      return this.roundScale.getValue();
   }

   public float getBackdropBlur() {
      return this.backdropBlur.getValue();
   }

   public float getGlassDensity() {
      return this.glassDensity.getValue();
   }

   public boolean isSecondColor() {
      return this.secondColorSetting.isValue();
   }

   public boolean isGlow() {
      return this.glowEnabled.isValue();
   }

   public float getGlowIntensity() {
      return this.glowIntensity.getValue();
   }

   public float getHudScale() {
      return this.scaleSetting.getValue();
   }

   public float getGuiScale() {
      return this.guiScaleSetting.getValue();
   }

   public float getModuleVolume() {
      return this.soundVolumeSetting.getValue();
   }
}
