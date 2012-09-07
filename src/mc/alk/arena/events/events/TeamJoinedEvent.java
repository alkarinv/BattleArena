package mc.alk.arena.events.events;

import mc.alk.arena.competition.events.Event;
import mc.alk.arena.events.CancellableEvent;
import mc.alk.arena.objects.teams.Team;

public class TeamJoinedEvent extends CancellableEvent {
	final Event event;
	final Team team;

	public TeamJoinedEvent(Event event,Team team) {
		this.event = event;
		this.team = team;
	}

	public Team getTeam() {
		return team;
	}

	public Event getEvent(){
		return event;
	}
}
