package no.jsosi;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class Value {

    private static final Set<String> INT_COLUMNS;

    static {
        INT_COLUMNS = Collections.unmodifiableSet(new HashSet<>(Arrays.asList("HØYDE", "GATENR", "HUSNR")));
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

        return value;
    }

}
