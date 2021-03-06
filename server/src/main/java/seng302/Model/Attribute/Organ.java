package seng302.Model.Attribute;

/**
 * An enum representing the different organ types.
 */
public enum Organ {
    LIVER("liver"), KIDNEY("kidney"), PANCREAS("pancreas"), HEART("heart"), LUNG("lung"), INTESTINE("intestine"), CORNEA("cornea"), EAR
            ("middle-ear"), SKIN("skin"), BONE("bone-marrow"), TISSUE("connective-tissue");

    private String organ;

    /**
     * constructor method to create a new organ attribute for a user
     * input can be of type: liver, kidney, pancreas, heart, lung, intestine, cornea, middle-ear, skin, bone-marrow, or connective-tissue
     * @param organ String the type of organ being added to a user donations or waiting list
     */
    Organ(String organ) { this.organ = organ; }

    public String toString() {
        return organ;
    }

    /**
     * Converts a string to its corresponding organ enum value. Throws an IllegalArgumentException if the string does not match any organ.
     *
     * @param text The string to convert
     * @return The organ corresponding to the input string
     */
    public static Organ parse(String text) {
        for (Organ organ : Organ.values()) {
            if (organ.toString().equalsIgnoreCase(text)) {
                return organ;
            }
        }
        throw new IllegalArgumentException("Organ not recognised");
    }

    /**
     * Change the first letter of a string to a capital letter.
     *
     * @param s The string to capitalise
     * @return The capitalised string
     */
    public static String capitalise(String s) {
        return s.substring(0, 1).toUpperCase() + s.substring(1);
    }
}


