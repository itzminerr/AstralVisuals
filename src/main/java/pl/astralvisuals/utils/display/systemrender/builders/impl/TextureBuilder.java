package pl.astralvisuals.utils.display.systemrender.builders.impl;

import net.minecraft.class_1044;
import pl.astralvisuals.utils.display.systemrender.builders.AbstractBuilder;
import pl.astralvisuals.utils.display.systemrender.builders.states.QuadColorState;
import pl.astralvisuals.utils.display.systemrender.builders.states.QuadRadiusState;
import pl.astralvisuals.utils.display.systemrender.builders.states.SizeState;
import pl.astralvisuals.utils.display.systemrender.renderers.impl.BuiltTexture;

public final class TextureBuilder extends AbstractBuilder<BuiltTexture> {
   private SizeState size;
   private QuadRadiusState radius;
   private QuadColorState color;
   private float smoothness;
   private float u;
   private float v;
   private float texWidth;
   private float texHeight;
   private int textureId;

   public TextureBuilder size(SizeState size) {
      this.size = size;
      return this;
   }

   public TextureBuilder radius(QuadRadiusState radius) {
      this.radius = radius;
      return this;
   }

   public TextureBuilder color(QuadColorState color) {
      this.color = color;
      return this;
   }

   public TextureBuilder smoothness(float smoothness) {
      this.smoothness = smoothness;
      return this;
   }

   public TextureBuilder texture(float u, float v, float texWidth, float texHeight, class_1044 texture) {
      return this.texture(u, v, texWidth, texHeight, texture.method_4624());
   }

   public TextureBuilder texture(float u, float v, float texWidth, float texHeight, int textureId) {
      this.u = u;
      this.v = v;
      this.texWidth = texWidth;
      this.texHeight = texHeight;
      this.textureId = textureId;
      return this;
   }

   protected BuiltTexture _build() {
      return new BuiltTexture(this.size, this.radius, this.color, this.smoothness, this.u, this.v, this.texWidth, this.texHeight, this.textureId);
   }

   @Override
   protected void reset() {
      this.size = SizeState.NONE;
      this.radius = QuadRadiusState.NO_ROUND;
      this.color = QuadColorState.WHITE;
      this.smoothness = 1.0F;
      this.u = 0.0F;
      this.v = 0.0F;
      this.texWidth = 0.0F;
      this.texHeight = 0.0F;
      this.textureId = 0;
   }
}
