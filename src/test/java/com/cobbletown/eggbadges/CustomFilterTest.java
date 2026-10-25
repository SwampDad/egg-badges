package com.cobbletown.eggbadges;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class CustomFilterTest {
    @Test void operatorControlSwitchesOnlyBetweenOpenBounds() {
        StatCondition condition = new StatCondition(StatCondition.Operator.GT, 29);
        EggBadgeSettingsScreen.toggleOperator(condition);
        assertEquals(StatCondition.Operator.LT, condition.operator);
        EggBadgeSettingsScreen.toggleOperator(condition);
        assertEquals(StatCondition.Operator.GT, condition.operator);
        assertEquals(">", EggBadgeSettingsScreen.operatorLabel(condition));
    }

    @Test void enablingAStatUsesAnOpenBoundAndDisablingClearsIt() {
        StatCondition condition = new StatCondition();
        EggBadgeSettingsScreen.setStatEnabled(condition, true);
        assertEquals(StatCondition.Operator.GT, condition.operator);
        assertEquals(31, condition.value);
        assertEquals(">", EggBadgeSettingsScreen.operatorLabel(condition));

        condition.value = 27;
        EggBadgeSettingsScreen.setStatEnabled(condition, false);
        assertEquals(StatCondition.Operator.ANY, condition.operator);
        assertEquals(-1, condition.value);
        assertEquals("?", EggBadgeSettingsScreen.operatorLabel(condition));
    }

    @Test void numericThresholdUpdatesPreserveMatchingSemantics() {
        ChannelConfig custom = ChannelConfig.iconDefault();
        custom.configured = true;
        custom.condition = BadgeCondition.CUSTOM;
        custom.stats[0] = new StatCondition(StatCondition.Operator.GT, 29);
        custom.stats[1] = new StatCondition(StatCondition.Operator.LT, 15);
        custom.five_iv_restricted = false;

        EggAttributes above = new EggAttributes("", "", "", "", new int[]{30, 10, 0, 0, 0, 0});
        EggAttributes below = new EggAttributes("", "", "", "", new int[]{29, 10, 0, 0, 0, 0});
        EggAttributes secondBound = new EggAttributes("", "", "", "", new int[]{30, 15, 0, 0, 0, 0});
        assertTrue(EggClassifier.matches(custom, above));
        assertFalse(EggClassifier.matches(custom, below));
        assertFalse(EggClassifier.matches(custom, secondBound));
    }

    @Test void existingExactComparisonsRemainIntactAndMatchExactly() {
        StatCondition exact = new StatCondition(StatCondition.Operator.EQ, 15);
        assertTrue(exact.matches(15));
        assertFalse(exact.matches(14));
        assertFalse(exact.matches(16));
        assertEquals("=", EggBadgeSettingsScreen.operatorLabel(exact));
    }

    @Test void thresholdTextAcceptsZeroThroughThirtyOneAndRejectsInvalidText() {
        for (int value = 0; value <= 31; value++) {
            assertTrue(EggBadgeSettingsScreen.validSixthIv(Integer.toString(value)));
            assertEquals(value, EggBadgeSettingsScreen.sixthIvValue(Integer.toString(value)));
        }
        assertFalse(EggBadgeSettingsScreen.validSixthIv("32"));
        assertFalse(EggBadgeSettingsScreen.validSixthIv("-1"));
        assertFalse(EggBadgeSettingsScreen.validSixthIv("3a"));
        assertEquals(-1, EggBadgeSettingsScreen.sixthIvValue(""));
        assertEquals(-2, EggBadgeSettingsScreen.sixthIvValue("32"));
    }
}
