package mc.alk.arena.events.matches;

import java.util.List;

import mc.alk.arena.competition.match.Match;
import mc.alk.arena.events.BAEvent;
import mc.alk.arena.objects.teams.Team;

public class MatchFindCurrentLeaderEvent extends BAEvent {
	final Match match;
	final List<Team> teams;
	Team currentLeader = null;
	
	public MatchFindCurrentLeaderEvent(Match match, List<Team> teams) {
		this.match = match;
		this.teams = teams;
	}
	
	public Match getMatch() {
		return match;
	}

	public List<Team> getTeams() {
		return teams;
	}

	public Team getCurrentLeader() {
		return currentLeader;
	}

	public void setCurrentLeader(Team currentLeader) {
		this.currentLeader = currentLeader;
	}

}
