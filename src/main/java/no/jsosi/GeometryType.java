package no.jsosi;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;

public enum GeometryType {

    PUNKT, KURVE, SVERM;

    public Geometry createGeometry(GeometryFactory gf, Coordinate[] coordinates) {
        switch (this) {
        case PUNKT:
            return gf.createPoint(coordinates[0]);
        case KURVE:
            return gf.createLineString(coordinates);
        case SVERM:
            return gf.createMultiPoint(coordinates);
        }
        return null;
    }

}
