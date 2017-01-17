package no.jsosi;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;

/**
 * <p>
 * ...AKILDE:NRL;...GKILDE:NRL;bardun festet i bakken og \nopp til kabel over
 * dalen(spenn)
 */
class AttributeMap {

    private final Map<String, Object> m = new LinkedHashMap<>();
    private String lastKey;
    private Object lastValue;

    AttributeMap() {
    }

    AttributeMap(AttributeMap o) {
        m.putAll(o.m);
    }

    @SuppressWarnings("unchecked")
    public void add(String key, Object value) {

        if (value instanceof String) {
            value = Value.value(key, (String) value);
        }
        lastKey = key;
        lastValue = value;

        Object oldValue = m.get(key);
        if (oldValue == null) {
            m.put(key, value);
        } else if (oldValue instanceof Collection<?>) {
            Collection<Object> oldCollection = (Collection<Object>) oldValue;
            List<Object> c = new ArrayList<>(oldCollection.size() + 1);
            c.addAll(oldCollection);
            c.add(value);
            m.put(key, Collections.unmodifiableList(c));
        } else {
            List<Object> c = new ArrayList<>(2);
            c.add(oldValue);
            c.add(value);
            m.put(key, Collections.unmodifiableList(c));
        }
    }

    public void remove(String key) {
        m.remove(key);
    }

    public void computeSubValues() {
        Set<String> keysToRemove = new HashSet<>();
        Map<String, Object> extras = new LinkedHashMap<>();
        for (Map.Entry<String, Object> e : m.entrySet()) {
            String key = e.getKey();
            Object value = e.getValue();

            if (!(value instanceof String)) {
                continue;
            }

            String vs = (String) value;
            if (!(vs.contains(";") && vs.contains(":"))) {
                continue;
            }

            // ...AKILDE:NRL;...GKILDE:NRL;bardun festet i bakken og \nopp
            // til kabel over dalen(spenn)
            keysToRemove.add(key);
            StringTokenizer st = new StringTokenizer(value.toString(), ";");
            while (st.hasMoreTokens()) {
                String part = st.nextToken();
                int p = part.indexOf(':');
                if (part.startsWith(".") && p > 0) {
                    while (part.startsWith(".")) {
                        part = part.substring(1);
                        p--;
                    }
                    extras.put(part.substring(0, p), part.substring(p + 1));
                } else {
                    extras.put(key, part);
                }
            }
        }
        for (String key : keysToRemove) {
            m.remove(key);
        }
        m.putAll(extras);
    }

    public Object get(String key) {
        return m.get(key);
    }

    public Object getLastValueForKey(String key) {
        return key.equals(lastKey) ? lastValue : null;
    }

    public Set<String> keySet() {
        return Collections.unmodifiableSet(m.keySet());
    }

    public void clear() {
        m.clear();
        lastKey = null;
        lastValue = null;
    }

    @Override
    public String toString() {
        return "AttributeMap{" + m + "}";
    }

}
