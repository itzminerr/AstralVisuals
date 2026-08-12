package pl.astralvisuals.utils.client.managers.api.draggable;

import net.minecraft.class_332;
import pl.astralvisuals.events.container.SetScreenEvent;
import pl.astralvisuals.events.packet.PacketEvent;

public interface Draggable {
   boolean visible();

   void tick();

   void render(class_332 var1, int var2, int var3, float var4);

   void packet(PacketEvent var1);

   void setScreen(SetScreenEvent var1);

   boolean mouseClicked(double var1, double var3, int var5);

   boolean mouseReleased(double var1, double var3, int var5);
}
