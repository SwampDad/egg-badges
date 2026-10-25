package com.cobbletown.eggbadges;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class SixthIvInputTest {
    @Test void acceptsEmptyAndEveryValueFromZeroThroughThirtyOne() {
        assertTrue(EggBadgeSettingsScreen.validSixthIv(""));
        for (int value = 0; value <= 31; value++) {
            assertTrue(EggBadgeSettingsScreen.validSixthIv(Integer.toString(value)), "value " + value);
            assertEquals(value, EggBadgeSettingsScreen.sixthIvValue(Integer.toString(value)));
        }
    }

    @Test void rejectsNonDigitsNegativeAndOutOfRangeValues() {
        assertFalse(EggBadgeSettingsScreen.validSixthIv("32"));
        assertFalse(EggBadgeSettingsScreen.validSixthIv("99"));
        assertFalse(EggBadgeSettingsScreen.validSixthIv("-1"));
        assertFalse(EggBadgeSettingsScreen.validSixthIv("3a"));
        assertFalse(EggBadgeSettingsScreen.validSixthIv(" 5"));
        assertFalse(EggBadgeSettingsScreen.validSixthIv("003"));
        assertEquals(-2, EggBadgeSettingsScreen.sixthIvValue("32"));
        assertEquals(-2, EggBadgeSettingsScreen.sixthIvValue("3a"));
    }

    @Test void emptyMeansTheExistingAnySemantics() {
        assertEquals(-1, EggBadgeSettingsScreen.sixthIvValue(""));
    }

    @Test void fifthIvControlAcceptsZeroThroughThirtyAndRejectsOthers() {
        for (int value = 0; value <= 30; value++) {
            assertTrue(EggBadgeSettingsScreen.validFifthAbove(Integer.toString(value)));
            assertEquals(value, EggBadgeSettingsScreen.fifthAboveValue(Integer.toString(value)));
        }
        assertFalse(EggBadgeSettingsScreen.validFifthAbove("31"));
        assertFalse(EggBadgeSettingsScreen.validFifthAbove("-1"));
        assertEquals(-2, EggBadgeSettingsScreen.fifthAboveValue("31"));
    }

    @Test void numericValueKeepsTheExistingFiveIvMatchingSemantics() {
        ChannelConfig five = ChannelConfig.iconDefault();
        five.configured = true;
        five.five_iv_minimum = 30;
        five.five_iv_restricted = true;
        EggAttributes egg = new EggAttributes("", "", "", "Male", new int[]{31, 31, 31, 31, 31, 29});
        assertFalse(EggClassifier.matches(five, egg));
        EggAttributes above = new EggAttributes("", "", "", "Male", new int[]{31, 31, 31, 31, 31, 30});
        assertTrue(EggClassifier.matches(five, above));
    }
}
