package mc.alk.arena.objects.victoryconditions;

import mc.alk.arena.match.Match;
import mc.alk.arena.objects.ArenaPlayer;
import mc.alk.arena.objects.MatchEventHandler;
import mc.alk.arena.objects.teams.Team;

import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class NDeaths extends VictoryCondition{

	final int ndeaths; /// number of deaths before teams are eliminated

	public NDeaths(Match match, Integer ndeaths) {
		super(match);
		this.ndeaths = ndeaths;
	}

	@MatchEventHandler
	public void onPlayerQuit(PlayerQuitEvent event, ArenaPlayer p) {
		killPlayer(p);		
	}
	@MatchEventHandler
	public void onPlayerKick(PlayerKickEvent event, ArenaPlayer p) {
		killPlayer(p);		
	}

	@Override
	public void playerLeft(ArenaPlayer p) {
		killPlayer(p);
	}

	protected void killPlayer(ArenaPlayer p){
		Team team = match.getTeam(p);
		if (team == null)
			return;
		team.killMember(p);
		playerDeath(p,team);
	}

	@MatchEventHandler(suppressCastWarnings=true)
	public void playerDeathEvent(PlayerDeathEvent event, ArenaPlayer p) {
		Team team = match.getTeam(p);
		playerDeath(p,team);		
	}


	private void playerDeath(ArenaPlayer p,Team team) {
		if (match.isWon()){
			//			match.unregister(this);
			return;
		}
		if (!match.isStarted())
			return;

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

	@Override
	public boolean hasTimeVictory() {
		return false;
	}
}
