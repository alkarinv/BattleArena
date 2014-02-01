package mc.alk.arena.events.events;

import mc.alk.arena.competition.events.Event;
import org.bukkit.event.Cancellable;

public class EventOpenEvent extends EventEvent implements Cancellable {
	/// Cancel status
	boolean cancelled = false;

	public EventOpenEvent(Event event){
		super(event);
	}

	public boolean isCancelled() {
		return cancelled;
	}

	public void setCancelled(boolean cancelled) {
		this.cancelled = cancelled;
	}
}
