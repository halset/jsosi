package no.jsosi;

import junit.framework.TestCase;

public class AttributeMapTest extends TestCase {

    public void testNRLNewline() {
        AttributeMap m = new AttributeMap();
        m.add("INFORMASJON", "...AKILDE:NRL;...GKILDE:NRL;bardun festet i bakken og \nopp til kabel over dalen(spenn)");
        m.computeSubValues();
        assertEquals(3, m.keySet().size());
        assertEquals("NRL", m.get("AKILDE"));
        assertEquals("NRL", m.get("GKILDE"));
        assertEquals("bardun festet i bakken og \nopp til kabel over dalen(spenn)", m.get("INFORMASJON"));
    }

    public void testNRLColon() {
        AttributeMap m = new AttributeMap();
        m.add("INFORMASJON", "...AKILDE:NRL;...GKILDE:NRL;En av 16 master. Se arkiv: ID 54131");
        m.computeSubValues();
        assertEquals(3, m.keySet().size());
        assertEquals("NRL", m.get("AKILDE"));
        assertEquals("NRL", m.get("GKILDE"));
        assertEquals("En av 16 master. Se arkiv: ID 54131", m.get("INFORMASJON"));
    }

}
