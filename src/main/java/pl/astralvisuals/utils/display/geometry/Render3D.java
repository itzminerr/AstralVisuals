package pl.astralvisuals.utils.display.geometry;

import com.mojang.blaze3d.platform.GlStateManager.class_4534;
import com.mojang.blaze3d.platform.GlStateManager.class_4535;
import com.mojang.blaze3d.systems.RenderSystem;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import net.minecraft.class_10017;
import net.minecraft.class_10142;
import net.minecraft.class_1297;
import net.minecraft.class_1309;
import net.minecraft.class_2338;
import net.minecraft.class_238;
import net.minecraft.class_243;
import net.minecraft.class_265;
import net.minecraft.class_286;
import net.minecraft.class_287;
import net.minecraft.class_289;
import net.minecraft.class_290;
import net.minecraft.class_2960;
import net.minecraft.class_3532;
import net.minecraft.class_3545;
import net.minecraft.class_4184;
import net.minecraft.class_4587;
import net.minecraft.class_4588;
import net.minecraft.class_4597;
import net.minecraft.class_7833;
import net.minecraft.class_897;
import net.minecraft.class_293.class_5596;
import net.minecraft.class_4587.class_4665;
import net.minecraft.class_4597.class_4598;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector4i;
import org.lwjgl.opengl.GL11;
import pl.astralvisuals.events.render.WorldRenderEvent;
import pl.astralvisuals.features.impl.render.TargetESP;
import pl.astralvisuals.utils.display.color.ColorAssist;
import pl.astralvisuals.utils.display.interfaces.QuickImports;
import pl.astralvisuals.utils.math.calc.Calculate;
import pl.astralvisuals.utils.math.projection.Projection;

public final class Render3D implements QuickImports {
   private static final Map<class_265, class_3545<List<class_238>, List<Render3D.Line>>> SHAPE_OUTLINES = new HashMap<>();
   private static final Map<class_265, List<class_238>> SHAPE_BOXES = new HashMap<>();
   public static final List<Render3D.Texture> TEXTURE_DEPTH = new ArrayList<>();
   public static final List<Render3D.Texture> TEXTURE = new ArrayList<>();
   public static final List<Render3D.Line> LINE_DEPTH = new ArrayList<>();
   public static final List<Render3D.Line> LINE = new ArrayList<>();
   public static final List<Render3D.Quad> QUAD_DEPTH = new ArrayList<>();
   public static final List<Render3D.Quad> QUAD = new ArrayList<>();
   public static Matrix4f lastProjMat = new Matrix4f();
   public static class_4665 lastWorldSpaceMatrix = new class_4587().method_23760();
   private static final class_2960 bloom = class_2960.method_60654("textures/features/particles/bloom.png");
   public static final List<Render3D.Crystal> crystalList = new ArrayList<>();
   private static float prevCubeSize;
   private static float prevEspValue;
   private static float espValue = 1.0F;
   private static float espSpeed = 1.0F;
   private static float circleStep;
   private static boolean flipSpeed;

   public static void drawEntity(class_1297 entity, class_243 pos, float yaw, int alpha, class_4587 matrices, float tickDelta) {
      if (entity instanceof class_1309 livingEntity) {
         matrices.method_22903();
         matrices.method_22904(pos.field_1352, pos.field_1351, pos.field_1350);
         matrices.method_22907(class_7833.field_40716.rotationDegrees(yaw));
         matrices.method_22905(1.0F, 1.0F, 1.0F);
         RenderSystem.enableBlend();
         RenderSystem.blendFunc(class_4535.SRC_ALPHA, class_4534.ONE_MINUS_SRC_ALPHA);
         RenderSystem.enableDepthTest();
         RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, alpha / 255.0F);
         class_897 renderer = mc.method_1561().method_3953(entity);
         if (renderer != null) {
            int light = renderer.method_24088(livingEntity, tickDelta);
            class_4597 vertexConsumers = mc.method_22940().method_23000();
            class_10017 renderState = renderer.method_62425(livingEntity, tickDelta);
            if (renderState != null) {
               renderer.method_3936(renderState, matrices, vertexConsumers, light);
            }

            ((class_4598)vertexConsumers).method_22993();
         }

         RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
         RenderSystem.disableBlend();
         matrices.method_22909();
      }
   }

   public static void onWorldRender(WorldRenderEvent e) {
      if (!TEXTURE.isEmpty()) {
         Set<class_2960> identifiers = TEXTURE.stream().map(texture -> texture.id).collect(Collectors.toCollection(LinkedHashSet::new));
         RenderSystem.enableBlend();
         RenderSystem.blendFunc(class_4535.SRC_ALPHA, class_4534.ONE_MINUS_CONSTANT_ALPHA);
         identifiers.forEach(
            id -> {
               RenderSystem.setShaderTexture(0, id);
               RenderSystem.setShader(class_10142.field_53880);
               class_287 bufferx = class_289.method_1348().method_60827(class_5596.field_27382, class_290.field_1575);
               TEXTURE.stream()
                  .filter(texture -> texture.id.equals(id))
                  .forEach(tex -> quadTexture(tex.entry, bufferx, tex.x, tex.y, tex.width, tex.height, tex.color));
               class_286.method_43433(bufferx.method_60800());
            }
         );
         RenderSystem.disableBlend();
         TEXTURE.clear();
      }

      if (!TEXTURE_DEPTH.isEmpty()) {
         Set<class_2960> identifiers = TEXTURE_DEPTH.stream().map(texture -> texture.id).collect(Collectors.toCollection(LinkedHashSet::new));
         RenderSystem.enableBlend();
         RenderSystem.enableDepthTest();
         RenderSystem.depthMask(false);
         RenderSystem.blendFunc(class_4535.SRC_ALPHA, class_4534.ONE_MINUS_CONSTANT_ALPHA);
         identifiers.forEach(
            id -> {
               RenderSystem.setShaderTexture(0, id);
               RenderSystem.setShader(class_10142.field_53880);
               class_287 bufferx = class_289.method_1348().method_60827(class_5596.field_27382, class_290.field_1575);
               TEXTURE_DEPTH.stream()
                  .filter(texture -> texture.id.equals(id))
                  .forEach(tex -> quadTexture(tex.entry, bufferx, tex.x, tex.y, tex.width, tex.height, tex.color));
               class_286.method_43433(bufferx.method_60800());
            }
         );
         RenderSystem.depthMask(true);
         RenderSystem.disableBlend();
         TEXTURE_DEPTH.clear();
      }

      if (!LINE.isEmpty()) {
         GL11.glEnable(2881);
         Set<Float> widths = LINE.stream().map(line -> line.width).collect(Collectors.toCollection(LinkedHashSet::new));
         RenderSystem.enableBlend();
         RenderSystem.disableCull();
         RenderSystem.disableDepthTest();
         RenderSystem.blendFunc(class_4535.SRC_ALPHA, class_4534.ONE_MINUS_CONSTANT_ALPHA);
         RenderSystem.setShader(class_10142.field_53864);
         widths.forEach(
            width -> {
               RenderSystem.lineWidth(width);
               class_287 bufferx = tessellator.method_60827(class_5596.field_27377, class_290.field_29337);
               LINE.stream()
                  .filter(line -> line.width == width)
                  .forEach(line -> vertexLine(line.entry, bufferx, line.start.method_46409(), line.end.method_46409(), line.colorStart, line.colorEnd));
               class_286.method_43433(bufferx.method_60800());
            }
         );
         RenderSystem.enableDepthTest();
         RenderSystem.enableCull();
         RenderSystem.disableBlend();
         LINE.clear();
         GL11.glDisable(2881);
      }

      if (!QUAD.isEmpty()) {
         RenderSystem.enableBlend();
         RenderSystem.disableCull();
         RenderSystem.disableDepthTest();
         RenderSystem.blendFunc(class_4535.SRC_ALPHA, class_4534.ONE_MINUS_CONSTANT_ALPHA);
         RenderSystem.setShader(class_10142.field_53876);
         class_287 buffer = tessellator.method_60827(class_5596.field_27382, class_290.field_1576);
         QUAD.forEach(quad -> vertexQuad(quad.entry, buffer, quad.x, quad.y, quad.w, quad.z, quad.color));
         class_286.method_43433(buffer.method_60800());
         RenderSystem.enableDepthTest();
         RenderSystem.enableCull();
         RenderSystem.disableBlend();
         QUAD.clear();
      }

      if (!LINE_DEPTH.isEmpty()) {
         GL11.glEnable(2881);
         Set<Float> widths = LINE_DEPTH.stream().map(line -> line.width).collect(Collectors.toCollection(LinkedHashSet::new));
         RenderSystem.enableBlend();
         RenderSystem.disableCull();
         RenderSystem.enableDepthTest();
         RenderSystem.depthMask(false);
         RenderSystem.blendFunc(class_4535.SRC_ALPHA, class_4534.ONE_MINUS_CONSTANT_ALPHA);
         RenderSystem.setShader(class_10142.field_53864);
         widths.forEach(
            width -> {
               RenderSystem.lineWidth(width);
               class_287 bufferx = tessellator.method_60827(class_5596.field_27377, class_290.field_29337);
               LINE_DEPTH.stream()
                  .filter(line -> line.width == width)
                  .forEach(line -> vertexLine(line.entry, bufferx, line.start.method_46409(), line.end.method_46409(), line.colorStart, line.colorEnd));
               class_286.method_43433(bufferx.method_60800());
            }
         );
         RenderSystem.depthMask(true);
         RenderSystem.enableCull();
         RenderSystem.disableBlend();
         LINE_DEPTH.clear();
         GL11.glDisable(2881);
      }

      if (!QUAD_DEPTH.isEmpty()) {
         RenderSystem.enableBlend();
         RenderSystem.disableCull();
         RenderSystem.enableDepthTest();
         RenderSystem.blendFunc(class_4535.SRC_ALPHA, class_4534.ONE_MINUS_CONSTANT_ALPHA);
         RenderSystem.setShader(class_10142.field_53876);
         class_287 buffer = tessellator.method_60827(class_5596.field_27382, class_290.field_1576);
         QUAD_DEPTH.forEach(quad -> vertexQuad(quad.entry, buffer, quad.x, quad.y, quad.w, quad.z, quad.color));
         class_286.method_43433(buffer.method_60800());
         RenderSystem.enableCull();
         RenderSystem.disableBlend();
         QUAD_DEPTH.clear();
      }
   }

   public static void drawCube(class_1309 lastTarget, float anim, float red, String png) {
      float baseSize = red - anim - 0.17F;
      float targetSize = baseSize;
      if (png != null) {
         if ("2".equals(png)) {
            targetSize = red - anim - 0.05F;
         } else if ("4".equals(png)) {
            targetSize = red - anim + 0.05F;
         } else if ("5".equals(png)) {
            targetSize = red - anim + 0.07F;
         }
      }

      float size = Calculate.interpolate(prevCubeSize, targetSize, 0.2F);
      prevCubeSize = size;
      size *= TargetESP.getInstance().getRadius();
      class_4184 camera = mc.method_1561().field_4686;
      class_243 vec = Calculate.interpolate(lastTarget).method_1020(camera.method_19326());
      class_4587 matrix = new class_4587();
      matrix.method_22903();
      matrix.method_22907(class_7833.field_40714.rotationDegrees(camera.method_19329()));
      matrix.method_22907(class_7833.field_40716.rotationDegrees(camera.method_19330() + 180.0F));
      matrix.method_22904(vec.field_1352, vec.field_1351 + lastTarget.method_5829().method_17940() / 2.0, vec.field_1350);
      matrix.method_22907(class_7833.field_40716.rotationDegrees(-camera.method_19330()));
      matrix.method_22907(class_7833.field_40714.rotationDegrees(camera.method_19329()));
      // Вращение в стиле EvaWare (Marker): только для текстур куба 1-4; на кубе 5 (и др. модах) не применяем.
      if (!"5".equals(png)) {
         matrix.method_22907(class_7833.field_40718.rotationDegrees(Calculate.interpolate(prevEspValue, espValue)));
      }

      class_4665 entry = matrix.method_23760().method_56822();
      Vector4i color = ColorAssist.multRedAndAlpha(
         new Vector4i(
            TargetESP.getInstance().colorSetting.getColor(),
            TargetESP.getInstance().colorSetting.getColor(),
            TargetESP.getInstance().colorSetting.getColor(),
            TargetESP.getInstance().colorSetting.getColor()
         ),
         1.0F + red * 10.0F,
         anim
      );
      drawTexture(entry, class_2960.method_60654("textures/features/targetesp/capture" + png + ".png"), -size / 2.0F, -size / 2.0F, size, size, color, false);
      matrix.method_22909();
   }

   public static void drawCircle(class_4587 matrix, class_1309 lastTarget, float anim, float red) {
      class_243 target = Calculate.interpolate(lastTarget);
      boolean canSee = Objects.requireNonNull(mc.field_1724).method_6057(lastTarget);
      float hitEffect = Math.min(red * 2.0F, 1.0F);
      float distanceMultiplier = 1.0F + (float)Math.sin(hitEffect * Math.PI) * 0.18F;
      GL11.glEnable(2881);
      if (canSee) {
         RenderSystem.enableDepthTest();
         RenderSystem.depthMask(false);
      } else {
         RenderSystem.disableDepthTest();
      }

      RenderSystem.enableBlend();
      RenderSystem.disableCull();
      RenderSystem.blendFunc(class_4535.SRC_ALPHA, class_4534.ONE);
      RenderSystem.setShader(class_10142.field_53876);
      class_287 buffer = class_289.method_1348().method_60827(class_5596.field_27380, class_290.field_1576);
      int size = 64;
      List<class_243> ringPoints = new ArrayList<>(size + 1);
      double animationTime = System.currentTimeMillis() / 1000.0 * 3.0;

      // Всё, что не зависит от i, считаем один раз за кадр (раньше пересчитывалось 65×):
      // размеры цели, цвет и вертикальные смещения кольца — константы в пределах кадра.
      float width = lastTarget.method_17681() * distanceMultiplier * TargetESP.getInstance().getRadius();
      float height = lastTarget.method_17682();
      double minYOffset = 0.05;
      double maxYOffset = Math.max(minYOffset, height - 0.05);
      double availableHeight = Math.max(0.0, maxYOffset - minYOffset);
      double centerY = minYOffset + (Math.sin(animationTime) + 1.0) * 0.5 * availableHeight;
      double ringHalfBand = Math.min(0.07, Math.max(0.035, height * 0.05));
      double trailHeight = Math.min(0.22, Math.max(0.09, height * 0.12));
      double topOffset = centerY + ringHalfBand;
      double bottomOffset = centerY - trailHeight;
      int baseColor = TargetESP.getInstance().colorSetting.getColor();
      int color = ColorAssist.interpolateColor(baseColor, ColorAssist.multRed(baseColor, 2.0F), red);
      int startColor = ColorAssist.multAlpha(color, 0.76F * anim);
      int endColor = ColorAssist.multAlpha(color, 0.0F);
      class_4665 entry = matrix.method_23760();

      for (int i = 0; i <= size; i++) {
         class_243 cosSin = Calculate.cosSin(i, size, width);
         class_243 start = target.method_1031(cosSin.field_1352, cosSin.field_1351 + topOffset, cosSin.field_1350);
         class_243 end = target.method_1031(cosSin.field_1352, cosSin.field_1351 + bottomOffset, cosSin.field_1350);
         ringPoints.add(start);
         vertexLine(entry, buffer, start.method_46409(), end.method_46409(), startColor, endColor);
      }

      class_286.method_43433(buffer.method_60800());
      RenderSystem.setShader(class_10142.field_53864);
      RenderSystem.lineWidth(2.0F);
      class_287 lineBuffer = class_289.method_1348().method_60827(class_5596.field_27377, class_290.field_29337);
      int finalColor = ColorAssist.multAlpha(color, anim);

      for (int i = 0; i < ringPoints.size() - 1; i++) {
         vertexLine(entry, lineBuffer, ringPoints.get(i).method_46409(), ringPoints.get(i + 1).method_46409(), finalColor, finalColor);
      }

      class_286.method_43433(lineBuffer.method_60800());
      if (canSee) {
         RenderSystem.depthMask(true);
         RenderSystem.disableDepthTest();
      } else {
         RenderSystem.enableDepthTest();
      }

      GL11.glDisable(2881);
   }

   public static void updateTargetEsp() {
      prevEspValue = espValue;
      espValue = espValue + espSpeed;
      if (espSpeed > 25.0F) {
         flipSpeed = true;
      }

      if (espSpeed < -25.0F) {
         flipSpeed = false;
      }

      espSpeed = flipSpeed ? espSpeed - 0.5F : espSpeed + 0.5F;
      circleStep += 0.15F;
   }

   public static void drawShape(class_2338 blockPos, class_265 voxelShape, int color, float width) {
      drawShape(blockPos, voxelShape, color, width, true, false);
   }

   public static void drawShape(class_2338 blockPos, class_265 voxelShape, int color, float width, boolean fill, boolean depth) {
      if (SHAPE_BOXES.containsKey(voxelShape)) {
         SHAPE_BOXES.get(voxelShape).forEach(box -> {
            box = box.method_996(blockPos);
            if (Projection.canSee(box)) {
               drawBox(box, color, width, true, fill, depth);
            }
         });
      } else {
         SHAPE_BOXES.put(voxelShape, voxelShape.method_1090());
      }
   }

   public static void drawShapeAlternative(class_2338 blockPos, class_265 voxelShape, int color, float width, boolean fill, boolean depth) {
      class_243 vec3d = class_243.method_24954(blockPos);
      if (Projection.canSee(new class_238(blockPos))) {
         if (SHAPE_OUTLINES.containsKey(voxelShape)) {
            class_3545<List<class_238>, List<Render3D.Line>> pair = SHAPE_OUTLINES.get(voxelShape);
            if (fill) {
               pair.method_15442().forEach(box -> drawBox(box.method_997(vec3d), color, width, false, true, depth));
            }

            pair.method_15441().forEach(line -> drawLine(line.start.method_1019(vec3d), line.end.method_1019(vec3d), color, width, depth));
            return;
         }

         List<Render3D.Line> lines = new ArrayList<>();
         voxelShape.method_1104(
            (minX, minY, minZ, maxX, maxY, maxZ) -> lines.add(
               new Render3D.Line(null, new class_243(minX, minY, minZ), new class_243(maxX, maxY, maxZ), 0, 0, 0.0F)
            )
         );
         SHAPE_OUTLINES.put(voxelShape, new class_3545(voxelShape.method_1090(), lines));
      }
   }

   public static void drawBox(class_238 box, int color, float width) {
      drawBox(box, color, width, true, true, false);
   }

   public static void drawBox(class_238 box, int color, float width, boolean line, boolean fill, boolean depth) {
      drawBox(null, box, color, width, line, fill, depth);
   }

   public static void drawBox(class_4665 entry, class_238 box, int color, float width, boolean line, boolean fill, boolean depth) {
      box = box.method_1014(0.001);
      double x1 = box.field_1323;
      double y1 = box.field_1322;
      double z1 = box.field_1321;
      double x2 = box.field_1320;
      double y2 = box.field_1325;
      double z2 = box.field_1324;
      if (fill) {
         int fillColor = ColorAssist.multAlpha(color, 0.1F);
         drawQuad(entry, new class_243(x1, y1, z1), new class_243(x2, y1, z1), new class_243(x2, y1, z2), new class_243(x1, y1, z2), fillColor, depth);
         drawQuad(entry, new class_243(x1, y1, z1), new class_243(x1, y2, z1), new class_243(x2, y2, z1), new class_243(x2, y1, z1), fillColor, depth);
         drawQuad(entry, new class_243(x2, y1, z1), new class_243(x2, y2, z1), new class_243(x2, y2, z2), new class_243(x2, y1, z2), fillColor, depth);
         drawQuad(entry, new class_243(x1, y1, z2), new class_243(x2, y1, z2), new class_243(x2, y2, z2), new class_243(x1, y2, z2), fillColor, depth);
         drawQuad(entry, new class_243(x1, y1, z1), new class_243(x1, y1, z2), new class_243(x1, y2, z2), new class_243(x1, y2, z1), fillColor, depth);
         drawQuad(entry, new class_243(x1, y2, z1), new class_243(x1, y2, z2), new class_243(x2, y2, z2), new class_243(x2, y2, z1), fillColor, depth);
      }

      if (line) {
         drawLine(entry, x1, y1, z1, x2, y1, z1, color, width, depth);
         drawLine(entry, x2, y1, z1, x2, y1, z2, color, width, depth);
         drawLine(entry, x2, y1, z2, x1, y1, z2, color, width, depth);
         drawLine(entry, x1, y1, z2, x1, y1, z1, color, width, depth);
         drawLine(entry, x1, y1, z2, x1, y2, z2, color, width, depth);
         drawLine(entry, x1, y1, z1, x1, y2, z1, color, width, depth);
         drawLine(entry, x2, y1, z2, x2, y2, z2, color, width, depth);
         drawLine(entry, x2, y1, z1, x2, y2, z1, color, width, depth);
         drawLine(entry, x1, y2, z1, x2, y2, z1, color, width, depth);
         drawLine(entry, x2, y2, z1, x2, y2, z2, color, width, depth);
         drawLine(entry, x2, y2, z2, x1, y2, z2, color, width, depth);
         drawLine(entry, x1, y2, z2, x1, y2, z1, color, width, depth);
      }
   }

   public static void vertexLine(class_4587 matrices, class_4588 buffer, class_243 start, class_243 end, int startColor, int endColor) {
      vertexLine(matrices.method_23760(), buffer, start.method_46409(), end.method_46409(), startColor, endColor);
   }

   public static void vertexLine(class_4665 entry, class_4588 buffer, Vector3f start, Vector3f end, int startColor, int endColor) {
      if (entry == null) {
         entry = lastWorldSpaceMatrix;
      }

      Vector3f vec = getNormal(start, end);
      buffer.method_61032(entry, start).method_39415(startColor).method_61959(entry, vec);
      buffer.method_61032(entry, end).method_39415(endColor).method_61959(entry, vec);
   }

   public static void vertexQuad(class_4665 entry, class_4588 buffer, class_243 vec1, class_243 vec2, class_243 vec3, class_243 vec4, int color) {
      vertexQuad(entry, buffer, vec1.method_46409(), vec2.method_46409(), vec3.method_46409(), vec4.method_46409(), color);
   }

   public static void vertexQuad(class_4665 entry, class_4588 buffer, Vector3f vec1, Vector3f vec2, Vector3f vec3, Vector3f vec4, int color) {
      if (entry == null) {
         entry = lastWorldSpaceMatrix;
      }

      buffer.method_61032(entry, vec1).method_39415(color);
      buffer.method_61032(entry, vec2).method_39415(color);
      buffer.method_61032(entry, vec3).method_39415(color);
      buffer.method_61032(entry, vec4).method_39415(color);
   }

   public static void quadTexture(class_4665 entry, class_287 buffer, float x, float y, float width, float height, Vector4i color) {
      buffer.method_56824(entry, x, y + height, 0.0F).method_22913(0.0F, 0.0F).method_39415(color.x);
      buffer.method_56824(entry, x + width, y + height, 0.0F).method_22913(0.0F, 1.0F).method_39415(color.y);
      buffer.method_56824(entry, x + width, y, 0.0F).method_22913(1.0F, 1.0F).method_39415(color.w);
      buffer.method_56824(entry, x, y, 0.0F).method_22913(1.0F, 0.0F).method_39415(color.z);
   }

   public static Vector3f getNormal(Vector3f start, Vector3f end) {
      Vector3f normal = new Vector3f(start).sub(end);
      float sqrt = class_3532.method_15355(normal.lengthSquared());
      return normal.div(sqrt);
   }

   public static void drawLine(
      class_4665 entry, double minX, double minY, double minZ, double maxX, double maxY, double maxZ, int color, float width, boolean depth
   ) {
      drawLine(entry, new class_243(minX, minY, minZ), new class_243(maxX, maxY, maxZ), color, color, width, depth);
   }

   public static void drawLine(class_243 start, class_243 end, int color, float width, boolean depth) {
      drawLine(null, start, end, color, color, width, depth);
   }

   public static void drawLine(class_4665 entry, class_243 start, class_243 end, int colorStart, int colorEnd, float width, boolean depth) {
      Render3D.Line line = new Render3D.Line(entry, start, end, colorStart, colorEnd, width);
      if (depth) {
         LINE_DEPTH.add(line);
      } else {
         LINE.add(line);
      }
   }

   public static void drawCircleQuad(class_287 buffer, class_4665 entry, class_243 center, double radius, int color, int segments) {
      buffer.method_56824(entry, (float)center.field_1352, (float)center.field_1351, (float)center.field_1350).method_39415(color);

      for (int i = 0; i <= segments; i++) {
         double angle = (Math.PI * 2) * i / segments;
         double dx = Math.cos(angle) * radius;
         double dz = Math.sin(angle) * radius;
         buffer.method_56824(entry, (float)(center.field_1352 + dx), (float)center.field_1351, (float)(center.field_1350 + dz)).method_39415(color);
      }
   }

   public static void drawQuad(class_243 x, class_243 y, class_243 w, class_243 z, int color, boolean depth) {
      drawQuad(null, x, y, w, z, color, depth);
   }

   public static void drawQuad(class_4665 entry, class_243 x, class_243 y, class_243 w, class_243 z, int color, boolean depth) {
      Render3D.Quad quad = new Render3D.Quad(entry, x, y, w, z, color);
      if (depth) {
         QUAD_DEPTH.add(quad);
      } else {
         QUAD.add(quad);
      }
   }

   public static void drawTexture(class_4665 entry, class_2960 id, float x, float y, float width, float height, Vector4i color, boolean depth) {
      Render3D.Texture texture = new Render3D.Texture(entry, id, x, y, width, height, color);
      if (depth) {
         TEXTURE_DEPTH.add(texture);
      } else {
         TEXTURE.add(texture);
      }
   }

   private Render3D() {
      throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
   }

   public static void setLastProjMat(Matrix4f lastProjMat) {
      Render3D.lastProjMat = lastProjMat;
   }

   public static void setLastWorldSpaceMatrix(class_4665 lastWorldSpaceMatrix) {
      Render3D.lastWorldSpaceMatrix = lastWorldSpaceMatrix;
   }

   private static class Crystal {
      private final class_1297 entity;
      private final class_243 position;
      private final class_243 rotation;
      private final float size;
      private final float rotationSpeed;

      public Crystal(class_1297 entity, class_243 position, class_243 rotation) {
         this.entity = entity;
         this.position = position;
         this.rotation = rotation;
         this.size = 0.09F;
         this.rotationSpeed = 0.5F + (float)(Math.random() * 1.5);
      }

      public void render(class_4587 ms) {
         ms.method_22903();
         ms.method_22904(this.position.field_1352, this.position.field_1351, this.position.field_1350);
         float pulsation = 1.0F + (float)(Math.sin(System.currentTimeMillis() / 500.0) * 0.1F);
         ms.method_22905(pulsation, pulsation, pulsation);
         float selfRotation = (float)(System.currentTimeMillis() % 36000L) / 100.0F * this.rotationSpeed;
         ms.method_22907(class_7833.field_40714.rotationDegrees((float)this.rotation.field_1352));
         ms.method_22907(class_7833.field_40716.rotationDegrees((float)this.rotation.field_1351 + selfRotation));
         ms.method_22907(class_7833.field_40718.rotationDegrees((float)this.rotation.field_1350));
         RenderSystem.disableCull();
         RenderSystem.enableBlend();
         RenderSystem.blendFunc(class_4535.SRC_ALPHA, class_4534.ONE_MINUS_SRC_ALPHA);
         RenderSystem.setShader(class_10142.field_53876);
         int baseColor = ColorAssist.getColor(225, 225, 255, 255);
         RenderSystem.blendFunc(class_4535.SRC_ALPHA, class_4534.ONE);
         this.drawCrystal(ms, baseColor, 0.2F, true);
         RenderSystem.blendFunc(class_4535.SRC_ALPHA, class_4534.ONE_MINUS_SRC_ALPHA);
         this.drawCrystal(ms, baseColor, 0.3F, true);
         this.drawCrystal(ms, baseColor, 0.8F, false);
         RenderSystem.disableBlend();
         RenderSystem.enableCull();
         ms.method_22909();
      }

      private void drawCrystal(class_4587 ms, int baseColor, float alpha, boolean filled) {
         class_287 bufferBuilder = class_289.method_1348().method_60827(filled ? class_5596.field_27379 : class_5596.field_29344, class_290.field_1576);
         float s = this.size;
         float h_prism = this.size * 1.0F;
         float h_pyramid = this.size * 1.5F;
         int numSides = 8;
         List<class_243> topVertices = new ArrayList<>();
         List<class_243> bottomVertices = new ArrayList<>();

         for (int i = 0; i < numSides; i++) {
            float angle = (float)((Math.PI * 2) * i / numSides);
            float x = (float)(s * Math.cos(angle));
            float z = (float)(s * Math.sin(angle));
            topVertices.add(new class_243(x, h_prism / 2.0F, z));
            bottomVertices.add(new class_243(x, -h_prism / 2.0F, z));
         }

         class_243 vTop = new class_243(0.0, h_prism / 2.0F + h_pyramid, 0.0);
         class_243 vBottom = new class_243(0.0, -h_prism / 2.0F - h_pyramid, 0.0);
         int finalColor = ColorAssist.setAlpha(baseColor, (int)(alpha * 255.0F));

         for (int i = 0; i < numSides; i++) {
            class_243 v1 = bottomVertices.get(i);
            class_243 v2 = bottomVertices.get((i + 1) % numSides);
            class_243 v3 = topVertices.get((i + 1) % numSides);
            class_243 v4 = topVertices.get(i);
            this.drawQuad(ms, bufferBuilder, v1, v2, v3, v4, finalColor, filled);
         }

         for (int i = 0; i < numSides; i++) {
            class_243 v1 = topVertices.get(i);
            class_243 v2 = topVertices.get((i + 1) % numSides);
            this.drawTriangle(ms, bufferBuilder, vTop, v1, v2, finalColor, filled);
         }

         for (int i = 0; i < numSides; i++) {
            class_243 v1 = bottomVertices.get(i);
            class_243 v2 = bottomVertices.get((i + 1) % numSides);
            this.drawTriangle(ms, bufferBuilder, vBottom, v2, v1, finalColor, filled);
         }

         class_286.method_43433(bufferBuilder.method_60800());
      }

      private void drawTriangle(class_4587 ms, class_287 bb, class_243 v1, class_243 v2, class_243 v3, int color, boolean filled) {
         if (filled) {
            bb.method_22918(ms.method_23760().method_23761(), (float)v1.field_1352, (float)v1.field_1351, (float)v1.field_1350).method_39415(color);
            bb.method_22918(ms.method_23760().method_23761(), (float)v2.field_1352, (float)v2.field_1351, (float)v2.field_1350).method_39415(color);
            bb.method_22918(ms.method_23760().method_23761(), (float)v3.field_1352, (float)v3.field_1351, (float)v3.field_1350).method_39415(color);
         } else {
            bb.method_22918(ms.method_23760().method_23761(), (float)v1.field_1352, (float)v1.field_1351, (float)v1.field_1350).method_39415(color);
            bb.method_22918(ms.method_23760().method_23761(), (float)v2.field_1352, (float)v2.field_1351, (float)v2.field_1350).method_39415(color);
            bb.method_22918(ms.method_23760().method_23761(), (float)v2.field_1352, (float)v2.field_1351, (float)v2.field_1350).method_39415(color);
            bb.method_22918(ms.method_23760().method_23761(), (float)v3.field_1352, (float)v3.field_1351, (float)v3.field_1350).method_39415(color);
            bb.method_22918(ms.method_23760().method_23761(), (float)v3.field_1352, (float)v3.field_1351, (float)v3.field_1350).method_39415(color);
            bb.method_22918(ms.method_23760().method_23761(), (float)v1.field_1352, (float)v1.field_1351, (float)v1.field_1350).method_39415(color);
         }
      }

      private void drawQuad(class_4587 ms, class_287 bb, class_243 v1, class_243 v2, class_243 v3, class_243 v4, int color, boolean filled) {
         if (filled) {
            this.drawTriangle(ms, bb, v1, v2, v3, color, true);
            this.drawTriangle(ms, bb, v1, v3, v4, color, true);
         } else {
            bb.method_22918(ms.method_23760().method_23761(), (float)v1.field_1352, (float)v1.field_1351, (float)v1.field_1350).method_39415(color);
            bb.method_22918(ms.method_23760().method_23761(), (float)v2.field_1352, (float)v2.field_1351, (float)v2.field_1350).method_39415(color);
            bb.method_22918(ms.method_23760().method_23761(), (float)v2.field_1352, (float)v2.field_1351, (float)v2.field_1350).method_39415(color);
            bb.method_22918(ms.method_23760().method_23761(), (float)v3.field_1352, (float)v3.field_1351, (float)v3.field_1350).method_39415(color);
            bb.method_22918(ms.method_23760().method_23761(), (float)v3.field_1352, (float)v3.field_1351, (float)v3.field_1350).method_39415(color);
            bb.method_22918(ms.method_23760().method_23761(), (float)v4.field_1352, (float)v4.field_1351, (float)v4.field_1350).method_39415(color);
            bb.method_22918(ms.method_23760().method_23761(), (float)v4.field_1352, (float)v4.field_1351, (float)v4.field_1350).method_39415(color);
            bb.method_22918(ms.method_23760().method_23761(), (float)v1.field_1352, (float)v1.field_1351, (float)v1.field_1350).method_39415(color);
         }
      }
   }

   public record Line(class_4665 entry, class_243 start, class_243 end, int colorStart, int colorEnd, float width) {
   }

   public record Quad(class_4665 entry, class_243 x, class_243 y, class_243 w, class_243 z, int color) {
   }

   public record Texture(class_4665 entry, class_2960 id, float x, float y, float width, float height, Vector4i color) {
   }
}
