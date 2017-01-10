package no.jsosi;

import junit.framework.TestCase;

public class RefPatternTest extends TestCase {

    public void testKurve() {
        RefPattern p = new RefPattern();
        assertTrue(p.match('.', 0));
        assertTrue(p.match('K', 1));
        assertTrue(p.match('U', 2));
        assertTrue(p.match('R', 3));
        assertTrue(p.match('V', 4));
        assertTrue(p.match('E', 5));
        assertTrue(p.match(' ', 6));
        assertTrue(p.atEndOfMatch(6));
    }

    public void testBuep() {
        RefPattern p = new RefPattern();
        assertTrue(p.match('.', 0));
        assertTrue(p.match('B', 1));
        assertTrue(p.match('U', 2));
        assertTrue(p.match('E', 3));
        assertTrue(p.match('P', 4));
        assertTrue(p.match(' ', 5));
        assertTrue(p.atEndOfMatch(5));
    }

    public void testMix() {
        RefPattern p = new RefPattern();
        assertTrue(p.match('.', 0));
        assertTrue(p.match('K', 1));
        assertTrue(p.match('U', 2));
        assertFalse(p.match('E', 3));
        assertFalse(p.match('V', 4));
    }

}
