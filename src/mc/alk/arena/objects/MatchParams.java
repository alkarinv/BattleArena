package mc.alk.arena.objects;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import mc.alk.arena.controllers.ParamController;
import mc.alk.arena.controllers.RoomController;
import mc.alk.arena.controllers.containers.GameManager;
import mc.alk.arena.objects.arenas.ArenaType;
import mc.alk.arena.objects.messaging.AnnouncementOptions;
import mc.alk.arena.objects.modules.ArenaModule;
import mc.alk.arena.objects.options.TransitionOption;
import mc.alk.arena.objects.victoryconditions.VictoryType;
import mc.alk.arena.util.MessageUtil;

import org.bukkit.ChatColor;


public class MatchParams extends ArenaParams implements Comparable<MatchParams>{

	String prefix = null;
	VictoryType vc = null;
	Integer matchTime = null;
	Integer intervalTime = null;
	AnnouncementOptions ao = null;

	Integer nLives = null;
	Integer numConcurrentCompetitions = null;
	Set<ArenaModule> modules = null;
	Boolean useBTPvP = null;
	Boolean useBTMessages = null;
	Boolean useBTTeamRating = null;

	MatchParams mparent=null;

	public MatchParams(ArenaType at) {
		super(at);
		this.setRating(rating);
	}

	public MatchParams(MatchParams mp) {
		super(mp);
		this.prefix = mp.prefix;
		this.vc = mp.vc;

		this.matchTime = mp.matchTime;
		this.intervalTime = mp.intervalTime;
		this.ao = mp.ao;
		this.nLives = mp.nLives;
		this.numConcurrentCompetitions = mp.numConcurrentCompetitions;
		this.mparent = mp.mparent;
		this.useBTMessages = mp.useBTMessages;
		this.useBTPvP = mp.useBTPvP;
		this.useBTTeamRating  = mp.useBTTeamRating;
		if (mp.modules != null)
			this.modules = new HashSet<ArenaModule>(mp.modules);
	}

	@Override
	public void flatten() {
		if (mparent != null){
			mparent = (MatchParams) ParamController.copy(mparent);
			this.mparent.flatten();
			if (this.prefix == null) this.prefix = mparent.prefix;
			if (this.vc == null) this.vc = mparent.vc;
			if (this.matchTime == null) this.matchTime = mparent.matchTime;
			if (this.intervalTime == null) this.intervalTime = mparent.intervalTime;
			if (this.ao == null) this.ao = mparent.ao;
			if (this.nLives == null) this.nLives = mparent.nLives;
			if (this.numConcurrentCompetitions == null) this.numConcurrentCompetitions = mparent.numConcurrentCompetitions;
			if (this.useBTMessages == null) this.useBTMessages = mparent.useBTMessages;
			if (this.useBTPvP == null) this.useBTPvP = mparent.useBTPvP;
			if (this.useBTTeamRating == null) this.useBTTeamRating = mparent.useBTTeamRating;
			if (this.modules != null && mparent.modules != null){
				this.modules.addAll(mparent.modules);
			} else if (mparent.modules != null){
				this.modules = new HashSet<ArenaModule>(mparent.modules);
			}
			this.mparent = null;
		}
		super.flatten();
	}

	public void setVictoryType(VictoryType type){this.vc = type;}

	public VictoryType getVictoryType() {
		return vc == null && mparent!=null ? mparent.getVictoryType() : vc;
	}

	public String getPrefix(){
		return prefix == null && mparent!=null ? mparent.getPrefix() : prefix;
	}

	public void setPrefix(String str){prefix = str;}

	public void setCommand(String str){cmd = str;}

	public int compareTo(MatchParams other) {
		Integer hash = this.hashCode();
		return hash.compareTo(other.hashCode());
	}


	public void setVictoryCondition(VictoryType victoryCondition) {
		this.vc = victoryCondition;
	}

	public Integer getMatchTime() {
		return matchTime == null && mparent!=null ? mparent.getMatchTime() : matchTime;
	}

	public void setMatchTime(Integer matchTime) {
		this.matchTime = matchTime;
	}

	public Integer getIntervalTime() {
		return intervalTime ==null && mparent!=null ? mparent.getIntervalTime() : intervalTime;
	}

	public void setIntervalTime(Integer intervalTime) {
		this.intervalTime = intervalTime;
	}

	public void setNLives(Integer nlives){
		this.nLives = nlives;
	}

	public Integer getNLives(){
		return nLives==null&&mparent!=null ? mparent.getNLives() : nLives;
	}

	@Override
	public int hashCode() {
		return ((arenaType.ordinal()) << 27);
	}

	@Override
	public boolean equals(Object other) {
		if (this == other) return true;
		if (!(other instanceof MatchParams)) return false;
		return this.hashCode() == ((MatchParams) other).hashCode();
	}

	public void setAnnouncementOptions(AnnouncementOptions announcementOptions) {
		this.ao = announcementOptions;
	}

	public AnnouncementOptions getAnnouncementOptions() {
		return ao;
	}

	@Override
	public String toString(){
		return super.toString()+",vc=" + vc;
	}

	public ChatColor getColor() {
		return MessageUtil.getFirstColor(prefix);
	}

	public void setNConcurrentCompetitions(int number) {
		this.numConcurrentCompetitions = number;
	}

	public Integer getNConcurrentCompetitions(){
		return numConcurrentCompetitions ==null && mparent!=null ? mparent.getNConcurrentCompetitions() : numConcurrentCompetitions;
	}

	public JoinType getJoinType() {
		return JoinType.QUEUE;
	}

	public void addModule(ArenaModule am) {
		if (modules == null)
			modules = new HashSet<ArenaModule>();
		modules.add(am);
	}

	public Collection<ArenaModule> getModules(){
		return modules;
	}

	public Collection<ArenaModule> getAllModules(){
		if (modules == null)
			return null;
		List<ArenaModule> ms = new ArrayList<ArenaModule>(modules);
		if (mparent != null){
			ms.addAll(mparent.getAllModules());}
		return ms;
	}

	public void setUseTrackerPvP(Boolean enable) {
		useBTPvP = enable;
	}

	public Boolean getUseTrackerPvP() {
		return useBTPvP != null ? useBTPvP : (mparent!= null ? mparent.getUseTrackerPvP() : null);
	}


	public Boolean getUseTrackerMessages() {
		return useBTMessages != null ? useBTMessages : (mparent!= null ? mparent.getUseTrackerMessages() : null);
	}

	public void setUseTrackerMessages(Boolean enable) {
		useBTMessages = enable;
	}

	public void setForceStartTime(long forceStartTime) {

	}
	@Override
	public boolean valid() {
		return super.valid() && (getTransitionOptions() != null ?
				(!getTransitionOptions().hasAnyOption(TransitionOption.TELEPORTLOBBY) ||
						RoomController.hasLobby(getType())) : true);
	}

	@Override
	public Collection<String> getInvalidReasons() {
		List<String> reasons = new ArrayList<String>();
		if (getTransitionOptions() != null &&
				getTransitionOptions().hasAnyOption(TransitionOption.TELEPORTLOBBY) && !RoomController.hasLobby(getType()))
			reasons.add("Needs a Lobby");
		reasons.addAll(super.getInvalidReasons());
		return reasons;
	}

	@Override
	public void setParent(ArenaParams parent) {
		super.setParent(parent);
		if (parent != null && parent instanceof MatchParams){
			this.mparent = (MatchParams) parent;}
		else
			this.mparent = null;
	}

	public boolean hasQueue() {
		return true;
	}

	public GameManager getGameManager() {
		return GameManager.getGameManager(this);
	}


	public Rating getRated() {
		return rating;
	}

	public void setTeamRating(Boolean b) {
		this.useBTTeamRating = b;
	}

	public Boolean isTeamRating(){
		return useBTTeamRating != null ? useBTTeamRating : (mparent!= null ? mparent.isTeamRating() : null);
	}

}
