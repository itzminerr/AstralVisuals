package pl.astralvisuals.features.impl.render.custommodels;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Supplier;
import net.minecraft.class_1921;
import net.minecraft.class_2960;
import net.minecraft.class_4587;
import net.minecraft.class_4588;
import net.minecraft.class_4597;
import net.minecraft.class_5603;
import net.minecraft.class_5605;
import net.minecraft.class_5606;
import net.minecraft.class_5609;
import net.minecraft.class_5610;
import net.minecraft.class_5607;
import net.minecraft.class_591;
import net.minecraft.class_10055;
import net.minecraft.class_630;
import pl.astralvisuals.features.impl.render.CustomModels;

/*
 * Портировано из sunshine.recode CustomModels (Yarn / новый render-command API)
 * под Minecraft 1.21.4: модели строятся стандартным ModelData/ModelPartBuilder,
 * а рендер идёт напрямую через ModelPart.render (а не OrderedRenderCommandQueue).
 */
public final class CustomPlayerModelRenderer {
   private static final Map<String, Supplier<CustomModel>> MODEL_FACTORIES = new HashMap<>();
   private static final Map<String, CustomModel> MODEL_CACHE = new HashMap<>();

   static {
      MODEL_FACTORIES.put(CustomModels.RABBIT, CustomPlayerModelRenderer::createRabbit);
      MODEL_FACTORIES.put(CustomModels.FREDDY, CustomPlayerModelRenderer::createFreddy);
      MODEL_FACTORIES.put(CustomModels.TUNG, CustomPlayerModelRenderer::createTung);
   }

   public static boolean render(
      String modelName, class_591 vanillaModel, class_10055 state, class_4587 matrices, class_4597 vertexConsumers, int light, int overlay, int color
   ) {
      CustomModel customModel = getModel(modelName);
      if (customModel == null) {
         return false;
      }

      customModel.copyAngles(vanillaModel, state);
      matrices.method_22903();
      customModel.transform(matrices, state);
      class_4588 buffer = vertexConsumers.getBuffer(class_1921.method_23580(customModel.texture));
      customModel.root.method_22699(matrices, buffer, light, overlay, color);
      matrices.method_22909();
      return true;
   }

   private static CustomModel getModel(String modelName) {
      Supplier<CustomModel> factory = MODEL_FACTORIES.get(modelName);
      if (factory == null) {
         return null;
      }

      return MODEL_CACHE.computeIfAbsent(modelName, ignored -> factory.get());
   }

   private static CustomModel createRabbit() {
      class_630 root = createRoot(64, 64, data -> {
         class_5610 rabbit = data.method_32117("rabbit", box(28, 45, -5.0F, -13.0F, -5.0F, 10.0F, 11.0F, 8.0F), class_5603.method_32090(0.0F, 24.0F, 0.0F));
         rabbit.method_32117("right_leg", box(0, 32, -2.0F, 0.0F, -2.0F, 4.0F, 2.0F, 4.0F), class_5603.method_32090(-3.0F, -2.0F, -1.0F));
         rabbit.method_32117("left_leg", box(0, 32, -2.0F, 0.0F, -2.0F, 4.0F, 2.0F, 4.0F), class_5603.method_32090(3.0F, -2.0F, -1.0F));
         rabbit.method_32117("left_arm", box(24, 16, 0.0F, 0.0F, -2.0F, 2.0F, 8.0F, 4.0F), class_5603.method_32091(5.0F, -13.0F, -1.0F, 0.0F, 0.0F, -0.0873F));
         rabbit.method_32117("right_arm", box(24, 16, -2.0F, 0.0F, -2.0F, 2.0F, 8.0F, 4.0F), class_5603.method_32091(-5.0F, -13.0F, -1.0F, 0.0F, 0.0F, 0.0873F));

         class_5610 head = rabbit.method_32117(
            "head",
            class_5606.method_32108()
               .method_32101(0, 0)
               .method_32097(-3.0F, 0.0F, -4.0F, 6.0F, 1.0F, 6.0F)
               .method_32101(0, 45)
               .method_32097(-4.0F, -11.0F, -4.0F, 8.0F, 11.0F, 8.0F)
               .method_32101(56, 0)
               .method_32097(-5.0F, -9.0F, -5.0F, 2.0F, 3.0F, 2.0F)
               .method_32101(56, 0)
               .method_32097(3.0F, -9.0F, -5.0F, 2.0F, 3.0F, 2.0F)
               .method_32101(46, 0)
               .method_32097(1.0F, -20.0F, 0.0F, 3.0F, 9.0F, 1.0F)
               .method_32101(46, 0)
               .method_32097(-4.0F, -20.0F, 0.0F, 3.0F, 9.0F, 1.0F),
            class_5603.method_32090(0.0F, -14.0F, -1.0F)
         );
         head.method_32117("nose", box(0, 7, -1.5F, -4.0F, -5.5F, 3.0F, 2.0F, 1.0F), class_5603.field_27701);
      });

      return new CustomModel(
         CustomModels.RABBIT,
         class_2960.method_60654("custommodels/rabbit.png"),
         root,
         root.method_32086("rabbit"),
         root.method_32086("rabbit").method_32086("head"),
         root.method_32086("rabbit"),
         root.method_32086("rabbit").method_32086("left_arm"),
         root.method_32086("rabbit").method_32086("right_arm"),
         root.method_32086("rabbit").method_32086("left_leg"),
         root.method_32086("rabbit").method_32086("right_leg")
      ) {
         @Override
         void transform(class_4587 matrices, class_10055 state) {
            matrices.method_22905(1.25F, 1.25F, 1.25F);
            matrices.method_22904(0.0F, -0.3F, 0.0F);
         }

         @Override
         void copyAngles(class_591 vanilla, class_10055 state) {
            this.copyHeadAngles(vanilla);
            this.clearAngles(this.body);
            this.copyLimbAngles(vanilla);
            this.leftArm.field_3674 -= 0.0873F;
            this.rightArm.field_3674 += 0.0873F;
         }
      };
   }

   private static CustomModel createFreddy() {
      class_630 root = createRoot(100, 80, data -> {
         class_5610 body = data.method_32117("body", box(0, 0, -1.0F, -14.0F, -1.0F, 2.0F, 24.0F, 2.0F), class_5603.method_32090(0.0F, -9.0F, 0.0F));
         body.method_32117("torso", box(8, 0, -6.0F, -9.0F, -4.0F, 12.0F, 18.0F, 8.0F), class_5603.method_32092((float)Math.PI / 180.0F, 0.0F, 0.0F));
         body.method_32117("crotch", box(56, 0, -5.5F, 0.0F, -3.5F, 11.0F, 3.0F, 7.0F), class_5603.method_32090(0.0F, 9.5F, 0.0F));

         class_5610 head = body.method_32117("head", box(39, 22, -5.5F, -8.0F, -4.5F, 11.0F, 8.0F, 9.0F), class_5603.method_32090(0.0F, -13.0F, -0.5F));
         head.method_32117("jaw", box(49, 65, -5.0F, 0.0F, -4.5F, 10.0F, 3.0F, 9.0F), class_5603.method_32091(0.0F, 0.5F, 0.0F, 0.08726646F, 0.0F, 0.0F));
         head.method_32117("nose", box(17, 67, -4.0F, -2.0F, -3.0F, 8.0F, 4.0F, 3.0F), class_5603.method_32090(0.0F, -2.0F, -4.5F));
         class_5610 rightEar = head.method_32117(
            "right_ear", box(8, 0, -1.0F, -3.0F, -0.5F, 2.0F, 3.0F, 1.0F), class_5603.method_32091(-4.5F, -5.5F, 0.0F, 0.05235988F, 0.0F, -1.0471976F)
         );
         rightEar.method_32117("right_ear_pad", box(85, 0, -2.0F, -5.0F, -1.0F, 4.0F, 4.0F, 2.0F), class_5603.method_32090(0.0F, -1.0F, 0.0F));
         class_5610 leftEar = head.method_32117(
            "left_ear", box(40, 0, -1.0F, -3.0F, -0.5F, 2.0F, 3.0F, 1.0F), class_5603.method_32091(4.5F, -5.5F, 0.0F, 0.05235988F, 0.0F, 1.0471976F)
         );
         leftEar.method_32117("left_ear_pad", box(40, 39, -2.0F, -5.0F, -1.0F, 4.0F, 4.0F, 2.0F), class_5603.method_32090(0.0F, -1.0F, 0.0F));
         class_5610 hat = head.method_32117(
            "hat", box(70, 24, -3.0F, -0.5F, -3.0F, 6.0F, 1.0F, 6.0F), class_5603.method_32091(0.0F, -8.4F, 0.0F, (float)(-Math.PI) / 180.0F, 0.0F, 0.0F)
         );
         hat.method_32117("hat_top", box(78, 61, -2.0F, -4.0F, -2.0F, 4.0F, 4.0F, 4.0F), class_5603.method_32090(0.0F, 0.1F, 0.0F));

         class_5610 rightArm = body.method_32117(
            "right_arm", box(48, 0, -1.0F, 0.0F, -1.0F, 2.0F, 10.0F, 2.0F), class_5603.method_32091(-6.5F, -8.0F, 0.0F, 0.0F, 0.0F, 0.2617994F)
         );
         rightArm.method_32117("right_arm_pad", box(70, 10, -2.5F, 0.0F, -2.5F, 5.0F, 9.0F, 5.0F), class_5603.method_32090(0.0F, 0.5F, 0.0F));
         class_5610 rightForearm = rightArm.method_32117(
            "right_forearm", box(90, 20, -1.0F, 0.0F, -1.0F, 2.0F, 8.0F, 2.0F), class_5603.method_32091(0.0F, 9.6F, 0.0F, -0.17453292F, 0.0F, 0.0F)
         );
         rightForearm.method_32117("right_hand", box(20, 26, -2.0F, 0.0F, -2.5F, 4.0F, 4.0F, 5.0F), class_5603.method_32091(0.0F, 8.0F, 0.0F, 0.0F, 0.0F, -0.05235988F));

         class_5610 leftArm = body.method_32117(
            "left_arm", box(62, 10, -1.0F, 0.0F, -1.0F, 2.0F, 10.0F, 2.0F), class_5603.method_32091(6.5F, -8.0F, 0.0F, 0.0F, 0.0F, -0.2617994F)
         );
         leftArm.method_32117("left_arm_pad", box(38, 54, -2.5F, 0.0F, -2.5F, 5.0F, 9.0F, 5.0F), class_5603.method_32090(0.0F, 0.5F, 0.0F));
         class_5610 leftForearm = leftArm.method_32117(
            "left_forearm", box(90, 48, -1.0F, 0.0F, -1.0F, 2.0F, 8.0F, 2.0F), class_5603.method_32091(0.0F, 9.6F, 0.0F, -0.17453292F, 0.0F, 0.0F)
         );
         leftForearm.method_32117("left_hand", box(58, 56, -1.0F, 0.0F, -2.5F, 4.0F, 4.0F, 5.0F), class_5603.method_32091(0.0F, 8.0F, 0.0F, 0.0F, 0.0F, 0.05235988F));

         class_5610 rightLeg = body.method_32117("right_leg", box(90, 8, -1.0F, 0.0F, -1.0F, 2.0F, 10.0F, 2.0F), class_5603.method_32090(-3.3F, 12.5F, 0.0F));
         rightLeg.method_32117("right_leg_pad", box(73, 33, -3.0F, 0.0F, -3.0F, 6.0F, 9.0F, 6.0F), class_5603.method_32090(0.0F, 0.5F, 0.0F));
         class_5610 rightLowerLeg = rightLeg.method_32117(
            "right_lower_leg", box(20, 35, -1.0F, 0.0F, -1.0F, 2.0F, 8.0F, 2.0F), class_5603.method_32091(0.0F, 9.6F, 0.0F, (float)Math.PI / 90.0F, 0.0F, 0.0F)
         );
         rightLowerLeg.method_32117("right_lower_leg_pad", box(0, 39, -2.5F, 0.0F, -3.0F, 5.0F, 7.0F, 6.0F), class_5603.method_32090(0.0F, 0.5F, 0.0F));
         rightLowerLeg.method_32117(
            "right_foot", box(22, 39, -2.5F, 0.0F, -6.0F, 5.0F, 3.0F, 8.0F), class_5603.method_32091(0.0F, 8.0F, 0.0F, (float)(-Math.PI) / 90.0F, 0.0F, 0.0F)
         );
         class_5610 leftLeg = body.method_32117("left_leg", box(54, 10, -1.0F, 0.0F, -1.0F, 2.0F, 10.0F, 2.0F), class_5603.method_32090(3.3F, 12.5F, 0.0F));
         leftLeg.method_32117("left_leg_pad", box(48, 39, -3.0F, 0.0F, -3.0F, 6.0F, 9.0F, 6.0F), class_5603.method_32090(0.0F, 0.5F, 0.0F));
         class_5610 leftLowerLeg = leftLeg.method_32117(
            "left_lower_leg", box(72, 48, -1.0F, 0.0F, -1.0F, 2.0F, 8.0F, 2.0F), class_5603.method_32091(0.0F, 9.6F, 0.0F, (float)Math.PI / 90.0F, 0.0F, 0.0F)
         );
         leftLowerLeg.method_32117("left_lower_leg_pad", box(16, 50, -2.5F, 0.0F, -3.0F, 5.0F, 7.0F, 6.0F), class_5603.method_32090(0.0F, 0.5F, 0.0F));
         leftLowerLeg.method_32117(
            "left_foot", box(72, 50, -2.5F, 0.0F, -6.0F, 5.0F, 3.0F, 8.0F), class_5603.method_32091(0.0F, 8.0F, 0.0F, (float)(-Math.PI) / 90.0F, 0.0F, 0.0F)
         );
      });

      class_630 body = root.method_32086("body");
      return new CustomModel(
         CustomModels.FREDDY,
         class_2960.method_60654("custommodels/freddy.png"),
         root,
         body,
         body.method_32086("head"),
         body,
         body.method_32086("left_arm"),
         body.method_32086("right_arm"),
         body.method_32086("left_leg"),
         body.method_32086("right_leg")
      ) {
         @Override
         void transform(class_4587 matrices, class_10055 state) {
            matrices.method_22905(0.75F, 0.65F, 0.75F);
            matrices.method_22904(0.0F, 0.85F, 0.0F);
         }

         @Override
         void copyAngles(class_591 vanilla, class_10055 state) {
            this.copyHeadAngles(vanilla);
            this.clearAngles(this.body);
            this.copyLimbAngles(vanilla);
         }
      };
   }

   private static CustomModel createTung() {
      class_630 root = createRoot(64, 64, data -> {
         class_5610 tung = data.method_32117("tung", class_5606.method_32108(), class_5603.method_32090(0.0F, 24.0F, 0.0F));
         class_5610 chest = tung.method_32117("chest", class_5606.method_32108(), class_5603.method_32090(0.0F, -11.0F, 1.0F));
         chest.method_32117("body", box(0, 18, -3.0F, -5.5F, -2.5F, 6.0F, 11.0F, 5.0F, 0.3F), class_5603.method_32090(0.0F, -5.5F, 0.0F));
         class_5610 head = chest.method_32117(
            "head",
            class_5606.method_32108()
               .method_32101(0, 0)
               .method_32098(-3.5F, -11.17242F, -2.89262F, 7.0F, 12.0F, 6.0F, new class_5605(0.15F))
               .method_32101(35, 4)
               .method_32098(-3.45F, -7.67242F, -2.99262F, 2.0F, 2.0F, 0.9F, new class_5605(0.5F))
               .method_32101(35, 4)
               .method_32106(true)
               .method_32098(1.45F, -7.67242F, -2.99262F, 2.0F, 2.0F, 0.9F, new class_5605(0.5F))
               .method_32106(false)
               .method_32101(36, 29)
               .method_32097(-4.1F, -8.42242F, -3.69262F, 3.25F, 0.5F, 2.25F)
               .method_32101(36, 29)
               .method_32097(0.85F, -8.42242F, -3.69262F, 3.25F, 0.5F, 2.25F),
            class_5603.method_32090(0.0F, -12.12758F, -0.10738F)
         );
         head.method_32117("nose", box(30, 24, -1.0F, -2.5F, -1.0F, 1.5F, 5.0F, 2.0F, 0.15F), class_5603.method_32091(0.25F, -5.67242F, -2.64262F, 0.34906585F, 0.0F, 0.0F));
         head.method_32117("brow_left", box(30, 31, -1.5F, -0.5F, -1.0F, 3.0F, 1.0F, 2.0F), class_5603.method_32090(-2.25F, -9.27242F, -2.64262F));
         head.method_32117("brow_right", box(30, 31, -1.5F, -0.5F, -1.0F, 3.0F, 1.0F, 2.0F), class_5603.method_32090(2.25F, -9.27242F, -2.64262F));
         head.method_32117("eye_left", box(6, 34, -0.5F, -0.5F, -0.5F, 1.0F, 1.0F, 1.0F, 0.4F), class_5603.method_32090(-2.35F, -5.97242F, -2.69262F));
         head.method_32117("eye_right", box(6, 34, -0.5F, -0.5F, -0.5F, 1.0F, 1.0F, 1.0F, 0.4F), class_5603.method_32090(2.35F, -5.97242F, -2.69262F));

         class_5610 leftArm = chest.method_32117("left_arm", box(26, 0, 0.25F, 5.15F, -1.0F, 2.0F, 5.0F, 2.0F, 0.1F), class_5603.method_32090(3.75F, -10.35F, 0.0F));
         leftArm.method_32117("left_arm_top", box(22, 24, -1.0F, -3.0F, -1.0F, 2.0F, 6.0F, 2.0F), class_5603.method_32091(0.75F, 2.35F, 0.0F, 0.0F, 0.0F, 0.17453292F));
         class_5610 rightArm = chest.method_32117("right_arm", box(26, 0, -2.25F, 5.15F, -1.0F, 2.0F, 5.0F, 2.0F, 0.1F), class_5603.method_32090(-3.75F, -10.35F, 0.0F));
         rightArm.method_32117("right_arm_top", box(22, 24, -1.0F, -3.0F, -1.0F, 2.0F, 6.0F, 2.0F), class_5603.method_32091(-0.75F, 2.35F, 0.0F, 0.0F, 0.0F, -0.17453292F));
         rightArm.method_32117("bat", box(34, 6, 0.0F, -1.0F, -1.0F, 12.0F, 2.0F, 2.0F), class_5603.method_32091(-1.25F, 9.1F, 0.1F, -1.5707964F, 1.134464F, -1.5707964F));

         class_5610 leftLeg = tung.method_32117(
            "left_leg",
            class_5606.method_32108()
               .method_32101(26, 7)
               .method_32098(-1.0F, 0.0F, -1.0F, 2.0F, 4.0F, 2.0F, new class_5605(0.15F))
               .method_32101(22, 32)
               .method_32098(-1.0F, 3.0F, -1.0F, 2.0F, 2.0F, 2.0F, new class_5605(0.3F))
               .method_32101(26, 0)
               .method_32097(-1.0F, 4.0F, -1.0F, 2.0F, 5.0F, 2.0F)
               .method_32101(22, 18)
               .method_32097(-1.5F, 9.0F, -3.0F, 3.0F, 2.0F, 4.0F),
            class_5603.method_32090(2.0F, -11.0F, 1.0F)
         );
         class_5610 rightLeg = tung.method_32117(
            "right_leg",
            class_5606.method_32108()
               .method_32101(26, 7)
               .method_32098(-1.0F, 0.0F, -1.0F, 2.0F, 4.0F, 2.0F, new class_5605(0.15F))
               .method_32101(22, 32)
               .method_32098(-1.0F, 3.0F, -1.0F, 2.0F, 2.0F, 2.0F, new class_5605(0.3F))
               .method_32101(26, 0)
               .method_32097(-1.0F, 4.0F, -1.0F, 2.0F, 5.0F, 2.0F)
               .method_32101(22, 18)
               .method_32097(-1.5F, 9.0F, -3.0F, 3.0F, 2.0F, 4.0F),
            class_5603.method_32090(-2.0F, -11.0F, 1.0F)
         );
      });

      class_630 tung = root.method_32086("tung");
      class_630 chest = tung.method_32086("chest");
      return new CustomModel(
         CustomModels.TUNG,
         class_2960.method_60654("custommodels/tung.png"),
         root,
         tung,
         chest.method_32086("head"),
         chest,
         chest.method_32086("left_arm"),
         chest.method_32086("right_arm"),
         tung.method_32086("left_leg"),
         tung.method_32086("right_leg")
      );
   }

   private static class_630 createRoot(int textureWidth, int textureHeight, Consumer<class_5610> builder) {
      class_5609 data = new class_5609();
      builder.accept(data.method_32111());
      return class_5607.method_32110(data, textureWidth, textureHeight).method_32109();
   }

   private static class_5606 box(int u, int v, float x, float y, float z, float width, float height, float depth) {
      return class_5606.method_32108().method_32101(u, v).method_32097(x, y, z, width, height, depth);
   }

   private static class_5606 box(int u, int v, float x, float y, float z, float width, float height, float depth, float dilation) {
      return class_5606.method_32108().method_32101(u, v).method_32098(x, y, z, width, height, depth, new class_5605(dilation));
   }

   private static class CustomModel {
      protected final String name;
      protected final class_2960 texture;
      protected final class_630 root;
      protected final class_630 bodyRoot;
      protected final class_630 head;
      protected final class_630 body;
      protected final class_630 leftArm;
      protected final class_630 rightArm;
      protected final class_630 leftLeg;
      protected final class_630 rightLeg;

      protected CustomModel(
         String name,
         class_2960 texture,
         class_630 root,
         class_630 bodyRoot,
         class_630 head,
         class_630 body,
         class_630 leftArm,
         class_630 rightArm,
         class_630 leftLeg,
         class_630 rightLeg
      ) {
         this.name = name;
         this.texture = texture;
         this.root = root;
         this.bodyRoot = bodyRoot;
         this.head = head;
         this.body = body;
         this.leftArm = leftArm;
         this.rightArm = rightArm;
         this.leftLeg = leftLeg;
         this.rightLeg = rightLeg;
      }

      void transform(class_4587 matrices, class_10055 state) {
      }

      void copyAngles(class_591 vanilla, class_10055 state) {
         this.copyHeadAngles(vanilla);
         this.copyBodyAngles(vanilla);
         this.copyLimbAngles(vanilla);
      }

      protected void copyHeadAngles(class_591 vanilla) {
         this.head.field_3654 = vanilla.field_3398.field_3654;
         this.head.field_3675 = vanilla.field_3398.field_3675;
         this.head.field_3674 = vanilla.field_3398.field_3674;
      }

      protected void copyBodyAngles(class_591 vanilla) {
         this.body.field_3654 = vanilla.field_3391.field_3654;
         this.body.field_3675 = vanilla.field_3391.field_3675;
         this.body.field_3674 = vanilla.field_3391.field_3674;
      }

      protected void copyLimbAngles(class_591 vanilla) {
         this.leftArm.field_3654 = vanilla.field_27433.field_3654;
         this.leftArm.field_3675 = vanilla.field_27433.field_3675;
         this.leftArm.field_3674 = vanilla.field_27433.field_3674;

         this.rightArm.field_3654 = vanilla.field_3401.field_3654;
         this.rightArm.field_3675 = vanilla.field_3401.field_3675;
         this.rightArm.field_3674 = vanilla.field_3401.field_3674;

         this.leftLeg.field_3654 = vanilla.field_3397.field_3654;
         this.leftLeg.field_3675 = vanilla.field_3397.field_3675;
         this.leftLeg.field_3674 = vanilla.field_3397.field_3674;

         this.rightLeg.field_3654 = vanilla.field_3392.field_3654;
         this.rightLeg.field_3675 = vanilla.field_3392.field_3675;
         this.rightLeg.field_3674 = vanilla.field_3392.field_3674;
      }

      protected void clearAngles(class_630 part) {
         part.field_3654 = 0.0F;
         part.field_3675 = 0.0F;
         part.field_3674 = 0.0F;
      }
   }

   private CustomPlayerModelRenderer() {
   }
}
