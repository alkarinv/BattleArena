package mc.alk.arena.objects.scoreboard;

import mc.alk.arena.objects.MatchParams;

public class ScoreboardFactory {

	public static ArenaScoreboard createScoreboard(MatchParams params) {
		try {
			Class.forName("org.bukkit.scoreboard.Scoreboard");
			return new BukkitScoreboard(params);
		} catch (ClassNotFoundException e) {
			return new ArenaScoreboard(params);
		}
	}

}
