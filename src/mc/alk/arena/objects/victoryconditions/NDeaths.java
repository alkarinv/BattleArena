package mc.alk.arena.objects.victoryconditions;

import mc.alk.arena.competition.match.Match;
import mc.alk.arena.objects.ArenaPlayer;
import mc.alk.arena.objects.teams.Team;

public class NDeaths extends PvPCount{

	final int maxdeaths; /// number of deaths before teams are eliminated

	public NDeaths(Match match, Integer maxdeaths) {
		super(match);
		this.maxdeaths = maxdeaths;
	}

	@Override
	protected void handleDeath(ArenaPlayer p,Team team, ArenaPlayer killer) {
		if (team.getNDeaths(p) >= maxdeaths){
			team.killMember(p);}
		super.handleDeath(p, team,killer);
	}
}
