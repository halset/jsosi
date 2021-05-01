package no.jsosi;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;

public class Feature {

    private final SosiReader reader;
    private final Integer id;
    private final GeometryType geometryType;
    private final Coordinate[] coordinates;
    private final AttributeMap attributes;
    private final RefList refs;
    private Map<String, Geometry> extraGeometryByName; 
    
    public static final String KEY_TEKST_LINJE = "TEKST_LINJE";

    Feature(SosiReader reader, Integer id, GeometryType geometryType, AttributeMap attributes, Coordinate[] coordinates,
            RefList refs) {
        this.reader = reader;
        this.id = id;
        this.geometryType = geometryType;
        this.coordinates = coordinates;
        this.refs = refs;
        this.attributes = new AttributeMap(attributes);
        this.attributes.computeSubValues();
        
        // for TEKST with 3 coordinates, create a line between the two last coordinates
        // for the text direction
        if (geometryType == GeometryType.TEKST && coordinates.length == 3) {
            Coordinate start = coordinates[1];
            Coordinate end = coordinates[2];
            if (!start.equals2D(end)) {
                extraGeometryByName = new HashMap<>(1);
                extraGeometryByName.put(KEY_TEKST_LINJE,
                        reader.getGeometryFactory().createLineString(new Coordinate[] { start, end }));
            }
        }
        
    }

    public Integer getId() {
        return id;
    }

    public Set<String> getAttributeKeySet() {
        return Collections.unmodifiableSet(attributes.keySet());
    }
    
    public Map<String, Object> getAttributeMap() {
        return attributes.toExternal();
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
    
    public boolean hasExtraGeometries() {
        return extraGeometryByName != null && !extraGeometryByName.isEmpty();
    }
    
    public Map<String, Geometry> getExtraGeometryByName() {
        if (extraGeometryByName == null) {
            return Collections.emptyMap();
        }
        return Collections.unmodifiableMap(extraGeometryByName);
    }

    int getCoordinateCount() throws IOException {
        return getGeometry().getCoordinates().length;
    }

    @Override
    public String toString() {
        return "Feature{attributes=" + attributes + "}";
    }

}
