package mc.alk.arena.events.events;

import mc.alk.arena.competition.events.Event;
import mc.alk.arena.events.BAEvent;

public class EventCompletedEvent extends BAEvent {
	final Event event;
	public EventCompletedEvent(Event event){
		this.event = event;
	}
	public Event getEvent(){
		return event;
	}
}
