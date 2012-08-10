package mc.alk.arena.objects.tournament;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import mc.alk.arena.objects.MatchParams;
import mc.alk.arena.objects.MatchResult;
import mc.alk.arena.objects.arenas.Arena;
import mc.alk.arena.objects.teams.Team;


public class Matchup {
	public MatchResult result = new MatchResult();
	public List<Team> teams = new ArrayList<Team>();
	Arena arena = null;

	public Arena getArena() {return arena;}
	public void setArena(Arena arena) {this.arena = arena;}
	
	MatchParams q = null;
	public Matchup(MatchParams q, Team team, Team team2) {
		this.q = q;
		teams.add(team);
		teams.add(team2);
	}
//
	public Matchup(MatchParams sq, Collection<Team> teams) {
		this.teams = new ArrayList<Team>(teams);
		this.q = new MatchParams(sq);
	}

//	public void warnBeReady(SpecificQ q) {
//		// TODO Auto-generated method stub
//		
//	}

//	public Matchup(SpecificQ sq, ArrayList<TournamentTeam> inEvent) {
//		this.teams.addAll(inEvent);
//		this.q = new SpecificQ(sq);
//	}

	public MatchParams getSpecificQ() {
		return q;
	}

	public void setResult(MatchResult result) {
		this.result = result;
	}

	public String toString(){
		StringBuilder sb = new StringBuilder();
		for (Team t: teams){
			sb.append("t=" + t +",");
		}
		return sb.toString() + " result=" + result;
	}
	public List<Team> getTeams() {return teams;}
	public Team getTeam(int i) {
		return teams.get(i);
	}
	public MatchResult getResult() {
		return result;
	}
}
