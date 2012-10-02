package mc.alk.arena.events.matches;

import mc.alk.arena.competition.match.Match;
import mc.alk.arena.events.BAEvent;

public class MatchFindNeededTeamsEvent extends BAEvent {
	final Match match;
	int neededTeams;
	
	public MatchFindNeededTeamsEvent(Match match) {
		this.match = match;
	}
	
	public Match getMatch() {
		return match;
	}

	public int getNeededTeams() {
		return neededTeams;
	}

	public void setNeededTeams(int neededTeams) {
		this.neededTeams = Math.max(this.neededTeams, neededTeams);
	}

}
