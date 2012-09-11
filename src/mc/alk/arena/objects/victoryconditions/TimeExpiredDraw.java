package mc.alk.arena.objects.victoryconditions;

import mc.alk.arena.competition.match.Match;
import mc.alk.arena.objects.ArenaPlayer;

public class TimeExpiredDraw extends VictoryCondition {

	public TimeExpiredDraw(Match match) {
		super(match);
	}

	@Override
	public boolean hasTimeVictory() {
		return true;
	}

	@Override
	public void playerLeft(ArenaPlayer p) {
		/// Dont care
	}

}
