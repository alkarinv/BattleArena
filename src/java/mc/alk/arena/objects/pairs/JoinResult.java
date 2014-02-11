package mc.alk.arena.objects.pairs;

import mc.alk.arena.objects.MatchParams;

public class JoinResult {
	public static enum JoinStatus{
		NONE, ADDED, ADDED_TO_EXISTING_MATCH, ADDED_TO_ARENA_QUEUE, ADDED_TO_QUEUE, STARTED_NEW_GAME,
        CANT_FIT, NOT_OPEN, NOTOPEN, WAITING_FOR_PLAYERS, ERROR
	}
    //		ADDED, CANT_FIT, ADDED_TO_EXISTING, ADDED_STILL_NEEDS_PLAYERS,
	public static enum TimeStatus{
		UNKNOWN, CANT_FORCESTART, TIME_EXPIRED, TIME_ONGOING
	}
//
//	public ArenaMatchQueue.FoundMatch matchfind;
	public MatchParams params;
	public int pos;
//	public ArenaTeam team;
	public int playersInQueue; ///
//	public int teamsInQueue; ///
	public int maxPlayers;
	public Long time;
	public JoinStatus status = JoinStatus.NONE;
//	public TimeStatus timeStatus = TimeStatus.UNKNOWN;
//    public JoinResult(){}
//
//    public JoinResult(JoinStatus status) {
//        this.status = status;
//    }
//
//    public JoinResult(JoinStatus status, int pos, ArenaTeam t) {
//        this.status = status;
//    }
//
//	public JoinResult(ArenaMatchQueue.FoundMatch match, MatchParams params, int pos, int playersInQueue, ArenaTeam t, int teamsInQueue){
//		this.matchfind = match;
//		this.params=params;
//		this.pos=pos;
//		this.team =t;
//		this.playersInQueue = playersInQueue;
//		this.teamsInQueue = teamsInQueue;
//		this.maxPlayers = params.getMaxPlayers();
//	}
//
//	public JoinResult(ArenaMatchQueue.FoundMatch match, MatchParams params, int pos, int playersInQueue, QueueObject to, int teamsInQueue) {
//		this.matchfind = match;
//		this.params=params;
//		this.pos=pos;
//		this.team =null;
//		this.playersInQueue = playersInQueue;
//		this.teamsInQueue = teamsInQueue;
//		this.maxPlayers = params.getMaxPlayers();
//	}

}
