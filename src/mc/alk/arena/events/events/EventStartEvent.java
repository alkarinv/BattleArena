package mc.alk.arena.events.events;

import java.util.Set;

import mc.alk.arena.competition.events.Event;
import mc.alk.arena.events.BAEvent;
import mc.alk.arena.objects.teams.Team;

public class EventStartEvent extends BAEvent {
	final Event event;
	final Set<Team> teams;
	public EventStartEvent(Event event, Set<Team> teams) {
		this.event = event;
		this.teams = teams;
	}
	public Set<Team> getTeams() {
		return teams;
	}

	public Event getEvent(){
		return event;
	}
}
