package mc.alk.arena.events.matches;

import java.util.List;

import mc.alk.arena.competition.match.Match;
import mc.alk.arena.objects.teams.Team;

public class MatchStartEvent extends MatchEvent {
	final List<Team> teams;

	public MatchStartEvent(Match match, List<Team> teams) {
		super(match);
		this.teams = teams;
	}

	public List<Team> getTeams() {
		return teams;
	}
}
