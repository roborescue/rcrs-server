package rescuecore2.misc.collections;

import java.util.Map;
import java.util.HashMap;
import java.util.Set;
import java.util.HashSet;
import java.util.Collection;

/**
   A DelegatingMap is a HashMap that delegates to another map for keys that do not have a value.
   @param <K> The key type.
   @param <V> The value type.
 */
public abstract class DelegatingMap<K, V> extends HashMap<K, V> {
    private Map<K, V> downstream;

    /**
       Construct a DelegatingMap that delegates to a given map.
       @param downstream The delegate map.
     */
    public DelegatingMap(Map<K, V> downstream) {
        this.downstream = downstream;
    }

    @Override
    public boolean containsKey(Object key) {
        return super.containsKey(key) || downstream.containsKey(key);
    }

    @Override
    public boolean containsValue(Object value) {
        return super.containsValue(value) || downstream.containsValue(value);
    }

    @Override
    public Set<Map.Entry<K, V>> entrySet() {
        Set<Map.Entry<K, V>> result = new HashSet<Map.Entry<K, V>>();
        result.addAll(super.entrySet());
        for (Map.Entry<K, V> next : downstream.entrySet()) {
            if (!super.containsKey(next.getKey())) {
                result.add(next);
            }
        }
        return result;
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof DelegatingMap) {
            return super.equals(o) && this.downstream.equals(((DelegatingMap)o).downstream);
        }
        return false;
    }

    @Override
    @SuppressWarnings("unchecked")
    public V get(Object key) {
        V result = super.get(key);
        if (result == null) {
            return downstream.get(key);
        }
        return result;
    }

    @Override
    public int hashCode() {
        return super.hashCode() ^ downstream.hashCode();
    }

    @Override
    public boolean isEmpty() {
        return super.isEmpty() && downstream.isEmpty();
    }

    @Override
    public Set<K> keySet() {
        Set<K> result = new HashSet<K>();
        result.addAll(super.keySet());
        result.addAll(downstream.keySet());
        return result;
    }

    @Override
    public int size() {
        return keySet().size();
    }

    @Override
    public Collection<V> values() {
        Collection<V> result = new HashSet<V>();
        result.addAll(super.values());
        result.addAll(downstream.values());
        return result;
    }
}
