package mc.alk.arena.competition.events.util;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import mc.alk.arena.competition.events.Event;
import mc.alk.arena.controllers.TeamController;
import mc.alk.arena.objects.ArenaPlayer;
import mc.alk.arena.objects.MatchParams;
import mc.alk.arena.objects.teams.CompositeTeam;
import mc.alk.arena.objects.teams.Team;
import mc.alk.arena.objects.teams.TeamHandler;

public abstract class TeamJoinHandler implements TeamHandler {

	public static final TeamJoinResult CANTFIT = new TeamJoinResult(TeamJoinStatus.CANT_FIT);
	public static final TeamJoinResult NOTOPEN = new TeamJoinResult(TeamJoinStatus.NOT_OPEN);

	public static enum TeamJoinStatus{
		ADDED, CANT_FIT, ADDED_TO_EXISTING, WAITING_FOR_PLAYERS, NOT_OPEN
	}

	public static class TeamJoinResult{
		public TeamJoinResult(TeamJoinStatus a, int n, Team t){this.a = a; this.n = n; this.team = t;}
		public TeamJoinResult(TeamJoinStatus a){this.a = a;}
		public TeamJoinStatus a;
		public int n;
		public Team team;
		public TeamJoinStatus getEventType(){ return a;}
		public int getRemaining(){return n;}	
	}

	final List<Team> teams;
	ArrayList<CompositeTeam> pickupTeams = new ArrayList<CompositeTeam>();
	final MatchParams mp;
	final Event event;
	final int minTeamSize,maxTeamSize;
	final int minTeams,maxTeams;

	public TeamJoinHandler(Event event){
		this.event = event;
		this.mp = event.getParams();
		this.minTeamSize = mp.getMinTeamSize(); this.maxTeamSize = mp.getMaxTeamSize();
		this.minTeams = mp.getMinTeams(); this.maxTeams = mp.getMaxTeams();
		this.teams = event.getTeams();
	}

	public void deconstruct() {
		for (Team t: pickupTeams){
			TeamController.removeTeamHandler(t, this);
		}
		pickupTeams.clear();
	}

	public abstract TeamJoinResult joiningTeam(Team team);

	public boolean canLeave(ArenaPlayer p) {
		return true;
	}
	public boolean leave(ArenaPlayer p) {
		for (Team t: pickupTeams){
			if (t.hasMember(p)){
				pickupTeams.remove(t);
				return true;
			}
		}
		return true;
	}

	public Set<ArenaPlayer> getExcludedPlayers() {
		Set<ArenaPlayer> tplayers = new HashSet<ArenaPlayer>();
		for (Team t: pickupTeams){
			tplayers.addAll(t.getPlayers());}
		return tplayers;
	}



}
