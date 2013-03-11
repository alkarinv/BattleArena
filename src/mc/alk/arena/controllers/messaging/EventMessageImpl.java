package mc.alk.arena.controllers.messaging;

import java.util.Collection;
import java.util.Set;

import mc.alk.arena.competition.events.Event;
import mc.alk.arena.objects.ArenaPlayer;
import mc.alk.arena.objects.MatchParams;
import mc.alk.arena.objects.messaging.Channel;
import mc.alk.arena.objects.messaging.Message;
import mc.alk.arena.objects.messaging.MessageOptions.MessageOption;
import mc.alk.arena.objects.teams.Team;
import mc.alk.arena.serializers.MessageSerializer;
import mc.alk.arena.util.MessageUtil;


/**
 *
 * @author alkarin
 *
 */
public class EventMessageImpl extends MessageSerializer implements EventMessageHandler {

	final MatchParams mp;
	final Event event;

	public EventMessageImpl(Event e ){
		super(e.getParams().getName());
		this.mp = e.getParams();
		this.event = e;
	}

	@Override
	public void sendCountdownTillEvent(Channel serverChannel, int seconds) {
		Message message = getNodeMessage("event.countdownTillEvent");
		Message serverMessage = getNodeMessage("event.server_countdownTillEvent");
		Set<MessageOption> ops = message.getOptions();
		if (serverChannel != Channel.NullChannel){
			ops.addAll(serverMessage.getOptions());
		}
		String msg = message.getMessage();
		MessageFormatter msgf = new MessageFormatter(this, event.getParams(), ops.size(), 0, message, ops);
		msgf.formatCommonOptions(null,seconds);

		if (serverChannel != Channel.NullChannel){
			msg = msgf.getFormattedMessage(serverMessage);
			serverChannel.broadcast(msg);
		}
	}

	@Override
	public void sendEventStarting(Channel serverChannel, Collection<Team> teams) {
		final String nTeamPath = getStringPathFromSize(teams.size());
		Message message = getNodeMessage("event."+ nTeamPath+".start");
		Message serverMessage = getNodeMessage("event."+ nTeamPath+".server_start");
		formatAndSend(serverChannel, teams, message, serverMessage);
	}

	@Override
	public void sendEventVictory(Channel serverChannel, Collection<Team> victors, Collection<Team> losers) {
		final String nTeamPath = getStringPathFromSize(losers.size()+1);
		sendVictory(serverChannel,victors,losers,mp,"event."+nTeamPath+".victory", "event."+nTeamPath+".loss",
				"event."+nTeamPath+".server_victory");
	}

	@Override
	public void sendEventOpenMsg(Channel serverChannel) {
		if (serverChannel == Channel.NullChannel){
			return;
		}

		final String nTeamPath = getStringPathFromSize(mp.getMinTeams());
		Message serverMessage;
		if (mp.getMinTeamSize() > 1)
			serverMessage = getNodeMessage("event."+nTeamPath+".server_open_teamSizeGreaterThanOne");
		else
			serverMessage = getNodeMessage("event."+nTeamPath+".server_open");
		Set<MessageOption> ops = serverMessage.getOptions();
		String msg = serverMessage.getMessage();
		MessageFormatter msgf = new MessageFormatter(this, event.getParams(), ops.size(), 0, serverMessage, ops);
		msgf.formatCommonOptions(null);
		msg = msgf.getFormattedMessage(serverMessage);
		serverChannel.broadcast(msg);
	}

	@Override
	public void sendEventCancelledDueToLackOfPlayers(Channel serverChannel, Set<ArenaPlayer> competingPlayers) {
		MessageUtil.sendMessage(competingPlayers,mp.getPrefix()+"&e The Event has been cancelled b/c there weren't enough players");
	}

	@Override
	public void sendTeamJoinedEvent(Channel serverChannel, Team t) {
		t.sendMessage("&eYou have joined the &6" + mp.getName());
	}

	@Override
	public void sendEventCancelled(Channel serverChannel, Collection<Team> teams) {
		Message message = getNodeMessage("event.team_cancelled");
		Message serverMessage = getNodeMessage("event.server_cancelled");
		formatAndSend(serverChannel, teams, message, serverMessage);
	}

	private void formatAndSend(Channel serverChannel, Collection<Team> teams, Message message, Message serverMessage) {
		Set<MessageOption> ops = message.getOptions();
		if (serverChannel != Channel.NullChannel){
			ops.addAll(serverMessage.getOptions());
		}

		String msg = message.getMessage();
		MessageFormatter msgf = new MessageFormatter(this, mp, ops.size(), teams.size(), message, ops);
		msgf.formatCommonOptions(teams);
		for (Team t: teams){
			msgf.formatTeamOptions(t,false);
			msgf.formatTeams(teams);
			String newmsg = msgf.getFormattedMessage(message);
			t.sendMessage(newmsg);
		}

		if (serverChannel != Channel.NullChannel){
			msg = msgf.getFormattedMessage(serverMessage);
			serverChannel.broadcast(msg);
		}
	}

	@Override
	public void sendCantFitTeam(Team t) {
		t.sendMessage("&cThe &6" + event.getDisplayName()+"&c is full");
	}

	@Override
	public void sendWaitingForMorePlayers(Team team, int remaining) {
		team.sendMessage("&eYou have joined the &6" + event.getDisplayName());
		team.sendMessage("&eYou will enter the Event when &6" +remaining+"&e more "+MessageUtil.playerOrPlayers(remaining)+
				"&e have joined to make your team");
	}

	@Override
	public void sendEventDraw(Channel serverChannel, Collection<Team> participants, Collection<Team> losers) {
		final String nTeamPath = getStringPathFromSize(participants.size());
		sendVictory(serverChannel,null,participants,mp,"event."+nTeamPath+".draw","event."+nTeamPath+".draw",
				"event."+nTeamPath+".server_draw");
	}

}
