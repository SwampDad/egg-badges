package com.cobbletown.eggbadges;

import java.util.Arrays;

public final class BadgeRule {
    public String id="",label="",glyph="!",presentation="GLYPH";public int color=0xFFFFFFFF;public boolean enabled;
    public String species="",form="",nature="",gender="Any";
    /** Legacy per-status values. When non-empty, routes to legacy matcher. */
    public String[] values={"","","","","",""};
    /** Per-stat comparison operators. Active in both legacy and new model paths. */
    public String[] operators={">=",">=",">=",">=",">=",">="};
    /** Legacy preset identifier. Cleared when saved from new UI. */
    public String preset="custom";
    public String perfectCountMode="none";public int perfectCount=0;
    public int remainderMin=-1;
    public int[] ivMinimums={-1,-1,-1,-1,-1,-1};
    public boolean isIvClassification=true;

    public BadgeRule(){}

    public static BadgeRule[] defaults(){
        BadgeRule perfect=makeIV("perfect","6×31 IV","\u25CF",0xFF55DD77,true,"exactly",6,-1,new int[]{31,31,31,31,31,31});
        perfect.presentation="BACKGROUND";
        return new BadgeRule[]{
            make("male","Male","M",0xFF5599FF,false,"Male",false),
            make("female","Female","F",0xFFFF5555,false,"Female",false),
            perfect,
            makeIV("five","5×31 IV","V",0xFF55AAFF,true,"exactly",5,-1,new int[]{-1,-1,-1,-1,-1,-1}),
            new BadgeRule()
        };
    }

    private static BadgeRule make(String id,String label,String glyph,int color,boolean enabled,String gender,boolean iv){
        BadgeRule r=new BadgeRule();r.id=id;r.label=label;r.glyph=glyph;r.color=color;r.enabled=enabled;
        r.gender=gender;r.isIvClassification=iv;return r;}
    private static BadgeRule makeIV(String id,String label,String glyph,int color,boolean enabled,
            String countMode,int count,int remMin,int[] mins){
        BadgeRule r=new BadgeRule();r.id=id;r.label=label;r.glyph=glyph;r.color=color;r.enabled=enabled;
        r.perfectCountMode=countMode;r.perfectCount=count;r.remainderMin=remMin;
        r.ivMinimums=Arrays.copyOf(mins,6);r.isIvClassification=true;return r;}

    public BadgeRule copy(){
        BadgeRule o=new BadgeRule();
        o.id=id;o.label=label;o.glyph=glyph;o.presentation=IndicatorPresentation.from(presentation).name();o.color=color;o.enabled=enabled;
        o.species=species;o.form=form;o.nature=nature;o.gender=gender;
        o.preset=preset;
        o.operators=operators==null?new String[]{">=",">=",">=",">=",">=",">="}:Arrays.copyOf(operators,6);
        o.values=values==null?new String[6]:Arrays.copyOf(values,6);
        o.perfectCountMode=perfectCountMode;o.perfectCount=perfectCount;
        o.remainderMin=remainderMin;
        o.ivMinimums=ivMinimums==null?new int[]{-1,-1,-1,-1,-1,-1}:Arrays.copyOf(ivMinimums,6);
        o.isIvClassification=isIvClassification;
        return o;
    }

    static void migrateToSchema3(BadgeRule[] rules){
        if(rules==null)return;
        for(BadgeRule r:rules){if(r==null)continue;
            if(r.perfectCountMode==null||r.perfectCountMode.isEmpty())r.perfectCountMode="none";
            if(r.ivMinimums==null||r.ivMinimums.length<6)r.ivMinimums=new int[]{-1,-1,-1,-1,-1,-1};
            if(r.values==null||r.values.length<6)r.values=new String[]{"","","","","",""};
            if(r.operators==null||r.operators.length<6)r.operators=new String[]{">=",">=",">=",">=",">=",">="};
        }
    }
    static boolean normalizePresentation(BadgeRule[] rules){
        boolean changed=false;if(rules==null)return false;
        for(BadgeRule r:rules)if(r!=null){String normalized=IndicatorPresentation.from(r.presentation).name();if(!normalized.equals(r.presentation)){r.presentation=normalized;changed=true;}}
        return changed;
    }
}
