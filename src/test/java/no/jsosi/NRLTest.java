package no.jsosi;

import java.io.File;
import java.io.FileInputStream;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.zip.ZipInputStream;

import org.locationtech.jts.geom.Envelope;
import org.locationtech.jts.geom.Point;

import junit.framework.TestCase;

public class NRLTest extends TestCase {

    public void testNRL() throws Exception {
        File file = new File("src/test/resources/NRL080416.sos.zip");
        assertTrue(file.canRead());

        FileInputStream fis = null;
        ZipInputStream zis = null;
        SosiReader ri = null;

        try {
            fis = new FileInputStream(file);
            zis = new ZipInputStream(fis);
            assertNotNull(zis.getNextEntry());
            ri = new SosiReader(zis);
            assertEquals("EPSG:25833", ri.getCrs());
            assertEquals(new Envelope(6161024, 8864310, -1021180, 1541478), ri.getBounds());
            Feature fi = null;
            int count = 0;
            Set<String> objtypes = new HashSet<String>();
            Set<String> keys = new HashSet<String>();
            Map<Integer, Feature> featureById = new HashMap<>();
            Set<Integer> wantedFeatureIds = new HashSet<>(Arrays.asList(Integer.valueOf(318160),
                    Integer.valueOf(318161), Integer.valueOf(291891), Integer.valueOf(318844), Integer.valueOf(3)));
            while ((fi = ri.nextFeature()) != null) {
                assertNotNull(fi);
                assertNotNull(fi.getGeometry());
                count++;
                objtypes.add(fi.get("OBJTYPE").toString());
                keys.addAll(fi.getAttributeKeySet());
                if (wantedFeatureIds.contains(fi.getId())) {
                    featureById.put(fi.getId(), fi);
                }
            }
            assertEquals(3, objtypes.size());
            assertEquals(318918, count);
            assertFalse(keys.contains(";opp"));
            assertFalse(keys.contains(";taubanen"));
            assertFalse(keys.contains("En av 16 master. Se arkiv"));
            // assertEquals(keys.toString(), 51, keys.size());

            Feature f1 = featureById.get(Integer.valueOf(318160));
            assertNotNull(f1);
            Object f1i = f1.get("INFORMASJON");
            assertNotNull(f1i);
            assertTrue(f1i instanceof Map);
            Map<?, ?> f1im = (Map<?, ?>) f1i;
            assertEquals(3, f1im.size());
            assertEquals("{AKILDE=NRL, GKILDE=NRL, =bardun festet i bakken og \nopp til kabel over dalen(spenn)}",
                    f1i.toString());

            assertEquals("sjekk status og agl", featureById.get(Integer.valueOf(318844)).get("INFORMASJON"));

            Feature f2 = featureById.get(Integer.valueOf(318161));
            assertNotNull(f2);
            assertEquals("FOT", f2.get("HREF"));
            Point p2 = (Point) f2.getGeometry();
            assertEquals(7495643.9, p2.getCoordinate().y, 0.001);
            assertEquals(505166.8, p2.getCoordinate().x, 0.001);
            assertEquals(18.0, p2.getCoordinate().z, 0.001);

            Feature f3 = featureById.get(Integer.valueOf(291891));
            assertEquals("TOP", f3.get("HREF"));

            Feature f4 = featureById.get(Integer.valueOf(3));
            Object f4a = f4.get("AVGRENSESAV");
            assertTrue(f4a instanceof Map);
            Map<?, ?> m = (Map<?, ?>) f4a;
            assertEquals(1, m.size());
            assertEquals(
                    "{KOMPONENTIDENT=[{LOKALID=1076257, NAVNEROM=NO.KARTVERKET.NRL, VERSJONID=2015-11-03T18:32:47.424+0100}, {LOKALID=1076260, NAVNEROM=NO.KARTVERKET.NRL, VERSJONID=2015-11-03T18:32:47.424+0100}]}",
                    f4a.toString());

        } finally {
            IOUtils.silentClose(ri, zis, fis);
        }
    }

}
