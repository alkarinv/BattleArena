package mc.alk.arena.events.events;

import java.util.Collection;

import mc.alk.arena.competition.events.Event;
import mc.alk.arena.events.BAEvent;
import mc.alk.arena.objects.teams.ArenaTeam;

public class EventStartEvent extends BAEvent {
	final Event event;
	final Collection<ArenaTeam> teams;
	public EventStartEvent(Event event, Collection<ArenaTeam> teams) {
		this.event = event;
		this.teams = teams;
	}
	public Collection<ArenaTeam> getTeams() {
		return teams;
	}

	public Event getEvent(){
		return event;
	}
}
