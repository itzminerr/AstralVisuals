package pl.astralvisuals.utils.display.shape;

import net.minecraft.class_4587;
import org.joml.Vector4f;
import org.joml.Vector4i;
import pl.astralvisuals.utils.display.scale.UiScale;

public class ShapeProperties {
   private class_4587 matrix;
   private float x;
   private float y;
   private float width;
   private float height;
   private float softness;
   private float thickness;
   private float start;
   private float end;
   private float quality;
   private Vector4f round;
   private int outlineColor;
   private Vector4i color;

   private ShapeProperties(
      class_4587 matrix,
      float x,
      float y,
      float width,
      float height,
      float softness,
      float thickness,
      float start,
      float end,
      float quality,
      Vector4f round,
      int outlineColor,
      Vector4i color
   ) {
      this.matrix = matrix;
      this.x = x;
      this.y = y;
      this.width = width;
      this.height = height;
      this.softness = softness;
      this.thickness = thickness;
      this.round = round != null ? round : new Vector4f(0.0F);
      this.outlineColor = outlineColor;
      this.color = color != null ? color : new Vector4i(-1);
      this.start = start;
      this.end = end;
      this.quality = quality;
   }

   public static ShapeProperties.ShapePropertiesBuilder create(class_4587 matrix, double x, double y, double width, double height) {
      return builder()
         .matrix(matrix)
         .x((float)UiScale.x(x))
         .y((float)UiScale.y(y))
         .width((float)UiScale.size(width))
         .height((float)UiScale.size(height));
   }

   private static float $default$quality() {
      return 20.0F;
   }

   private static int $default$outlineColor() {
      return -1;
   }

   public static ShapeProperties.ShapePropertiesBuilder builder() {
      return new ShapeProperties.ShapePropertiesBuilder();
   }

   public ShapeProperties.ShapePropertiesBuilder toBuilder() {
      return new ShapeProperties.ShapePropertiesBuilder()
         .matrix(this.matrix)
         .x(this.x)
         .y(this.y)
         .width(this.width)
         .height(this.height)
         .softness(this.softness)
         .thickness(this.thickness)
         .start(this.start)
         .end(this.end)
         .quality(this.quality)
         .round(this.round)
         .outlineColor(this.outlineColor)
         .color(this.color);
   }

   public class_4587 getMatrix() {
      return this.matrix;
   }

   public float getX() {
      return this.x;
   }

   public float getY() {
      return this.y;
   }

   public float getWidth() {
      return this.width;
   }

   public float getHeight() {
      return this.height;
   }

   public float getSoftness() {
      return this.softness;
   }

   public float getThickness() {
      return this.thickness;
   }

   public float getStart() {
      return this.start;
   }

   public float getEnd() {
      return this.end;
   }

   public float getQuality() {
      return this.quality;
   }

   public Vector4f getRound() {
      return this.round;
   }

   public int getOutlineColor() {
      return this.outlineColor;
   }

   public Vector4i getColor() {
      return this.color;
   }

   public void setMatrix(class_4587 matrix) {
      this.matrix = matrix;
   }

   public void setX(float x) {
      this.x = x;
   }

   public void setY(float y) {
      this.y = y;
   }

   public void setWidth(float width) {
      this.width = width;
   }

   public void setHeight(float height) {
      this.height = height;
   }

   public void setSoftness(float softness) {
      this.softness = softness;
   }

   public void setThickness(float thickness) {
      this.thickness = thickness;
   }

   public void setStart(float start) {
      this.start = start;
   }

   public void setEnd(float end) {
      this.end = end;
   }

   public void setQuality(float quality) {
      this.quality = quality;
   }

   public void setRound(Vector4f round) {
      this.round = round;
   }

   public void setOutlineColor(int outlineColor) {
      this.outlineColor = outlineColor;
   }

   public void setColor(Vector4i color) {
      this.color = color;
   }

   public static class ShapePropertiesBuilder {
      private class_4587 matrix;
      private float x;
      private float y;
      private float width;
      private float height;
      private float softness;
      private float thickness;
      private float start;
      private float end;
      private boolean quality$set;
      private float quality$value;
      private Vector4f round;
      private boolean outlineColor$set;
      private int outlineColor$value;
      private Vector4i color;
      private float quality;
      private int outlineColor;

      public ShapeProperties.ShapePropertiesBuilder color(int color) {
         this.color = new Vector4i(color);
         return this;
      }

      public ShapeProperties.ShapePropertiesBuilder color(Vector4i color) {
         this.color = color;
         return this;
      }

      public ShapeProperties.ShapePropertiesBuilder color(int... color) {
         this.color = new Vector4i(color);
         return this;
      }

      public ShapeProperties.ShapePropertiesBuilder round(float round) {
         this.round = new Vector4f(UiScale.size(round));
         return this;
      }

      public ShapeProperties.ShapePropertiesBuilder round(Vector4f round) {
         this.round = new Vector4f(round).mul(UiScale.scale());
         return this;
      }

      public ShapeProperties.ShapePropertiesBuilder round(float... round) {
         this.round = new Vector4f(round).mul(UiScale.scale());
         return this;
      }

      ShapePropertiesBuilder() {
      }

      public ShapeProperties.ShapePropertiesBuilder matrix(class_4587 matrix) {
         this.matrix = matrix;
         return this;
      }

      public ShapeProperties.ShapePropertiesBuilder x(float x) {
         this.x = x;
         return this;
      }

      public ShapeProperties.ShapePropertiesBuilder y(float y) {
         this.y = y;
         return this;
      }

      public ShapeProperties.ShapePropertiesBuilder width(float width) {
         this.width = width;
         return this;
      }

      public ShapeProperties.ShapePropertiesBuilder height(float height) {
         this.height = height;
         return this;
      }

      public ShapeProperties.ShapePropertiesBuilder softness(float softness) {
         this.softness = UiScale.size(softness);
         return this;
      }

      public ShapeProperties.ShapePropertiesBuilder thickness(float thickness) {
         this.thickness = UiScale.size(thickness);
         return this;
      }

      public ShapeProperties.ShapePropertiesBuilder start(float start) {
         this.start = start;
         return this;
      }

      public ShapeProperties.ShapePropertiesBuilder end(float end) {
         this.end = end;
         return this;
      }

      public ShapeProperties.ShapePropertiesBuilder quality(float quality) {
         this.quality$value = quality;
         this.quality$set = true;
         return this;
      }

      public ShapeProperties.ShapePropertiesBuilder outlineColor(int outlineColor) {
         this.outlineColor$value = outlineColor;
         this.outlineColor$set = true;
         return this;
      }

      public ShapeProperties build() {
         float quality$value = this.quality$value;
         if (!this.quality$set) {
            quality$value = ShapeProperties.$default$quality();
         }

         quality$value *= Math.max(1.0F, UiScale.scale());

         int outlineColor$value = this.outlineColor$value;
         if (!this.outlineColor$set) {
            outlineColor$value = ShapeProperties.$default$outlineColor();
         }

         return new ShapeProperties(
            this.matrix,
            this.x,
            this.y,
            this.width,
            this.height,
            this.softness,
            this.thickness,
            this.start,
            this.end,
            quality$value,
            this.round,
            outlineColor$value,
            this.color
         );
      }

      @Override
      public String toString() {
         return "ShapeProperties.ShapePropertiesBuilder(matrix="
            + this.matrix
            + ", x="
            + this.x
            + ", y="
            + this.y
            + ", width="
            + this.width
            + ", height="
            + this.height
            + ", softness="
            + this.softness
            + ", thickness="
            + this.thickness
            + ", start="
            + this.start
            + ", end="
            + this.end
            + ", quality$value="
            + this.quality$value
            + ", round="
            + this.round
            + ", outlineColor$value="
            + this.outlineColor$value
            + ", color="
            + this.color
            + ")";
      }
   }
}
