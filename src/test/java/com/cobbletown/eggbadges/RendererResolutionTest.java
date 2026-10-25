package com.cobbletown.eggbadges;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class RendererResolutionTest {
    private static EggAttributes egg(String gender, int... ivs) {
        return new EggAttributes("Eevee", "", "", gender, ivs);
    }

    private static EggBadgesConfig defaults() {
        EggBadgesConfig config = new EggBadgesConfig();
        config.resetInMemory();
        return config;
    }

    @Test void approvedDefaultsRenderOnlyTheGreenSixBackground() {
        EggBadgesConfig config = defaults();
        BadgeState state = EggBadgeRenderer.resolve(egg("Female", 31, 31, 31, 31, 31, 31), config);
        assertEquals(EggBadgeRenderer.backgroundColor(QuickSettings.STANDARD_SIX_COLOR), state.backgroundArgb());
        assertEquals("", state.gender(), "Show Gender is off by default");
        assertNull(state.perfectIvCount());
        assertNull(state.icon(), "no Badge option is selected by default");
    }

    @Test void showGenderOffRemovesTriangleAndLettersInPreviewsAndLiveState() {
        EggBadgesConfig config = defaults();
        config.gender.show = false;
        BadgeState state = EggBadgeRenderer.resolve(egg("Male", 31, 31, 31, 31, 31, 31), config);
        assertEquals("", state.gender());
        assertFalse(state.empty(), "background still renders");

        config.gender.show = true;
        config.gender.style = GenderStyle.LETTERS.name();
        BadgeState letters = EggBadgeRenderer.resolve(egg("Male", 31, 31, 31, 31, 31, 31), config);
        assertEquals("Male", letters.gender());
        assertEquals(GenderStyle.LETTERS, letters.genderStyle());
    }

    @Test void genderlessNeverGetsAnIndicator() {
        EggBadgesConfig config = defaults();
        BadgeState state = EggBadgeRenderer.resolve(egg("Genderless", 0, 0, 0, 0, 0, 0), config);
        assertEquals("", state.gender());
    }

    @Test void countIsIndependentOfHighlightConditions() {
        EggBadgesConfig config = defaults();
        config.background.configured = false;
        config.icon.configured = false;
        config.show_perfect_iv_count = true;
        BadgeState state = EggBadgeRenderer.resolve(egg("", 31, 31, 31, 31, 31, 30), config);
        assertEquals(5, state.perfectIvCount());
        assertNull(state.backgroundArgb());
        assertNull(state.icon());
    }

    @Test void countSupportsEveryValueZeroThroughSix() {
        EggBadgesConfig config = defaults();
        config.background.configured = false;
        config.show_perfect_iv_count = true;
        for (int perfect = 0; perfect <= 6; perfect++) {
            int[] ivs = new int[6];
            for (int i = 0; i < 6; i++) ivs[i] = i < perfect ? 31 : 0;
            assertEquals(perfect, EggBadgeRenderer.resolve(egg("", ivs), config).perfectIvCount());
        }
    }

    @Test void incompleteIvsNeverProduceACount() {
        EggBadgesConfig config = defaults();
        config.show_perfect_iv_count = true;
        assertNull(EggBadgeRenderer.resolve(egg("", 31, 31, 31, 31, 31, -1), config).perfectIvCount());
    }

    @Test void countTakesPrecedenceOverTheStandardFiveDot() {
        EggBadgesConfig config = defaults();
        QuickSettings.setFive(config.icon, true);
        config.show_perfect_iv_count = true;
        BadgeState state = EggBadgeRenderer.resolve(egg("", 31, 31, 31, 31, 31, 30), config);
        assertEquals(5, state.perfectIvCount());
        assertNull(state.icon(), "the blue dot must yield to the numeral in the upper-right");

        config.show_perfect_iv_count = false;
        BadgeState dot = EggBadgeRenderer.resolve(egg("", 31, 31, 31, 31, 31, 30), config);
        assertNotNull(dot.icon());
        assertEquals(BadgeState.Anchor.TOP_RIGHT, dot.icon().anchor());
    }

    @Test void countSuppressesTheCircleBecauseTheyShareTheUpperRight() {
        EggBadgesConfig config = defaults();
        config.icon.configured = true;
        config.icon.condition = BadgeCondition.CUSTOM;
        config.show_perfect_iv_count = true;
        BadgeState state = EggBadgeRenderer.resolve(egg("", 31, 31, 31, 31, 31, 30), config);
        assertEquals(5, state.perfectIvCount());
        assertNull(state.icon());
    }

    @Test void circleBadgeUsesTheCorrectedUpperRightOrigin() {
        assertEquals(12, EggBadgeRenderer.iconX(0, 4));
        assertEquals(11, EggBadgeRenderer.iconX(0, 5));
        assertEquals(0, EggBadgeRenderer.iconY(0));
        assertEquals(12, EggBadgeRenderer.iconX(1, 5));
    }

    @Test void fiveAndSixDetectionAreExactAcrossEveryStatPosition() {
        int[][] fiveEggs = {
                {30, 31, 31, 31, 31, 31},
                {31, 30, 31, 31, 31, 31},
                {31, 31, 30, 31, 31, 31},
                {31, 31, 31, 30, 31, 31},
                {31, 31, 31, 31, 30, 31},
                {31, 31, 31, 31, 31, 30}
        };
        ChannelConfig five = ChannelConfig.iconDefault();
        five.configured = true;
        ChannelConfig six = ChannelConfig.backgroundDefault();
        for (int[] ivs : fiveEggs) {
            assertTrue(EggClassifier.matches(five, egg("", ivs)), "any five-IV position must match");
            assertFalse(EggClassifier.matches(six, egg("", ivs)));
        }
        assertTrue(EggClassifier.matches(six, egg("", 31, 31, 31, 31, 31, 31)));
        assertFalse(EggClassifier.matches(five, egg("", 31, 31, 31, 31, 31, 31)));
    }

    @Test void triangleIsASolidFiveByFiveCornerTriangle() {
        assertEquals(5, EggBadgeRenderer.TRIANGLE_SIZE);
        assertEquals(5, EggBadgeRenderer.triangleWidthAtRow(0));
        assertEquals(4, EggBadgeRenderer.triangleWidthAtRow(1));
        assertEquals(1, EggBadgeRenderer.triangleWidthAtRow(4));
        assertEquals(0, EggBadgeRenderer.triangleWidthAtRow(5));
    }

    @Test void everyCountDigitHasPixelsInsideTheCompactGlyphBounds() {
        assertEquals(3, EggBadgeRenderer.countGlyphWidth());
        assertEquals(5, EggBadgeRenderer.countGlyphHeight());
        for (int digit = 0; digit <= 6; digit++) {
            int pixels = 0;
            for (int row = 0; row < 5; row++)
                for (int column = 0; column < 3; column++)
                    if (EggBadgeRenderer.digitPixel(digit, row, column)) pixels++;
            assertTrue(pixels > 5, "digit " + digit + " should be recognizable");
        }
        assertFalse(EggBadgeRenderer.digitPixel(7, 0, 0));
    }

    @Test void countZeroStillRendersAValueRatherThanSuppressingTheIndicator() {
        EggBadgesConfig config = defaults();
        config.show_perfect_iv_count = true;
        BadgeState state = EggBadgeRenderer.resolve(egg("", 0, 0, 0, 0, 0, 0), config);
        assertEquals(0, state.perfectIvCount());
        assertFalse(state.empty());
    }

    @Test void hiddenAbilityTriangleRequiresAConfirmedHiddenResultAndTheSetting() {
        EggBadgesConfig config = defaults();
        EggAttributes hidden = new EggAttributes("Bulbasaur", "normal", "Gentle", "Male",
                new int[]{31, 31, 31, 31, 31, 31}, "chlorophyll", HiddenAbilityStatus.HIDDEN);
        EggAttributes normal = new EggAttributes("Bulbasaur", "normal", "Gentle", "Male",
                new int[]{31, 31, 31, 31, 31, 31}, "overgrow", HiddenAbilityStatus.NORMAL);
        EggAttributes unresolved = new EggAttributes("Bulbasaur", "normal", "Gentle", "Male",
                new int[]{31, 31, 31, 31, 31, 31}, "chlorophyll", HiddenAbilityStatus.UNRESOLVED);

        assertFalse(EggBadgeRenderer.resolve(hidden, config).hiddenAbility(), "setting defaults off");
        config.show_hidden_ability = true;
        assertTrue(EggBadgeRenderer.resolve(hidden, config).hiddenAbility());
        assertFalse(EggBadgeRenderer.resolve(normal, config).hiddenAbility());
        assertFalse(EggBadgeRenderer.resolve(unresolved, config).hiddenAbility());
    }

    @Test void hiddenAbilityTriangleUsesTheOppositeCornerAndOrange() {
        assertEquals(0xFFFFAA55, EggBadgeRenderer.HIDDEN_ABILITY_COLOR);
        assertEquals(15, EggBadgeRenderer.bottomRightTriangleStart(0, 0));
        assertEquals(11, EggBadgeRenderer.bottomRightTriangleStart(0, 4));
    }

    @Test void ivCountColorsAreFixedByCount() {
        assertEquals(0xFF9E9E9E, EggBadgeRenderer.countColor(0));
        assertEquals(0xFFFFFFFF, EggBadgeRenderer.countColor(1));
        assertEquals(0xFF55DD77, EggBadgeRenderer.countColor(2));
        assertEquals(0xFF5599FF, EggBadgeRenderer.countColor(3));
        assertEquals(0xFFFFD54A, EggBadgeRenderer.countColor(4));
        assertEquals(0xFFFFAA55, EggBadgeRenderer.countColor(5));
        assertEquals(0xFFFF5555, EggBadgeRenderer.countColor(6));
    }

    @Test void backgroundUsesASharedOpaqueDarkBaseForPreviewAndLiveSlots() {
        assertEquals(0xFF182028, EggBadgeRenderer.BACKGROUND_UNDERLAY);
        assertEquals(0xFF, (EggBadgeRenderer.BACKGROUND_UNDERLAY >>> 24) & 0xFF);
    }

    @Test void overlayLayerIsAboveTheItemSpriteLayer() {
        assertEquals(200.0f, EggBadgeRenderer.OVERLAY_Z);
        assertTrue(EggBadgeRenderer.OVERLAY_Z > 100.0f);
    }


    @Test void everyBadgeConditionUsesTheCircleInTheUpperRight() {
        EggBadgesConfig config = defaults();
        for (BadgeCondition condition : new BadgeCondition[]{BadgeCondition.PERFECT_6, BadgeCondition.FIVE_5, BadgeCondition.CUSTOM}) {
            config.icon.configured = true;
            config.icon.condition = condition;
            config.icon.shape = IconShape.STAR;
            BadgeState state = EggBadgeRenderer.resolve(egg("Female", 31, 31, 31, 31, 31, 30), config);
            if (condition == BadgeCondition.PERFECT_6) {
                assertNull(state.icon(), "the sample is not six-perfect");
            } else {
                assertNotNull(state.icon());
                assertEquals(IconShape.CIRCLE.glyph, state.icon().glyph());
                assertEquals(BadgeState.Anchor.TOP_RIGHT, state.icon().anchor());
            }
        }
    }

}
