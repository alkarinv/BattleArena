package mc.alk.arena.events.events;

import mc.alk.arena.competition.events.Event;
import mc.alk.arena.events.CompetitionEvent;

public class EventEvent extends CompetitionEvent{
	public EventEvent(Event event) {
		super();
		setCompetition(event);
	}

	/**
	 * Returns the match for this event
	 * @return Match
	 */
	public Event getEvent() {
		return (Event) getCompetition();
	}
}
