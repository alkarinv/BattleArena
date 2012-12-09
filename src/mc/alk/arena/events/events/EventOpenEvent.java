package mc.alk.arena.events.events;

import mc.alk.arena.competition.events.Event;
import mc.alk.arena.events.BAEvent;
import mc.alk.arena.events.CancellableEvent;

public class EventOpenEvent extends BAEvent implements CancellableEvent {
	final Event event;

	/// Cancel status
	boolean cancelled = false;

	public EventOpenEvent(Event event){
		this.event = event;
	}
	public Event getEvent(){
		return event;
	}

	public boolean isCancelled() {
		return cancelled;
	}

	public void setCancelled(boolean cancelled) {
		this.cancelled = cancelled;
	}
}
