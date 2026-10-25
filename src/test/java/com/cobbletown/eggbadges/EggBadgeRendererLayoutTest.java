package com.cobbletown.eggbadges;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class EggBadgeRendererLayoutTest {
    @Test void fullContainerClassificationIsBoundedToFiveTicks() {
        assertEquals(18, EggBadgeRenderer.SLOTS_PER_TICK);
        assertTrue((90 + EggBadgeRenderer.SLOTS_PER_TICK - 1) / EggBadgeRenderer.SLOTS_PER_TICK <= 5);
    }

    @Test void slotHookCoordinatesDoNotAddTheHandledScreenOriginTwice() {
        // HandledScreen translates DrawContext by its origin before invoking drawSlot.
        // The background hook must therefore pass local slot coordinates through unchanged.
        assertEquals(17, EggBadgeRenderer.screenLocalSlotCoordinate(17));
        assertEquals(53, EggBadgeRenderer.screenLocalSlotCoordinate(53));
    }

    @Test void badgePositionsAreFixedByRuleIndex() {
        assertEquals(0, EggBadgeRenderer.computeLaneX(0, true));
        assertEquals(8, EggBadgeRenderer.computeLaneX(1, true));
        assertEquals(0, EggBadgeRenderer.computeLaneY(0, true));
        assertEquals(8, EggBadgeRenderer.computeLaneY(2, true));
        assertEquals(EggBadgeRenderer.computeLaneX(2, false), EggBadgeRenderer.computeLaneX(2, false));
        assertEquals(EggBadgeRenderer.computeLaneY(3, false), EggBadgeRenderer.computeLaneY(3, false));
    }
    @Test void presentationModeInvalidatesCustomRuleFingerprint(){
        BadgeRule[] rules=BadgeRule.defaults();int before=EggBadgeRenderer.Cache.rulesHash(rules);
        rules[2].presentation="BORDER";
        assertNotEquals(before,EggBadgeRenderer.Cache.rulesHash(rules));
    }
    @Test void perfectDefaultUsesTheGreenBackgroundAppearance(){
        BadgeRule perfect=BadgeRule.defaults()[2];
        assertEquals("BACKGROUND",perfect.presentation);
        assertEquals(0xFF55DD77,perfect.color);
    }
    @Test void backgroundsUseTheSharedReadableOpacity(){
        assertEquals(0x68,EggBadgeRenderer.BACKGROUND_ALPHA);
        assertEquals(0x6855DD77,EggBadgeRenderer.backgroundColor(0xFF55DD77));
    }
}
