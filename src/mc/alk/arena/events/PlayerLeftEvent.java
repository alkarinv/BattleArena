package mc.alk.arena.events;

import mc.alk.arena.objects.ArenaPlayer;

public class PlayerLeftEvent extends BAEvent{
	final ArenaPlayer arenaPlayer;

	public PlayerLeftEvent(ArenaPlayer arenaPlayer) {
		this.arenaPlayer = arenaPlayer;
	}

	public ArenaPlayer getPlayer(){
		return arenaPlayer;
	}

}
