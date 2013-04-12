package mc.alk.arena.objects.queues;

import java.util.Collection;
import java.util.List;

import mc.alk.arena.objects.ArenaPlayer;
import mc.alk.arena.objects.teams.ArenaTeam;
import mc.alk.arena.objects.tournament.Matchup;

public class MatchTeamQObject extends QueueObject{
	final Matchup matchup;

	public MatchTeamQObject(Matchup matchup){
		matchParams = matchup.getMatchParams();
		this.matchup = matchup;
		this.priority = matchup.getPriority();
	}

	@Override
	public Integer getPriority() {
		return priority;
	}

	@Override
	public boolean hasMember(ArenaPlayer p) {
		return matchup.hasMember(p);
	}

	@Override
	public ArenaTeam getTeam(ArenaPlayer p) {
		return matchup.getTeam(p);
	}

	@Override
	public int size() {
		return matchup.size();
	}

	@Override
	public String toString(){
		return priority+" " + matchup.toString();
	}

	@Override
	public Collection<ArenaTeam> getTeams() {
		return matchup.getTeams();
	}

	public Matchup getMatchup() {
		return matchup;
	}

	@Override
	public boolean hasTeam(ArenaTeam team) {
		List<ArenaTeam> teams = matchup.getTeams();
		return teams != null ? teams.contains(team) : false;
	}

}
