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
public class MapOfSet<K,V> extends HashMap<K,Set<V>>{
	private static final long serialVersionUID = 1L;
	@SuppressWarnings("rawtypes")
	Class<? extends Set> instanstiationClass = HashSet.class;

	public MapOfSet(){
		super();
	}
	public MapOfSet(Class<? extends Set<V>> setClass){
		this.instanstiationClass = setClass;
	}

	public void add(K k, V v) {
		Set<V> set = getOrMake(k);
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

	@SuppressWarnings("unchecked")
	private Set<V> getOrMake(K k) {
		Set<V> set = get(k);
		if (set == null){
			try {
				set = instanstiationClass.newInstance();
			} catch (Exception e) {
				e.printStackTrace();
				return null;
			}
			synchronized(this){
				put(k, set);
			}
		}
		return set;
	}

	public Set<V> getSafer(K k){
		return !containsKey(k) ? null : new HashSet<V>(get(k));
	}

}
