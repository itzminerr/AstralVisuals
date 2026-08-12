package pl.astralvisuals.features.impl.movement;

import pl.astralvisuals.events.player.TickEvent;
import pl.astralvisuals.features.module.Module;
import pl.astralvisuals.features.module.ModuleCategory;
import pl.astralvisuals.features.module.setting.implement.SelectSetting;
import pl.astralvisuals.features.module.setting.implement.SliderSettings;
import pl.astralvisuals.mixins.client.IMinecraftClient;
import pl.astralvisuals.utils.client.Instance;
import pl.astralvisuals.utils.client.managers.event.EventHandler;

// "Залипание мыши" (порт логики Pulse): каждые N тиков вызывает doAttack()/doItemUse()
// напрямую, поэтому корректно бьёт по цели под прицелом. Раньше держал клавишу — отсюда инверсия.
public class TapeMouse extends Module {
   public static final String ATTACK = "Атака";
   public static final String USE = "Использование";

   private final SelectSetting buttonSetting = new SelectSetting("Кнопка", "Какое действие повторять")
      .value(ATTACK, USE)
      .selected(ATTACK);
   private final SliderSettings delaySetting = new SliderSettings("Задержка", "Пауза между действиями (тики)")
      .setValue(4.0F)
      .range(0, 20);

   private int cooldown;

   public static TapeMouse getInstance() {
      return Instance.get(TapeMouse.class);
   }

   public TapeMouse() {
      super("TapeMouse", "Tape Mouse", ModuleCategory.PLAYER);
      this.setup(this.buttonSetting, this.delaySetting);
   }

   @EventHandler
   public void onTick(TickEvent e) {
      if (mc.field_1724 == null || mc.field_1687 == null || mc.field_1724.method_6115()) {
         return;
      }

      if (this.cooldown <= 0) {
         if (this.buttonSetting.isSelected(USE)) {
            ((IMinecraftClient)(Object)mc).astral$doItemUse();
         } else {
            ((IMinecraftClient)(Object)mc).astral$doAttack();
         }

         this.cooldown = Math.round(this.delaySetting.getValue());
      } else {
         this.cooldown--;
      }
   }

   @Override
   public void activate() {
      this.cooldown = 0;
   }
}
