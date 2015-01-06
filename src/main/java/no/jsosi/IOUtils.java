package no.jsosi;

import java.io.BufferedOutputStream;
import java.io.Closeable;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class IOUtils {

    private IOUtils() {

    }

    public static File toTempFile(InputStream in) throws IOException {
        File file = File.createTempFile("jsosi", null);
        file.deleteOnExit();
        writeToFile(in, file);
        return file;
    }

    public static void writeToFile(InputStream is, File file) throws IOException {
        byte[] buf = new byte[4096];
        FileOutputStream fos = null;
        BufferedOutputStream bos = null;
        try {
            fos = new FileOutputStream(file);
            bos = new BufferedOutputStream(fos, buf.length);
            IOUtils.copy(is, bos);
            bos.flush();
        } finally {
            silentClose(bos, fos);
        }
    }

    public static final void copy(InputStream in, OutputStream out) throws IOException {
        byte[] buf = new byte[8192];
        int bytesRead = 0;
        while ((bytesRead = in.read(buf)) != -1) {
            out.write(buf, 0, bytesRead);
        }
    }

    public static final void silentClose(Closeable closeable) {
        try {
            closeable.close();
        } catch (IOException e) {
            // ignore
        }
    }
    
    public static final void silentClose(Closeable ... closeable) {
        for (Closeable c : closeable) {
            silentClose(c);
        }
    }


}
