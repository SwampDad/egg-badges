package com.cobbletown.eggbadges;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class AutoIndicatorsTest {
    private static EggAttributes attrs(String gender,int... ivs){return new EggAttributes("Eevee","","",gender,ivs);}
    @Test void genderUsesExactlyOneFixedIndicator(){
        AutoIndicatorConfig config=new AutoIndicatorConfig();config.show_gender=true;config.show_perfect_iv_count=false;
        assertTrue((AutoIndicators.mask(attrs("Male",0,0,0,0,0,0),config)&AutoIndicators.GENDER_MALE)!=0);
        assertTrue((AutoIndicators.mask(attrs("Female",0,0,0,0,0,0),config)&AutoIndicators.GENDER_FEMALE)!=0);
        assertTrue((AutoIndicators.mask(attrs("Genderless",0,0,0,0,0,0),config)&AutoIndicators.GENDERLESS)!=0);
    }
    @Test void incompleteIvsNeverProduceCount(){
        AutoIndicatorConfig config=new AutoIndicatorConfig();
        assertEquals(0,AutoIndicators.mask(attrs("",31,31,31,31,31,-1),config)&AutoIndicators.COUNT_PRESENT);
    }
    @Test void minimumPerfectCountFiltersAndPacksValue(){
        AutoIndicatorConfig config=new AutoIndicatorConfig();config.show_gender=false;config.show_perfect_iv_count=true;config.minimum_perfect_ivs=5;
        assertEquals(0,AutoIndicators.mask(attrs("",31,31,31,31,0,0),config));
        int mask=AutoIndicators.mask(attrs("",31,31,31,31,31,30),config);
        assertTrue((mask&AutoIndicators.COUNT_PRESENT)!=0);assertEquals(5,AutoIndicators.count(mask));
    }
    @Test void cacheHashIncludesAutomaticSettings(){
        AutoIndicatorConfig config=new AutoIndicatorConfig();int before=EggBadgeRenderer.Cache.configHash(BadgeRule.defaults(),config);
        config.minimum_perfect_ivs=6;
        assertNotEquals(before,EggBadgeRenderer.Cache.configHash(BadgeRule.defaults(),config));
        int afterMinimum=EggBadgeRenderer.Cache.configHash(BadgeRule.defaults(),config);
        config.gender_presentation="BORDER";
        assertNotEquals(afterMinimum,EggBadgeRenderer.Cache.configHash(BadgeRule.defaults(),config));
    }
    @Test void automaticIndicatorsDefaultOffAndDuplicateExactCustomIsSuppressed(){
        AutoIndicatorConfig config=new AutoIndicatorConfig();
        assertEquals(0,AutoIndicators.mask(attrs("Male",31,31,31,31,31,30),config));
        BadgeRule[] rules=BadgeRule.defaults(); int mask=1<<3;
        assertEquals(0,EggBadgeRenderer.suppressDuplicateCount(mask,rules,5));
    }
    @Test void automaticPresentationChoicesAreIndependentAndNormalized(){
        AutoIndicatorConfig config=new AutoIndicatorConfig();config.gender_presentation="background";config.iv_count_presentation="border";config.normalize();
        assertEquals("BACKGROUND",config.gender_presentation);assertEquals("BORDER",config.iv_count_presentation);
        config.iv_count_presentation="unknown";config.normalize();assertEquals("GLYPH",config.iv_count_presentation);
    }
    @Test void automaticDefaultsNeverCreateAnUnrequestedIndicator(){
        AutoIndicatorConfig config=new AutoIndicatorConfig();
        assertEquals(0,AutoIndicators.mask(attrs("Male",31,31,31,31,31,31),config));
    }
    @Test void appearanceNamesDescribeWhatIsDrawn(){
        assertEquals("Symbol",IndicatorPresentation.GLYPH.label);
        assertEquals("Background",IndicatorPresentation.BACKGROUND.label);
        assertEquals("Border",IndicatorPresentation.BORDER.label);
    }
}
