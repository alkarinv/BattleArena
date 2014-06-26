package mc.alk.arena.controllers.joining;

import mc.alk.arena.Defaults;
import mc.alk.arena.competition.Competition;
import mc.alk.arena.objects.MatchParams;
import mc.alk.arena.objects.exceptions.NeverWouldJoinException;
import mc.alk.arena.objects.teams.ArenaTeam;

import java.util.List;

public class TeamJoinFactory {

    public static AbstractJoinHandler createTeamJoinHandler(MatchParams params) throws NeverWouldJoinException {
        return createTeamJoinHandler(params,null, null);
    }

    public static AbstractJoinHandler createTeamJoinHandler(MatchParams params, Competition competition) throws NeverWouldJoinException {
		return createTeamJoinHandler(params,competition,null);
	}

    public static AbstractJoinHandler createTeamJoinHandler(MatchParams params, List<ArenaTeam> teams) throws NeverWouldJoinException {
        AbstractJoinHandler as = createTeamJoinHandler(params, null, teams);
        return as;
    }

	public static AbstractJoinHandler createTeamJoinHandler(MatchParams params, Competition competition,
			List<ArenaTeam> teams) throws NeverWouldJoinException {
		if (params.getMaxTeams() <= Defaults.MAX_TEAMS ){
			return new AddToLeastFullTeam(params, competition, teams);	/// lets try and add players to all players first
		} else { /// finite team size
			return new BinPackAdd(params, competition, teams);
		}
	}

}
