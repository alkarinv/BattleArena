package mc.alk.arena.objects;

import mc.alk.arena.objects.teams.Team;
import mc.alk.arena.util.MessageUtil;
import mc.alk.arena.util.Util;
import mc.alk.arena.util.Util.MinMax;


public class WantedTeamSizeResult {
	public int size;
	public boolean manuallySet = false;

	/**
	 * Check to see what size of team the player should join based on their team, preferences, and the MatchParams
	 * @param player
	 * @param t
	 * @param mp
	 * @param string
	 * @return
	 */
	public static WantedTeamSizeResult getWantedTeamSize(ArenaPlayer player, Team t, MatchParams mp, String string) {
		/// Check to see if the user has specified a wanted team size
		MinMax mm = Util.getMinMax(string);
		final int min = mp.getMinTeamSize();
		final int max = mp.getMaxTeamSize();
		WantedTeamSizeResult result = new WantedTeamSizeResult();
		if (mm != null){
			if (mm.min > max){ /// They want a team size that is greater than what is offered by this match type
				MessageUtil.sendMessage(player, "&cYou wanted to join with a team of &6" + mm.min+"&c players");
				MessageUtil.sendMessage(player, "&cBut this match type only supports up to &6"+max+"&c players per team");
				return null;
			}
			result.size = mm.min;
			result.manuallySet = true;
			return result;
		}
		if (t.size() > max){
			MessageUtil.sendMessage(player, "&cYour team has &6" + t.size()+"&c players");
			MessageUtil.sendMessage(player, "&cBut this match type only supports up to &6"+max+"&c players per team");
			return null;
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
