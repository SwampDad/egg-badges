package com.cobbletown.eggbadges;

/** Pure geometry for the simplified Background + Icon settings screen. */
final class EggBadgeSettingsLayout {
    static final int ROW_HEIGHT = 16;
    static final int STAT_ROW_HEIGHT = 20;

    private EggBadgeSettingsLayout() {}

    record Panel(int x, int y, int width, int height) {
        int innerX() { return x + 12; }
        int innerWidth() { return width - 24; }
        int innerRight() { return x + width - 12; }
        int bottom() { return y + height; }
    }

    record Main(Panel panel, int titleY, int ruleY,
                int previewBoxX, int previewBoxY, int previewBoxSize,
                int previewEggX, int previewEggY, int previewEggSize, int previewScale,
                int contentX, int contentY, int leftWidth, int contentHeight,
                int contentTotalHeight, int scrollMax,
                int perfectHeadingY, int backgroundRowY,
                int iconHeadingY, int iconRuleY,
                int circleModeY, int circleColorY, int fiveFilterY, int customFilterY,
                int customStatsY, boolean customStatsVisible,
                int orY, int countModeY, int otherY,
                int versionLabelY, int bottomY, int resetWidth, int cancelWidth, int saveWidth) {}

    static Main main(int width, int height, boolean customStatsVisible) {
        int panelWidth = Math.max(200, Math.min(680, width - 8));
        int panelHeight = Math.max(160, Math.min(560, height - 8));
        int panelX = (width - panelWidth) / 2;
        int panelY = Math.max(4, (height - panelHeight) / 2);
        Panel panel = new Panel(panelX, panelY, panelWidth, panelHeight);

        int titleY = panelY + 10;
        int ruleY = titleY + 14;
        int contentY = titleY + 22;
        int bottomY = panel.bottom() - 24;
        // Footer label sits immediately above, never inside, the Save/Cancel button row.
        int versionLabelY = bottomY - 11;
        int contentBottom = bottomY - 6;
        int contentX = panel.innerX();
        int contentWidth = panel.innerWidth();

        int previewBoxSize = Math.max(64, Math.min(96, contentWidth / 6));
        // Keep the preview beside the controls rather than stranded at the panel edge.
        int previewOffset = Math.min(contentWidth - previewBoxSize, Math.max(180, (contentWidth * 2) / 3));
        int leftWidth = Math.max(140, previewOffset - 12);
        int previewScale = Math.max(3, (previewBoxSize - 12) / 16);
        int previewEggSize = previewScale * 16;
        int previewBoxX = contentX + previewOffset;

        int y = contentY;
        int perfectHeadingY = y; y += 12;
        int backgroundRowY = y; y += 22;
        int iconHeadingY = y; y += 14;
        int iconRuleY = iconHeadingY + 11;
        int circleModeY = y; y += 20;
        int circleColorY = y; y += 18;
        int fiveFilterY = y; y += 20;
        int customFilterY = y; y += 16;
        int customStatsY = customStatsVisible ? y + 2 : -1;
        if (customStatsVisible) y += 6 * STAT_ROW_HEIGHT + 4;
        int orY = y + 1; y += 8;
        int countModeY = y; y += 16;
        int otherY = y + 2; y += 20;

        int contentTotalHeight = otherY + 20 - contentY;
        int contentHeight = Math.max(0, contentBottom - contentY);
        int scrollMax = Math.max(0, contentTotalHeight - contentHeight);

        int previewRegionBottom = customFilterY + 16;
        int previewBoxY = contentY + Math.max(0,
                (previewRegionBottom - contentY - previewBoxSize) / 2);
        int previewEggX = previewBoxX + (previewBoxSize - previewEggSize) / 2;
        int previewEggY = previewBoxY + (previewBoxSize - previewEggSize) / 2;

        int inner = panel.innerWidth();
        int resetWidth = Math.min(92, Math.max(70, inner / 3));
        int remaining = Math.min(76, Math.max(52, inner / 8));

        return new Main(panel, titleY, ruleY, previewBoxX, previewBoxY, previewBoxSize,
                previewEggX, previewEggY, previewEggSize, previewScale,
                contentX, contentY, leftWidth, contentHeight, contentTotalHeight, scrollMax,
                perfectHeadingY, backgroundRowY, iconHeadingY, iconRuleY,
                circleModeY, circleColorY, fiveFilterY, customFilterY,
                customStatsY, customStatsVisible, orY, countModeY, otherY,
                versionLabelY, bottomY, resetWidth, remaining, remaining);
    }
}
