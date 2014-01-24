package mc.alk.arena.events.matches;

import mc.alk.arena.competition.match.Match;
import mc.alk.arena.events.CompetitionEvent;

public class MatchEvent extends CompetitionEvent {
	public MatchEvent(Match match) {
		super();
		setCompetition(match);
	}

	/**
	 * Returns the match for this event
	 * @return Match
	 */
	public Match getMatch() {
		return (Match) getCompetition();
	}
}
