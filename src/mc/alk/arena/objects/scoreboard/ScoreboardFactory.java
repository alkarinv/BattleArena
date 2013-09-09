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

	public static ArenaScoreboard createScoreboard(Match match, MatchParams params) {
		if (!Defaults.USE_SCOREBOARD || Defaults.TESTSERVER)
			return new ArenaScoreboard(match, params);
		return hasBukkitScoreboard ? new ArenaBukkitScoreboard(match, params) :  new ArenaScoreboard(match, params);
	}

//	public static ArenaObjective createObjective(String name, String criteria, int priority) {
//		if (!Defaults.USE_SCOREBOARD || Defaults.TESTSERVER)
//			return new ArenaObjective(match, params);
//		return hasBukkitScoreboard ? new ArenaObjective(name,criteria,priority) :
//			new BAObjective(name,criteria,priority);
//	}

	public static boolean hasBukkitScoreboard(){
		return hasBukkitScoreboard;
	}


}
