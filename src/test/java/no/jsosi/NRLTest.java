package no.jsosi;

import java.io.File;
import java.io.FileInputStream;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.zip.ZipInputStream;

import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Point;

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
            while ((fi = ri.nextFeature()) != null) {
                assertNotNull(fi);
                assertNotNull(fi.getGeometry());
                count++;
                objtypes.add(fi.get("OBJTYPE").toString());
                keys.addAll(fi.getAttributeKeySet());
                featureById.put(fi.getId(), fi);
            }
            assertEquals(3, objtypes.size());
            assertEquals(318918, count);
            assertFalse(keys.contains(";opp"));
            assertFalse(keys.contains(";taubanen"));
            assertFalse(keys.contains("En av 16 master. Se arkiv"));
            assertEquals(keys.toString(), 51, keys.size());

            Feature f1 = featureById.get(Integer.valueOf(318160));
            assertNotNull(f1);
            assertFalse("...AKILDE:NRL;...GKILDE:NRL;bardun festet i bakken og \nopp til kabel over dalen(spenn)"
                    .equals(f1.get("INFORMASJON")));
            Object o = f1.get("INFORMASJON");
            System.out.println(o);
            System.out.println(f1);
            assertEquals("NRL", f1.get("AKILDE"));
            assertEquals("NRL", f1.get("GKILDE"));
            assertEquals("bardun festet i bakken og \nopp til kabel over dalen(spenn)", f1.get("INFORMASJON"));

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
            assertEquals("JA", f4.get("LINJESPENN"));
            Object lokalid = f4.get("LOKALID");
            assertNotNull(lokalid);
            assertTrue(lokalid instanceof Collection<?>);
            Collection<?> lokalidList = (Collection<?>) lokalid;
            assertEquals(3, lokalidList.size());
            Iterator<?> lokalidListIterator = lokalidList.iterator();
            assertEquals(Integer.valueOf(1076257), lokalidListIterator.next());
            assertEquals(Integer.valueOf(1076260), lokalidListIterator.next());
            assertEquals(Integer.valueOf(1076263), lokalidListIterator.next());

        } finally {
            IOUtils.silentClose(ri, zis, fis);
        }
    }

}
