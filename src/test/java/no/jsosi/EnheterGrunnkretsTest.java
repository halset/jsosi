package no.jsosi;

import java.io.File;
import java.util.HashSet;
import java.util.Set;

import org.locationtech.jts.geom.Envelope;

import junit.framework.TestCase;

public class EnheterGrunnkretsTest extends TestCase {

    public void testEnheterGrunnkrets() throws Exception {
        File file = new File("src/test/resources/STAT_enheter_grunnkretser.sos");
        assertTrue(file.canRead());
        SosiReader ri = new SosiReader(file);
        assertEquals("EPSG:25833", ri.getCrs());
        assertEquals(new Envelope(6426048, 7962744, -99553, 1121942), ri.getBounds());
        Feature fi = null;
        int count = 0;
        Set<String> objtypes = new HashSet<String>();
        while ((fi = ri.nextFeature()) != null) {
            assertNotNull(fi);
            assertNotNull(fi.getGeometry());
            count++;
            assertNotNull(fi.get("OBJTYPE"));
            objtypes.add(fi.get("OBJTYPE").toString());
        }
        assertEquals(8, objtypes.size());
        assertEquals(79724, count);
        ri.close();
    }

}
