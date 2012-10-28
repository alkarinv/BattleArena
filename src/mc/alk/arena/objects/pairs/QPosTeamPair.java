package mc.alk.arena.objects.pairs;

import mc.alk.arena.objects.ArenaParams;
import mc.alk.arena.objects.MatchParams;
import mc.alk.arena.objects.queues.QueueObject;
import mc.alk.arena.objects.teams.Team;


public class QPosTeamPair {
	public ArenaParams q;
	public final int pos;
	public final Team t;
	public final int playersInQueue;
	public QPosTeamPair(){this(null,-1,-1,(Team)null);}
	
	public QPosTeamPair(ArenaParams q, int pos, int playersInQueue, Team t){
		this.q=q;
		this.pos=pos;
		this.t =t;
		this.playersInQueue = playersInQueue;
	}

	public QPosTeamPair(MatchParams q, int pos, int playersInQueue, QueueObject to) {
		this.q=q;
		this.pos=pos;
		this.t =null;
		this.playersInQueue = playersInQueue;
	}
}
