package com.cobbletown.eggbadges;

/** Immutable resolved visual state; construction makes multiple backgrounds/icons impossible. */
public record BadgeState(Integer backgroundArgb, IconState icon, String gender,
                         GenderStyle genderStyle, Integer perfectIvCount, boolean hiddenAbility) {
    public enum Anchor { TOP_RIGHT, BOTTOM_LEFT }

    public record IconState(String glyph, int color, Anchor anchor) {
        public IconState(String glyph, int color) { this(glyph, color, Anchor.TOP_RIGHT); }
    }

    public BadgeState {
        genderStyle = genderStyle == null ? GenderStyle.CORNER : genderStyle;
    }

    /** Compatibility constructor for channel-only previews and older call sites. */
    public BadgeState(Integer backgroundArgb, IconState icon, String gender) {
        this(backgroundArgb, icon, gender, GenderStyle.CORNER, null, false);
    }

    public BadgeState(Integer backgroundArgb, IconState icon, String gender,
                      GenderStyle genderStyle, Integer perfectIvCount) {
        this(backgroundArgb, icon, gender, genderStyle, perfectIvCount, false);
    }

    public boolean empty() {
        return backgroundArgb == null && icon == null && perfectIvCount == null && !hiddenAbility
                && (gender == null || gender.isEmpty());
    }
}
