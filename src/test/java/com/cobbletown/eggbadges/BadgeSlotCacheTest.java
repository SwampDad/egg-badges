package com.cobbletown.eggbadges;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class BadgeSlotCacheTest {
    private static final Object STACK = new Object();
    private static final Object ITEM = new Object();
    private static final Object COMPONENTS = new Object();
    private static final BadgeState STATE = new BadgeState(null, null, "Female");

    @Test void resizeMarksEverySlotForInitialClassification() {
        BadgeSlotCache cache = new BadgeSlotCache();
        cache.resize(3);
        for (int i = 0; i < 3; i++) assertTrue(cache.isDirty(i));
        cache.accept(0, STACK, ITEM, COMPONENTS, 1, STATE);
        assertFalse(cache.isDirty(0));
        assertTrue(cache.isDirty(1));
    }

    @Test void unchangedStackReferenceDoesNotCauseReclassification() {
        BadgeSlotCache cache = new BadgeSlotCache();
        cache.resize(1);
        cache.accept(0, STACK, ITEM, COMPONENTS, 1, STATE);
        assertFalse(cache.observe(0, STACK, ITEM, COMPONENTS, 1));
        assertFalse(cache.isDirty(0));
    }

    @Test void timerComponentRefreshKeepsTheLastGoodBadgeUntilTheRescanLands() {
        BadgeSlotCache cache = new BadgeSlotCache();
        cache.resize(1);
        cache.accept(0, STACK, ITEM, COMPONENTS, 1, STATE);
        Object refreshedComponents = new Object();

        assertTrue(cache.observe(0, STACK, ITEM, refreshedComponents, 1));
        assertTrue(cache.isDirty(0));
        assertSame(STATE, cache.state(0), "the badge must stay visible while the timer lore refreshes");
    }

    @Test void transientParseMissesRetainTheBadgeForTheGracePeriod() {
        BadgeSlotCache cache = new BadgeSlotCache();
        cache.resize(1);
        cache.accept(0, STACK, ITEM, COMPONENTS, 1, STATE);
        assertTrue(cache.miss(0, ITEM));
        assertSame(STATE, cache.state(0));
        assertTrue(cache.miss(0, ITEM));
        assertSame(STATE, cache.state(0));
        assertFalse(cache.miss(0, ITEM));
        assertNull(cache.state(0));
    }

    @Test void aDifferentItemFailsClosedImmediately() {
        BadgeSlotCache cache = new BadgeSlotCache();
        cache.resize(1);
        cache.accept(0, STACK, ITEM, COMPONENTS, 1, STATE);
        assertFalse(cache.miss(0, new Object()));
        assertNull(cache.state(0));
    }

    @Test void removingTheEggClearsOnlyThatSlotImmediately() {
        BadgeSlotCache cache = new BadgeSlotCache();
        cache.resize(2);
        cache.accept(0, STACK, ITEM, COMPONENTS, 1, STATE);
        cache.accept(1, STACK, ITEM, COMPONENTS, 1, STATE);
        Object emptyStack = new Object();
        Object air = new Object();
        cache.clear(0, emptyStack, air, COMPONENTS, 0);
        assertNull(cache.state(0));
        assertFalse(cache.isDirty(0));
        assertSame(STATE, cache.state(1));
    }

    @Test void slotIndexingPreventsCrossSlotStaleness() {
        BadgeSlotCache cache = new BadgeSlotCache();
        cache.resize(2);
        BadgeState male = new BadgeState(null, null, "Male");
        cache.accept(0, STACK, ITEM, COMPONENTS, 1, STATE);
        cache.accept(1, STACK, ITEM, COMPONENTS, 1, male);
        assertSame(STATE, cache.state(0));
        assertSame(male, cache.state(1));
    }
}
