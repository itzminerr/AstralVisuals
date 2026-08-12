package pl.astralvisuals.common.animation.implement;

import pl.astralvisuals.common.animation.Animation;

public class Decelerate extends Animation {
   @Override
   public double calculation(double value) {
      double x = value / this.ms;
      return 1.0 - (x - 1.0) * (x - 1.0);
   }
}
