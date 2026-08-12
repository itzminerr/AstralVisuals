package pl.astralvisuals.utils.display.shape.implement;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.class_10149;
import net.minecraft.class_10156;
import net.minecraft.class_286;
import net.minecraft.class_287;
import net.minecraft.class_290;
import net.minecraft.class_2960;
import net.minecraft.class_5944;
import net.minecraft.class_293.class_5596;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector4f;
import pl.astralvisuals.utils.display.color.ColorAssist;
import pl.astralvisuals.utils.display.interfaces.QuickImports;
import pl.astralvisuals.utils.display.shape.Shape;
import pl.astralvisuals.utils.display.shape.ShapeProperties;

public class Rectangle implements Shape, QuickImports {
   private final class_10156 SHADER_KEY = new class_10156(class_2960.method_60655("minecraft", "core/round"), class_290.field_1592, class_10149.field_53930);

   @Override
   public void render(ShapeProperties shape) {
      RenderSystem.enableBlend();
      RenderSystem.defaultBlendFunc();
      RenderSystem.enableDepthTest();
      RenderSystem.enableCull();
      float scale = (float)mc.method_22683().method_4495();
      float alpha = RenderSystem.getShaderColor()[3];
      Matrix4f matrix4f = shape.getMatrix().method_23760().method_23761();
      Vector3f pos = matrix4f.transformPosition(shape.getX(), shape.getY(), 0.0F, new Vector3f()).mul(scale);
      Vector3f size = matrix4f.getScale(new Vector3f()).mul(scale);
      Vector4f round = shape.getRound().mul(size.y);
      float softness = shape.getSoftness() * alpha;
      float thickness = shape.getThickness();
      float width = shape.getWidth() * size.x;
      float height = shape.getHeight() * size.y;
      class_287 buffer = tessellator.method_60827(class_5596.field_27382, class_290.field_1592);
      drawEngine.quad(
         matrix4f, buffer, shape.getX() - softness / 2.0F, shape.getY() - softness / 2.0F, shape.getWidth() + softness, shape.getHeight() + softness
      );
      class_5944 shader = RenderSystem.setShader(this.SHADER_KEY);
      shader.method_35785("size").method_1255(width, height);
      shader.method_35785("location").method_1255(pos.x, window.method_4507() - height - pos.y);
      shader.method_35785("radius").method_35652(round);
      shader.method_35785("softness").method_1251(softness);
      shader.method_35785("thickness").method_1251(thickness);
      shader.method_35785("color1")
         .method_35657(
            ColorAssist.redf(shape.getColor().x),
            ColorAssist.greenf(shape.getColor().x),
            ColorAssist.bluef(shape.getColor().x),
            ColorAssist.alphaf(ColorAssist.multAlpha(shape.getColor().x, alpha))
         );
      shader.method_35785("color2")
         .method_35657(
            ColorAssist.redf(shape.getColor().y),
            ColorAssist.greenf(shape.getColor().y),
            ColorAssist.bluef(shape.getColor().y),
            ColorAssist.alphaf(ColorAssist.multAlpha(shape.getColor().y, alpha))
         );
      shader.method_35785("color3")
         .method_35657(
            ColorAssist.redf(shape.getColor().z),
            ColorAssist.greenf(shape.getColor().z),
            ColorAssist.bluef(shape.getColor().z),
            ColorAssist.alphaf(ColorAssist.multAlpha(shape.getColor().z, alpha))
         );
      shader.method_35785("color4")
         .method_35657(
            ColorAssist.redf(shape.getColor().w),
            ColorAssist.greenf(shape.getColor().w),
            ColorAssist.bluef(shape.getColor().w),
            ColorAssist.alphaf(ColorAssist.multAlpha(shape.getColor().w, alpha))
         );
      shader.method_35785("outlineColor")
         .method_35657(
            ColorAssist.redf(shape.getOutlineColor()),
            ColorAssist.greenf(shape.getOutlineColor()),
            ColorAssist.bluef(shape.getOutlineColor()),
            ColorAssist.alphaf(ColorAssist.multAlpha(shape.getOutlineColor(), alpha))
         );
      class_286.method_43433(buffer.method_60800());
      RenderSystem.disableBlend();
   }
}
