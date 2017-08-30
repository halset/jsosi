package no.jsosi;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.vividsolutions.jts.geom.Coordinate;

public class Ref {

    private final int id;
    private final boolean forward;

    private Ref(int id, boolean forward) {
        this.id = id;
        this.forward = forward;
    }

    public static Ref create(String value) {
        boolean forward = true;
        for (int i = 0; i < value.length(); i++) {
            char c = value.charAt(i);
            switch (c) {
            case ':':
            case ' ':
                break;
            case '-':
                forward = false;
                break;
            default:
                try {
                    return new Ref(Integer.parseInt(value.substring(i).trim()), forward);
                } catch (NumberFormatException e) {
                    return null;
                }
            }
        }
        return null;
    }

    public int getId() {
        return id;
    }

    public boolean isForward() {
        return forward;
    }
    
    List<Coordinate> getCoordinates(SosiReader reader) throws IOException {
        List<Coordinate> coords = reader.getKurve(getId());
        if (isForward()) {
            return coords;
        } else {
            List<Coordinate> cs = new ArrayList<Coordinate>(coords);
            Collections.reverse(cs);
            return Collections.unmodifiableList(cs);
        }
    }

    @Override
    public String toString() {
        return ":" + (isForward() ? "" : "-") + id;
    }

}
