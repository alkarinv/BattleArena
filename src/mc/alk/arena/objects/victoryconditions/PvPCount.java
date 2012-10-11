package mc.alk.arena.objects.victoryconditions;

import mc.alk.arena.competition.match.Match;
import mc.alk.arena.objects.ArenaPlayer;
import mc.alk.arena.objects.events.MatchEventHandler;
import mc.alk.arena.objects.teams.Team;
import mc.alk.arena.util.DmgDeathUtil;

import org.bukkit.event.entity.PlayerDeathEvent;

public class PvPCount extends OneTeamLeft{

	public PvPCount(Match match) {
		super(match);
	}

	@MatchEventHandler(suppressCastWarnings=true)
	public void playerDeathEvent(PlayerDeathEvent event, ArenaPlayer p) {
		if (p==null || match.isWon() || !match.isStarted()){
			return;}
		Team team = match.getTeam(p);
		if (team == null)
			return;
		ArenaPlayer killer = DmgDeathUtil.getPlayerCause(event);
		handleDeath(p,team, killer);
	}

	protected void handleDeath(ArenaPlayer p,Team team, ArenaPlayer killer) {
		/// Add a kill to the killing team, and a death to the other team
		if (killer != null && killer != p){
			Team killerTeam = match.getTeam(killer);
			if (killerTeam != null)
				killerTeam.addKill(killer);
		}
		team.addDeath(p);
		super.handleDeath(team);
	}
}
