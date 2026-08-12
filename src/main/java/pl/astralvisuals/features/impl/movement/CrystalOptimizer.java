package pl.astralvisuals.features.impl.movement;

import net.minecraft.class_1268;
import net.minecraft.class_1293;
import net.minecraft.class_1294;
import net.minecraft.class_1297;
import net.minecraft.class_1304;
import net.minecraft.class_1511;
import net.minecraft.class_1799;
import net.minecraft.class_239;
import net.minecraft.class_243;
import net.minecraft.class_2824;
import net.minecraft.class_3966;
import net.minecraft.class_5134;
import net.minecraft.class_5712;
import net.minecraft.class_746;
import pl.astralvisuals.features.module.Module;
import pl.astralvisuals.features.module.ModuleCategory;
import pl.astralvisuals.utils.client.Instance;

/*
 * Логика портирована из Marlow's Crystal Optimizer (лицензия MIT).
 * Copyright (c) 2025 Marlow and Bram
 * https://github.com/Bram1903/MarlowsCrystalOptimizer
 *
 * Убирает задержку после удара по энд-кристаллу: клиент сразу удаляет сущность
 * кристалла локально, не дожидаясь подтверждения от сервера, и перенаводит прицел.
 */
public class CrystalOptimizer extends Module implements class_2824.class_5908 {
   public static CrystalOptimizer getInstance() {
      return Instance.get(CrystalOptimizer.class);
   }

   public CrystalOptimizer() {
      super("CrystalOptimizer", "Crystal Optimizer", ModuleCategory.PLAYER);
   }

   public void onAttackPacket(class_2824 packet) {
      packet.method_34209(this);
   }

   @Override
   public void method_34218() {
      if (mc.field_1765 instanceof class_3966 entityHitResult && entityHitResult.method_17782() instanceof class_1511 crystal) {
         class_746 player = mc.field_1724;
         if (player != null && this.canDestroyCrystal(player)) {
            this.destroyCrystal(crystal);
            this.retargetCrosshair(crystal);
         }
      }
   }

   @Override
   public void method_34219(class_1268 hand) {
   }

   @Override
   public void method_34220(class_1268 hand, class_243 pos) {
   }

   private void retargetCrosshair(class_1511 crystal) {
      class_746 player = mc.field_1724;
      if (player != null && mc.field_1765 != null && mc.field_1692 == crystal) {
         class_239 retraced = player.method_5745(player.method_55754(), 1.0F, false);
         mc.field_1692 = null;
         mc.field_1765 = retraced;
      }
   }

   private boolean canDestroyCrystal(class_746 player) {
      double damage = player.method_45326(class_5134.field_23721);
      damage += this.getWeaponDamage(player.method_6047());
      class_1293 strength = player.method_6112(class_1294.field_5910);
      if (strength != null) {
         damage += 3.0 * (strength.method_5578() + 1);
      }

      class_1293 weakness = player.method_6112(class_1294.field_5911);
      if (weakness != null) {
         damage -= 4.0 * (weakness.method_5578() + 1);
      }

      return damage > 0.0;
   }

   private double getWeaponDamage(class_1799 item) {
      if (item.method_7960()) {
         return 0.0;
      } else {
         double[] sum = new double[]{0.0};
         item.method_57354(class_1304.field_6173, (attribute, modifier) -> {
            if (class_5134.field_23721.equals(attribute)) {
               sum[0] += modifier.comp_2449();
            }
         });
         return sum[0];
      }
   }

   private void destroyCrystal(class_1297 crystal) {
      crystal.method_5650(class_1297.class_5529.field_26998);
      crystal.method_32876(class_5712.field_37676);
   }
}
