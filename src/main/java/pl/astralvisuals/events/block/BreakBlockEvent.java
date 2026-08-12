package pl.astralvisuals.events.block;

import net.minecraft.class_2338;
import pl.astralvisuals.utils.client.managers.event.events.Event;

public record BreakBlockEvent(class_2338 blockPos) implements Event {
}
