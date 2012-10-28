package mc.alk.arena.events.matches;

import mc.alk.arena.competition.match.Match;
import mc.alk.arena.objects.MatchState;

public class MatchFinishedEvent extends MatchEvent {
	final MatchState state;

	public MatchFinishedEvent(Match match){
		super(match);
		this.state = match.getState();
	}

	public MatchState getState() {
		return state;
	}
}
