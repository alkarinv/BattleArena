package mc.alk.arena.objects.pairs;

import mc.alk.arena.objects.MatchParams;
import mc.alk.arena.objects.arenas.Arena;
import mc.alk.arena.objects.queues.ArenaMatchQueue;
import mc.alk.arena.objects.queues.ArenaMatchQueue.QueueType;
import mc.alk.arena.objects.teams.ArenaTeam;

public class ParamTeamPair {
	public final MatchParams params;
	public final ArenaTeam team;
	public final QueueType type;
	public final Arena arena;
	public final int nPlayersInQueue;

	public ParamTeamPair(){this(null,null,null,null,-1);}

	public ParamTeamPair(MatchParams q, ArenaTeam t, ArenaMatchQueue.QueueType type){
		this(q,t,type,null,-1);
	}

	public ParamTeamPair(MatchParams matchParams, ArenaTeam t,
			QueueType type, Arena arena, int playersInQueue) {
		this.params=matchParams;
		this.team=t;
		this.type=type;
		this.arena = arena;
		this.nPlayersInQueue = playersInQueue;
	}

	public MatchParams getMatchParams() {return params;}

	public Arena getArena(){
		return arena;
	}
	public int getNPlayersInQueue(){
		return this.nPlayersInQueue;
	}
}
