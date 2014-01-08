package no.jsosi;

import junit.framework.TestCase;

public class KoordsysTest extends TestCase {

    public void testGetEpsgForKoordsys() {
        assertEquals("EPSG:32632", Koordsys.getEpsgForKoordsys(62));
        assertEquals("EPSG:25832", Koordsys.getEpsgForKoordsys(22));
    }

}
