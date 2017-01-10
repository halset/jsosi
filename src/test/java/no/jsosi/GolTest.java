package no.jsosi;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import junit.framework.TestCase;

public class GolTest extends TestCase {

	public void testGol() throws IOException {
		File file = new File("src/test/resources/Kartdata_617_Gol_UTM33_N50_SOSI.zip");
		ZipInputStream zis = new ZipInputStream(new FileInputStream(file));
		ZipEntry entry = null;
		byte[] buff = new byte[1024];
		while ((entry = zis.getNextEntry()) != null) {
			if (!entry.getName().endsWith(".sos")) {
				while (zis.read(buff) > 0) {
				}
				continue;
			}
			SosiReader reader = new SosiReader(zis);
			Feature fi = null;
			while ((fi = reader.nextFeature()) != null) {
				assertNotNull(fi);
				assertNotNull(fi.getGeometry());
			}
			reader.close();
		}
		zis.close();
	}

}
