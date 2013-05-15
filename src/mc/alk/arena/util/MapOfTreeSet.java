package mc.alk.arena.util;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Set;
import java.util.TreeSet;

/**
 * Thread safe Map of hashes that I was using enough to warrant creating a class
 * @author alkarin
 *
 * @param <K>
 * @param <V>
 */
public class MapOfTreeSet<K,V> extends HashMap<K,TreeSet<V>>{
	private static final long serialVersionUID = 1L;
	Comparator<V> comparator = null;

	public MapOfTreeSet() {
		super();
	}

	public MapOfTreeSet(Comparator<V> comparator) {
		this.comparator = comparator;
	}

	public boolean add(K k, V v) {
		TreeSet<V> set = getOrMake(k);
		return set.add(v);
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

	private TreeSet<V> getOrMake(K k) {
		TreeSet<V> set = get(k);
		if (set == null){
			if (comparator != null)
				set = new TreeSet<V>(comparator);
			else
				set = new TreeSet<V>();
			synchronized(this){
				put(k, set);
			}
		}
		return set;
	}

	public TreeSet<V> getSafe(K k){
		TreeSet<V> set = get(k);
		if (set == null)
			return null;
		return new TreeSet<V>(set);
	}

}
