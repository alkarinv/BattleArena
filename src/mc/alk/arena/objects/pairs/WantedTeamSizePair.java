package mc.alk.arena.objects.pairs;

import mc.alk.arena.objects.ArenaPlayer;
import mc.alk.arena.objects.MatchParams;
import mc.alk.arena.objects.exceptions.InvalidOptionException;
import mc.alk.arena.objects.teams.ArenaTeam;
import mc.alk.arena.util.MinMax;


public class WantedTeamSizePair {
	public int size;
	public boolean manuallySet = false;

	/**
	 * Check to see what size of team the player should join based on their team, preferences, and the MatchParams
	 * @param player
	 * @param t
	 * @param mp
	 * @param stringsize
	 * @return
	 * @throws InvalidOptionException
	 */
	public static WantedTeamSizePair getWantedTeamSize(ArenaPlayer player, ArenaTeam t, MatchParams mp, String stringsize) throws InvalidOptionException {
		/// Check to see if the user has specified a wanted team size
		MinMax mm = null;
		try{mm = MinMax.valueOf(stringsize);} catch (Exception e){}
		final int min = mp.getMinTeamSize();
		final int max = mp.getMaxTeamSize();
		WantedTeamSizePair result = new WantedTeamSizePair();
		if (mm != null){
			if (mm.min > max){ /// They want a team size that is greater than what is offered by this match type
				throw new InvalidOptionException("&cYou wanted to join with a team of &6" + mm.min+"&c players\n" +
						"&cBut this match type only supports up to &6"+max+"&c players per team");
			} else if (mm.min < t.size()){
				throw new InvalidOptionException("&cYou wanted to join an arena with less members than your team has!");
			}
			result.size = mm.min;
			result.manuallySet = true;
			return result;
		}
		if (t.size() > max){
			throw new InvalidOptionException("&cYour team has &6" + t.size()+"&c players\n" +
					"&cBut this match type only supports up to &6"+max+"&c players per team");
		}

		/// Are they joining a match that needs more people than their team has
		if (t.size() < min){
			result.size = min;
			return result;
		}

		/// They are good with their current team size
		result.size = t.size();
		return result;
	}
}
