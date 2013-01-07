package mc.alk.arena.events.events;

import mc.alk.arena.competition.events.Event;
import mc.alk.arena.events.BAEvent;
import mc.alk.arena.objects.CompetitionResult;

public class EventResultEvent extends BAEvent {
	final Event event;

	final CompetitionResult result;
	public EventResultEvent(Event event, CompetitionResult result) {
		this.event = event;
		this.result = result;
	}

	public Event getEvent(){
		return event;
	}

	public CompetitionResult getResult(){
		return result;
	}
}
