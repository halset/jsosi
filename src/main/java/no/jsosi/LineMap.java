package no.jsosi;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.StringTokenizer;

/**
 * ...AKILDE:NRL;...GKILDE:NRL;bardun festet i bakken og \nopp til kabel over
 * dalen(spenn)
 */
public class LineMap {

    private LineMap() {
    }

    public static Map<String, Object> create(String key, Object value) {
        if (!(value instanceof String)) {
            return Collections.singletonMap(key, value);
        } else {
            String vs = value.toString();
            if (!(vs.contains(";") && vs.contains(":"))) {
                return Collections.singletonMap(key, value);
            } else {
                StringTokenizer st = new StringTokenizer(value.toString(), ";");
                Map<String, Object> map = new LinkedHashMap<>();
                while (st.hasMoreTokens()) {
                    String part = st.nextToken();
                    int p = part.indexOf(':');
                    if (part.startsWith(".") && p > 0) {
                        while (part.startsWith(".")) {
                            part = part.substring(1);
                            p--;
                        }
                        map.put(part.substring(0, p), part.substring(p + 1));
                    } else {
                        map.put(key, part);
                    }
                }
                return Collections.unmodifiableMap(map);
            }
        }
    }

}
