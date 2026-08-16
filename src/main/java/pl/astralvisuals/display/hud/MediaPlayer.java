package pl.astralvisuals.display.hud;

import dev.redstones.mediaplayerinfo.IMediaSession;
import dev.redstones.mediaplayerinfo.MediaInfo;
import dev.redstones.mediaplayerinfo.MediaPlayerInfo;
import java.util.Arrays;
import java.util.Collection;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;
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
   private static final long FETCH_INTERVAL_PLAYING_MS = 1250L;
   private static final long FETCH_INTERVAL_IDLE_MS = 2500L;
   private static final long CONTROL_REFRESH_DELAY_MS = 200L;
   private static final long KEEP_VISIBLE_MS = 5000L;
   private static final int ARTWORK_TEXTURE_SIZE = 96;
   private static final int CARD_WIDTH = 164;
   private static final int CARD_HEIGHT = 52;
   private final ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor(runnable -> {
      Thread thread = new Thread(runnable, "AstralVisuals Media Player");
      thread.setDaemon(true);
      return thread;
   });
   private final ExecutorService artworkExecutor = Executors.newSingleThreadExecutor(runnable -> {
      Thread thread = new Thread(runnable, "AstralVisuals Media Artwork");
      thread.setDaemon(true);
      return thread;
   });
   private final AtomicBoolean fetchInProgress = new AtomicBoolean();
   private final AtomicLong artworkRevision = new AtomicLong();
   private final ControlButton previous = new ControlButton();
   private final ControlButton playPause = new ControlButton();
   private final ControlButton next = new ControlButton();
   private volatile MediaInfo mediaInfo = PLACEHOLDER;
   private volatile IMediaSession session;
   private volatile long lastMediaAt;
   private volatile long snapshotAtNanos;
   private volatile long lastFetchAt;
   private volatile boolean artworkRegistered;
   private volatile int artworkHash;
   private volatile int artworkLength = -1;
   private float displayedProgress;
   private String cachedTitleSource = "";
   private String cachedArtistSource = "";
   private String cachedTitle = "";
   private String cachedArtist = "";
   private String cachedTime = "0:00 / 0:00";
   private long cachedPositionSeconds = -1L;
   private long cachedDurationSeconds = -1L;

   public MediaPlayer() {
      super(Interface.ELEMENT_MEDIA_PLAYER, 10, 112, CARD_WIDTH, CARD_HEIGHT, true);
   }

   @Override
   public boolean visible() {
      return PlayerInteractionHelper.isChat(mc.field_1755) || System.currentTimeMillis() - this.lastMediaAt <= KEEP_VISIBLE_MS;
   }

   @Override
   public void tick() {
      if (!this.isPollingEnabled()) {
         return;
      }
      long now = System.currentTimeMillis();
      long interval = this.session != null && this.mediaInfo.getPlaying() ? FETCH_INTERVAL_PLAYING_MS : FETCH_INTERVAL_IDLE_MS;
      if (now - this.lastFetchAt >= interval) {
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
      if (this.artworkRegistered) {
         Render2D.drawTextureOriginal(context, ARTWORK_TEXTURE, artworkX, artworkY, artworkSize, artworkSize);
      } else {
         GlassStyle.surface(matrix, artworkX, artworkY, artworkSize, artworkSize, 7.0F, false);
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

      this.updateTextCache(info, titleFont, textFont, contentWidth);
      titleFont.drawString(matrix, this.cachedTitle, contentX, y + 8.0F, ColorAssist.getText());
      textFont.drawString(matrix, this.cachedArtist, contentX, y + 17.5F, ColorAssist.getText(0.65F));

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

      String time = this.updateTimeCache(info);
      smallFont.drawString(matrix, time, x + CARD_WIDTH - 5.0F - smallFont.getStringWidth(time), y + 41.0F, ColorAssist.getText(0.55F));
   }

   @Override
   public boolean mouseClicked(double mouseX, double mouseY, int button) {
      if (button == 0 && this.session != null && this.visible()) {
         IMediaSession current = this.session;
         if (this.isInside(this.previous, mouseX, mouseY)) {
            this.runControl(current, IMediaSession::previous);
            return true;
         }
         if (this.isInside(this.playPause, mouseX, mouseY)) {
            this.runControl(current, IMediaSession::playPause);
            return true;
         }
         if (this.isInside(this.next, mouseX, mouseY)) {
            this.runControl(current, IMediaSession::next);
            return true;
         }
      }
      return super.mouseClicked(mouseX, mouseY, button);
   }

   private void drawButton(class_4587 matrix, FontRenderer font, ControlButton button, String label, boolean active, boolean accent) {
      GlassStyle.button(matrix, button.x, button.y, button.width, button.height, 4.0F, 0.0F);
      int color = active ? (accent ? GlassStyle.accentStart() : ColorAssist.getText()) : ColorAssist.getText(0.35F);
      font.drawString(matrix, label, button.x + (button.width - font.getStringWidth(label)) / 2.0F, button.y + 5.0F, color);
   }

   private void drawPlaybackButton(class_4587 matrix, ControlButton button, boolean active, boolean playing) {
      GlassStyle.button(matrix, button.x, button.y, button.width, button.height, 4.0F, 0.0F);
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

   private boolean isPollingEnabled() {
      Interface hud = Interface.getInstance();
      return hud != null && hud.isState() && hud.interfaceSettings.isSelected(Interface.ELEMENT_MEDIA_PLAYER);
   }

   private void runControl(IMediaSession current, Consumer<IMediaSession> action) {
      this.executor.execute(() -> {
         try {
            action.accept(current);
         } catch (Throwable ignored) {
         } finally {
            this.executor.schedule(this::requestMediaUpdate, CONTROL_REFRESH_DELAY_MS, TimeUnit.MILLISECONDS);
         }
      });
   }

   private void requestMediaUpdate() {
      if (!this.fetchInProgress.compareAndSet(false, true)) {
         return;
      }
      this.lastFetchAt = System.currentTimeMillis();
      this.executor.execute(() -> {
         try {
            SessionSnapshot selected = this.selectBest(MediaPlayerInfo.getMediaSessions());
            if (selected == null) {
               this.session = null;
               return;
            }
            MediaInfo info = selected.info();
            if (!this.isUseful(info)) {
               this.session = null;
               return;
            }
            this.mediaInfo = new MediaInfo(
               info.getTitle(), info.getArtist(), new byte[0], info.getPosition(), info.getDuration(), info.getPlaying()
            );
            this.session = selected.session();
            this.snapshotAtNanos = System.nanoTime();
            this.lastMediaAt = System.currentTimeMillis();
            this.updateArtwork(info.getArtworkPng());
         } catch (Throwable ignored) {
         } finally {
            this.fetchInProgress.set(false);
         }
      });
   }

   private SessionSnapshot selectBest(Collection<IMediaSession> sessions) {
      if (sessions == null || sessions.isEmpty()) {
         return null;
      }
      SessionSnapshot best = null;
      int bestScore = 0;
      for (IMediaSession value : sessions) {
         if (value == null) {
            continue;
         }
         try {
            MediaInfo info = value.getMedia();
            int score = this.score(info);
            if (score > bestScore) {
               bestScore = score;
               best = new SessionSnapshot(value, info);
            }
         } catch (Throwable ignored) {
         }
      }
      return best;
   }

   private int score(MediaInfo info) {
      if (info == null) {
         return 0;
      }
      int score = info.getPlaying() ? 100 : 0;
      score += this.clean(info.getTitle(), "").isEmpty() ? 0 : 40;
      score += this.clean(info.getArtist(), "").isEmpty() ? 0 : 25;
      score += info.getDuration() > 0L ? 15 : 0;
      return score;
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
      if (hash == this.artworkHash && artwork.length == this.artworkLength) {
         return;
      }
      this.artworkHash = hash;
      this.artworkLength = artwork.length;
      long revision = this.artworkRevision.incrementAndGet();
      if (artwork.length == 0) {
         mc.execute(() -> {
            if (revision == this.artworkRevision.get()) {
               mc.method_1531().method_4615(ARTWORK_TEXTURE);
               this.artworkRegistered = false;
            }
         });
         return;
      }

      byte[] copy = artwork.clone();
      this.artworkExecutor.execute(() -> {
         if (revision != this.artworkRevision.get()) {
            return;
         }
         class_1011 image;
         try {
            image = prepareArtwork(copy);
         } catch (Throwable ignored) {
            if (revision == this.artworkRevision.get()) {
               this.artworkHash = 0;
               this.artworkLength = -1;
            }
            return;
         }
         if (revision != this.artworkRevision.get()) {
            image.close();
            return;
         }
         try {
            mc.execute(() -> this.uploadArtwork(revision, image));
         } catch (Throwable ignored) {
            image.close();
         }
      });
   }

   static class_1011 prepareArtwork(byte[] artwork) throws Exception {
      class_1011 decoded = class_1011.method_49277(artwork);
      class_1011 scaled = new class_1011(ARTWORK_TEXTURE_SIZE, ARTWORK_TEXTURE_SIZE, true);
      boolean success = false;
      try {
         decoded.method_4300(0, 0, decoded.method_4307(), decoded.method_4323(), scaled);
         success = true;
         return scaled;
      } finally {
         decoded.close();
         if (!success) {
            scaled.close();
         }
      }
   }

   private void uploadArtwork(long revision, class_1011 image) {
      if (revision != this.artworkRevision.get()) {
         image.close();
         return;
      }
      class_1043 texture = new class_1043(image);
      boolean registered = false;
      try {
         mc.method_1531().method_4615(ARTWORK_TEXTURE);
         mc.method_1531().method_4616(ARTWORK_TEXTURE, texture);
         registered = true;
         this.artworkRegistered = true;
      } catch (Throwable ignored) {
         this.artworkRegistered = false;
         this.artworkHash = 0;
         this.artworkLength = -1;
      } finally {
         if (!registered) {
            texture.close();
         }
      }
   }

   private MediaInfo getDisplayInfo() {
      return this.visible() && this.mediaInfo != null ? this.mediaInfo : PLACEHOLDER;
   }

   private float getTargetProgress() {
      MediaInfo info = this.mediaInfo;
      long duration = info == null ? 0L : this.normalizeTime(info.getDuration());
      if (duration <= 0L) {
         return 0.0F;
      }
      return Math.max(0.0F, Math.min(1.0F, (float)this.estimatedPositionSeconds(info) / (float)duration));
   }

   private long estimatedPositionSeconds(MediaInfo info) {
      if (info == null) {
         return 0L;
      }
      long position = this.normalizeTime(info.getPosition());
      if (info.getPlaying() && this.snapshotAtNanos > 0L) {
         position += Math.max(0L, System.nanoTime() - this.snapshotAtNanos) / 1_000_000_000L;
      }
      long duration = this.normalizeTime(info.getDuration());
      return duration > 0L ? Math.min(position, duration) : position;
   }

   private void updateTextCache(MediaInfo info, FontRenderer titleFont, FontRenderer artistFont, float maxWidth) {
      String title = this.clean(info.getTitle(), "Без названия");
      String artist = this.clean(info.getArtist(), "Неизвестный исполнитель");
      if (!title.equals(this.cachedTitleSource)) {
         this.cachedTitleSource = title;
         this.cachedTitle = this.trim(titleFont, title, maxWidth);
      }
      if (!artist.equals(this.cachedArtistSource)) {
         this.cachedArtistSource = artist;
         this.cachedArtist = this.trim(artistFont, artist, maxWidth);
      }
   }

   private String updateTimeCache(MediaInfo info) {
      long position = this.estimatedPositionSeconds(info);
      long duration = this.normalizeTime(info.getDuration());
      if (position != this.cachedPositionSeconds || duration != this.cachedDurationSeconds) {
         this.cachedPositionSeconds = position;
         this.cachedDurationSeconds = duration;
         this.cachedTime = this.formatTime(position) + " / " + this.formatTime(duration);
      }
      return this.cachedTime;
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
      return hours > 0L
         ? hours + ":" + this.twoDigits(minutes) + ":" + this.twoDigits(remainder)
         : minutes + ":" + this.twoDigits(remainder);
   }

   private String twoDigits(long value) {
      return value < 10L ? "0" + value : Long.toString(value);
   }

   private record SessionSnapshot(IMediaSession session, MediaInfo info) {
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
