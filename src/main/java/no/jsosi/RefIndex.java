package no.jsosi;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.locationtech.jts.geom.Coordinate;

class RefIndex implements Closeable {

    private RandomAccessFile raf;
    private FileChannel channel;
    private final double xyfactor;

    private final Map<Integer, Long> posById = new HashMap<>();
    private final Set<Integer> allRefs = new HashSet<Integer>();

    public RefIndex(File file, double xyfactor) throws IOException {

        this.xyfactor = xyfactor;

        // first find all '.KURVE '
        InputStream in = null;
        try {
            int bufSize = 1024 * 1024;
            in = new BufferedInputStream(new FileInputStream(file), bufSize);

            long pos = 0;
            char c;
            final RefPattern pattern = new RefPattern();
            int patternIndex = 0;
            long patternMatchStart = 0;
            StringBuilder id = new StringBuilder();
            boolean idReading = false;
            byte[] buf = new byte[bufSize];
            int len;

            // look for lines like '.KURVE 21398:' and '.BUEP 34799:'
            while ((len = in.read(buf)) > 0) {
                for (int i = 0; i < len; i++) {
                    c = (char) buf[i];
                    if (idReading) {
                        if (':' == c) {
                            posById.put(Integer.valueOf(id.toString()), patternMatchStart);
                            idReading = false;
                            id.setLength(0);
                            patternMatchStart = 0;
                            pattern.reset();
                            patternIndex = 0;
                        } else {
                            id.append(c);
                        }
                    } else if (pattern.match(c, patternIndex)) {
                        // match :)
                        if (patternIndex == 0) {
                            patternMatchStart = pos;
                        }
                        if (pattern.atEndOfMatch(patternIndex)) {
                            // found the complete pattern, now look for id
                            idReading = true;
                            id.setLength(0);
                        }
                        patternIndex++;
                    } else {
                        // no match :(
                        idReading = false;
                        patternMatchStart = 0;
                        patternIndex = 0;
                        pattern.reset();
                    }
                    pos = pos + 1l;
                }
            }
        } finally {
            IOUtils.silentClose(in);
        }

        // a separate scan for REF
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new FileReader(file));
            String line = null;
            while ((line = reader.readLine()) != null) {
                if (line.startsWith("..REF")) {
                    RefList refs = new RefList();
                    refs.add(line.substring("..REF".length()));
                    SosiReader.readRefs(reader, refs);
                    allRefs.addAll(refs.getRefs());
                }
            }
        } finally {
            IOUtils.silentClose(reader);
        }

        // then open for random access
        raf = new RandomAccessFile(file, "r");
        channel = raf.getChannel();

    }

    public Long position(Integer id) {
        return posById.get(id);
    }

    public List<Coordinate> getCoordinates(Integer id, String characterSet) throws IOException {
        Long position = position(id);
        if (position == null) {
            return null;
        }

        List<Coordinate> coords = new ArrayList<>();

        if (!channel.isOpen()) {
            throw new IllegalStateException("closed file channel");
        }

        channel.position(position.longValue());

        BufferedReader reader = null;
        reader = new BufferedReader(Channels.newReader(channel, characterSet));
        String line = null;
        while ((line = reader.readLine()) != null) {
            if (line.startsWith("..NØH")) {
                coords.addAll(SosiReader.readCoordinateLines(reader, xyfactor, 3));
            } else if (line.startsWith("..NØ")) {
                coords.addAll(SosiReader.readCoordinateLines(reader, xyfactor, 2));
            } else if (!coords.isEmpty()) {
                break;
            }
        }

        return Collections.unmodifiableList(coords);

    }

    public boolean isRef(Integer ref) {
        return allRefs.contains(ref);
    }

    @Override
    public void close() throws IOException {
        IOUtils.silentClose(channel, raf);
    }

}
