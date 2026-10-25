package com.cobbletown.eggbadges;

/** Experimental gender indicator presentation. Persisted by enum name. */
public enum GenderStyle {
    CORNER("Colored corner"), LETTERS("M/F letters");

    public final String label;

    GenderStyle(String label) { this.label = label; }

    public static GenderStyle from(String value) {
        if (value != null) for (GenderStyle style : values()) if (style.name().equalsIgnoreCase(value)) return style;
        return CORNER;
    }

    public GenderStyle next() { return values()[(ordinal() + 1) % values().length]; }
}
