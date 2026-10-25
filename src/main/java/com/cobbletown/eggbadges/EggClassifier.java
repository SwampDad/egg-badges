package com.cobbletown.eggbadges;

import net.minecraft.component.DataComponentTypes;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public final class EggClassifier {
    private static final String[] IV_NAMES={"HP","Attack","Defence","Sp.Attack","Sp.Defence","Speed"};
    private static final Identifier COBBLEMON_EGG=Identifier.of("cobblemon","pokemon_egg");
    private EggClassifier(){}
    public static EggAttributes read(ItemStack stack){
        return read(stack, BadgeRule.defaults()).attributes();
    }
    public static ReadResult read(ItemStack stack,BadgeRule[] rules){
        return read(stack,rules,null);
    }
    public static ReadResult read(ItemStack stack,BadgeRule[] rules,AutoIndicatorConfig automatic){
        if(!isEgg(stack))return new ReadResult(null,0,false);
        ParsedLore parsed=parseLore(stack.get(DataComponentTypes.LORE)==null?java.util.List.of():stack.get(DataComponentTypes.LORE).lines(),rules,automatic);
        return new ReadResult(parsed.attributes(),parsed.lines(),true);
    }
    /** Live three-channel path: parse every field needed by the current badges. */
    public static ReadResult readNew(ItemStack stack){
        if(!isEgg(stack))return new ReadResult(null,0,false);
        java.util.List<Text> lore=loreOf(stack);
        return new ReadResult(parseAllLore(lore).attributes(), lore.size(), true);
    }

    static ParsedLore parseAllLore(Iterable<Text> lore){
        return parseLore(lore,null,null,true);
    }

    private static java.util.List<Text> loreOf(ItemStack stack){
        var component=stack.get(DataComponentTypes.LORE);
        return component==null?java.util.List.of():java.util.List.copyOf(component.lines());
    }
    /** Parses only explicitly labelled lore fields. Missing labels remain unknown (empty). */
    static ParsedLore parseLore(Iterable<Text> lore,BadgeRule[] rules,AutoIndicatorConfig automatic){
        return parseLore(lore,rules,automatic,false);
    }
    private static ParsedLore parseLore(Iterable<Text> lore,BadgeRule[] rules,AutoIndicatorConfig automatic,boolean allNewFields){
        Needs needs=allNewFields?new Needs(true,true,true,true,true,true):Needs.from(rules,automatic);
        String species="",form="",nature="",gender="",ability="";int[] ivs={-1,-1,-1,-1,-1,-1};
        int loreLines=0;
        if(lore!=null)for(Text line:lore){
            loreLines++;String s=stripFormatting(line.getString());int c=s.indexOf(':');if(c<=0)continue;
            String k=s.substring(0,c).trim(),v=s.substring(c+1).trim();
            if(needs.species&&"Species".equalsIgnoreCase(k))species=v;
            else if(needs.form&&"Form".equalsIgnoreCase(k))form=v;
            else if(needs.nature&&"Nature".equalsIgnoreCase(k))nature=v;
            else if(needs.gender&&("Gender".equalsIgnoreCase(k)||"Sex".equalsIgnoreCase(k)))gender=normalizeGender(v);
            else if(needs.ability&&"Ability".equalsIgnoreCase(k))ability=v;
            else if(needs.ivs){int i=ivIndex(k);if(i>=0)ivs[i]=parse(v);}
        }
        return new ParsedLore(new EggAttributes(species,form,nature,gender,ivs,ability,HiddenAbilityStatus.UNRESOLVED),loreLines);
    }
    static boolean isEgg(ItemStack stack){
        if(stack==null||stack.isEmpty())return false;
        if(COBBLEMON_EGG.equals(Registries.ITEM.getId(stack.getItem())))return true;
        String name=stripFormatting(stack.getName().getString());
        if("Pok\u00e9mon Egg".equals(name)||"Pokemon Egg".equals(name))return true;
        var lore=stack.get(DataComponentTypes.LORE);
        return lore!=null&&hasExplicitEggLore(lore.lines());
    }
    /** Identifies server-disguised eggs only from labelled data already used by the matcher. */
    static boolean hasExplicitEggLore(Iterable<Text> lore){
        boolean species=false,iv=false;
        if(lore!=null)for(Text line:lore){String s=stripFormatting(line.getString());int c=s.indexOf(':');if(c<=0)continue;
            String key=s.substring(0,c).trim();
            if("Species".equalsIgnoreCase(key))species=true;
            if(ivIndex(key)>=0)iv=true;
        }
        return species&&iv;
    }
    static String normalizeGender(String value){
        if(value==null)return "";
        if("M".equalsIgnoreCase(value)||"MALE".equalsIgnoreCase(value))return "Male";
        if("F".equalsIgnoreCase(value)||"FEMALE".equalsIgnoreCase(value))return "Female";
        if("GENDERLESS".equalsIgnoreCase(value))return "Genderless";
        return value;
    }
    static String stripFormatting(String s){int first=s.indexOf('\u00a7');if(first<0)return s.trim();StringBuilder out=new StringBuilder(s.length());out.append(s,0,first);for(int i=first;i<s.length();i++){if(s.charAt(i)=='\u00a7'&&i+1<s.length()){i++;continue;}out.append(s.charAt(i));}return out.toString().trim();}
    private static int ivIndex(String k){for(int i=0;i<IV_NAMES.length;i++)if(IV_NAMES[i].equalsIgnoreCase(k))return i;return -1;}
    private static int parse(String s){int value=0;boolean digit=false;for(int i=0;i<s.length();i++){char c=s.charAt(i);if(c>='0'&&c<='9'){value=value*10+(c-'0');digit=true;if(value>31)return -1;}else if(digit)break;}return digit?value:-1;}
    static int countPerfectAtLeast(int[] ivs,int threshold){if(ivs==null)return 0;int n=0;for(int iv:ivs)if(iv>=threshold)n++;return n;}
    static boolean complete(int[] ivs){if(ivs==null||ivs.length!=6)return false;for(int iv:ivs)if(iv<0||iv>31)return false;return true;}
    /** -1 means the lore did not provide a complete six-IV set; 0..6 otherwise. */
    static int perfectCount(int[] ivs){return complete(ivs)?countPerfectAtLeast(ivs,31):-1;}
    static boolean matches(ChannelConfig c,EggAttributes a){
        if(c==null||!c.configured||a==null||c.condition==null)return false;
        return switch(c.condition){
            case PERFECT_6 -> complete(a.ivs())&&countPerfectAtLeast(a.ivs(),31)==6;
            case FIVE_5 -> {if(!complete(a.ivs())||countPerfectAtLeast(a.ivs(),31)!=5)yield false;int remainder=0;for(int iv:a.ivs())if(iv<31)remainder=iv;yield !c.five_iv_restricted||c.five_iv_minimum<0||remainder>=c.five_iv_minimum;}
            case GENDER -> "Male".equals(a.gender())||"Female".equals(a.gender());
            case IV_COUNT -> complete(a.ivs());
            case CUSTOM -> matchesCustom(c,a.ivs());
        };
    }
    static boolean matchesCustom(ChannelConfig c,int[] ivs){if(c==null)return false;StatCondition[] rows=c.stats;if(rows==null)return true;for(int i=0;i<Math.min(6,rows.length);i++)if(rows[i]!=null&&!rows[i].matches(ivs!=null&&i<ivs.length?ivs[i]:-1))return false;return true;}
    private static long ruleEvalCounter;
    static long drainRuleEvals(){long v=ruleEvalCounter;ruleEvalCounter=0;return v;}

    public static boolean matches(EggAttributes a,BadgeRule r){
        ruleEvalCounter++;
        if(a==null||r==null||!r.enabled)return false;
        if(!eq(a.species(),r.species)||!eq(a.form(),r.form)||!eq(a.nature(),r.nature))return false;
        if(r.gender!=null&&!r.gender.isBlank()&&!"Any".equalsIgnoreCase(r.gender)&&!r.gender.equalsIgnoreCase(a.gender()))return false;
        if(hasLegacyIvOps(r))return matchesLegacy(a,r);
        return matchesNew(a,r);
    }

    private static boolean matchesNew(EggAttributes a,BadgeRule r){
        if(!"none".equals(r.perfectCountMode)&&r.perfectCount>0){
            int n=countPerfectAtLeast(a.ivs(),31);
            if("exactly".equals(r.perfectCountMode)){if(n!=r.perfectCount)return false;}
            else{if(n<r.perfectCount)return false;}
        }
        if(r.remainderMin>=0){
            if(!"exactly".equals(r.perfectCountMode)||r.perfectCount<=0)return false;
            int nonPerfect=0;for(int iv:a.ivs())if(iv<31)nonPerfect++;
            if(nonPerfect!=1)return false;
            for(int iv:a.ivs())if(iv<31){if(iv<r.remainderMin)return false;break;}
        }
        if(r.ivMinimums!=null)for(int i=0;i<Math.min(6,r.ivMinimums.length);i++){
            if(r.ivMinimums[i]<0)continue;
            int actual=a.ivs()[i];if(actual<0)return false;
            String op=r.operators!=null&&i<r.operators.length?r.operators[i]:">=";
            if(!compare(actual,r.ivMinimums[i],op))return false;}
        return true;
    }

    private static boolean matchesLegacy(EggAttributes a,BadgeRule r){
        if("male".equals(r.preset)&&!"male".equalsIgnoreCase(a.gender()))return false;
        if("female".equals(r.preset)&&!"female".equalsIgnoreCase(a.gender()))return false;
        if("perfect".equals(r.preset))for(int iv:a.ivs())if(iv!=31)return false;
        if("five".equals(r.preset)){int n=0;for(int iv:a.ivs())if(iv==31)n++;if(n!=5)return false;}
        if(r.values!=null)for(int i=0;i<Math.min(6,r.values.length);i++){
            if(r.values[i]==null||r.values[i].isBlank())continue;
            int wanted;try{wanted=Integer.parseInt(r.values[i]);}catch(Exception e){return false;}
            int actual=a.ivs()[i];if(actual<0)return false;
            String op=r.operators!=null&&i<r.operators.length?r.operators[i]:"=";
            if(!compare(actual,wanted,op))return false;}
        return true;
    }

    static boolean hasLegacyIvOps(BadgeRule r){
        if(r.values!=null)for(String v:r.values)if(v!=null&&!v.isBlank())return true;
        if("perfect".equals(r.preset)||"five".equals(r.preset)
                ||"male".equals(r.preset)||"female".equals(r.preset))return true;
        return false;
    }

    private static boolean compare(int a,int b,String op){return switch(op){case ">"->a>b;case "<"->a<b;case ">="->a>=b;case "<="->a<=b;default->a==b;};}
    private static boolean eq(String a,String b){
        if(b==null||b.isBlank())return true;
        return a!=null&&!a.isBlank()&&a.trim().equalsIgnoreCase(b.trim());
    }
    public record ReadResult(EggAttributes attributes,int loreLines,boolean egg){}
    record ParsedLore(EggAttributes attributes,int lines){}
    private record Needs(boolean species,boolean form,boolean nature,boolean gender,boolean ivs,boolean ability){
        static Needs from(BadgeRule[] rules,AutoIndicatorConfig automatic){
            boolean species=false,form=false,nature=false,gender=false,ivs=false;
            if(rules!=null)for(BadgeRule r:rules)if(r!=null&&r.enabled){
                species|=r.species!=null&&!r.species.isBlank();
                form|=r.form!=null&&!r.form.isBlank();
                nature|=r.nature!=null&&!r.nature.isBlank();
                gender|=(r.gender!=null&&!r.gender.isBlank()&&!"Any".equalsIgnoreCase(r.gender));
                ivs|=hasLegacyIvOps(r)
                    ||!"none".equals(r.perfectCountMode)
                    ||r.remainderMin>=0
                    ||(r.ivMinimums!=null&&java.util.Arrays.stream(r.ivMinimums).anyMatch(v->v>=0));
            }
            if(automatic!=null){gender|=automatic.show_gender;ivs|=automatic.show_perfect_iv_count;}
            return new Needs(species,form,nature,gender,ivs,false);
        }
    }
}
