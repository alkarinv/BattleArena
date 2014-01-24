package mc.alk.arena.objects.victoryconditions;

import mc.alk.arena.competition.match.Match;
import mc.alk.arena.events.teams.TeamDeathEvent;
import mc.alk.arena.objects.MatchResult;
import mc.alk.arena.objects.events.ArenaEventHandler;
import mc.alk.arena.objects.teams.ArenaTeam;
import mc.alk.arena.objects.victoryconditions.interfaces.DefinesNumTeams;
import mc.alk.arena.util.MinMax;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class NTeamsNeeded extends VictoryCondition implements DefinesNumTeams{
	MinMax neededTeams;

	public NTeamsNeeded(Match match, int nTeams) {
		super(match);
		this.neededTeams = new MinMax(nTeams);
	}

	public MinMax getNeededNumberOfTeams(){
		return neededTeams;
	}

	@SuppressWarnings("UnusedParameters")
    @ArenaEventHandler
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
		if (leftAlive.size() < neededTeams.min){
			MatchResult mr = new MatchResult();
			mr.setVictors(leftAlive);
			Set<ArenaTeam> losers = new HashSet<ArenaTeam>(match.getTeams());
			losers.removeAll(leftAlive);
			mr.setLosers(losers);
			match.endMatchWithResult(mr);
		}
	}

    @Override
    public String toString(){
        return "[VC "+this.getClass().getSimpleName()+" : " + id+" nTeams="+neededTeams+"]";
    }
}
