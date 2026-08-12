package pl.astralvisuals.utils.client.discord.utils;

import com.sun.jna.Structure;
import java.util.Arrays;
import java.util.List;
import pl.astralvisuals.utils.client.discord.callbacks.DisconnectedCallback;
import pl.astralvisuals.utils.client.discord.callbacks.ErroredCallback;
import pl.astralvisuals.utils.client.discord.callbacks.JoinGameCallback;
import pl.astralvisuals.utils.client.discord.callbacks.JoinRequestCallback;
import pl.astralvisuals.utils.client.discord.callbacks.ReadyCallback;
import pl.astralvisuals.utils.client.discord.callbacks.SpectateGameCallback;

public class DiscordEventHandlers extends Structure {
   public ReadyCallback ready;
   public DisconnectedCallback disconnected;
   public ErroredCallback errored;
   public JoinGameCallback joinGame;
   public SpectateGameCallback spectateGame;
   public JoinRequestCallback joinRequest;

   protected List<String> getFieldOrder() {
      return Arrays.asList("ready", "disconnected", "errored", "joinGame", "spectateGame", "joinRequest");
   }

   public static class Builder {
      private final DiscordEventHandlers handlers = new DiscordEventHandlers();

      public DiscordEventHandlers.Builder ready(ReadyCallback callback) {
         this.handlers.ready = callback;
         return this;
      }

      public DiscordEventHandlers.Builder disconnected(DisconnectedCallback callback) {
         this.handlers.disconnected = callback;
         return this;
      }

      public DiscordEventHandlers.Builder errored(ErroredCallback callback) {
         this.handlers.errored = callback;
         return this;
      }

      public DiscordEventHandlers.Builder joinGame(JoinGameCallback callback) {
         this.handlers.joinGame = callback;
         return this;
      }

      public DiscordEventHandlers.Builder spectateGame(SpectateGameCallback callback) {
         this.handlers.spectateGame = callback;
         return this;
      }

      public DiscordEventHandlers.Builder joinRequest(JoinRequestCallback callback) {
         this.handlers.joinRequest = callback;
         return this;
      }

      public DiscordEventHandlers build() {
         return this.handlers;
      }
   }
}
