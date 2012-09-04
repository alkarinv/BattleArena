package mc.alk.arena.objects.victoryconditions;

import java.util.List;
import java.util.Random;

import mc.alk.arena.match.Match;
import mc.alk.arena.objects.ArenaPlayer;
import mc.alk.arena.objects.teams.Team;
import mc.alk.arena.util.VictoryUtil;

public class HighestKills extends VictoryCondition{
	public HighestKills(Match match) {
		super(match);
	}

	static Random rand = new Random(); /// Our randomizer

	public void timeExpired() {
		final Team h = VictoryUtil.highestKills(match);
		match.setVictor(h);		
	}

	/**
	 * Return the current leader, 
	 * @return
	 */
	@Override
	public Team currentLeader() {
		return VictoryUtil.highestKills(match);
	}
	public List<List<Team>> rankings() {
		return null;
	}

	public void timeInterval(int remaining) {}
	
	@Override
	public boolean hasTimeVictory() {
		return true;
	}

	@Override
	public void playerLeft(ArenaPlayer p) {
		/// Get to do nothing, 
	}

}
