package mc.alk.arena.events.matches;

import mc.alk.arena.competition.match.Match;
import mc.alk.arena.events.BAEvent;

public class MatchCompletedEvent extends BAEvent {
	final Match match;
	public MatchCompletedEvent(Match match){
		this.match = match;
	}
	public Match getMatch(){
		return match;
	}

}
