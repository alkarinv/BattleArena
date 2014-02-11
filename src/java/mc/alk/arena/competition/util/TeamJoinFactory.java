package mc.alk.arena.competition.util;

import mc.alk.arena.competition.Competition;
import mc.alk.arena.objects.ArenaSize;
import mc.alk.arena.objects.MatchParams;
import mc.alk.arena.objects.exceptions.NeverWouldJoinException;
import mc.alk.arena.objects.teams.ArenaTeam;
import mc.alk.arena.objects.teams.CompositeTeam;

import java.util.Collection;

public class TeamJoinFactory {

    public static TeamJoinHandler createTeamJoinHandler(MatchParams params) throws NeverWouldJoinException {
        return createTeamJoinHandler(params,null,CompositeTeam.class);
    }

    public static TeamJoinHandler createTeamJoinHandler(MatchParams params, Competition competition) throws NeverWouldJoinException {
		return createTeamJoinHandler(params,competition,CompositeTeam.class);
	}
    public static TeamJoinHandler createTeamJoinHandler(MatchParams params, Collection<ArenaTeam> teams) throws NeverWouldJoinException {
        return new BinPackAdd(params, teams, CompositeTeam.class);
    }

	public static TeamJoinHandler createTeamJoinHandler(MatchParams params, Competition competition,
			Class<? extends ArenaTeam> clazz) throws NeverWouldJoinException {
		/// do we have a finite set of players
		if (params.getMaxTeams() != ArenaSize.MAX ){
			return new AddToLeastFullTeam(params, competition,clazz);	/// lets try and add players to all players first
		} else { /// finite team size
			return new BinPackAdd(params, competition,clazz);
		}
	}

}
