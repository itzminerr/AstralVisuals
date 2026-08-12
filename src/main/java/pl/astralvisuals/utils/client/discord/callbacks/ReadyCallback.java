package pl.astralvisuals.utils.client.discord.callbacks;

import com.sun.jna.Callback;
import pl.astralvisuals.utils.client.discord.utils.DiscordUser;

public interface ReadyCallback extends Callback {
   void apply(DiscordUser var1);
}
