package mc.alk.arena.objects.scoreboard;

import mc.alk.arena.Defaults;
import mc.alk.arena.competition.match.Match;
import mc.alk.arena.objects.MatchParams;
import mc.alk.arena.objects.scoreboard.base.ArenaBukkitScoreboard;

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

    public static ArenaScoreboard createScoreboard(String scoreboardName, MatchParams params) {
        // Intellij warning suppression
        // noinspection PointlessBooleanExpression,ConstantConditions
        return (Defaults.USE_SCOREBOARD && hasBukkitScoreboard && !Defaults.TESTSERVER) ?
                new ArenaBukkitScoreboard(scoreboardName, params) : new ArenaScoreboard(scoreboardName);
    }

    public static ArenaScoreboard createScoreboard(Match match, MatchParams params) {
        return createScoreboard(match.getName(),params);
    }

	public static boolean hasBukkitScoreboard(){
		return hasBukkitScoreboard;
	}

}
