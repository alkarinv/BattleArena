package mc.alk.arena.events.arenas;

import mc.alk.arena.events.BAEvent;
import mc.alk.arena.objects.arenas.Arena;

public class ArenaDeleteEvent extends BAEvent{
	final Arena arena;
	
	public Arena getArena() {
		return arena;
	}
	public ArenaDeleteEvent(Arena arena) {
		this.arena = arena;
	}

}
