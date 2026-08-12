package pl.astralvisuals.utils.interactions.interact;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;
import net.minecraft.class_1268;
import net.minecraft.class_1291;
import net.minecraft.class_1297;
import net.minecraft.class_1309;
import net.minecraft.class_1657;
import net.minecraft.class_2246;
import net.minecraft.class_2248;
import net.minecraft.class_2338;
import net.minecraft.class_238;
import net.minecraft.class_243;
import net.minecraft.class_2596;
import net.minecraft.class_266;
import net.minecraft.class_2680;
import net.minecraft.class_2824;
import net.minecraft.class_2848;
import net.minecraft.class_2886;
import net.minecraft.class_304;
import net.minecraft.class_3532;
import net.minecraft.class_4050;
import net.minecraft.class_408;
import net.minecraft.class_437;
import net.minecraft.class_5250;
import net.minecraft.class_6880;
import net.minecraft.class_7204;
import net.minecraft.class_8646;
import net.minecraft.class_9013;
import net.minecraft.class_9025;
import net.minecraft.class_2848.class_2849;
import net.minecraft.class_3675.class_307;
import org.lwjgl.glfw.GLFW;
import pl.astralvisuals.features.module.setting.implement.BindSetting;
import pl.astralvisuals.utils.display.color.ColorAssist;
import pl.astralvisuals.utils.display.interfaces.QuickImports;
import pl.astralvisuals.utils.player.rotation.MathAngle;
import pl.astralvisuals.utils.player.rotation.Turns;

public final class PlayerInteractionHelper implements QuickImports {
   public static void sendSequencedPacket(class_7204 packetCreator) {
      mc.field_1761.method_41931(mc.field_1687, packetCreator);
   }

   public static void interactItem(class_1268 hand) {
      interactItem(hand, MathAngle.cameraAngle());
   }

   public static void interactItem(class_1268 hand, Turns angle) {
      sendSequencedPacket(sequence -> new class_2886(hand, sequence, angle.getYaw(), angle.getPitch()));
   }

   public static void interactEntity(class_1297 entity) {
      mc.field_1724.field_3944.method_52787(class_2824.method_34208(entity, false, class_1268.field_5808, entity.method_5829().method_1005()));
      mc.field_1724.field_3944.method_52787(class_2824.method_34207(entity, false, class_1268.field_5808));
   }

   public static void startFallFlying() {
      mc.field_1724.field_3944.method_52787(new class_2848(mc.field_1724, class_2849.field_12982));
      mc.field_1724.method_23669();
   }

   public static void sendPacketWithOutEvent(class_2596<?> packet) {
      mc.method_1562().method_48296().method_10752(packet, null);
   }

   public static String getHealthString(class_1309 entity) {
      return getHealthString(getHealth(entity));
   }

   public static String getHealthString(float hp) {
      return String.format("%.1f", hp).replace(",", ".").replace(".0", "");
   }

   public static float getHealth(class_1309 entity) {
      float hp = entity.method_6032() + entity.method_6067();
      if (entity instanceof class_1657 player) {
         class_266 scoreBoard = player.method_7327().method_1189(class_8646.field_45158);
         if (scoreBoard != null) {
            class_5250 text = class_9013.method_55398(player.method_7327().method_55430(player, scoreBoard), scoreBoard.method_55380(class_9025.field_47566));

            try {
               hp = Float.parseFloat(ColorAssist.removeFormatting(text.getString()));
            } catch (NumberFormatException var6) {
            }
         }
      }

      return class_3532.method_15363(hp, 0.0F, entity.method_6063());
   }

   public static void jump() {
      if (mc.field_1724.method_5624()) {
         float yaw = mc.field_1724.method_36454() * (float) (Math.PI / 180.0);
         mc.field_1724.method_45319(new class_243(-class_3532.method_15374(yaw) * 0.2F, 0.0, class_3532.method_15362(yaw) * 0.2F));
      }

      mc.field_1724.field_6007 = true;
   }

   public static List<class_2338> getCube(class_2338 center, float radius) {
      return getCube(center, radius, radius, true);
   }

   public static List<class_2338> getCube(class_2338 center, float radiusXZ, float radiusY) {
      return getCube(center, radiusXZ, radiusY, true);
   }

   public static List<class_2338> getCube(class_2338 center, float radiusXZ, float radiusY, boolean down) {
      List<class_2338> positions = new ArrayList<>();
      int centerX = center.method_10263();
      int centerY = center.method_10264();
      int centerZ = center.method_10260();
      int posY = down ? centerY - (int)radiusY : centerY;

      for (int x = centerX - (int)radiusXZ; x <= centerX + radiusXZ; x++) {
         for (int z = centerZ - (int)radiusXZ; z <= centerZ + radiusXZ; z++) {
            for (int y = posY; y <= centerY + radiusY; y++) {
               positions.add(new class_2338(x, y, z));
            }
         }
      }

      return positions;
   }

   public static List<class_2338> getCube(class_2338 start, class_2338 end) {
      List<class_2338> positions = new ArrayList<>();

      for (int x = start.method_10263(); x <= end.method_10263(); x++) {
         for (int z = start.method_10260(); z <= end.method_10260(); z++) {
            for (int y = start.method_10264(); y <= end.method_10264(); y++) {
               positions.add(new class_2338(x, y, z));
            }
         }
      }

      return positions;
   }

   public static class_307 getKeyType(int key) {
      return key < 8 ? class_307.field_1672 : class_307.field_1668;
   }

   public static Stream<class_1297> streamEntities() {
      return StreamSupport.stream(mc.field_1687.method_18112().spliterator(), false);
   }

   public static boolean canChangeIntoPose(class_4050 pose, class_243 pos) {
      return mc.field_1724.method_37908().method_8587(mc.field_1724, mc.field_1724.method_18377(pose).method_30757(pos).method_1011(1.0E-7));
   }

   public static boolean isPotionActive(class_6880<class_1291> statusEffect) {
      return mc.field_1724.method_6088().containsKey(statusEffect);
   }

   public static boolean isPlayerInBlock(class_2248 block) {
      return isBoxInBlock(mc.field_1724.method_5829().method_1014(-0.001), block);
   }

   public static boolean isBoxInBlock(class_238 box, class_2248 block) {
      return isBox(box, pos -> mc.field_1687.method_8320(pos).method_26204().equals(block));
   }

   public static boolean isBoxInBlocks(class_238 box, List<class_2248> blocks) {
      return isBox(box, pos -> blocks.contains(mc.field_1687.method_8320(pos).method_26204()));
   }

   public static boolean isBox(class_238 box, Predicate<class_2338> pos) {
      return class_2338.method_29715(box).anyMatch(pos);
   }

   public static boolean isKey(BindSetting setting) {
      int key = setting.getKey();
      return mc.field_1755 == null && setting.isVisible() && isKey(getKeyType(key), key);
   }

   public static boolean isKey(class_304 key) {
      return isKey(key.method_1429().method_1442(), key.method_1429().method_1444());
   }

   public static boolean isKey(class_307 type, int keyCode) {
      if (keyCode != -1) {
         switch (type) {
            case field_1668:
               return GLFW.glfwGetKey(mc.method_22683().method_4490(), keyCode) == 1;
            case field_1672:
               return GLFW.glfwGetMouseButton(mc.method_22683().method_4490(), keyCode) == 1;
            default:
               return false;
         }
      } else {
         return false;
      }
   }

   public static boolean isAir(class_2338 blockPos) {
      return isAir(mc.field_1687.method_8320(blockPos));
   }

   public static boolean isAir(class_2680 state) {
      return state.method_26215() || state.method_26204().equals(class_2246.field_10543) || state.method_26204().equals(class_2246.field_10243);
   }

   public static boolean isChat(class_437 screen) {
      return screen instanceof class_408;
   }

   public static boolean nullCheck() {
      return mc.field_1724 == null || mc.field_1687 == null;
   }

   private PlayerInteractionHelper() {
      throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
   }
}
