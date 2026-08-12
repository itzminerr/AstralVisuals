package pl.astralvisuals.features.impl.render;

import net.minecraft.class_310;
import net.minecraft.class_742;
import pl.astralvisuals.common.repository.friend.FriendUtils;
import pl.astralvisuals.features.module.Module;
import pl.astralvisuals.features.module.ModuleCategory;
import pl.astralvisuals.features.module.setting.implement.BooleanSetting;
import pl.astralvisuals.features.module.setting.implement.SelectSetting;
import pl.astralvisuals.utils.client.Instance;

public class CustomModels extends Module {
   public static final String RABBIT = "Crazy Rabbit";
   public static final String FREDDY = "Freddy Bear";
   public static final String TUNG = "тунг тунг сахур";

   private final SelectSetting models = new SelectSetting("Моделька", "Кастомная модель игрока").value(RABBIT, FREDDY, TUNG).selected(RABBIT);
   private final BooleanSetting friends = new BooleanSetting("Друзья", "Применять модель и к друзьям").setValue(true);

   public static CustomModels getInstance() {
      return Instance.get(CustomModels.class);
   }

   public CustomModels() {
      super("CustomModels", "Custom Models", ModuleCategory.RENDER);
      this.setup(this.models, this.friends);
   }

   public String getModelName() {
      return this.models.getSelected();
   }

   public boolean shouldApplyTo(class_742 player) {
      if (!this.isState() || player == null || player.method_7325()) {
         return false;
      }

      class_310 client = class_310.method_1551();
      if (client.field_1724 != null && player == client.field_1724) {
         return true;
      }

      return this.friends.isValue() && FriendUtils.isFriend(player);
   }
}
