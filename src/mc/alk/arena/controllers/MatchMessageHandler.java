package mc.alk.arena.controllers;

import java.util.Collection;
import java.util.List;

import mc.alk.arena.objects.MatchParams;
import mc.alk.arena.objects.arenas.Arena;
import mc.alk.arena.objects.teams.Team;

public interface MatchMessageHandler {
	public void sendOnPreStartMsg(List<Team> teams, Arena arena);
	public void sendOnStartMsg(List<Team> teams);
	public void sendOnVictoryMsg(Team victor, Collection<Team> losers,  MatchParams mp);
	public void sendYourTeamNotReadyMsg(Team t1);
	public void sendOtherTeamNotReadyMsg(Team t1);
	public void sendOnIntervalMsg(int remaining);
	public void sendTimeExpired();

}
