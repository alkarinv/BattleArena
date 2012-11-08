package mc.alk.arena.competition.util;

import mc.alk.arena.competition.Competition;
import mc.alk.arena.controllers.TeamController;
import mc.alk.arena.objects.exceptions.NeverWouldJoinException;
import mc.alk.arena.objects.teams.CompositeTeam;
import mc.alk.arena.objects.teams.Team;

/**
 * When there is an infinite number of teams
 * @author alkarin
 *
 */
public class BinPackAdd extends TeamJoinHandler {
	boolean full = false;

	public BinPackAdd(Competition competition) throws NeverWouldJoinException{
		super(competition);
	}

	@Override
	public TeamJoinResult joiningTeam(Team team) {
		if ( maxTeamSize < team.size()){
			return CANTFIT;}
		final int teamSize = team.size();
		if (teamSize >= minTeamSize && teamSize <= maxTeamSize && teams.size() < maxTeams){ /// just add the team to the current team list
			CompositeTeam ct = TeamController.createCompositeTeam(team,this);
			TeamController.removeTeamHandler(ct, this);
			ct.addTeam(team);
			ct.finish();
			competition.addTeam(ct);
			return new TeamJoinResult(TeamJoinStatus.ADDED, 0,ct);
		}
		for (CompositeTeam t: pickupTeams){
			final int size = t.size()+team.size();
			if (size <= maxTeamSize){
				CompositeTeam ct = t;
				ct.addTeam(team);
				ct.finish();
				if ( size >= minTeamSize){ /// the new team would be a valid range, add them
					pickupTeams.remove(t);
					competition.addTeam(ct);
					TeamController.removeTeamHandler(ct, this);
					return new TeamJoinResult(TeamJoinStatus.ADDED, 0,ct);
				} else{
					return new TeamJoinResult(TeamJoinStatus.ADDED_TO_EXISTING, minTeamSize - ct.size(),ct);
				}
			}
		}
		/// So we couldnt add them to an existing team
		/// Can we add them to a new team
		if (teams.size() < maxTeams){
			CompositeTeam ct = TeamController.createCompositeTeam(team,this);
			ct.addTeam(team);
			ct.finish();
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
