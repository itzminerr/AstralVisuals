package pl.astralvisuals.features.impl.render;

import net.minecraft.class_1268;
import net.minecraft.class_1306;
import net.minecraft.class_3532;
import net.minecraft.class_4587;
import net.minecraft.class_7833;
import pl.astralvisuals.events.item.HandAnimationEvent;
import pl.astralvisuals.events.item.SwingDurationEvent;
import pl.astralvisuals.features.module.Module;
import pl.astralvisuals.features.module.ModuleCategory;
import pl.astralvisuals.features.module.setting.implement.BooleanSetting;
import pl.astralvisuals.features.module.setting.implement.SelectSetting;
import pl.astralvisuals.features.module.setting.implement.SliderSettings;
import pl.astralvisuals.utils.client.managers.event.EventHandler;

public class SwingAnimation extends Module {
   private final SelectSetting swingType = new SelectSetting("Тип взмаха", "Стиль анимации")
      .value("Удар", "Вниз", "Плавная", "Плавная 2", "Сила", "Пир", "Вращение", "Обычная");
   private final SliderSettings hitStrengthSetting = new SliderSettings("Сила взмаха", "Насколько далеко движется рука").setValue(1.0F).range(0.5F, 3.0F);
   private final SliderSettings swingSpeedSetting = new SliderSettings("Длительность взмаха", "Скорость анимации удара").setValue(1.0F).range(0.5F, 4.0F);
   private final BooleanSetting onlySwing = new BooleanSetting("Только при взмахе", "Показывать анимацию только во время взмаха").setValue(false);

   public SwingAnimation() {
      super("SwingAnimation", "Swing Animation", ModuleCategory.RENDER);
      this.setup(this.swingType, this.hitStrengthSetting, this.swingSpeedSetting, this.onlySwing);
   }

   @EventHandler
   public void onSwingDuration(SwingDurationEvent event) {
      event.setAnimation(this.swingSpeedSetting.getValue());
      event.cancel();
   }

   @EventHandler
   public void onHandAnimation(HandAnimationEvent event) {
      if (event.getHand() == class_1268.field_5808) {
         class_4587 matrix = event.getMatrices();
         float swingProgress = event.getSwingProgress();
         int direction = mc.field_1724.method_6068().equals(class_1306.field_6183) ? 1 : -1;
         float sin1 = class_3532.method_15374(swingProgress * swingProgress * (float) Math.PI);
         float sin2 = class_3532.method_15374(class_3532.method_15355(swingProgress) * (float) Math.PI);
         float sinSmooth = (float)(Math.sin(swingProgress * Math.PI) * 0.5);
         float strength = this.hitStrengthSetting.getValue();
         if (this.onlySwing.isValue() && mc.field_1724.field_6279 == 0) {
            matrix.method_46416(direction * 0.56F, -0.52F, -0.72F);
            event.cancel();
         } else {
            String var9 = this.normalizeSwingType(this.swingType.getSelected());
            switch (var9) {
               case "Spin":
                  matrix.method_46416(direction * 0.56F, -0.36F, -0.72F);
                  matrix.method_22907(class_7833.field_40716.rotationDegrees(80 * direction));
                  matrix.method_22907(class_7833.field_40714.rotationDegrees(sin2 * -90.0F * strength));
                  matrix.method_22907(class_7833.field_40718.rotationDegrees((sin1 - sin2) * 60.0F * direction * strength));
                  matrix.method_22907(class_7833.field_40714.rotationDegrees(-30.0F));
                  matrix.method_46416(0.0F, -0.1F, 0.05F);
                  break;
               case "Hit":
                  matrix.method_46416(0.56F * direction, -0.32F, -0.72F);
                  matrix.method_22907(class_7833.field_40716.rotationDegrees(70 * direction));
                  matrix.method_22907(class_7833.field_40718.rotationDegrees(-20 * direction));
                  matrix.method_22907(class_7833.field_40716.rotationDegrees(sin2 * sin1 * -5.0F * strength));
                  matrix.method_22907(class_7833.field_40714.rotationDegrees(sin2 * sin1 * -120.0F * strength));
                  matrix.method_22907(class_7833.field_40714.rotationDegrees(-70.0F));
                  break;
               case "Default":
                  matrix.method_46416(direction * 0.56F, -0.52F - sin2 * 0.5F * strength, -0.72F);
                  matrix.method_22907(class_7833.field_40716.rotationDegrees(45 * direction));
                  matrix.method_22907(class_7833.field_40716.rotationDegrees(-45 * direction));
                  break;
               case "Down":
                  matrix.method_46416(direction * 0.56F, -0.32F, -0.72F);
                  matrix.method_22907(class_7833.field_40716.rotationDegrees(76 * direction));
                  matrix.method_22907(class_7833.field_40716.rotationDegrees(sin2 * -5.0F * strength));
                  matrix.method_22907(class_7833.field_40713.rotationDegrees(sin2 * -100.0F * strength));
                  matrix.method_22907(class_7833.field_40714.rotationDegrees(sin2 * -155.0F * strength));
                  matrix.method_22907(class_7833.field_40714.rotationDegrees(-100.0F));
                  break;
               case "Smooth":
                  matrix.method_46416(direction * 0.56F, -0.42F, -0.72F);
                  matrix.method_22907(class_7833.field_40716.rotationDegrees(direction * (45.0F + sin1 * -20.0F * strength)));
                  matrix.method_22907(class_7833.field_40718.rotationDegrees(direction * sin2 * -20.0F * strength));
                  matrix.method_22907(class_7833.field_40714.rotationDegrees(sin2 * -80.0F * strength));
                  matrix.method_22907(class_7833.field_40716.rotationDegrees(direction * -45.0F));
                  matrix.method_22904(0.0, -0.1, 0.0);
                  break;
               case "Smooth 2":
                  matrix.method_46416(direction * 0.56F, -0.42F, -0.72F);
                  matrix.method_22907(class_7833.field_40714.rotationDegrees(sin2 * -80.0F * strength));
                  matrix.method_22904(0.0, -0.1, 0.0);
                  break;
               case "Power":
                  matrix.method_46416(direction * 0.56F, -0.32F, -0.72F);
                  matrix.method_46416(-sinSmooth * sinSmooth * sin1 * direction * strength, 0.0F, 0.0F);
                  matrix.method_22907(class_7833.field_40716.rotationDegrees(61 * direction));
                  matrix.method_22907(class_7833.field_40718.rotationDegrees(sin2 * strength));
                  matrix.method_22907(class_7833.field_40716.rotationDegrees(sin2 * sin1 * -5.0F * strength));
                  matrix.method_22907(class_7833.field_40714.rotationDegrees(sin2 * sin1 * -30.0F * strength));
                  matrix.method_22907(class_7833.field_40714.rotationDegrees(-60.0F));
                  matrix.method_22907(class_7833.field_40714.rotationDegrees(sinSmooth * -60.0F * strength));
                  break;
               case "Pir":
                  matrix.method_46416(direction * 0.56F, -0.32F, -0.72F);
                  matrix.method_22907(class_7833.field_40716.rotationDegrees(30 * direction));
                  matrix.method_22907(class_7833.field_40716.rotationDegrees(sin2 * 75.0F * direction * strength));
                  matrix.method_22907(class_7833.field_40714.rotationDegrees(sin2 * -45.0F * strength));
                  matrix.method_22907(class_7833.field_40716.rotationDegrees(30 * direction));
                  matrix.method_22907(class_7833.field_40714.rotationDegrees(-80.0F));
                  matrix.method_22907(class_7833.field_40716.rotationDegrees(35 * direction));
            }

            event.cancel();
         }
      }
   }

   private String normalizeSwingType(String selected) {
      return switch (selected) {
         case "Удар" -> "Hit";
         case "Вниз" -> "Down";
         case "Плавная" -> "Smooth";
         case "Плавная 2" -> "Smooth 2";
         case "Сила" -> "Power";
         case "Пир" -> "Pir";
         case "Вращение" -> "Spin";
         case "Обычная" -> "Default";
         default -> selected;
      };
   }
}
