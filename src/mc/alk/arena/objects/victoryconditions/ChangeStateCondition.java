package mc.alk.arena.objects.victoryconditions;

import java.util.Random;

import mc.alk.arena.competition.match.Match;
import mc.alk.arena.objects.arenas.ArenaListener;

public class ChangeStateCondition implements ArenaListener{
	static Random rand = new Random(); /// Our randomizer

	protected final Match match;

	public ChangeStateCondition(Match match){
		this.match = match;
	}
}
