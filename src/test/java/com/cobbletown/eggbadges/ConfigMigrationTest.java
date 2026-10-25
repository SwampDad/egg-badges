package com.cobbletown.eggbadges;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class ConfigMigrationTest {
    @Test void newInstallAndResetUseTheSimplifiedDefaults() {
        EggBadgesConfig config = new EggBadgesConfig();
        config.resetInMemory();
        assertTrue(config.background.configured);
        assertEquals(BadgeCondition.PERFECT_6, config.background.condition);
        assertEquals(AppearancePalette.BACKGROUND_GREEN, config.background.color);
        assertFalse(config.icon.configured);
        assertEquals(BadgeCondition.FIVE_5, config.icon.condition);
        assertEquals(AppearancePalette.CIRCLE_GOLD, config.icon.color);
        assertEquals(IconShape.CIRCLE, config.icon.shape);
        assertFalse(config.icon.five_iv_restricted);
        assertFalse(config.show_perfect_iv_count);
        assertFalse(config.gender.show);
        assertFalse(config.show_hidden_ability);
        assertEquals(16, config.schema_version);
    }

    @Test void schema14To15PreservesTheOwnersLiveConfiguration() {
        EggBadgesConfig config = new EggBadgesConfig();
        config.schema_version = 14;
        config.background.configured = true;
        config.background.condition = BadgeCondition.PERFECT_6;
        config.background.color = AppearancePalette.BACKGROUND_GREEN;
        config.icon.configured = true;
        config.icon.condition = BadgeCondition.FIVE_5;
        config.icon.color = AppearancePalette.CIRCLE_GOLD;
        config.icon.shape = IconShape.CIRCLE;
        config.icon.five_iv_minimum = 30;
        config.icon.five_iv_restricted = true;
        config.icon.stats[0] = new StatCondition(StatCondition.Operator.EQ, 31);
        config.show_perfect_iv_count = false;
        config.gender.show = true;
        config.show_hidden_ability = true;

        EggBadgesConfig.migrateV14ToV15(config);

        assertEquals(15, config.schema_version);
        assertTrue(config.background.configured);
        assertEquals(AppearancePalette.BACKGROUND_GREEN, config.background.color);
        assertTrue(config.icon.configured);
        assertEquals(BadgeCondition.FIVE_5, config.icon.condition);
        assertEquals(AppearancePalette.CIRCLE_GOLD, config.icon.color);
        assertTrue(config.icon.five_iv_restricted);
        assertEquals(30, config.icon.five_iv_minimum);
        assertEquals(StatCondition.Operator.EQ, config.icon.stats[0].operator);
        assertFalse(config.show_perfect_iv_count);
        assertTrue(config.gender.show);
        assertTrue(config.show_hidden_ability);
    }

    @Test void schema15To16MapsEveryPriorSelectableColorToItsNewDistinctPalette() {
        EggBadgesConfig config = new EggBadgesConfig();
        config.schema_version = 15;
        config.background.configured = true;
        config.background.color = 0xFFB388FF; // prior Purple
        config.icon.configured = true;
        config.icon.color = 0xFFFFD54A; // prior Yellow

        EggBadgesConfig.migrateV15ToV16(config);

        assertEquals(16, config.schema_version);
        assertEquals(AppearancePalette.BACKGROUND_PURPLE, config.background.color);
        assertEquals(AppearancePalette.CIRCLE_GOLD, config.icon.color);
        assertTrue(config.background.configured);
        assertTrue(config.icon.configured);
    }

    @Test void schema15BlueCircleMapsToBrightCyanWithoutLosingItsFilter() {
        EggBadgesConfig config = new EggBadgesConfig();
        config.schema_version = 15;
        config.icon.configured = true;
        config.icon.condition = BadgeCondition.CUSTOM;
        config.icon.color = 0xFF55AAFF;
        config.icon.stats[0] = new StatCondition(StatCondition.Operator.GT, 29);

        EggBadgesConfig.migrateV15ToV16(config);

        assertEquals(AppearancePalette.CIRCLE_CYAN, config.icon.color);
        assertEquals(BadgeCondition.CUSTOM, config.icon.condition);
        assertEquals(StatCondition.Operator.GT, config.icon.stats[0].operator);
        assertEquals(29, config.icon.stats[0].value);
    }

    @Test void unsupportedBackgroundConditionIsDeactivatedButPreserved() {
        EggBadgesConfig config = new EggBadgesConfig();
        config.schema_version = 14;
        config.background.configured = true;
        config.background.condition = BadgeCondition.FIVE_5;
        config.background.color = AppearancePalette.BACKGROUND_PURPLE;
        EggBadgesConfig.migrateV14ToV15(config);
        assertFalse(config.background.configured);
        assertEquals(BadgeCondition.FIVE_5, config.background.condition);
        assertEquals(AppearancePalette.BACKGROUND_PURPLE, config.background.color);
    }

    @Test void unsupportedBackgroundColorsAreDeactivatedWithoutSubstitution() {
        EggBadgesConfig config = new EggBadgesConfig();
        config.schema_version = 14;
        config.background.configured = true;
        config.background.condition = BadgeCondition.PERFECT_6;
        config.background.color = 0xFFFFD54A;
        EggBadgesConfig.migrateV14ToV15(config);
        assertFalse(config.background.configured);
        assertEquals(0xFFFFD54A, config.background.color);
    }

    @Test void legacyBlueBackgroundMapsToTheDarkerBackgroundBlue() {
        EggBadgesConfig config = new EggBadgesConfig();
        config.schema_version = 14;
        config.background.configured = true;
        config.background.color = 0xFF5599FF;
        EggBadgesConfig.migrateV14ToV15(config);
        assertTrue(config.background.configured);
        assertEquals(AppearancePalette.BACKGROUND_BLUE, config.background.color);
    }

    @Test void unsupportedIconConditionAndColorAreDeactivatedButPreserved() {
        EggBadgesConfig config = new EggBadgesConfig();
        config.schema_version = 14;
        config.icon.configured = true;
        config.icon.condition = BadgeCondition.PERFECT_6;
        config.icon.color = 0xFFFF5555;
        EggBadgesConfig.migrateV14ToV15(config);
        assertFalse(config.icon.configured);
        assertEquals(BadgeCondition.PERFECT_6, config.icon.condition);
        assertEquals(0xFFFF5555, config.icon.color);
    }

    @Test void legacyCircleBlueMapsToTheCanonicalCircleBlue() {
        EggBadgesConfig config = new EggBadgesConfig();
        config.schema_version = 14;
        config.icon.configured = true;
        config.icon.condition = BadgeCondition.FIVE_5;
        config.icon.color = 0xFF5599FF;
        EggBadgesConfig.migrateV14ToV15(config);
        assertTrue(config.icon.configured);
        assertEquals(AppearancePalette.CIRCLE_CYAN, config.icon.color);
    }

    @Test void legacyCountAndCircleConflictPrefersTheCircleWithBackupLogging() {
        EggBadgesConfig config = new EggBadgesConfig();
        config.schema_version = 14;
        config.icon.configured = true;
        config.icon.condition = BadgeCondition.FIVE_5;
        config.icon.color = AppearancePalette.CIRCLE_CYAN;
        config.show_perfect_iv_count = true;
        EggBadgesConfig.migrateV14ToV15(config);
        assertTrue(config.icon.configured);
        assertFalse(config.show_perfect_iv_count);
    }

    @Test void schema13To14NoLongerDestroysBlueBackgroundColors() {
        EggBadgesConfig config = new EggBadgesConfig();
        config.schema_version = 13;
        config.background.color = 0xFF5599FF;
        EggBadgesConfig.migrateV13ToV14(config);
        assertEquals(14, config.schema_version);
        assertEquals(0xFF5599FF, config.background.color);
    }

    @Test void schema12To13RetainsCircleAndThresholdMigration() {
        EggBadgesConfig config = new EggBadgesConfig();
        config.schema_version = 12;
        config.icon.shape = IconShape.DIAMOND;
        config.icon.five_iv_minimum = 29;
        config.icon.stats[0] = new StatCondition(StatCondition.Operator.EQ, 31);
        EggBadgesConfig.migrateV12ToV13(config);
        assertEquals(13, config.schema_version);
        assertEquals(IconShape.CIRCLE, config.icon.shape);
        assertTrue(config.icon.five_iv_restricted);
        assertEquals(29, config.icon.five_iv_minimum);
        assertEquals(StatCondition.Operator.EQ, config.icon.stats[0].operator);
    }

    @Test void schema9To10LiftsTheOldAutomaticCount() {
        EggBadgesConfig config = new EggBadgesConfig();
        config.schema_version = 9;
        config.icon.configured = true;
        config.icon.condition = BadgeCondition.IV_COUNT;
        EggBadgesConfig.migrateV9ToV10(config);
        assertEquals(10, config.schema_version);
        assertTrue(config.show_perfect_iv_count);
        assertFalse(config.icon.configured);
    }

    @Test void schema11GenderBackgroundBecomesTheGenderCheckbox() {
        EggBadgesConfig config = new EggBadgesConfig();
        config.schema_version = 11;
        config.background.configured = true;
        config.background.condition = BadgeCondition.GENDER;
        EggBadgesConfig.migrateV11ToV12(config);
        assertFalse(config.background.configured);
        assertTrue(config.gender.show);
    }

    @Test void normalizationDeactivatesUnsupportedManualStates() {
        EggBadgesConfig config = new EggBadgesConfig();
        config.background.condition = BadgeCondition.CUSTOM;
        config.background.color = 0xFF55DD77;
        config.icon.condition = BadgeCondition.PERFECT_6;
        config.icon.color = 0xFFFF5555;
        config.icon.configured = true;
        config.normalizeForTest();
        assertFalse(config.background.configured);
        assertFalse(config.icon.configured);
        assertEquals(0xFFFF5555, config.icon.color);
    }

    @Test void migrationIsIdempotent() {
        EggBadgesConfig config = new EggBadgesConfig();
        config.schema_version = 14;
        config.icon.configured = true;
        config.icon.condition = BadgeCondition.CUSTOM;
        config.icon.color = AppearancePalette.CIRCLE_GOLD;
        config.icon.stats[0] = new StatCondition(StatCondition.Operator.GT, 29);
        EggBadgesConfig.migrateV14ToV15(config);
        EggBadgesConfig.migrateV14ToV15(config);
        assertEquals(15, config.schema_version);
        assertEquals(BadgeCondition.CUSTOM, config.icon.condition);
        assertEquals(AppearancePalette.CIRCLE_GOLD, config.icon.color);
        assertEquals(StatCondition.Operator.GT, config.icon.stats[0].operator);
        assertEquals(29, config.icon.stats[0].value);
    }
}
