package com.cobbletown.eggbadges;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/** Regression tests for the schema-10 product contract, independent of Minecraft UI classes. */
class ThreeChannelTest {
    private static EggAttributes egg(int... ivs) { return new EggAttributes("", "", "", "Male", ivs); }

    @Test void perfectAndFiveAreExactAndThresholdIsInclusive() {
        ChannelConfig five = ChannelConfig.iconDefault();
        five.configured = true;
        five.five_iv_minimum = 29;
        five.five_iv_restricted = true;
        assertTrue(EggClassifier.matches(five, egg(31, 31, 31, 31, 31, 29)));
        assertFalse(EggClassifier.matches(five, egg(31, 31, 31, 31, 31, 28)));
        assertFalse(EggClassifier.matches(five, egg(31, 31, 31, 31, 31, 31)));
    }

    @Test void customOperatorsFailClosedForUnknownValues() {
        ChannelConfig c = ChannelConfig.iconDefault();
        c.configured = true;
        c.condition = BadgeCondition.CUSTOM;
        c.stats[0] = new StatCondition(StatCondition.Operator.GT, 30);
        assertTrue(EggClassifier.matches(c, egg(31, 0, 0, 0, 0, 0)));
        assertFalse(EggClassifier.matches(c, egg(-1, 0, 0, 0, 0, 0)));
        assertFalse(EggClassifier.matches(c, egg(30, 0, 0, 0, 0, 0)));
    }

    @Test void stateHasOneIconOneBackgroundAndIndependentCount() {
        BadgeState state = new BadgeState(0x68000000,
                new BadgeState.IconState("V", 0xFF55AAFF), "Male", GenderStyle.LETTERS, 5);
        assertNotNull(state.backgroundArgb());
        assertNotNull(state.icon());
        assertEquals("Male", state.gender());
        assertEquals(GenderStyle.LETTERS, state.genderStyle());
        assertEquals(5, state.perfectIvCount());
        assertFalse(state.hiddenAbility());
    }

    @Test void draftCancelIsIsolatedAndResetRestoresApprovedDefaults() {
        EggBadgesConfig source = new EggBadgesConfig();
        EggBadgeDraftModel draft = new EggBadgeDraftModel(source);
        draft.background.configured = false;
        draft.showPerfectIvCount = true;
        assertTrue(source.background.configured);
        assertFalse(source.show_perfect_iv_count);
        draft.reset();
        assertTrue(draft.background.configured);
        assertEquals(BadgeCondition.PERFECT_6, draft.background.condition);
        assertFalse(draft.icon.configured);
        assertFalse(draft.gender.show);
        assertFalse(draft.showPerfectIvCount);
        assertFalse(draft.showHiddenAbility);
    }
}
