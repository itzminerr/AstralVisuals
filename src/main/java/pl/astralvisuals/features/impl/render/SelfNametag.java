package pl.astralvisuals.features.impl.render;

import pl.astralvisuals.features.module.Module;
import pl.astralvisuals.features.module.ModuleCategory;
import pl.astralvisuals.utils.client.Instance;

// Показывает собственный никнейм над игроком от третьего лица.
// Логику отрисовки лейбла включает LivingEntityRendererMixin (method_4055 = hasLabel).
public class SelfNametag extends Module {
   public static SelfNametag getInstance() {
      return Instance.get(SelfNametag.class);
   }

   public SelfNametag() {
      super("SelfNametag", "Self Nametag", ModuleCategory.RENDER);
   }
}
