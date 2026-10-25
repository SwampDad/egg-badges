package com.cobbletown.eggbadges;

/** A direct, understandable presentation choice for a matching indicator. */
enum IndicatorPresentation {
    // GLYPH remains the persisted value for compatibility with existing configurations.
    GLYPH("Symbol"), BACKGROUND("Background"), BORDER("Border");

    final String label;
    IndicatorPresentation(String label) { this.label=label; }
    static IndicatorPresentation from(String value) {
        if(value!=null)for(IndicatorPresentation presentation:values())if(presentation.name().equalsIgnoreCase(value))return presentation;
        return GLYPH;
    }
    IndicatorPresentation next(){return values()[(ordinal()+1)%values().length];}
}
