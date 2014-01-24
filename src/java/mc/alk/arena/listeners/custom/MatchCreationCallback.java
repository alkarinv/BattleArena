package mc.alk.arena.listeners.custom;

import mc.alk.arena.competition.match.Match;
import mc.alk.arena.objects.tournament.Matchup;

public interface MatchCreationCallback {
	public void matchCreated(Match match, Matchup matchup);
}
