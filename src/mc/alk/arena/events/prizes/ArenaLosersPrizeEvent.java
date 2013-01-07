package mc.alk.arena.events.prizes;

import java.util.Collection;

import mc.alk.arena.competition.Competition;
import mc.alk.arena.objects.teams.Team;

public class ArenaLosersPrizeEvent extends ArenaPrizeEvent {

	public ArenaLosersPrizeEvent(Competition competition, Collection<Team> teams) {
		super(competition, teams);
	}

}
