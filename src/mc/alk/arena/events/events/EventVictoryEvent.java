package mc.alk.arena.events.events;

import java.util.Collection;

import mc.alk.arena.competition.events.Event;
import mc.alk.arena.events.BAEvent;
import mc.alk.arena.objects.teams.Team;

public class EventVictoryEvent extends BAEvent {
	final Event event;

	final Collection<Team> victors;
	final Collection<Team> losers;

	public EventVictoryEvent(Event event, Collection<Team> victors, Collection<Team> losers) {
		this.event = event;
		this.victors = victors;
		this.losers = losers;
	}

	public Collection<Team> getVictors() {
		return victors;
	}
	public Collection<Team> getLosers() {
		return losers;
	}

	public Event getEvent(){
		return event;
	}
}
