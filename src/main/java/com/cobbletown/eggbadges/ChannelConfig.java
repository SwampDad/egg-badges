package com.cobbletown.eggbadges;

/** Persisted background or icon channel. There is intentionally one instance per channel. */
public final class ChannelConfig {
    public boolean configured;
    public BadgeCondition condition;
    public int color;
    public IconShape shape;
    /** Inclusive sixth-IV threshold when {@link #five_iv_restricted} is true. */
    public int five_iv_minimum = 30;
    /** Whether the sixth-IV restriction is active; the value is preserved when off. */
    public boolean five_iv_restricted = false;
    public StatCondition[] stats = defaults();

    /** New-install / Reset All background: green fill for an exact 6x31 egg. */
    public static ChannelConfig backgroundDefault() {
        ChannelConfig c = new ChannelConfig();
        c.configured = true;
        c.condition = BadgeCondition.PERFECT_6;
        c.color = AppearancePalette.BACKGROUND_GREEN;
        c.shape = IconShape.CIRCLE;
        return c;
    }

    /**
     * New-install / Reset All icon: 5x31 highlighting starts OFF. The suggested standard
     * appearance is a blue dot, applied when the quick toggle is turned on.
     */
    public static ChannelConfig iconDefault() {
        ChannelConfig c = new ChannelConfig();
        c.configured = false;
        c.condition = BadgeCondition.FIVE_5;
        c.color = AppearancePalette.CIRCLE_GOLD;
        c.shape = IconShape.CIRCLE;
        c.five_iv_minimum = 30;
        return c;
    }

    static StatCondition[] defaults() {
        StatCondition[] rows = new StatCondition[6];
        for (int i = 0; i < 6; i++) rows[i] = new StatCondition();
        return rows;
    }

    public ChannelConfig copy() {
        ChannelConfig c = new ChannelConfig();
        c.configured = configured;
        c.condition = condition;
        c.color = color;
        c.shape = shape;
        c.five_iv_minimum = five_iv_minimum;
        c.five_iv_restricted = five_iv_restricted;
        c.stats = defaults();
        if (stats != null) for (int i = 0; i < Math.min(6, stats.length); i++)
            if (stats[i] != null) c.stats[i] = stats[i].copy();
        return c;
    }

    public void normalize(boolean background) {
        if (condition == null || (background && condition == BadgeCondition.IV_COUNT)
                || (!background && (condition == BadgeCondition.GENDER || condition == BadgeCondition.IV_COUNT)))
            condition = background ? BadgeCondition.PERFECT_6 : BadgeCondition.FIVE_5;
        if (shape == null) shape = IconShape.CIRCLE;
        if (!background) shape = IconShape.CIRCLE;
        if (five_iv_minimum < 0 || five_iv_minimum > 31) {
            five_iv_minimum = 30;
            five_iv_restricted = false;
        }
        if (five_iv_minimum == 0) {
            five_iv_minimum = 30;
            five_iv_restricted = false;
        }
        if (stats == null || stats.length != 6) {
            StatCondition[] old = stats;
            stats = defaults();
            if (old != null) for (int i = 0; i < Math.min(6, old.length); i++)
                if (old[i] != null) stats[i] = old[i];
        }
        for (int i = 0; i < 6; i++) {
            if (stats[i] == null) stats[i] = new StatCondition();
            if (stats[i].operator == null) stats[i].operator = StatCondition.Operator.ANY;
            if (stats[i].operator == StatCondition.Operator.ANY) stats[i].value = -1;
            else if (stats[i].value < 0 || stats[i].value > 31) {
                stats[i].operator = StatCondition.Operator.ANY;
                stats[i].value = -1;
            }
        }
    }

    boolean sameAs(ChannelConfig other) {
        if (other == null || configured != other.configured || condition != other.condition
                || color != other.color || shape != other.shape
                || five_iv_minimum != other.five_iv_minimum
                || five_iv_restricted != other.five_iv_restricted) return false;
        for (int i = 0; i < 6; i++) {
            StatCondition a = stats != null && i < stats.length ? stats[i] : null;
            StatCondition b = other.stats != null && i < other.stats.length ? other.stats[i] : null;
            if (a == null && b == null) continue;
            if (a == null || b == null) return false;
            if (a.operator != b.operator || (a.operator != StatCondition.Operator.ANY && a.value != b.value)) return false;
        }
        return true;
    }
}
