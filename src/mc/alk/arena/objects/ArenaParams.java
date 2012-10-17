package mc.alk.arena.objects;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import mc.alk.arena.Defaults;
import mc.alk.arena.objects.arenas.ArenaType;
import mc.alk.arena.util.Util;
import mc.alk.arena.util.Util.MinMax;



public class ArenaParams {
	public static final int ANY = Integer.MAX_VALUE-1;
	public static final int CLANS = Integer.MAX_VALUE;
	public static final int MAX = Integer.MAX_VALUE-2;
	public static final int NONE = -1;

	Integer minTeamSize = 1;
	Integer maxTeamSize = MAX;

	Integer preferredMinTeamSize = minTeamSize;
	Integer preferredMaxTeamSize = maxTeamSize;

	Integer minTeams = ANY;
	Integer maxTeams = ANY;

	ArenaType arenaType = ArenaType.ANY;
	Rating rating = Rating.ANY;

	String name;
	String cmd;

	int maxNumberPlayers = MAX;

	int timeBetweenRounds = Defaults.TIME_BETWEEN_ROUNDS;
	int secondsTillMatch = Defaults.SECONDS_TILL_MATCH;
	int secondsToLoot = Defaults.SECONDS_TO_LOOT;

	MatchTransitions allTops;
	String dbName;

	public ArenaParams(ArenaType at,Rating rating) {
		this.arenaType = at;
		this.rating = rating;
		calcMaxPlayers();
	}

	public ArenaParams(ArenaParams ap) {
		if (this == ap)
			return;
		this.arenaType = ap.arenaType;
		this.rating = ap.rating;
		this.minTeamSize = ap.minTeamSize;
		this.maxTeamSize = ap.maxTeamSize;
		this.preferredMaxTeamSize = ap.preferredMaxTeamSize;
		this.preferredMinTeamSize = ap.preferredMinTeamSize;
		this.minTeams = ap.minTeams;
		this.maxTeams = ap.maxTeams;
		this.cmd = ap.cmd;
		this.timeBetweenRounds = ap.timeBetweenRounds;
		this.secondsTillMatch = ap.secondsTillMatch;
		this.secondsToLoot = ap.secondsToLoot;
		allTops = new MatchTransitions(ap.allTops);
		this.dbName = ap.dbName;
		calcMaxPlayers();
	}

	public void setAllTransitionOptions(MatchTransitions allTops) {
		this.allTops = allTops;
	}
	public MatchTransitions getTransitionOptions(){
		return allTops;
	}
	public ArenaParams(String teamSize, ArenaType arenaType) {
		final MinMax q = Util.getMinMax(teamSize);
		this.minTeamSize = q.min;
		this.maxTeamSize = q.max;
		this.arenaType = arenaType;
		calcMaxPlayers();
	}
	public ArenaParams(Integer minTeamSize, Integer maxTeamSize, ArenaType at){
		this.minTeamSize = minTeamSize;
		this.maxTeamSize = maxTeamSize;
		this.arenaType = at;
		calcMaxPlayers();
	}

	private void calcMaxPlayers() {
		if (maxTeams == ArenaParams.MAX || maxTeamSize == ArenaParams.MAX){
			maxNumberPlayers = ArenaParams.MAX;
		} else {
			maxNumberPlayers = maxTeams * maxTeamSize;
		}
	}

	public static String rangeString(final int min,final int max){
		if (max == MAX){ return min+"+";} /// Example: 2+
		if (min == max){ return min+"";} /// Example: 2	
		return min + "-" + max; //Example 2-4
	}

	public String getTeamSizeRange() {return rangeString(minTeamSize,maxTeamSize);}
	public String getNTeamRange() {return rangeString(minTeams,maxTeams);}
	public ArenaType getType() {return arenaType;}

	public void setType(ArenaType type) {this.arenaType = type;}

	public boolean matches(final ArenaParams ap) {
		return ( ((arenaType == null || ap.arenaType == null) || arenaType.matches(ap.arenaType)) && 
				matchesTeamSize(ap) && 
				matchesNTeams(ap));
	}


	public boolean matchesNTeams(final ArenaParams ap) {
		return (this.minTeams <= ap.getMinTeams() && maxTeams >= ap.getMaxTeams()) 
				|| maxTeams == MAX  || ap.getMaxTeams() == MAX;
	}

	public boolean matchesNTeams(int nteams) {
		return ( (minTeams <= nteams && maxTeams>=nteams) || minTeams==ANY || nteams==ANY);
	}

	public Collection<String> getInvalidMatchReasons(ArenaParams ap) {
		List<String> reasons = new ArrayList<String>();
		if (arenaType == null) reasons.add("ArenaType is null");
		if (ap.arenaType == null) reasons.add("Passed params have an arenaType of null");
		reasons.addAll(arenaType.getInvalidMatchReasons(ap.getType()));
		if (!matchesNTeams(ap)) reasons.add("Arena accepts nteams="+getNTeamRange()+
				". you requested "+ap.getNTeamRange());
		if (!matchesTeamSize(ap)) reasons.add("Arena accepts teamSize="+getTeamSizeRange()+
				". you requested "+ap.getTeamSizeRange());		
		return reasons;
	}

	public boolean valid() {
		return (arenaType != null && minTeamSize > 0 && maxTeamSize > 0 && minTeamSize <= maxTeamSize);
	}

	public Collection<String> getInvalidReasons() {
		List<String> reasons = new ArrayList<String>();
		if (arenaType == null) reasons.add("ArenaType is null");
		if (minTeamSize <= 0) reasons.add("Min Team Size is <= 0");
		if (maxTeamSize <= 0) reasons.add("Max Team Size is <= 0");
		if (minTeamSize > maxTeamSize) reasons.add("Min Team Size is greater than Max Team Size " + minTeamSize+":"+ maxTeamSize);
		return reasons;
	}

	public boolean matchesTeamSize(final ArenaParams q) {
		return (this.minTeamSize <= q.minTeamSize ) && (this.maxTeamSize == MAX  || this.maxTeamSize >= q.maxTeamSize);
	}

	public boolean matchesTeamSize(int i) {
		return (minTeamSize==ANY || i>= minTeamSize && i<= maxTeamSize);
	}
	public void setTeamSize(int size) {
		preferredMinTeamSize = preferredMaxTeamSize = minTeamSize = maxTeamSize = size;
		calcMaxPlayers();
	}

	public void setTeamSizes(MinMax mm) {
		preferredMinTeamSize = minTeamSize = mm.min;
		preferredMaxTeamSize = maxTeamSize = mm.max;
		calcMaxPlayers();
	}
	public void setNTeams(MinMax mm) {
		minTeams = mm.min;
		maxTeams = mm.max;
		calcMaxPlayers();
	}

	public int getMaxPlayers(){return maxNumberPlayers;}
	public int getMinTeams() {return minTeams;}
	public int getMaxTeams() {return maxTeams;}
	public void setMinTeams(Integer nteams) {this.minTeams = nteams;}
	public void setMaxTeams(Integer nteams) {
		this.maxTeams = nteams;
		calcMaxPlayers();
	}

	public void setMinTeamSize(int size) {minTeamSize=size;}
	public void setMaxTeamSize(int size) {
		maxTeamSize=size;
		calcMaxPlayers();
	}
	public int getMinTeamSize() {return minTeamSize;}
	public int getMaxTeamSize() {return maxTeamSize;}

	public Integer getPreferredMinTeamSize() {
		return preferredMinTeamSize;
	}
	public void setPreferredMinTeamSize(Integer preferredMinTeamSize) {
		this.preferredMinTeamSize = preferredMinTeamSize;
	}
	public Integer getPreferredMaxTeamSize() {
		return preferredMaxTeamSize;
	}
	public void setPreferredMaxTeamSize(Integer preferredMaxTeamSize) {
		this.preferredMaxTeamSize = preferredMaxTeamSize;
		calcMaxPlayers();

	}

	public String getCommand() {
		return cmd;
	}
	public boolean isRated(){
		return rating == Rating.RATED;
	}
	public void setRated(boolean rated) {
		this.rating = rated ? Rating.RATED : Rating.UNRATED;
	}
	public void setRating(Rating rating) {
		this.rating = rating;
	}

	public void setSecondsToLoot(int i) {
		secondsToLoot=i;
	}
	public int getSecondsToLoot() {
		return secondsToLoot;
	}

	public void setSecondsTillMatch(int i) {
		secondsTillMatch=i;
	}
	public int getSecondsTillMatch() {
		return secondsTillMatch;
	}

	public void setTimeBetweenRounds(int i) {
		timeBetweenRounds=i;
	}
	public int getTimeBetweenRounds() {
		return timeBetweenRounds;
	}

	public void setDBName(String dbName) {
		this.dbName = dbName;
	}
	public String getDBName(){
		return dbName;
	}

	public String toPrettyString() {
		StringBuilder sb = new StringBuilder();
		sb.append("&e"+arenaType.toPrettyString(minTeamSize, maxTeamSize));
		return sb.toString();
	}

	public String toString(){
		return  name+":"+cmd+":"+arenaType +" rating="+rating +",nteams="+getNTeamRange()+",teamSize="+getTeamSizeRange();
	}


}
