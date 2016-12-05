package no.jsosi;

import junit.framework.TestCase;

public class ValueTest extends TestCase {

    public void testValue() {
        assertEquals("0123", Value.value("KOMM", "0123"));
        assertEquals(Integer.valueOf(123), Value.value("HØYDE", "0123"));
        assertEquals(Float.valueOf(123), Value.value("VERTIKALUTSTREKNING", "0123"));
    }
    
}
