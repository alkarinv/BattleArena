package mc.alk.arena.competition;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import mc.alk.arena.controllers.MethodController;
import mc.alk.arena.events.BAEvent;
import mc.alk.arena.listeners.ArenaListener;
import mc.alk.arena.objects.ArenaPlayer;
import mc.alk.arena.objects.CompetitionState;
import mc.alk.arena.objects.MatchParams;
import mc.alk.arena.objects.teams.Team;

/**
 * Base class for Matches and Events
 * @author alkarin
 *
 */
public abstract class Competition implements ArenaListener {

	/** Our teams */
	protected List<Team> teams = Collections.synchronizedList(new ArrayList<Team>());

	/** Players that have left the match */
	protected final Set<String> leftPlayers = Collections.synchronizedSet(new HashSet<String>());

	/** Our Method Controller that will handle anyone listening to this competition*/
	protected final MethodController methodController = new MethodController();

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
	 * Remove the team from the competition
	 * @param team
	 * @return whether or not the team was removed
	 */
	public abstract boolean removeTeam(Team team);

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
	 * Signify that the set of players were removed from the team
	 * @param t
	 * @param players
	 */
	public abstract void removedFromTeam(Team team, Collection<ArenaPlayer> players);

	/**
	 * Signify that the set of players were added to the team
	 * @param t
	 * @param players
	 */
	public abstract void removedFromTeam(Team team, ArenaPlayer player);

	/**
	 * Set our teams
	 * @param teams
	 */
	public void setTeams(List<Team> teams){
		this.teams = teams;
	}

	/**
	 * return the teams for this competition
	 * @return
	 */
	public List<Team> getTeams() {
		return teams;
	}

	/**
	 * Notify Bukkit Listeners and specific listeners to this match
	 * @param BAevent event
	 */
	public void callEvent(BAEvent event) {
		methodController.callListeners(event); /// Call our listeners listening to only this competition
		event.callEvent(); /// Call anyone using generic bukkit listeners
	}


	/**
	 * Add a collection of listeners for this competition
	 * @param transitionListeners
	 */
	public void addArenaListeners(Collection<ArenaListener> transitionListeners){
		for (ArenaListener tl: transitionListeners){
			addArenaListener(tl);}
	}

	/**
	 * Add an arena listener for this competition
	 * @param al
	 */
	public void addArenaListener(ArenaListener al){
		methodController.addListener(al);
	}

	/**
	 * Remove an arena listener for this competition
	 * @param al
	 */
	public boolean removeArenaListener(ArenaListener al){
		return methodController.removeListener(al);
	}

}
