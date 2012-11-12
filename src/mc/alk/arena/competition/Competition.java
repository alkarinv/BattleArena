package mc.alk.arena.competition;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import mc.alk.arena.controllers.TransitionMethodController;
import mc.alk.arena.objects.ArenaPlayer;
import mc.alk.arena.objects.CompetitionState;
import mc.alk.arena.objects.MatchParams;
import mc.alk.arena.objects.teams.Team;

/**
 * Base class for Matches and Events
 * @author alkarin
 *
 */
public abstract class Competition {

	/** Our teams */
	protected final List<Team> teams = new ArrayList<Team>();

	/** Our Transition Controller that will handle transition from one state to the next*/
	protected final TransitionMethodController tmc = new TransitionMethodController();

	/** Players that have left the match */
	protected final Set<String> leftPlayers = Collections.synchronizedSet(new HashSet<String>());

	/**
	 * Get the time of when the competition did the given state
	 * @return time or null if not found
	 */
	public abstract Long getTime(CompetitionState state);

	/**
	 * Get the unique ID for this competition
	 * @return
	 */
	public abstract int getID();

	/**
	 * Returns the current state of the competition
	 * @return
	 */
	public abstract CompetitionState getState();

	/**
	 * Transition from one state to another
	 * onStart -> onVictory
	 * @param state
	 */
	protected abstract void transitionTo(CompetitionState state);

	/**
	 * Signify that a player has left the competition
	 * @param player
	 * @return
	 */
	public boolean playerLeft(ArenaPlayer player) {
		return leftPlayers.contains(player.getName());
	}

	/**
	 * Returns either the MatchParams or EventParams of the match/event
	 * @return
	 */
	public abstract MatchParams getParams();

	/**
	 * add a team to this competition
	 * @param team
	 */
	public abstract void addTeam(Team team);

	/**
	 * Signify that the set of players were added to the team
	 * @param t
	 * @param players
	 */
	public abstract void addedToTeam(Team team, Collection<ArenaPlayer> players);

	/**
	 * Signify that the set of players were added to the team
	 * @param t
	 * @param players
	 */
	public abstract void addedToTeam(Team team, ArenaPlayer player);

	/**
	 * Set our teams
	 * @param teams
	 */
	public void setTeams(List<Team> teams){
		this.teams.clear();
		this.teams.addAll(teams);
	}

	/**
	 * return the teams for this competition
	 * @return
	 */
	public List<Team> getTeams() {
		return teams;
	}

}
