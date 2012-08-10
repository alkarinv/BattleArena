package mc.alk.arena.listeners;

import mc.alk.arena.match.Match;

public interface MatchListener {

	void matchComplete(Match am);
	void matchCancelled(Match am);

}
