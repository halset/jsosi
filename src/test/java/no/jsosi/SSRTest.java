package no.jsosi;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import junit.framework.TestCase;

public class SSRTest extends TestCase {

    public void testSSR() throws IOException {
        File file = new File("src/test/resources/0540_Navn_utf8.sos");
        assertTrue(file.canRead());
        SosiReader ri = new SosiReader(file);

        Feature fi = null;
        Map<Integer, Feature> featureById = new HashMap<>();
        while ((fi = ri.nextFeature()) != null) {
            assertNotNull(fi);
            assertNotNull(fi.getGeometry());
            featureById.put(fi.getId(), fi);
        }

        Feature f1 = featureById.get(Integer.valueOf(117285));
        assertNotNull(f1);
        assertEquals("Morud", f1.get("STRENG"));
        assertEquals("108", f1.get("NAVNTYPE"));

        Object o = f1.get("SSR");
        assertNotNull(o);
        assertTrue(o.toString(), o instanceof Map);
        Map<?, ?> m = (Map<?, ?>) o;
        assertEquals(2, m.size());
        assertEquals("Morud", m.get("SNAVN"));
        assertEquals("114192", m.get("SSR-ID"));

        ri.close();
    }

    @SuppressWarnings("unchecked")
    public void testSSRSydalsfjellet() throws IOException {
        File file = new File("src/test/resources/SSR-Sydalsfjellet.sos");
        assertTrue(file.canRead());
        SosiReader ri = new SosiReader(file);

        Feature fi = ri.nextFeature();
        assertNotNull(fi);
        
        List<?> stedsnavn = (List<?>) fi.get("STEDSNAVN");
        assertNotNull(stedsnavn);
        assertEquals(2, stedsnavn.size());
        
        Map<String, Object> stedsnavn0 = (Map<String, Object>) stedsnavn.get(0);
        Map<String, Object> skrivemate0 = (Map<String, Object>) stedsnavn0.get("SKRIVEMÅTE");
        assertEquals("Svarttinden", skrivemate0.get("LANGNAVN"));

        Map<String, Object> stedsnavn1 = (Map<String, Object>) stedsnavn.get(1);
        Map<String, Object> skrivemate1 = (Map<String, Object>) stedsnavn1.get("SKRIVEMÅTE");
        assertEquals("Sydalsfjellet", skrivemate1.get("LANGNAVN"));

        fi = ri.nextFeature();
        assertNull(fi);

        ri.close();
    }

    public void testSSRKurveWithoutAttributes() throws IOException {
        File file = new File("src/test/resources/SSR-KurveWithoutAttributes.sos");
        assertTrue(file.canRead());
        SosiReader ri = new SosiReader(file);

        Feature fi = null;
        while ((fi = ri.nextFeature()) != null) {
            assertNotNull(fi);
            assertNotNull(fi.getGeometry());
            assertNotNull(fi.getAttributeMap());
        }

        ri.close();
    }

}
