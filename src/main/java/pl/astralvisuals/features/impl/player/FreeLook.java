package pl.astralvisuals.features.impl.player;

import net.minecraft.class_3532;
import net.minecraft.class_5498;
import pl.astralvisuals.events.keyboard.KeyEvent;
import pl.astralvisuals.events.keyboard.MouseRotationEvent;
import pl.astralvisuals.events.render.CameraEvent;
import pl.astralvisuals.events.render.FovEvent;
import pl.astralvisuals.features.module.Module;
import pl.astralvisuals.features.module.ModuleCategory;
import pl.astralvisuals.features.module.setting.implement.BindSetting;
import pl.astralvisuals.utils.client.managers.event.EventHandler;
import pl.astralvisuals.utils.interactions.interact.PlayerInteractionHelper;
import pl.astralvisuals.utils.player.rotation.MathAngle;
import pl.astralvisuals.utils.player.rotation.Turns;

public class FreeLook extends Module {
   private class_5498 perspective;
   private Turns angle;
   public static BindSetting freeLookSetting = new BindSetting("Свободный обзор", "Клавиша свободного обзора");

   public FreeLook() {
      super("FreeLook", "Free Look", ModuleCategory.PLAYER);
      this.setup(freeLookSetting);
   }

   @EventHandler
   public void onKey(KeyEvent e) {
      if (e.isKeyDown(freeLookSetting.getKey())) {
         this.perspective = mc.field_1690.method_31044();
         if (this.angle == null) {
            this.angle = MathAngle.cameraAngle();
         }
      }
   }

   @EventHandler
   public void onFov(FovEvent e) {
      if (PlayerInteractionHelper.isKey(freeLookSetting)) {
         if (mc.field_1690.method_31044().method_31034()) {
            mc.field_1690.method_31043(class_5498.field_26665);
         }

         if (this.angle == null) {
            this.angle = MathAngle.cameraAngle();
         }
      } else if (this.perspective != null) {
         mc.field_1690.method_31043(this.perspective);
         this.perspective = null;
         this.angle = null;
      }
   }

   @EventHandler
   public void onMouseRotation(MouseRotationEvent e) {
      if (PlayerInteractionHelper.isKey(freeLookSetting)) {
         if (this.angle == null) {
            this.angle = MathAngle.cameraAngle();
         }

         this.angle.setYaw(this.angle.getYaw() + e.getCursorDeltaX() * 0.15F);
         this.angle.setPitch(class_3532.method_15363(this.angle.getPitch() + e.getCursorDeltaY() * 0.15F, -90.0F, 90.0F));
         e.cancel();
      } else {
         this.angle = null;
      }
   }

   @EventHandler
   public void onCamera(CameraEvent e) {
      if (PlayerInteractionHelper.isKey(freeLookSetting) && this.angle != null) {
         e.setAngle(this.angle);
         e.cancel();
      }
   }
}
