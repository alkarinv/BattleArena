package mc.alk.arena.objects.victoryconditions.extensions;

import java.util.Collection;

import mc.alk.arena.BattleArena;
import mc.alk.arena.competition.match.Match;
import mc.alk.arena.events.matches.MatchFindCurrentLeaderEvent;
import mc.alk.arena.objects.ArenaPlayer;
import mc.alk.arena.objects.arenas.ArenaListener;
import mc.alk.arena.objects.events.EventPriority;
import mc.alk.arena.objects.events.MatchEventHandler;
import mc.alk.arena.objects.teams.Team;
import mc.alk.arena.util.DmgDeathUtil;
import mc.alk.arena.util.VictoryUtil;

import org.bukkit.event.entity.PlayerDeathEvent;

public class PvPCount implements ArenaListener{
	final Match match;
	public PvPCount(Match match) {
		this.match = match;
	}

	@MatchEventHandler(suppressCastWarnings=true, priority=EventPriority.LOW)
	public void playerDeathEvent(PlayerDeathEvent event) {
		if (match.isWon()){
			return;}
		final ArenaPlayer p = BattleArena.toArenaPlayer(event.getEntity());
		if (p==null)
			return;
		final Team team = match.getTeam(p);
		if (team == null)
			return;
		final ArenaPlayer killer = DmgDeathUtil.getPlayerCause(event);
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
	}

	@MatchEventHandler(priority = EventPriority.LOW)
	public void onFindCurrentLeader(MatchFindCurrentLeaderEvent event) {

		Collection<Team> leaders = VictoryUtil.getLeaderByHighestKills(match);
		if (leaders.size() > 1){
			event.setCurrentDrawers(leaders);
		} else {
			event.setCurrentLeaders(leaders);
		}
	}
}
