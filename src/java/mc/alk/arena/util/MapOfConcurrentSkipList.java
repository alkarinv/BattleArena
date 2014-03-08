package mc.alk.arena.util;

import java.util.Comparator;
import java.util.HashMap;
import java.util.concurrent.ConcurrentSkipListSet;

/**
 * Concurrent Map of List
 * @author alkarin
 *
 * @param <K>
 * @param <V>
 */
public class MapOfConcurrentSkipList<K,V> extends HashMap<K,ConcurrentSkipListSet<V>>{
    private static final long serialVersionUID = 1L;
    Comparator<V> comparator = null;

    public MapOfConcurrentSkipList() {
        super();
    }

    public MapOfConcurrentSkipList(Comparator<V> comparator) {
        this.comparator = comparator;
    }

    public boolean add(K k, V v) {
        ConcurrentSkipListSet<V> set = getOrMake(k);
        set.add(v);
        return set.add(v);
    }

    public boolean remove(K k, V v) {
        if (!containsKey(k))
            return false;
        ConcurrentSkipListSet<V> set = get(k);
        if (set.remove(v) && set.isEmpty()) {
            synchronized(this){
                remove(k);
            }
            return true;
        }
        return false;
    }

    private ConcurrentSkipListSet<V> getOrMake(K k) {
        ConcurrentSkipListSet<V> set = get(k);
        if (set == null){
            if (comparator != null){
                set = new ConcurrentSkipListSet<V>(comparator);
            } else {
                set = new ConcurrentSkipListSet<V>();
            }
            synchronized(this){
                put(k, set);
            }
        }
        return set;
    }
}
