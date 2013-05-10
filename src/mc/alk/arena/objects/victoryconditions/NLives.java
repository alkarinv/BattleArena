package mc.alk.arena.objects.victoryconditions;

import mc.alk.arena.competition.match.Match;
import mc.alk.arena.events.players.ArenaPlayerDeathEvent;
import mc.alk.arena.objects.events.EventPriority;
import mc.alk.arena.objects.events.ArenaEventHandler;
import mc.alk.arena.objects.teams.ArenaTeam;
import mc.alk.arena.objects.victoryconditions.interfaces.DefinesNumLivesPerPlayer;

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

	@ArenaEventHandler(suppressCastWarnings=true, priority=EventPriority.LOW)
	public void playerDeathEvent(ArenaPlayerDeathEvent event) {
		ArenaTeam team = event.getTeam();
		Integer deaths = team.getNDeaths(event.getPlayer());
		if (deaths == null)
			deaths = 1;
		if (deaths >= nLives){
			team.killMember(event.getPlayer());}
	}

	@Override
	public int getLivesPerPlayer() {
		return nLives;
	}
}
