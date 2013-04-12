package mc.alk.arena.competition.util;

import mc.alk.arena.competition.Competition;
import mc.alk.arena.objects.ArenaParams;
import mc.alk.arena.objects.MatchParams;
import mc.alk.arena.objects.exceptions.NeverWouldJoinException;
import mc.alk.arena.objects.teams.CompositeTeam;
import mc.alk.arena.objects.teams.ArenaTeam;

public class TeamJoinFactory {

	public static TeamJoinHandler createTeamJoinHandler(MatchParams params, Competition competition) throws NeverWouldJoinException {
		return createTeamJoinHandler(params,competition,CompositeTeam.class);
	}

	public static TeamJoinHandler createTeamJoinHandler(MatchParams params, Competition competition,
			Class<? extends ArenaTeam> clazz) throws NeverWouldJoinException {
		/// do we have a finite set of players
		if (params.getMaxTeams() != ArenaParams.MAX ){
			return new AddToLeastFullTeam(params, competition,clazz);	/// lets try and add players to all players first
		} else { /// finite team size
			return new BinPackAdd(params, competition,clazz);
		}
	}
}
