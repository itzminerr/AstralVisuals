package pl.astralvisuals.main.listener.impl;

import net.minecraft.class_1309;
import net.minecraft.class_2848;
import net.minecraft.class_2868;
import pl.astralvisuals.Force;
import pl.astralvisuals.events.packet.PacketEvent;
import pl.astralvisuals.events.player.AttackEvent;
import pl.astralvisuals.events.player.TickEvent;
import pl.astralvisuals.main.listener.Listener;
import pl.astralvisuals.utils.client.managers.api.draggable.AbstractDraggable;
import pl.astralvisuals.utils.client.managers.event.EventHandler;
import pl.astralvisuals.utils.client.packet.network.Network;
import pl.astralvisuals.utils.client.target.TargetTracker;

public class EventListener implements Listener {
   public static boolean serverSprint;
   public static int selectedSlot;

   @EventHandler
   public void onTick(TickEvent e) {
      Network.tick();
      TargetTracker.tick();
      Force.getInstance().getDraggableRepository().draggable().forEach(AbstractDraggable::tick);
   }

   @EventHandler
   public void onPacket(PacketEvent e) {
      switch (e.getPacket()) {
         case class_2848 command:
            serverSprint = switch (command.method_12365()) {
               case field_12981 -> true;
               case field_12985 -> false;
               default -> serverSprint;
            };
            break;
         case class_2868 slot:
            selectedSlot = slot.method_12442();
            break;
         default:
      }

      Network.packet(e);
      Force.getInstance().getDraggableRepository().draggable().forEach(drag -> drag.packet(e));
   }

   @EventHandler
   public void onAttack(AttackEvent e) {
      if (e.getEntity() instanceof class_1309 livingEntity) {
         TargetTracker.setTarget(livingEntity);
      }
   }
}
