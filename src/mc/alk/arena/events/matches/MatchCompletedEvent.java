package mc.alk.arena.events.matches;

import mc.alk.arena.competition.match.Match;

public class MatchCompletedEvent extends MatchEvent {
	public MatchCompletedEvent(Match match){
		super(match);
	}
}
