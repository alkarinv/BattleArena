package mc.alk.arena.events.util;

import mc.alk.arena.controllers.TeamController;
import mc.alk.arena.events.Event;
import mc.alk.arena.objects.teams.CompositeTeam;
import mc.alk.arena.objects.teams.Team;

public class BinPackAdd extends TeamJoinHandler {
	
	public BinPackAdd(Event event) throws NeverWouldJoinException{
		super(event);
	}
	
	public TeamJoinResult joiningTeam(Team team) {
		if ( maxTeamSize < team.size()){
			return CANTFIT;}

		if (team.size() == maxTeamSize && teams.size() < maxTeams){ /// just add the team to the current team list
			CompositeTeam ct = TeamController.createCompositeTeam(team,this);
			TeamController.removeTeam(ct, this);
			ct.addTeam(team);
			ct.finish();
			event.addTeam(ct);
//			System.out.println("Adding team " + ct +"  ct size = " + ct.size() +"   teamSize=" + inEvent.size());
			return new TeamJoinResult(TeamJoinStatus.ADDED, 0,ct);
		}
		for (CompositeTeam t: pickupTeams){
			final int size = t.size()+team.size();
//			System.out.println("Checking here " + size +"   mts =" + minTeamSize +":" + maxTeamSize +"   t " +t);
			if (size <= maxTeamSize){
				CompositeTeam ct = (CompositeTeam) t;
				ct.addTeam(team);
				ct.finish();
				if ( size >= minTeamSize){ /// the new team would be a valid range, add them
					pickupTeams.remove(t);
					event.addTeam(ct);
					TeamController.removeTeam(ct, this);
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
	
	public String toString(){
		return "["+event.getName() +":JH:BinPackAdd]";
	}

}
