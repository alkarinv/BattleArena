package mc.alk.arena.competition.events.tournament;

import mc.alk.arena.objects.Matchup;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;


class Round {
	int round;
	List<Matchup> matchups = new CopyOnWriteArrayList<Matchup>();
	public Round(int round) {
		this.round = round;
	}
	public void addMatchup(Matchup m){
		matchups.add(m);
	}
	public List<Matchup> getMatchups(){
		return matchups;
	}
	public List<Matchup> getCompleteMatchups() {
		List<Matchup> completed = new ArrayList<Matchup>();
		for (Matchup m : matchups){
			if (m.getResult().isFinished())
				completed.add(m);
		}
		return completed;
	}
}
