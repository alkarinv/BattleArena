package mc.alk.arena.events;

import org.bukkit.event.Event;

public abstract class ExtendedBukkitEvent {
	protected Event event;

	public abstract Event getBukkitEvent();
}
