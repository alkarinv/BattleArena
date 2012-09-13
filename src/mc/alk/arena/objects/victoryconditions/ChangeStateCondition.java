package mc.alk.arena.objects.victoryconditions;

import java.util.Random;

import mc.alk.arena.competition.match.Match;
import mc.alk.arena.listeners.ArenaListener;
import mc.alk.arena.listeners.TransitionListener;

public class ChangeStateCondition implements ArenaListener, TransitionListener{
	static Random rand = new Random(); /// Our randomizer
	
	final Match match;
	
	public ChangeStateCondition(Match match){
		this.match = match;
	}
}
