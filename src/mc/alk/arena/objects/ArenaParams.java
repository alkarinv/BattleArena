package mc.alk.arena.objects;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import mc.alk.arena.controllers.ParamController;
import mc.alk.arena.objects.arenas.ArenaType;
import mc.alk.arena.objects.options.TransitionOption;
import mc.alk.arena.util.MinMax;



public class ArenaParams{
	ArenaType arenaType;
	Rating rating = Rating.ANY;

	String name;
	String cmd;

	Integer timeBetweenRounds;
	Integer secondsTillMatch;
	Integer secondsToLoot;

	MatchTransitions allTops;
	String dbName;

	ArenaParams parent;
	ArenaSize size;

	public ArenaParams(ArenaType at) {
		this.arenaType = at;
	}

	public ArenaParams(ArenaParams ap) {
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
		this.setParent(ap.parent);
		if (ap.allTops != null)
			this.allTops = new MatchTransitions(ap.allTops);
		if (ap.size != null)
			this.size = new ArenaSize(ap.size);
	}

	public MatchTransitions getTransitionOptions(){
		return allTops == null ? ParamController.getTransitionOptions(this) : allTops;
	}

	public void setTransitionOptions(MatchTransitions transitionOptions) {
		this.allTops = new MatchTransitions(transitionOptions);
	}

	public String getTeamSizeRange() {
		return size != null ? ArenaSize.rangeString(size.minTeamSize,size.maxTeamSize) : "";
	}
	public String getNTeamRange() {
		return size != null ? ArenaSize.rangeString(size.minTeams,size.maxTeams) : "";
	}
	public ArenaType getType() {return arenaType;}

	public void setType(ArenaType type) {this.arenaType = type;}

	public boolean intersect(ArenaParams params) {
		if (!getType().matches(params.getType()))
			return false;

		if (getSize() != null && params.getSize() != null){
			if(this.size == null){
				size = new ArenaSize(getSize());}
			return size.intersect(params.getSize());
		}
		return true;
	}

	public boolean intersectMax(ArenaParams params) {
		if (!getType().matches(params.getType()))
			return false;
		if (getSize() != null && params.getSize() != null){
			if(this.size == null){
				size = new ArenaSize(getSize());}
			return size.intersectMax(params.getSize());
		}
		return true;
	}

	public boolean matches(final ArenaParams ap) {
		return (arenaType != null && ap.arenaType != null &&
				arenaType.matches(ap.arenaType) && (
						ArenaSize.matchesTeamSize(getSize(), ap.getSize()) &&
						ArenaSize.matchesNTeams(getSize(),ap.getSize())));
	}


	public Collection<String> getInvalidMatchReasons(ArenaParams ap) {
		List<String> reasons = new ArrayList<String>();
		if (arenaType == null) reasons.add("ArenaType is null");
		if (ap.arenaType == null) reasons.add("Passed params have an arenaType of null");
		else reasons.addAll(arenaType.getInvalidMatchReasons(ap.getType()));
		if (!ArenaSize.matchesNTeams(getSize(), ap.getSize()))
			reasons.add("Arena accepts nteams="+getNTeamRange()+". you requested "+ap.getNTeamRange());
		if (!ArenaSize.matchesTeamSize(getSize(), ap.getSize())) reasons.add("Arena accepts teamSize="+getTeamSizeRange()+
				". you requested "+ap.getTeamSizeRange());
		return reasons;
	}

	public boolean valid() {
		return (arenaType != null && (size == null || size.valid()));
	}

	public Collection<String> getInvalidReasons() {
		List<String> reasons = new ArrayList<String>();
		if (arenaType == null) reasons.add("ArenaType is null");
		if (size != null){ reasons.addAll(size.getInvalidReasons());}
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

	public Integer getSecondsToLoot() {
		return secondsToLoot != null ? secondsToLoot :
			(parent != null ? parent.getSecondsToLoot() : null);
	}

	public void setSecondsTillMatch(int i) {
		secondsTillMatch=i;
	}

	public Integer getSecondsTillMatch() {
		return secondsTillMatch != null ? secondsTillMatch :
			(parent != null ? parent.getSecondsTillMatch() : null);
	}

	public void setTimeBetweenRounds(int i) {
		timeBetweenRounds=i;
	}
	public Integer getTimeBetweenRounds() {
		return timeBetweenRounds != null ? timeBetweenRounds :
			(parent != null ? parent.getTimeBetweenRounds() : null);
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
		sb.append("&e"+arenaType.getName());
		return sb.toString();
	}

	@Override
	public String toString(){
		return  name+":"+cmd+":"+arenaType +" rating="+rating +",nteams="+getNTeamRange()+",teamSize="+getTeamSizeRange();
	}


	public boolean isDuelOnly() {
		return getTransitionOptions().hasOptionAt(MatchState.DEFAULTS, TransitionOption.DUELONLY);
	}

	public boolean getAlwaysOpen(){
		return getTransitionOptions().hasOptionAt(MatchState.DEFAULTS, TransitionOption.ALWAYSOPEN);
	}

	public void setParent(ArenaParams parent) {
		this.parent=parent;
	}
	public ArenaParams getParent() {
		return parent;
	}

	public Integer getMinTeamSize() {
		return size != null ? size.getMinTeamSize() :
			(parent != null ? parent.getMinTeamSize() : null);
	}

	public Integer getMaxTeamSize() {
		return size != null ? size.getMaxTeamSize() :
			(parent != null ? parent.getMaxTeamSize() : null);
	}

	public Integer getMinTeams() {
		return size != null ? size.getMinTeams() :
			(parent != null ? parent.getMinTeams() : null);
	}

	public Integer getMaxTeams() {
		return size != null ? size.getMaxTeams() :
			(parent != null ? parent.getMaxTeams() : null);
	}
	public ArenaSize getSize(){
		return size != null ? size :
			(parent != null ? parent.getSize() : null);
	}

	public Integer getMaxPlayers() {
		return size != null ? size.getMaxPlayers() :
			(parent != null ? parent.getMaxPlayers() : null);
	}
	public Integer getMinPlayers() {
		return size != null ? size.getMinPlayers() :
			(parent != null ? parent.getMinPlayers() : null);
	}
	public void setTeamSize(int n) {
		if (size == null){ size = new ArenaSize();}
		size.setMinTeams(n);
		size.setMaxTeams(n);
	}
	public void setNTeams(MinMax mm) {
		if (size == null){ size = new ArenaSize();}
		size.setNTeams(mm);
	}

	public void setTeamSizes(MinMax mm) {
		if (size == null){ size = new ArenaSize();}
		size.setTeamSizes(mm);
	}

	public void setMinTeamSize(int n) {
		if (size == null){ size = new ArenaSize();}
		size.setMinTeamSize(n);
	}
	public void setMaxTeamSize(int n) {
		if (size == null){ size = new ArenaSize();}
		size.setMaxTeamSize(n);
	}
	public void setMaxTeams(int n) {
		if (size == null){ size = new ArenaSize();}
		size.setMaxTeams(n);
	}
	public void setMinTeams(int n) {
		if (size == null){ size = new ArenaSize();}
		size.setMinTeams(n);
	}

	public boolean matchesTeamSize(int i) {
		return size != null ? size.matchesTeamSize(i) :
			(parent != null ? parent.matchesTeamSize(i) : false);
	}

}
