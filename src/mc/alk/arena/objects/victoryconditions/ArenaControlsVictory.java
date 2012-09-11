package mc.alk.arena.objects.victoryconditions;

import mc.alk.arena.competition.match.Match;
import mc.alk.arena.objects.ArenaPlayer;
import mc.alk.arena.objects.MatchEventHandler;
import mc.alk.arena.objects.teams.Team;
import mc.alk.arena.util.Log;

import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class ArenaControlsVictory extends VictoryCondition {
	public ArenaControlsVictory(Match match) {
		super(match);
	}

	@Override
	public boolean hasTimeVictory() {
		return false;
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

	private void playerDeath(ArenaPlayer p,Team team) {
		if (match.isWon()){
			return;}

		if (!match.isStarted())
			return;

		team.addDeath(p);
		team.killMember(p);

		if (!team.allPlayersOffline()){ /// Team is still around
			return;}

		/// This player leaving made the team all offline
		Team leftAlive = null;
		/// Iterate over the players to see if we have one team left standing

		for (Team t: match.getTeams()){
			if (t.allPlayersOffline())
				continue;
			if (leftAlive != null) /// we have more than 1 team competing still
				return;
			leftAlive = t;				
		}
		/// One team still around = victory
		match.setVictor(leftAlive);
	}
}
