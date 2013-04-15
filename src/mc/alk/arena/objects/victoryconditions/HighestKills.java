package mc.alk.arena.objects.victoryconditions;

import java.util.Collection;
import java.util.List;
import java.util.TreeMap;

import mc.alk.arena.competition.match.Match;
import mc.alk.arena.objects.scoreboard.ArenaScoreboard;
import mc.alk.arena.objects.teams.ArenaTeam;
import mc.alk.arena.objects.victoryconditions.interfaces.DefinesNumLivesPerPlayer;
import mc.alk.arena.objects.victoryconditions.interfaces.ScoreTracker;
import mc.alk.arena.util.VictoryUtil;

public class HighestKills extends VictoryCondition implements DefinesNumLivesPerPlayer, ScoreTracker{
	PlayerKills pkills;

	public HighestKills(Match match) {
		super(match);
		pkills = new PlayerKills(match);
		match.addArenaListener(pkills);
	}

	@Override
	public List<ArenaTeam> getLeaders() {
		return VictoryUtil.getLeaderByHighestKills(match);
	}

	@Override
	public TreeMap<Integer,Collection<ArenaTeam>> getRanks() {
		return VictoryUtil.getRankingByHighestKills(match.getTeams());
	}

	@Override
	public int getLivesPerPlayer() {
		return Integer.MAX_VALUE;
	}

	@Override
	public void setScoreBoard(ArenaScoreboard scoreboard) {
		this.pkills.setScoreBoard(scoreboard);
	}
}
