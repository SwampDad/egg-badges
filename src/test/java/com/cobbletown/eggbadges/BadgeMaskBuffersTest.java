package com.cobbletown.eggbadges;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class BadgeMaskBuffersTest {
    @Test void pendingResultsBecomeVisibleOnlyAtSwap() {
        BadgeMaskBuffers buffers = new BadgeMaskBuffers();
        buffers.resize(3);
        buffers.setActive(0, 1);
        buffers.setPending(1, 2);

        assertEquals(1, buffers.activeMask(0));
        assertEquals(0, buffers.activeMask(1));
        buffers.swap();
        assertEquals(0, buffers.activeMask(0));
        assertEquals(2, buffers.activeMask(1));
        assertEquals(1, buffers.nextActive(0));
    }

    @Test void changedSlotCanBeClearedWithoutClearingUnchangedBadges() {
        BadgeMaskBuffers buffers = new BadgeMaskBuffers();
        buffers.resize(3);
        buffers.setActive(0, 1);
        buffers.setActive(2, 4);
        buffers.clearActive(0);

        assertEquals(2, buffers.nextActive(0));
        assertEquals(4, buffers.activeMask(2));
    }

    @Test void inProgressResultsSurviveRevisionReconciliationUntilTheScanSwaps() {
        BadgeMaskBuffers buffers = new BadgeMaskBuffers();
        buffers.resize(4);

        // The first tick classifies slots 0 and 1.  A lore/timer revision then
        // refreshes slot 0 while the remaining slots are still being scanned.
        buffers.setPending(0, 1);
        buffers.setPending(1, 2);
        buffers.setPending(0, 4);
        buffers.setPending(2, 8);
        buffers.setPending(3, 16);

        assertEquals(0, buffers.activeMask(0));
        buffers.swap();
        assertEquals(4, buffers.activeMask(0));
        assertEquals(2, buffers.activeMask(1));
        assertEquals(8, buffers.activeMask(2));
        assertEquals(16, buffers.activeMask(3));
    }
}
