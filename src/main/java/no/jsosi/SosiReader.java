package no.jsosi;

import java.io.BufferedReader;
import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.charset.MalformedInputException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.io.ByteOrderMark;
import org.apache.commons.io.input.BOMInputStream;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.GeometryFactory;

public class SosiReader implements Closeable {

    private GeometryFactory gf = new GeometryFactory();

    private boolean deleteFileOnClose = false;
    private final File file;
    private final RandomAccessFile raf;
    private final FileChannel channel;
    private BufferedReader reader;
    private RefIndex index;

    private int level;
    private String key;
    private String value;
    private String crs;
    private double xyfactor;

    private static final Set<String> HEADERS = Collections.unmodifiableSet(new HashSet<String>(
            Arrays.asList("TEGNSETT", "KOORDSYS", "ENHET")));
    
    public SosiReader(File in) throws IOException {

        this.file = in;
        
        // find BOM
        int bomLength = 0;
        BOMInputStream bis = null;
        try {
            bis = new BOMInputStream(new FileInputStream(in));
            ByteOrderMark bom = bis.getBOM();
            bomLength = bom == null ? 0 : bom.length();
        } finally {
            IOUtils.silentClose(bis);
        }
        
        this.raf = new RandomAccessFile(in, "r");
        this.channel = raf.getChannel();
        this.channel.position(bomLength);

        // reader character set from head
        reader = new BufferedReader(Channels.newReader(channel, "ISO-8859-1"));

        Map<String, String> head = new HashMap<String, String>();
        while (readLine()) {
            head.put(key, value);

            // make sure we do not look too far
            if (head.keySet().containsAll(HEADERS)) {
                break;
            }
            if (level == 1 && !"HODE".equals(key)) {
                break;
            }
        }

        String[] values = head.get("KOORDSYS").split(" ");
        crs = Koordsys.getEpsgForKoordsys(Integer.parseInt(values[0]));

        xyfactor = Double.parseDouble(head.get("ENHET"));

        // spool back and read with proper character set.
        channel.position(bomLength);
        String characterSet = Tegnsett.getCharsetForTegnsett(head.get("TEGNSETT"));
        reader = new BufferedReader(Channels.newReader(channel, characterSet));
        
        // some files are ISO-8859-1, but marked as UTF-8 :(
        if ("UTF-8".equals(characterSet)) {
            try {
                while (reader.readLine() != null) {
                }
            } catch (MalformedInputException e) {
                characterSet = "ISO-8859-1";
            }
            channel.position(bomLength);
            reader = new BufferedReader(Channels.newReader(channel, characterSet));
        }

        // need to parse all features as FLATE can reference KURVE later in the
        // file
        index = new RefIndex(in, xyfactor);

        // spool back once more and read for real
        channel.position(bomLength);
        reader = new BufferedReader(Channels.newReader(channel, characterSet));

    }
    
    /**
     * {@link SosiReader} operate on file to save memory while handling KURVE
     * references, so this constructor copy the {@link InputStream} to a
     * temporary {@link File}
     * 
     * @param in
     * @throws IOException
     */
    public SosiReader(InputStream in) throws IOException {
        this(IOUtils.toTempFile(in));
        this.deleteFileOnClose = true;
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

                return new Feature(this, previousFeatureId, previousGeometryType,
                        previousAttributes, previousCoordinates, refList);

            default:

                if ("NØ".equals(key)) {
                    currentCoordinates.addAll(readCoordinateLines(reader, xyfactor, 2));
                } else if ("NØH".equals(key)) {
                    currentCoordinates.addAll(readCoordinateLines(reader, xyfactor, 3));
                } else if ("REF".equals(key)) {
                    currentRefs = new RefList();
                    currentRefs.add(value);
                    readRefs(reader, currentRefs);
                } else if (key != null && key.length() > 0 && value != null) {
                    currentAttributes.put(key, value);
                }

            }

        }
        return null;
    }

    static List<Coordinate> readCoordinateLines(BufferedReader reader, double xyfactor, int dim) throws IOException {

        List<Coordinate> coords = new ArrayList<>();
        
        reader.mark(100);
        while (true) {
            String line = reader.readLine();

            if (line == null || line.startsWith(".")) {
                reader.reset();
                break;
            }

            // handle comment in coordinate line
            int commentPosition = line.indexOf('!');
            if (commentPosition >= 0) {
                line = line.substring(0, commentPosition);
                if (line.length() == 0) {
                    reader.mark(100);
                    continue;
                }
            }

            String[] tokens = line.split(" ");

            Coordinate coord = new Coordinate(Double.parseDouble(tokens[1]) * xyfactor,
                    Double.parseDouble(tokens[0]) * xyfactor);

            coords.add(coord);

            reader.mark(100);
        }
        
        return coords;
    }

    static void readRefs(BufferedReader reader, RefList refs) throws IOException {
        reader.mark(100);
        while (true) {
            String line = reader.readLine();
            if (line == null || line.startsWith(".")) {
                reader.reset();
                break;
            }
            refs.add(line);
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

            // look for ""
            int q1 = value.indexOf('"');
            int q2 = value.indexOf('"', q1 + 1);

            // look for comment. after ""
            int cp = value.indexOf('!', q2);
            if (cp >= 0) {
                value = value.substring(0, cp);
            }

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

    double getXYFactor() {
        return xyfactor;
    }

    List<Coordinate> getKurve(Integer id) throws IOException {
        return index.getCoordinates(id);
    }

    public Feature nextFeature() throws IOException {
        Feature feature = null;
        while ((feature = nextFeatureInternal()) != null) {

            // skipping referenced features. hope this is fine..
            if (index.isRef(feature.getId())) {
                String objtype = (String) feature.get("OBJTYPE");
                if (objtype != null && objtype.endsWith("grense")) {
                    continue;
                }
            }

            return feature;
        }
        return null;
    }
    
    public float getProgress() throws IOException {
        return (float) raf.getFilePointer() / (float) raf.length();
    }

    public void close() throws IOException {
        IOUtils.silentClose(reader, channel, raf, index);
        if (deleteFileOnClose) {
            file.delete();
        }
    }

}
