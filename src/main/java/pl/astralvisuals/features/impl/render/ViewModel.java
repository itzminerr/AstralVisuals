package pl.astralvisuals.features.impl.render;

import net.minecraft.class_1268;
import net.minecraft.class_1764;
import net.minecraft.class_4587;
import pl.astralvisuals.events.item.HandOffsetEvent;
import pl.astralvisuals.features.module.Module;
import pl.astralvisuals.features.module.ModuleCategory;
import pl.astralvisuals.features.module.setting.implement.SliderSettings;
import pl.astralvisuals.utils.client.Instance;
import pl.astralvisuals.utils.client.managers.event.EventHandler;

public class ViewModel extends Module {
   private final SliderSettings mainHandXSetting = new SliderSettings("Основная рука X", "Смещение основной руки по X").setValue(0.0F).range(-1.0F, 1.0F);
   private final SliderSettings mainHandYSetting = new SliderSettings("Основная рука Y", "Смещение основной руки по Y").setValue(0.0F).range(-1.0F, 1.0F);
   private final SliderSettings mainHandZSetting = new SliderSettings("Основная рука Z", "Смещение основной руки по Z").setValue(0.0F).range(-2.5F, 2.5F);
   private final SliderSettings mainHandScaleSetting = new SliderSettings("Размер предмета (осн.)", "Размер предмета в основной руке").setValue(1.0F).range(0.1F, 3.0F);
   private final SliderSettings offHandXSetting = new SliderSettings("Вторая рука X", "Смещение второй руки по X").setValue(0.0F).range(-1.0F, 1.0F);
   private final SliderSettings offHandYSetting = new SliderSettings("Вторая рука Y", "Смещение второй руки по Y").setValue(0.0F).range(-1.0F, 1.0F);
   private final SliderSettings offHandZSetting = new SliderSettings("Вторая рука Z", "Смещение второй руки по Z").setValue(0.0F).range(-2.5F, 2.5F);
   private final SliderSettings offHandScaleSetting = new SliderSettings("Размер предмета (вт.)", "Размер предмета во второй руке").setValue(1.0F).range(0.1F, 3.0F);

   public static ViewModel getInstance() {
      return Instance.get(ViewModel.class);
   }

   public ViewModel() {
      super("ViewModel", "View Model", ModuleCategory.RENDER);
      this.setup(
         this.mainHandXSetting, this.mainHandYSetting, this.mainHandZSetting, this.mainHandScaleSetting,
         this.offHandXSetting, this.offHandYSetting, this.offHandZSetting, this.offHandScaleSetting
      );
   }

   // Смещение руки: применяется в самом начале renderFirstPersonItem (до трансформаций ваниллы),
   // поэтому работает как сдвиг всей руки.
   @EventHandler
   public void onHandOffset(HandOffsetEvent e) {
      class_1268 hand = e.getHand();
      if (!hand.equals(class_1268.field_5808) || !(e.getStack().method_7909() instanceof class_1764)) {
         class_4587 matrix = e.getMatrices();
         if (hand.equals(class_1268.field_5808)) {
            matrix.method_46416(this.mainHandXSetting.getValue(), this.mainHandYSetting.getValue(), this.mainHandZSetting.getValue());
         } else {
            matrix.method_46416(this.offHandXSetting.getValue(), this.offHandYSetting.getValue(), this.offHandZSetting.getValue());
         }
      }
   }

   // Размер предмета: вызывается из HeldItemRendererMixin РОВНО перед отрисовкой модели предмета
   // (после всех позиционных трансформаций ваниллы), поэтому меняется только размер, без улёта предмета.
   public void applyItemScale(class_4587 matrices, class_1268 hand) {
      float scale = hand.equals(class_1268.field_5808) ? this.mainHandScaleSetting.getValue() : this.offHandScaleSetting.getValue();
      if (scale != 1.0F) {
         matrices.method_22905(scale, scale, scale);
      }
   }
}
