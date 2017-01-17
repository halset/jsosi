package no.jsosi;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class Value {

    private static final Set<String> INT_COLUMNS;

    private static final Set<String> FLOAT_COLUMNS;

    static {
        INT_COLUMNS = Collections.unmodifiableSet(new HashSet<>(Arrays.asList("GATENR", "HUSNR", "LOKALID")));
        FLOAT_COLUMNS = Collections.unmodifiableSet(new HashSet<>(Arrays.asList("HØYDE", "VERTIKALUTSTREKNING")));
    }

    public static Object value(String key, String value) {
        if (value == null) {
            return value;
        }
        if (value.length() == 0) {
            return value;
        }

        if (INT_COLUMNS.contains(key)) {
            return Integer.valueOf(value);
        }

        if (FLOAT_COLUMNS.contains(key)) {
            return Float.valueOf(value);
        }

        return value;
    }

}
