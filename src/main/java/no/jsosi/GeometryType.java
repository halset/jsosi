package no.jsosi;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;

public enum GeometryType {

    PUNKT, KURVE, SVERM, FLATE, TEKST, OBJEKT, BUEP;

    public Geometry createGeometry(GeometryFactory gf, Coordinate[] coordinates) {
        switch (this) {
        case PUNKT:
            if (coordinates.length == 0) {
                return gf.createGeometryCollection(new Geometry[0]);
            }
            return gf.createPoint(coordinates[0]);
        case KURVE:
            return gf.createLineString(coordinates);
        case SVERM:
            return gf.createMultiPoint(coordinates);
        case FLATE:
            return gf.createPolygon(coordinates);
        case TEKST:
            if (coordinates.length == 0) {
                return gf.createGeometryCollection(new Geometry[0]);
            }
            return gf.createPoint(coordinates[0]);
        case OBJEKT:
            if (coordinates.length == 0) {
                return gf.createGeometryCollection(new Geometry[0]);
            }
            return gf.createPoint(coordinates[0]);
        case BUEP:
            return Buep.create(gf, coordinates);
		}
        return null;
    }

}
