package mc.alk.arena.controllers.joining;

import mc.alk.arena.Defaults;
import mc.alk.arena.competition.Competition;
import mc.alk.arena.objects.MatchParams;
import mc.alk.arena.objects.exceptions.NeverWouldJoinException;
import mc.alk.arena.objects.teams.ArenaTeam;
import mc.alk.arena.objects.teams.CompositeTeam;

import java.util.Collection;

public class TeamJoinFactory {

    public static AbstractJoinHandler createTeamJoinHandler(MatchParams params) throws NeverWouldJoinException {
        return createTeamJoinHandler(params,null,CompositeTeam.class);
    }

    public static AbstractJoinHandler createTeamJoinHandler(MatchParams params, Competition competition) throws NeverWouldJoinException {
		return createTeamJoinHandler(params,competition,CompositeTeam.class);
	}

    public static AbstractJoinHandler createTeamJoinHandler(MatchParams params, Collection<ArenaTeam> teams) throws NeverWouldJoinException {
        AbstractJoinHandler as = createTeamJoinHandler(params, null, CompositeTeam.class);
        for (ArenaTeam at : teams) {
            as.addTeam(at);}
        return as;
    }

	public static AbstractJoinHandler createTeamJoinHandler(MatchParams params, Competition competition,
			Class<? extends ArenaTeam> clazz) throws NeverWouldJoinException {
		if (params.getMaxTeams() <= Defaults.MAX_TEAMS ){
			return new AddToLeastFullTeam(params, competition);	/// lets try and add players to all players first
		} else { /// finite team size
			return new BinPackAdd(params, competition);
		}
	}

}
