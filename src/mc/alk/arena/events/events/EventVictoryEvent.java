package mc.alk.arena.events.events;

import java.util.Collection;

import mc.alk.arena.competition.events.Event;
import mc.alk.arena.events.BAEvent;
import mc.alk.arena.objects.teams.Team;

public class EventVictoryEvent extends BAEvent {
	final Event event;

	final Team victor;
	final Collection<Team> losers;

	public EventVictoryEvent(Event event, Team victor, Collection<Team> losers) {
		this.event = event;
		this.victor = victor;
		this.losers = losers;
	}

	public Team getVictor() {
		return victor;
	}
	public Collection<Team> getLosers() {
		return losers;
	}

	public Event getEvent(){
		return event;
	}
}
