package mc.alk.arena.events.matches;

import mc.alk.arena.competition.match.Match;


public class MatchTimerIntervalEvent extends MatchEvent {
	int secondsRemaining;
	public MatchTimerIntervalEvent(Match match, int remaining) {
		super(match);
		this.secondsRemaining = remaining;
	}

	public int getSecondsRemaining(){
		return secondsRemaining;
	}

}
