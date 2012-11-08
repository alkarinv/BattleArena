package mc.alk.arena.competition.util;

import mc.alk.arena.competition.Competition;
import mc.alk.arena.objects.ArenaParams;
import mc.alk.arena.objects.MatchParams;
import mc.alk.arena.objects.exceptions.NeverWouldJoinException;
import mc.alk.arena.util.Log;

public class TeamJoinFactory {

	public static TeamJoinHandler createTeamJoinHandler(Competition competition) throws NeverWouldJoinException {
		TeamJoinHandler joinHandler = null;
		MatchParams params = competition.getParams();
		if (params.getMaxTeams() != ArenaParams.MAX){ /// we have a finite set of players
			joinHandler = new AddToLeastFullTeam(competition);	/// lets try and add players to all players first
		} else { /// finite team size
			joinHandler = new BinPackAdd(competition);
		}
		return joinHandler;
	}
}
