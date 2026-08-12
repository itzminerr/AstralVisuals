package pl.astralvisuals.common.animation.Easy;

public enum Direction {
   FORWARDS,
   BACKWARDS;

   public Direction opposite() {
      return this == FORWARDS ? BACKWARDS : FORWARDS;
   }
}
