package com.cobbletown.eggbadges;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class PreviewStateTest {
    private static EggBadgeDraftModel draft() {
        EggBadgesConfig config = new EggBadgesConfig();
        config.resetInMemory();
        return new EggBadgeDraftModel(config);
    }

    @Test void iconOffShowsConfiguredBackgroundAndNoIcon() {
        SettingsSelection.Preview preview = SettingsSelection.preview(draft());
        assertEquals(EggBadgeRenderer.backgroundColor(AppearancePalette.BACKGROUND_GREEN),
                preview.configuredBackgroundArgb());
        assertNull(preview.indicators().backgroundArgb());
        assertNull(preview.indicators().icon());
        assertNull(preview.indicators().perfectIvCount());
    }

    @Test void fiveCircleKeepsConfiguredBackgroundIndependentOfLiveEligibility() {
        EggBadgeDraftModel draft = draft();
        SettingsSelection.selectMode(draft, SettingsSelection.IconMode.CIRCLE);
        draft.icon.color = AppearancePalette.CIRCLE_GOLD;
        SettingsSelection.Preview preview = SettingsSelection.preview(draft);
        assertNotNull(preview.indicators().icon());
        assertEquals(AppearancePalette.CIRCLE_GOLD, preview.indicators().icon().color());
        assertEquals(BadgeState.Anchor.TOP_RIGHT, preview.indicators().icon().anchor());
        assertNotNull(preview.configuredBackgroundArgb());
    }

    @Test void countUsesThreePerfectSampleAndKeepsConfiguredBackground() {
        EggBadgeDraftModel draft = draft();
        SettingsSelection.selectMode(draft, SettingsSelection.IconMode.COUNT);
        SettingsSelection.Preview preview = SettingsSelection.preview(draft);
        assertEquals(3, preview.indicators().perfectIvCount());
        assertNull(preview.indicators().icon());
        assertNotNull(preview.configuredBackgroundArgb());
    }

    @Test void filterSelectionAloneActivatesTheCirclePreview() {
        EggBadgeDraftModel draft = draft();
        SettingsSelection.selectFilter(draft, SettingsSelection.CircleFilter.FIVE, true);
        SettingsSelection.Preview preview = SettingsSelection.preview(draft);
        assertNotNull(preview.indicators().icon());
        assertEquals(AppearancePalette.CIRCLE_GOLD, preview.indicators().icon().color());
        assertNotNull(preview.configuredBackgroundArgb());
    }

    @Test void clearingTheActiveFilterRemovesTheCirclePreview() {
        EggBadgeDraftModel draft = draft();
        SettingsSelection.selectFilter(draft, SettingsSelection.CircleFilter.FIVE, true);
        SettingsSelection.selectFilter(draft, SettingsSelection.CircleFilter.FIVE, false);
        SettingsSelection.Preview preview = SettingsSelection.preview(draft);
        assertNull(preview.indicators().icon());
        assertNull(preview.indicators().perfectIvCount());
        assertNotNull(preview.configuredBackgroundArgb());
    }

    @Test void customSampleCoexistsWithConfiguredBackground() {
        EggBadgeDraftModel draft = draft();
        SettingsSelection.selectMode(draft, SettingsSelection.IconMode.CIRCLE);
        SettingsSelection.setFilter(draft, SettingsSelection.CircleFilter.CUSTOM);
        draft.icon.stats[0] = new StatCondition(StatCondition.Operator.EQ, 31);
        SettingsSelection.Preview preview = SettingsSelection.preview(draft);
        assertNotNull(preview.indicators().icon());
        assertNotNull(preview.configuredBackgroundArgb());
    }

    @Test void fiveThresholdIsRepresentedInTheSample() {
        EggBadgeDraftModel draft = draft();
        SettingsSelection.selectMode(draft, SettingsSelection.IconMode.CIRCLE);
        draft.icon.five_iv_restricted = true;
        draft.icon.five_iv_minimum = 30;
        SettingsSelection.Preview preview = SettingsSelection.preview(draft);
        assertNotNull(preview.indicators().icon());
    }

    @Test void genderAndHiddenAbilityPreviewUseTheirExistingIndicators() {
        EggBadgeDraftModel draft = draft();
        draft.gender.show = true;
        draft.showHiddenAbility = true;
        SettingsSelection.Preview preview = SettingsSelection.preview(draft);
        assertEquals("Female", preview.indicators().gender());
        assertEquals(GenderStyle.CORNER, preview.indicators().genderStyle());
        assertTrue(preview.indicators().hiddenAbility());
    }

    @Test void backgroundOffMeansNoFill() {
        EggBadgeDraftModel draft = draft();
        SettingsSelection.setBackgroundOff(draft);
        SettingsSelection.Preview preview = SettingsSelection.preview(draft);
        assertNull(preview.configuredBackgroundArgb());
    }

    @Test void eachBackgroundSelectionImmediatelyChangesPreviewFill() {
        EggBadgeDraftModel draft = draft();
        for (int color : SettingsSelection.backgroundColors()) {
            assertTrue(SettingsSelection.setBackground(draft, color));
            assertEquals(EggBadgeRenderer.backgroundColor(color),
                    SettingsSelection.preview(draft).configuredBackgroundArgb());
        }
    }

    @Test void previewHasASeparateNeutralSlotBaseAndSelectedSwatchesUseWhiteOnly() {
        assertEquals(0xFF8B8B8B, EggBadgeSettingsScreen.PREVIEW_NEUTRAL_SLOT);
        assertEquals(EggBadgeSettingsScreen.SELECTED_SWATCH_BORDER,
                EggBadgeSettingsScreen.swatchBorderColor(true));
        assertEquals(EggBadgeSettingsScreen.NO_SWATCH_BORDER,
                EggBadgeSettingsScreen.swatchBorderColor(false));
    }
}
