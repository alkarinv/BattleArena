package mc.alk.arena.objects.joining;

import mc.alk.arena.objects.ArenaPlayer;
import mc.alk.arena.objects.MatchParams;
import mc.alk.arena.objects.options.JoinOptions;
import mc.alk.arena.objects.teams.ArenaTeam;

import java.util.ArrayList;
import java.util.List;

public class TeamJoinObject extends QueueObject{
	final ArenaTeam team;

	public TeamJoinObject(ArenaTeam team, MatchParams params, JoinOptions joinOptions) {
		super(joinOptions, params);
		this.team = team;
		priority = team.getPriority();
		numPlayers += team.size();
	}

	public ArenaTeam getTeam() {
		return team;
	}

	@Override
	public Integer getPriority() {
		return priority;
	}
	@Override
	public boolean hasMember(ArenaPlayer p) {
		return team.hasMember(p);
	}
	@Override
	public ArenaTeam getTeam(ArenaPlayer p) {
		return team.hasMember(p) ? team : null;
	}
	@Override
	public int size() {
		return team.size();
	}

	@Override
	public String toString(){
		return team.getPriority()+" " + team.toString()+":" + team.getId();
	}

	@Override
	public List<ArenaTeam> getTeams() {
		ArrayList<ArenaTeam> teams = new ArrayList<ArenaTeam>(1);
		teams.add(team);
		return teams;
	}

	@Override
	public boolean hasTeam(ArenaTeam team) {
		if (this.team.getId() == team.getId())
			return true;
		for (ArenaPlayer ap : this.team.getPlayers()){
			if (team.hasMember(ap)){
				return true;
			}
		}
		return false;
	}

	public boolean hasStartPerms() {
		return false;
	}
}
