package mc.alk.arena.events.matches;

import mc.alk.arena.competition.match.Match;

public class MatchFindNeededTeamsEvent extends MatchEvent {
	int neededTeams;

	public MatchFindNeededTeamsEvent(Match match) {
		super(match);
	}

	public int getNeededTeams() {
		return neededTeams;
	}

	public void setNeededTeams(int neededTeams) {
		this.neededTeams = Math.max(this.neededTeams, neededTeams);
	}

}
