package mc.alk.arena.events.prizes;

import java.util.Collection;

import mc.alk.arena.competition.Competition;
import mc.alk.arena.objects.teams.Team;

public class ArenaDrawersPrizeEvent extends ArenaPrizeEvent {

	public ArenaDrawersPrizeEvent(Competition competition, Collection<Team> teams) {
		super(competition, teams);
	}

}
