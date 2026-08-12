package pl.astralvisuals.utils.display.style;

import java.awt.Color;
import net.minecraft.class_3532;
import net.minecraft.class_4587;
import org.joml.Vector4f;
import pl.astralvisuals.features.impl.render.ClientColor;
import pl.astralvisuals.utils.display.color.ColorAssist;
import pl.astralvisuals.utils.display.interfaces.QuickImports;
import pl.astralvisuals.utils.display.shape.ShapeProperties;

/**
 * Единый стиль "Cyber Neon" — глубокий чёрный с синим отливом, угловатые формы
 * и яркий неоновый акцент (циан → маджента).
 *
 * <p>Визуальный язык, противоположный прежнему «Acrylic Dark»:
 * <ul>
 *   <li>почти чёрные (#06070B) панели с лёгким синим отливом и диагональным градиентом;</li>
 *   <li>угловатые формы — радиусы скругления сжимаются, углы почти прямые (киберпанк);</li>
 *   <li>яркие неоновые рамки (циан), тонкие, со свечением;</li>
 *   <li>двойное неоновое свечение на активных элементах (циан → маджента);</li>
 *   <li>акцент-градиент берётся из Client Color, когда модуль включён.</li>
 * </ul>
 */
public final class GlassStyle implements QuickImports {
   // Тела панелей — диагональный градиент (верх-лево / низ-лево).
   private static final int BG_TOP = new Color(13, 15, 22, 238).getRGB();
   private static final int BG_BOT = new Color(4, 5, 9, 238).getRGB();
   private static final int BG_STRONG_TOP = new Color(9, 10, 16, 246).getRGB();
   private static final int BG_STRONG_BOT = new Color(3, 3, 6, 246).getRGB();
   private static final int SURF_TOP = new Color(18, 21, 30, 232).getRGB();
   private static final int SURF_BOT = new Color(8, 9, 14, 232).getRGB();
   private static final int SURF_HOVER_TOP = new Color(24, 34, 48, 240).getRGB();
   private static final int SURF_HOVER_BOT = new Color(10, 16, 26, 240).getRGB();

   // Неоновые рамки: спокойный циан, на hover — яркий циан.
   private static final int BORDER = new Color(0, 200, 230, 70).getRGB();
   private static final int BORDER_HOVER = new Color(0, 229, 255, 200).getRGB();

   // Неоновый акцент по умолчанию (если Client Color недоступен): циан → маджента.
   public static final int ACCENT_A = new Color(0, 240, 255).getRGB(); // #00F0FF (циан)
   public static final int ACCENT_B = new Color(255, 45, 168).getRGB(); // #FF2DA8 (маджента)
   public static final int ACCENT_MID = new Color(168, 79, 255).getRGB(); // #A84FFF (фиолет)

   // Сжатие радиусов: cyber = угловатые формы.
   private static final float ROUND_SCALE = 0.55F;

   private GlassStyle() {
   }

   // Акцент берётся из Client Color, когда модуль включён (неоновые кнопки меняют цвет вместе с ним).
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

   public static void backdrop(class_4587 matrix, float x, float y, float w, float h, float round) {
      backdrop(matrix, x, y, w, h, new Vector4f(round));
   }

   public static void backdrop(class_4587 matrix, float x, float y, float w, float h, Vector4f round) {
      panel(matrix, x, y, w, h, sharpen(round), BG_TOP, BG_BOT, BORDER);
   }

   public static void strongBackdrop(class_4587 matrix, float x, float y, float w, float h, float round) {
      panel(matrix, x, y, w, h, sharpen(new Vector4f(round)), BG_STRONG_TOP, BG_STRONG_BOT, BORDER);
   }

   public static void surface(class_4587 matrix, float x, float y, float w, float h, float round, boolean hovered) {
      surface(matrix, x, y, w, h, new Vector4f(round), hovered);
   }

   public static void surface(class_4587 matrix, float x, float y, float w, float h, Vector4f round, boolean hovered) {
      int top = hovered ? SURF_HOVER_TOP : SURF_TOP;
      int bot = hovered ? SURF_HOVER_BOT : SURF_BOT;
      int border = hovered ? BORDER_HOVER : BORDER;
      panel(matrix, x, y, w, h, sharpen(round), top, bot, border);
   }

   /**
    * Тёмная кибер-кнопка с плавной hover-анимацией (0..1): тело и рамка плавно вспыхивают неоном.
    */
   public static void button(class_4587 matrix, float x, float y, float w, float h, float round, float hover) {
      hover = class_3532.method_15363(hover, 0.0F, 1.0F);
      int accent = accentMid();
      int top = ColorAssist.interpolateColor(SURF_TOP, SURF_HOVER_TOP, hover);
      int bot = ColorAssist.interpolateColor(SURF_BOT, SURF_HOVER_BOT, hover);
      int border = ColorAssist.interpolateColor(BORDER, accent, hover);
      if (hover > 0.01F) {
         glow(matrix, x, y, w, h, round * ROUND_SCALE, accent, 0.16F * hover);
      }

      panel(matrix, x, y, w, h, sharpen(new Vector4f(round)), top, bot, border);
   }

   /**
    * Активный (неоновый) акцент: сильное двойное свечение (циан + маджента) + неоновая рамка.
    * Используется для включённых модулей.
    */
   public static void accent(class_4587 matrix, float x, float y, float w, float h, float round, float intensity) {
      intensity = class_3532.method_15363(intensity, 0.0F, 1.0F);
      if (intensity <= 0.01F) {
         return;
      }

      // Двухцветное свечение: широкий маджента-ореол + плотный циан-слой.
      int cyan = accentStart();
      int magenta = accentEnd();
      neonGlow(matrix, x, y, w, h, round * ROUND_SCALE, cyan, magenta, 0.30F * intensity);
      rectangle.render(
         ShapeProperties.create(matrix, x, y, w, h)
            .round(round * ROUND_SCALE)
            .thickness(1.2F)
            .outlineColor(ColorAssist.replAlpha(cyan, 0.9F * intensity))
            .color(0)
            .build()
      );
   }

   /**
    * Мягкое неоновое свечение вокруг прямоугольника (через softness, без блюр-шейдера).
    * Два прохода: широкий мягкий ореол + плотный внутренний слой — свечение насыщеннее.
    */
   public static void glow(class_4587 matrix, float x, float y, float w, float h, float round, int color, float alpha) {
      alpha = class_3532.method_15363(alpha, 0.0F, 1.0F);
      // Внешний, широкий и мягкий ореол.
      int outer = ColorAssist.replAlpha(color, alpha * 0.5F);
      rectangle.render(
         ShapeProperties.create(matrix, x - 3.5F, y - 3.5F, w + 7.0F, h + 7.0F).round(round + 3.5F).softness(6.0F).color(outer).build()
      );
      // Внутренний, более плотный слой у самой рамки.
      int inner = ColorAssist.replAlpha(color, alpha * 0.8F);
      rectangle.render(
         ShapeProperties.create(matrix, x - 1.5F, y - 1.5F, w + 3.0F, h + 3.0F).round(round + 1.5F).softness(2.5F).color(inner).build()
      );
   }

   /** Двухцветное неоновое свечение: широкий ореол цветом outerColor + плотный слой color. */
   private static void neonGlow(class_4587 matrix, float x, float y, float w, float h, float round, int color, int outerColor, float alpha) {
      alpha = class_3532.method_15363(alpha, 0.0F, 1.0F);
      // Широкий размытый ореол вторым цветом (маджента) — цветная подложка.
      int halo = ColorAssist.replAlpha(outerColor, alpha * 0.45F);
      rectangle.render(
         ShapeProperties.create(matrix, x - 4.5F, y - 4.5F, w + 9.0F, h + 9.0F).round(round + 4.5F).softness(7.5F).color(halo).build()
      );
      // Плотный неоновый слой основным цветом (циан).
      int core = ColorAssist.replAlpha(color, alpha * 0.8F);
      rectangle.render(
         ShapeProperties.create(matrix, x - 1.5F, y - 1.5F, w + 3.0F, h + 3.0F).round(round + 1.5F).softness(2.5F).color(core).build()
      );
   }

   /** Панель с диагональным градиентом объёма и тонкой неоновой рамкой. */
   private static void panel(class_4587 matrix, float x, float y, float w, float h, Vector4f round, int top, int bot, int border) {
      // Диагональный градиент: верх-лево = top, низ-лево = bot, правая сторона чуть темнее.
      int topRight = ColorAssist.multDark(top, 0.96F);
      int botRight = ColorAssist.multDark(bot, 0.9F);
      rectangle.render(ShapeProperties.create(matrix, x, y, w, h).round(round).color(top, bot, topRight, botRight).build());
      rectangle.render(ShapeProperties.create(matrix, x, y, w, h).round(round).thickness(1.0F).outlineColor(border).color(0).build());
   }

   /** Сжимает радиусы скругления — углы становятся острее (киберпанк-эстетика). */
   private static Vector4f sharpen(Vector4f round) {
      if (round == null) {
         return new Vector4f(0.0F);
      }
      return new Vector4f(
         Math.max(0.0F, round.x * ROUND_SCALE),
         Math.max(0.0F, round.y * ROUND_SCALE),
         Math.max(0.0F, round.z * ROUND_SCALE),
         Math.max(0.0F, round.w * ROUND_SCALE)
      );
   }
}
