package mc.alk.arena.events.players;

import mc.alk.arena.objects.ArenaPlayer;
import mc.alk.arena.objects.teams.ArenaTeam;

public class ArenaPlayerEnterLobbyEvent extends ArenaPlayerEvent{
	final ArenaTeam team;

	public ArenaPlayerEnterLobbyEvent(ArenaPlayer arenaPlayer, ArenaTeam team) {
		super(arenaPlayer);
		this.team = team;
	}

	public ArenaTeam getTeam() {
		return team;
	}

}
