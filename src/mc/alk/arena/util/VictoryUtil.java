package mc.alk.arena.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import mc.alk.arena.competition.match.Match;
import mc.alk.arena.objects.teams.Team;

public class VictoryUtil {
	static final Random rand = new Random();
	
	public static Team highestKills(Match match){
		List<Team> teams = match.getTeams();
		Team highest = null;
		List<Team> victors = getMostKills(teams);
		if (victors.size() > 1){ /// try to tie break by number of deaths
			victors = getLeastDeaths(victors);			
		}

		/// We still have a tie.. time to go random
		if (victors.size() == 1){
			highest = victors.get(rand.nextInt(victors.size()));
		} else if (victors.size() > 1){
			highest = victors.get(rand.nextInt(victors.size()));
		}
		if (highest == null){ /// no one has killed, or died, choose someone at random
			highest = teams.get(rand.nextInt(teams.size()));}
		return highest;
	}

	public static List<Team> getMostKills(List<Team> teams){
		int highest = Integer.MIN_VALUE;
		List<Team> victors = new ArrayList<Team>();
		for (Team t: teams){
			int nkills = t.getNKills();
			if (nkills == highest){ /// we have some sort of tie
				victors.add(t);}
			if (nkills > highest){
				victors.clear();
				highest = nkills;
				victors.add(t);
			}
		}
		return victors;
	}

	public static List<Team> getLeastDeaths(List<Team> teams){
		int lowest = Integer.MAX_VALUE;
		List<Team> result = new ArrayList<Team>();
		for (Team t: teams){
			final int ndeaths = t.getNDeaths();
			if (ndeaths == lowest){ /// we have some sort of tie
				result.add(t);}
			if (ndeaths < lowest){
				result.clear();
				lowest = ndeaths;
				result.add(t);
			}
		}
		return result;
	}

}
