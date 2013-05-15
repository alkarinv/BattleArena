package mc.alk.arena.objects.tournament;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import mc.alk.arena.competition.match.Match;
import mc.alk.arena.listeners.custom.MatchCreationCallback;
import mc.alk.arena.objects.ArenaPlayer;
import mc.alk.arena.objects.MatchParams;
import mc.alk.arena.objects.MatchResult;
import mc.alk.arena.objects.arenas.Arena;
import mc.alk.arena.objects.arenas.ArenaListener;
import mc.alk.arena.objects.teams.ArenaTeam;


public class Matchup {
	static int count = 0;
	final int id = count++; /// id

	public MatchResult result = new MatchResult();
	public List<ArenaTeam> teams = new ArrayList<ArenaTeam>();
	Arena arena = null;

	public Arena getArena() {return arena;}
	public void setArena(Arena arena) {this.arena = arena;}
	List<ArenaListener> listeners = new ArrayList<ArenaListener>();

	MatchParams params = null;
	MatchCreationCallback creationListener;
	Match match = null;

	public Matchup(MatchParams params, ArenaTeam team, ArenaTeam team2) {
		this.params = params;
		teams.add(team);
		teams.add(team2);
	}

	public Matchup(MatchParams params, Collection<ArenaTeam> teams) {
		this.teams = new ArrayList<ArenaTeam>(teams);
		this.params = new MatchParams(params);
	}

	public MatchParams getMatchParams() {
		return params;
	}

	public void setResult(MatchResult result) {
		this.result = result;
	}

	@Override
	public String toString(){
		StringBuilder sb = new StringBuilder();
		for (ArenaTeam t: teams){
			sb.append("t=" + t +",");
		}
		return sb.toString() + " result=" + result;
	}
	public List<ArenaTeam> getTeams() {return teams;}
	public ArenaTeam getTeam(int i) {
		return teams.get(i);
	}
	public MatchResult getResult() {
		return result;
	}

	@Override
	public boolean equals(Object other) {
		if (this == other) return true;
		if (!(other instanceof Matchup)) return false;
		return this.hashCode() == ((Matchup) other).hashCode();
	}

	@Override
	public int hashCode() { return id;}

	public void addArenaListener(ArenaListener transitionListener) {
		listeners.add(transitionListener);
	}

	public List<ArenaListener> getArenaListeners() {
		return listeners;
	}

	public void addMatch(Match match) {
		this.match = match;
	}

	public Match getMatch(){
		return match;
	}
	public Integer getPriority() {
		Integer priority = Integer.MAX_VALUE;
		for (ArenaTeam t: teams){
			if (t.getPriority() < priority){
				priority = t.getPriority();}
		}
		return priority;
	}
	public boolean hasMember(ArenaPlayer p) {
		for (ArenaTeam t: teams){
			if (t.hasMember(p))
				return true;
		}
		return false;
	}
	public ArenaTeam getTeam(ArenaPlayer p) {
		for (ArenaTeam t: teams){
			if (t.hasMember(p))
				return t;
		}
		return null;
	}
	public int size() {
		int size = 0;
		for (ArenaTeam t: teams){
			size += t.size();
		}
		return size;
	}
	public void addMatchCreationListener(MatchCreationCallback listener) {
		creationListener = listener;
	}

	public MatchCreationCallback getMatchCreationListener() {
		return creationListener;
	}
}
