package no.jsosi;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.LineString;

import junit.framework.TestCase;

public class BuepTest extends TestCase {

    public void testSamferdsel() throws IOException {
        File file = new File("src/test/resources/0128_N50_Samferdsel.sos");
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
        assertEquals(8, objtypes.size());
        assertEquals(3274, count);
        ri.close();
    }

    public void testBuep() {
        GeometryFactory gf = new GeometryFactory();
        Coordinate c0 = new Coordinate(0, 5);
        Coordinate c1 = new Coordinate(5, 10);
        Coordinate c2 = new Coordinate(10, 5);
        Coordinate c3 = new Coordinate(5, -10);
        Geometry buep = Buep.create(gf, new Coordinate[] { c0, c1, c2 });
        assertNotNull(buep);
        assertTrue(buep instanceof LineString);

        Geometry buffered = buep.buffer(0.1);
        assertTrue(buffered.intersects(gf.createPoint(c0)));
        assertTrue(buffered.intersects(gf.createPoint(c1)));
        assertTrue(buffered.intersects(gf.createPoint(c2)));
        assertFalse(buffered.intersects(gf.createPoint(c3)));

    }

    public void testBuepHard() {
        GeometryFactory gf = new GeometryFactory();
        Coordinate c0 = new Coordinate(-39782.26, 6570926.04);
        Coordinate c1 = new Coordinate(-39782.35, 6570926.04);
        Coordinate c2 = new Coordinate(-39782.4, 6570926.11);
        Geometry buep = Buep.create(gf, new Coordinate[] { c0, c1, c2 });
        assertNotNull(buep);
        assertTrue(buep instanceof LineString);
        assertTrue(buep.isValid());
    }

}
