package mc.alk.arena.objects;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import mc.alk.arena.Defaults;
import mc.alk.arena.controllers.ParamController;
import mc.alk.arena.objects.arenas.ArenaType;
import mc.alk.arena.objects.options.TransitionOption;



public class ArenaParams extends ArenaSize{
	ArenaType arenaType;
	Rating rating = Rating.ANY;

	String name;
	String cmd;

	int timeBetweenRounds = Defaults.TIME_BETWEEN_ROUNDS;
	int secondsTillMatch = Defaults.SECONDS_TILL_MATCH;
	int secondsToLoot = Defaults.SECONDS_TO_LOOT;

	MatchTransitions allTops;
	String dbName;

	public ArenaParams(ArenaType at) {
		super();
		this.arenaType = at;
	}

	public ArenaParams(ArenaParams ap) {
		super(ap);
		if (this == ap)
			return;
		this.arenaType = ap.arenaType;
		this.rating = ap.rating;
		this.cmd = ap.cmd;
		this.name = ap.name;
		this.timeBetweenRounds = ap.timeBetweenRounds;
		this.secondsTillMatch = ap.secondsTillMatch;
		this.secondsToLoot = ap.secondsToLoot;
		this.dbName = ap.dbName;
		if (ap.allTops != null)
			this.allTops = new MatchTransitions(ap.allTops);
	}

	public MatchTransitions getTransitionOptions(){
		return allTops == null ? ParamController.getTransitionOptions(this) : allTops;
	}

	public void setTransitionOptions(MatchTransitions transitionOptions) {
		this.allTops = new MatchTransitions(transitionOptions);
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
		return ( !(arenaType == null && ap.arenaType == null) && arenaType.matches(ap.arenaType) &&
				matchesTeamSize(ap) &&
				matchesNTeams(ap));
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
		if (minTeams > maxTeams) reasons.add("Min Teams is greater than Max Teams" + minTeams+":"+ maxTeams);
		return reasons;
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

	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}

	public String toPrettyString() {
		StringBuilder sb = new StringBuilder();
		sb.append("&e"+arenaType.toPrettyString(minTeamSize, maxTeamSize));
		return sb.toString();
	}

	@Override
	public String toString(){
		return  name+":"+cmd+":"+arenaType +" rating="+rating +",nteams="+getNTeamRange()+",teamSize="+getTeamSizeRange();
	}

	public boolean intersect(ArenaParams params) {
		if (!getType().matches(params.getType()))
			return false;
		return super.intersect(params);
	}

	public boolean isDuelOnly() {
		return getTransitionOptions().hasOptionAt(MatchState.DEFAULTS, TransitionOption.DUELONLY);
	}

	public boolean getAlwaysOpen(){
		return getTransitionOptions().hasOptionAt(MatchState.DEFAULTS, TransitionOption.ALWAYSOPEN);
	}
}
