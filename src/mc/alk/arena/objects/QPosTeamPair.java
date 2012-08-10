package mc.alk.arena.objects;

import mc.alk.arena.objects.teams.Team;


public class QPosTeamPair {
	public ArenaParams q;
	public int pos;
	public Team t;
	public QPosTeamPair(){q= null; pos =-1;t = null;}
	public QPosTeamPair(ArenaParams q, int pos, Team t){this.q=q;this.pos=pos;this.t =t;}
}
