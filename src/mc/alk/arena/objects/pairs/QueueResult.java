package mc.alk.arena.objects.pairs;

import mc.alk.arena.competition.match.Match;
import mc.alk.arena.objects.MatchParams;
import mc.alk.arena.objects.queues.QueueObject;
import mc.alk.arena.objects.teams.Team;


public class QueueResult {
	public static enum QueueStatus{
		ADDED_TO_QUEUE, ADDED_TO_EXISTING_MATCH, MATCH_FOUND, QUEUE_BUSY, INVALID_SIZE, ERROR
	}
	public static enum TimeStatus{
		UNKNOWN, CANT_FORCESTART, TIME_EXPIRED, TIME_ONGOING
	}

	public Match match;
	public MatchParams params;
	public int pos;
	public Team team;
	public int playersInQueue; ///
	public int teamsInQueue; ///
	public int neededPlayers;
	public Long time;
	public QueueStatus status = QueueStatus.ADDED_TO_QUEUE;
	public TimeStatus timeStatus = TimeStatus.UNKNOWN;
	public QueueResult(){}

	public QueueResult(Match match, MatchParams params, int pos, int playersInQueue, Team t, int teamsInQueue){
		this.match = match;
		this.params=params;
		this.pos=pos;
		this.team =t;
		this.playersInQueue = playersInQueue;
		this.teamsInQueue = teamsInQueue;
		this.neededPlayers = params.getMaxPlayers();
	}

	public QueueResult(Match match, MatchParams params, int pos, int playersInQueue, QueueObject to, int teamsInQueue) {
		this.match = match;
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
