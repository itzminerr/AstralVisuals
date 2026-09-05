package pl.astralvisuals.utils.display.style;

import java.awt.Color;
import net.minecraft.class_3532;
import net.minecraft.class_4587;
import org.joml.Vector4f;
import pl.astralvisuals.features.impl.render.ClientColor;
import pl.astralvisuals.features.impl.render.Interface;
import pl.astralvisuals.utils.display.color.ColorAssist;
import pl.astralvisuals.utils.display.interfaces.QuickImports;
import pl.astralvisuals.utils.display.shape.ShapeProperties;

/**
 * Единый стеклянный стиль интерфейса (как в Kimiko): матовое стекло с размытием фона,
 * полупрозрачная градиентная тонировка из настраиваемых цветов клиента и мягкая светлая рамка.
 *
 * <p>Визуальный язык:
 * <ul>
 *   <li>панели — размытый фон мира + плотная тонировка цветом стекла (диагональный градиент);</li>
 *   <li>скругление полное, множитель настраивается в модуле Interface;</li>
 *   <li>тонкая светлая рамка и опциональное свечение вокруг панелей;</li>
 *   <li>вложенные поверхности — лёгкие полупрозрачные карточки поверх стекла;</li>
 *   <li>акцент берётся из Client Color для включённых модулей и активных элементов.</li>
 * </ul>
 */
public final class GlassStyle implements QuickImports {
   // Резервные цвета стекла (если модуль Interface ещё не создан).
   private static final int FALLBACK_PRIMARY = new Color(221, 231, 255).getRGB();
   private static final int FALLBACK_SECONDARY = new Color(255, 50, 150).getRGB();

   // Плотность тонировки по ролям панелей (умножается на «Плотность стекла»).
   private static final float TINT_BACKDROP = 0.80F;
   private static final float TINT_STRONG = 0.97F;

   // Вложенные карточки поверх стекла: тёмная полупрозрачная подложка под текст.
   private static final int SURF_TOP = new Color(15, 18, 26, 86).getRGB();
   private static final int SURF_BOT = new Color(7, 9, 14, 98).getRGB();
   private static final int SURF_HOVER_TOP = new Color(32, 42, 62, 122).getRGB();
   private static final int SURF_HOVER_BOT = new Color(16, 22, 36, 134).getRGB();
   private static final int SURF_BORDER = new Color(255, 255, 255, 38).getRGB();
   private static final int SURF_BORDER_HOVER = new Color(255, 255, 255, 74).getRGB();

   // Акцент по умолчанию (если Client Color недоступен): циан → маджента.
   public static final int ACCENT_A = new Color(0, 240, 255).getRGB();
   public static final int ACCENT_B = new Color(255, 45, 168).getRGB();
   public static final int ACCENT_MID = new Color(168, 79, 255).getRGB();

   private GlassStyle() {
   }

   // Акцент берётся из Client Color, когда модуль включён.
   public static int accentStart() {
      ClientColor c = ClientColor.getInstance();
      return c != null && c.isState() ? c.getStart() : ACCENT_A;
   }

   public static int accentEnd() {
      ClientColor c = ClientColor.getInstance();
      return c != null && c.isState() ? c.getEnd() : ACCENT_B;
   }

   public static int accentMid() {
      ClientColor c = ClientColor.getInstance();
      return c != null && c.isState() ? c.getMid() : ACCENT_MID;
   }

   // --- Настройки стекла из модуля Interface (null-безопасно) ---

   private static Interface iface() {
      return Interface.getInstance();
   }

   /** Множитель скругления углов (0..1). */
   public static float roundScale() {
      Interface i = iface();
      return i == null ? 0.5F : class_3532.method_15363(i.getRoundScale(), 0.0F, 1.0F);
   }

   /** Радиус размытия фона за стеклом. */
   public static float backdropBlur() {
      Interface i = iface();
      return i == null ? 18.0F : Math.max(0.0F, i.getBackdropBlur());
   }

   /** Плотность перекрытия фона тонировкой (0.25..1). */
   public static float glassDensity() {
      Interface i = iface();
      return i == null ? 0.78F : class_3532.method_15363(i.getGlassDensity(), 0.1F, 1.0F);
   }

   /** Основной цвет стекла. */
   public static int glassPrimary() {
      Interface i = iface();
      int raw = i == null ? FALLBACK_PRIMARY : i.colorSetting.rawColor();
      return raw & 0xFFFFFF;
   }

   /** Второй цвет стекла (градиент). */
   public static int glassSecondary() {
      Interface i = iface();
      int raw = i == null || !i.isSecondColor() ? FALLBACK_SECONDARY : i.secondGlassColor.rawColor();
      return raw & 0xFFFFFF;
   }

   public static boolean isGlow() {
      Interface i = iface();
      return i != null && i.isGlow();
   }

   public static float glowStrength() {
      Interface i = iface();
      return i == null ? 0.4F : class_3532.method_15363(i.getGlowIntensity(), 0.0F, 1.0F);
   }

   // --- Панели ---

   public static void backdrop(class_4587 matrix, float x, float y, float w, float h, float round) {
      backdrop(matrix, x, y, w, h, new Vector4f(round));
   }

   public static void backdrop(class_4587 matrix, float x, float y, float w, float h, Vector4f round) {
      glassPanel(matrix, x, y, w, h, round, TINT_BACKDROP);
   }

   public static void strongBackdrop(class_4587 matrix, float x, float y, float w, float h, float round) {
      glassPanel(matrix, x, y, w, h, new Vector4f(round), TINT_STRONG);
   }

   public static void surface(class_4587 matrix, float x, float y, float w, float h, float round, boolean hovered) {
      surface(matrix, x, y, w, h, new Vector4f(round), hovered);
   }

   /**
    * Вложенная поверхность поверх стекла: полупрозрачная карточка без собственного размытия.
    */
    public static void surface(class_4587 matrix, float x, float y, float w, float h, Vector4f round, boolean hovered) {
      Vector4f r = fitRound(scaleRound(round), w, h);
      int top = hovered ? SURF_HOVER_TOP : SURF_TOP;
      int bot = hovered ? SURF_HOVER_BOT : SURF_BOT;
      int topRight = ColorAssist.multDark(top, 0.94F);
      int botRight = ColorAssist.multDark(bot, 0.92F);
      if (hovered) {
         glow(matrix, x, y, w, h, r.x, accentMid(), 0.08F);
      }

      rectangle.render(
         ShapeProperties.create(matrix, x, y, w, h)
            .round(r)
            .color(top, bot, topRight, botRight)
            .build()
      );
      rectangle.render(
         ShapeProperties.create(matrix, x, y, w, h)
            .round(r)
            .thickness(1.0F)
            .outlineColor(hovered ? SURF_BORDER_HOVER : SURF_BORDER)
            .color(0)
            .build()
      );
   }

   /**
    * Стеклянная кнопка/ползунок с плавной hover-анимацией (0..1): тело и рамка плавно вспыхивают акцентом.
    */
   public static void button(class_4587 matrix, float x, float y, float w, float h, float round, float hover) {
      hover = class_3532.method_15363(hover, 0.0F, 1.0F);
      Vector4f r = fitRound(scaleRound(new Vector4f(round)), w, h);
      int accent = accentMid();
      int top = ColorAssist.interpolateColor(SURF_TOP, SURF_HOVER_TOP, hover);
      int bot = ColorAssist.interpolateColor(SURF_BOT, SURF_HOVER_BOT, hover);
      int border = ColorAssist.interpolateColor(SURF_BORDER, accent, hover * 0.75F);
      if (hover > 0.01F) {
         glow(matrix, x, y, w, h, r.x, accent, 0.12F * hover);
      }

      rectangle.render(
         ShapeProperties.create(matrix, x, y, w, h)
            .round(r)
            .color(top, bot, ColorAssist.multDark(top, 0.94F), ColorAssist.multDark(bot, 0.92F))
            .build()
      );
      rectangle.render(
         ShapeProperties.create(matrix, x, y, w, h)
            .round(r)
            .thickness(1.0F)
            .outlineColor(border)
            .color(0)
            .build()
      );
   }

   /**
    * Активный акцент включённых модулей: мягкое двухцветное свечение + тонкая неоновая рамка.
    */
   public static void accent(class_4587 matrix, float x, float y, float w, float h, float round, float intensity) {
      intensity = class_3532.method_15363(intensity, 0.0F, 1.0F);
      if (intensity <= 0.01F) {
         return;
      }

      int cyan = accentStart();
      int magenta = accentEnd();
      neonGlow(matrix, x, y, w, h, scaleRound(new Vector4f(round)).x, cyan, magenta, 0.28F * intensity);
      rectangle.render(
         ShapeProperties.create(matrix, x, y, w, h)
            .round(scaleRound(new Vector4f(round)))
            .thickness(1.1F)
            .outlineColor(ColorAssist.replAlpha(cyan, 0.85F * intensity))
            .color(0)
            .build()
      );
   }

   /**
     * Мягкое свечение вокруг прямоугольника (через softness, без блюр-шейдера).
     * Два прохода: широкий мягкий ореол + плотный внутренний слой.
     */
   public static void glow(class_4587 matrix, float x, float y, float w, float h, float round, int color, float alpha) {
      alpha = class_3532.method_15363(alpha, 0.0F, 1.0F);
      int outer = ColorAssist.replAlpha(color, alpha * 0.5F);
      rectangle.render(
         ShapeProperties.create(matrix, x - 3.5F, y - 3.5F, w + 7.0F, h + 7.0F).round(round + 3.5F).softness(6.0F).color(outer).build()
      );
      int inner = ColorAssist.replAlpha(color, alpha * 0.8F);
      rectangle.render(
         ShapeProperties.create(matrix, x - 1.5F, y - 1.5F, w + 3.0F, h + 3.0F).round(round + 1.5F).softness(2.5F).color(inner).build()
      );
   }

   /** Двухцветное свечение: широкий ореол цветом outerColor + плотный слой color. */
   private static void neonGlow(class_4587 matrix, float x, float y, float w, float h, float round, int color, int outerColor, float alpha) {
      alpha = class_3532.method_15363(alpha, 0.0F, 1.0F);
      int halo = ColorAssist.replAlpha(outerColor, alpha * 0.4F);
      rectangle.render(
         ShapeProperties.create(matrix, x - 4.0F, y - 4.0F, w + 8.0F, h + 8.0F).round(round + 4.0F).softness(7.0F).color(halo).build()
      );
      int core = ColorAssist.replAlpha(color, alpha * 0.75F);
      rectangle.render(
         ShapeProperties.create(matrix, x - 1.5F, y - 1.5F, w + 3.0F, h + 3.0F).round(round + 1.5F).softness(2.5F).color(core).build()
      );
   }

   /**
    * Ядро стиля: матовое стекло = захваченный фон с размытием + градиентная тонировка
    * цветами клиента + тонкая светлая рамка (+ опциональное свечение).
    */
   private static void glassPanel(class_4587 matrix, float x, float y, float w, float h, Vector4f round, float tintRole) {
      Vector4f r = scaleRound(round);
      float tint = class_3532.method_15363(glassDensity() * tintRole, 0.02F, 1.0F);
      float globalAlpha = com.mojang.blaze3d.systems.RenderSystem.getShaderColor()[3];
      if (tint <= 0.004F) {
         return;
      }

      if (isGlow()) {
         float g = glowStrength();
         if (g > 0.01F) {
            int primary = glassPrimary();
            glow(matrix, x, y, w, h, r.x, primary, 0.20F * g);
         }
      }

      float blurRadius = backdropBlur();
      // Настоящее матовое стекло рисует фон непрозрачным: блюр-шейдер не умножает свои пиксели
      // на глобальную альфу, поэтому при уходе панели (анимации появления/исчезновения) остался бы
      // «айсберг» — матовый квадрат висит дольше содержимого и пропадает рывком. Используем блюр
      // только при почти полной непрозрачности, а во время любых fade-анимаций рисуем обычную
      // тонировку: у прямоугольника альфа умножается на глобальную и стекло гаснет вместе с текстом.
      boolean canBlur = blurRadius >= 1.0F && blur.input != null && globalAlpha >= 0.98F;
      if (canBlur) {
         float animatedBlur = Math.max(1.0F, blurRadius * class_3532.method_15363(globalAlpha, 0.0F, 1.0F));
         blur.render(
            ShapeProperties.create(matrix, x, y, w, h)
               .round(r)
               .quality(animatedBlur)
               .color(tintCorners(tint))
               .build()
         );
      } else {
         // Размытие выключено или кадр не захвачен — плотная полупрозрачная тонировка.
         int[] corners = tintCorners(tint);
         rectangle.render(
            ShapeProperties.create(matrix, x, y, w, h)
               .round(r)
               .color(corners[0], corners[1], corners[2], corners[3])
               .build()
         );
      }

      // Тонкая светлая рамка — грань стекла.
      rectangle.render(
         ShapeProperties.create(matrix, x, y, w, h)
            .round(r)
            .thickness(1.0F)
            .outlineColor(ColorAssist.replAlpha(panelBorderColor(), 0.16F))
            .color(0)
            .build()
      );
   }

   /** Цвета четырёх углов тонировки: диагональный градиент основного/второго цвета. */
   private static int[] tintCorners(float tint) {
      int primary = glassPrimary();
      int second = glassSecondary();
      boolean useSecond = iface() != null && iface().isSecondColor();
      int topLeft = ColorAssist.replAlpha(primary, tint);
      int bottomLeft = ColorAssist.replAlpha(ColorAssist.multDark(primary, 0.82F), tint);
      int topRight = ColorAssist.replAlpha(useSecond ? second : ColorAssist.multBright(primary, 1.08F), tint);
      int bottomRight = ColorAssist.replAlpha(useSecond ? ColorAssist.multDark(second, 0.82F) : ColorAssist.multDark(primary, 0.88F), tint);
      // Shader: color1 = верх-лево, color2 = низ-лево, color3 = верх-право, color4 = низ-право.
      return new int[]{topLeft, bottomLeft, topRight, bottomRight};
   }

   private static int panelBorderColor() {
      return ColorAssist.multBright(glassPrimary(), 1.1F);
   }

   /** Полное скругление, умноженное на настройку «Скругление углов». */
   private static Vector4f scaleRound(Vector4f round) {
      if (round == null) {
         return new Vector4f(0.0F);
      }
      return new Vector4f(round).mul(roundScale());
   }

   /**
    * Ограничивает скругление половиной меньшей стороны фигуры.
    * Без этого при большом «Скруглении углов» радиус превышает половину стороны
    * и шейдер рисует угловатый «перехлёст» вместо круга.
    */
   private static Vector4f fitRound(Vector4f round, float w, float h) {
      float max = Math.max(0.0F, Math.min(w, h) * 0.5F);
      return new Vector4f(
         class_3532.method_15363(round.x, 0.0F, max),
         class_3532.method_15363(round.y, 0.0F, max),
         class_3532.method_15363(round.z, 0.0F, max),
         class_3532.method_15363(round.w, 0.0F, max)
      );
   }

   /** Скругление + ограничение по размеру для одной цифры (свечение и т.п.). */
   private static float fitRound(float round, float w, float h) {
      return class_3532.method_15363(round, 0.0F, Math.max(0.0F, Math.min(w, h) * 0.5F));
   }
}
