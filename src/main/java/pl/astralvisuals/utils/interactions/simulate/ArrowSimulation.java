package pl.astralvisuals.utils.interactions.simulate;

import net.minecraft.class_1667;
import net.minecraft.class_1675;
import net.minecraft.class_1799;
import net.minecraft.class_1802;
import net.minecraft.class_1937;
import net.minecraft.class_238;
import net.minecraft.class_239;
import net.minecraft.class_243;
import net.minecraft.class_3959;
import net.minecraft.class_638;
import net.minecraft.class_239.class_240;
import net.minecraft.class_3959.class_242;
import net.minecraft.class_3959.class_3960;
import pl.astralvisuals.utils.display.interfaces.QuickImports;

public class ArrowSimulation implements QuickImports {
   public final class_638 world;
   public class_243 pos;
   public class_243 velocity;
   public final boolean collideEntities;
   public boolean inGround = false;

   public ArrowSimulation(class_638 world, class_243 pos, class_243 velocity) {
      this(world, pos, velocity, true);
   }

   public ArrowSimulation(class_638 world, class_243 pos, class_243 velocity, boolean collideEntities) {
      this.world = world;
      this.pos = pos;
      this.velocity = velocity;
      this.collideEntities = collideEntities;
   }

   public class_239 tick() {
      if (this.inGround) {
         return null;
      } else {
         class_243 newPos = this.pos.method_1019(this.velocity);
         double drag = this.isTouchingWater() ? 0.6 : 0.99;
         this.velocity = this.velocity.method_1021(drag);
         this.velocity = new class_243(this.velocity.field_1352, this.velocity.field_1351 - 0.05F, this.velocity.field_1350);
         class_239 hitResult = this.updateCollision(this.pos, newPos);
         if (hitResult != null) {
            this.pos = hitResult.method_17784();
            this.inGround = true;
            return hitResult;
         } else {
            this.pos = newPos;
            return null;
         }
      }
   }

   private class_239 updateCollision(class_243 pos, class_243 newPos) {
      class_1937 world = this.world;
      class_1667 arrowEntity = new class_1667(
         this.world, this.pos.field_1352, this.pos.field_1351, this.pos.field_1350, new class_1799(class_1802.field_8107), null
      );
      class_239 blockHitResult = world.method_17742(new class_3959(pos, newPos, class_3960.field_17558, class_242.field_1348, arrowEntity));
      if (this.collideEntities) {
         double size = 0.45;
         class_239 entityHitResult = class_1675.method_18077(
            this.world,
            arrowEntity,
            pos,
            newPos,
            new class_238(-size, -size, -size, size, size, size).method_997(pos).method_18804(newPos.method_1020(pos)).method_1014(1.0),
            entity -> !entity.method_7325() && entity.method_5805() && (entity.method_5863() || entity != mc.field_1724 && entity == arrowEntity)
               ? !arrowEntity.method_5794(entity)
               : false
         );
         if (entityHitResult != null && entityHitResult.method_17783() != class_240.field_1333) {
            return entityHitResult;
         }
      }

      return blockHitResult != null && blockHitResult.method_17783() != class_240.field_1333 ? blockHitResult : null;
   }

   private boolean isTouchingWater() {
      return false;
   }
}
