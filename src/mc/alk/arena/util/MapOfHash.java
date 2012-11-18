package mc.alk.arena.util;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

/**
 * Thread safe Map of hashes that I was using enough to warrant creating a class
 * @author alkarin
 *
 * @param <K>
 * @param <V>
 */
public class MapOfHash<K,V> extends HashMap<K,HashSet<V>>{
	private static final long serialVersionUID = 1L;

	public void add(K k, V v) {
		HashSet<V> set = getOrMake(k);
		set.add(v);
	}

	public boolean remove(K k, V v) {
		if (!containsKey(k))
			return false;
		Set<V> set = get(k);
		boolean removed = set.remove(v);
		if (set.isEmpty()){
			synchronized(this){
				remove(k);
			}
		}
		return removed;
	}

	private HashSet<V> getOrMake(K k) {
		HashSet<V> set = get(k);
		if (set == null){
			set = new HashSet<V>();
			synchronized(this){
				put(k, set);
			}
		}
		return set;
	}

	public HashSet<V> getSafe(K k){
		HashSet<V> set = get(k);
		if (set == null)
			return null;
		return new HashSet<V>(set);
	}

}
