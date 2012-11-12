package mc.alk.arena.competition.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import mc.alk.arena.competition.Competition;
import mc.alk.arena.competition.events.Event.TeamSizeComparator;
import mc.alk.arena.objects.ArenaParams;
import mc.alk.arena.objects.exceptions.NeverWouldJoinException;
import mc.alk.arena.objects.options.JoinOptions;
import mc.alk.arena.objects.options.JoinOptions.JoinOption;
import mc.alk.arena.objects.teams.CompositeTeam;
import mc.alk.arena.objects.teams.Team;
import mc.alk.arena.objects.teams.TeamFactory;

public class AddToLeastFullTeam extends TeamJoinHandler {

	public AddToLeastFullTeam(Competition competition) throws NeverWouldJoinException{
		super(competition);
		if (maxTeams == ArenaParams.MAX)
			throw new NeverWouldJoinException("If you add players by adding them to the next team in the list, there must be a finite number of players");
		/// Lets add in all our teams first
		for (int i=0;i<maxTeams;i++){
			CompositeTeam ct = TeamFactory.createCompositeTeam();
			competition.addTeam(ct);
		}
	}

	@Override
	public TeamJoinResult joiningTeam(Team team) {
		if ( maxTeamSize < team.size()){
			return CANTFIT;}
		/// Try to let them join their specified team if possible
		JoinOptions jo = team.getJoinPreferences();
		if (jo != null && jo.hasOption(JoinOption.TEAM)){
			Integer index = (Integer) jo.getOption(JoinOption.TEAM);
			if (index < maxTeams){ /// they specified a team index within range
				Team t= teams.get(index);
				TeamJoinResult tjr = teamFits(team, t);
				if (tjr != CANTFIT)
					return tjr;
			}
		}
		/// Try to fit them with an existing team
		List<Team> sortedBySize = new ArrayList<Team>(teams);
		Collections.sort(sortedBySize, new TeamSizeComparator());
		for (int i=0;i<sortedBySize.size();i++){
			Team t = sortedBySize.get(i);
			TeamJoinResult tjr = teamFits(team, t);
//			System.out.println("@@@@@@@@@@@@ teams.size()=" + teams.size() +"   " + t.size() +"    " + tjr);
			if (tjr != CANTFIT)
				return tjr;
		}
		return CANTFIT; /// we couldnt find a place for them
	}

	private TeamJoinResult teamFits(Team team, Team t) {
		if ( team.size() + t.size() <= maxTeamSize){
			CompositeTeam ct = (CompositeTeam) t;
			competition.addedToTeam(t,team.getPlayers());
			ct.addTeam(team);
			ct.finish();
			return new TeamJoinResult(TeamJoinStatus.ADDED_TO_EXISTING, minTeamSize - t.size(),t);
		}
		return CANTFIT;
	}

	@Override
	public String toString(){
		return "["+competition.getParams().getName() +":JH:AddToLeast]";
	}
}
