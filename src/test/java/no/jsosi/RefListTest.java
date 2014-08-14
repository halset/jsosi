package no.jsosi;

import junit.framework.TestCase;

public class RefListTest extends TestCase {

    public void testAdd() {
        RefList rl = new RefList();
        rl.add(":123 :-234");
        assertEquals(2, rl.getExteriour().size());
        assertEquals(0, rl.getNumOfHoles());
        assertEquals(123, rl.getExteriour().get(0).getId());
        assertTrue(rl.getExteriour().get(0).isForward());
        assertEquals(234, rl.getExteriour().get(1).getId());
        assertFalse(rl.getExteriour().get(1).isForward());

        rl.add(":456 (:-333 :444");
        assertEquals(3, rl.getExteriour().size());
        assertEquals(1, rl.getNumOfHoles());
        assertEquals(2, rl.getHole(0).size());

        rl.add(":222)");
        assertEquals(3, rl.getExteriour().size());
        assertEquals(1, rl.getNumOfHoles());
        assertEquals(3, rl.getHole(0).size());
        assertEquals(222, rl.getHole(0).get(2).getId());
        assertTrue(rl.getHole(0).get(2).isForward());
    }

    public void testToString() {
        String s = ":12825 :12827 :-10907 :-11823 :13931 :-10902 :-8723 :-12877 :-7157 "
                + ":9745 :-9755 :9756 :-11753 :13864 :5855 :10853 :-10861 (:-5857 :-12820 :-8670 "
                + ":-8671 :-12821 :-10856 :-5858 :-12822 :-10855 :-10854 :-7099) (:-9759) (:11754)";
        RefList rl = new RefList();
        rl.add(s);
        assertEquals(s, rl.toString());
    }

    public void testIsEmpty() {
        RefList rl = new RefList();
        assertTrue(rl.isEmpty());
        rl.add(":123");
        assertFalse(rl.isEmpty());
    }

}
