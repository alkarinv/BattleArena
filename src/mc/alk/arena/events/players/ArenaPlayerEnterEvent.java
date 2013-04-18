package mc.alk.arena.events.players;

import mc.alk.arena.objects.ArenaPlayer;
import mc.alk.arena.objects.teams.ArenaTeam;

/**
 * Signifies that the player has typed the command to leave the competition
 */
public class ArenaPlayerEnterEvent extends ArenaPlayerEvent{
	final ArenaTeam team;

	public ArenaPlayerEnterEvent(ArenaPlayer arenaPlayer, ArenaTeam team) {
		super(arenaPlayer);
		this.team = team;
	}

	public ArenaTeam getTeam() {
		return team;
	}

}
