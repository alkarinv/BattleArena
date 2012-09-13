package mc.alk.arena.events.matches;

import mc.alk.arena.competition.match.Match;
import mc.alk.arena.events.CancellableEvent;

public class MatchOpenEvent extends CancellableEvent {
	final Match match;
	public MatchOpenEvent(Match match){
		this.match = match;
	}
	public Match getMatch(){
		return match;
	}
}
