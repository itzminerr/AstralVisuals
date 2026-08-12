package pl.astralvisuals.utils.display.systemrender.renderers.impl;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.class_10149;
import net.minecraft.class_10156;
import net.minecraft.class_286;
import net.minecraft.class_287;
import net.minecraft.class_289;
import net.minecraft.class_290;
import net.minecraft.class_5944;
import net.minecraft.class_293.class_5596;
import org.joml.Matrix4f;
import pl.astralvisuals.utils.display.atlasfont.providers.ResourceProvider;
import pl.astralvisuals.utils.display.systemrender.builders.states.QuadColorState;
import pl.astralvisuals.utils.display.systemrender.builders.states.QuadRadiusState;
import pl.astralvisuals.utils.display.systemrender.builders.states.SizeState;
import pl.astralvisuals.utils.display.systemrender.renderers.IRenderer;

public record BuiltTexture(
   SizeState size, QuadRadiusState radius, QuadColorState color, float smoothness, float u, float v, float texWidth, float texHeight, int textureId
) implements IRenderer {
   private static final class_10156 TEXTURE_SHADER_KEY = new class_10156(
      ResourceProvider.getShaderIdentifier("texture"), class_290.field_1575, class_10149.field_53930
   );

   @Override
   public void render(Matrix4f matrix, float x, float y, float z) {
      RenderSystem.enableBlend();
      RenderSystem.defaultBlendFunc();
      RenderSystem.disableCull();
      RenderSystem.setShaderTexture(0, this.textureId);
      float width = this.size.width();
      float height = this.size.height();
      class_5944 shader = RenderSystem.setShader(TEXTURE_SHADER_KEY);
      shader.method_34582("Size").method_1255(width, height);
      shader.method_34582("Radius").method_35657(this.radius.radius1(), this.radius.radius2(), this.radius.radius3(), this.radius.radius4());
      shader.method_34582("Smoothness").method_1251(this.smoothness);
      class_287 builder = class_289.method_1348().method_60827(class_5596.field_27382, class_290.field_1575);
      builder.method_22918(matrix, x, y, z).method_22913(this.u, this.v).method_39415(this.color.color1());
      builder.method_22918(matrix, x, y + height, z).method_22913(this.u, this.v + this.texHeight).method_39415(this.color.color2());
      builder.method_22918(matrix, x + width, y + height, z).method_22913(this.u + this.texWidth, this.v + this.texHeight).method_39415(this.color.color3());
      builder.method_22918(matrix, x + width, y, z).method_22913(this.u + this.texWidth, this.v).method_39415(this.color.color4());
      class_286.method_43433(builder.method_60800());
      RenderSystem.setShaderTexture(0, 0);
      RenderSystem.enableCull();
      RenderSystem.disableBlend();
   }
}
