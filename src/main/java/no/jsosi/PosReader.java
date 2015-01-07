package no.jsosi;

import java.io.BufferedReader;
import java.io.Closeable;
import java.io.IOException;
import java.io.Reader;

public class PosReader implements Closeable {

    private final BufferedReader reader;
    private long position = 0;
    private long mark = 0;

    public PosReader(Reader reader) {
        this(new BufferedReader(reader));
    }

    public PosReader(BufferedReader reader) {
        this.reader = reader;
    }

    public void mark(int readAheadLimit) throws IOException {
        reader.mark(readAheadLimit);
        mark = position;
    }

    public void reset() throws IOException {
        reader.reset();
        position = mark;
        mark = 0;
    }

    public String readLine() throws IOException {
        String line = reader.readLine();
        if (line == null) {
            return null;
        }
        position = position + line.length();
        return line;
    }

    public long position() {
        return position;
    }

    @Override
    public void close() throws IOException {
        reader.close();
    }

}
