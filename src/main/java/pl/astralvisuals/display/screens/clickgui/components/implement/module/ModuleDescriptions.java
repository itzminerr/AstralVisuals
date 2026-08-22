package pl.astralvisuals.display.screens.clickgui.components.implement.module;

import pl.astralvisuals.features.module.Module;

public class ModuleDescriptions {
   public static String getDescription(Module module) {
      String var1 = module.getName();

      return switch (var1) {
         case "AutoSprint" -> "Автоматически включает бег при движении вперед";
         case "FreeLook" -> "Позволяет осматриваться без смены направления движения";
         case "FakePlayer" -> "Создает тренировочного фейк-игрока";
         case "ExtraScreenshot" -> "Копирует сделанные скриншоты в буфер обмена";
         case "JumpCircle" -> "Добавляет визуальный круг при прыжке";
         case "BetterMinecraft" -> "Добавляет полезные элементы интерфейса";
         case "AspectRatio" -> "Меняет соотношение сторон экрана";
         case "Interface" -> "Настраивает экранные элементы интерфейса";
         case "Particles" -> "Частицы в мире и при ударе по цели";
         case "MotionBlur" -> "Размытие изображения в движении";
         case "Camera" -> "Настраивает поведение и дистанцию камеры";
         case "SwingAnimation" -> "Меняет анимацию взмаха руки";
         case "ViewModel" -> "Меняет положение предметов в руках";
         case "BlockOverlay" -> "Улучшает отображение выбранного блока";
         case "WorldTweaks" -> "Содержит визуальные улучшения мира";
         case "NoRender" -> "Скрывает лишние визуальные эффекты";
         case "ChinaHat" -> "Рисует цветную шляпу над тобой и друзьями";
         case "TargetESP" -> "Подсвечивает текущую цель в мире";
         case "CrossHair" -> "Позволяет настроить прицел";
         case "CustomHitbox" -> "Меняет цвет хитбокса и убирает линии зрения";
         case "HitColor" -> "Меняет цвет и прозрачность вспышки при ударе";
         case "KillEffect" -> "Добавляет эффект после смерти цели";
         default -> "Описание модуля отсутствует";
      };
   }
}
