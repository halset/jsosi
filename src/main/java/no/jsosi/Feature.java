package no.jsosi;

import java.util.Collections;
import java.util.Map;

import com.vividsolutions.jts.geom.Geometry;

public class Feature {

    private final GeometryType geometryType;
    private final Geometry geometry;
    private final Map<String, Object> attributes;

    public Feature(GeometryType geometryType, Map<String, Object> attributes, Geometry geometry) {
        this.geometryType = geometryType;
        this.attributes = attributes;
        this.geometry = geometry;
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

    public Geometry getGeometry() {
        return geometry;
    }

    public int getCoordinateCount() {
        return getGeometry().getCoordinates().length;
    }

}
