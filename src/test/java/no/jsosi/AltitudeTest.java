package no.jsosi;

import java.io.File;
import java.io.IOException;

import junit.framework.TestCase;

public class AltitudeTest extends TestCase {

    public void testAltitude() throws IOException {
        File file = new File("src/test/resources/1151_N50_Hoyde.sos");
        assertTrue(file.canRead());

        SosiReader r = new SosiReader(file);

        Feature feature = null;
        while ((feature = r.nextFeature()) != null) {
            Object altitude = feature.get("HØYDE");
            assertNotNull(altitude);
            assertTrue(altitude instanceof Number);
        }

        r.close();
    }

}
