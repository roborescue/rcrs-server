package rescuecore2.misc.collections;

import java.util.Map;
import java.util.HashMap;
import java.util.Set;
import java.util.Collection;

/**
   A LazyMap is a Map that generates default values for keys that do not yet have a value. This class delegates to a real Map implementation for all methods with the exception of {@link #get(Object)}. If the downstream get returns a value then that value is returned to the caller. If the downstream get returns null then the {@link #createValue()} method will be called and the result of that is added to the downstream map and returned.
   @param <K> The key type.
   @param <V> The value type.
 */
public abstract class LazyMap<K, V> implements Map<K, V> {
    private Map<K, V> downstream;

    /**
       Construct a LazyMap that delegates to a new HashMap.
     */
    public LazyMap() {
        this(new HashMap<K, V>());
    }

    /**
       Construct a LazyMap that delegates to a given map.
       @param downstream The delegate map.
     */
    public LazyMap(Map<K, V> downstream) {
        this.downstream = downstream;
    }

    /**
       Construct a new value object for a key that does not yet have a value.
       @return A new value object.
    */
    public abstract V createValue();

    @Override
    public void clear() {
        downstream.clear();
    }

    @Override
    public boolean containsKey(Object key) {
        return downstream.containsKey(key);
    }

    @Override
    public boolean containsValue(Object value) {
        return downstream.containsValue(value);
    }

    @Override
    public Set<Map.Entry<K, V>> entrySet() {
        return downstream.entrySet();
    }

    @Override
    public boolean equals(Object o) {
        return (o instanceof LazyMap) && (((LazyMap)o).downstream.equals(this.downstream));
    }

    @Override
    @SuppressWarnings("unchecked")
    public V get(Object key) {
        V result = downstream.get(key);
        if (result == null) {
            try {
                result = createValue();
                downstream.put((K)key, result);
            }
            catch (ClassCastException e) {
                result = null;
            }
        }
        return result;
    }

    @Override
    public int hashCode() {
        return downstream.hashCode();
    }

    @Override
    public boolean isEmpty() {
        return downstream.isEmpty();
    }

    @Override
    public Set<K> keySet() {
        return downstream.keySet();
    }

    @Override
    public V put(K key, V value) {
        return downstream.put(key, value);
    }

    @Override
    public void putAll(Map<? extends K, ? extends V> m) {
        downstream.putAll(m);
    }

    @Override
    public V remove(Object key) {
        return downstream.remove(key);
    }

    @Override
    public int size() {
        return downstream.size();
    }

    @Override
    public Collection<V> values() {
        return downstream.values();
    }
}
