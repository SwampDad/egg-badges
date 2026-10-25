package com.cobbletown.eggbadges;

public enum IconShape {
    CIRCLE("●"), TRIANGLE("▲"), DIAMOND("◆"), STAR("★");
    public final String glyph;
    IconShape(String glyph) { this.glyph = glyph; }
    public IconShape next() { return values()[(ordinal() + 1) % values().length]; }
}
