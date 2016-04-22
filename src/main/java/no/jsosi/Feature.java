package no.jsosi;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;

public class Feature {

    private final SosiReader reader;
    private final Integer id;
    private final GeometryType geometryType;
    private final Coordinate[] coordinates;
    private final Map<String, Object> attributes = new HashMap<>();
    private final RefList refs;

    Feature(SosiReader reader, Integer id, GeometryType geometryType, Map<String, Object> attributes,
            Coordinate[] coordinates, RefList refs) {
        this.reader = reader;
        this.id = id;
        this.geometryType = geometryType;
        this.coordinates = coordinates;
        this.refs = refs;
        putAll(attributes);
    }
    
    private void put(String key, Object value) {
        attributes.putAll(LineMap.create(key, value));
    }

    private void putAll(Map<String, Object> as) {
        for (Map.Entry<String, Object> e : as.entrySet()) {
            put(e.getKey(), e.getValue());
        }
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

    public Geometry getGeometry() throws IOException {
        if (geometryType == GeometryType.FLATE) {
            return refs.createGeometry(reader);
        } else {
            return geometryType.createGeometry(reader.getGeometryFactory(), coordinates);
        }
    }

    int getCoordinateCount() throws IOException {
        return getGeometry().getCoordinates().length;
    }

}
