package pl.astralvisuals.utils.client.managers.event.impl;

import net.minecraft.class_1297;

public class EventAttack extends EventLayer {
   private class_1297 entity;
   boolean pre;

   public EventAttack(class_1297 entity, boolean pre) {
      this.entity = entity;
      this.pre = pre;
   }

   public class_1297 getEntity() {
      return this.entity;
   }

   public boolean isPre() {
      return this.pre;
   }
}
