package mc.alk.arena.objects.pairs;

import mc.alk.arena.objects.MatchParams;

public class JoinResult {
	public static enum JoinStatus{
		NONE, ADDED, ADDED_TO_EXISTING_MATCH, ADDED_TO_ARENA_QUEUE, ADDED_TO_QUEUE, STARTED_NEW_GAME,
        CANT_FIT, NOT_OPEN, NOTOPEN, WAITING_FOR_PLAYERS, ERROR
	}

	public static enum TimeStatus{
		UNKNOWN, CANT_FORCESTART, TIME_EXPIRED, TIME_ONGOING
	}

	public MatchParams params;
	public int pos;
	public int playersInQueue; ///
	public int maxPlayers;
	public Long time;
	public JoinStatus status = JoinStatus.NONE;

}
