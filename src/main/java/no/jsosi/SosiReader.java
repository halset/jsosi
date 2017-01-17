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
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.GeometryFactory;

/**
 * 
 * @see http://www.kartverket.no/globalassets/standard/sosi-standarden-del-1-og-2/sosi-standarden/del1_3_sosi_notasjon.pdf
 */
public class SosiReader implements Closeable {

    private GeometryFactory gf = new GeometryFactory();

    private boolean deleteFileOnClose = false;
    private final File file;
    private final RandomAccessFile raf;
    private final FileChannel channel;
    private BufferedReader reader;
    private RefIndex index;
    private String characterSet;

    private int level;
    private String key;
    private String value;
    private String crs;
    private double xyfactor;
    private Envelope bounds;

    private static final Set<String> HEADERS = Collections.unmodifiableSet(new HashSet<String>(
            Arrays.asList("TEGNSETT", "KOORDSYS", "ENHET", "MIN-NØ", "MAX-NØ")));
    
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

        String initialCharacterSet = "ISO-8859-1"; //Common assumption
        
        // reader character set from head
        reader = new BufferedReader(Channels.newReader(channel, initialCharacterSet));

        Map<String, String> head = readHead();
        characterSet = findCharSet(bomLength, head);
        if (!characterSet.equals(initialCharacterSet)) {
        	head = readHead();
        }

        parseHead(head);        

        // need to parse all features as FLATE can reference KURVE later in the
        // file
        index = new RefIndex(in, xyfactor);

        // spool back once more and read for real
        channel.position(bomLength);
        reader = new BufferedReader(Channels.newReader(channel, characterSet));

    }

	private Map<String, String> readHead() throws IOException {
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
		return head;
	}

	private String findCharSet(int bomLength, Map<String, String> head) throws IOException {
		String characterSet;
		// spool back and read with proper character set.
        channel.position(bomLength);
        characterSet = Tegnsett.getCharsetForTegnsett(head.get("TEGNSETT"));
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
		return characterSet;
	}

	private void parseHead(Map<String, String> head) {
		String[] values = head.get("KOORDSYS").split(" ");
        crs = Koordsys.getEpsgForKoordsys(Integer.parseInt(values[0]));

        xyfactor = Double.parseDouble(head.get("ENHET"));
        
        //RegEx for malformed files with multiple spaces between values
        String[] minNEvalues = head.get("MIN-NØ").trim().split("\\s+");
        String[] maxNEvalues = head.get("MAX-NØ").trim().split("\\s+");
        Coordinate minNE = new Coordinate(
        		Double.parseDouble(minNEvalues[0]), 
        		Double.parseDouble(minNEvalues[1]));
        Coordinate maxNE = new Coordinate(
        		Double.parseDouble(maxNEvalues[0]), 
        		Double.parseDouble(maxNEvalues[1]));
        
        bounds = new Envelope(minNE, maxNE);
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
    private final AttributeMap currentAttributes = new AttributeMap();
    private final List<Coordinate> currentCoordinates = new ArrayList<Coordinate>();
    private RefList currentRefs = null;
    private String lastAttributeKey;

    public Feature nextFeature() throws IOException {
        while (readLine()) {

            // special case for multiple line text attribute value
            if (level == 0 && key.length() > 0 && key.startsWith(";")) {
                Object prevValue = currentAttributes.getLastValueForKey(lastAttributeKey);
                if (prevValue != null && prevValue instanceof String) {
                    String v = prevValue.toString() + "\n" + key.substring(1);
                    if (v.startsWith("\"") && v.endsWith("\"")) {
                        v = v.substring(1, v.length() - 1);
                    }
                    currentAttributes.remove(lastAttributeKey);
                    currentAttributes.add(lastAttributeKey, v);
                }
                continue;
            }

            switch (level) {
            case 1:
                if ("HODE".equals(key)) {
                    currentGeometryType = null;
                    currentFeatureId = null;
                    head = true;
                    break;
                }
                
                if ("DEF".equals(key)) {
                	break;
                }

                GeometryType previousGeometryType = currentGeometryType;
                Integer previousFeatureId = currentFeatureId;

                AttributeMap previousAttributes = new AttributeMap(
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
                lastAttributeKey = null;

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
                } else if (key != null && key.length() > 0 && value != null && !key.startsWith(";")) {
                    currentAttributes.add(key, value);
                    lastAttributeKey = key;
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
            
            if (line.length() == 0) {
                continue;
            }

            String[] tokens = line.split(" ");

            Coordinate coord = new Coordinate(Double.parseDouble(tokens[1]) * xyfactor,
                    Double.parseDouble(tokens[0]) * xyfactor);

            if (dim >= 3) {
                coord.z = Double.parseDouble(tokens[2]) * xyfactor;
            }

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
        
        // special case for text attribute value with multiple lines
        if (line.length() > 0 && level == 0 && line.charAt(0) == ';') {
            key = line;
            value = null;
            return true;
        }

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

    public Envelope getBounds() {
		return new Envelope(bounds);
	}

	List<Coordinate> getKurve(Integer id) throws IOException {
        return index.getCoordinates(id, characterSet);
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
