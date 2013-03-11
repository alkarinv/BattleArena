package mc.alk.arena.events;

import mc.alk.arena.objects.ArenaPlayer;

/**
 * Player has either typed command or clicked block to say they are ready
 */
public class PlayerReadyEvent extends BAEvent{
	final ArenaPlayer arenaPlayer;
	boolean isReady;

	public PlayerReadyEvent(ArenaPlayer arenaPlayer, boolean isReady) {
		this.arenaPlayer = arenaPlayer;
		this.isReady = isReady;
	}

	public ArenaPlayer getPlayer(){
		return arenaPlayer;
	}

	public boolean isReady(){
		return isReady;
	}
}
