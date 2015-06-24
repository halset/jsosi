package no.jsosi;

public class Tegnsett {
    
    private Tegnsett() {
        
    }
    
    public static String getCharsetForTegnsett(String characterSet) {
        
        if (characterSet.startsWith("ISO") && !characterSet.startsWith("ISO-")) {
            characterSet = characterSet.replace("ISO", "ISO-");
        }

        // fake this one for now. sorry
        if (characterSet.equals("ISO-8859-10") || characterSet.equalsIgnoreCase("ANSI")) {
            characterSet = "ISO-8859-1";
        }
        
        return characterSet;

    }

}
