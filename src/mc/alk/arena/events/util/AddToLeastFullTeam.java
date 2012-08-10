package mc.alk.arena.events.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import mc.alk.arena.controllers.TeamController;
import mc.alk.arena.events.Event;
import mc.alk.arena.events.Event.TeamSizeComparator;
import mc.alk.arena.objects.ArenaParams;
import mc.alk.arena.objects.teams.CompositeTeam;
import mc.alk.arena.objects.teams.Team;

public class AddToLeastFullTeam extends TeamJoinHandler {

	public AddToLeastFullTeam(Event event) throws NeverWouldJoinException{
		super(event);
		if (maxTeams == ArenaParams.MAX)
			throw new NeverWouldJoinException("If you add players by adding them to the next team in the list, there must be a finite number of inEvent");
	}

	public TeamJoinResult joiningTeam(Team team) {
		if ( maxTeamSize < team.size()){
			return CANTFIT;}

		if (teams.size() < maxTeams){ /// just add the team to the current team list
			CompositeTeam ct = TeamController.createCompositeTeam(team.getPlayers());
			ct.addTeam(team);
			ct.finish();
			event.addTeam(ct);
			//			System.out.println("Adding team " + ct +"  ct size = " + ct.size() +"   teamSize=" + inEvent.size());
			return new TeamJoinResult(TeamJoinStatus.ADDED, 0, ct);
		} else { /// we are full up on inEvent.. try to add them to another team
			/// Try to fit them with an existing team
			List<Team> sortedBySize = new ArrayList<Team>(teams);
			Collections.sort(sortedBySize, new TeamSizeComparator());
			for (int i=0;i<sortedBySize.size();i++){
				Team t = sortedBySize.get(i);	
				if ( team.size() + t.size() <= maxTeamSize){
					CompositeTeam ct = (CompositeTeam) t;
					ct.addTeam(team);
					ct.finish();
					//					System.out.println("Adding team " + ct +"  ct size = " + ct.size() +"   teamSize=" + inEvent.size());
					return new TeamJoinResult(TeamJoinStatus.ADDED_TO_EXISTING, minTeamSize - t.size(),t);
					//					return true;
				}
			}
			return CANTFIT; /// we couldnt find a place for them
		}
	}

	public String toString(){
		return "["+event.getName() +":JH:AddToLeast]";

	}
}
