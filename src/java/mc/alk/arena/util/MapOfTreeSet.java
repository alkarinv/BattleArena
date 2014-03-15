package mc.alk.arena.util;

import java.lang.reflect.Array;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Set;
import java.util.TreeSet;

/**
 * Concurrent Map of List
 * @author alkarin
 *
 * @param <K>
 * @param <V>
 */
public class MapOfTreeSet<K,V> extends HashMap<K,TreeSet<V>>{
    private static final long serialVersionUID = 1L;
    Comparator<V> comparator = null;
    final Class<V> vClass;

    public MapOfTreeSet(Class<V> valueParameterClass) {
        super();
        vClass = valueParameterClass;
    }

    public MapOfTreeSet(Class<V> valueParameterClass, Comparator<V> comparator) {
        this.comparator = comparator;
        vClass = valueParameterClass;
    }

    public boolean add(K k, V v) {
        synchronized(this){
            TreeSet<V> set = get(k);
            if (set == null){
                if (comparator != null)
                    set = new TreeSet<V>(comparator);
                else
                    set = new TreeSet<V>();
                put(k, set);
            }
            return set.add(v);
        }
    }

    public boolean remove(K k, V v) {
        if (!containsKey(k))
            return false;
        Set<V> set = get(k);
        if (set.remove(v) && set.isEmpty()){
            remove(k);
            return true;
        }
        return false;
    }

    public V[] getSafe(K k){
        TreeSet<V> set = get(k);
        synchronized (this){
            if (set == null)
                return null;
            //noinspection unchecked
            return  set.toArray((V[]) Array.newInstance(vClass, set.size()));
        }
    }
}
