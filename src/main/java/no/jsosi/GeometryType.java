package no.jsosi;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;

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
            if (coordinates.length == 1) {
                return gf.createPoint(coordinates[0]);
            }
            return gf.createLineString(coordinates);
        case SVERM:
            return gf.createMultiPointFromCoords(coordinates);
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
