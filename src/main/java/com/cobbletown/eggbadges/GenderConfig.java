package com.cobbletown.eggbadges;

/** Independent gender indicator settings. Colors remain fixed; there is no color editor. */
public final class GenderConfig {
    public boolean show = false;
    public int male_color = 0xFF5599FF;
    public int female_color = 0xFFFF5555;
    /** Persisted {@link GenderStyle} name. */
    public String style = GenderStyle.CORNER.name();

    public GenderConfig copy() {
        GenderConfig c = new GenderConfig();
        c.show = show;
        c.male_color = male_color;
        c.female_color = female_color;
        c.style = style;
        return c;
    }

    void normalize() { style = GenderStyle.from(style).name(); }

    boolean sameAs(GenderConfig other) {
        return other != null && show == other.show && male_color == other.male_color
                && female_color == other.female_color && GenderStyle.from(style) == GenderStyle.from(other.style);
    }
}
