package com.cobbletown.eggbadges;

import java.util.BitSet;

/**
 * Slot-indexed badge cache. It is deliberately independent of Minecraft classes so the
 * first-slot flicker contract can be unit-tested.
 *
 * <p>The cache keeps the last good badge while a slot's item reference/components are
 * being refreshed by a server timer, instead of dropping the badge for a frame. A slot is
 * cleared immediately when it becomes empty or changes item; a transient classification
 * miss is retried for {@link #MISS_GRACE_TICKS} ticks before failing closed.</p>
 */
final class BadgeSlotCache {
    static final int MISS_GRACE_TICKS = 2;

    private static final class Entry {
        Object stackRef, itemRef, componentsRef;
        int count;
        BadgeState state;
        int missed;
    }

    private Entry[] entries = new Entry[0];
    private final BitSet dirty = new BitSet();

    void resize(int size) {
        if (entries.length == size) return;
        entries = new Entry[Math.max(0, size)];
        dirty.clear();
        dirty.set(0, entries.length);
    }

    int size() { return entries.length; }

    /** Detects a stack/count/component change without parsing lore. */
    boolean observe(int slot, Object stackRef, Object itemRef, Object componentsRef, int count) {
        Entry e = entry(slot);
        boolean changed = e.stackRef != stackRef || e.itemRef != itemRef
                || e.componentsRef != componentsRef || e.count != count;
        if (changed) dirty.set(slot);
        return changed;
    }

    boolean isDirty(int slot) { return slot >= 0 && slot < entries.length && dirty.get(slot); }

    BadgeState state(int slot) {
        return slot >= 0 && slot < entries.length && entries[slot] != null ? entries[slot].state : null;
    }

    boolean sameItem(int slot, Object itemRef) {
        return slot >= 0 && slot < entries.length && entries[slot] != null && entries[slot].itemRef == itemRef;
    }

    void accept(int slot, Object stackRef, Object itemRef, Object componentsRef, int count, BadgeState state) {
        Entry e = entry(slot);
        e.stackRef = stackRef;
        e.itemRef = itemRef;
        e.componentsRef = componentsRef;
        e.count = count;
        e.state = state;
        e.missed = 0;
        dirty.clear(slot);
    }

    /** Clears immediately (empty slot, different item, or explicit removal). */
    void clear(int slot, Object stackRef, Object itemRef, Object componentsRef, int count) {
        Entry e = entry(slot);
        e.stackRef = stackRef;
        e.itemRef = itemRef;
        e.componentsRef = componentsRef;
        e.count = count;
        e.state = null;
        e.missed = 0;
        dirty.clear(slot);
    }

    /**
     * Records a transient parse miss. Same-item misses keep the last good state and stay
     * dirty for a bounded retry; a different item fails closed immediately.
     */
    boolean miss(int slot, Object itemRef) {
        Entry e = entry(slot);
        if (e.state == null) {
            dirty.clear(slot);
            return false;
        }
        if (e.itemRef != itemRef) {
            e.state = null;
            e.missed = 0;
            dirty.clear(slot);
            return false;
        }
        if (++e.missed > MISS_GRACE_TICKS) {
            e.state = null;
            e.missed = 0;
            dirty.clear(slot);
            return false;
        }
        return true;
    }

    void clearAll() {
        entries = new Entry[0];
        dirty.clear();
    }

    private Entry entry(int slot) {
        Entry e = entries[slot];
        if (e == null) {
            e = new Entry();
            entries[slot] = e;
        }
        return e;
    }
}
