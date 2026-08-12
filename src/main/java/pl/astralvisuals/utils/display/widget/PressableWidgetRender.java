package pl.astralvisuals.utils.display.widget;

import com.mojang.blaze3d.systems.RenderSystem;
import java.awt.Color;
import net.minecraft.class_332;
import pl.astralvisuals.utils.display.font.Fonts;
import pl.astralvisuals.utils.display.interfaces.QuickImports;
import pl.astralvisuals.utils.display.style.GlassStyle;

public class PressableWidgetRender implements QuickImports {
   public static void render(class_332 context, int x, int y, int width, int height, boolean active, String text, float hover) {
      // Кнопка рисуется в immediate-режиме (свой шейдер + Tessellator), а ванильный батч DrawContext
      // (включая фон экрана) сбрасывается ПОСЛЕ виджетов и перекрыл бы нашу отрисовку. Поэтому
      // сбрасываем накопленный батч здесь, чтобы кнопка легла поверх фона. setShaderColor(1,1,1,1)
      // нужен, т.к. Rectangle берёт альфу из RenderSystem.getShaderColor() (иначе может быть 0).
      context.method_51452();
      RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
      // Живой hover: кнопка слегка «вырастает» и ярче светится.
      float grow = (active ? hover : 0.0F) * 1.4F;
      GlassStyle.button(context.method_51448(), x - grow, y - grow, width + grow * 2.0F, height + grow * 2.0F, 9.0F, active ? hover : 0.0F);
      if (text != null && !text.isEmpty()) {
         int textColor = active ? new Color(255, 255, 255, 255).getRGB() : new Color(255, 255, 255, 140).getRGB();
         Fonts.getSize(18, Fonts.Type.DEFAULT)
            .drawString(
               context.method_51448(),
               text,
               x - Fonts.getSize(18, Fonts.Type.DEFAULT).getStringWidth(text) / 2.0F + width / 2.0F,
               y + 7.0F,
               textColor
            );
      }
   }
}
