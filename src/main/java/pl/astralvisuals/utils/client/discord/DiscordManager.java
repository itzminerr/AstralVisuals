package pl.astralvisuals.utils.client.discord;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Set;

import net.minecraft.class_310;
import net.minecraft.class_642;
import pl.astralvisuals.Force;
import pl.astralvisuals.main.client.ClientInfoProvider;
import pl.astralvisuals.utils.client.discord.utils.DiscordEventHandlers;
import pl.astralvisuals.utils.client.discord.utils.DiscordRPC;
import pl.astralvisuals.utils.client.discord.utils.DiscordRichPresence;
import pl.astralvisuals.utils.client.discord.utils.RPCButton;
import pl.astralvisuals.utils.client.logs.Logger;

public class DiscordManager {
   private static final String APPLICATION_ID = "1517995503831351466";
   private static final String LARGE_IMAGE = "https://files.catbox.moe/cenka4.png";
   private static final String TELEGRAM = "https://t.me/astralvis";
   private static final long CALLBACK_INTERVAL_MS = 250L;
   private static final long PRESENCE_UPDATE_INTERVAL_MS = 5000L;
   private static final Set<String> OWNER_DISCORD_IDS = Set.of("810219966116790312", "1082963686706130985");
   private static final Set<String> DEVELOPER_DISCORD_IDS = Set.of("940995224971403305");
   private final Object rpcLock = new Object();
   private volatile boolean running;
   private volatile boolean connected;
   private long startTimestamp;
   private long lastPresenceUpdate;
   private boolean presenceUpdateRequested;
   private boolean clearBeforeNextUpdate;
   private int burstUpdatesRemaining;
   private long nextBurstUpdateTime;
   private DiscordEventHandlers handlers;
   private DiscordManager.DiscordInfo info = new DiscordManager.DiscordInfo("Неизвестно", "", "");

   public void init() {
      if (!this.running) {
         String os = System.getProperty("os.name").toLowerCase();
         if (!os.contains("linux")) {
            this.handlers = new DiscordEventHandlers.Builder().ready(user -> {
               this.connected = true;
               String discordId = this.normalizeDiscordId(user.userId);
               this.setInfo(new DiscordManager.DiscordInfo(user.username, this.getDiscordAvatarUrl(user.userId, user.avatar), discordId));
               Logger.info("DiscordRPC connected: " + user.username + " (" + discordId + ")");
               this.requestPresenceUpdate(true);
            }).errored((code, message) -> Logger.warn("DiscordRPC error " + code + ": " + message)).disconnected((code, message) -> {
               this.connected = false;
               this.info = new DiscordManager.DiscordInfo("Неизвестно", "", "");
               this.resetPendingPresenceUpdates();
               Logger.warn("DiscordRPC disconnected " + code + ": " + message);
            }).build();

            try {
               this.startTimestamp = System.currentTimeMillis() / 1000L;
               this.running = true;
               synchronized (this.rpcLock) {
                  DiscordRPC.INSTANCE.Discord_Initialize("1517995503831351466", this.handlers, true, "");
               }

               Logger.info("DiscordRPC initialized: 1517995503831351466");
               DiscordManager.DiscordDaemonThread discordDaemonThread = new DiscordManager.DiscordDaemonThread();
               discordDaemonThread.setDaemon(true);
               discordDaemonThread.start();
            } catch (Throwable var5) {
               this.running = false;
               this.connected = false;
               Logger.error("DiscordRPC failed to initialize", var5);
            }
         }
      }
   }

   public void stopRPC() {
      if (this.running) {
         this.running = false;
         this.connected = false;
         this.lastPresenceUpdate = 0L;
         this.resetPendingPresenceUpdates();

         try {
            synchronized (this.rpcLock) {
               DiscordRPC.INSTANCE.Discord_ClearPresence();
               DiscordRPC.INSTANCE.Discord_Shutdown();
            }
         } catch (Throwable var4) {
            Logger.error("DiscordRPC failed to shutdown cleanly", var4);
         }
      }
   }

   public void updatePresence() {
      this.requestPresenceUpdate(false);
   }

   public void updatePresenceImmediately() {
      synchronized (this) {
         this.requestPresenceUpdate(true);
         this.burstUpdatesRemaining = 2;
         this.nextBurstUpdateTime = System.currentTimeMillis() + 350L;
      }
   }

   private void sendPresence(boolean clearBeforeUpdate) {
      if (this.running && this.hasDiscordUser()) {
         try {
            DiscordRichPresence presence = this.buildPresence();
            synchronized (this.rpcLock) {
               if (clearBeforeUpdate) {
                  DiscordRPC.INSTANCE.Discord_ClearPresence();
               }

               DiscordRPC.INSTANCE.Discord_UpdatePresence(presence);
            }

            this.lastPresenceUpdate = System.currentTimeMillis();
         } catch (Throwable var6) {
            Logger.error("DiscordRPC failed to update presence", var6);
         }
      }
   }

   private DiscordRichPresence buildPresence() {
      DiscordRichPresence.Builder builder = new DiscordRichPresence.Builder()
         .setStartTimestamp(this.startTimestamp)
         .setDetails("Аккаунт: " + this.getPlayerName() + " | Роль: " + this.getRole())
         .setState(this.getWorldState())
         .setLargeImage(LARGE_IMAGE, "AstralVisuals")
         .setButtons(RPCButton.create("Телеграм", "https://t.me/astralvis"));
      class_642 serverInfo = this.getCurrentServerInfo();
      if (serverInfo != null && serverInfo.field_3761 != null && !serverInfo.field_3761.isBlank()) {
         builder.setSmallImage(this.getServerIconUrl(serverInfo), this.getServerIconText(serverInfo));
      }

      return builder.build();
   }

   private synchronized void requestPresenceUpdate(boolean clearBeforeUpdate) {
      this.presenceUpdateRequested = true;
      this.clearBeforeNextUpdate = this.clearBeforeNextUpdate || clearBeforeUpdate;
   }

   private synchronized DiscordManager.PresenceUpdate pollPresenceUpdate(long now) {
      if (!this.connected || !this.hasDiscordUser()) {
         return DiscordManager.PresenceUpdate.NONE;
      } else if (this.presenceUpdateRequested) {
         boolean clear = this.clearBeforeNextUpdate;
         this.presenceUpdateRequested = false;
         this.clearBeforeNextUpdate = false;
         return new DiscordManager.PresenceUpdate(true, clear);
      } else if (this.burstUpdatesRemaining > 0 && now >= this.nextBurstUpdateTime) {
         this.burstUpdatesRemaining--;
         this.nextBurstUpdateTime = this.burstUpdatesRemaining > 0 ? now + 900L : 0L;
         return new DiscordManager.PresenceUpdate(true, false);
      } else {
         return now - this.lastPresenceUpdate >= 5000L ? new DiscordManager.PresenceUpdate(true, false) : DiscordManager.PresenceUpdate.NONE;
      }
   }

   private synchronized void resetPendingPresenceUpdates() {
      this.presenceUpdateRequested = false;
      this.clearBeforeNextUpdate = false;
      this.burstUpdatesRemaining = 0;
      this.nextBurstUpdateTime = 0L;
   }

   private String getPlayerName() {
      Force force = Force.getInstance();
      if (force != null && force.getAccountRepository() != null) {
         String currentAccount = force.getAccountRepository().currentAccount;
         return currentAccount != null && !currentAccount.isBlank() ? currentAccount : "Пусто";
      } else {
         class_310 mc = class_310.method_1551();
         return mc.method_1548() == null ? "Пусто" : mc.method_1548().method_1676();
      }
   }

   private String getWorldState() {
      class_310 mc = class_310.method_1551();
      if (mc.field_1724 != null && mc.field_1687 != null) {
         class_642 serverInfo = this.getCurrentServerInfo();
         return serverInfo != null && serverInfo.field_3761 != null && !serverInfo.field_3761.isBlank() ? "Сервер: " + serverInfo.field_3761 : "Одиночная игра";
      } else {
         return "В главном меню";
      }
   }

   private String getClientName() {
      Force force = Force.getInstance();
      if (force == null) {
         return "AstralVisuals";
      } else {
         ClientInfoProvider clientInfoProvider = force.getClientInfoProvider();
         return clientInfoProvider == null ? "AstralVisuals" : clientInfoProvider.clientName();
      }
   }

   private String getRole() {
      String userId = this.normalizeDiscordId(this.info.userId());
      if (OWNER_DISCORD_IDS.contains(userId)) {
         return "Владелец";
      }
      if (DEVELOPER_DISCORD_IDS.contains(userId)) {
         return "Разработчик";
      }
      return "Юзер";
   }

   private String normalizeDiscordId(String discordId) {
      return discordId == null ? "" : discordId.trim();
   }

   private boolean hasDiscordUser() {
      return !this.normalizeDiscordId(this.info.userId()).isEmpty();
   }

   private String getDiscordAvatarUrl(String userId, String avatarHash) {
      return userId != null && !userId.isBlank() && avatarHash != null && !avatarHash.isBlank()
         ? "https://cdn.discordapp.com/avatars/" + userId + "/" + avatarHash + ".png"
         : "";
   }

   private String getServerIconUrl(class_642 serverInfo) {
      return "https://api.mcstatus.io/v2/icon/" + this.encodeUrlPart(serverInfo.field_3761);
   }

   private String getServerIconText(class_642 serverInfo) {
      return serverInfo.field_3752 != null && !serverInfo.field_3752.isBlank() ? serverInfo.field_3752 : serverInfo.field_3761;
   }

   private class_642 getCurrentServerInfo() {
      class_310 mc = class_310.method_1551();
      if (mc.field_1724 == null || mc.field_1687 == null) {
         return null;
      } else if (mc.method_1558() != null) {
         return mc.method_1558();
      } else {
         return mc.method_1562() == null ? null : mc.method_1562().method_45734();
      }
   }

   private String encodeUrlPart(String value) {
      return URLEncoder.encode(value, StandardCharsets.UTF_8).replace("+", "%20");
   }

   public Object getRpcLock() {
      return this.rpcLock;
   }

   public boolean isRunning() {
      return this.running;
   }

   public boolean isConnected() {
      return this.connected;
   }

   public long getStartTimestamp() {
      return this.startTimestamp;
   }

   public long getLastPresenceUpdate() {
      return this.lastPresenceUpdate;
   }

   public boolean isPresenceUpdateRequested() {
      return this.presenceUpdateRequested;
   }

   public boolean isClearBeforeNextUpdate() {
      return this.clearBeforeNextUpdate;
   }

   public int getBurstUpdatesRemaining() {
      return this.burstUpdatesRemaining;
   }

   public long getNextBurstUpdateTime() {
      return this.nextBurstUpdateTime;
   }

   public DiscordEventHandlers getHandlers() {
      return this.handlers;
   }

   public DiscordManager.DiscordInfo getInfo() {
      return this.info;
   }

   public void setRunning(boolean running) {
      this.running = running;
   }

   public void setConnected(boolean connected) {
      this.connected = connected;
   }

   public void setStartTimestamp(long startTimestamp) {
      this.startTimestamp = startTimestamp;
   }

   public void setLastPresenceUpdate(long lastPresenceUpdate) {
      this.lastPresenceUpdate = lastPresenceUpdate;
   }

   public void setPresenceUpdateRequested(boolean presenceUpdateRequested) {
      this.presenceUpdateRequested = presenceUpdateRequested;
   }

   public void setClearBeforeNextUpdate(boolean clearBeforeNextUpdate) {
      this.clearBeforeNextUpdate = clearBeforeNextUpdate;
   }

   public void setBurstUpdatesRemaining(int burstUpdatesRemaining) {
      this.burstUpdatesRemaining = burstUpdatesRemaining;
   }

   public void setNextBurstUpdateTime(long nextBurstUpdateTime) {
      this.nextBurstUpdateTime = nextBurstUpdateTime;
   }

   public void setHandlers(DiscordEventHandlers handlers) {
      this.handlers = handlers;
   }

   public void setInfo(DiscordManager.DiscordInfo info) {
      this.info = info;
   }

   private class DiscordDaemonThread extends Thread {
      @Override
      public void run() {
         this.setName("Discord-RPC");

         try {
            for (; DiscordManager.this.running; Thread.sleep(250L)) {
               synchronized (DiscordManager.this.rpcLock) {
                  DiscordRPC.INSTANCE.Discord_RunCallbacks();
               }

               long now = System.currentTimeMillis();
               DiscordManager.PresenceUpdate presenceUpdate = DiscordManager.this.pollPresenceUpdate(now);
               if (presenceUpdate.requested()) {
                  DiscordManager.this.sendPresence(presenceUpdate.clearBeforeUpdate());
               }
            }
         } catch (InterruptedException var5) {
            Thread.currentThread().interrupt();
         } catch (Throwable var6) {
            DiscordManager.this.running = false;
            DiscordManager.this.connected = false;
            Logger.error("DiscordRPC daemon stopped", var6);
         }
      }
   }

   public record DiscordInfo(String userName, String avatarUrl, String userId) {
   }

   private record PresenceUpdate(boolean requested, boolean clearBeforeUpdate) {
      private static final DiscordManager.PresenceUpdate NONE = new DiscordManager.PresenceUpdate(false, false);
   }
}
