package mc.alk.arena.objects.victoryconditions;

import mc.alk.arena.competition.match.Match;
import mc.alk.arena.events.matches.MatchFindCurrentLeaderEvent;
import mc.alk.arena.events.matches.MatchFindNeededTeamsEvent;
import mc.alk.arena.objects.ArenaPlayer;
import mc.alk.arena.objects.events.MatchEventHandler;
import mc.alk.arena.objects.events.TransitionEventHandler;
import mc.alk.arena.objects.teams.Team;
import mc.alk.arena.util.VictoryUtil;

import org.bukkit.event.entity.PlayerDeathEvent;

public class HighestKills extends VictoryCondition{
	public HighestKills(Match match) {
		super(match);
	}

	@TransitionEventHandler
	public void onFindCurrentLeader(MatchFindCurrentLeaderEvent event) {
		event.setCurrentLeader(VictoryUtil.highestKills(match));
	}

	@TransitionEventHandler
	public void onNeededTeams(MatchFindNeededTeamsEvent event) {
		event.setNeededTeams(2);
	}
	
	@MatchEventHandler(suppressCastWarnings=true)
	public void playerDeathEvent(PlayerDeathEvent event, ArenaPlayer p) {
		killPlayer(p);		
	}

	protected void killPlayer(ArenaPlayer p){
		if (match.isWon() || !match.isStarted()){
			return;}
		Team team = match.getTeam(p);
		if (team == null)
			return;
		killPlayer(p,team);
	}

	private void killPlayer(ArenaPlayer p,Team team) {
		team.addDeath(p);
		return;
	}
}
