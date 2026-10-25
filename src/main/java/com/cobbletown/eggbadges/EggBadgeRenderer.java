package com.cobbletown.eggbadges;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.slot.Slot;

/** Cache-first three-channel painter. Lore is classified on ticks, never from rendering. */
public final class EggBadgeRenderer {
    static final int BACKGROUND_ALPHA = 0x68;
    /** Opaque dark base makes the live fill match the darker preview appearance. */
    static final int BACKGROUND_UNDERLAY = 0xFF182028;
    /** Bounds initial classification of a full 90-slot container to five client ticks. */
    static final int SLOTS_PER_TICK = 18;
    static final int TRIANGLE_SIZE = 5;
    static final float OVERLAY_Z = 200.0f;
    static final int HIDDEN_ABILITY_COLOR = 0xFFFFAA55;
    private static final int COUNT_GLYPH_WIDTH = 3, COUNT_GLYPH_HEIGHT = 5;
    private static final int COUNT_SHADOW = 0xC0181818;
    private static final int[] COUNT_COLORS = {
            0xFF9E9E9E, // 0 grey
            0xFFFFFFFF, // 1 white
            0xFF55DD77, // 2 green
            0xFF5599FF, // 3 blue
            0xFFFFD54A, // 4 yellow
            0xFFFFAA55, // 5 orange
            0xFFFF5555  // 6 red
    };

    /** 3x5 neutral pixel digits 0..6. Only the values a six-IV egg can produce are needed. */
    private static final int[][] DIGITS = {
            {0b111, 0b101, 0b101, 0b101, 0b111},
            {0b010, 0b110, 0b010, 0b010, 0b111},
            {0b111, 0b001, 0b111, 0b100, 0b111},
            {0b111, 0b001, 0b111, 0b001, 0b111},
            {0b101, 0b101, 0b111, 0b001, 0b001},
            {0b111, 0b100, 0b111, 0b001, 0b111},
            {0b111, 0b100, 0b111, 0b101, 0b111}
    };

    private static final BadgeSlotCache CACHE = new BadgeSlotCache();
    private static final BadgeSlotCache CURSOR = new BadgeSlotCache();
    private static Object handler;
    private static long lastRenderDiagnostic;
    private static int nextSlot;

    private EggBadgeRenderer() {}

    public static void tick(MinecraftClient client) {
        if (!(client.currentScreen instanceof HandledScreen<?> screen)) {
            CACHE.clearAll();
            CURSOR.clearAll();
            handler = null;
            nextSlot = 0;
            return;
        }
        var screenHandler = screen.getScreenHandler();
        if (handler != screenHandler) {
            handler = screenHandler;
            CACHE.clearAll();
            CACHE.resize(screenHandler.slots.size());
            CURSOR.clearAll();
            nextSlot = 0;
        } else {
            CACHE.resize(screenHandler.slots.size());
        }

        var slots = screenHandler.slots;
        for (int i = 0; i < slots.size(); i++) {
            ItemStack stack = slots.get(i).getStack();
            CACHE.observe(i, stack, stack.getItem(), stack.getComponents(), stack.getCount());
        }

        int processed = 0;
        int slotIndex = nextSlot;
        for (int checked = 0; checked < slots.size() && processed < SLOTS_PER_TICK; checked++) {
            if (CACHE.isDirty(slotIndex)) {
                classifySlot(slots.get(slotIndex).getStack(), slotIndex);
                processed++;
            }
            slotIndex++;
            if (slotIndex >= slots.size()) slotIndex = 0;
        }
        nextSlot = slotIndex;

        tickCursor(screenHandler.getCursorStack());

        if (EggBadgesConfig.INSTANCE.debug()) {
            long now = System.nanoTime();
            if (now - lastRenderDiagnostic > 1_000_000_000L) {
                lastRenderDiagnostic = now;
                EggBadgesConfig.debug("tick slots=" + slots.size() + " budget=" + SLOTS_PER_TICK);
            }
        }
    }

    private static void classifySlot(ItemStack stack, int index) {
        if (stack.isEmpty()) {
            CACHE.clear(index, stack, stack.getItem(), stack.getComponents(), stack.getCount());
            return;
        }
        EggAttributes attributes = classify(stack);
        if (attributes == null) {
            if (CACHE.state(index) == null)
                CACHE.clear(index, stack, stack.getItem(), stack.getComponents(), stack.getCount());
            else
                CACHE.miss(index, stack.getItem());
            return;
        }
        CACHE.accept(index, stack, stack.getItem(), stack.getComponents(), stack.getCount(), resolve(attributes));
    }

    private static EggAttributes classify(ItemStack stack) {
        return HiddenAbilityResolver.resolve(EggClassifier.readNew(stack).attributes(),
                HiddenAbilityLookupProvider.get());
    }

    private static void tickCursor(ItemStack stack) {
        if (stack.isEmpty()) {
            CURSOR.clearAll();
            return;
        }
        CURSOR.resize(1);
        CURSOR.observe(0, stack, stack.getItem(), stack.getComponents(), stack.getCount());
        if (!CURSOR.isDirty(0)) return;
        EggAttributes attributes = classify(stack);
        if (attributes == null) {
            if (CURSOR.state(0) == null)
                CURSOR.clear(0, stack, stack.getItem(), stack.getComponents(), stack.getCount());
            else
                CURSOR.miss(0, stack.getItem());
            return;
        }
        CURSOR.accept(0, stack, stack.getItem(), stack.getComponents(), stack.getCount(), resolve(attributes));
    }

    static BadgeState resolve(EggAttributes attributes) {
        return resolve(attributes, EggBadgesConfig.INSTANCE);
    }

    static BadgeState resolve(EggAttributes attributes, EggBadgesConfig config) {
        if (config == null) return new BadgeState(null, null, "");
        return resolve(attributes, config.background, config.icon, config.gender,
                config.show_perfect_iv_count, config.show_hidden_ability);
    }

    /** Compatibility overload for tests and older call sites. */
    static BadgeState resolve(EggAttributes attributes, ChannelConfig background, ChannelConfig icon,
                              GenderConfig gender, boolean showPerfectIvCount) {
        return resolve(attributes, background, icon, gender, showPerfectIvCount, false);
    }

    /** Pure resolution shared by live rendering, main previews, and tests. */
    static BadgeState resolve(EggAttributes attributes, ChannelConfig background, ChannelConfig icon,
                              GenderConfig gender, boolean showPerfectIvCount, boolean showHiddenAbility) {
        if (attributes == null) return new BadgeState(null, null, "");

        Integer backgroundArgb = null;
        if (EggClassifier.matches(background, attributes)) {
            if (background.condition == BadgeCondition.GENDER) {
                int maleColor = gender == null ? 0xFF5599FF : gender.male_color;
                int femaleColor = gender == null ? 0xFFFF5555 : gender.female_color;
                if ("Male".equals(attributes.gender())) backgroundArgb = backgroundColor(maleColor);
                else if ("Female".equals(attributes.gender())) backgroundArgb = backgroundColor(femaleColor);
            } else {
                backgroundArgb = backgroundColor(background.color);
            }
        }

        BadgeState.IconState iconState = null;
        if (EggClassifier.matches(icon, attributes)) {
            if (icon.condition == BadgeCondition.IV_COUNT) {
                // Legacy schema-9 shape: lift the embedded count, never draw the old V glyph.
                int legacyCount = EggClassifier.perfectCount(attributes.ivs());
                if (legacyCount >= 0) showPerfectIvCount = true;
            } else if (icon.shape != null) {
                iconState = new BadgeState.IconState(IconShape.CIRCLE.glyph, icon.color,
                        BadgeState.Anchor.TOP_RIGHT);
            }
        }

        Integer perfectIvCount = null;
        if (showPerfectIvCount) {
            int count = EggClassifier.perfectCount(attributes.ivs());
            if (count >= 0) perfectIvCount = count;
        }

        // IV Count and Badge share the upper-right position and are mutually exclusive.
        if (iconState != null && perfectIvCount != null) iconState = null;

        String genderValue = "";
        if (gender != null && gender.show && ("Male".equals(attributes.gender()) || "Female".equals(attributes.gender())))
            genderValue = attributes.gender();

        boolean hiddenAbility = showHiddenAbility
                && attributes.hiddenAbility() == HiddenAbilityStatus.HIDDEN;
        return new BadgeState(backgroundArgb, iconState, genderValue,
                gender == null ? GenderStyle.CORNER : GenderStyle.from(gender.style),
                perfectIvCount, hiddenAbility);
    }

    /** Cursor mixin calls this before vanilla draws the held item. */
    public static void renderCursorBackground(DrawContext context, HandledScreen<?> screen, ItemStack stack, int x, int y) {
        BadgeState state = cursorState(screen, stack);
        if (state != null && state.backgroundArgb() != null) drawBackground(context, x, y, state);
    }

    /** Cursor mixin calls this after vanilla draws the held item, at the same origin. */
    public static void renderCursorOverlays(DrawContext context, HandledScreen<?> screen, ItemStack stack, int x, int y) {
        BadgeState state = cursorState(screen, stack);
        if (state != null) drawOverlaysForeground(context, x, y, state);
    }

    private static BadgeState cursorState(HandledScreen<?> screen, ItemStack stack) {
        if (screen == null || stack == null || stack.isEmpty()) return null;
        if (stack != screen.getScreenHandler().getCursorStack()) return null;
        BadgeState state = CURSOR.state(0);
        if (state == null || !CURSOR.sameItem(0, stack.getItem())) return null;
        return state;
    }

    /** Mixin invokes this before vanilla's item sprite; coordinates are slot-local. */
    public static void renderSlotBackground(HandledScreen<?> screen, DrawContext context, Slot slot) {
        int index = slotIndex(screen, slot);
        BadgeState state = CACHE.state(index);
        ItemStack stack = slot.getStack();
        if (state != null && state.backgroundArgb() != null && !stack.isEmpty()
                && CACHE.sameItem(index, stack.getItem()))
            drawBackground(context, slot.x, slot.y, state);
    }

    public static void render(Screen screen, DrawContext context, int mouseX, int mouseY) {
        if (!(screen instanceof HandledScreen<?> handled) || handler != handled.getScreenHandler()) return;
        int rendered = 0;
        var slots = handled.getScreenHandler().slots;
        for (int i = 0; i < slots.size(); i++) {
            Slot slot = slots.get(i);
            BadgeState state = CACHE.state(i);
            ItemStack stack = slot.getStack();
            if (state == null || stack.isEmpty() || !CACHE.sameItem(i, stack.getItem())) continue;
            drawOverlaysForeground(context, handled.x + slot.x, handled.y + slot.y, state);
            rendered++;
        }
        if (rendered > 0 && EggBadgesConfig.INSTANCE.debug()) {
            long now = System.nanoTime();
            if (now - lastRenderDiagnostic > 1_000_000_000L) {
                lastRenderDiagnostic = now;
                EggBadgesConfig.debug("render slots=" + rendered + " channels=three");
            }
        }
    }

    private static int slotIndex(HandledScreen<?> screen, Slot slot) {
        var slots = screen.getScreenHandler().slots;
        int id = slot.id;
        if (id >= 0 && id < slots.size() && slots.get(id) == slot) return id;
        for (int i = 0; i < slots.size(); i++) if (slots.get(i) == slot) return i;
        return -1;
    }

    static int iconX(int slotX, int glyphWidth) {
        return Math.max(slotX, slotX + 16 - glyphWidth);
    }

    static int iconY(int slotY) {
        return slotY;
    }

    static void drawBackground(DrawContext c, int x, int y, BadgeState state) {
        if (state.backgroundArgb() == null) return;
        c.fill(x, y, x + 16, y + 16, BACKGROUND_UNDERLAY);
        c.fill(x, y, x + 16, y + 16, state.backgroundArgb());
    }

    static void drawOverlays(DrawContext c, int x, int y, BadgeState state) {
        if (state.gender() != null && !state.gender().isEmpty()) {
            boolean male = "Male".equals(state.gender());
            int color = male ? 0xFF5599FF : 0xFFFF5555;
            if (state.genderStyle() == GenderStyle.LETTERS) {
                var font = MinecraftClient.getInstance().textRenderer;
                c.drawTextWithShadow(font, male ? "M" : "F", x + 1, y + 1, color);
            } else {
                drawCornerTriangle(c, x, y, color);
            }
        }
        if (state.hiddenAbility()) drawCornerTriangleBottomRight(c, x, y, HIDDEN_ABILITY_COLOR);
        if (state.perfectIvCount() != null) drawCountGlyph(c, x, y, state.perfectIvCount());
        if (state.icon() != null) {
            var font = MinecraftClient.getInstance().textRenderer;
            int iconX = iconX(x, font.getWidth(state.icon().glyph()));
            int iconY = iconY(y);
            c.drawTextWithShadow(font, state.icon().glyph(), iconX, iconY, state.icon().color());
        }
    }

    /** Draw overlays above item sprites; item sprites use a higher z than default GUI text. */
    static void drawOverlaysForeground(DrawContext c, int x, int y, BadgeState state) {
        var matrices = c.getMatrices();
        matrices.push();
        matrices.translate(0.0f, 0.0f, OVERLAY_Z);
        drawOverlays(c, x, y, state);
        matrices.pop();
    }

    /** Solid right triangle, right angle at the slot's upper-left, 5x5 native GUI pixels. */
    static void drawCornerTriangle(DrawContext c, int x, int y, int color) {
        for (int row = 0; row < TRIANGLE_SIZE; row++)
            c.fill(x, y + row, x + triangleWidthAtRow(row), y + row + 1, color);
    }

    static int triangleWidthAtRow(int row) {
        return row < 0 || row >= TRIANGLE_SIZE ? 0 : TRIANGLE_SIZE - row;
    }

    /**
     * Solid right triangle, right angle at the slot's lower-right. The wide row is at the
     * bottom and the point is at the top-right, mirroring the upper-left gender triangle.
     */
    static void drawCornerTriangleBottomRight(DrawContext c, int x, int y, int color) {
        for (int row = 0; row < TRIANGLE_SIZE; row++) {
            int width = row + 1;
            c.fill(x + 16 - width, y + 16 - TRIANGLE_SIZE + row,
                    x + 16, y + 16 - TRIANGLE_SIZE + row + 1, color);
        }
    }

    static int bottomRightTriangleStart(int x, int row) {
        return row < 0 || row >= TRIANGLE_SIZE ? x + 16 : x + 16 - (row + 1);
    }

    static int countColor(int count) {
        return count < 0 || count >= COUNT_COLORS.length ? COUNT_COLORS[0] : COUNT_COLORS[count];
    }

    static boolean digitPixel(int digit, int row, int column) {
        if (digit < 0 || digit >= DIGITS.length || row < 0 || row >= COUNT_GLYPH_HEIGHT
                || column < 0 || column >= COUNT_GLYPH_WIDTH) return false;
        return (DIGITS[digit][row] & (1 << (COUNT_GLYPH_WIDTH - 1 - column))) != 0;
    }

    static int countGlyphWidth() { return COUNT_GLYPH_WIDTH; }
    static int countGlyphHeight() { return COUNT_GLYPH_HEIGHT; }

    /** Compact 3x5 Arabic numeral in the upper-right, with a 1 px dark shadow. */
    static void drawCountGlyph(DrawContext c, int x, int y, int count) {
        int glyphX = x + 16 - COUNT_GLYPH_WIDTH - 1;
        int glyphY = y + 1;
        for (int row = 0; row < COUNT_GLYPH_HEIGHT; row++)
            for (int column = 0; column < COUNT_GLYPH_WIDTH; column++)
                if (digitPixel(count, row, column))
                    c.fill(glyphX + column + 1, glyphY + row + 1,
                            glyphX + column + 2, glyphY + row + 2, COUNT_SHADOW);
        for (int row = 0; row < COUNT_GLYPH_HEIGHT; row++)
            for (int column = 0; column < COUNT_GLYPH_WIDTH; column++)
                if (digitPixel(count, row, column))
                    c.fill(glyphX + column, glyphY + row, glyphX + column + 1, glyphY + row + 1, countColor(count));
    }

    static int backgroundColor(int color) { return (color & 0x00FFFFFF) | (BACKGROUND_ALPHA << 24); }

    static int screenLocalSlotCoordinate(int coordinate) { return coordinate; }

    // Compatibility helpers retained for existing focused layout/legacy tests.
    static int computeLaneX(int badge, boolean cursor) { return switch (badge) { case 0, 2 -> 0; case 1, 3 -> 8; default -> 4; }; }
    static int computeLaneY(int badge, boolean cursor) { return switch (badge) { case 0, 1 -> cursor ? 0 : 8; case 2, 3 -> cursor ? 8 : 0; default -> 4; }; }

    static boolean isQualityRule(BadgeRule rule) {
        if (rule == null || !rule.isIvClassification) return false;
        if ("perfect".equals(rule.preset) || "five".equals(rule.preset)
                || !"none".equals(rule.perfectCountMode) || rule.remainderMin >= 0) return true;
        if (rule.values != null) for (String v : rule.values) if (v != null && !v.isBlank()) return true;
        if (rule.ivMinimums != null) for (int v : rule.ivMinimums) if (v >= 0) return true;
        return false;
    }

    static int applyPrecedence(int mask, BadgeRule[] rules) {
        if (rules == null) return mask;
        int winner = -1, other = 0;
        for (int i = 0; i < rules.length; i++) if ((mask & (1 << i)) != 0) {
            if (isQualityRule(rules[i])) {
                if (winner < 0) winner = i;
            } else other |= 1 << i;
        }
        return winner < 0 ? other : (other | (1 << winner));
    }

    static int suppressDuplicateCount(int mask, BadgeRule[] rules, int count) {
        if (rules == null) return mask;
        for (int i = 0; i < rules.length; i++) {
            BadgeRule r = rules[i];
            if (r != null && r.enabled && "exactly".equals(r.perfectCountMode) && r.perfectCount == count)
                mask &= ~(1 << i);
        }
        return mask;
    }

    /** Compatibility fingerprint helper; schema 12 itself fingerprints channel state directly. */
    static final class Cache {
        static int rulesHash(BadgeRule[] rules) {
            int h = 1;
            if (rules != null) for (BadgeRule r : rules) h = 31 * h + (r == null ? 0 : java.util.Objects.hash(
                    r.id, r.label, r.glyph, r.presentation, r.color, r.enabled, r.species, r.form, r.nature,
                    r.gender, r.preset, r.perfectCountMode, r.perfectCount, r.remainderMin,
                    r.isIvClassification, java.util.Arrays.hashCode(r.operators),
                    java.util.Arrays.hashCode(r.values), java.util.Arrays.hashCode(r.ivMinimums)));
            return h;
        }

        static int configHash(BadgeRule[] rules, AutoIndicatorConfig a) {
            return 31 * rulesHash(rules) + (a == null ? 0 : java.util.Objects.hash(
                    a.show_gender, a.show_perfect_iv_count, a.minimum_perfect_ivs,
                    a.gender_presentation, a.iv_count_presentation));
        }
    }
}
