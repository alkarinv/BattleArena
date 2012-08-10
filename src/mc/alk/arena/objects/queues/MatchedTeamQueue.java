package mc.alk.arena.objects.queues;

import java.util.LinkedList;

import mc.alk.arena.objects.MatchParams;
import mc.alk.arena.objects.tournament.Matchup;


public class MatchedTeamQueue extends LinkedList<Matchup>{
	private static final long serialVersionUID = 1L;
	MatchParams q = null;
	public MatchedTeamQueue(MatchParams q) {
		this.q = new MatchParams(q);
	}

	public MatchParams getMatchParams() {
		return q;
	}

}
