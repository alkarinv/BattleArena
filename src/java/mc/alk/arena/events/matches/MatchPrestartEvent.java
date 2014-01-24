package mc.alk.arena.events.matches;

import java.util.List;

import mc.alk.arena.competition.match.Match;
import mc.alk.arena.objects.teams.ArenaTeam;

public class MatchPrestartEvent extends MatchEvent {
	final List<ArenaTeam> teams;

	public MatchPrestartEvent(Match match, List<ArenaTeam> teams) {
		super(match);
		this.teams = teams;
	}

	public List<ArenaTeam> getTeams() {
		return teams;
	}
}
