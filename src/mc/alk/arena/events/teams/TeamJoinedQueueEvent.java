package mc.alk.arena.events.teams;

import mc.alk.arena.events.BAEvent;
import mc.alk.arena.objects.ArenaParams;
import mc.alk.arena.objects.pairs.QueueResult;
import mc.alk.arena.objects.teams.ArenaTeam;

public class TeamJoinedQueueEvent extends BAEvent{
	final ArenaTeam team;
	final int playersInQueue;
	final int teamsInQueue;
	final int pos;
	final Long timeToStart;
	final ArenaParams params;

	public TeamJoinedQueueEvent(ArenaTeam team, QueueResult qpp) {
		this.team = team;
		this.playersInQueue = qpp.playersInQueue;
		this.pos = qpp.pos;
		this.timeToStart = qpp.time;
		this.params = qpp.params;
		this.teamsInQueue = qpp.teamsInQueue;
	}


	public ArenaTeam getTeam() {
		return team;
	}

	public int getPlayersInQueue() {
		return playersInQueue;
	}

	public int getPos() {
		return pos;
	}

	public Long getTimeToStart() {
		return timeToStart;
	}

	public ArenaParams getParams() {
		return params;
	}

	public int getTeamsInQueue() {
		return teamsInQueue;
	}
}
