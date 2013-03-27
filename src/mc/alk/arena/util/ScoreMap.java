package mc.alk.arena.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;

public class ScoreMap<K> extends ConcurrentHashMap<K,Integer> {
	private static final long serialVersionUID = 1L;

	public int addPoints(K key, int points){
		Integer p = get(key);
		if (p == null){
			put(key, points);
			return points;
		}
		p += points;
		put(key, p);
		return p;
	}

	public int subtractPoints(K key, int points){
		return addPoints(key,-points);
	}

	public int getPoints(K key){
		Integer p = get(key);
		return p == null ? 0 : p;
	}

	public void reset() {
		this.clear();
	}

	public List<K> getLeaders() {
		int highest = Integer.MIN_VALUE;
		List<K> victors = new ArrayList<K>();
		for (K t: keySet()){
			int points = getPoints(t);
			if (points == highest){ /// we have some sort of tie
				victors.add(t);}
			if (points > highest){
				victors.clear();
				highest = points;
				victors.add(t);
			}
		}
		return victors;
	}

	public synchronized TreeMap<Integer,Collection<K>> getRankings() {
		TreeMap<Integer,Collection<K>> map = new TreeMap<Integer,Collection<K>>(Collections.reverseOrder());
		for (Entry<K,Integer> entry : this.entrySet()){
			Collection<K> col = map.get(entry.getValue());
			if (col == null){
				col = new ArrayList<K>();
				map.put(entry.getValue(), col);
			}
			col.add(entry.getKey());
		}
		return map;
	}

	@Deprecated
	public synchronized List<K> getOldRankings() {
		ArrayList<K> ts = new ArrayList<K>(keySet());
		Collections.sort(ts, new Comparator<K>(){
			@Override
			public int compare(K arg0, K arg1) {
				Integer k1 = getPoints(arg0);
				Integer k2 = getPoints(arg1);
				return k1.compareTo(k2);
			}
		});
		return ts;
	}
}
