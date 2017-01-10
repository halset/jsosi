package no.jsosi;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.StringTokenizer;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.LinearRing;

class RefList {

    private final List<List<Ref>> list = new ArrayList<List<Ref>>();
    private List<Ref> current = new ArrayList<Ref>();

    RefList() {
        list.add(current);
    }

    private void startHole() {
        current = new ArrayList<Ref>();
        list.add(current);
    }

    void add(String refList) {
        StringTokenizer st = new StringTokenizer(refList);
        while (st.hasMoreTokens()) {
            String token = st.nextToken();
            if (token.startsWith("(")) {
                startHole();
                token = token.substring(1);
            }
            if (token.endsWith(")")) {
                token = token.substring(0, token.length() - 1);
            }
            current.add(Ref.create(token));
        }
    }

    boolean isEmpty() {
        return list.size() <= 1 && current.isEmpty();
    }

    int getNumOfHoles() {
        return list.size() - 1;
    }

    List<Ref> getExteriour() {
        return Collections.unmodifiableList(list.get(0));
    }

    List<Ref> getHole(int n) {
        return Collections.unmodifiableList(list.get(n + 1));
    }

    Set<Integer> getRefs() {
        Set<Integer> refs = new HashSet<Integer>();
        for (List<Ref> refList : list) {
            for (Ref ref : refList) {
                refs.add(ref.getId());
            }
        }
        return Collections.unmodifiableSet(refs);
    }

    Geometry createGeometry(SosiReader reader) throws IOException {
        try {
            Geometry outer = createRing(reader, list.get(0));

            // handle a specific error condition
            if (!(outer instanceof LinearRing) && list.size() == 1) {
                return outer;
            }

            LinearRing shell = (LinearRing) outer;
            List<LinearRing> holes = new ArrayList<LinearRing>();
            for (int i = 1; i < list.size(); i++) {
                holes.add((LinearRing) createRing(reader, list.get(i)));
            }
            return reader.getGeometryFactory().createPolygon(shell, holes.toArray(new LinearRing[holes.size()]));
        } catch (RuntimeException e) {
            throw new RuntimeException("Could not create geometry for " + toString(), e);
        }
    }

    private static Geometry createRing(SosiReader reader, List<Ref> refs) throws IOException {
        List<Coordinate> cs = new ArrayList<Coordinate>();
        for (Ref ref : refs) {
            cs.addAll(ref.getCoordinates(reader));
        }

        // handle a specific error condition
        if (cs.size() == 2 && cs.get(0).equals2D(cs.get(1))) {
            return reader.getGeometryFactory().createPoint(cs.get(0));
        }

        // hack a specific error condition
        if (cs.size() == 3 && cs.get(0).equals2D(cs.get(2))) {
        	cs.add(2, cs.get(1));
        }
        
        try {
            return reader.getGeometryFactory().createLinearRing(cs.toArray(new Coordinate[cs.size()]));
        } catch (IllegalArgumentException e) {
            if (cs.size() > 2 && !cs.get(0).equals2D(cs.get(cs.size() - 1))) {
                // hack ring to close
                cs.add(cs.get(0));
                return reader.getGeometryFactory().createLinearRing(cs.toArray(new Coordinate[cs.size()]));
            }
            throw e;
        }
    }

    @Override
    public String toString() {
        StringBuilder s = new StringBuilder();
        for (int i = 0; i < list.size(); i++) {
            if (i > 0) {
                s.append(" (");
            }
            for (Iterator<Ref> it = list.get(i).iterator(); it.hasNext();) {
                s.append(it.next().toString());
                if (it.hasNext()) {
                    s.append(' ');
                }
            }
            if (i > 0) {
                s.append(')');
            }
        }
        return s.toString();
    }

}
