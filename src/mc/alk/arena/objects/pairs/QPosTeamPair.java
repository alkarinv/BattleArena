package mc.alk.arena.objects.pairs;

import mc.alk.arena.objects.ArenaParams;
import mc.alk.arena.objects.MatchParams;
import mc.alk.arena.objects.queues.QueueObject;
import mc.alk.arena.objects.teams.Team;


public class QPosTeamPair {
	public final ArenaParams params;
	public final int pos;
	public final Team t;
	public final int playersInQueue;
	public QPosTeamPair(){this(null,-1,-1,(Team)null);}

	public QPosTeamPair(ArenaParams params, int pos, int playersInQueue, Team t){
		this.params=params;
		this.pos=pos;
		this.t =t;
		this.playersInQueue = playersInQueue;
	}

	public QPosTeamPair(MatchParams params, int pos, int playersInQueue, QueueObject to) {
		this.params=params;
		this.pos=pos;
		this.t =null;
		this.playersInQueue = playersInQueue;
	}
}
