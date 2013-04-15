package mc.alk.arena.objects.victoryconditions;

import java.util.ArrayList;
import java.util.List;

import mc.alk.arena.competition.match.Match;
import mc.alk.arena.events.teams.TeamDeathEvent;
import mc.alk.arena.objects.events.MatchEventHandler;
import mc.alk.arena.objects.teams.ArenaTeam;
import mc.alk.arena.objects.victoryconditions.interfaces.DefinesNumTeams;
import mc.alk.arena.util.MinMax;

public class NTeamsNeeded extends VictoryCondition implements DefinesNumTeams{
	MinMax neededTeams;
	public NTeamsNeeded(Match match, int nTeams) {
		super(match);
		this.neededTeams = new MinMax(nTeams);
	}

	public MinMax getNeededNumberOfTeams(){
		return neededTeams;
	}

	@MatchEventHandler
	public void onTeamDeathEvent(TeamDeathEvent event) {
		/// Killing this player killed the team
		List<ArenaTeam> leftAlive = new ArrayList<ArenaTeam>(neededTeams.min+1);
		/// Iterate over the players to see if we have one team left standing
		for (ArenaTeam t: match.getTeams()){
			if (t.isDead())
				continue;
			leftAlive.add(t);
			if (leftAlive.size() >= neededTeams.min){ ///more than enough teams still in the match
				return;}
		}
		if (leftAlive.isEmpty()){
			match.setLosers();
			return;
		}
		switch(neededTeams.min){
		case 2:
			if (leftAlive.size() ==1){
				match.setVictor(leftAlive.get(0));
			}
		}
	}
}
