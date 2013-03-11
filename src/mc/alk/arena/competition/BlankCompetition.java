package mc.alk.arena.competition;

import java.util.Collection;

import mc.alk.arena.objects.ArenaPlayer;
import mc.alk.arena.objects.CompetitionState;
import mc.alk.arena.objects.MatchParams;
import mc.alk.arena.objects.teams.Team;

public class BlankCompetition extends Competition{
	MatchParams params;
	static int count = 0;
	int id = count++;

	public BlankCompetition(MatchParams mp) {this.params = mp;}

	@Override
	public Long getTime(CompetitionState state) {return null;}

	@Override
	public int getID() {return id;}

	@Override
	public CompetitionState getState() {return null;}

	@Override
	public String getName() {return id+"";}

	@Override
	protected void transitionTo(CompetitionState state) {}

	@Override
	public MatchParams getParams() {return this.params;}

	@Override
	public void addTeam(Team team) {this.teams.add(team);}

	@Override
	public boolean removeTeam(Team team) {return this.teams.remove(team);}

	@Override
	public void addedToTeam(Team team, Collection<ArenaPlayer> players) {/* do nothing */}

	@Override
	public void addedToTeam(Team team, ArenaPlayer player) {/* do nothing */}

	@Override
	public void removedFromTeam(Team team, Collection<ArenaPlayer> players) {/* do nothing */}

	@Override
	public void removedFromTeam(Team team, ArenaPlayer player) {/* do nothing */}

	@Override
	public boolean canLeave(ArenaPlayer p) {return true;}

	@Override
	public boolean leave(ArenaPlayer p) {return true;}

}
