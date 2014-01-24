package mc.alk.arena.events.teams;

import mc.alk.arena.events.CompetitionEvent;
import mc.alk.arena.objects.teams.ArenaTeam;

public class TeamDeathEvent extends CompetitionEvent{
	final ArenaTeam team;

	public TeamDeathEvent(ArenaTeam team) {
		this.team = team;
	}

	public ArenaTeam getTeam() {
		return team;
	}
}
