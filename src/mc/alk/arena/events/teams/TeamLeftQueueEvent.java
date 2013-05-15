package mc.alk.arena.events.teams;

import mc.alk.arena.events.BAEvent;
import mc.alk.arena.objects.MatchParams;
import mc.alk.arena.objects.teams.ArenaTeam;

public class TeamLeftQueueEvent extends BAEvent{
	final ArenaTeam team;
	MatchParams params;

	public TeamLeftQueueEvent(ArenaTeam team, MatchParams params) {
		this.team = team;
		this.params = params;
	}

	public ArenaTeam getTeam(){
		return team;
	}
	public MatchParams getParams(){
		return params;
	}
}
