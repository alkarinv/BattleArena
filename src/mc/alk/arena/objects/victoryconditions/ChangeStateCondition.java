package mc.alk.arena.objects.victoryconditions;

import mc.alk.arena.competition.match.Match;
import mc.alk.arena.objects.arenas.ArenaListener;

public class ChangeStateCondition implements ArenaListener{
	protected final Match match;

	public ChangeStateCondition(Match match){
		this.match = match;
	}
}
