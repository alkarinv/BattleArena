package mc.alk.arena.objects.scoreboard;

import mc.alk.arena.Defaults;
import mc.alk.arena.competition.match.Match;
import mc.alk.arena.objects.MatchParams;

public class ScoreboardFactory {
	private static boolean hasBukkitScoreboard = false;
	static{
		try {
			Class.forName("org.bukkit.scoreboard.Scoreboard");
			ScoreboardFactory.hasBukkitScoreboard = true;
		} catch (ClassNotFoundException e) {
			ScoreboardFactory.hasBukkitScoreboard = false;
		}
	}

	public static ArenaScoreboard createScoreboard(Match match, MatchParams params) {
		if (!Defaults.USE_SCOREBOARD || Defaults.TESTSERVER)
			return new ArenaScoreboard(match, params);
		return hasBukkitScoreboard ? new BukkitScoreboard(match, params) :  new ArenaScoreboard(match, params);
	}

	public static boolean hasBukkitScoreboard(){
		return hasBukkitScoreboard;
	}
}
