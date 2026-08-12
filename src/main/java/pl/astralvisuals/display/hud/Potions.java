package pl.astralvisuals.display.hud;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Random;
import net.minecraft.class_124;
import net.minecraft.class_1291;
import net.minecraft.class_1293;
import net.minecraft.class_1294;
import net.minecraft.class_2678;
import net.minecraft.class_2718;
import net.minecraft.class_2724;
import net.minecraft.class_2783;
import net.minecraft.class_2960;
import net.minecraft.class_332;
import net.minecraft.class_4587;
import net.minecraft.class_6880;
import net.minecraft.class_7923;
import org.joml.Vector4f;
import pl.astralvisuals.common.animation.Animation;
import pl.astralvisuals.common.animation.Direction;
import pl.astralvisuals.common.animation.implement.Decelerate;
import pl.astralvisuals.events.packet.PacketEvent;
import pl.astralvisuals.utils.client.managers.api.draggable.AbstractDraggable;
import pl.astralvisuals.utils.display.color.ColorAssist;
import pl.astralvisuals.utils.display.font.FontRenderer;
import pl.astralvisuals.utils.display.font.Fonts;
import pl.astralvisuals.utils.display.geometry.Render2D;
import pl.astralvisuals.utils.display.shape.ShapeProperties;
import pl.astralvisuals.utils.display.style.GlassStyle;
import pl.astralvisuals.utils.interactions.interact.PlayerInteractionHelper;
import pl.astralvisuals.utils.math.calc.Calculate;

public class Potions extends AbstractDraggable {
   private final List<Potions.Potion> list = new ArrayList<>();
   private static final class_6880<class_1291>[] NEGATIVE_EFFECTS = new class_6880[]{
      class_1294.field_5899,
      class_1294.field_5920,
      class_1294.field_5916,
      class_1294.field_5919,
      class_1294.field_5903,
      class_1294.field_5909,
      class_1294.field_5901,
      class_1294.field_5921,
      class_1294.field_5911,
      class_1294.field_5902,
      class_1294.field_5908,
      class_1294.field_16595
   };
   private final List<class_6880<class_1291>> previewEffects = new ArrayList<>();
   private final Random random = new Random();
   private long lastEffectChange = 0L;
   private class_6880<class_1291> currentRandomEffect = class_1294.field_5904;

   public Potions() {
      super("Эффекты", 200, 40, 80, 23, true);
   }

   @Override
   public boolean visible() {
      return !this.list.isEmpty() || PlayerInteractionHelper.isChat(mc.field_1755);
   }

   private void loadCurrentEffects() {
      if (mc.field_1724 != null) {
         this.list.clear();

         for (class_1293 effect : mc.field_1724.method_6026()) {
            Animation anim = new Decelerate().setMs(150).setValue(1.0);
            this.list.add(new Potions.Potion(effect, anim));
         }
      }
   }

   @Override
   public void tick() {
      if (mc.field_1724 != null) {
         for (class_1293 activeEffect : mc.field_1724.method_6026()) {
            Potions.Potion existingPotion = null;

            for (Potions.Potion potion : this.list) {
               if (potion.effect.method_5579().method_55840().equals(activeEffect.method_5579().method_55840())) {
                  existingPotion = potion;
                  break;
               }
            }

            if (existingPotion != null) {
               existingPotion.effect = activeEffect;
            } else {
               Animation anim = new Decelerate().setMs(150).setValue(1.0);
               anim.setDirection(Direction.FORWARDS);
               this.list.add(new Potions.Potion(activeEffect, anim));
            }
         }

         List<Potions.Potion> toRemove = new ArrayList<>();

         for (Potions.Potion potionx : this.list) {
            boolean stillActive = false;

            for (class_1293 activeEffect : mc.field_1724.method_6026()) {
               if (activeEffect.method_5579().method_55840().equals(potionx.effect.method_5579().method_55840())) {
                  stillActive = true;
                  break;
               }
            }

            if (!stillActive) {
               potionx.anim.setDirection(Direction.BACKWARDS);
               if (potionx.anim.isFinished(Direction.BACKWARDS)) {
                  toRemove.add(potionx);
               }
            }
         }

         this.list.removeAll(toRemove);
      }

      this.list.removeIf(p -> {
         if (p.effect.method_5584() <= 0 && !p.effect.method_48559()) {
            p.anim.setDirection(Direction.BACKWARDS);
            return true;
         } else {
            return p.anim.isFinished(Direction.BACKWARDS);
         }
      });
      if (this.list.isEmpty() && PlayerInteractionHelper.isChat(mc.field_1755)) {
         long currentTime = System.currentTimeMillis();
         if (currentTime - this.lastEffectChange >= 1000L) {
            this.ensurePreviewEffects();
            if (!this.previewEffects.isEmpty()) {
               this.currentRandomEffect = this.previewEffects.get(this.random.nextInt(this.previewEffects.size()));
               this.lastEffectChange = currentTime;
            }
         }
      }
   }

   @Override
   public void packet(PacketEvent e) {
      switch (e.getPacket()) {
         case class_2783 effect:
            if (!PlayerInteractionHelper.nullCheck() && effect.method_11943() == Objects.requireNonNull(mc.field_1724).method_5628()) {
               class_6880<class_1291> effectId = effect.method_11946();
               this.list
                  .stream()
                  .filter(pxx -> pxx.effect.method_5579().method_55840().equals(effectId.method_55840()))
                  .forEach(s -> s.anim.setDirection(Direction.BACKWARDS));
               class_1293 instance = new class_1293(
                  effectId, effect.method_11944(), effect.method_11945(), effect.method_11950(), effect.method_11949(), effect.method_11942()
               );
               this.list.add(new Potions.Potion(instance, new Decelerate().setMs(150).setValue(1.0)));
            }
            break;
         case class_2718 effectx:
            this.list
               .stream()
                .filter(s -> s.effect.method_5579().method_55840().equals(effectx.comp_2176().method_55840()))
               .forEach(s -> s.anim.setDirection(Direction.BACKWARDS));
            break;
         case class_2724 p:
            this.list.clear();
            this.loadCurrentEffects();
            break;
         case class_2678 px:
            this.list.clear();
            this.loadCurrentEffects();
            break;
         default:
      }
   }

   @Override
   public void drawDraggable(class_332 context) {
      class_4587 matrix = context.method_51448();
      FontRenderer font = Fonts.getSize(13, Fonts.Type.DEFAULT);
      FontRenderer fontPotion = Fonts.getSize(13, Fonts.Type.DEFAULT);
      FontRenderer icon = Fonts.getSize(17, Fonts.Type.ICONS);
      FontRenderer items = Fonts.getSize(12, Fonts.Type.DEFAULT);
      GlassStyle.backdrop(matrix, this.getX(), this.getY(), this.getWidth(), 15.5F, new Vector4f(9.0F, 0.0F, 9.0F, 0.0F));
      icon.drawString(matrix, "C", this.getX() + 5.0F, this.getY() + 6.5F, new Color(225, 225, 255, 255).getRGB());
      font.drawString(matrix, this.getName(), this.getX() + 22, this.getY() + 6.5F, ColorAssist.getText());
      GlassStyle.backdrop(matrix, this.getX(), this.getY() + 16.5F, this.getWidth(), this.getHeight() - 17, new Vector4f(0.0F, 9.0F, 0.0F, 9.0F));
      float centerX = this.getX() + this.getWidth() / 2.0F;
      int offset = 23;
      int maxWidth = 95;
      if (this.list.isEmpty() && PlayerInteractionHelper.isChat(mc.field_1755)) {
         float centerY = this.getY() + offset;
         String name = "Пример эффекта";
         String duration = "**:**";
         int textColor = ColorAssist.getText();
         int textAlpha = 255;
         int colorWithAlpha = ColorAssist.rgba(textColor >> 16 & 0xFF, textColor >> 8 & 0xFF, textColor & 0xFF, textAlpha);
         int color = new Color(225, 225, 255, 255).getRGB();
         int colorWithAlphaRectangle = ColorAssist.rgba(textColor >> 16 & 205, textColor >> 8 & 205, textColor & 205, textAlpha - 125);
         float durationWidth = fontPotion.getStringWidth(duration);
         Calculate.scale(
            matrix,
            centerX,
            centerY,
            1.0F,
            1.0F,
            () -> {
               Render2D.drawSprite(
                  matrix, mc.method_18505().method_18663(this.currentRandomEffect), this.getX() + 3.5F, (int)centerY - 2, 8.0F, 8, colorWithAlpha
               );
               rectangle.render(ShapeProperties.create(matrix, this.getX() + 14, centerY - 1.0F, 0.5, 7.0).color(colorWithAlphaRectangle).build());
               fontPotion.drawString(matrix, name, this.getX() + 18, centerY + 1.0F, colorWithAlpha);
               fontPotion.drawString(matrix, duration, this.getX() + this.getWidth() - durationWidth - 8.0F, centerY + 1.0F, color);
            }
         );
         int width = (int)fontPotion.getStringWidth(name + duration) + 30;
         maxWidth = Math.max(width, maxWidth);
         offset += 11;
      } else {
         for (Potions.Potion potion : this.list) {
            class_1293 effect = potion.effect;
            float animation = potion.anim.getOutput().floatValue();
            float centerY = this.getY() + offset;
            int amplifier = effect.method_5578();
            String name = ((class_1291)effect.method_5579().comp_349()).method_5560().getString();
            String duration = this.getDuration(effect);
            String lvl = amplifier > 0 ? class_124.field_1061 + " " + (amplifier + 1) + class_124.field_1070 : "";
            boolean isBadEffect = this.isBadEffect(effect.method_5579());
            int textColor = isBadEffect ? ColorAssist.rgba(255, 85, 75, 255) : ColorAssist.getText();
            int textAlpha = 255;
            if (effect.method_5584() <= 200 && effect.method_5584() > 0) {
               double output = 0.5 + 0.5 * Math.cos((Math.PI * 2) * (System.currentTimeMillis() % 700L) / 700.0);
               textAlpha = (int)(100.0 + 155.0 * output);
            } else if (effect.method_5584() == 0) {
               textAlpha = 0;
            }

            int colorWithAlpha = isBadEffect
               ? ColorAssist.rgba(255, 85, 75, textAlpha)
               : ColorAssist.rgba(textColor >> 16 & 0xFF, textColor >> 8 & 0xFF, textColor & 0xFF, textAlpha);
            int color = new Color(225, 225, 255, 255).getRGB();
            int colorWithAlphaRectangle = isBadEffect
               ? ColorAssist.rgba(255, 85, 75, textAlpha - 125)
               : ColorAssist.rgba(textColor >> 16 & 205, textColor >> 8 & 205, textColor & 205, textAlpha - 125);
            float durationWidth = fontPotion.getStringWidth(duration);
            Calculate.scale(matrix, centerX, centerY, 1.0F, animation, () -> {
               Render2D.drawSprite(matrix, mc.method_18505().method_18663(effect.method_5579()), this.getX() + 3.5F, (int)centerY - 2, 8.0F, 8, colorWithAlpha);
               rectangle.render(ShapeProperties.create(matrix, this.getX() + 14, centerY - 1.0F, 0.5, 7.0).color(colorWithAlphaRectangle).build());
               fontPotion.drawString(matrix, name, this.getX() + 18, centerY + 1.0F, colorWithAlpha);
               if (amplifier > 0) {
                  String level = " " + (amplifier + 1);
                  fontPotion.drawString(matrix, level, this.getX() + 18 + fontPotion.getStringWidth(name), centerY + 1.0F, colorWithAlpha);
               }

               fontPotion.drawString(matrix, duration, this.getX() + this.getWidth() - durationWidth - 8.0F, centerY + 1.0F, color);
            });
            int width = (int)fontPotion.getStringWidth(name + lvl + duration) + 30;
            maxWidth = Math.max(width, maxWidth);
            offset += (int)(11.0F * animation);
         }
      }

      this.setWidth(maxWidth);
      this.setHeight(offset);
   }

   private String getDuration(class_1293 pe) {
      int var1 = pe.method_5584();
      int mins = var1 / 1200;
      if (!pe.method_48559() && mins <= 60) {
         int seconds = var1 % 1200 / 20;
         return mins + ":" + (seconds < 10 ? "0" : "") + seconds;
      } else {
         return "**:**";
      }
   }

   private void ensurePreviewEffects() {
      if (this.previewEffects.isEmpty()) {
         for (class_2960 id : class_7923.field_41174.method_10235()) {
            class_7923.field_41174.method_10223(id).ifPresent(this.previewEffects::add);
         }
      }
   }

   private boolean isBadEffect(class_6880<class_1291> effect) {
      for (class_6880<class_1291> negativeEffect : NEGATIVE_EFFECTS) {
         if (effect == negativeEffect) {
            return true;
         }
      }

      return false;
   }

   private static class Potion {
      class_1293 effect;
      final Animation anim;

      Potion(class_1293 effect, Animation anim) {
         this.effect = effect;
         this.anim = anim;
      }
   }
}
