package no.jsosi;

import java.io.File;
import java.io.IOException;

import junit.framework.TestCase;

public class RefIndexTest extends TestCase {

    public void testIndex() throws IOException {
        RefIndex index = new RefIndex(new File("src/test/resources/1421_Arealdekke.sos"), 1.0);
        assertEquals(Long.valueOf(10777804), index.position(21401));
        assertEquals(2, index.getCoordinates(21401).size());
        assertTrue(index.isRef(13771));
        assertTrue(index.isRef(12720));
        index.close();
    }

}
