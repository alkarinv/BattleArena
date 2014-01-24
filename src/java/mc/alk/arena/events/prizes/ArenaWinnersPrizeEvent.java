package mc.alk.arena.events.prizes;

import java.util.Collection;

import mc.alk.arena.competition.Competition;
import mc.alk.arena.objects.teams.ArenaTeam;

public class ArenaWinnersPrizeEvent extends ArenaPrizeEvent {

	public ArenaWinnersPrizeEvent(Competition competition,Collection<ArenaTeam> teams) {
		super(competition, teams);
	}

}
