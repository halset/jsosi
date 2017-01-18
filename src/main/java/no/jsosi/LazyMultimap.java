package no.jsosi;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

class LazyMultimap<K, V> implements Cloneable {

    private final Map<K, Object> m = new LinkedHashMap<>();

    @SuppressWarnings("unchecked")
    public void add(K key, V value) {
        Object oldValue = m.get(key);
        if (oldValue == null) {
            m.put(key, value);
        } else if (oldValue instanceof MyList<?>) {
            MyList<Object> oldCollection = (MyList<Object>) oldValue;
            oldCollection.add(value);
        } else {
            MyList<V> c = new MyList<>();
            c.add((V) oldValue);
            c.add(value);
            m.put(key, c);
        }
    }

    @SuppressWarnings("unchecked")
    public List<V> getAll(Object key) {
        Object v = m.get(key);
        if (v == null) {
            return Collections.emptyList();
        } else if (v instanceof MyList) {
            return (MyList<V>) v;
        } else {
            return Collections.singletonList((V) v);
        }
    }

    public Set<K> keySet() {
        return m.keySet();
    }

    public Object get(Object key) {
        return m.get(key);
    }

    public void putAll(Map<K, V> extras) {
        m.putAll(extras);
    }

    public void remove(K key) {
        m.remove(key);
    }

    @Override
    public Object clone() {
        LazyMultimap<K, V> c = new LazyMultimap<>();
        c.m.putAll(m);
        return c;
    }

    @Override
    public String toString() {
        return m.toString();
    }

    public boolean isEmpty() {
        return m.isEmpty();
    }

    private static final class MyList<V> extends ArrayList<V> {

        private static final long serialVersionUID = 1L;

    }

}
