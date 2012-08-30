package mc.alk.arena.objects;

import mc.alk.arena.objects.teams.Team;


public class QPosTeamPair {
	public ArenaParams q;
	public final int pos;
	public final Team t;
	public final int playersInQueue;
	public QPosTeamPair(){this(null,-1,-1,null);}
	
	public QPosTeamPair(ArenaParams q, int pos, int playersInQueue, Team t){
		this.q=q;
		this.pos=pos;
		this.t =t;
		this.playersInQueue = playersInQueue;
	}
}
