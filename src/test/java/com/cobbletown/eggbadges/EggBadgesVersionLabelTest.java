package com.cobbletown.eggbadges;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class EggBadgesVersionLabelTest {
    @Test void preservesDevelopmentSuffixFromInstalledMetadata() {
        assertEquals("Egg Badges · v0.5.0-dev.10", EggBadgesVersionLabel.format("0.5.0-dev.10"));
    }

    @Test void formatsStableVersionWithoutChangingIt() {
        assertEquals("Egg Badges · v0.5.0", EggBadgesVersionLabel.format("0.5.0"));
    }

    @Test void hasAnExplicitFallbackForMissingMetadata() {
        assertEquals("Egg Badges · vunknown", EggBadgesVersionLabel.format(null));
    }
}
