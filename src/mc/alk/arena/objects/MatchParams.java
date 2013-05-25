package mc.alk.arena.objects;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import mc.alk.arena.controllers.LobbyController;
import mc.alk.arena.objects.arenas.ArenaType;
import mc.alk.arena.objects.messaging.AnnouncementOptions;
import mc.alk.arena.objects.modules.ArenaModule;
import mc.alk.arena.objects.options.TransitionOption;
import mc.alk.arena.objects.victoryconditions.VictoryType;
import mc.alk.arena.util.MessageUtil;

import org.bukkit.ChatColor;


public class MatchParams extends ArenaParams implements Comparable<MatchParams>{

	String prefix;
	VictoryType vc = null;
	Integer matchTime, intervalTime;
	AnnouncementOptions ao = null;

	Integer nLives = 1;
	int numConcurrentCompetitions = Integer.MAX_VALUE;
	Set<ArenaModule> modules = new HashSet<ArenaModule>();
	Boolean useBTPvP;
	Boolean useBTMessages;
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
		this.modules = new HashSet<ArenaModule>(mp.modules);
		this.mparent = mp.mparent;
	}

	public void setVictoryType(VictoryType type){this.vc = type;}

	public VictoryType getVictoryType() {return vc;}

	public String getPrefix(){return prefix;}

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
		return ((arenaType.ordinal()) << 27) +(rating.ordinal() << 25) + (minTeams<<12)+(vc.ordinal() << 8) + minTeamSize;
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

	public int getNConcurrentCompetitions(){
		return numConcurrentCompetitions;
	}

	public JoinType getJoinType() {
		return JoinType.QUEUE;
	}

	public void addModule(ArenaModule am) {
		modules.add(am);
	}

	public Collection<ArenaModule> getModules(){
		return modules;
	}

	public Collection<ArenaModule> getAllModules(){
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
		return super.valid() &&
				(!getTransitionOptions().hasAnyOption(TransitionOption.TELEPORTLOBBY) ||
						LobbyController.hasLobby(getType()));
	}

	@Override
	public Collection<String> getInvalidReasons() {
		List<String> reasons = new ArrayList<String>();
		if (getTransitionOptions().hasAnyOption(TransitionOption.TELEPORTLOBBY) && !LobbyController.hasLobby(getType()))
				reasons.add("Needs a Lobby");
		reasons.addAll(super.getInvalidReasons());
		return reasons;
	}

	@Override
	public void setParent(ArenaParams parent) {
		super.setParent(parent);
		if (parent instanceof MatchParams){
			this.mparent = (MatchParams) parent;}
		else
			this.mparent = null;
	}

	public boolean hasLobby() {
		return this.getTransitionOptions().hasAnyOption(TransitionOption.TELEPORTLOBBY);
	}

}
