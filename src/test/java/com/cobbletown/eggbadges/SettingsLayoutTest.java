package com.cobbletown.eggbadges;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class SettingsLayoutTest {
    private static final int[][] SIZES = {{320, 240}, {427, 240}, {515, 270}, {854, 480}, {1280, 720}, {2146, 1744}};

    @Test void mainLayoutStaysInsideRepresentativeGuiBounds() {
        for (int[] size : SIZES) {
            EggBadgeSettingsLayout.Main layout = EggBadgeSettingsLayout.main(size[0], size[1], false);
            EggBadgeSettingsLayout.Panel p = layout.panel();
            assertTrue(p.x() >= 0);
            assertTrue(p.y() >= 0);
            assertTrue(p.x() + p.width() <= size[0]);
            assertTrue(p.y() + p.height() <= size[1]);
            assertTrue(layout.previewBoxSize() >= 64 && layout.previewBoxSize() <= 96);
            assertTrue(layout.previewEggSize() > 32);
            assertTrue(layout.previewBoxX() > layout.contentX() + layout.leftWidth());
            assertTrue(layout.previewBoxY() >= layout.contentY());
            assertTrue(layout.previewBoxY() + layout.previewBoxSize() <= layout.bottomY());
            assertTrue(layout.versionLabelY() + 9 <= layout.bottomY(),
                    "version label must clear footer controls at " + size[0] + "x" + size[1]);
            assertTrue(layout.versionLabelY() >= layout.contentY(),
                    "version label must remain in the fixed panel footer at " + size[0] + "x" + size[1]);
            assertTrue(layout.bottomY() + 20 <= p.bottom());
            assertTrue(layout.leftWidth() > 100);
        }
    }

    @Test void previewIsUpperRightAndAboutHalfTheOldSize() {
        EggBadgeSettingsLayout.Main layout = EggBadgeSettingsLayout.main(854, 480, false);
        assertEquals(96, layout.previewBoxSize());
        assertTrue(layout.previewBoxX() > layout.contentX());
        assertTrue(layout.previewBoxY() >= layout.contentY());
        assertTrue(layout.previewBoxY() + layout.previewBoxSize() <= layout.customFilterY() + 16);
        assertTrue(layout.leftWidth() < layout.panel().innerWidth());
    }

    @Test void controlOrderMatchesTheApprovedLayout() {
        EggBadgeSettingsLayout.Main layout = EggBadgeSettingsLayout.main(854, 480, false);
        assertTrue(layout.perfectHeadingY() < layout.backgroundRowY());
        assertTrue(layout.backgroundRowY() < layout.iconHeadingY());
        assertTrue(layout.iconHeadingY() - layout.backgroundRowY() >= 22,
                "ICON heading needs clearance below Background controls");
        assertTrue(layout.iconHeadingY() < layout.iconRuleY());
        assertTrue(layout.iconRuleY() < layout.circleModeY());
        assertTrue(layout.circleModeY() < layout.circleColorY());
        assertTrue(layout.circleColorY() - layout.circleModeY() >= 20,
                "Color row needs clearance below Colored Circle");
        assertTrue(layout.circleColorY() < layout.fiveFilterY());
        assertTrue(layout.fiveFilterY() < layout.customFilterY());
        assertTrue(layout.customFilterY() < layout.orY());
        assertTrue(layout.orY() < layout.countModeY());
        assertTrue(layout.countModeY() < layout.otherY());
        assertTrue(layout.otherY() < layout.bottomY());
    }

    @Test void collapsedLayoutFitsWithoutScrollingAtSupportedScales() {
        for (int[] size : SIZES) {
            EggBadgeSettingsLayout.Main layout = EggBadgeSettingsLayout.main(size[0], size[1], false);
            assertEquals(0, layout.scrollMax(), "collapsed layout at " + size[0] + "x" + size[1]);
            assertFalse(layout.customStatsVisible());
            assertEquals(-1, layout.customStatsY());
        }
    }

    @Test void expandedCustomEditorScrollsOnlyWhenItOverflows() {
        EggBadgeSettingsLayout.Main wide = EggBadgeSettingsLayout.main(854, 480, true);
        assertTrue(wide.customStatsVisible());
        assertTrue(wide.customStatsY() > wide.customFilterY());
        assertTrue(wide.orY() > wide.customStatsY() + 5 * EggBadgeSettingsLayout.STAT_ROW_HEIGHT);
        assertEquals(0, wide.scrollMax(), "wide expanded custom still fits");

        EggBadgeSettingsLayout.Main compact = EggBadgeSettingsLayout.main(515, 270, true);
        assertTrue(compact.scrollMax() > 0, "compact expanded custom must scroll");
    }

    @Test void previewStaysAboveTheOrDividerAndCountMode() {
        EggBadgeSettingsLayout.Main layout = EggBadgeSettingsLayout.main(320, 240, false);
        assertTrue(layout.previewBoxY() + layout.previewBoxSize() <= layout.countModeY());
        assertTrue(layout.previewBoxY() + layout.previewBoxSize() <= layout.customFilterY() + 16);
    }
}
