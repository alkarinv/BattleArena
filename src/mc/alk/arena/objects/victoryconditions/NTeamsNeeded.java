package mc.alk.arena.objects.victoryconditions;

import java.util.ArrayList;
import java.util.List;

import mc.alk.arena.competition.match.Match;
import mc.alk.arena.events.PlayerLeftEvent;
import mc.alk.arena.events.matches.MatchFindNeededTeamsEvent;
import mc.alk.arena.objects.ArenaPlayer;
import mc.alk.arena.objects.events.MatchEventHandler;
import mc.alk.arena.objects.teams.Team;

public class NTeamsNeeded extends VictoryCondition{
	final int neededTeams;
	public NTeamsNeeded(Match match, int nTeams) {
		super(match);
		this.neededTeams = nTeams;
	}

	@MatchEventHandler
	public void onNeededTeams(MatchFindNeededTeamsEvent event) {
		event.setNeededTeams(neededTeams);
	}

	@MatchEventHandler
	public void onPlayerLeft(PlayerLeftEvent event) {
		ArenaPlayer p = event.getPlayer();
		if (match.isWon() || !match.isStarted()){
			return;}

		Team team = match.getTeam(p);
		if (team == null)
			return;
		if (team.killMember(p)){
			handleDeath(team);}
	}

	protected void handleDeath(Team team) {
		if (!team.isDead()){ /// team isnt dead
			return;}

		/// Killing this player killed the team
		List<Team> leftAlive = new ArrayList<Team>(neededTeams+1);
		/// Iterate over the players to see if we have one team left standing

		for (Team t: match.getTeams()){
			if (t.isDead())
				continue;
			leftAlive.add(t);
			if (leftAlive.size() >= neededTeams){ /// obviously more than one team is still in the match
				return;
			}
		}
		if (leftAlive.isEmpty()){
			match.setLosers();
			return;
		}
		switch(neededTeams){
		case 2:
			if (leftAlive.size() ==1){
				match.setVictor(leftAlive.get(0));
			}
		}
	}
}
