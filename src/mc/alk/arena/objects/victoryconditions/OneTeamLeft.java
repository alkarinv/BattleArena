package mc.alk.arena.objects.victoryconditions;

import mc.alk.arena.competition.match.Match;
import mc.alk.arena.events.PlayerLeftEvent;
import mc.alk.arena.events.matches.MatchFindNeededTeamsEvent;
import mc.alk.arena.objects.ArenaPlayer;
import mc.alk.arena.objects.events.MatchEventHandler;
import mc.alk.arena.objects.teams.Team;

public class OneTeamLeft extends VictoryCondition{
	public OneTeamLeft(Match match) {
		super(match);
	}

	@MatchEventHandler
	public void onNeededTeams(MatchFindNeededTeamsEvent event) {
		event.setNeededTeams(2);
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
		if (!team.isDead()){ /// one team isn't even dead
			return;}

		/// Killing this player killed the team
		Team leftAlive = null;
		/// Iterate over the players to see if we have one team left standing

		for (Team t: match.getTeams()){
			if (t.isDead())
				continue;
			if (leftAlive != null) /// obviously more than one team is still in the match
				return;
			leftAlive = t;
		}
		if (leftAlive == null)
			match.setDraw();
		else /// One team left alive = victory
			match.setVictor(leftAlive);
	}
}
