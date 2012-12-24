package mc.alk.arena.events.matches;

import java.util.Collection;
import java.util.List;
import java.util.Set;

import mc.alk.arena.competition.match.Match;
import mc.alk.arena.objects.MatchResult;
import mc.alk.arena.objects.MatchResult.WinLossDraw;
import mc.alk.arena.objects.teams.Team;

public class MatchFindCurrentLeaderEvent extends MatchEvent {
	final List<Team> teams;
	MatchResult result = new MatchResult();

	public MatchFindCurrentLeaderEvent(Match match, List<Team> teams) {
		super(match);
		this.teams = teams;
	}

	public List<Team> getTeams() {
		return teams;
	}

	public Set<Team> getCurrentLeaders() {
		return result.getVictors();
	}

	public void setCurrentLeader(Team currentLeader) {
		result.setVictor(currentLeader);
		result.setResult(WinLossDraw.WIN);
	}

	public void setCurrentLeaders(Collection<Team> currentLeaders) {
		result.setVictors(currentLeaders);
		result.setResult(WinLossDraw.WIN);
	}

	public void setCurrentDrawers(Collection<Team> currentLeaders) {
		result.setDrawers(currentLeaders);
		result.setResult(WinLossDraw.DRAW);
	}

	public void setCurrentLosers(Collection<Team> currentLosers) {
		result.setLosers(currentLosers);
	}

	public MatchResult getResult(){
		return result;
	}

	public void setResult(MatchResult result){
		this.result = result;
	}
}
