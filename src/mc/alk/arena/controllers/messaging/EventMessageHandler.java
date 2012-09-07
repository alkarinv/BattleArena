package mc.alk.arena.controllers.messaging;

import java.util.Collection;
import java.util.Set;

import mc.alk.arena.objects.ArenaPlayer;
import mc.alk.arena.objects.messaging.Channel;
import mc.alk.arena.objects.teams.Team;

public interface EventMessageHandler {
	public void sendCountdownTillEvent(Channel serverChannel, int seconds);
	public void sendEventStarting(Channel serverChannel, Collection<Team> teams);
	public void sendEventVictory(Channel serverChannel, Team victor, Collection<Team> losers);
	public void sendEventOpenMsg(Channel serverChannel);
	public void sendEventCancelledDueToLackOfPlayers(Channel serverChannel, Set<ArenaPlayer> competingPlayers);
	public void sendTeamJoinedEvent(Channel serverChannel, Team team);
	public void sendEventCancelled(Channel serverChannel);
	public void sendCantFitTeam(Team team);
	public void sendWaitingForMorePlayers(Team team, int remaining);
}
