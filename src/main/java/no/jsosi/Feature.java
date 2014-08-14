package no.jsosi;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;

public class Feature {

    private final Integer id;
    private final GeometryType geometryType;
    private final Coordinate[] coordinates;
    private final Map<String, Object> attributes;
    private final RefList refs;

    private Geometry geometry;

    Feature(Integer id, GeometryType geometryType, Map<String, Object> attributes,
            Coordinate[] coordinates, RefList refs) {
        this.id = id;
        this.geometryType = geometryType;
        this.attributes = attributes;
        this.coordinates = coordinates;
        this.refs = refs;
    }

    public Integer getId() {
        return id;
    }

    public Map<String, Object> getAttributeMap() {
        return Collections.unmodifiableMap(attributes);
    }

    public Object get(String key) {
        return attributes.get(key);
    }

    public GeometryType getGeometryType() {
        return geometryType;
    }

    void prepareGeometry(SosiReader reader) {
        if (geometry != null) {
            return;
        }

        if (geometryType == GeometryType.FLATE) {
            geometry = refs.createGeometry(reader);
        } else {
            geometry = geometryType.createGeometry(reader.getGeometryFactory(), coordinates);
        }
    }

    public Geometry getGeometry() {
        return geometry;
    }

    public int getCoordinateCount() {
        return getGeometry().getCoordinates().length;
    }

    public List<Coordinate> getCoordinates() {
        return Collections.unmodifiableList(Arrays.asList(coordinates));
    }

}
