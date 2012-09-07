package mc.alk.arena.objects.victoryconditions;

import java.util.Random;

import mc.alk.arena.competition.match.Match;
import mc.alk.arena.listeners.ArenaListener;

public class ChangeStateCondition implements ArenaListener{
	static Random rand = new Random(); /// Our randomizer
	
	final Match match;

	final Integer timeInterval;
	
	public ChangeStateCondition(Match match){
		this.match = match;
		timeInterval = null;
	}
}
