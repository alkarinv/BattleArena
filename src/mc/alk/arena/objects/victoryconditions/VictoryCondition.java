package mc.alk.arena.objects.victoryconditions;

import mc.alk.arena.BattleArena;
import mc.alk.arena.competition.match.Match;
import mc.alk.arena.objects.MatchParams;

public abstract class VictoryCondition extends ChangeStateCondition  {
	final VictoryType vt;

	public VictoryCondition(Match match){
		super(match);
		final MatchParams mp = match.getParams();
		this.vt = mp.getVictoryType();
		if (!VictoryType.registered(this)){
			VictoryType.register(this.getClass(), BattleArena.getSelf());
		}
	}

	public String toString(){
		return getName();
	}

	public String getName() {
		return "[VC "+vt.getName()+"]";
	}
}
