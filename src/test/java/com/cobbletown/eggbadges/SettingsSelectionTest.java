package com.cobbletown.eggbadges;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class SettingsSelectionTest {
    private static EggBadgeDraftModel draft() {
        EggBadgesConfig config = new EggBadgesConfig();
        config.resetInMemory();
        return new EggBadgeDraftModel(config);
    }

    @Test void defaultsArePerfectGreenBackgroundAndIconOff() {
        EggBadgeDraftModel draft = draft();
        assertTrue(SettingsSelection.backgroundOn(draft));
        assertEquals(AppearancePalette.BACKGROUND_GREEN, draft.background.color);
        assertEquals(SettingsSelection.IconMode.OFF, SettingsSelection.mode(draft));
        assertFalse(draft.showPerfectIvCount);
        assertFalse(draft.gender.show);
        assertFalse(draft.showHiddenAbility);
    }

    @Test void activeModeClickTurnsEverythingOff() {
        EggBadgeDraftModel draft = draft();
        assertEquals(SettingsSelection.IconMode.CIRCLE,
                SettingsSelection.selectMode(draft, SettingsSelection.IconMode.CIRCLE));
        assertEquals(SettingsSelection.IconMode.OFF,
                SettingsSelection.selectMode(draft, SettingsSelection.IconMode.CIRCLE));
        assertFalse(draft.icon.configured);
        assertFalse(draft.showPerfectIvCount);
    }

    @Test void circleAndCountSwitchDirectlyAndPreserveCircleSettings() {
        EggBadgeDraftModel draft = draft();
        SettingsSelection.selectMode(draft, SettingsSelection.IconMode.CIRCLE);
        draft.icon.color = AppearancePalette.CIRCLE_GOLD;
        draft.icon.condition = BadgeCondition.CUSTOM;
        draft.icon.stats[0] = new StatCondition(StatCondition.Operator.GT, 29);
        draft.icon.five_iv_restricted = true;
        draft.icon.five_iv_minimum = 30;

        SettingsSelection.selectMode(draft, SettingsSelection.IconMode.COUNT);
        assertEquals(SettingsSelection.IconMode.COUNT, SettingsSelection.mode(draft));
        assertTrue(draft.showPerfectIvCount);
        assertFalse(draft.icon.configured);
        assertEquals(AppearancePalette.CIRCLE_GOLD, draft.icon.color);
        assertEquals(BadgeCondition.CUSTOM, draft.icon.condition);
        assertEquals(StatCondition.Operator.GT, draft.icon.stats[0].operator);

        SettingsSelection.selectMode(draft, SettingsSelection.IconMode.CIRCLE);
        assertEquals(SettingsSelection.IconMode.CIRCLE, SettingsSelection.mode(draft));
        assertFalse(draft.showPerfectIvCount);
        assertTrue(draft.icon.configured);
        assertEquals(AppearancePalette.CIRCLE_GOLD, draft.icon.color);
        assertEquals(BadgeCondition.CUSTOM, draft.icon.condition);
        assertTrue(draft.icon.five_iv_restricted);
        assertEquals(30, draft.icon.five_iv_minimum);
    }

    @Test void circleEnableUsesSafeDefaultsWhenLegacyFieldsAreUnsupported() {
        EggBadgeDraftModel draft = draft();
        draft.icon.condition = BadgeCondition.PERFECT_6;
        draft.icon.color = 0xFFFF5555;
        draft.icon.configured = false;
        SettingsSelection.selectMode(draft, SettingsSelection.IconMode.CIRCLE);
        assertEquals(BadgeCondition.FIVE_5, draft.icon.condition);
        assertEquals(AppearancePalette.CIRCLE_GOLD, draft.icon.color);
        assertEquals(IconShape.CIRCLE, draft.icon.shape);
    }

    @Test void backgroundSupportsOnlyTheApprovedPaletteAndOff() {
        EggBadgeDraftModel draft = draft();
        assertEquals(3, SettingsSelection.backgroundColors().length);
        assertTrue(SettingsSelection.setBackground(draft, AppearancePalette.BACKGROUND_BLUE));
        assertTrue(SettingsSelection.backgroundOn(draft));
        assertEquals(BadgeCondition.PERFECT_6, draft.background.condition);
        SettingsSelection.setBackgroundOff(draft);
        assertFalse(SettingsSelection.backgroundOn(draft));
        assertFalse(SettingsSelection.setBackground(draft, 0xFFFFD54A));
    }

    @Test void circleFiltersAreFiveOrCustom() {
        EggBadgeDraftModel draft = draft();
        SettingsSelection.selectMode(draft, SettingsSelection.IconMode.CIRCLE);
        SettingsSelection.setFilter(draft, SettingsSelection.CircleFilter.CUSTOM);
        assertEquals(BadgeCondition.CUSTOM, draft.icon.condition);
        SettingsSelection.setFilter(draft, SettingsSelection.CircleFilter.FIVE);
        assertEquals(BadgeCondition.FIVE_5, draft.icon.condition);
    }

    @Test void selectingAFilterActivatesTheCircleDirectlyAndClearsCount() {
        EggBadgeDraftModel draft = draft();
        draft.showPerfectIvCount = true;

        SettingsSelection.selectFilter(draft, SettingsSelection.CircleFilter.FIVE, true);
        assertEquals(SettingsSelection.IconMode.CIRCLE, SettingsSelection.mode(draft));
        assertTrue(draft.icon.configured);
        assertEquals(BadgeCondition.FIVE_5, draft.icon.condition);
        assertFalse(draft.showPerfectIvCount);

        SettingsSelection.selectFilter(draft, SettingsSelection.CircleFilter.CUSTOM, true);
        assertEquals(SettingsSelection.IconMode.CIRCLE, SettingsSelection.mode(draft));
        assertTrue(draft.icon.configured);
        assertEquals(BadgeCondition.CUSTOM, draft.icon.condition);
        assertFalse(draft.showPerfectIvCount);
    }

    @Test void clearingTheActiveFilterLeavesNoCircleOrCount() {
        EggBadgeDraftModel draft = draft();
        SettingsSelection.selectFilter(draft, SettingsSelection.CircleFilter.FIVE, true);
        SettingsSelection.selectFilter(draft, SettingsSelection.CircleFilter.FIVE, false);
        assertEquals(SettingsSelection.IconMode.OFF, SettingsSelection.mode(draft));
        assertFalse(draft.icon.configured);
        assertFalse(draft.showPerfectIvCount);

        SettingsSelection.selectFilter(draft, SettingsSelection.CircleFilter.CUSTOM, true);
        SettingsSelection.selectFilter(draft, SettingsSelection.CircleFilter.CUSTOM, false);
        assertEquals(SettingsSelection.IconMode.OFF, SettingsSelection.mode(draft));
        assertFalse(draft.icon.configured);
        assertFalse(draft.showPerfectIvCount);
    }

    @Test void clearingAnInactiveFilterDoesNotChangeTheActiveCircle() {
        EggBadgeDraftModel draft = draft();
        SettingsSelection.selectFilter(draft, SettingsSelection.CircleFilter.FIVE, true);
        SettingsSelection.selectFilter(draft, SettingsSelection.CircleFilter.CUSTOM, false);
        assertEquals(SettingsSelection.IconMode.CIRCLE, SettingsSelection.mode(draft));
        assertTrue(draft.icon.configured);
        assertEquals(BadgeCondition.FIVE_5, draft.icon.condition);
    }

    @Test void filterActivationPreservesSavedCircleColorThresholdAndStats() {
        EggBadgeDraftModel draft = draft();
        draft.icon.color = AppearancePalette.CIRCLE_WHITE;
        draft.icon.five_iv_restricted = true;
        draft.icon.five_iv_minimum = 30;
        draft.icon.stats[0] = new StatCondition(StatCondition.Operator.GT, 29);

        SettingsSelection.selectFilter(draft, SettingsSelection.CircleFilter.CUSTOM, true);
        assertEquals(AppearancePalette.CIRCLE_WHITE, draft.icon.color);
        assertEquals(StatCondition.Operator.GT, draft.icon.stats[0].operator);
        assertEquals(29, draft.icon.stats[0].value);

        SettingsSelection.selectFilter(draft, SettingsSelection.CircleFilter.FIVE, true);
        assertEquals(AppearancePalette.CIRCLE_WHITE, draft.icon.color);
        assertTrue(draft.icon.five_iv_restricted);
        assertEquals(30, draft.icon.five_iv_minimum);
        assertEquals(StatCondition.Operator.GT, draft.icon.stats[0].operator);
    }

    @Test void sixthIvDisplayUsesExclusiveAboveValue() {
        ChannelConfig channel = ChannelConfig.iconDefault();
        assertEquals(29, SettingsSelection.fiveIvAbove(channel));
        SettingsSelection.setFiveIvAbove(channel, 30);
        assertEquals(31, channel.five_iv_minimum);
        assertEquals(30, SettingsSelection.fiveIvAbove(channel));
        SettingsSelection.setFiveIvAbove(channel, 0);
        assertEquals(1, channel.five_iv_minimum);
        assertEquals(0, SettingsSelection.fiveIvAbove(channel));
    }

    @Test void colorPalettesMatchTheApprovedOptions() {
        assertArrayEquals(new int[]{AppearancePalette.BACKGROUND_GREEN,
                AppearancePalette.BACKGROUND_BLUE, AppearancePalette.BACKGROUND_PURPLE},
                SettingsSelection.backgroundColors());
        assertArrayEquals(new int[]{AppearancePalette.CIRCLE_GOLD,
                AppearancePalette.CIRCLE_CYAN, AppearancePalette.CIRCLE_WHITE},
                SettingsSelection.circleColors());
    }
}
