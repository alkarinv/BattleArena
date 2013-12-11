package mc.alk.arena.objects;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import mc.alk.arena.BattleArena;
import mc.alk.arena.controllers.ParamController;
import mc.alk.arena.objects.arenas.ArenaType;
import mc.alk.arena.objects.options.TransitionOption;
import mc.alk.arena.util.MinMax;

import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;



public class ArenaParams {
	ArenaType arenaType = null;
	Rating rating = Rating.ANY;

	String name = null;
	String cmd = null;

	Integer timeBetweenRounds = null;
	Integer secondsTillMatch = null;
	Integer secondsToLoot = null;

	MatchTransitions allTops = null;
	String dbName = null;

	ArenaParams parent = null;
	ArenaSize size = null;
	Boolean closeWaitroomWhileRunning = null;
	Boolean cancelIfNotEnoughPlayers = null;
	Integer arenaCooldown = null;
	Integer allowedTeamSizeDifference = null;

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
		this.closeWaitroomWhileRunning = ap.closeWaitroomWhileRunning;
		this.cancelIfNotEnoughPlayers= ap.cancelIfNotEnoughPlayers;
		this.arenaCooldown = ap.arenaCooldown;
		this.allowedTeamSizeDifference = ap.allowedTeamSizeDifference;
		this.setParent(ap.parent);
		if (ap.allTops != null)
			this.allTops = new MatchTransitions(ap.allTops);
		if (ap.size != null)
			this.size = new ArenaSize(ap.size);
	}

	public void flatten() {
		if (parent == null){
			return;}
		parent = ParamController.copy(parent);
		parent.flatten();
		if (this.arenaType == null) this.arenaType = parent.arenaType;
		if (this.rating == Rating.ANY) this.rating = parent.rating;
		if (this.cmd == null) this.cmd = parent.cmd;
		if (this.name == null) this.name = parent.name;
		if (this.timeBetweenRounds == null) this.timeBetweenRounds = parent.timeBetweenRounds;
		if (this.secondsTillMatch == null) this.secondsTillMatch = parent.secondsTillMatch;
		if (this.secondsToLoot == null) this.secondsToLoot = parent.secondsToLoot;
		if (this.dbName == null) this.dbName = parent.dbName;
		if (this.closeWaitroomWhileRunning == null) this.closeWaitroomWhileRunning = parent.closeWaitroomWhileRunning;
		if (this.cancelIfNotEnoughPlayers == null) this.cancelIfNotEnoughPlayers = parent.cancelIfNotEnoughPlayers;
		if (this.arenaCooldown== null) this.arenaCooldown = parent.arenaCooldown;
		if (this.allowedTeamSizeDifference== null) this.allowedTeamSizeDifference= parent.allowedTeamSizeDifference;
		this.allTops = MatchTransitions.mergeChildWithParent(this.allTops, parent.allTops);
		if (this.size == null) this.size = parent.size;
		this.parent = null;
	}

	public MatchTransitions getTransitionOptions(){
		return allTops;
	}

	public void setTransitionOptions(MatchTransitions transitionOptions) {
		this.allTops = new MatchTransitions(transitionOptions);
	}

	public String getTeamSizeRange() {
		return size != null ? ArenaSize.rangeString(size.minTeamSize,size.maxTeamSize) :
			(parent != null ? parent.getTeamSizeRange() : "");
	}

	public String getNTeamRange() {
		return size != null ? ArenaSize.rangeString(size.minTeams,size.maxTeams) :
			(parent != null ? parent.getNTeamRange() : "");
	}

	public String getPlayerRange() {
		return size != null ? ArenaSize.rangeString(size.getMinPlayers(),size.getMaxPlayers()) :
			(parent != null ? parent.getPlayerRange() : "");
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
		return cmd != null ? cmd :
			(parent != null ? parent.getCommand() : null);

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

	public void setSecondsToLoot(Integer i) {
		secondsToLoot=i;
	}

	public Integer getSecondsToLoot() {
		return secondsToLoot != null ? secondsToLoot :
			(parent != null ? parent.getSecondsToLoot() : null);
	}

	public void setSecondsTillMatch(Integer i) {
		secondsTillMatch=i;
	}

	public Integer getSecondsTillMatch() {
		return secondsTillMatch != null ? secondsTillMatch :
			(parent != null ? parent.getSecondsTillMatch() : null);
	}

	public void setTimeBetweenRounds(Integer i) {
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
		return dbName != null ? dbName :
			(parent != null ? parent.getDBName() : null);
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
		return  name+":"+cmd+":"+arenaType +" rating="+rating +",nteams="+getNTeamRange()+",teamSize="+getTeamSizeRange();
	}

	@Override
	public String toString(){
		return  name+":"+cmd+":"+arenaType +" rating="+rating +",nteams="+
				getNTeamRange()+",teamSize="+getTeamSizeRange() +" options=\n"+
				(getTransitionOptions()==null ? "" : getTransitionOptions().getOptionString());
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

	public void setTeamSize(Integer n) {
		if (n == null){
			size = null;
			return;
		}
		if (size == null){ size = new ArenaSize();}
		size.setMinTeams(n);
		size.setMaxTeams(n);
	}

	public void setNTeams(MinMax mm) {
		if (mm == null){
			size = null;
		} else {
			if (size == null){ size = new ArenaSize();}
			size.setNTeams(mm);
		}
	}

	public MinMax getNTeams(){
		return size != null ? new MinMax(size.getMinTeams(),size.getMaxTeams()) :
			(parent != null ? parent.getNTeams() : null);
	}

	public MinMax getTeamSizes(){
		return size != null ? new MinMax(size.getMinTeamSize(),size.getMaxTeamSize()) :
			(parent != null ? parent.getNTeams() : null);
	}

	public void setTeamSizes(MinMax mm) {
		if (mm == null){
			size = null;
		} else {
			if (size == null){ size = new ArenaSize();}
			size.setTeamSizes(mm);
		}
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

	public boolean hasOptionAt(MatchState state, TransitionOption op) {
		return ( ( getTransitionOptions() != null && getTransitionOptions().hasOptionAt(state, op) ||
				( parent != null && parent.hasOptionAt(state, op)) ));
	}

	public boolean hasEntranceFee() {
		return hasOptionAt(MatchState.PREREQS,TransitionOption.MONEY);
	}

	public Double getEntranceFee(){
		return getDoubleOption(MatchState.PREREQS, TransitionOption.MONEY);
	}

	public Double getDoubleOption(MatchState state, TransitionOption option){
		MatchTransitions tops = getTransitionOptions();
		if (tops != null){
			Double value = tops.getDoubleOption(state,option);
			if (value != null) {
				return value;
			} else if (parent != null){
				return parent.getDoubleOption(state,option);
			}
		} else if (parent != null){
			return parent.getDoubleOption(state, option);
		}
		return null;
	}
	public boolean hasAnyOption(TransitionOption option) {
		MatchTransitions tops = getTransitionOptions();
		return (tops != null && tops.hasAnyOption(option)) || (parent != null && parent.hasAnyOption(option));
	}

	public List<ItemStack> getWinnerItems() {
		return getGiveItems(MatchState.WINNERS);
	}

	public List<ItemStack> getLoserItems() {
		return getGiveItems(MatchState.LOSERS);
	}

	public List<ItemStack> getGiveItems(MatchState state) {
		MatchTransitions tops = getTransitionOptions();
		return (tops!=null && tops.hasOptionAt(state, TransitionOption.GIVEITEMS)) ?
				tops.getOptions(state).getGiveItems() : (parent != null ? parent.getGiveItems(state) : null);
	}

	public List<PotionEffect> getEffects(MatchState state) {
		MatchTransitions tops = getTransitionOptions();
		return (tops!=null && tops.hasOptionAt(state, TransitionOption.ENCHANTS)) ?
				tops.getOptions(state).getEffects() : (parent != null ? parent.getEffects(state) : null);
	}

	public boolean needsWaitroom() {
		return ( (allTops != null &&
				getTransitionOptions().hasAnyOption(
						TransitionOption.TELEPORTMAINWAITROOM,TransitionOption.TELEPORTWAITROOM)) ||
				(parent != null && parent.needsWaitroom())
				);
	}

	public boolean needsSpectate() {
		return ( (allTops != null &&
				getTransitionOptions().hasAnyOption(TransitionOption.TELEPORTSPECTATE)) ||
				(parent != null && parent.needsSpectate())
				);
	}

	public boolean needsLobby() {
		return ( (allTops != null &&
				getTransitionOptions().hasAnyOption(
						TransitionOption.TELEPORTMAINLOBBY,TransitionOption.TELEPORTLOBBY)) ||
				(parent != null && parent.needsLobby())
				);
	}

	public Boolean isWaitroomClosedWhenRunning(){
		return closeWaitroomWhileRunning != null ? closeWaitroomWhileRunning :
			(parent != null ? parent.isWaitroomClosedWhenRunning() : null);
	}

	public void setWaitroomClosedWhileRunning(Boolean value) {
		this.closeWaitroomWhileRunning = value;
	}

	public Boolean isCancelIfNotEnoughPlayers(){
		return cancelIfNotEnoughPlayers != null ? cancelIfNotEnoughPlayers :
			(parent != null ? parent.isCancelIfNotEnoughPlayers() : null);
	}

	public void setCancelIfNotEnoughPlayers(Boolean value) {
		this.cancelIfNotEnoughPlayers = value;
	}
	public String getDisplayName() {
		return this.getName();
	}

	public int getQueueCount() {
		return BattleArena.getBAController().getArenaMatchQueue().getAllQueueCount(this);
	}

	public void setArenaCooldown(int cooldown) {
		this.arenaCooldown = cooldown;
	}

	public Integer getArenaCooldown() {
		return arenaCooldown != null ? arenaCooldown :
			(parent != null ? parent.getArenaCooldown(): null);
	}

	public void setAllowedTeamSizeDifference(int difference) {
		this.allowedTeamSizeDifference = difference;
	}

	public Integer getAllowedTeamSizeDifference() {
		return allowedTeamSizeDifference != null ? allowedTeamSizeDifference :
			(parent != null ? parent.getAllowedTeamSizeDifference(): null);
	}

}
