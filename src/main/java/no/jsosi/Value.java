package no.jsosi;

public class Value {

    private static final int MAXLENGTH_INT = Integer.valueOf(Integer.MAX_VALUE).toString().length() - 1;
    private static final int MAXLENGTH_LONG = Long.valueOf(Long.MAX_VALUE).toString().length() - 1;

    public static Object value(String value) {
        if (value == null) {
            return value;
        }
        if (value.length() == 0) {
            return value;
        }

        boolean isInteger = true;
        for (int i = 0; i < value.length(); i++) {
            char c = value.charAt(i);
            if (!Character.isDigit(c)) {
                isInteger = false;
            }
        }

        if (isInteger) {
            if (value.length() < MAXLENGTH_INT) {
                return Integer.parseInt(value);
            }
            if (value.length() < MAXLENGTH_LONG) {
                return Long.parseLong(value);
            }
        }

        return value;
    }

}
