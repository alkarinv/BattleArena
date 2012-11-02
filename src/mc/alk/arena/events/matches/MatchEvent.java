package mc.alk.arena.events.matches;

import mc.alk.arena.competition.match.Match;
import mc.alk.arena.events.BAEvent;

public class MatchEvent extends BAEvent {
	final Match match;

	public MatchEvent(Match match) {
		this.match = match;
	}

	/**
	 * Returns the match for this event
	 * @return Match
	 */
	public Match getMatch() {
		return match;
	}
}
