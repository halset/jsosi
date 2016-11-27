package no.jsosi;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class Navntype {

    private final static Map<Integer, String> nameById = new HashMap<>();

    static {

        // copy from
        // http://www.kartverket.no/globalassets/standard/sosi-standarden-del-1-og-2/sosi-standarden/stedsnavn.pdf
        // and "fixed" using sed 's/^[ ]*/r(/g'|sed -E 's/[[:space:]]+/,"/'|sed
        // 's/$/");/g' and such
        
        
        // https://github.com/phaza/norwegian-ssr-parser/blob/master/src/SSR/Feature/NameType.php

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

        r(84, "Øy i sjø");
        r(216, "Øygruppe i sjø");
        r(85, "Holme i sjø");
        r(265, "Holmegruppe i sjø");
        r(86, "Halvøy i sjø");
        r(87, "Nes i sjø");
        r(88, "Eid i sjø");
        r(89, "Strand i sjø");
        r(90, "Skjær i sjø");
        r(91, "Båe i sjø");
        r(92, "Grunne i sjø");
        r(93, "Renne");
        r(94, "Banke i sjø");
        r(95, "Bakke i sjø");
        r(96, "Søkk i sjø");
        r(97, "Dyp (havdyp)");
        r(98, "Rygg i sjø");
        r(99, "Egg");
        r(217, "Klakk");
        r(256, "Fiskeplass");
        r(100, "By");
        r(132, "Bydel");
        r(101, "Tettsted");
        r(266, "Tettsteddel");
        r(102, "Tettbebyggelse");
        r(103, "Bygdelag (bygd)");
        r(104, "Grend");
        r(105, "Boligfelt");
        r(228, "Hyttefelt");
        r(106, "Borettslag");
        r(107, "Industriområde");
        r(280, "Gard");
        r(108, "Bruk (gardsbruk)");
        r(109, "Enebolig/mindre boligbygg (villa)");
        r(259, "Boligblokk");
        r(110, "Fritidsbolig (hytte, sommerhus)");
        r(111, "Seter (sel, støl)");
        r(112, "Bygg for jordbruk, fiske og fangst");
        r(113, "Fabrikk");
        r(114, "Kraftstasjon");
        r(218, "Bergverk (underjord./dagbrudd)");
        r(115, "Verksted");
        r(116, "Forretningsbygg");
        r(117, "Hotell");
        r(118, "Pensjonat");
        r(119, "Turisthytte");
        r(267, "Serveringssted");
        r(191, "Campingplass");

        r(270, "Adm. tettsted");
        r(183, "Sogn");
        r(184, "Grunnkrets");
        r(185, "Allmenning");
        r(186, "Skytefelt");
        r(187, "Verneområder");
        r(188, "Soneinndeling til havs");
        r(253, "Annen adm. Inndeling");
        r(258, "Grensemerke");
        r(190, "Idrettsanlegg");
        r(210, "Skytebane");
        r(235, "Gravplass");
        r(192, "Skiheis");
        r(193, "Fjellheis");
        r(194, "Slalåm- og utforbakke");
        r(195, "Småbåthavn");
        r(222, "Badeplass");
        r(223, "Fornøyelsespark");
        r(196, "Rørledning");
        r(197, "Oljeinstallasjon (Sjø)");
        r(198, "Kraftledning");
        r(199, "Kraftgate (Rørgate)");
        r(200, "Kabel");
        r(201, "Dam");
        r(202, "Tømmerrenne");
        r(203, "Taubane");
        r(204, "Fløtningsannlegg");
        r(205, "Fiskeoppdrettsanlegg");
        r(206, "Gammel bosettingsplass");
        r(207, "Offersted");
        r(208, "Severdighet");
        r(209, "Utsiktspunkt");
        r(224, "Melkeplass");
        r(225, "Annen kulturdetalj");
        r(233, "Gjerde");
        r(234, "Stø");
        r(254, "Varde");
        r(272, "Lanterne");
        r(273, "Stang");
        r(274, "Stake");
        r(275, "Lysbøye");
        r(276, "Båke");
        r(277, "Overett");

    }

    private static void r(int id, String name) {
        nameById.put(Integer.valueOf(id), name);
    }

    public static String getName(int id) {
        return nameById.get(Integer.valueOf(id));
    }

    public static Set<Integer> getIds() {
        return Collections.unmodifiableSet(nameById.keySet());
    }

    public static Map<Integer, String> getNameById() {
        return Collections.unmodifiableMap(nameById);
    }
    
}
