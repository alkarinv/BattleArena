package mc.alk.arena.events;

import mc.alk.arena.objects.teams.Team;

public class TeamLeftQueueEvent extends BAEvent{
	final Team team;

	public TeamLeftQueueEvent(Team team) {
		this.team = team;
	}

	public Team getTeam(){
		return team;
	}
}
