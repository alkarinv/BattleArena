package mc.alk.arena.objects.queues;

import java.util.LinkedList;

import mc.alk.arena.objects.arenas.Arena;


public class ArenaQueue extends LinkedList<Arena> {
	private static final long serialVersionUID = 1L;

	@Override
	public void addLast(Arena arena){
		for (Arena a : this){
			if (a.getName().equals(arena.getName()))
				return;
		}
		super.addLast(arena);
	}

}
