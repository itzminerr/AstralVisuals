package pl.astralvisuals.events.block;

import net.minecraft.class_2338;
import net.minecraft.class_2350;
import pl.astralvisuals.utils.client.managers.event.events.Event;

public record BlockBreakingEvent(class_2338 blockPos, class_2350 direction) implements Event {
}
