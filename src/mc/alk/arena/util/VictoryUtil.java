package mc.alk.arena.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Random;
import java.util.TreeMap;

import mc.alk.arena.competition.match.Match;
import mc.alk.arena.objects.teams.ArenaTeam;

public class VictoryUtil {
	static final Random rand = new Random();


	public static List<ArenaTeam> getLeaderByHighestKills(Match match){
		List<ArenaTeam> teams = match.getTeams();
		List<ArenaTeam> victors = getLeaderByHighestKills(teams);
		if (victors.size() > 1){ /// try to tie break by number of deaths
			victors = getLeaderByLeastDeaths(victors);
		}
		return victors;
	}

	public static List<ArenaTeam> getLeaderByHighestKills(List<ArenaTeam> teams){
		int highest = Integer.MIN_VALUE;
		List<ArenaTeam> victors = new ArrayList<ArenaTeam>();
		for (ArenaTeam t: teams){
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

	public static List<ArenaTeam> getLeaderByLeastDeaths(List<ArenaTeam> teams){
		int lowest = Integer.MAX_VALUE;
		List<ArenaTeam> result = new ArrayList<ArenaTeam>();
		for (ArenaTeam t: teams){
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

	public static List<ArenaTeam> getRanksByHighestKills(List<ArenaTeam> teams) {
		ArrayList<ArenaTeam> ts = new ArrayList<ArenaTeam>(teams);
		Collections.sort(ts, new Comparator<ArenaTeam>(){
			@Override
			public int compare(ArenaTeam arg0, ArenaTeam arg1) {
				Integer k1 = arg0.getNKills();
				Integer k2 = arg1.getNKills();
				int c = k1.compareTo(k2);
				return c != 0? -c : new Integer(arg0.getNDeaths()).compareTo(arg1.getNDeaths());
			}
		});
		return ts;
	}

	public static TreeMap<Integer, Collection<ArenaTeam>> getRankingByHighestKills(List<ArenaTeam> teams) {
		TreeMap<Integer,Collection<ArenaTeam>> map = new TreeMap<Integer,Collection<ArenaTeam>>(Collections.reverseOrder());
		for (ArenaTeam t: teams){
			Collection<ArenaTeam> col = map.get(t.getNKills());
			if (col == null){
				col = new ArrayList<ArenaTeam>();
				map.put(t.getNKills(), col);
			}
			col.add(t);
		}
		return map;
	}

}
