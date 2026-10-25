package com.cobbletown.eggbadges;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class DraftDirtyStateTest {
    private static EggBadgesConfig defaults() {
        EggBadgesConfig config = new EggBadgesConfig();
        config.resetInMemory();
        return config;
    }

    @Test void openingAndClosingWithoutEditsLeavesSaveDisabled() {
        EggBadgesConfig config = defaults();
        EggBadgeDraftModel draft = new EggBadgeDraftModel(config);
        assertFalse(draft.differsFrom(config));
    }

    @Test void changingQuickSettingsMarksDirtyAndChangingBackClearsIt() {
        EggBadgesConfig config = defaults();
        EggBadgeDraftModel draft = new EggBadgeDraftModel(config);

        draft.showPerfectIvCount = true;
        assertTrue(draft.differsFrom(config));
        draft.showPerfectIvCount = false;
        assertFalse(draft.differsFrom(config));

        draft.gender.show = true;
        assertTrue(draft.differsFrom(config));
        draft.gender.show = false;
        assertFalse(draft.differsFrom(config));

        draft.background.color = AppearancePalette.BACKGROUND_PURPLE;
        assertTrue(draft.differsFrom(config));
        draft.background.color = AppearancePalette.BACKGROUND_GREEN;
        assertFalse(draft.differsFrom(config));
    }

    @Test void badgeAndBackgroundSelectionChangesParticipateInDirtyState() {
        EggBadgesConfig config = defaults();
        EggBadgeDraftModel draft = new EggBadgeDraftModel(config);
        SettingsSelection.selectMode(draft, SettingsSelection.IconMode.CIRCLE);
        assertTrue(draft.differsFrom(config));
        SettingsSelection.selectMode(draft, SettingsSelection.IconMode.CIRCLE);
        assertFalse(draft.differsFrom(config));

        SettingsSelection.setBackgroundOff(draft);
        assertTrue(draft.differsFrom(config));
        SettingsSelection.setBackground(draft, AppearancePalette.BACKGROUND_GREEN);
        assertFalse(draft.differsFrom(config));
    }

    @Test void directFilterActivationIsDraftOnlyUntilSave() {
        EggBadgesConfig config = defaults();
        EggBadgeDraftModel draft = new EggBadgeDraftModel(config);
        SettingsSelection.selectFilter(draft, SettingsSelection.CircleFilter.FIVE, true);
        assertTrue(draft.differsFrom(config));
        assertFalse(config.icon.configured);

        EggBadgeDraftModel reopened = new EggBadgeDraftModel(config);
        assertFalse(reopened.icon.configured);

        draft.commit(config);
        assertTrue(config.icon.configured);
        assertEquals(BadgeCondition.FIVE_5, config.icon.condition);
        assertFalse(config.show_perfect_iv_count);
        assertFalse(draft.differsFrom(config));
    }

    @Test void advancedFilterChangesParticipateInTheSameDirtyState() {
        EggBadgesConfig config = defaults();
        EggBadgeDraftModel draft = new EggBadgeDraftModel(config);
        draft.icon.configured = true;
        draft.icon.condition = BadgeCondition.CUSTOM;
        draft.icon.stats[0] = new StatCondition(StatCondition.Operator.GT, 29);
        assertTrue(draft.differsFrom(config));

        draft.icon.stats[0] = new StatCondition();
        draft.icon.configured = false;
        draft.icon.condition = BadgeCondition.FIVE_5;
        assertFalse(draft.differsFrom(config));
    }

    @Test void hiddenAbilityToggleParticipatesInDirtyStateAndCommits() {
        EggBadgesConfig config = defaults();
        EggBadgeDraftModel draft = new EggBadgeDraftModel(config);
        draft.showHiddenAbility = true;
        assertTrue(draft.differsFrom(config));
        draft.showHiddenAbility = false;
        assertFalse(draft.differsFrom(config));

        draft.showHiddenAbility = true;
        draft.commit(config);
        assertTrue(config.show_hidden_ability);
        assertFalse(draft.differsFrom(config));
    }

    @Test void cancelDiscardsTheDraftWithoutTouchingSavedConfiguration() {
        EggBadgesConfig config = defaults();
        EggBadgeDraftModel draft = new EggBadgeDraftModel(config);
        draft.background.configured = false;
        draft.showPerfectIvCount = true;
        draft.gender.show = true;

        EggBadgeDraftModel reopened = new EggBadgeDraftModel(config);
        assertTrue(reopened.background.configured);
        assertFalse(reopened.showPerfectIvCount);
        assertFalse(reopened.gender.show);
    }

    @Test void resetAllUpdatesTheDraftAndOnlySavesWhenTheUserSaves() {
        EggBadgesConfig config = defaults();
        config.background.color = 0xFFB388FF;
        config.gender.show = true;
        config.show_perfect_iv_count = true;
        EggBadgeDraftModel draft = new EggBadgeDraftModel(config);

        draft.reset();
        assertTrue(draft.differsFrom(config));
        assertEquals(AppearancePalette.BACKGROUND_GREEN, draft.background.color);
        assertFalse(draft.gender.show);
        assertFalse(draft.showPerfectIvCount);
        assertEquals(0xFFB388FF, config.background.color, "Reset All must not persist immediately");
        assertTrue(config.gender.show);
        assertTrue(config.show_perfect_iv_count);
    }

    @Test void resetAllClearsADirectFilterCircleOnlyInTheDraft() {
        EggBadgesConfig config = defaults();
        config.icon.configured = true;
        config.icon.condition = BadgeCondition.CUSTOM;
        config.icon.color = AppearancePalette.CIRCLE_WHITE;
        EggBadgeDraftModel draft = new EggBadgeDraftModel(config);

        draft.reset();
        assertEquals(SettingsSelection.IconMode.OFF, SettingsSelection.mode(draft));
        assertFalse(draft.icon.configured);
        assertFalse(draft.showPerfectIvCount);
        assertTrue(config.icon.configured, "Reset All must not persist immediately");
        assertEquals(BadgeCondition.CUSTOM, config.icon.condition);
    }

    @Test void resetAllIsNotDirtyWhenTheSavedConfigurationIsAlreadyDefault() {
        EggBadgesConfig config = defaults();
        EggBadgeDraftModel draft = new EggBadgeDraftModel(config);
        draft.reset();
        assertFalse(draft.differsFrom(config));
    }

    @Test void saveCommitsTheDraftAndClearsDirtyState() {
        EggBadgesConfig config = defaults();
        EggBadgeDraftModel draft = new EggBadgeDraftModel(config);
        draft.gender.show = true;
        SettingsSelection.selectMode(draft, SettingsSelection.IconMode.CIRCLE);

        draft.commit(config);
        assertFalse(draft.differsFrom(config));
        assertFalse(config.show_perfect_iv_count);
        assertTrue(config.gender.show);
        assertTrue(config.icon.configured);
        assertEquals(BadgeCondition.FIVE_5, config.icon.condition);
    }

    @Test void countModeCommitsAsTheIndependentFlag() {
        EggBadgesConfig config = defaults();
        EggBadgeDraftModel draft = new EggBadgeDraftModel(config);
        SettingsSelection.selectMode(draft, SettingsSelection.IconMode.COUNT);
        assertTrue(draft.differsFrom(config));
        draft.commit(config);
        assertTrue(config.show_perfect_iv_count);
        assertFalse(config.icon.configured);
    }
}
