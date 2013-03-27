package mc.alk.arena.objects.victoryconditions.interfaces;

import java.util.Collection;
import java.util.List;
import java.util.TreeMap;

import mc.alk.arena.objects.teams.Team;

public interface DefinesLeaderRanking {

	/**
	 * Returns the list of currently tied for the lead teams
	 * @return
	 */
	public List<Team> getLeaders();

	/**
	 * Returns the list of teams sorted by their current ranking
	 * Teams with the same score are at the same key
	 * @return
	 */
	public TreeMap<?,Collection<Team>> getRanks();

	@Deprecated
	public List<Team> getRankings();
}
