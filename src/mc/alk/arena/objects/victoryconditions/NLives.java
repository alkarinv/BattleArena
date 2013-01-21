package mc.alk.arena.objects.victoryconditions;

import mc.alk.arena.BattleArena;
import mc.alk.arena.competition.match.Match;
import mc.alk.arena.objects.ArenaPlayer;
import mc.alk.arena.objects.events.EventPriority;
import mc.alk.arena.objects.events.MatchEventHandler;
import mc.alk.arena.objects.teams.Team;
import mc.alk.arena.objects.victoryconditions.interfaces.DefinesNumLivesPerPlayer;
import mc.alk.arena.util.DmgDeathUtil;

import org.bukkit.event.entity.PlayerDeathEvent;

public class NLives extends VictoryCondition implements DefinesNumLivesPerPlayer{

	int nLives; /// number of lives before a player is eliminated from a team

	public NLives(Match match) {
		super(match);
		nLives = 1;
	}

	public NLives(Match match, Integer maxLives) {
		super(match);
		this.nLives = maxLives;
	}
	public void setMaxLives(Integer maxLives) {
		this.nLives = maxLives;
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
		int deaths = team.addDeath(p);
		if (deaths >= nLives){
			team.killMember(p);}
	}

	@Override
	public int getLivesPerPlayer() {
		return nLives;
	}
}
