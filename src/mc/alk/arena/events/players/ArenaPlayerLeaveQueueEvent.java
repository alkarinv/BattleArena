package mc.alk.arena.events.players;

import mc.alk.arena.objects.ArenaPlayer;
import mc.alk.arena.objects.MatchParams;
import mc.alk.arena.objects.teams.ArenaTeam;

public class ArenaPlayerLeaveQueueEvent extends ArenaPlayerEvent{
	final ArenaTeam team;
	final MatchParams params;

	public ArenaPlayerLeaveQueueEvent(ArenaPlayer arenaPlayer, ArenaTeam team, MatchParams params) {
		super(arenaPlayer);
		this.team = team;
		this.params = params;
	}

	public ArenaTeam getTeam() {
		return team;
	}

	public MatchParams getParams(){
		return params;
	}
}
