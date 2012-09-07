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

	public EventMessager(Event event){
		this.impl = new EventMessageImpl(event);
		this.bos = event.getParams().getAnnouncementOptions();
	}

	private Channel getChannel(MatchState state) {
		return bos != null && bos.hasOption(false,state) ? bos.getChannel(false,state) : AnnouncementOptions.getDefaultChannel(false,state);
	}

	public void setMessageHandler(EventMessageHandler handler) {
		this.impl = handler;
	}

	public EventMessageHandler getMessageHandler() {
		return impl;
	}

	public void sendCountdownTillEvent(int seconds) {
		impl.sendCountdownTillEvent(getChannel(MatchState.ONPRESTART), seconds);		
	}

	public void sendEventStarting(Collection<Team> teams) {
		impl.sendEventStarting(getChannel(MatchState.ONSTART), teams);		
	}

	public void sendEventVictory(Team victor, Collection<Team> losers) {
		impl.sendEventVictory(getChannel(MatchState.ONVICTORY), victor,losers);		
	}

	public void sendEventOpenMsg() {
		impl.sendEventOpenMsg(getChannel(MatchState.ONOPEN));		
	}

	public void sendEventCancelledDueToLackOfPlayers(Set<ArenaPlayer> competingPlayers) {
		impl.sendEventCancelledDueToLackOfPlayers(getChannel(MatchState.ONCANCEL), competingPlayers);		
	}

	public void sendTeamJoinedEvent(Team t) {
		impl.sendTeamJoinedEvent(getChannel(MatchState.ONJOIN),t);
	}

	public void sendEventCancelled() {
		impl.sendEventCancelled(getChannel(MatchState.ONCANCEL));		
	}

	public void sendCantFitTeam(Team t) {
		impl.sendCantFitTeam(t);		
	}

	public void sendWaitingForMorePlayers(Team t, int remaining) {
		impl.sendWaitingForMorePlayers(t, remaining);		
	}


}
