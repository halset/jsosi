package no.jsosi;

import java.util.HashMap;
import java.util.Map;

public class Navntype {

    private final static Map<Integer, String> nameById = new HashMap<>();

    static {

        // copy from
        // http://www.kartverket.no/globalassets/standard/sosi-standarden-del-1-og-2/sosi-standarden/stedsnavn.pdf
        // and "fixed" using sed 's/^[ ]*/r(/g'|sed -E 's/[[:space:]]+/,"/'|sed
        // 's/$/");/g' and such

        r(1, "Berg");
        r(2, "Fjell");
        r(239, "Fjellside");
        r(3, "Fjellområde");
        r(246, "Skogområde");
        r(247, "Landskapsområde");
        r(4, "Hei");
        r(5, "Høyde");
        r(6, "Ås");
        r(7, "Rygg");
        r(8, "Haug");
        r(211, "Topp (fjelltopp/tind)");
        r(212, "Hylle (hjell)");
        r(245, "Fjellkant (aksel)");
        r(9, "Bakke");
        r(10, "Li");
        r(11, "Stup");
        r(12, "Vidde");
        r(13, "Slette");
        r(14, "Mo");
        r(15, "Dalføre");
        r(16, "Dal");
        r(17, "Botn");
        r(244, "Senkning");
        r(18, "Skar");
        r(19, "Juv");
        r(20, "Søkk");
        r(21, "Stein");
        r(260, "Grotte");
        r(22, "Heller");
        r(213, "Terrengdetalj");
    }

    private static void r(int id, String name) {
        nameById.put(Integer.valueOf(id), name);
    }

    public static String getName(int id) {
        return nameById.get(Integer.valueOf(id));
    }

}
