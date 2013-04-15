package mc.alk.arena.events.teams;

import mc.alk.arena.events.BAEvent;
import mc.alk.arena.objects.teams.ArenaTeam;

public class TeamLeftQueueEvent extends BAEvent{
	final ArenaTeam team;

	public TeamLeftQueueEvent(ArenaTeam team) {
		this.team = team;
	}

	public ArenaTeam getTeam(){
		return team;
	}
}
