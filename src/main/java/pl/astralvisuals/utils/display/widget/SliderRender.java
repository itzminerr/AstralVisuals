package pl.astralvisuals.utils.display.widget;

import com.mojang.blaze3d.systems.RenderSystem;
import java.awt.Color;
import net.minecraft.class_332;
import pl.astralvisuals.utils.display.font.Fonts;
import pl.astralvisuals.utils.display.interfaces.QuickImports;
import pl.astralvisuals.utils.display.shape.ShapeProperties;
import pl.astralvisuals.utils.display.style.GlassStyle;

public class SliderRender implements QuickImports {
   public static void renderSlider(class_332 context, int x, int y, int width, int height, double value, boolean active, String text, float hover) {
      // см. PressableWidgetRender: сбрасываем батч и фиксируем shader color, чтобы слайдер был виден на любом экране.
      context.method_51452();
      RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
      GlassStyle.button(context.method_51448(), x, y, width, height, 9.0F, hover);
      // Ползунок — стеклянная «капля».
      float knobX = (float)(x + value * (width - 8));
      GlassStyle.surface(context.method_51448(), knobX, y + 1.0F, 8.0F, height - 2.0F, 3.5F, hover > 0.5F);
      ShapeProperties knobGlow = ShapeProperties.create(context.method_51448(), knobX + 1.0F, y + 2.0F, 6.0, height - 4)
         .round(3.0F)
         .color(new Color(255, 255, 255, active ? 200 : 90).getRGB())
         .build();
      rectangle.render(knobGlow);
      if (text != null && !text.isEmpty()) {
         Fonts.getSize(18, Fonts.Type.DEFAULT)
            .drawString(
               context.method_51448(),
               text,
               x - Fonts.getSize(18, Fonts.Type.DEFAULT).getStringWidth(text) / 2.0F + width / 2.0F,
               y + 7.0F,
               Color.WHITE.getRGB()
            );
      }
   }
}
