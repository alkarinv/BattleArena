package mc.alk.arena.events.arenas;

import mc.alk.arena.events.BAEvent;
import mc.alk.arena.objects.arenas.Arena;

public class ArenaCreateEvent extends BAEvent{
	final Arena arena;
	
	public Arena getArena() {
		return arena;
	}
	public ArenaCreateEvent(Arena arena) {
		this.arena = arena;
	}

}
