package mc.alk.arena.events.events;

import mc.alk.arena.competition.events.Event;
import mc.alk.arena.events.CancellableEvent;

public class EventOpenEvent extends CancellableEvent {
	final Event event;
	public EventOpenEvent(Event event){
		this.event = event;
	}
	public Event getEvent(){
		return event;
	}
}
