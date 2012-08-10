package mc.alk.arena.controllers;

import java.util.Collection;
import java.util.Set;

import mc.alk.arena.objects.teams.Team;

import org.bukkit.entity.Player;

public interface EventMessageHandler {
	public void sendCountdownTillEvent(int seconds);
	public void sendEventStarting(Collection<Team> teams);
	public void sendEventWon(Team victor, Integer elo);
	public void sendEventOpenMsg();
	public void sendEventCancelledDueToLackOfPlayers(Set<Player> competingPlayers);
	public void sendTeamJoinedEvent(Team t);
	public void sendPlayerJoinedEvent(Player p);
	public void sendEventCancelled();
}
