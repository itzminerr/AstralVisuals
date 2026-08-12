package pl.astralvisuals.mixins.game.render;

import net.minecraft.class_1011;
import net.minecraft.class_1043;
import net.minecraft.class_4608;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import pl.astralvisuals.utils.client.interfaces.IOverlayTexture;

// class_4608 = OverlayTexture. Строки v < 8 текстуры оверлея — вспышка урона (ванильно ARGB(178,255,0,0)).
// Перекрашиваем их в кастомный ARGB-цвет. Не-битые сущности берут строку v=10, поэтому эффект виден
// только "при ударе". Драйвится покадрово из GameRendererMixin (методы setup/teardownOverlayColor в 1.21.4
// мертвы — не вызываются). Имена intermediary (проект собирается с -proc:none).
@Mixin(class_4608.class)
public class OverlayTextureMixin implements IOverlayTexture {
   @Shadow
   @Final
   private class_1043 field_21013;
   @Unique
   private int astral$lastColor = 0xB2FF0000;

   @Override
   public void astral$applyHitColor(int argb) {
      if (argb == this.astral$lastColor) {
         return;
      }

      class_1011 image = this.field_21013.method_4525();
      if (image == null) {
         return;
      }

      for (int v = 0; v < 8; v++) {
         for (int u = 0; u < 16; u++) {
            image.method_61941(u, v, argb);
         }
      }

      this.field_21013.method_4524();
      this.astral$lastColor = argb;
   }
}
