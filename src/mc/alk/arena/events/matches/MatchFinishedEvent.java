package mc.alk.arena.events.matches;

import mc.alk.arena.competition.match.Match;
import mc.alk.arena.events.BAEvent;
import mc.alk.arena.objects.MatchState;

public class MatchFinishedEvent extends BAEvent {
	final Match match;
	final MatchState state;
	
	public MatchFinishedEvent(Match match){
		this.match = match;
		this.state = match.getMatchState();
	}

	public Match getMatch() {
		return match;
	}

	public MatchState getState() {
		return state;
	}
}
