package no.jsosi;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class Koordsys {

	private static final Map<Integer, String> epsgByKoordsys;

	static {
		// http://www.kartverket.no/Documents/Standard/SOSI-standarden%20del%201%20og%202/SOSI%20standarden/del1_2_RealiseringSosiGml_45_20120608.pdf
		Map<Integer, String> m = new HashMap<Integer, String>();
		for (int i = 1; i <= 8; i++) {
			m.put(i, "EPSG:2739" + i);
		}
		for (int i = 19; i <= 26; i++) {
			m.put(i, "EPSG:258" + (i + 10));
		}
		for (int i = 31; i <= 36; i++) {
			m.put(i, "EPSG:230" + i);
		}
		for (int i = 59; i <= 66; i++) {
			m.put(i, "EPSG:326" + (i - 30));
		}
		epsgByKoordsys = Collections.unmodifiableMap(m);
	}
	
	public static String getEpsgForKoordsys(Integer koordsys) {
		return epsgByKoordsys.get(koordsys);
	}

}
