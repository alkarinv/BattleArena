package mc.alk.arena.competition;

import java.util.ArrayList;
import java.util.List;

import mc.alk.arena.controllers.TransitionMethodController;
import mc.alk.arena.objects.CompetitionState;
import mc.alk.arena.objects.teams.Team;

public abstract class Competition {

	protected final List<Team> teams = new ArrayList<Team>(); /// Our players

	/// Our Transition Controller
	protected final TransitionMethodController tmc = new TransitionMethodController();

	/**
	 * Get the time of when the competition did the given state
	 * @return time or null if not found
	 */
	public abstract Long getTime(CompetitionState state);

	public abstract CompetitionState getState();

	protected abstract void transitionTo(CompetitionState state);

	/**
	 * Get the unique ID for this competition
	 * @return
	 */
	public abstract int getID();
}
