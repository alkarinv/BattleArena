package mc.alk.arena.events.players;

import mc.alk.arena.objects.ArenaPlayer;
import mc.alk.arena.objects.teams.ArenaTeam;

public class ArenaPlayerLeaveLobbyEvent extends ArenaPlayerEvent{
	final ArenaTeam team;

	public ArenaPlayerLeaveLobbyEvent(ArenaPlayer arenaPlayer, ArenaTeam team) {
		super(arenaPlayer);
		this.team = team;
	}

	public ArenaTeam getTeam() {
		return team;
	}

}
