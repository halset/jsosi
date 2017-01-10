package no.jsosi;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import junit.framework.TestCase;

public class DefTest extends TestCase {
	
    public void testDef() throws IOException {
        File file = new File("src/test/resources/0617_N50_AdministrativeOmrader.sos");
        assertTrue(file.canRead());
        SosiReader ri = new SosiReader(file);
        Feature fi = null;
        int count = 0;
        Set<String> objtypes = new HashSet<String>();
        while ((fi = ri.nextFeature()) != null) {
            assertNotNull(fi);
            assertNotNull(fi.getGeometry());
            count++;
            objtypes.add(fi.get("OBJTYPE").toString());
        }
        assertEquals(3, objtypes.size());
        assertEquals(6, count);
        ri.close();
    }


}
