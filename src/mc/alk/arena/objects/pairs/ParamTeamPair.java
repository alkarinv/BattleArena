package mc.alk.arena.objects.pairs;

import mc.alk.arena.objects.MatchParams;
import mc.alk.arena.objects.teams.ArenaTeam;

public class ParamTeamPair {
	public MatchParams q;
	public ArenaTeam team;
	public ParamTeamPair(){q=null; team =null;}
	public ParamTeamPair(MatchParams q, ArenaTeam t){this.q=q;this.team=t;}
}
