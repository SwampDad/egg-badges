package com.cobbletown.eggbadges;

/** Automatic, informational indicators kept separate from the five custom rules. */
public final class AutoIndicatorConfig {
    public boolean show_gender = false;
    public boolean show_perfect_iv_count = false;
    /** Set only by the settings UI; protects an explicit user choice during later migrations. */
    public boolean automatic_preferences_established;
    public int minimum_perfect_ivs = 5;
    /** Advanced near-perfect threshold: 186 minus the six-IV sum. Zero disables it. */
    public int near_perfect_max_deficit = 1;
    /** Separate appearance choices keep automatic indicators independent of custom badges. */
    public String gender_presentation = "GLYPH";
    public String iv_count_presentation = "BACKGROUND";

    void normalize() {
        minimum_perfect_ivs = Math.max(1, Math.min(6, minimum_perfect_ivs));
        near_perfect_max_deficit = Math.max(0, Math.min(186, near_perfect_max_deficit));
        gender_presentation=IndicatorPresentation.from(gender_presentation).name();
        iv_count_presentation=IndicatorPresentation.from(iv_count_presentation).name();
    }
}
