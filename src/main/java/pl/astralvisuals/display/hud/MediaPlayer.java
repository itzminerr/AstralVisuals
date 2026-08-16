package pl.astralvisuals.display.hud;

import dev.redstones.mediaplayerinfo.IMediaSession;
import dev.redstones.mediaplayerinfo.MediaInfo;
import dev.redstones.mediaplayerinfo.MediaPlayerInfo;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;
import net.minecraft.class_1011;
import net.minecraft.class_1043;
import net.minecraft.class_2960;
import net.minecraft.class_332;
import net.minecraft.class_4587;
import pl.astralvisuals.features.impl.render.Interface;
import pl.astralvisuals.utils.client.managers.api.draggable.AbstractDraggable;
import pl.astralvisuals.utils.display.color.ColorAssist;
import pl.astralvisuals.utils.display.font.FontRenderer;
import pl.astralvisuals.utils.display.font.Fonts;
import pl.astralvisuals.utils.display.geometry.Render2D;
import pl.astralvisuals.utils.display.shape.ShapeProperties;
import pl.astralvisuals.utils.display.style.GlassStyle;
import pl.astralvisuals.utils.interactions.interact.PlayerInteractionHelper;

public final class MediaPlayer extends AbstractDraggable {
   private static final class_2960 ARTWORK_TEXTURE = class_2960.method_60654("astralvisuals:media_player_artwork");
   private static final MediaInfo PLACEHOLDER = new MediaInfo("Media Player", "Нет активного трека", new byte[0], 0L, 0L, false);
   private static final long FETCH_INTERVAL_MS = 350L;
   private static final long KEEP_VISIBLE_MS = 5000L;
   private static final int CARD_WIDTH = 164;
   private static final int CARD_HEIGHT = 52;
   private final ExecutorService executor = Executors.newSingleThreadExecutor(runnable -> {
      Thread thread = new Thread(runnable, "AstralVisuals Media Player");
      thread.setDaemon(true);
      return thread;
   });
   private final AtomicBoolean fetchInProgress = new AtomicBoolean();
   private final ControlButton previous = new ControlButton();
   private final ControlButton playPause = new ControlButton();
   private final ControlButton next = new ControlButton();
   private volatile MediaInfo mediaInfo = PLACEHOLDER;
   private volatile IMediaSession session;
   private volatile long lastMediaAt;
   private long lastFetchAt;
   private boolean artworkRegistered;
   private int artworkHash;
   private float displayedProgress;

   public MediaPlayer() {
      super(Interface.ELEMENT_MEDIA_PLAYER, 10, 112, CARD_WIDTH, CARD_HEIGHT, true);
   }

   @Override
   public boolean visible() {
      return PlayerInteractionHelper.isChat(mc.field_1755) || System.currentTimeMillis() - this.lastMediaAt <= KEEP_VISIBLE_MS;
   }

   @Override
   public void tick() {
      long now = System.currentTimeMillis();
      if (now - this.lastFetchAt >= FETCH_INTERVAL_MS) {
         this.lastFetchAt = now;
         this.requestMediaUpdate();
      }
      float target = this.getTargetProgress();
      this.displayedProgress += (target - this.displayedProgress) * 0.2F;
      this.displayedProgress = Math.max(0.0F, Math.min(1.0F, this.displayedProgress));
   }

   @Override
   public void drawDraggable(class_332 context) {
      class_4587 matrix = context.method_51448();
      float x = this.getX();
      float y = this.getY();
      float artworkX = x + 5.0F;
      float artworkY = y + 5.0F;
      float artworkSize = 42.0F;
      float contentX = artworkX + artworkSize + 6.0F;
      float contentWidth = CARD_WIDTH - (contentX - x) - 5.0F;
      MediaInfo info = this.getDisplayInfo();
      FontRenderer titleFont = Fonts.getSize(14, Fonts.Type.BOLD);
      FontRenderer textFont = Fonts.getSize(12, Fonts.Type.DEFAULT);
      FontRenderer smallFont = Fonts.getSize(10, Fonts.Type.DEFAULT);

      GlassStyle.strongBackdrop(matrix, x, y, CARD_WIDTH, CARD_HEIGHT, 9.0F);
      GlassStyle.surface(matrix, artworkX, artworkY, artworkSize, artworkSize, 7.0F, false);
      if (this.artworkRegistered) {
         Render2D.drawTextureOriginal(context, ARTWORK_TEXTURE, artworkX, artworkY, artworkSize, artworkSize);
      } else {
         String label = "MP";
         FontRenderer placeholderFont = Fonts.getSize(18, Fonts.Type.BOLD);
         placeholderFont.drawString(
            matrix,
            label,
            artworkX + (artworkSize - placeholderFont.getStringWidth(label)) / 2.0F,
            artworkY + 18.0F,
            GlassStyle.accentMid()
         );
      }

      String title = this.trim(titleFont, this.clean(info.getTitle(), "Без названия"), contentWidth);
      String artist = this.trim(textFont, this.clean(info.getArtist(), "Неизвестный исполнитель"), contentWidth);
      titleFont.drawString(matrix, title, contentX, y + 8.0F, ColorAssist.getText());
      textFont.drawString(matrix, artist, contentX, y + 17.5F, ColorAssist.getText(0.65F));

      float progressY = y + 27.0F;
      rectangle.render(ShapeProperties.create(matrix, contentX, progressY, contentWidth, 2.5F).round(1.5F).color(ColorAssist.getText(0.12F)).build());
      float progressWidth = contentWidth * this.displayedProgress;
      if (progressWidth > 0.25F) {
         rectangle.render(ShapeProperties.create(matrix, contentX, progressY, progressWidth, 2.5F).round(1.5F).color(GlassStyle.accentMid()).build());
      }

      float buttonY = y + 34.0F;
      this.previous.set(contentX, buttonY, 15.0F, 12.0F);
      this.playPause.set(contentX + 19.0F, buttonY, 18.0F, 12.0F);
      this.next.set(contentX + 41.0F, buttonY, 15.0F, 12.0F);
      boolean active = this.session != null;
      this.drawButton(matrix, smallFont, this.previous, "<", active, false);
      this.drawPlaybackButton(matrix, this.playPause, active, info.getPlaying());
      this.drawButton(matrix, smallFont, this.next, ">", active, false);

      String time = this.formatTime(this.normalizeTime(info.getPosition())) + " / " + this.formatTime(this.normalizeTime(info.getDuration()));
      smallFont.drawString(matrix, time, x + CARD_WIDTH - 5.0F - smallFont.getStringWidth(time), y + 41.0F, ColorAssist.getText(0.55F));
      this.setWidth(CARD_WIDTH);
      this.setHeight(CARD_HEIGHT);
   }

   @Override
   public boolean mouseClicked(double mouseX, double mouseY, int button) {
      if (button == 0 && this.session != null && this.visible()) {
         IMediaSession current = this.session;
         try {
            if (this.isInside(this.previous, mouseX, mouseY)) {
               current.previous();
               return true;
            }
            if (this.isInside(this.playPause, mouseX, mouseY)) {
               current.playPause();
               return true;
            }
            if (this.isInside(this.next, mouseX, mouseY)) {
               current.next();
               return true;
            }
         } catch (Throwable ignored) {
         }
      }
      return super.mouseClicked(mouseX, mouseY, button);
   }

   private void drawButton(class_4587 matrix, FontRenderer font, ControlButton button, String label, boolean active, boolean accent) {
      GlassStyle.button(matrix, button.x, button.y, button.width, button.height, 4.0F, active ? 0.35F : 0.0F);
      int color = active ? (accent ? GlassStyle.accentStart() : ColorAssist.getText()) : ColorAssist.getText(0.35F);
      font.drawString(matrix, label, button.x + (button.width - font.getStringWidth(label)) / 2.0F, button.y + 5.0F, color);
   }

   private void drawPlaybackButton(class_4587 matrix, ControlButton button, boolean active, boolean playing) {
      GlassStyle.button(matrix, button.x, button.y, button.width, button.height, 4.0F, active ? 0.35F : 0.0F);
      int color = active ? GlassStyle.accentStart() : ColorAssist.getText(0.35F);
      float centerX = button.x + button.width / 2.0F;
      float centerY = button.y + button.height / 2.0F;
      if (playing) {
         rectangle.render(ShapeProperties.create(matrix, centerX - 2.6F, centerY - 3.0F, 1.6F, 6.0F).round(0.6F).color(color).build());
         rectangle.render(ShapeProperties.create(matrix, centerX + 1.0F, centerY - 3.0F, 1.6F, 6.0F).round(0.6F).color(color).build());
      } else {
         rectangle.render(ShapeProperties.create(matrix, centerX - 2.5F, centerY - 2.5F, 5.0F, 5.0F).round(1.0F).color(color).build());
      }
   }

   private boolean isInside(ControlButton button, double mouseX, double mouseY) {
      float scale = Interface.getInstance() == null ? 1.0F : Interface.getInstance().getHudScale();
      float centerX = this.getX() + this.getWidth() / 2.0F;
      float centerY = this.getY() + this.getHeight() / 2.0F;
      float left = centerX + (button.x - centerX) * scale;
      float top = centerY + (button.y - centerY) * scale;
      return mouseX >= left && mouseX <= left + button.width * scale && mouseY >= top && mouseY <= top + button.height * scale;
   }

   private void requestMediaUpdate() {
      if (!this.fetchInProgress.compareAndSet(false, true)) {
         return;
      }
      this.executor.execute(() -> {
         try {
            IMediaSession selected = this.selectBest(MediaPlayerInfo.getMediaSessions());
            if (selected == null) {
               this.session = null;
               return;
            }
            MediaInfo info = selected.getMedia();
            if (!this.isUseful(info)) {
               this.session = null;
               return;
            }
            this.updateArtwork(info.getArtworkPng());
            this.mediaInfo = info;
            this.session = selected;
            this.lastMediaAt = System.currentTimeMillis();
         } catch (Throwable ignored) {
         } finally {
            this.fetchInProgress.set(false);
         }
      });
   }

   private IMediaSession selectBest(Collection<IMediaSession> sessions) {
      if (sessions == null || sessions.isEmpty()) {
         return null;
      }
      return sessions.stream()
         .filter(Objects::nonNull)
         .max(Comparator.comparingInt(this::score))
         .filter(value -> this.score(value) > 0)
         .orElse(null);
   }

   private int score(IMediaSession value) {
      try {
         MediaInfo info = value.getMedia();
         if (info == null) {
            return 0;
         }
         int score = info.getPlaying() ? 100 : 0;
         score += this.clean(info.getTitle(), "").isEmpty() ? 0 : 40;
         score += this.clean(info.getArtist(), "").isEmpty() ? 0 : 25;
         score += info.getDuration() > 0L ? 15 : 0;
         return score;
      } catch (Throwable ignored) {
         return 0;
      }
   }

   private boolean isUseful(MediaInfo info) {
      return info != null
         && (!this.clean(info.getTitle(), "").isEmpty()
            || !this.clean(info.getArtist(), "").isEmpty()
            || info.getDuration() > 0L
            || info.getPlaying());
   }

   private void updateArtwork(byte[] bytes) {
      byte[] artwork = bytes == null ? new byte[0] : bytes;
      int hash = artwork.length == 0 ? 0 : Arrays.hashCode(artwork);
      if (hash == this.artworkHash) {
         return;
      }
      this.artworkHash = hash;
      byte[] copy = artwork.clone();
      mc.execute(() -> {
         try {
            mc.method_1531().method_4615(ARTWORK_TEXTURE);
            if (copy.length == 0) {
               this.artworkRegistered = false;
               return;
            }
            class_1011 image = class_1011.method_49277(copy);
            mc.method_1531().method_4616(ARTWORK_TEXTURE, new class_1043(image));
            this.artworkRegistered = true;
         } catch (Throwable ignored) {
            this.artworkRegistered = false;
            this.artworkHash = 0;
         }
      });
   }

   private MediaInfo getDisplayInfo() {
      return this.visible() && this.mediaInfo != null ? this.mediaInfo : PLACEHOLDER;
   }

   private float getTargetProgress() {
      MediaInfo info = this.mediaInfo;
      if (info == null || info.getDuration() <= 0L) {
         return 0.0F;
      }
      return Math.max(0.0F, Math.min(1.0F, (float)info.getPosition() / (float)info.getDuration()));
   }

   private String clean(String text, String fallback) {
      return text == null || text.isBlank() ? fallback : text.trim();
   }

   private String trim(FontRenderer font, String text, float maxWidth) {
      if (font.getStringWidth(text) <= maxWidth) {
         return text;
      }
      String value = text;
      while (value.length() > 1 && font.getStringWidth(value + "...") > maxWidth) {
         value = value.substring(0, value.length() - 1);
      }
      return value + "...";
   }

   private long normalizeTime(long value) {
      return value > 100000L ? value / 1000L : Math.max(0L, value);
   }

   private String formatTime(long seconds) {
      long hours = seconds / 3600L;
      long minutes = seconds % 3600L / 60L;
      long remainder = seconds % 60L;
      return hours > 0L ? String.format("%d:%02d:%02d", hours, minutes, remainder) : String.format("%d:%02d", minutes, remainder);
   }

   private static final class ControlButton {
      private float x;
      private float y;
      private float width;
      private float height;

      private void set(float x, float y, float width, float height) {
         this.x = x;
         this.y = y;
         this.width = width;
         this.height = height;
      }
   }
}
