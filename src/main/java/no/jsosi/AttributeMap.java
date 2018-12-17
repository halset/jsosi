package no.jsosi;

import java.util.ArrayList;
import java.util.Collections;
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
class AttributeMap implements Cloneable {

    private final TreeElement root;
    private TreeElement currentTreeElement;
    private int pathDepth = 0;

    AttributeMap() {
        root = new TreeElement();
        currentTreeElement = root;
    }

    AttributeMap(AttributeMap o) {
        this.root = (TreeElement) o.root.clone();
        currentTreeElement = root;
    }

    public void addPathElement(String pathElement) {
        currentTreeElement = currentTreeElement.addChild(pathElement);
        pathDepth++;
    }

    public void removeLastPathElement() {
        currentTreeElement = currentTreeElement.parent;
        pathDepth--;
    }

    public int pathDepth() {
        return pathDepth;
    }

    public void addValue(Object value) {
        currentTreeElement.addValue(value);
    }

    public void remove(String key) {
        root.remove(key);
    }

    public void computeSubValues() {
        root.computeSubValues();
    }

    public Object get(String key) {
        Object v = root.get(key);
        if (v instanceof TreeElement) {
            TreeElement te = (TreeElement) v;
            return te.toExternal();
        }
        if (v instanceof List<?>) {
            List<Object> vo = new ArrayList<>((List<?>) v);
            for (int i = 0; i < vo.size(); i++) {
                Object voo = vo.get(i);
                if (voo instanceof TreeElement) {
                    vo.set(i, ((TreeElement) voo).toExternal());
                }
            }
            return Collections.unmodifiableList(vo);
        }
        return v;
    }

    public Object getLastValue() {
        return currentTreeElement.getLastValue();
    }

    public void removeLastValue() {
        currentTreeElement.removeLastValue();
    }

    public Set<String> keySet() {
        return Collections.unmodifiableSet(root.children.keySet());
    }
    
    @SuppressWarnings("unchecked")
    public Map<String, Object> toExternal() {
        Object o = root.toExternal();
        if (o == null) {
            return Collections.emptyMap();
        }
        if (o instanceof Map) {
            return (Map<String, Object>)o;
        }
        throw new IllegalStateException("unknown external " + o);
    }

    public void clear() {
        root.clear();
        currentTreeElement = root;
        pathDepth = 0;
    }

    @Override
    public Object clone() {
        return new AttributeMap(this);
    }

    @Override
    public String toString() {
        return "AttributeMap{" + root + "}";
    }

    private static final class TreeElement implements Cloneable {

        private TreeElement parent;
        private List<Object> values;
        private LazyMultimap<String, TreeElement> children;

        public void remove(String key) {
            if (children != null) {
                children.remove(key);
            }
        }

        public Object get(Object key) {
            if (key instanceof Integer && values != null) {
                Integer index = (Integer) key;
                if (index < 0 || index >= values.size()) {
                    return null;
                }
                return values.get(index.intValue());
            }

            if (key instanceof String && children != null) {
                return children.get(key);
            }

            return null;
        }

        public TreeElement addChild(String key) {
            if (children == null) {
                children = new LazyMultimap<>();
            }

            TreeElement child = new TreeElement();
            child.parent = this;
            children.add(key, child);
            return child;
        }

        public void addValue(Object newValue) {
            if (values == null) {
                values = new ArrayList<>(2);
            }
            values.add(newValue);
        }

        public Object getLastValue() {
            if (values == null) {
                return null;
            }
            return values.get(values.size() - 1);
        }

        public void removeLastValue() {
            if (values == null) {
                return;
            }
            values.remove(values.size() - 1);
        }

        public Object toExternal() {
            if ((values == null || values.isEmpty()) && (children == null || children.isEmpty())) {
                return null;
            }

            if (children != null && !children.isEmpty()) {
                Map<String, Object> r = new LinkedHashMap<>();
                for (String key : children.keySet()) {
                    List<Object> vs = new ArrayList<>();
                    for (TreeElement te : children.getAll(key)) {
                        Object v = te.toExternal();
                        if (v != null) {
                            vs.add(v);
                        }
                    }
                    if (vs.size() == 1) {
                        r.put(key, vs.get(0));
                    } else if (vs.size() > 1) {
                        r.put(key, vs);
                    }
                }

                if (values != null) {
                    if (values.size() == 1) {
                        r.put("", values.get(0));
                    } else if (values.size() > 1) {
                        r.put("", values);
                    }
                }

                return r;
            }

            if (values != null && !values.isEmpty()) {
                if (values.size() == 1) {
                    return values.get(0);
                }
                return values;
            }

            return null;
        }

        public void computeSubValues() {

            if (values != null) {
                for (Object value : new ArrayList<>(values)) {
                    if (!(value instanceof String)) {
                        continue;
                    }

                    String vs = (String) value;
                    if (!(vs.contains(";") && vs.contains(":"))) {
                        continue;
                    }

                    // ...AKILDE:NRL;...GKILDE:NRL;bardun festet i bakken og
                    // \nopp //
                    // til
                    // kabel over dalen(spenn)
                    // TreeElement submap = new TreeElement();
                    StringTokenizer st = new StringTokenizer(value.toString(), ";");
                    while (st.hasMoreTokens()) {
                        String part = st.nextToken();
                        int p = part.indexOf(':');
                        if (part.startsWith(".") && p > 0) {
                            while (part.startsWith(".")) {
                                part = part.substring(1);
                                p--;
                            }
                            addChild(part.substring(0, p)).addValue(part.substring(p + 1));
                        } else {
                            addValue(part);
                        }
                    }

                    values.remove(value);
                }
            }

            if (children != null) {
                for (String key : children.keySet()) {
                    for (TreeElement te : children.getAll(key)) {
                        te.computeSubValues();
                    }

                }
            }
        }

        public void clear() {
            values = null;
            children = null;
        }

        @SuppressWarnings("unchecked")
        @Override
        public Object clone() {
            TreeElement c = new TreeElement();
            if (values != null) {
                c.values = new ArrayList<>(values);
            }
            if (children != null) {
                c.children = (LazyMultimap<String, TreeElement>) children.clone();
            }
            c.fixParent();
            return c;
        }

        private void fixParent() {
            if (children != null) {
                for (String key : children.keySet()) {
                    for (TreeElement te : children.getAll(key)) {
                        te.parent = this;
                        te.fixParent();
                    }
                }
            }
        }

        @Override
        public String toString() {
            return "TreeElement{values=" + values + ",children=" + children + "}";
        }

    }

}
