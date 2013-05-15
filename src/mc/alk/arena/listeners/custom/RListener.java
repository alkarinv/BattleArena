package mc.alk.arena.listeners.custom;

import java.util.Comparator;

import mc.alk.arena.objects.arenas.ArenaListener;
import mc.alk.arena.objects.events.ArenaEventMethod;
import mc.alk.arena.objects.events.EventPriority;

public class RListener {
	ArenaListener al;
	ArenaEventMethod mem;

	public RListener(ArenaListener spl, ArenaEventMethod mem) {
		this.al = spl;
		this.mem = mem;
	}
	public boolean isSpecificPlayerMethod(){
		return mem.isSpecificPlayerMethod();
	}
	public ArenaEventMethod getMethod() {
		return mem;
	}

	public ArenaListener getListener() {
		return al;
	}

	public EventPriority getPriority() {
		return mem.getPriority();
	}

	@Override
	public String toString(){
		return "["+this.al.getClass().getSimpleName()+" : " + this.mem +"]";
	}

	public static class RListenerPriorityComparator implements Comparator<RListener>{
		@Override
		public int compare(RListener o1, RListener o2) {
			int c = o1.getMethod().getPriority().compareTo(o2.getMethod().getPriority());
			if (c != 0)
				return c;
			if (o1.getListener() == o2.getListener()){
				return o1.getMethod().getMethod().getName().compareTo(o2.getMethod().getMethod().getName());}
			return o1.getListener().getClass().toString().compareTo(o2.getListener().getClass().toString());
		}
	}
}
