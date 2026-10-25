package com.cobbletown.eggbadges;

public record EggBadgeLayout(int width,int height,int contentLeft,int contentRight,int selectorWidth,int editorX,int headerY,int attributeY,int countY,int ivY,int presetY,int secondaryY,int bottomY,int rowGap,int ivRowGap){
    public static EggBadgeLayout calculate(int width,int height){
        int content=Math.min(Math.max(width-32,420),1100);
        int left=Math.max(16,(width-content)/2);
        int selector=width>=900?240:width>=650?200:Math.max(140,content/3);
        int editor=left+selector+16;
        boolean compact=height<360;
        int top=compact?20:height<600?34:48;
        int rowGap=compact?22:height<600?20:28;
        int ivGap=compact?21:height<600?21:28;
        int attribute=top+26;
        int count=attribute+2*rowGap+4;
        int iv=count+26;
        int preset=iv+3*ivGap+(compact?5:10);
        int secondary=preset+24;
        return new EggBadgeLayout(width,height,left,left+content,selector,editor,top,attribute,count,iv,preset,secondary,height-30,rowGap,ivGap);
    }
    public int selectorX(){return contentLeft;}
    public int selectorTop(){return headerY;}
    public int selectorRowHeight(){return height<360?20:height<600?24:28;}
    public int automaticY(){return selectorTop()+5*selectorRowHeight()+6;}
    public int editorWidth(){return contentRight-editorX;}
    public int titleY(){return 12;}
    public int subtitleY(){return headerY-15;}
    public int attributeLabelY(){return attributeY-13;}
    public int ivLabelY(){return ivY-15;}
    public int leftBottomX(){return contentLeft;}
    public int rightBottomX(){return contentRight;}
}
