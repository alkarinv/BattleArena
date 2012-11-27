package mc.alk.arena.events.matches;

import java.util.ArrayList;
import java.util.List;

import mc.alk.arena.competition.match.Match;
import mc.alk.arena.objects.teams.Team;

public class MatchFindCurrentLeaderEvent extends MatchEvent {
	final List<Team> teams;
	List<Team> currentLeaders = null;

	public MatchFindCurrentLeaderEvent(Match match, List<Team> teams) {
		super(match);
		this.teams = teams;
	}

	public List<Team> getTeams() {
		return teams;
	}

	public List<Team> getCurrentLeaders() {
		return currentLeaders;
	}

	public void setCurrentLeader(Team currentLeader) {
		if (currentLeaders==null) currentLeaders = new ArrayList<Team>();
		this.currentLeaders.clear();
		this.currentLeaders.add(currentLeader);
	}
	public void setCurrentLeaders(List<Team> currentLeaders) {
		if (this.currentLeaders==null) this.currentLeaders = new ArrayList<Team>();
		this.currentLeaders.clear();
		this.currentLeaders.addAll(currentLeaders);
	}
}
