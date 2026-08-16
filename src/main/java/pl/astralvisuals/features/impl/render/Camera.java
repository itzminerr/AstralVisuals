package pl.astralvisuals.features.impl.render;

import net.minecraft.class_3532;
import pl.astralvisuals.events.keyboard.HotBarScrollEvent;
import pl.astralvisuals.events.keyboard.KeyEvent;
import pl.astralvisuals.events.render.CameraEvent;
import pl.astralvisuals.events.render.FovEvent;
import pl.astralvisuals.features.impl.player.FreeLook;
import pl.astralvisuals.features.module.Module;
import pl.astralvisuals.features.module.ModuleCategory;
import pl.astralvisuals.features.module.setting.implement.BindSetting;
import pl.astralvisuals.features.module.setting.implement.SliderSettings;
import pl.astralvisuals.utils.client.Instance;
import pl.astralvisuals.utils.client.managers.event.EventHandler;
import pl.astralvisuals.utils.interactions.interact.PlayerInteractionHelper;
import pl.astralvisuals.utils.math.calc.Calculate;
import pl.astralvisuals.utils.player.rotation.MathAngle;

public class Camera extends Module {
   private float fov = 110.0F;
   private float smoothFov = 30.0F;
   private float lastChangedFov = 30.0F;
   private SliderSettings distanceSetting = new SliderSettings("Дистанция камеры", "Дистанция камеры от третьего лица").setValue(3.0F).range(2.0F, 5.0F);
   private BindSetting zoomSetting = new BindSetting("Зум", "Клавиша приближения камеры");
   private SliderSettings zoomStrengthSetting = new SliderSettings("Сила зума", "Кратность приближения").setValue(3.0F).range(1.5F, 10.0F);

   public Camera() {
      super("Camera", "Camera", ModuleCategory.RENDER);
      this.setup(this.distanceSetting, this.zoomSetting, this.zoomStrengthSetting);
   }

   @EventHandler
   public void onKey(KeyEvent e) {
      if (e.isKeyDown(this.zoomSetting.getKey())) {
         float baseFov = ((Integer)mc.field_1690.method_41808().method_41753()).floatValue();
         this.fov = Math.min(this.lastChangedFov, baseFov / this.zoomStrengthSetting.getValue());
      }

      if (e.isKeyReleased(this.zoomSetting.getKey(), true)) {
         this.lastChangedFov = this.fov;
         this.fov = ((Integer)mc.field_1690.method_41808().method_41753()).intValue();
      }
   }

   @EventHandler
   public void onHotBarScroll(HotBarScrollEvent e) {
      if (PlayerInteractionHelper.isKey(this.zoomSetting)) {
         this.fov = (int)class_3532.method_15350(this.fov - e.getVertical() * 10.0, 10.0, ((Integer)mc.field_1690.method_41808().method_41753()).intValue());
         e.cancel();
      }
   }

   @EventHandler
   public void onFov(FovEvent e) {
      e.setFov(
         (int)class_3532.method_15363(
            (this.smoothFov = Calculate.interpolateSmooth(1.6, this.smoothFov, this.fov)) + 1.0F,
            10.0F,
            ((Integer)mc.field_1690.method_41808().method_41753()).intValue()
         )
      );
      e.cancel();
   }

   @EventHandler
   public void onCamera(CameraEvent e) {
      e.setCameraClip(false);
      e.setDistance(this.distanceSetting.getValue());
      FreeLook freeLook = Instance.get(FreeLook.class);
      if (!freeLook.isState() || !freeLook.isHeld()) {
         e.setAngle(MathAngle.cameraAngle());
      }

      e.cancel();
   }
}
