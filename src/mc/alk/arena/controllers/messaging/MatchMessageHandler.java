package mc.alk.arena.controllers.messaging;

import java.util.Collection;
import java.util.Map;

import mc.alk.arena.objects.ArenaPlayer;
import mc.alk.arena.objects.messaging.Channel;
import mc.alk.arena.objects.teams.ArenaTeam;

public interface MatchMessageHandler {
	public void sendOnBeginMsg(Channel channel, Collection<ArenaTeam> teams);
	public void sendOnPreStartMsg(Channel serverChannel, Collection<ArenaTeam> teams);
	public void sendOnStartMsg(Channel serverChannel, Collection<ArenaTeam> teams);
	public void sendOnVictoryMsg(Channel serverChannel, Collection<ArenaTeam> victors, Collection<ArenaTeam> losers);
	public void sendOnDrawMsg(Channel serverChannel, Collection<ArenaTeam> drawers, Collection<ArenaTeam> losers);
	public void sendYourTeamNotReadyMsg(ArenaTeam team);
	public void sendOtherTeamNotReadyMsg(ArenaTeam team);
	public void sendOnIntervalMsg(Channel serverChannel,Collection<ArenaTeam> currentLeaders, int remaining);
	public void sendTimeExpired(Channel serverChannel);
	public String getMessage(String node);
	public String getMessage(String node, Map<String, String> params);
	public void sendMessage(String node);
	public void sendMessage(String node, Map<String, String> params);
	public String format(String text, Map<String, String> params);
	public void sendAddedToTeam(ArenaTeam team, ArenaPlayer player);
	public void sendTeamJoinedEvent(Channel serverChannel, ArenaTeam team);

}
