package mc.alk.arena.events.events;

import java.util.Collection;

import mc.alk.arena.competition.events.Event;
import mc.alk.arena.events.BAEvent;
import mc.alk.arena.objects.teams.Team;

public class EventStartEvent extends BAEvent {
	final Event event;
	final Collection<Team> teams;
	public EventStartEvent(Event event, Collection<Team> teams) {
		this.event = event;
		this.teams = teams;
	}
	public Collection<Team> getTeams() {
		return teams;
	}

	public Event getEvent(){
		return event;
	}
}
