package mc.alk.arena.objects.queues;

import java.util.ArrayList;
import java.util.Collection;

import mc.alk.arena.objects.ArenaPlayer;
import mc.alk.arena.objects.MatchParams;
import mc.alk.arena.objects.options.JoinOptions;
import mc.alk.arena.objects.teams.Team;

public class TeamQObject extends QueueObject{
	final Team team;

	public TeamQObject(Team team, MatchParams params, JoinOptions joinOptions) {
		this.matchParams = params;
		this.team = team;
		priority = team.getPriority();
		this.jp = joinOptions;
	}

	public Team getTeam() {
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
	public Team getTeam(ArenaPlayer p) {
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
	public Collection<Team> getTeams() {
		ArrayList<Team> teams = new ArrayList<Team>(1);
		teams.add(team);
		return teams;
	}

	@Override
	public boolean hasTeam(Team team) {
		return this.team.getId() == team.getId();
	}
}
