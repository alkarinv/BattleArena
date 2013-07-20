package mc.alk.arena.objects.queues;

import java.util.Iterator;
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

	@Override
	public boolean remove(Object obj){
		if (!(obj instanceof Arena))
			return false;
		Arena arena = (Arena) obj;
		Iterator<Arena> iter = this.iterator();
		while (iter.hasNext() ){
			if (iter.next().getName().equals(arena.getName())){
				iter.remove();
				return true;
			}
		}
		return false;
	}

}
