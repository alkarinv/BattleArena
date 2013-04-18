package mc.alk.arena.objects;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import mc.alk.arena.objects.arenas.ArenaType;
import mc.alk.arena.objects.messaging.AnnouncementOptions;
import mc.alk.arena.objects.modules.ArenaModule;
import mc.alk.arena.objects.victoryconditions.VictoryType;
import mc.alk.arena.util.MessageUtil;

import org.bukkit.ChatColor;


public class MatchParams extends ArenaParams implements Comparable<MatchParams>{

	String prefix;
	VictoryType vc = null;
	Integer matchTime, intervalTime;
	AnnouncementOptions ao = null;

	Integer nDeaths = 1;
	boolean overrideDefaultBattleTracker = true;
	int numConcurrentCompetitions = Integer.MAX_VALUE;
	Set<ArenaModule> modules = new HashSet<ArenaModule>();

	public MatchParams(ArenaType at, Rating rating, VictoryType vc) {
		super(at);
		this.setRating(rating);
		this.vc = vc;
	}

	public MatchParams(MatchParams mp) {
		super(mp);
		this.prefix = mp.prefix;
		this.vc = mp.vc;
		this.matchTime = mp.matchTime;
		this.intervalTime = mp.intervalTime;
		this.ao = mp.ao;
		this.nDeaths = mp.nDeaths;
		this.numConcurrentCompetitions = mp.numConcurrentCompetitions;
		this.modules = new HashSet<ArenaModule>(mp.modules);
	}

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
		return matchTime;
	}

	public void setMatchTime(Integer matchTime) {
		this.matchTime = matchTime;
	}

	public Integer getIntervalTime() {
		return intervalTime;
	}

	public void setIntervalTime(Integer intervalTime) {
		this.intervalTime = intervalTime;
	}

	public void setNLives(Integer ndeaths){
		this.nDeaths = ndeaths;
	}

	public Integer getNLives(){
		return nDeaths;
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
		return super.toString()+",vc=" + vc.getName();
	}

	public ChatColor getColor() {
		return MessageUtil.getFirstColor(prefix);
	}

	public void setOverrideBattleTracker(boolean enable) {
		overrideDefaultBattleTracker = enable;
	}
	public boolean getOverrideBattleTracker() {
		return overrideDefaultBattleTracker;
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
}
