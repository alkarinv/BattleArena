package mc.alk.arena.controllers.messaging;

import java.util.Collection;
import java.util.List;

import mc.alk.arena.objects.messaging.Channel;
import mc.alk.arena.objects.teams.Team;

public interface MatchMessageHandler {
	public void sendOnPreStartMsg(Channel serverChannel, List<Team> teams);
	public void sendOnStartMsg(Channel serverChannel, List<Team> teams);
	public void sendOnVictoryMsg(Channel serverChannel, Team victor, Collection<Team> losers);
	public void sendOnDrawMsg(Channel serverChannel, Collection<Team> participants);
	public void sendYourTeamNotReadyMsg(Team team);
	public void sendOtherTeamNotReadyMsg(Team team);
	public void sendOnIntervalMsg(Channel serverChannel,Team currentLeader, int remaining);
	public void sendTimeExpired(Channel serverChannel);

}
