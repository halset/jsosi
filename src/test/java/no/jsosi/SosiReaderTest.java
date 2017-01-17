package no.jsosi;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.zip.GZIPInputStream;

import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;

import junit.framework.TestCase;

public class SosiReaderTest extends TestCase {

    public void testAddress() throws IOException {
        File file = new File("src/test/resources/0219Adresser.SOS");
        assertTrue(file.canRead());

        SosiReader r = new SosiReader(file);
        assertEquals("EPSG:25833", r.getCrs());
        assertEquals(0.01, r.getXYFactor());
        assertEquals(new Envelope(6640758, 6663653, 239438, 256376), r.getBounds());

        List<Feature> features = new ArrayList<Feature>();
        Feature feature;
        while ((feature = r.nextFeature()) != null) {
            features.add(feature);
        }

        assertEquals(33545, features.size());

        Feature f = features.get(0);
        assertEquals("Hans Hanssens vei !nocomment", f.get("GATENAVN"));
        assertEquals("SNARØYA", f.get("POSTNAVN"));
        assertEquals("0219", f.get("KOMM"));
        assertEquals(Integer.valueOf(4), f.get("HUSNR"));
        assertNull(f.get("NØ"));
        assertNotNull(f.getGeometry());
        assertTrue(f.getGeometry() instanceof Point);
        assertEquals(1, f.getCoordinateCount());
        assertEquals(253673.99, f.getGeometry().getCoordinates()[0].x, 0.01);
        assertEquals(6645919.76, f.getGeometry().getCoordinates()[0].y, 0.01);

        r.close();

    }

    public void testVbase() throws IOException {
        File file = new File("src/test/resources/Vbase_02.SOS");
        assertTrue(file.canRead());

        SosiReader r = new SosiReader(file);
        assertEquals("EPSG:25833", r.getCrs());
        assertEquals(new Envelope(6601345, 6724088, 238657, 326808), r.getBounds());

        Feature f1 = r.nextFeature();
        assertEquals("P V 99834", f1.get("VNR"));
        assertNull(f1.get("GATENAVN"));
        assertEquals(12, f1.getCoordinateCount());
        assertTrue(f1.getGeometry() instanceof LineString);

        Feature f2 = r.nextFeature();
        assertEquals("Åsveien", f2.get("GATENAVN"));
        assertEquals(15, f2.getCoordinateCount());
        assertTrue(f2.getGeometry() instanceof LineString);

        int count = 1;
        Feature f = null;
        while ((f = r.nextFeature()) != null) {
            count++;
            assertNotNull(f.getGeometry());
        }

        assertTrue(count > 10000);

        r.close();

    }

    public void testArealdekke() throws IOException {
        File file = new File("src/test/resources/1421_Arealdekke.sos");
        assertTrue(file.canRead());

        SosiReader r = new SosiReader(file);
        assertEquals("EPSG:25832", r.getCrs());
        assertEquals(new Envelope(6724129, 6774390, 374177, 433608), r.getBounds());

        Feature f1 = r.nextFeature();
        assertEquals("10000101", f1.get("OPPDATERINGSDATO"));
        assertEquals("ÅpentOmråde", f1.get("OBJTYPE"));
        assertNull(f1.get("GATENAVN"));
        assertTrue(f1.getGeometry() instanceof Polygon);
        assertTrue(f1.getGeometry().isValid());

        int count = 0;
        Set<String> objtypes = new HashSet<String>();

        Feature f = null;
        Feature f5763 = null;
        while ((f = r.nextFeature()) != null) {
            String objtype = (String) f.get("OBJTYPE");
            assertNotNull(objtype);

            count++;
            objtypes.add(objtype);

            if (f.getId().intValue() == 5763) {
                f5763 = f;
            }

            if (f.getGeometryType() != GeometryType.KURVE) {
                continue;
            }
        }

        assertNotNull(f5763);
        assertEquals(5763, f5763.getId().intValue());
        assertEquals("Innsjø", f5763.get("OBJTYPE"));
        assertTrue(f5763.getGeometry() instanceof Polygon);
        assertTrue(f5763.getGeometry().isValid());

        assertEquals(21313, count);
        assertEquals(27, objtypes.size());

        r.close();
    }

    public void testNavnISO() throws IOException {
        File file = new File("src/test/resources/1421_Navn_iso.sos");
        assertTrue(file.canRead());
        SosiReader ri = new SosiReader(file);
        assertEquals("EPSG:25832", ri.getCrs());
        assertEquals(new Envelope(6714086, 6784443, 362296, 445490), ri.getBounds());

        Feature fi = null;
        while ((fi = ri.nextFeature()) != null) {
            assertEquals("Skrivemåte", fi.get("OBJTYPE"));
            assertNotNull(fi.getGeometry());
            assertTrue(fi.getGeometry().isValid());
        }
        ri.close();
    }

    public void testInputStream() throws IOException {
        File file = new File("src/test/resources/1421_Navn_iso.sos");
        assertTrue(file.canRead());
        SosiReader ri = new SosiReader(new FileInputStream(file));
        assertEquals("EPSG:25832", ri.getCrs());
        assertEquals(new Envelope(6714086, 6784443, 362296, 445490), ri.getBounds());
        Feature fi = null;
        while ((fi = ri.nextFeature()) != null) {
            assertNotNull(fi);
        }
        ri.close();
    }

    public void testProgress() throws IOException {
        File file = new File("src/test/resources/1421_Navn_iso.sos");
        assertTrue(file.canRead());
        SosiReader ri = new SosiReader(file);
        assertEquals("EPSG:25832", ri.getCrs());
        assertEquals(0f, ri.getProgress(), 0.0001f);
        assertEquals(new Envelope(6714086, 6784443, 362296, 445490), ri.getBounds());

        for (int i = 0; i < 1500; i++) {
            assertNotNull(ri.nextFeature());
        }
        assertEquals(0.571257f, ri.getProgress(), 0.0001f);

        Feature fi = null;
        while ((fi = ri.nextFeature()) != null) {
            assertNotNull(fi);
        }
        assertEquals(1f, ri.getProgress(), 0.0001f);
        ri.close();

    }

    public void testBOM() throws IOException {
        File file = new File("src/test/resources/BOM_Navn_utf8.sos");
        assertTrue(file.canRead());
        SosiReader ri = new SosiReader(file);
        assertEquals("EPSG:25833", ri.getCrs());
        assertEquals(new Envelope(6447273, 7941195, -80700, 1108962), ri.getBounds());
        Feature fi = null;
        int count = 0;
        while ((fi = ri.nextFeature()) != null) {
            assertNotNull(fi);
            assertNotNull(fi.getGeometry());
            count++;
        }
        assertEquals(5557, count);
        ri.close();
    }

    public void testISOUTF8() throws IOException {
        File file = new File("src/test/resources/ISO_Navn_utf8.sos");
        assertTrue(file.canRead());
        SosiReader ri = new SosiReader(file);
        assertEquals("EPSG:25833", ri.getCrs());
        assertEquals(new Envelope(6577658, 6651204, 24803, 92200), ri.getBounds());
        Feature fi = null;
        int count = 0;
        while ((fi = ri.nextFeature()) != null) {
            assertNotNull(fi);
            assertNotNull(fi.getGeometry());
            count++;
        }
        assertEquals(2916, count);
        ri.close();
    }

    public void testEmptyLine() throws IOException {
        File file = new File("src/test/resources/0633_Navn_utf8.sos");
        assertTrue(file.canRead());
        SosiReader ri = new SosiReader(file);
        assertEquals("EPSG:25833", ri.getCrs());
        assertEquals(new Envelope(6663986, 6735573, 63576, 198523), ri.getBounds());
        Feature fi = null;
        int count = 0;
        while ((fi = ri.nextFeature()) != null) {
            assertNotNull(fi);
            assertNotNull(fi.getGeometry());

            for (String key : fi.getAttributeKeySet()) {
                if ("SSR".equals(key)) {
                    continue;
                }
                assertNotNull("feature should not have null key. " + fi.toString(), key);
                assertTrue("feature should not have empty key. " + fi.toString(), key.length() > 0);
                assertNotNull("key '" + key + "' should not have null value", fi.get(key));
            }

            count++;
        }
        assertEquals(3844, count);
        ri.close();
    }

    public void testMissingGeometry() throws IOException {
        File file = new File("src/test/resources/0540_Navn_utf8.sos");
        assertTrue(file.canRead());
        SosiReader ri = new SosiReader(file);
        assertEquals("EPSG:25833", ri.getCrs());
        assertEquals(new Envelope(6703549, 6771747, 179199, 236843), ri.getBounds());
        Feature fi = null;
        int count = 0;
        while ((fi = ri.nextFeature()) != null) {
            assertNotNull(fi);
            assertNotNull(fi.getGeometry());
            if ("Fønhuskoia".equals(fi.get("STRENG"))) {
                assertTrue(fi.getGeometry().isEmpty());
            } else {
                assertFalse(fi.getGeometry().isEmpty());
            }
            count++;
        }
        assertEquals(2304, count);
        ri.close();
    }

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
            objtypes.add(fi.get("OBJTYPE").toString());
        }
        assertEquals(8, objtypes.size());
        assertEquals(79724, count);
        ri.close();
    }

    public void testRadonAktsomhet() throws IOException {
        File file = new File("src/test/resources/RadonAktsomhet.sos");
        assertTrue(file.canRead());
        SosiReader ri = new SosiReader(file);
        assertEquals("EPSG:25833", ri.getCrs());
        assertEquals(new Envelope(6636699, 6675271, 248151, 274432), ri.getBounds());
        Feature fi = null;
        int count = 0;
        Set<String> objtypes = new HashSet<String>();
        while ((fi = ri.nextFeature()) != null) {
            assertNotNull(fi);
            assertNotNull(fi.getGeometry());
            Geometry geometry = fi.getGeometry();
            if (geometry instanceof Polygon) {
                Polygon p = (Polygon) geometry;
                assertTrue("" + fi.getId(), p.getExteriorRing().getNumPoints() >= 3);
                for (int i = 0; i < p.getNumInteriorRing(); i++) {
                    assertTrue(p.getInteriorRingN(i).getNumPoints() >= 3);
                }
            }
            count++;
            objtypes.add(fi.get("OBJTYPE").toString());
        }
        assertEquals(2, objtypes.size());
        assertEquals(1792, count);
        ri.close();
    }

    public void testN1000Arealdekke() throws IOException {
        File t = File.createTempFile("jsosi-n1000arealdekke", null);
        File file = new File("src/test/resources/NO_N1000_Arealdekke.sos.gz");
        Files.copy(new GZIPInputStream(new FileInputStream(file)), t.toPath(), StandardCopyOption.REPLACE_EXISTING);
        file = t;

        assertTrue(file.canRead());
        SosiReader ri = new SosiReader(file);
        assertEquals("EPSG:25833", ri.getCrs());
        Feature fi = null;
        int count = 0;
        Set<String> objtypes = new HashSet<String>();
        while ((fi = ri.nextFeature()) != null) {
            assertNotNull(fi);
            assertNotNull(fi.getGeometry());
            count++;
            objtypes.add(fi.get("OBJTYPE").toString());
        }
        assertEquals(19, objtypes.size());
        assertEquals(49796, count);
        ri.close();
    }

    public void testRestriksjonsOmrader() throws IOException {
        File file = new File("src/test/resources/1417_N50_RestriksjonsOmrader.sos");
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
        assertEquals(5, objtypes.size());
        assertEquals(45, count);
        ri.close();
    }

}
