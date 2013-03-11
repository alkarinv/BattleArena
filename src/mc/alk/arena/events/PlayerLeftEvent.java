package mc.alk.arena.events;

import mc.alk.arena.objects.ArenaPlayer;

/**
 * Signifies that the player has typed the command to leave the competition
 */
public class PlayerLeftEvent extends BAEvent{
	final ArenaPlayer arenaPlayer;

	public PlayerLeftEvent(ArenaPlayer arenaPlayer) {
		this.arenaPlayer = arenaPlayer;
	}

	public ArenaPlayer getPlayer(){
		return arenaPlayer;
	}
}
