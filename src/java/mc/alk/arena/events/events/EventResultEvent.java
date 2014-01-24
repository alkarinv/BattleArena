package mc.alk.arena.events.events;

import mc.alk.arena.competition.events.Event;
import mc.alk.arena.objects.CompetitionResult;

public class EventResultEvent extends EventEvent {
	final CompetitionResult result;
	public EventResultEvent(Event event, CompetitionResult result) {
		super(event);
		this.result = result;
	}

	public CompetitionResult getResult(){
		return result;
	}
}
