package mc.alk.arena.objects;

import java.util.ArrayList;
import java.util.List;

import mc.alk.arena.objects.arenas.ArenaType;
import mc.alk.arena.objects.messaging.AnnouncementOptions;
import mc.alk.arena.objects.victoryconditions.VictoryType;
import mc.alk.arena.util.MessageUtil;

import org.bukkit.ChatColor;


public class MatchParams extends ArenaParams implements Comparable<MatchParams>{

	List<String> matchWillBeginMsgs = null;
	List<String> startMsgs = null;
	String prefix;
	String prettyName;
	VictoryType vc = null;
	Integer matchTime, intervalTime;
	AnnouncementOptions ao = null;

	public MatchParams(ArenaType at, Rating rating, VictoryType vc) {
		super(at,rating);
		this.vc = vc;
	}

	public MatchParams(MatchParams q) {
		super(q);
		this.matchWillBeginMsgs = q.matchWillBeginMsgs;
		this.startMsgs = q.startMsgs;
		this.prefix = q.prefix;
		this.prettyName = q.prettyName;
		this.vc = q.vc;
		this.matchTime = q.matchTime;
		this.intervalTime = q.intervalTime;
		this.ao = q.ao;
	}

	@Override
	public int getMinTeams(){return minTeams;}
	@Override
	public int getMinTeamSize(){ return minTeamSize;}
	public VictoryType getVictoryType() {return vc;}

	public int getSize() {return minTeamSize;}
	public String getPrefix(){
		return prefix;
	}
	public void setPrefix(String str){prefix = str;}
	public void setCommand(String str){cmd = str;}
	public void setPrettyName(String str){prettyName = str;}
	public void addStartMessage(String str){
		if (startMsgs==null)
			startMsgs = new ArrayList<String>();
		startMsgs.add(str);
	}

	public String getStartMsgs(){return MessageUtil.convertToString(startMsgs);}
	public String getSendMatchWillBeginMessage() {return MessageUtil.convertToString(matchWillBeginMsgs);}

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
		String lbl = prefix.replaceAll("&", "§");
		int index = lbl.indexOf("§");
		if (index != -1 && lbl.length() > index+2){
			ChatColor cc = ChatColor.getByChar(lbl.charAt(index+2));
			if (cc != null)
				return cc;
		}
		return ChatColor.GREEN;
	}

}
