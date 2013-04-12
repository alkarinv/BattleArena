package mc.alk.arena.objects.victoryconditions;

import mc.alk.arena.BattleArena;
import mc.alk.arena.competition.match.Match;

public abstract class VictoryCondition extends ChangeStateCondition  {
	final VictoryType vt;

	public VictoryCondition(Match match){
		super(match);
		if (!VictoryType.registered(this)){
			VictoryType.register(this.getClass(), BattleArena.getSelf());
		}
		this.vt = VictoryType.getType(this.getClass());
	}

	@Override
	public String toString(){
		return getName();
	}

	public String getName() {
		return "[VC "+vt.getName()+"]";
	}


}
