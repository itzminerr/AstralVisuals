package pl.astralvisuals.mixins.client.display.title;

import com.mojang.blaze3d.platform.GlStateManager;
import java.awt.Color;
import net.minecraft.class_2561;
import net.minecraft.class_310;
import net.minecraft.class_327;
import net.minecraft.class_332;
import net.minecraft.class_339;
import net.minecraft.class_364;
import net.minecraft.class_4185;
import net.minecraft.class_437;
import net.minecraft.class_442;
import net.minecraft.class_4587;
import net.minecraft.class_7919;
import net.minecraft.class_8020;
import net.minecraft.class_8519;
import net.minecraft.class_4185.class_4241;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import pl.astralvisuals.display.screens.mainmenu.AltManagerHost;
import pl.astralvisuals.utils.display.shape.ShapeProperties;
import pl.astralvisuals.utils.display.shape.implement.Rectangle;

@Mixin(class_442.class)
public abstract class TitleScreenMixin extends class_437 {
   @Unique
   private static final class_2561 ALT_MANAGER_TEXT = class_2561.method_30163("Смена аккаунта");
   @Unique
   private static final class_8519 FORCE_SPLASH_TEXT = new class_8519("t.me/astralvisuals");
   @Shadow
   private class_8519 field_2586;

   protected TitleScreenMixin(class_2561 title) {
      super(title);
   }

   @Inject(method = "init", at = @At("TAIL"))
   private void addAltManagerButton(CallbackInfo ci) {
      this.field_2586 = FORCE_SPLASH_TEXT;
      int buttonSize = 20;
      int x = this.field_22789 / 2 + 104;
      int y = this.field_22790 / 4 + 96;
      this.method_37063(
          new TitleScreenMixin.AltManagerIconButton(x, y, buttonSize, button -> class_310.method_1551().method_1507(new AltManagerHost((class_442)(Object)this)))
      );

      // Кнопки Language и Accessibility Settings оставляем, но убираем их текстовые подписи.
      for (class_364 child : this.method_25396()) {
         if (child instanceof class_339 w && w.method_25369() != null) {
            String label = w.method_25369().getString().toLowerCase();
            if (label.contains("language") || label.contains("accessibility")
                  || label.contains("язык") || label.contains("специальн") || label.contains("возможност")) {
               w.method_25355(class_2561.method_43473());
            }
         }
      }
   }

   // Вместо панорамы (3D-куб мира) — глубокий неоновый градиент в духе cyber-темы.
   // Перехватываем РОВНО вызов renderPanoramaBackground (method_57728) внутри render:
   // сначала чистим буфер в чёрный, затем рисуем вертикальный градиент
   // (индиго/пурпур сверху → почти чёрный снизу) на весь экран.
   @Redirect(
      method = "method_25394",
      at = @At(value = "INVOKE", target = "Lnet/minecraft/class_442;method_57728(Lnet/minecraft/class_332;F)V"),
      remap = false
   )
   private void astral$blackInsteadOfPanorama(class_442 screen, class_332 context, float delta) {
      GlStateManager._clearColor(0.0F, 0.0F, 0.0F, 1.0F);
      GlStateManager._clear(16384); // GL_COLOR_BUFFER_BIT
      // Вертикальный градиент: верх-лево и верх-право — насыщенный индиго/пурпур,
      // низ — почти чёрный. Цвета углов шейдера round: TL, BL, TR, BR.
      class_4587 matrix = context.method_51448();
      // Размеры экрана берём из Window клиента — это надёжный способ для 1.21.4.
      int screenW = class_310.method_1551().method_22683().method_4486();
      int screenH = class_310.method_1551().method_22683().method_4502();
      // round=0 + thickness=0 → обычный прямоугольник с 4-угловым градиентом.
      new Rectangle().render(
         ShapeProperties.create(matrix, 0.0, 0.0, (double)screenW, (double)screenH)
            .color(
               new Color(28, 16, 58).getRGB(), // TL — индиго-пурпур (верх)
               new Color(2, 2, 6).getRGB(),    // BL — почти чёрный (низ)
               new Color(16, 30, 64).getRGB(), // TR — синий (верх)
               new Color(2, 2, 6).getRGB()     // BR — почти чёрный (низ)
            )
            .build()
      );
   }

   // Убираем логотип Minecraft.
   @Redirect(
      method = "method_25394",
      at = @At(value = "INVOKE", target = "Lnet/minecraft/class_8020;method_48209(Lnet/minecraft/class_332;IF)V"),
      remap = false
   )
   private void astral$noLogo(class_8020 logo, class_332 context, int width, float alpha) {
   }

   // Убираем splash-текст.
   @Redirect(
      method = "method_25394",
      at = @At(value = "INVOKE", target = "Lnet/minecraft/class_8519;method_51453(Lnet/minecraft/class_332;ILnet/minecraft/class_327;I)V"),
      remap = false
   )
   private void astral$noSplash(class_8519 splash, class_332 context, int centerX, class_327 font, int color) {
   }

   // Убираем строку версии "Minecraft 1.21.4" снизу.
   @Redirect(
      method = "method_25394",
      at = @At(value = "INVOKE", target = "Lnet/minecraft/class_332;method_25303(Lnet/minecraft/class_327;Ljava/lang/String;III)I"),
      remap = false
   )
   private int astral$noVersion(class_332 context, class_327 font, String text, int x, int y, int color) {
      return 0;
   }

   private static class AltManagerIconButton extends class_4185 {
      private AltManagerIconButton(int x, int y, int size, class_4241 onPress) {
         super(x, y, size, size, class_2561.method_43473(), onPress, field_40754);
         this.method_47400(class_7919.method_47407(TitleScreenMixin.ALT_MANAGER_TEXT));
      }

      public void method_48589(class_332 context, class_327 textRenderer, int color) {
      }

      protected void method_48579(class_332 context, int mouseX, int mouseY, float delta) {
         super.method_48579(context, mouseX, mouseY, delta);
         this.drawAccountIcon(context, this.method_46426(), this.method_46427(), this.field_22763 ? -2039584 : -6250336);
      }

      private void drawAccountIcon(class_332 context, int x, int y, int color) {
         int shadow = -12632257;
         this.drawIconPixels(context, x + 1, y + 1, shadow);
         this.drawIconPixels(context, x, y, color);
      }

      private void drawIconPixels(class_332 context, int x, int y, int color) {
         context.method_25294(x + 8, y + 4, x + 12, y + 8, color);
         context.method_25294(x + 7, y + 8, x + 13, y + 10, color);
         context.method_25294(x + 5, y + 11, x + 15, y + 14, color);
         context.method_25294(x + 6, y + 14, x + 14, y + 16, color);
      }
   }
}
