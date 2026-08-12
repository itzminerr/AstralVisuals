package pl.astralvisuals.features.impl.render;

import pl.astralvisuals.events.render.AspectRatioEvent;
import pl.astralvisuals.features.module.Module;
import pl.astralvisuals.features.module.ModuleCategory;
import pl.astralvisuals.features.module.setting.implement.SliderSettings;
import pl.astralvisuals.utils.client.managers.event.EventHandler;

public class AspectRatio extends Module {
   private final SliderSettings ratioSetting = new SliderSettings("Соотношение", "Соотношение сторон экрана").setValue(1.0F).range(0.1F, 2.0F);

   public AspectRatio() {
      super("AspectRatio", "Aspect Ratio", ModuleCategory.RENDER);
      this.setup(this.ratioSetting);
   }

   @EventHandler
   public void onAspectRatio(AspectRatioEvent e) {
      e.setRatio(this.ratioSetting.getValue());
      e.cancel();
   }
}
