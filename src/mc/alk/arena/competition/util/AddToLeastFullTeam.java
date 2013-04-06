package mc.alk.arena.competition.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import mc.alk.arena.competition.Competition;
import mc.alk.arena.competition.events.Event.TeamSizeComparator;
import mc.alk.arena.objects.ArenaParams;
import mc.alk.arena.objects.MatchParams;
import mc.alk.arena.objects.exceptions.NeverWouldJoinException;
import mc.alk.arena.objects.options.JoinOptions;
import mc.alk.arena.objects.options.JoinOptions.JoinOption;
import mc.alk.arena.objects.queues.TeamQObject;
import mc.alk.arena.objects.teams.Team;
import mc.alk.arena.objects.teams.TeamFactory;

public class AddToLeastFullTeam extends TeamJoinHandler {

	public AddToLeastFullTeam(MatchParams params, Competition competition, Class<? extends Team> clazz) throws NeverWouldJoinException{
		super(params,competition,clazz);
		if (maxTeams == ArenaParams.MAX)
			throw new NeverWouldJoinException("If you add players by adding them to the next team in the list, there must be a finite number of players");
		/// Lets add in all our teams first
		for (int i=0;i<minTeams;i++){
			Team team = TeamFactory.createTeam(clazz);
			addTeam(team);
		}
	}

	@Override
	public TeamJoinResult joiningTeam(TeamQObject tqo) {
		Team team = tqo.getTeam();
		if (team.size()==1){
			Team oldTeam = addToPreviouslyLeftTeam(team.getPlayers().iterator().next());
			if (oldTeam != null)
				return new TeamJoinResult(TeamJoinStatus.ADDED_TO_EXISTING,minTeamSize - oldTeam.size(), oldTeam);
		}
		if ( maxTeamSize < team.size()){
			return CANTFIT;}
		/// Try to let them join their specified team if possible
		JoinOptions jo = tqo.getJoinOptions();
		if (jo != null && jo.hasOption(JoinOption.TEAM)){
			Integer index = (Integer) jo.getOption(JoinOption.TEAM);
			if (index < maxTeams){ /// they specified a team index within range
				Team baseTeam= teams.get(index);
				TeamJoinResult tjr = teamFits(baseTeam, team);
				if (tjr != CANTFIT)
					return tjr;
			}
		}
		/// Try to fit them with an existing team
		List<Team> sortedBySize = new ArrayList<Team>(teams);
		Collections.sort(sortedBySize, new TeamSizeComparator());
		for (Team baseTeam : sortedBySize){
			TeamJoinResult tjr = teamFits(baseTeam, team);
			if (tjr != CANTFIT)
				return tjr;
		}
		/// Since this is nearly the same as BinPack add... can we merge somehow easily?
		if (teams.size() < maxTeams){
			Team ct = TeamFactory.createTeam(clazz);
			ct.addPlayers(team.getPlayers());
			if (ct.size() == maxTeamSize){
				addTeam(ct);
				return new TeamJoinResult(TeamJoinStatus.ADDED, minTeamSize - ct.size(),ct);
			} else {
				pickupTeams.add(ct);
				TeamJoinResult ar = new TeamJoinResult(TeamJoinStatus.WAITING_FOR_PLAYERS, minTeamSize - ct.size(),ct);
				return ar;
			}
		} else {
			/// sorry peeps.. full up
			return CANTFIT;
		}
	}

	private TeamJoinResult teamFits(Team baseTeam, Team team) {
		if ( baseTeam.size() + team.size() <= maxTeamSize){
			addToTeam(baseTeam, team.getPlayers());
			if (baseTeam.size() == 0){
				return new TeamJoinResult(TeamJoinStatus.ADDED, minTeamSize - baseTeam.size(),baseTeam);
			} else {
				return new TeamJoinResult(TeamJoinStatus.ADDED_TO_EXISTING, minTeamSize - baseTeam.size(),baseTeam);
			}
		}
		return CANTFIT;
	}

	@Override
	public String toString(){
		return "["+competition.getParams().getName() +":JH:AddToLeast]";
	}
}
