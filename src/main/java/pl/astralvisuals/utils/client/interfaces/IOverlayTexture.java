package pl.astralvisuals.utils.client.interfaces;

// Реализуется миксином на OverlayTexture (class_4608). Позволяет перекрасить строки
// "урона" (v < 8) текстуры оверлея в произвольный ARGB-цвет — это вспышка при ударе.
public interface IOverlayTexture {
   void astral$applyHitColor(int argb);
}
