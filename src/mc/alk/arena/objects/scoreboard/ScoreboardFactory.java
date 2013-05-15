package mc.alk.arena.objects.scoreboard;

import mc.alk.arena.Defaults;
import mc.alk.arena.competition.match.Match;
import mc.alk.arena.objects.MatchParams;

public class ScoreboardFactory {

	public static ArenaScoreboard createScoreboard(Match match, MatchParams params) {
		if (!Defaults.USE_SCOREBOARD)
			return new ArenaScoreboard(match, params);
		try {
			Class.forName("org.bukkit.scoreboard.Scoreboard");
			return new BukkitScoreboard(match, params);
		} catch (ClassNotFoundException e) {
			return new ArenaScoreboard(match, params);
		}
	}

}
