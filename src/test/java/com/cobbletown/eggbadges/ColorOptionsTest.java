package com.cobbletown.eggbadges;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class ColorOptionsTest {
    @Test void backgroundPaletteIsForestGreenSlateBlueAndDeepPurple() {
        assertArrayEquals(new int[]{AppearancePalette.BACKGROUND_GREEN,
                AppearancePalette.BACKGROUND_BLUE, AppearancePalette.BACKGROUND_PURPLE},
                SettingsSelection.backgroundColors());
    }

    @Test void circlePaletteIsGoldCyanAndWhite() {
        assertArrayEquals(new int[]{AppearancePalette.CIRCLE_GOLD,
                AppearancePalette.CIRCLE_CYAN, AppearancePalette.CIRCLE_WHITE},
                SettingsSelection.circleColors());
    }

    @Test void backgroundBlueIsDarkEnoughToDifferFromMaleBlue() {
        assertNotEquals(0xFF5599FF, AppearancePalette.BACKGROUND_BLUE);
        assertTrue(AppearancePalette.BACKGROUND_BLUE != AppearancePalette.CIRCLE_CYAN);
    }

    @Test void unsupportedPaletteColorsAreNotReportedAsSupported() {
        assertFalse(AppearancePalette.supportedBackground(AppearancePalette.CIRCLE_GOLD));
        assertFalse(AppearancePalette.supportedCircle(0xFF55DD77));
        assertTrue(AppearancePalette.supportedBackground(AppearancePalette.BACKGROUND_BLUE));
        assertTrue(AppearancePalette.supportedCircle(AppearancePalette.CIRCLE_GOLD));
    }

    @Test void palettesHaveNoOverlappingSelectableColors() {
        for (int background : SettingsSelection.backgroundColors())
            for (int circle : SettingsSelection.circleColors())
                assertNotEquals(background, circle);
    }
}
