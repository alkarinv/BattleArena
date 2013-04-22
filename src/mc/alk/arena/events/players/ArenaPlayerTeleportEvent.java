package mc.alk.arena.events.players;

import mc.alk.arena.objects.ArenaLocation;
import mc.alk.arena.objects.ArenaPlayer;
import mc.alk.arena.objects.teams.ArenaTeam;

public class ArenaPlayerTeleportEvent extends ArenaPlayerEvent{
	final ArenaTeam team;
	ArenaLocation src;
	ArenaLocation dest;

	public ArenaPlayerTeleportEvent(ArenaPlayer arenaPlayer, ArenaTeam team,
			ArenaLocation src, ArenaLocation dest) {
		super(arenaPlayer);
		this.team = team;
		this.src = src;
		this.dest = dest;
	}

	public ArenaTeam getTeam() {
		return team;
	}

}
