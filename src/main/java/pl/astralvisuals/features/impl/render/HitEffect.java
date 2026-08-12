package pl.astralvisuals.features.impl.render;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import net.minecraft.class_1511;
import net.minecraft.class_238;
import net.minecraft.class_2338;
import net.minecraft.class_243;
import net.minecraft.class_265;
import net.minecraft.class_2680;
import pl.astralvisuals.events.player.AttackEvent;
import pl.astralvisuals.events.render.WorldRenderEvent;
import pl.astralvisuals.features.module.Module;
import pl.astralvisuals.features.module.ModuleCategory;
import pl.astralvisuals.features.module.setting.implement.BooleanSetting;
import pl.astralvisuals.features.module.setting.implement.ColorSetting;
import pl.astralvisuals.utils.client.Instance;
import pl.astralvisuals.utils.client.managers.event.EventHandler;
import pl.astralvisuals.utils.display.color.ColorAssist;
import pl.astralvisuals.utils.display.geometry.Render3D;

public class HitEffect extends Module {
   private final List<WaveEffect> waveEffects = new ArrayList<>();
   private final ColorSetting colorSetting = new ColorSetting("Цвет", "Цвет волны от удара")
      .setColor(-7722014)
      .presets(-1, -65536, -23178, -16711936, -16776961, -7722014);
   private final BooleanSetting crystalSetting = new BooleanSetting("При взрыве кристалла", "Создавать волну при взрыве энд-кристалла").setValue(false);

   public static HitEffect getInstance() {
      return Instance.get(HitEffect.class);
   }

   public HitEffect() {
      super("HitEffect", "Hit Effect", ModuleCategory.RENDER);
      this.setup(this.colorSetting, this.crystalSetting);
   }

   @EventHandler
   public void onAttack(AttackEvent e) {
      if (e.getEntity() == null) {
         return;
      }

      // Волны для энд-кристаллов управляются настройкой "При взрыве кристалла" и создаются
      // при взрыве (ClientWorldMixin.removeEntity), а не при прямом ударе по кристаллу.
      if (e.getEntity() instanceof class_1511) {
         return;
      }

      class_243 pos = e.getEntity().method_19538();
      class_2338 basePos = class_2338.method_49637(pos.field_1352, pos.field_1351 - 0.1, pos.field_1350);
      this.addWave(basePos);
   }

   public boolean isCrystalWaveEnabled() {
      return this.crystalSetting.isValue();
   }

   public void addWave(class_2338 pos) {
      if (mc.field_1687 != null) {
         this.waveEffects.add(new WaveEffect(pos, System.currentTimeMillis()));
      }
   }

   @EventHandler
   public void onWorldRender(WorldRenderEvent e) {
      if (this.waveEffects.isEmpty() || mc.field_1687 == null) {
         return;
      }

      Iterator<WaveEffect> iterator = this.waveEffects.iterator();
      while (iterator.hasNext()) {
         WaveEffect wave = iterator.next();
         if (wave.isExpired()) {
            iterator.remove();
         } else {
            wave.render();
         }
      }
   }

   private class WaveEffect {
      private final class_2338 centerPos;
      private final long startTime;
      private final long duration = 1500L;
      private final int maxRadius = 12;

      WaveEffect(class_2338 centerPos, long startTime) {
         this.centerPos = centerPos;
         this.startTime = startTime;
      }

      boolean isExpired() {
         return System.currentTimeMillis() - this.startTime > this.duration;
      }

      void render() {
         if (mc.field_1687 == null) {
            return;
         }

         long elapsed = System.currentTimeMillis() - this.startTime;
         float progress = (float)elapsed / this.duration;
         float currentRadius = progress * this.maxRadius;
         float waveWidth = 2.5F;
         float globalAlpha = (float)Math.pow(1.0F - progress, 0.6);
         int rendered = 0;
         int maxPerFrame = 400;
         float minRadSq = (currentRadius - waveWidth) * (currentRadius - waveWidth);
         float maxRadSq = (currentRadius + 0.5F) * (currentRadius + 0.5F);
         int clientColor = HitEffect.this.colorSetting.getColor();

         for (int x = -this.maxRadius; x <= this.maxRadius; x++) {
            for (int z = -this.maxRadius; z <= this.maxRadius; z++) {
               if (rendered >= maxPerFrame) {
                  return;
               }

               float distSq = (float)(x * x + z * z);
               if (!(distSq < minRadSq) && !(distSq > maxRadSq)) {
                  class_2338 checkPos = this.centerPos.method_10069(x, 0, z);
                  class_2338 renderPos = this.findSurface(checkPos);
                  if (renderPos != null) {
                     class_2680 state = mc.field_1687.method_8320(renderPos);
                     class_265 shape = state.method_26218(mc.field_1687, renderPos);
                     if (!shape.method_1110()) {
                        rendered++;
                        float distance = (float)Math.sqrt(distSq);
                        float localAlpha = 1.0F - Math.abs(distance - currentRadius) / waveWidth;
                        localAlpha = Math.max(0.0F, Math.min(1.0F, localAlpha)) * globalAlpha;
                        if (localAlpha > 0.05F) {
                           int color = ColorAssist.setAlpha(clientColor, (int)(localAlpha * 255.0F));
                           float lineWidth = 1.0F + localAlpha * 2.5F;

                           try {
                              // Обводим только ВЕРХНЮЮ грань блока (4 ребра по верху), а не весь куб.
                              class_238 box = shape.method_1107();
                              double minX = renderPos.method_10263() + box.field_1323;
                              double maxX = renderPos.method_10263() + box.field_1320;
                              double topY = renderPos.method_10264() + box.field_1325;
                              double minZ = renderPos.method_10260() + box.field_1321;
                              double maxZ = renderPos.method_10260() + box.field_1324;
                              class_243 c1 = new class_243(minX, topY, minZ);
                              class_243 c2 = new class_243(maxX, topY, minZ);
                              class_243 c3 = new class_243(maxX, topY, maxZ);
                              class_243 c4 = new class_243(minX, topY, maxZ);
                              Render3D.drawLine(c1, c2, color, lineWidth, true);
                              Render3D.drawLine(c2, c3, color, lineWidth, true);
                              Render3D.drawLine(c3, c4, color, lineWidth, true);
                              Render3D.drawLine(c4, c1, color, lineWidth, true);
                           } catch (Exception var20) {
                           }
                        }
                     }
                  }
               }
            }
         }
      }

      private class_2338 findSurface(class_2338 pos) {
         for (int y = 2; y >= -4; y--) {
            class_2338 p = pos.method_10086(y);
            if (!mc.field_1687.method_8320(p).method_26215() && mc.field_1687.method_8320(p.method_10084()).method_26215()) {
               return p;
            }
         }

         return null;
      }
   }
}
