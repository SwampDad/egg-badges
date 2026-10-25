package com.cobbletown.eggbadges;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class QuickSettingsTest {
    @Test void standardStatesAndOffStatesAreDistinctFromCustom() {
        ChannelConfig background = ChannelConfig.backgroundDefault();
        ChannelConfig icon = ChannelConfig.iconDefault();
        assertEquals(QuickSettings.State.ON, QuickSettings.sixState(background));
        assertEquals(QuickSettings.State.OFF, QuickSettings.fiveState(icon));

        background.condition = BadgeCondition.CUSTOM;
        icon.configured = true;
        icon.condition = BadgeCondition.CUSTOM;
        assertEquals(QuickSettings.State.CUSTOM, QuickSettings.sixState(background));
        assertEquals(QuickSettings.State.CUSTOM, QuickSettings.fiveState(icon));
    }

    @Test void sixToggleEnablesTheStandardGreenBackgroundAndDisablesCleanly() {
        ChannelConfig background = ChannelConfig.backgroundDefault();
        QuickSettings.setSix(background, false);
        assertEquals(QuickSettings.State.OFF, QuickSettings.sixState(background));
        QuickSettings.setSix(background, true);
        assertEquals(QuickSettings.State.ON, QuickSettings.sixState(background));
        assertEquals(BadgeCondition.PERFECT_6, background.condition);
        assertEquals(QuickSettings.STANDARD_SIX_COLOR, background.color);
    }

    @Test void fiveToggleEnablesTheStandardBlueDotAndDisablesCleanly() {
        ChannelConfig icon = ChannelConfig.iconDefault();
        QuickSettings.setFive(icon, true);
        assertEquals(QuickSettings.State.ON, QuickSettings.fiveState(icon));
        assertEquals(BadgeCondition.FIVE_5, icon.condition);
        assertEquals(IconShape.CIRCLE, icon.shape);
        assertEquals(QuickSettings.STANDARD_FIVE_COLOR, icon.color);
        assertEquals(-1, icon.five_iv_minimum);
        QuickSettings.setFive(icon, false);
        assertEquals(QuickSettings.State.OFF, QuickSettings.fiveState(icon));
    }

    @Test void togglingAStandardQuickRuleDoesNotOverwriteACustomRule() {
        ChannelConfig background = ChannelConfig.backgroundDefault();
        background.condition = BadgeCondition.CUSTOM;
        background.color = 0xFFB388FF;
        QuickSettings.setSix(background, true);
        assertEquals(BadgeCondition.CUSTOM, background.condition);
        assertEquals(0xFFB388FF, background.color);
        assertEquals(QuickSettings.State.CUSTOM, QuickSettings.sixState(background));

        ChannelConfig icon = ChannelConfig.iconDefault();
        icon.configured = true;
        icon.condition = BadgeCondition.PERFECT_6;
        icon.color = 0xFFFFD54A;
        QuickSettings.setFive(icon, true);
        assertEquals(BadgeCondition.PERFECT_6, icon.condition);
        assertEquals(0xFFFFD54A, icon.color);
    }

    @Test void labelsNeverShowRawIdentifiers() {
        assertEquals("Highlight 6×31 Eggs: On", QuickSettings.toggleLabel("Highlight 6×31 Eggs", QuickSettings.State.ON));
        assertEquals("Highlight 5×31 Eggs: Custom…", QuickSettings.toggleLabel("Highlight 5×31 Eggs", QuickSettings.State.CUSTOM));
    }
}
