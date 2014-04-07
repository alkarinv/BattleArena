package mc.alk.arena.events.matches;

import mc.alk.arena.competition.match.Match;
import mc.alk.arena.objects.MatchResult;
import mc.alk.arena.objects.WinLossDraw;
import mc.alk.arena.objects.teams.ArenaTeam;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.SortedMap;

public class MatchFindCurrentLeaderEvent extends MatchEvent {
	final List<ArenaTeam> teams;
	MatchResult result = new MatchResult();
	final boolean matchEnding;

    public MatchFindCurrentLeaderEvent(Match match) {
        this(match, match.getTeams(), false);
    }

    public MatchFindCurrentLeaderEvent(Match match, List<ArenaTeam> teams) {
        this(match, teams, false);
    }

	public MatchFindCurrentLeaderEvent(Match match, List<ArenaTeam> teams, boolean matchEnding) {
		super(match);
		this.teams = teams;
		this.matchEnding = matchEnding;
	}

	public List<ArenaTeam> getTeams() {
		return teams;
	}

	public Set<ArenaTeam> getCurrentLeaders() {
		return result.getVictors();
	}

	public void setCurrentLeader(ArenaTeam currentLeader) {
		result.setVictor(currentLeader);
		result.setResult(WinLossDraw.WIN);
	}

	public void setCurrentLeaders(Collection<ArenaTeam> currentLeaders) {
		result.setVictors(currentLeaders);
		result.setResult(WinLossDraw.WIN);
	}

	public void setCurrentDrawers(Collection<ArenaTeam> currentLeaders) {
		result.setDrawers(currentLeaders);
		result.setResult(WinLossDraw.DRAW);
	}

	public void setCurrentLosers(Collection<ArenaTeam> currentLosers) {
		result.setLosers(currentLosers);
	}

	public MatchResult getResult(){
		return result;
	}

	public void setResult(MatchResult result){
		this.result = result;
	}

	public boolean isMatchEnding(){
		return matchEnding;
	}

    public SortedMap<Integer,Collection<ArenaTeam>> getRanking() {
        return result.getRanking();
    }
}
