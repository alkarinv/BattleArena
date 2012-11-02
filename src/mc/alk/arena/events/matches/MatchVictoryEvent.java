package mc.alk.arena.events.matches;

import mc.alk.arena.competition.match.Match;
import mc.alk.arena.objects.MatchResult;

public class MatchVictoryEvent extends MatchEvent {
	MatchResult matchResult;

	public MatchVictoryEvent(Match match, MatchResult matchResult) {
		super(match);
		this.matchResult = matchResult;
	}

	public MatchResult getMatchResult() {
		return matchResult;
	}
	public void setMatchResult(MatchResult matchResult) {
		this.matchResult = matchResult;
	}
}
