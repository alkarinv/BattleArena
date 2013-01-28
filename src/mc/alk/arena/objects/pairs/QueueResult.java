package mc.alk.arena.objects.pairs;

import mc.alk.arena.competition.match.Match;
import mc.alk.arena.objects.MatchParams;
import mc.alk.arena.objects.queues.QueueObject;
import mc.alk.arena.objects.teams.Team;


public class QueueResult {
	public static enum QueueStatus{
		ADDED, ADDED_, RESU
	}
	public static enum TimeStatus{

	}

	public Match match;
	public final MatchParams params;
	public final int pos;
	public final Team team;
	public int playersInQueue; ///
	public int teamsInQueue; ///
	public final int neededPlayers;
	public Long time;

	public QueueResult(){this(null,-1,-1,(Team)null,-1);}

	public QueueResult(MatchParams params, int pos, int playersInQueue, Team t, int teamsInQueue){
		this.params=params;
		this.pos=pos;
		this.team =t;
		this.playersInQueue = playersInQueue;
		this.teamsInQueue = teamsInQueue;
		this.neededPlayers = params.getMaxPlayers();
	}

	public QueueResult(MatchParams params, int pos, int playersInQueue, QueueObject to, int teamsInQueue) {
		this.params=params;
		this.pos=pos;
		this.team =null;
		this.playersInQueue = playersInQueue;
		this.teamsInQueue = teamsInQueue;
		this.neededPlayers = params.getMaxPlayers();
	}

//	public QPosTeamPair(MatchParams params, FindMatchResult fmr, Team team, int teamsInQueue){
//		this.params = (fmr != null) ? fmr.params : params;
//		this.pos = (fmr !=null) ? fmr.peopleWaitingForThisQueue: 1;
//		this.team = team;
//		this.playersInQueue = (fmr!=null) ? fmr.peopleWaitingForThisQueue : 1;
//		this.teamsInQueue = teamsInQueue;
//		this.neededPlayers = params.getMaxPlayers();
//		this.time = (fmr != null ) ? fmr.timeRemaining : null;
//	}
}
