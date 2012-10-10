package mc.alk.arena.events.matches;

import java.util.List;

import mc.alk.arena.competition.match.Match;
import mc.alk.arena.objects.teams.Team;

public class MatchFindCurrentLeaderEvent extends MatchEvent {
	final List<Team> teams;
	Team currentLeader = null;
	
	public MatchFindCurrentLeaderEvent(Match match, List<Team> teams) {
		super(match);
		this.teams = teams;
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
