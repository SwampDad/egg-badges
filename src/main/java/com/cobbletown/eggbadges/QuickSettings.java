package com.cobbletown.eggbadges;

/**
 * Maps the four main-screen quick toggles onto the same two channel objects used by the
 * advanced editors. A configured channel with a different condition is "Custom", never a
 * misleading standard ON.
 */
public final class QuickSettings {
    public enum State { OFF, ON, CUSTOM }

    public static final int STANDARD_SIX_COLOR = AppearancePalette.BACKGROUND_GREEN;
    public static final int STANDARD_FIVE_COLOR = AppearancePalette.CIRCLE_GOLD;

    private QuickSettings() {}

    public static State sixState(ChannelConfig background) {
        if (background == null || !background.configured) return State.OFF;
        return background.condition == BadgeCondition.PERFECT_6 ? State.ON : State.CUSTOM;
    }

    public static State fiveState(ChannelConfig icon) {
        if (icon == null || !icon.configured) return State.OFF;
        return icon.condition == BadgeCondition.FIVE_5 ? State.ON : State.CUSTOM;
    }

    public static void setSix(ChannelConfig background, boolean enabled) {
        if (background == null) return;
        if (enabled) {
            if (!background.configured) {
                background.condition = BadgeCondition.PERFECT_6;
                background.color = STANDARD_SIX_COLOR;
                background.shape = IconShape.CIRCLE;
            }
            background.configured = true;
        } else if (background.condition == BadgeCondition.PERFECT_6) {
            background.configured = false;
        }
    }

    public static void setFive(ChannelConfig icon, boolean enabled) {
        if (icon == null) return;
        if (enabled) {
            if (!icon.configured) {
                icon.condition = BadgeCondition.FIVE_5;
                icon.color = STANDARD_FIVE_COLOR;
                icon.shape = IconShape.CIRCLE;
                icon.five_iv_minimum = -1;
            }
            icon.configured = true;
        } else if (icon.condition == BadgeCondition.FIVE_5) {
            icon.configured = false;
        }
    }

    public static String toggleLabel(String name, State state) {
        return name + ": " + (state == State.CUSTOM ? "Custom…" : state == State.ON ? "On" : "Off");
    }
}
