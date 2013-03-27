package mc.alk.arena.listeners;

import mc.alk.arena.objects.arenas.ArenaListener;
import mc.alk.arena.objects.events.EventPriority;
import mc.alk.arena.objects.events.MatchEventMethod;

public class RListener implements Comparable<RListener> {
	ArenaListener al;
	MatchEventMethod mem;

	public RListener(ArenaListener spl, MatchEventMethod mem) {
		this.al = spl;
		this.mem = mem;
	}
	public boolean isSpecificPlayerMethod(){
		return mem.isSpecificPlayerMethod();
	}
	public MatchEventMethod getMethod() {
		return mem;
	}

	public ArenaListener getListener() {
		return al;
	}

	public EventPriority getPriority() {
		return mem.getPriority();
	}

	@Override
	public int compareTo(RListener arg0) {
		int c = this.mem.getPriority().compareTo(arg0.mem.getPriority());
		if (c != 0)
			return c;
		if (this.al == arg0.al){
			return this.mem.getMethod().getName().compareTo(arg0.mem.getMethod().getName());}
		return this.al.getClass().toString().compareTo(arg0.al.getClass().toString());
	}

	@Override
	public String toString(){
		return "["+this.al.getClass().getSimpleName()+" : " + this.mem +"]";
	}
}
