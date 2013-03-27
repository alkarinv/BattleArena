package mc.alk.arena.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Random;
import java.util.TreeMap;

import mc.alk.arena.competition.match.Match;
import mc.alk.arena.objects.teams.Team;

public class VictoryUtil {
	static final Random rand = new Random();


	public static List<Team> getLeaderByHighestKills(Match match){
		List<Team> teams = match.getTeams();
		List<Team> victors = getLeaderByHighestKills(teams);
		if (victors.size() > 1){ /// try to tie break by number of deaths
			victors = getLeaderByLeastDeaths(victors);
		}
		return victors;
	}

	public static List<Team> getLeaderByHighestKills(List<Team> teams){
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

	public static List<Team> getLeaderByLeastDeaths(List<Team> teams){
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

	public static List<Team> getRanksByHighestKills(List<Team> teams) {
		ArrayList<Team> ts = new ArrayList<Team>(teams);
		Collections.sort(ts, new Comparator<Team>(){
			@Override
			public int compare(Team arg0, Team arg1) {
				Integer k1 = arg0.getNKills();
				Integer k2 = arg1.getNKills();
				int c = -k1.compareTo(k2);
				return c != 0? c : new Integer(arg0.getNDeaths()).compareTo(arg1.getNDeaths());
			}
		});
		return ts;
	}

	public static TreeMap<Integer, Collection<Team>> getRankingByHighestKills(List<Team> teams) {
		TreeMap<Integer,Collection<Team>> map = new TreeMap<Integer,Collection<Team>>(Collections.reverseOrder());
		for (Team t: teams){
			Collection<Team> col = map.get(t.getNKills());
			if (col == null){
				col = new ArrayList<Team>();
				map.put(t.getNKills(), col);
			}
			col.add(t);
		}
		return map;
	}

}
