package no.jsosi;

import java.util.Map;

import junit.framework.TestCase;

public class LineMapTest extends TestCase {

    public void testNRLNewline() {
        Map<String, Object> m = LineMap.create("INFORMASJON",
                "...AKILDE:NRL;...GKILDE:NRL;bardun festet i bakken og \nopp til kabel over dalen(spenn)");
        assertEquals(3, m.size());
        assertEquals("NRL", m.get("AKILDE"));
        assertEquals("NRL", m.get("GKILDE"));
        assertEquals("bardun festet i bakken og \nopp til kabel over dalen(spenn)", m.get("INFORMASJON"));
    }

    public void testNRLColon() {
        Map<String, Object> m = LineMap.create("INFORMASJON",
                "...AKILDE:NRL;...GKILDE:NRL;En av 16 master. Se arkiv: ID 54131");
        assertEquals(3, m.size());
        assertEquals("NRL", m.get("AKILDE"));
        assertEquals("NRL", m.get("GKILDE"));
        assertEquals("En av 16 master. Se arkiv: ID 54131", m.get("INFORMASJON"));
    }

}
