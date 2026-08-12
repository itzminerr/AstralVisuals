package pl.astralvisuals.events.render;

import pl.astralvisuals.utils.client.managers.event.events.callables.EventCancellable;
import pl.astralvisuals.utils.player.rotation.Turns;

public class CameraEvent extends EventCancellable {
   private boolean cameraClip;
   private float distance;
   private Turns angle;

   public boolean isCameraClip() {
      return this.cameraClip;
   }

   public float getDistance() {
      return this.distance;
   }

   public Turns getAngle() {
      return this.angle;
   }

   public void setCameraClip(boolean cameraClip) {
      this.cameraClip = cameraClip;
   }

   public void setDistance(float distance) {
      this.distance = distance;
   }

   public void setAngle(Turns angle) {
      this.angle = angle;
   }

   public CameraEvent(boolean cameraClip, float distance, Turns angle) {
      this.cameraClip = cameraClip;
      this.distance = distance;
      this.angle = angle;
   }
}
