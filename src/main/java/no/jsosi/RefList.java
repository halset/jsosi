package no.jsosi;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.StringTokenizer;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.LinearRing;

public class RefList {

    private final List<List<Ref>> list = new ArrayList<List<Ref>>();
    private List<Ref> current = new ArrayList<Ref>();

    public RefList() {
        list.add(current);
    }

    public void startHole() {
        current = new ArrayList<Ref>();
        list.add(current);
    }

    public void add(String refList) {
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

    public boolean isEmpty() {
        return list.size() <= 1 && current.isEmpty();
    }

    public int getNumOfHoles() {
        return list.size() - 1;
    }

    public List<Ref> getExteriour() {
        return Collections.unmodifiableList(list.get(0));
    }

    public List<Ref> getHole(int n) {
        return Collections.unmodifiableList(list.get(n + 1));
    }

    Geometry createGeometry(SosiReader reader) {
        try {
            LinearRing shell = createRing(reader, list.get(0));
            List<LinearRing> holes = new ArrayList<LinearRing>();
            for (int i = 1; i < list.size(); i++) {
                holes.add(createRing(reader, list.get(i)));
            }
            return reader.getGeometryFactory().createPolygon(shell,
                    holes.toArray(new LinearRing[holes.size()]));
        } catch (RuntimeException e) {
            throw new RuntimeException("Could not create geometry for " + toString(), e);
        }
    }

    private static LinearRing createRing(SosiReader reader, List<Ref> refs) {
        List<Coordinate> cs = new ArrayList<Coordinate>();
        for (Ref ref : refs) {
            cs.addAll(ref.getCoordinates(reader));
        }
        return reader.getGeometryFactory().createLinearRing(cs.toArray(new Coordinate[cs.size()]));
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
