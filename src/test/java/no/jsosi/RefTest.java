package no.jsosi;

import junit.framework.TestCase;

public class RefTest extends TestCase {

    public void testCreate() {
        assertRef(123, true, Ref.create(":123"));
        assertRef(123, false, Ref.create(":-123"));
        assertRef(123, true, Ref.create(" : 123"));
        assertRef(123, false, Ref.create(" : - 123"));
        assertRef(123, true, Ref.create(" : 123 "));
        assertRef(123, true, Ref.create("123"));
        assertRef(123, false, Ref.create("-123"));
    }

    public void testToString() {
        assertEquals(":123", Ref.create(":123").toString());
        assertEquals(":-123", Ref.create(":-123").toString());
    }

    private void assertRef(int id, boolean forward, Ref ref) {
        assertNotNull(ref);
        assertEquals(id, ref.getId());
        assertEquals(forward, ref.isForward());
    }

}
