package com.cobbletown.eggbadges;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.CheckboxWidget;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import java.util.function.Consumer;

/** Simplified settings screen: Perfect background, one Icon mode, and two indicators. */
public final class EggBadgeSettingsScreen extends Screen {
    static final int PREVIEW_NEUTRAL_SLOT = 0xFF8B8B8B;
    static final int NO_SWATCH_BORDER = 0;
    static final int SELECTED_SWATCH_BORDER = 0xFFFFFFFF;
    private static final int SWATCH_SIZE = 12, SWATCH_GAP = 3;
    private static final int CUSTOM_EDIT_GAP = 12;
    private static final int HEADING = 0xFFFFD98A;
    private static final int MUTED = 0xFFB8C6D8;
    private static final int VERSION_LABEL = 0xFF8492A4;

    private final Screen parent;
    private final EggBadgeDraftModel draft;
    private final Identifier[] textures = {
            Identifier.of("daycareplus", "textures/item/pokemon_egg.png"),
            Identifier.of("cobblemon", "textures/item/pokemon_egg.png"),
            Identifier.of("minecraft", "textures/item/egg.png")
    };

    private Identifier previewTexture;
    private EggBadgeSettingsLayout.Main layout;
    private ButtonWidget saveButton;
    private double scroll;
    private boolean customExpanded;

    public EggBadgeSettingsScreen(Screen parent) {
        super(Text.literal("Egg Badges"));
        this.parent = parent;
        this.draft = new EggBadgeDraftModel(EggBadgesConfig.INSTANCE);
    }

    public static EggBadgeSettingsScreen create(Screen parent) {
        return new EggBadgeSettingsScreen(parent);
    }

    @Override protected void init() {
        resolveTexture();
        clearChildren();
        addDrawable((context, mouseX, mouseY, delta) -> renderContent(context));
        initMain();
        EggBadgesConfig.debug("settings scroll=" + scroll);
    }

    private void initMain() {
        SettingsSelection.IconMode mode = SettingsSelection.mode(draft);
        boolean customVisible = mode == SettingsSelection.IconMode.CIRCLE
                && SettingsSelection.filter(draft) == SettingsSelection.CircleFilter.CUSTOM
                && customExpanded;
        layout = EggBadgeSettingsLayout.main(width, height, customVisible);
        scroll = clamp(scroll, 0, layout.scrollMax());

        addBackgroundRow();
        addIconControls(mode);
        if (customVisible) addCustomRows(layout.customStatsY());
        addOtherRow();

        int x = layout.contentX();
        int bottom = layout.bottomY();
        int gap = 8;
        addDrawableChild(button(x, bottom, layout.resetWidth(), 20, "Reset All", b -> {
            draft.reset();
            customExpanded = false;
            scroll = 0;
            refresh("reset-all");
        }));
        int saveX = layout.panel().innerRight() - layout.saveWidth();
        int cancelX = saveX - gap - layout.cancelWidth();
        addDrawableChild(button(cancelX, bottom, layout.cancelWidth(), 20,
                "Cancel", b -> close()));
        saveButton = button(saveX,
                bottom, layout.saveWidth(), 20, "Save", b -> save());
        saveButton.active = dirty();
        addDrawableChild(saveButton);
    }

    private void addBackgroundRow() {
        int x = layout.contentX();
        addCheckbox(x, rowY(layout.backgroundRowY()), compactWidth("Background"), "Background",
                SettingsSelection.backgroundOn(draft), value -> {
                    if (value) SettingsSelection.setBackground(draft, draft.background.color);
                    else SettingsSelection.setBackgroundOff(draft);
                    refresh("background=" + value);
                });
        x += compactWidth("Background") + 16;
        int[] colors = SettingsSelection.backgroundColors();
        for (int i = 0; i < colors.length; i++) {
            int color = colors[i];
            addContent(new ColorSwatchWidget(x + i * (SWATCH_SIZE + SWATCH_GAP), rowY(layout.backgroundRowY()),
                    SWATCH_SIZE, color, SettingsSelection.backgroundOn(draft)
                    && draft.background.color == color, AppearancePalette.name(color), b -> {
                        SettingsSelection.setBackground(draft, color);
                        refresh("background=" + AppearancePalette.name(color));
                    }));
        }
    }

    private void addIconControls(SettingsSelection.IconMode mode) {
        boolean circle = mode == SettingsSelection.IconMode.CIRCLE;
        boolean count = mode == SettingsSelection.IconMode.COUNT;

        int[] colors = SettingsSelection.circleColors();
        int colorX = layout.contentX() + 46;
        for (int i = 0; i < colors.length; i++) {
            int color = colors[i];
            ColorSwatchWidget swatch = new ColorSwatchWidget(colorX + i * (SWATCH_SIZE + SWATCH_GAP),
                    rowY(layout.circleColorY()), SWATCH_SIZE, color,
                    circle && draft.icon.color == color, AppearancePalette.name(color), b -> {
                        draft.icon.color = color;
                        refresh("circle-color=" + AppearancePalette.name(color));
                    });
            addContent(swatch);
            swatch.active = swatch.visible && circle;
        }

        CheckboxWidget five = CheckboxWidget.builder(Text.literal("5×31"), textRenderer)
                .pos(layout.contentX(), rowY(layout.fiveFilterY()))
                .checked(circle && SettingsSelection.filter(draft) == SettingsSelection.CircleFilter.FIVE)
                .maxWidth(58)
                .callback((widget, value) -> {
                    SettingsSelection.selectFilter(draft, SettingsSelection.CircleFilter.FIVE, value);
                    customExpanded = false;
                    refresh("circle-filter=five=" + value);
                }).build();
        addContent(five);
        five.active = five.visible;

        int sixthX = layout.contentX() + 64;
        CheckboxWidget restriction = CheckboxWidget.builder(Text.literal("6th IV >"), textRenderer)
                .pos(sixthX, rowY(layout.fiveFilterY()))
                .checked(draft.icon.five_iv_restricted)
                .maxWidth(62)
                .callback((widget, value) -> {
                    draft.icon.five_iv_restricted = value;
                    refresh("five-restricted=" + value);
                })
                .build();
        addContent(restriction);
        restriction.active = restriction.visible && circle
                && SettingsSelection.filter(draft) == SettingsSelection.CircleFilter.FIVE;

        int fieldX = sixthX + 64;
        TextFieldWidget field = new TextFieldWidget(textRenderer, fieldX, rowY(layout.fiveFilterY()) + 1,
                30, 16, Text.literal("6th IV threshold"));
        field.setMaxLength(2);
        field.setTextPredicate(EggBadgeSettingsScreen::validFifthAbove);
        field.setText(String.valueOf(SettingsSelection.fiveIvAbove(draft.icon)));
        field.setChangedListener(text -> {
            int value = fifthAboveValue(text);
            if (value >= 0) {
                SettingsSelection.setFiveIvAbove(draft.icon, value);
                if (saveButton != null) saveButton.active = dirty();
            }
        });
        addContent(field);
        field.active = field.visible && circle && draft.icon.five_iv_restricted
                && SettingsSelection.filter(draft) == SettingsSelection.CircleFilter.FIVE;

        int customWidth = compactWidth("Custom IV Filter");
        CheckboxWidget custom = CheckboxWidget.builder(Text.literal("Custom IV Filter"), textRenderer)
                .pos(layout.contentX(), rowY(layout.customFilterY()))
                .checked(circle && SettingsSelection.filter(draft) == SettingsSelection.CircleFilter.CUSTOM)
                .maxWidth(customWidth)
                .callback((widget, value) -> {
                    SettingsSelection.selectFilter(draft, SettingsSelection.CircleFilter.CUSTOM, value);
                    customExpanded = value;
                    refresh("circle-filter=custom=" + value);
                }).build();
        addContent(custom);
        custom.active = custom.visible;

        ButtonWidget edit = addContent(button(layout.contentX() + customWidth + CUSTOM_EDIT_GAP,
                rowY(layout.customFilterY()), 42, 16, "Edit", b -> {
                    customExpanded = !customExpanded;
                    refresh("custom-expanded=" + customExpanded);
                }));
        edit.active = edit.visible && circle
                && SettingsSelection.filter(draft) == SettingsSelection.CircleFilter.CUSTOM;

        addCheckbox(layout.contentX(), rowY(layout.countModeY()), compactWidth("Show # of 31 IVs"),
                "Show # of 31 IVs", count, value -> chooseMode(SettingsSelection.IconMode.COUNT));
    }

    private void addCustomRows(int absoluteY) {
        int x = layout.contentX();
        int rowWidth = layout.leftWidth();
        int checkboxWidth = Math.min(84, Math.max(54, rowWidth / 3));
        int operatorWidth = 24;
        int fieldWidth = Math.min(38, Math.max(26, rowWidth - checkboxWidth - operatorWidth - 16));
        int operatorX = x + checkboxWidth + 8;
        int fieldX = operatorX + operatorWidth + 4;

        for (int i = 0; i < 6; i++) {
            StatCondition condition = draft.icon.stats[i];
            boolean enabled = condition.operator != null && condition.operator != StatCondition.Operator.ANY;
            int y = rowY(absoluteY + i * EggBadgeSettingsLayout.STAT_ROW_HEIGHT);
            int index = i;
            addContent(CheckboxWidget.builder(Text.literal(SettingsLabels.statName(i)), textRenderer)
                    .pos(x, y).checked(enabled).maxWidth(checkboxWidth)
                    .callback((widget, value) -> {
                        setStatEnabled(condition, value);
                        refresh("custom-enabled=" + index);
                    }).build());
            ButtonWidget operator = addContent(button(operatorX, y + 3, operatorWidth, 14,
                    operatorLabel(condition), b -> {
                        toggleOperator(condition);
                        refresh("custom-operator=" + index);
                    }));
            operator.active = operator.visible && enabled;
            TextFieldWidget field = new TextFieldWidget(textRenderer, fieldX, y + 3, fieldWidth, 14,
                    Text.literal("IV threshold"));
            field.setMaxLength(2);
            field.setTextPredicate(EggBadgeSettingsScreen::validSixthIv);
            field.setPlaceholder(Text.literal("31"));
            field.setText(enabled && condition.value >= 0 ? String.valueOf(condition.value) : "");
            field.setChangedListener(text -> {
                int value = sixthIvValue(text);
                if (value >= 0) {
                    condition.value = value;
                    if (saveButton != null) saveButton.active = dirty();
                }
            });
            addContent(field);
            field.active = field.visible && enabled;
        }
    }

    private void addOtherRow() {
        int x = layout.contentX();
        int y = rowY(layout.otherY());
        int genderWidth = Math.min(92, textRenderer.getWidth("Show Gender") + 24);
        int hiddenWidth = Math.min(142, textRenderer.getWidth("Show Hidden Ability") + 24);
        addCheckbox(x, y, genderWidth, "Show Gender", draft.gender.show, value -> {
            draft.gender.show = value;
            refresh("gender=" + value);
        });
        addCheckbox(x + genderWidth + 8, y, hiddenWidth, "Show Hidden Ability", draft.showHiddenAbility, value -> {
            draft.showHiddenAbility = value;
            refresh("hidden-ability=" + value);
        });
    }

    private void chooseMode(SettingsSelection.IconMode clicked) {
        SettingsSelection.IconMode next = SettingsSelection.selectMode(draft, clicked);
        if (next == SettingsSelection.IconMode.CIRCLE
                && SettingsSelection.filter(draft) == SettingsSelection.CircleFilter.CUSTOM) {
            customExpanded = true;
        }
        refresh("icon-mode=" + next);
    }

    private void addCheckbox(int x, int y, int width, String label, boolean checked, Consumer<Boolean> action) {
        if (width < 20) return;
        addContent(CheckboxWidget.builder(Text.literal(label), textRenderer)
                .pos(x, y).checked(checked).maxWidth(width).callback((widget, value) -> action.accept(value)).build());
    }

    private <T extends ClickableWidget> T addContent(T widget) {
        boolean visible = isFullyVisible(widget.getY(), widget.getHeight());
        widget.visible = visible;
        widget.active = visible;
        addDrawableChild(widget);
        return widget;
    }

    private boolean isFullyVisible(int y, int height) {
        return y >= layout.contentY() && y + height <= layout.contentY() + layout.contentHeight();
    }

    private int rowY(int absoluteY) {
        return absoluteY - (int) Math.round(scroll);
    }

    private void save() {
        if (!dirty()) return;
        draft.commit(EggBadgesConfig.INSTANCE);
        EggBadgesConfig.INSTANCE.save();
        changed("save");
        close();
    }

    private boolean dirty() { return draft.differsFrom(EggBadgesConfig.INSTANCE); }

    private void refresh(String what) {
        changed(what);
        init();
    }

    private void changed(String what) {
        EggBadgesConfig.debug("settings " + what + " dirty=" + dirty());
    }

    private void renderContent(DrawContext context) {
        if (layout == null) return;
        drawPanel(context, layout.panel());
        renderMain(context, layout);
        String version = EggBadgesVersionLabel.text();
        int versionX = layout.panel().innerRight() - textRenderer.getWidth(version);
        context.drawTextWithShadow(textRenderer, version, versionX, layout.versionLabelY(), VERSION_LABEL);
    }

    private void drawPanel(DrawContext context, EggBadgeSettingsLayout.Panel p) {
        context.fill(p.x(), p.y(), p.x() + p.width(), p.y() + p.height(), 0xD0202834);
        context.fill(p.x(), p.y(), p.x() + p.width(), p.y() + 1, 0xFF465A74);
        context.fill(p.x(), p.y() + p.height() - 1, p.x() + p.width(), p.y() + p.height(), 0xFF465A74);
        context.fill(p.x(), p.y(), p.x() + 1, p.y() + p.height(), 0xFF465A74);
        context.fill(p.x() + p.width() - 1, p.y(), p.x() + p.width(), p.y() + p.height(), 0xFF465A74);
    }

    private void renderMain(DrawContext context, EggBadgeSettingsLayout.Main layout) {
        EggBadgeSettingsLayout.Panel p = layout.panel();
        context.drawCenteredTextWithShadow(textRenderer, "EGG BADGES", p.x() + p.width() / 2, layout.titleY(), 0xFFFFFFFF);
        context.fill(layout.contentX(), layout.ruleY(), layout.contentX() + p.innerWidth(),
                layout.ruleY() + 1, 0x88465A74);

        drawText(context, "PERFECT EGGS (6×31)", layout.perfectHeadingY(), HEADING);
        // The Background checkbox and its swatches supply their own labels.
        drawText(context, "ICON", layout.iconHeadingY(), HEADING);
        context.fill(layout.contentX(), layout.iconRuleY(), layout.contentX() + layout.leftWidth(),
                layout.iconRuleY() + 1, 0x88465A74);
        drawText(context, "Colored Circle", layout.circleModeY(), MUTED);
        drawText(context, "Color", layout.circleColorY(), MUTED);

        int orY = rowY(layout.orY());
        if (lineVisible(orY)) {
            int center = layout.contentX() + layout.leftWidth() / 2;
            context.drawCenteredTextWithShadow(textRenderer, "OR", center, orY, MUTED);
            context.fill(layout.contentX(), orY + 4, center - 14, orY + 5, 0x55465A74);
            context.fill(center + 14, orY + 4, layout.contentX() + layout.leftWidth(), orY + 5, 0x55465A74);
        }
        renderPreviewBox(context, layout);
    }

    private void drawText(DrawContext context, String text, int absoluteY, int color) {
        int y = rowY(absoluteY);
        if (lineVisible(y)) context.drawTextWithShadow(textRenderer, text, layout.contentX(), y, color);
    }

    private boolean lineVisible(int y) {
        return y >= layout.contentY() - 2 && y + 9 <= layout.contentY() + layout.contentHeight();
    }

    private void renderPreviewBox(DrawContext context, EggBadgeSettingsLayout.Main layout) {
        int bx = layout.previewBoxX(), by = layout.previewBoxY() - (int) Math.round(scroll), size = layout.previewBoxSize();
        if (by < layout.contentY() || by + size > layout.contentY() + layout.contentHeight()) return;
        context.fill(bx, by, bx + size, by + size, 0xA0182028);
        context.fill(bx, by, bx + size, by + 1, 0xFF465A74);
        context.fill(bx, by + size - 1, bx + size, by + size, 0xFF465A74);
        context.fill(bx, by, bx + 1, by + size, 0xFF465A74);
        context.fill(bx + size - 1, by, bx + size, by + size, 0xFF465A74);

        SettingsSelection.Preview preview = SettingsSelection.preview(draft);
        BadgeState state = preview.indicators();
        int eggX = layout.previewEggX(), eggY = layout.previewEggY() - (int) Math.round(scroll), scale = layout.previewScale();
        var matrices = context.getMatrices();
        matrices.push();
        matrices.translate(eggX, eggY, 0.0f);
        matrices.scale(scale, scale, 1.0f);
        // Draw a real slot-sized neutral base before the optional configured fill. The
        // configured fill is preview state, intentionally independent of sample IVs.
        context.fill(0, 0, 16, 16, PREVIEW_NEUTRAL_SLOT);
        if (preview.configuredBackgroundArgb() != null) {
            context.fill(0, 0, 16, 16, EggBadgeRenderer.BACKGROUND_UNDERLAY);
            context.fill(0, 0, 16, 16, preview.configuredBackgroundArgb());
        }
        if (previewTexture != null) context.drawTexture(previewTexture, 0, 0, 0, 0, 16, 16, 16, 16);
        else {
            context.fill(2, 2, 14, 14, 0xFFE8D6A8);
            context.fill(5, 5, 11, 11, 0xFFB588D0);
        }
        EggBadgeRenderer.drawOverlays(context, 0, 0, state);
        matrices.pop();

    }

    private int compactWidth(String label) {
        return Math.max(34, textRenderer.getWidth(label) + 12);
    }

    private ButtonWidget button(int x, int y, int w, int h, String text, ButtonWidget.PressAction action) {
        return ButtonWidget.builder(Text.literal(text), action).dimensions(x, y, w, h).build();
    }

    private void resolveTexture() {
        previewTexture = null;
        var resources = MinecraftClient.getInstance().getResourceManager();
        for (Identifier candidate : textures) if (resources.getResource(candidate).isPresent()) {
            previewTexture = candidate;
            break;
        }
        EggBadgesConfig.debug("preview texture=" + (previewTexture == null ? "placeholder" : previewTexture));
    }

    private static final class ColorSwatchWidget extends ButtonWidget {
        private final int color;
        private final boolean selected;

        private ColorSwatchWidget(int x, int y, int size, int color, boolean selected, String name,
                                  PressAction action) {
            super(x, y, size, size, Text.literal(name), action, DEFAULT_NARRATION_SUPPLIER);
            this.color = color;
            this.selected = selected;
        }

        @Override protected void renderWidget(DrawContext context, int mouseX, int mouseY, float delta) {
            context.fill(getX(), getY(), getRight(), getBottom(), color);
            int border = swatchBorderColor(selected);
            if (border != NO_SWATCH_BORDER) context.drawBorder(getX(), getY(), getWidth(), getHeight(), border);
            if (!active) {
                context.fill(getX(), getY(), getRight(), getBottom(), 0x99000000);
            }
        }
    }

    static int swatchBorderColor(boolean selected) {
        return selected ? SELECTED_SWATCH_BORDER : NO_SWATCH_BORDER;
    }

    static void setStatEnabled(StatCondition condition, boolean enabled) {
        if (!enabled) {
            condition.operator = StatCondition.Operator.ANY;
            condition.value = -1;
            return;
        }
        if (condition.operator == null || condition.operator == StatCondition.Operator.ANY) {
            condition.operator = StatCondition.Operator.GT;
            if (condition.value < 0 || condition.value > 31) condition.value = 31;
        }
    }

    static void toggleOperator(StatCondition condition) {
        if (condition.operator == StatCondition.Operator.LT) condition.operator = StatCondition.Operator.GT;
        else condition.operator = StatCondition.Operator.LT;
    }

    static String operatorLabel(StatCondition condition) {
        if (condition == null || condition.operator == null || condition.operator == StatCondition.Operator.ANY)
            return "?";
        return switch (condition.operator) {
            case LT -> "<";
            case EQ -> "=";
            case GT -> ">";
            default -> "?";
        };
    }

    static boolean validSixthIv(String text) {
        if (text == null || text.isEmpty()) return true;
        if (text.length() > 2 || !text.chars().allMatch(Character::isDigit)) return false;
        try {
            int value = Integer.parseInt(text);
            return value >= 0 && value <= 31;
        } catch (NumberFormatException invalid) {
            return false;
        }
    }

    static int sixthIvValue(String text) {
        if (text == null || text.isBlank()) return -1;
        try {
            int value = Integer.parseInt(text);
            return value >= 0 && value <= 31 ? value : -2;
        } catch (NumberFormatException invalid) {
            return -2;
        }
    }

    static boolean validFifthAbove(String text) {
        if (text == null || text.isEmpty()) return true;
        if (text.length() > 2 || !text.chars().allMatch(Character::isDigit)) return false;
        try {
            int value = Integer.parseInt(text);
            return value >= 0 && value <= 30;
        } catch (NumberFormatException invalid) {
            return false;
        }
    }

    static int fifthAboveValue(String text) {
        if (text == null || text.isBlank()) return -1;
        try {
            int value = Integer.parseInt(text);
            return value >= 0 && value <= 30 ? value : -2;
        } catch (NumberFormatException invalid) {
            return -2;
        }
    }

    @Override public boolean mouseScrolled(double mouseX, double mouseY, double horizontal, double vertical) {
        if (layout != null && layout.scrollMax() > 0) {
            double next = clamp(scroll - vertical * 12.0, 0, layout.scrollMax());
            if (next != scroll) {
                scroll = next;
                init();
                return true;
            }
        }
        return super.mouseScrolled(mouseX, mouseY, horizontal, vertical);
    }

    private static double clamp(double value, double min, double max) {
        return Math.max(min, Math.min(max, value));
    }

    @Override public void close() {
        MinecraftClient.getInstance().setScreen(parent);
    }

    @Override public boolean shouldPause() { return false; }
}
