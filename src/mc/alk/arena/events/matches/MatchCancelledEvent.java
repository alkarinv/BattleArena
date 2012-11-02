package mc.alk.arena.events.matches;

import mc.alk.arena.competition.match.Match;
import mc.alk.arena.events.BAEvent;

public class MatchCancelledEvent extends BAEvent {
	final Match match;
	public MatchCancelledEvent(Match match){
		this.match = match;
	}

	/**
	 * Return the match for this event
	 * @return Match
	 */
	public Match getMatch(){
		return match;
	}
}
