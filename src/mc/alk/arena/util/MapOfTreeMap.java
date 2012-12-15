package mc.alk.arena.util;

import java.util.HashMap;
import java.util.TreeMap;

/**
 * Thread safe Map of hashes that I was using enough to warrant creating a class
 * @author alkarin
 *
 * @param <K>
 * @param <V>
 */
public class MapOfTreeMap<K,V, C extends Comparable<C>> extends HashMap<K,TreeMap<C,V>>{

	private static final long serialVersionUID = 1L;
	public void add(K k, V v, C c) {
		TreeMap<C,V> set = getOrMake(k);
//		set.add(v)
		set.put(c, v);
	}

	public V remove(K k, V v) {
		if (!containsKey(k))
			return null;
		TreeMap<C,V> set = get(k);
		V removed = set.remove(v);
		if (set.isEmpty()){
			synchronized(this){
				remove(k);
			}
		}
		return removed;
	}

	private TreeMap<C,V> getOrMake(K k) {
		TreeMap<C,V> set = get(k);
		if (set == null){
			set = new TreeMap<C,V>();
			synchronized(this){
				put(k, set);
			}
		}
		return set;
	}

	public TreeMap<C,V> getSafe(K k){
		TreeMap<C,V> set = get(k);
		if (set == null)
			return null;
		synchronized(this){
			return new TreeMap<C,V>(set);
		}
	}

}
