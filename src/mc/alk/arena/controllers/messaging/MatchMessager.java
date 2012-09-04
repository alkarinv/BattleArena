package mc.alk.arena.controllers.messaging;

import java.util.Collection;
import java.util.List;

import mc.alk.arena.match.Match;
import mc.alk.arena.objects.MatchState;
import mc.alk.arena.objects.messaging.AnnouncementOptions;
import mc.alk.arena.objects.messaging.Channel;
import mc.alk.arena.objects.teams.Team;


public class MatchMessager {
	MatchMessageHandler impl;
	final AnnouncementOptions bos;

	public MatchMessager(Match match){
		this.impl = new MatchMessageImpl(match);
		this.bos = match.getParams().getAnnouncementOptions();
	}

	private Channel getChannel(MatchState state) {
//		System.out.println("!!!!!!! bos  = " + (bos != null && bos.hasOption(state) ? 
//				bos.getChannel(state) : AnnouncementOptions.getDefaultChannel(state)));
		return bos != null && bos.hasOption(state) ? bos.getChannel(state) : AnnouncementOptions.getDefaultChannel(state);
	}

	public void sendOnPreStartMsg(List<Team> teams) {
		impl.sendOnPreStartMsg(getChannel(MatchState.ONPRESTART), teams);
	}

	public void sendOnStartMsg(List<Team> teams) {
		impl.sendOnStartMsg(getChannel(MatchState.ONSTART), teams);
	}

	public void sendOnVictoryMsg(Team victor, Collection<Team> losers) {
		impl.sendOnVictoryMsg(getChannel(MatchState.ONVICTORY), victor,losers);
	}

	public void sendYourTeamNotReadyMsg(Team t1) {
		impl.sendYourTeamNotReadyMsg(t1);
	}

	public void sendOtherTeamNotReadyMsg(Team t1) {
		impl.sendOtherTeamNotReadyMsg(t1);
	}

	public void sendOnIntervalMsg(int remaining) {
		impl.sendOnIntervalMsg(getChannel(MatchState.ONMATCHINTERVAL), remaining);
	}

	public void sendTimeExpired() {
		impl.sendTimeExpired(getChannel(MatchState.ONMATCHTIMEEXPIRED));
	}

	public void setMessageHandler(MatchMessageHandler mc) {
		this.impl = mc;
	}

	public MatchMessageHandler getMessageHandler() {
		return impl;
	}
}
