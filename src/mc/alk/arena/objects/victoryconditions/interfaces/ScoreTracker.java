package mc.alk.arena.objects.victoryconditions.interfaces;

import mc.alk.arena.objects.scoreboard.ArenaScoreboard;

public interface ScoreTracker extends DefinesLeaderRanking {
	public void setScoreBoard(ArenaScoreboard scoreboard);

	public void setDisplayTeams(boolean display);

}
