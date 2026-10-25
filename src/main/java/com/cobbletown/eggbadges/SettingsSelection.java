package com.cobbletown.eggbadges;

/** Simplified Background + Icon mode model over the existing two channels. */
final class SettingsSelection {
    enum IconMode { OFF, CIRCLE, COUNT }
    enum CircleFilter { FIVE, CUSTOM }

    /**
     * Preview-only composition. Its slot background deliberately represents the user's
     * selection, not whether the representative egg happens to qualify in live inventory.
     */
    record Preview(Integer configuredBackgroundArgb, BadgeState indicators) {}

    private SettingsSelection() {}

    static IconMode mode(EggBadgeDraftModel draft) {
        if (draft == null) return IconMode.OFF;
        if (draft.showPerfectIvCount) return IconMode.COUNT;
        if (draft.icon.configured) return IconMode.CIRCLE;
        return IconMode.OFF;
    }

    static IconMode toggle(IconMode current, IconMode clicked) {
        return current == clicked ? IconMode.OFF : clicked;
    }

    /** Applies a mode; subordinate circle values are retained while inactive. */
    static IconMode selectMode(EggBadgeDraftModel draft, IconMode clicked) {
        if (draft == null || clicked == null) return IconMode.OFF;
        IconMode next = toggle(mode(draft), clicked);
        switch (next) {
            case OFF -> {
                draft.showPerfectIvCount = false;
                draft.icon.configured = false;
            }
            case COUNT -> {
                draft.showPerfectIvCount = true;
                draft.icon.configured = false;
            }
            case CIRCLE -> {
                draft.showPerfectIvCount = false;
                draft.icon.configured = true;
                draft.icon.shape = IconShape.CIRCLE;
                if (draft.icon.condition != BadgeCondition.FIVE_5
                        && draft.icon.condition != BadgeCondition.CUSTOM) {
                    draft.icon.condition = BadgeCondition.FIVE_5;
                }
                ensureCircleDefaults(draft);
            }
        }
        return next;
    }

    static boolean circleControlsEnabled(EggBadgeDraftModel draft) {
        return mode(draft) == IconMode.CIRCLE;
    }

    static boolean backgroundOn(EggBadgeDraftModel draft) {
        return draft != null && draft.background.configured
                && draft.background.condition == BadgeCondition.PERFECT_6;
    }

    static boolean setBackground(EggBadgeDraftModel draft, int color) {
        if (draft == null || !AppearancePalette.supportedBackground(color)) return false;
        draft.background.configured = true;
        draft.background.condition = BadgeCondition.PERFECT_6;
        draft.background.color = AppearancePalette.canonicalBackground(color);
        return true;
    }

    static void setBackgroundOff(EggBadgeDraftModel draft) {
        if (draft != null) draft.background.configured = false;
    }

    static CircleFilter filter(EggBadgeDraftModel draft) {
        return draft != null && draft.icon.condition == BadgeCondition.CUSTOM
                ? CircleFilter.CUSTOM : CircleFilter.FIVE;
    }

    static void setFilter(EggBadgeDraftModel draft, CircleFilter circleFilter) {
        selectFilter(draft, circleFilter, true);
    }

    /**
     * Selecting a filter directly activates the circle. Clearing the active filter leaves
     * neither circle nor count active; clearing an inactive filter changes nothing.
     */
    static void selectFilter(EggBadgeDraftModel draft, CircleFilter circleFilter, boolean selected) {
        if (draft == null || circleFilter == null) return;
        if (!selected) {
            if (mode(draft) == IconMode.CIRCLE && filter(draft) == circleFilter) {
                draft.icon.configured = false;
                draft.showPerfectIvCount = false;
            }
            return;
        }
        draft.icon.condition = circleFilter == CircleFilter.CUSTOM
                ? BadgeCondition.CUSTOM : BadgeCondition.FIVE_5;
        draft.icon.shape = IconShape.CIRCLE;
        draft.icon.configured = true;
        draft.showPerfectIvCount = false;
        ensureCircleDefaults(draft);
    }

    private static void ensureCircleDefaults(EggBadgeDraftModel draft) {
        if (!AppearancePalette.supportedCircle(draft.icon.color)) {
            draft.icon.color = AppearancePalette.CIRCLE_GOLD;
        } else {
            draft.icon.color = AppearancePalette.canonicalCircle(draft.icon.color);
        }
        if (draft.icon.five_iv_minimum < 0 || draft.icon.five_iv_minimum > 31) {
            draft.icon.five_iv_minimum = 30;
            draft.icon.five_iv_restricted = false;
        }
    }

    static int[] backgroundColors() {
        return AppearancePalette.BACKGROUNDS.clone();
    }

    static int[] circleColors() {
        return AppearancePalette.CIRCLES.clone();
    }

    static int fiveIvAbove(ChannelConfig channel) {
        if (channel == null) return 29;
        return Math.max(0, Math.min(30, channel.five_iv_minimum - 1));
    }

    static void setFiveIvAbove(ChannelConfig channel, int above) {
        if (channel == null) return;
        channel.five_iv_minimum = Math.max(1, Math.min(31, above + 1));
    }

    /** Representative sample follows the selected icon mode without changing live eligibility rules. */
    static Preview preview(EggBadgeDraftModel draft) {
        if (draft == null) return new Preview(null, new BadgeState(null, null, ""));
        IconMode mode = mode(draft);
        int[] ivs;
        switch (mode) {
            case COUNT -> ivs = new int[]{31, 31, 31, 0, 0, 0};
            case CIRCLE -> ivs = filter(draft) == CircleFilter.CUSTOM
                    ? customSample(draft.icon) : fiveSample(draft.icon);
            default -> ivs = new int[]{31, 31, 31, 31, 31, 31};
        }
        if (ivs == null) ivs = new int[]{31, 31, 31, 31, 31, 31};

        EggAttributes attributes = new EggAttributes("", "", "", "Female", ivs, "",
                draft.showHiddenAbility ? HiddenAbilityStatus.HIDDEN : HiddenAbilityStatus.NORMAL);

        // This is a settings sample, so background selection remains visible with a 3-IV
        // count or five-IV circle even though live slots still require a perfect egg.
        Integer backgroundArgb = backgroundOn(draft)
                ? EggBadgeRenderer.backgroundColor(draft.background.color) : null;

        BadgeState.IconState icon = null;
        if (mode == IconMode.CIRCLE && draft.icon.configured
                && EggClassifier.matches(draft.icon, attributes)) {
            icon = new BadgeState.IconState(IconShape.CIRCLE.glyph, draft.icon.color,
                    BadgeState.Anchor.TOP_RIGHT);
        }
        Integer perfectly = mode == IconMode.COUNT ? 3 : null;
        String gender = draft.gender.show ? "Female" : "";
        BadgeState state = new BadgeState(null, icon, gender,
                GenderStyle.from(draft.gender.style), perfectly, draft.showHiddenAbility);
        return new Preview(backgroundArgb, state);
    }

    private static int[] fiveSample(ChannelConfig icon) {
        int remainder = 30;
        if (icon != null && icon.five_iv_restricted && icon.five_iv_minimum >= 0)
            remainder = Math.max(0, Math.min(30, icon.five_iv_minimum));
        return new int[]{31, 31, 31, 31, 31, remainder};
    }

    private static int[] customSample(ChannelConfig icon) {
        if (icon == null || icon.stats == null) return null;
        int[] ivs = {31, 31, 31, 31, 31, 31};
        for (int i = 0; i < Math.min(6, icon.stats.length); i++) {
            StatCondition condition = icon.stats[i];
            if (condition == null || condition.operator == null
                    || condition.operator == StatCondition.Operator.ANY) continue;
            switch (condition.operator) {
                case GT -> {
                    if (condition.value >= 31) return null;
                    ivs[i] = condition.value + 1;
                }
                case LT -> {
                    if (condition.value <= 0) return null;
                    ivs[i] = 0;
                }
                case EQ -> ivs[i] = condition.value;
                default -> { }
            }
        }
        EggAttributes attributes = new EggAttributes("", "", "", "Female", ivs);
        if (!EggClassifier.matches(icon, attributes)) return null;
        return ivs;
    }
}
