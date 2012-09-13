package mc.alk.arena.controllers.messaging;

import java.util.Collection;
import java.util.Set;

import mc.alk.arena.competition.events.Event;
import mc.alk.arena.objects.ArenaPlayer;
import mc.alk.arena.objects.MatchState;
import mc.alk.arena.objects.messaging.AnnouncementOptions;
import mc.alk.arena.objects.messaging.Channel;
import mc.alk.arena.objects.teams.Team;


public class EventMessager {
	EventMessageHandler impl;
	final AnnouncementOptions bos;
	boolean silent = false;

	public EventMessager(Event event){
		this.impl = new EventMessageImpl(event);
		this.bos = event.getParams().getAnnouncementOptions();
	}

	private Channel getChannel(MatchState state) {
		if (silent) return Channel.NullChannel;
		return bos != null && bos.hasOption(false,state) ? bos.getChannel(false,state) : AnnouncementOptions.getDefaultChannel(false,state);
	}

	public void setMessageHandler(EventMessageHandler handler) {
		this.impl = handler;
	}

	public EventMessageHandler getMessageHandler() {
		return impl;
	}

	public void sendCountdownTillEvent(int seconds) {
		try{impl.sendCountdownTillEvent(getChannel(MatchState.ONPRESTART), seconds);}catch(Exception e){e.printStackTrace();}		
	}

	public void sendEventStarting(Collection<Team> teams) {
		try{impl.sendEventStarting(getChannel(MatchState.ONSTART), teams);}catch(Exception e){e.printStackTrace();}
	}

	public void sendEventVictory(Team victor, Collection<Team> losers) {
		try{impl.sendEventVictory(getChannel(MatchState.ONVICTORY), victor,losers);}catch(Exception e){e.printStackTrace();}
	}

	public void sendEventOpenMsg() {
		try{impl.sendEventOpenMsg(getChannel(MatchState.ONOPEN));}catch(Exception e){e.printStackTrace();}		
	}

	public void sendEventCancelledDueToLackOfPlayers(Set<ArenaPlayer> competingPlayers) {
		try{impl.sendEventCancelledDueToLackOfPlayers(getChannel(MatchState.ONCANCEL), competingPlayers);
	}catch(Exception e){e.printStackTrace();}
	}

	public void sendTeamJoinedEvent(Team t) {
		try{impl.sendTeamJoinedEvent(getChannel(MatchState.ONJOIN),t);}catch(Exception e){e.printStackTrace();}
	}

	public void sendEventCancelled() {
		try{impl.sendEventCancelled(getChannel(MatchState.ONCANCEL));}catch(Exception e){e.printStackTrace();}
	}

	public void sendCantFitTeam(Team t) {
		try{impl.sendCantFitTeam(t);}catch(Exception e){e.printStackTrace();}
	}

	public void sendWaitingForMorePlayers(Team t, int remaining) {
		try{ impl.sendWaitingForMorePlayers(t, remaining);}catch(Exception e){e.printStackTrace();}
	}
	public void setSilent(boolean silent){
		this.silent = silent;
	}

	public void sendEventDraw(Collection<Team> losers) {
		try{impl.sendEventDraw(getChannel(MatchState.ONVICTORY), losers);}catch(Exception e){e.printStackTrace();}		
	}

}
