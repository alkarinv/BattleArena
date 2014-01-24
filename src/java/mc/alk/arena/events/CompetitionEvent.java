package mc.alk.arena.events;

import mc.alk.arena.competition.Competition;

public class CompetitionEvent extends BAEvent {
	protected Competition competition;

	public void setCompetition(Competition competition){
		this.competition = competition;
	}

	public Competition getCompetition(){
		return competition;
	}
}
