package pl.astralvisuals.features.module;

import net.minecraft.class_124;
import net.minecraft.class_310;
import pl.astralvisuals.Force;
import pl.astralvisuals.common.animation.Animation;
import pl.astralvisuals.common.animation.Direction;
import pl.astralvisuals.common.animation.implement.Decelerate;
import pl.astralvisuals.display.hud.Notifications;
import pl.astralvisuals.features.impl.render.Interface;
import pl.astralvisuals.features.module.setting.SettingRepository;
import pl.astralvisuals.utils.client.managers.event.EventManager;
import pl.astralvisuals.utils.client.sound.SoundManager;
import pl.astralvisuals.utils.display.interfaces.QuickImports;

public class Module extends SettingRepository implements QuickImports {
   private final String name;
   private final String visibleName;
   private final ModuleCategory category;
   private final Animation animation = new Decelerate().setMs(175).setValue(1.0);
   private int key = -1;
   private int type = 1;
   public boolean state;

   public Module(String name, ModuleCategory category) {
      this.name = name;
      this.category = category;
      this.visibleName = name;
   }

   public Module(String name, String visibleName, ModuleCategory category) {
      this.name = name;
      this.visibleName = visibleName;
      this.category = category;
   }

   public void switchState() {
      this.setState(!this.state);
   }

   public void setState(boolean state) {
      this.animation.setDirection(state ? Direction.FORWARDS : Direction.BACKWARDS);
      if (state != this.state) {
         this.state = state;
         this.handleStateChange();
      }
   }

   private void handleStateChange() {
      class_310 mc = class_310.method_1551();
      float volume = Interface.getInstance().getModuleVolume();
      if (mc.field_1724 != null && mc.field_1687 != null) {
         if (this.state) {
            if (Interface.getInstance().notificationSettings.isSelected("Переключение модулей")) {
               Notifications.getInstance().addList("Модуль " + class_124.field_1080 + this.visibleName + class_124.field_1070 + " включен", 4000L, null);
               SoundManager.playSound(SoundManager.ENABLE_MODULE, volume, 1.0F);
            }

            this.activate();
         } else {
            if (Interface.getInstance().notificationSettings.isSelected("Переключение модулей")) {
               Notifications.getInstance().addList("Модуль " + class_124.field_1080 + this.visibleName + class_124.field_1070 + " выключен", 4000L, null);
               SoundManager.playSound(SoundManager.DISABLE_MODULE, volume, 1.0F);
            }

            this.deactivate();
         }
      }

      this.toggleSilent(this.state);
   }

   private void toggleSilent(boolean activate) {
      EventManager eventManager = Force.getInstance().getEventManager();
      if (activate) {
         eventManager.register(this);
      } else {
         eventManager.unregister(this);
      }
   }

   public void activate() {
   }

   public void deactivate() {
   }

   public String getName() {
      return this.name;
   }

   public String getVisibleName() {
      return this.visibleName;
   }

   public ModuleCategory getCategory() {
      return this.category;
   }

   public Animation getAnimation() {
      return this.animation;
   }

   public int getKey() {
      return this.key;
   }

   public int getType() {
      return this.type;
   }

   public boolean isState() {
      return this.state;
   }

   public void setKey(int key) {
      this.key = key;
   }

   public void setType(int type) {
      this.type = type;
   }
}
