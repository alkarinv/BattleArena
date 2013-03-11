package mc.alk.arena.controllers.messaging;

import java.util.Collection;
import java.util.Map;

import mc.alk.arena.objects.messaging.Channel;
import mc.alk.arena.objects.teams.Team;

public interface MatchMessageHandler {
	public void sendOnBeginMsg(Channel channel, Collection<Team> teams);
	public void sendOnPreStartMsg(Channel serverChannel, Collection<Team> teams);
	public void sendOnStartMsg(Channel serverChannel, Collection<Team> teams);
	public void sendOnVictoryMsg(Channel serverChannel, Collection<Team> victors, Collection<Team> losers);
	public void sendOnDrawMsg(Channel serverChannel, Collection<Team> drawers, Collection<Team> losers);
	public void sendYourTeamNotReadyMsg(Team team);
	public void sendOtherTeamNotReadyMsg(Team team);
	public void sendOnIntervalMsg(Channel serverChannel,Collection<Team> currentLeaders, int remaining);
	public void sendTimeExpired(Channel serverChannel);
	public String getMessage(String node);
	public String getMessage(String node, Map<String, String> params);
	public void sendMessage(String node);
	public void sendMessage(String node, Map<String, String> params);
	public String format(String text, Map<String, String> params);
}
