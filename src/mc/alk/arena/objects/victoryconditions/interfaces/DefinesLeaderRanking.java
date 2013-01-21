package mc.alk.arena.objects.victoryconditions.interfaces;

import java.util.List;

import mc.alk.arena.objects.teams.Team;

public interface DefinesLeaderRanking {

	/**
	 * Returns the list of currently tied for the lead teams
	 * @return
	 */
	public List<Team> getLeaders();

	/**
	 * Returns the list of teams sorted by their current ranking
	 * @return
	 */
	public List<Team> getRankings();

}
