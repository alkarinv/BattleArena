package mc.alk.arena.objects.victoryconditions;

import mc.alk.arena.competition.match.Match;
import mc.alk.arena.events.PlayerLeftEvent;
import mc.alk.arena.events.matches.MatchFindNeededTeamsEvent;
import mc.alk.arena.objects.ArenaPlayer;
import mc.alk.arena.objects.events.MatchEventHandler;
import mc.alk.arena.objects.events.TransitionEventHandler;
import mc.alk.arena.objects.teams.Team;
import mc.alk.arena.util.DmgDeathUtil;

import org.bukkit.event.entity.PlayerDeathEvent;

public class NDeaths extends VictoryCondition{

	final int ndeaths; /// number of deaths before teams are eliminated

	public NDeaths(Match match, Integer ndeaths) {
		super(match);
		this.ndeaths = ndeaths;
	}

	@TransitionEventHandler
	public void onPlayerLeft(PlayerLeftEvent event) {
		killPlayer(event.getPlayer());		
	}

	@TransitionEventHandler
	public void onNeededTeams(MatchFindNeededTeamsEvent event) {
		event.setNeededTeams(2);
	}

	@MatchEventHandler(suppressCastWarnings=true)
	public void playerDeathEvent(PlayerDeathEvent event, ArenaPlayer p) {
		ArenaPlayer killer = DmgDeathUtil.getPlayerCause(event);
		if (killer != null){
			Team team = match.getTeam(p);
			if (team != null)
				team.addDeath(p);
			team = match.getTeam(killer);
			if (team != null)
				team.addKill(killer);
		}
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
		if (team.getNDeaths(p) >= ndeaths){
			team.killMember(p);}

		if (!team.isDead()){ /// killing the player didnt even kill the team, definitely no victory here
			return;}

		/// Killing this player killed the team
		Team leftAlive = null;
		/// Iterate over the players to see if we have one team left standing

		for (Team t: match.getTeams()){
			if (t.isDead())
				continue;
			if (leftAlive != null) /// obviously more than one team is still in the match
				return;
			leftAlive = t;				
		}
		/// One team left alive = victory
		match.setVictor(leftAlive);
	}
}
