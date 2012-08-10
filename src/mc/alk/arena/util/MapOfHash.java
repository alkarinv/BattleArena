package mc.alk.arena.util;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

public class MapOfHash<K,V> extends HashMap<K,HashSet<V>>{
	private static final long serialVersionUID = 1L;

	public void add(K k, V o) {
		HashSet<V> set = getOrMake(k);
		set.add(o);
	}

	public boolean remove(K p, V o) {
		if (!containsKey(p))
			return false;
		Set<V> set = get(p); 
		boolean removed = set.remove(o);
		if (set.isEmpty()){
			synchronized(this){
				remove(p);
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
