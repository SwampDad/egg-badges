package com.cobbletown.eggbadges;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class SettingsLabelsTest {
    @Test void persistedIdentifiersNeverLeakIntoHumanFacingLabels() {
        assertEquals("Perfect (6×31)", SettingsLabels.condition(BadgeCondition.PERFECT_6));
        assertEquals("Five Perfect IVs (5×31)", SettingsLabels.condition(BadgeCondition.FIVE_5));
        assertEquals("Perfect IV Count", SettingsLabels.condition(BadgeCondition.IV_COUNT));
        assertEquals("Custom IV requirements", SettingsLabels.condition(BadgeCondition.CUSTOM));
        assertEquals("Circle", SettingsLabels.shape(IconShape.CIRCLE));
        assertEquals("Green", SettingsLabels.color(0xFF55DD77));
        assertEquals("Custom color", SettingsLabels.color(0xFF123456));
        assertEquals("Colored corner", SettingsLabels.genderStyle(GenderStyle.CORNER));
        assertFalse(SettingsLabels.condition(BadgeCondition.PERFECT_6).contains("_"));
    }

    @Test void statLabelsAreReadableAndNeverShowRawOperators() {
        assertEquals("HP: Any", SettingsLabels.stat(new StatCondition(), 0));
        assertEquals("Attack: exactly 31", SettingsLabels.stat(new StatCondition(StatCondition.Operator.EQ, 31), 1));
        assertEquals("Sp. Defense: above 29", SettingsLabels.stat(new StatCondition(StatCondition.Operator.GT, 29), 4));
        assertEquals("Speed: below 10", SettingsLabels.stat(new StatCondition(StatCondition.Operator.LT, 10), 5));
        assertFalse(SettingsLabels.stat(new StatCondition(StatCondition.Operator.EQ, 31), 0).contains("EQ"));
    }

    @Test void operatorControlOnlyOffersOpenBoundsForNewEdits() {
        assertEquals("?", EggBadgeSettingsScreen.operatorLabel(new StatCondition()));
        assertEquals(">", EggBadgeSettingsScreen.operatorLabel(new StatCondition(StatCondition.Operator.GT, 29)));
        assertEquals("<", EggBadgeSettingsScreen.operatorLabel(new StatCondition(StatCondition.Operator.LT, 10)));
        assertEquals("=", EggBadgeSettingsScreen.operatorLabel(new StatCondition(StatCondition.Operator.EQ, 31)),
                "legacy exact filters remain visible until edited");
    }
}
