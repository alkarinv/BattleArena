package mc.alk.arena.objects.tournament;

import java.util.ArrayList;
import java.util.List;


public class Round {
	int round;
	ArrayList<Matchup> matchups = new ArrayList<Matchup>();
	public Round(int round) {
		this.round = round;
	}
	public void addMatchup(Matchup m){
		matchups.add(m);
	}
	public List<Matchup> getMatchups(){
		return matchups;
	}
}
