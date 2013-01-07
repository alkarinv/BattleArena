package mc.alk.arena.events.matches;

import mc.alk.arena.events.BAEvent;
import mc.alk.arena.objects.ArenaParams;
import mc.alk.arena.objects.pairs.QPosTeamPair;
import mc.alk.arena.objects.teams.Team;

public class TeamJoinedQueueEvent extends BAEvent{
	final Team team;
	final int playersInQueue;
	final int teamsInQueue;
	final int pos;
	final Long timeToStart;
	final ArenaParams params;

	public TeamJoinedQueueEvent(QPosTeamPair qpp) {
		this.team = qpp.t;
		this.playersInQueue = qpp.playersInQueue;
		this.pos = qpp.pos;
		this.timeToStart = qpp.time;
		this.params = qpp.params;
		this.teamsInQueue = qpp.teamsInQueue;
	}


	public Team getTeam() {
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
