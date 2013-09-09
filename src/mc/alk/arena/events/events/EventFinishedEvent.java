package mc.alk.arena.events.events;

import mc.alk.arena.competition.events.Event;

public class EventFinishedEvent extends EventEvent {
	public EventFinishedEvent(Event event){
		super(event);
	}
}
