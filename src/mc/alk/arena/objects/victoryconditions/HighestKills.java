package mc.alk.arena.objects.victoryconditions;

import java.util.Collection;
import java.util.List;
import java.util.TreeMap;

import mc.alk.arena.competition.match.Match;
import mc.alk.arena.objects.teams.Team;
import mc.alk.arena.objects.victoryconditions.extensions.PvPCount;
import mc.alk.arena.objects.victoryconditions.interfaces.DefinesLeaderRanking;
import mc.alk.arena.objects.victoryconditions.interfaces.DefinesNumLivesPerPlayer;
import mc.alk.arena.util.VictoryUtil;

public class HighestKills extends VictoryCondition implements DefinesLeaderRanking, DefinesNumLivesPerPlayer{
	PvPCount pvpcount;

	public HighestKills(Match match) {
		super(match);
		pvpcount = new PvPCount(match);
		match.addArenaListener(pvpcount);
	}

	@Override
	public List<Team> getLeaders() {
		return VictoryUtil.getLeaderByHighestKills(match);
	}

	@Override
	public TreeMap<Integer,Collection<Team>> getRanks() {
		return VictoryUtil.getRankingByHighestKills(match.getTeams());
	}

	@Override
	@Deprecated
	public List<Team> getRankings() {
		return VictoryUtil.getRanksByHighestKills(match.getTeams());
	}

	@Override
	public int getLivesPerPlayer() {
		return Integer.MAX_VALUE;
	}

}
