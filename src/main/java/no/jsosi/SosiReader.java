package no.jsosi;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.GeometryFactory;

public class SosiReader implements Closeable {

    private GeometryFactory gf = new GeometryFactory();

    private BufferedReader reader;

    private int level;
    private String key;
    private String value;
    private String crs;
    private double xyfactor;

    private final Map<Integer, Feature> featureById = new LinkedHashMap<Integer, Feature>();
    private Iterator<Feature> featureIterator;

    public SosiReader(InputStream in) throws IOException {
        // reader character set from head
        BufferedInputStream bin = new BufferedInputStream(in);
        bin.mark(1024);
        reader = new BufferedReader(new InputStreamReader(bin, "UTF-8"));

        Map<String, String> head = new HashMap<String, String>();
        while (readLine()) {
            head.put(key, value);

            // make sure we do not look too far
            if (level == 1 && !"HODE".equals(key)) {
                break;
            }
        }

        String characterSet = head.get("TEGNSETT");
        if (characterSet.startsWith("ISO") && !characterSet.startsWith("ISO-")) {
            characterSet = characterSet.replace("ISO", "ISO-");
        }

        // fake this one for now. sorry
        if (characterSet.equals("ISO-8859-10")) {
            characterSet = "ISO-8859-1";
        }

        String[] values = head.get("KOORDSYS").split(" ");
        crs = Koordsys.getEpsgForKoordsys(Integer.parseInt(values[0]));

        xyfactor = Double.parseDouble(head.get("ENHET"));

        // spool back and read with proper character set.
        bin.reset();
        reader = new BufferedReader(new InputStreamReader(bin, characterSet));

        // need to parse all features as FLATE can reference KURVE later in the
        // file
        Feature feature = null;
        while ((feature = nextFeatureInternal()) != null) {
            featureById.put(feature.getId(), feature);
        }
        featureIterator = featureById.values().iterator();

    }

    public String getCrs() {
        return crs;
    }

    private boolean head = false;
    private GeometryType currentGeometryType = null;
    private Integer currentFeatureId = null;
    private Map<String, Object> currentAttributes = new HashMap<String, Object>();
    private List<Coordinate> currentCoordinates = new ArrayList<Coordinate>();
    private RefList currentRefs = null;

    private Feature nextFeatureInternal() throws IOException {
        while (readLine()) {
            switch (level) {
            case 1:
                if ("HODE".equals(key)) {
                    currentGeometryType = null;
                    currentFeatureId = null;
                    head = true;
                    break;
                }

                GeometryType previousGeometryType = currentGeometryType;
                Integer previousFeatureId = currentFeatureId;

                Map<String, Object> previousAttributes = new HashMap<String, Object>(
                        currentAttributes);
                Coordinate[] previousCoordinates = currentCoordinates
                        .toArray(new Coordinate[currentCoordinates.size()]);

                if ("SLUTT".equals(key)) {
                    currentGeometryType = null;
                    currentFeatureId = null;
                } else {
                    currentGeometryType = GeometryType.valueOf(key);
                    currentFeatureId = Integer.valueOf(value);
                }

                currentAttributes.clear();
                currentCoordinates.clear();

                if (head) {
                    head = false;
                    continue;
                }

                RefList refList = currentRefs;
                currentRefs = null;

                return new Feature(previousFeatureId, previousGeometryType, previousAttributes,
                        previousCoordinates, refList);
            default:

                if ("NØ".equals(key)) {
                    readCoordinateLines(2);
                } else if ("NØH".equals(key)) {
                    readCoordinateLines(3);
                } else if ("REF".equals(key)) {
                    currentRefs = new RefList();
                    currentRefs.add(value);
                    readRefs();
                } else {
                    currentAttributes.put(key, value);
                }

            }

        }
        return null;
    }

    private void readCoordinateLines(int dim) throws IOException {

        reader.mark(100);
        while (true) {
            String line = reader.readLine();

            if (line == null || line.startsWith(".")) {
                reader.reset();
                break;
            }

            String[] tokens = line.split(" ");

            Coordinate coord = new Coordinate(Double.parseDouble(tokens[1]) * xyfactor,
                    Double.parseDouble(tokens[0]) * xyfactor);

            currentCoordinates.add(coord);

            reader.mark(100);
        }
    }

    private void readRefs() throws IOException {
        reader.mark(100);
        while (true) {
            String line = reader.readLine();
            if (line == null || line.startsWith(".")) {
                reader.reset();
                break;
            }
            currentRefs.add(line);
            reader.mark(100);
        }
    }

    private boolean readLine() throws IOException {
        level = 0;
        key = null;
        value = null;

        String line = reader.readLine();

        if (line == null) {
            return false;
        }

        int thisLevel = 0;
        for (int i = 0; i < line.length(); i++) {
            if (line.charAt(i) == '.') {
                thisLevel++;
            } else {
                break;
            }
        }

        level = thisLevel;
        line = line.substring(level);

        int p = line.indexOf(' ');
        if (p > 0) {
            key = line.substring(0, p);
            value = line.substring(p + 1);

            if (value.startsWith("\"") && value.endsWith("\"")) {
                value = value.substring(1, value.length() - 1);
            }

            if (value.endsWith(":")) {
                value = value.substring(0, value.length() - 1);
            }

        } else {
            key = line;
            value = null;
        }

        return true;
    }

    GeometryFactory getGeometryFactory() {
        return gf;
    }

    public Feature getFeature(Integer id) {
        Feature feature = featureById.get(id);
        feature.prepareGeometry(this);
        return feature;
    }

    public Feature nextFeature() {
        if (featureIterator == null || !featureIterator.hasNext()) {
            return null;
        }
        Feature feature = featureIterator.next();
        feature.prepareGeometry(this);
        return feature;
    }

    public void close() throws IOException {
        reader.close();
    }

}
