package mc.alk.arena.competition.util;

import mc.alk.arena.competition.Competition;
import mc.alk.arena.controllers.TeamController;
import mc.alk.arena.objects.MatchParams;
import mc.alk.arena.objects.exceptions.NeverWouldJoinException;
import mc.alk.arena.objects.teams.Team;
import mc.alk.arena.objects.teams.TeamFactory;

/**
 * When there is an infinite number of teams
 * @author alkarin
 *
 */
public class BinPackAdd extends TeamJoinHandler {
	boolean full = false;

	public BinPackAdd(MatchParams params, Competition competition, Class<? extends Team> clazz) throws NeverWouldJoinException{
		super(params, competition,clazz);
	}
	@Override
	public TeamJoinResult joiningTeam(Team team) {
		if ( maxTeamSize < team.size()){
			return CANTFIT;}
		final int teamSize = team.size();
		if (teamSize >= minTeamSize && teamSize <= maxTeamSize && teams.size() < maxTeams){ /// just add the team to the current team list
			Team ct = TeamFactory.createTeam(clazz);
			ct.addPlayers(team.getPlayers());
			addTeam(ct);
			return new TeamJoinResult(TeamJoinStatus.ADDED, 0,ct);
		}
		for (Team t: pickupTeams){
			final int size = t.size()+team.size();
			if (size <= maxTeamSize){
				t.addPlayers(team.getPlayers());
				if ( size >= minTeamSize){ /// the new team would be a valid range, add them
					pickupTeams.remove(t);
					addTeam(t);
					TeamController.removeTeamHandler(t, this);
					return new TeamJoinResult(TeamJoinStatus.ADDED, 0,t);
				} else {
					return new TeamJoinResult(TeamJoinStatus.WAITING_FOR_PLAYERS, minTeamSize - t.size(),t);
				}
			}
		}
		/// So we couldnt add them to an existing team
		/// Can we add them to a new team
		if (teams.size() < maxTeams){
			Team ct = TeamFactory.createTeam(clazz);
			ct.addPlayers(team.getPlayers());
			pickupTeams.add(ct);
			TeamJoinResult ar = new TeamJoinResult(TeamJoinStatus.WAITING_FOR_PLAYERS, minTeamSize - ct.size(),ct);
			return ar;
		} else {
			/// sorry peeps.. full up
			return CANTFIT;
		}
	}

	@Override
	public String toString(){
		return "["+competition.getParams().getName() +":JH:BinPackAdd]";
	}
}
