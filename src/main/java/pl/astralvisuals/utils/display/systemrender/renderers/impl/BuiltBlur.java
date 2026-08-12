package pl.astralvisuals.utils.display.systemrender.renderers.impl;

import com.google.common.base.Supplier;
import com.google.common.base.Suppliers;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.class_10149;
import net.minecraft.class_10156;
import net.minecraft.class_276;
import net.minecraft.class_286;
import net.minecraft.class_287;
import net.minecraft.class_289;
import net.minecraft.class_290;
import net.minecraft.class_310;
import net.minecraft.class_5944;
import net.minecraft.class_6367;
import net.minecraft.class_293.class_5596;
import org.joml.Matrix4f;
import pl.astralvisuals.utils.display.atlasfont.providers.ResourceProvider;
import pl.astralvisuals.utils.display.systemrender.builders.states.QuadColorState;
import pl.astralvisuals.utils.display.systemrender.builders.states.QuadRadiusState;
import pl.astralvisuals.utils.display.systemrender.builders.states.SizeState;
import pl.astralvisuals.utils.display.systemrender.renderers.IRenderer;

public record BuiltBlur(SizeState size, QuadRadiusState radius, QuadColorState color, float smoothness, float blurRadius) implements IRenderer {
   private static final class_10156 BLUR_SHADER_KEY = new class_10156(
      ResourceProvider.getShaderIdentifier("blur"), class_290.field_1576, class_10149.field_53930
   );
   private static final Supplier<class_6367> TEMP_FBO_SUPPLIER = Suppliers.memoize(() -> new class_6367(1, 1, true));

   @Override
   public void render(Matrix4f matrix, float x, float y, float z) {
      class_276 mainFbo = class_310.method_1551().method_1522();
      if (mainFbo != null && mainFbo.field_1482 > 0 && mainFbo.field_1481 > 0) {
         class_6367 tempFbo = (class_6367)TEMP_FBO_SUPPLIER.get();
         if (tempFbo.field_1482 != mainFbo.field_1482 || tempFbo.field_1481 != mainFbo.field_1481) {
            tempFbo.method_1234(mainFbo.field_1482, mainFbo.field_1481);
         }

         RenderSystem.enableBlend();
         RenderSystem.defaultBlendFunc();
         RenderSystem.disableCull();
         tempFbo.method_1235(false);
         mainFbo.method_1237(mainFbo.field_1482, mainFbo.field_1481);
         mainFbo.method_1235(false);
         RenderSystem.setShaderTexture(0, tempFbo.method_30277());
         float width = this.size.width();
         float height = this.size.height();
         class_5944 shader = RenderSystem.setShader(BLUR_SHADER_KEY);
         shader.method_34582("Size").method_1255(width, height);
         shader.method_34582("Radius").method_35657(this.radius.radius1(), this.radius.radius2(), this.radius.radius3(), this.radius.radius4());
         shader.method_34582("Smoothness").method_1251(this.smoothness);
         shader.method_34582("BlurRadius").method_1251(this.blurRadius);
         class_287 builder = class_289.method_1348().method_60827(class_5596.field_27382, class_290.field_1576);
         builder.method_22918(matrix, x, y, z).method_39415(this.color.color1());
         builder.method_22918(matrix, x, y + height, z).method_39415(this.color.color2());
         builder.method_22918(matrix, x + width, y + height, z).method_39415(this.color.color3());
         builder.method_22918(matrix, x + width, y, z).method_39415(this.color.color4());
         class_286.method_43433(builder.method_60800());
         RenderSystem.setShaderTexture(0, 0);
         RenderSystem.enableCull();
         RenderSystem.disableBlend();
      }
   }
}
