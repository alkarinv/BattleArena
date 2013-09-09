package mc.alk.arena.events.events;

import mc.alk.arena.competition.events.Event;

public class EventCompletedEvent extends EventEvent {
	public EventCompletedEvent(Event event){
		super(event);
	}
}
