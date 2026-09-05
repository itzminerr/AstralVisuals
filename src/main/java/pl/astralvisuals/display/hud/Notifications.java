package pl.astralvisuals.display.hud;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import net.minecraft.class_2561;
import net.minecraft.class_332;
import net.minecraft.class_3414;
import net.minecraft.class_408;
import net.minecraft.class_4587;
import pl.astralvisuals.common.animation.Animation;
import pl.astralvisuals.common.animation.Direction;
import pl.astralvisuals.common.animation.implement.InOutCirc;
import pl.astralvisuals.events.container.SetScreenEvent;
import pl.astralvisuals.events.packet.PacketEvent;
import pl.astralvisuals.utils.client.Instance;
import pl.astralvisuals.utils.client.managers.api.draggable.AbstractDraggable;
import pl.astralvisuals.utils.client.sound.SoundManager;
import pl.astralvisuals.utils.display.font.FontRenderer;
import pl.astralvisuals.utils.display.font.Fonts;
import pl.astralvisuals.utils.display.shape.ShapeProperties;
import pl.astralvisuals.utils.display.style.GlassStyle;
import pl.astralvisuals.utils.interactions.interact.PlayerInteractionHelper;
import pl.astralvisuals.utils.math.calc.Calculate;

public class Notifications extends AbstractDraggable {
   private final List<Notifications.Notification> list = new ArrayList<>();

   public static Notifications getInstance() {
      return Instance.getDraggable(Notifications.class);
   }

   public Notifications() {
      super("Уведомления", 0, 0, 100, 15, false);
   }

   @Override
   public void tick() {
      this.list
         .forEach(
            notif -> {
               if (System.currentTimeMillis() > notif.removeTime
                  || notif.text.getString().contains("Пример уведомления") && !PlayerInteractionHelper.isChat(mc.field_1755)) {
                  notif.anim.setDirection(Direction.BACKWARDS);
               }
            }
         );
      this.list.removeIf(notif -> notif.anim.isFinished(Direction.BACKWARDS));
   }

   @Override
   public void packet(PacketEvent e) {
      if (!PlayerInteractionHelper.nullCheck()) {
         switch (e.getPacket()) {
            default -> {
            }
         }
      }
   }

   @Override
   public void setScreen(SetScreenEvent e) {
      if (e.getScreen() instanceof class_408) {
         this.addList("Пример уведомления", 99999999L);
      }
   }

   @Override
   public void drawDraggable(class_332 context) {
      class_4587 matrix = context.method_51448();
      FontRenderer font = Fonts.getSize(12, Fonts.Type.DEFAULT);
      int windowHeight = mc.method_22683().method_4502();
      int windowWidth = mc.method_22683().method_4486();
      int crosshairY = windowHeight / 2;
      int crosshairX = windowWidth / 2;
      this.setX(crosshairX - 55);
      this.setY(crosshairY + 100);
      float offsetY = 0.0F;
      float offsetX = 5.0F;

      for (Notifications.Notification notification : this.list) {
         float anim = notification.anim.getOutput().floatValue();
         float width = font.getStringWidth(notification.text) + offsetX * 2.0F;
         float startY = this.getY() + offsetY;
         float startX = this.getX() + (100.0F - width) / 2.0F;
         Calculate.setAlpha(
            anim,
            () -> {
               GlassStyle.backdrop(matrix, startX, startY, width + 10.0F, this.getHeight() + 1, 9.0F);
               font.drawText(matrix, notification.text, (int)(startX + offsetX) + 6, startY + 7.0F);
               if (!notification.isExpired()) {
                  long elapsed = System.currentTimeMillis() - notification.startTime;
                  long totalTime = notification.removeTime - notification.startTime;
                  float progress = 1.0F - Math.min(1.0F, (float)elapsed / (float)totalTime);
                  float progressWidth = width * progress;
                  if (progressWidth > 0.0F) {
                     rectangle.render(
                        ShapeProperties.create(matrix, startX + 2.0F, startY + 0.05F, progressWidth + 6.0F, 1.0)
                           .round(0.75F, 0.0F, 0.75F, 0.0F)
                           .color(new Color(155, 155, 155, 255).getRGB())
                           .build()
                     );
                  }
               }
            }
         );
         offsetY += (this.getHeight() + 3) * anim;
      }
   }

   public void addList(String text, long removeTime) {
      this.addList(text, removeTime, null);
   }

   public void addList(class_2561 text, long removeTime) {
      this.addList(text, removeTime, null);
   }

   public void addList(String text, long removeTime, class_3414 sound) {
      this.addList(class_2561.method_43473().method_27693(text), removeTime, sound);
   }

   public void addList(class_2561 text, long removeTime, class_3414 sound) {
      this.list
         .add(new Notifications.Notification(text, new InOutCirc().setMs(400).setValue(1.0), System.currentTimeMillis(), System.currentTimeMillis() + removeTime));
      if (this.list.size() > 12) {
         this.list.removeFirst();
      }

      this.list.sort(Comparator.comparingDouble(notif -> -notif.removeTime));
      if (sound != null) {
         SoundManager.playSound(sound);
      }
   }

   public record Notification(class_2561 text, Animation anim, long startTime, long removeTime) {
      public boolean isExpired() {
         return System.currentTimeMillis() > this.removeTime;
      }
   }
}
