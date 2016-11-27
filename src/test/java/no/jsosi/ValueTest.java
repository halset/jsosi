package no.jsosi;

import junit.framework.TestCase;

public class ValueTest extends TestCase {

    public void testValue() {
        assertEquals("abc", Value.value("abc"));
        assertEquals(Integer.valueOf(123), Value.value("123"));
        assertEquals("0123", Value.value("0123"));
        assertEquals("1.2.3", Value.value("1.2.3"));
        assertEquals(Long.valueOf(20100701000000l), Value.value("20100701000000"));
    }
    
}
