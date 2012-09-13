package mc.alk.arena.objects;

import mc.alk.arena.objects.arenas.ArenaType;
import mc.alk.arena.objects.victoryconditions.VictoryType;


public class EventParams extends MatchParams{

	public EventParams(MatchParams q) {
		super(q);
	}

	public EventParams(ArenaType at, Rating rating, VictoryType vc) {
		super(at, rating, vc);
	}

}
