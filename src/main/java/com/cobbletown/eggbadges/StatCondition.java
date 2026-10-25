package com.cobbletown.eggbadges;

/** One row of a custom IV condition. ANY deliberately has no value. */
public final class StatCondition {
    public enum Operator { ANY, EQ, GT, LT }
    public Operator operator = Operator.ANY;
    public int value = -1;
    public StatCondition() {}
    public StatCondition(Operator operator, int value) { this.operator = operator; this.value = value; }
    public StatCondition copy() { return new StatCondition(operator, value); }

    public boolean matches(int actual) {
        if (operator == null || operator == Operator.ANY) return true;
        if (actual < 0 || value < 0 || value > 31) return false;
        return switch (operator) { case EQ -> actual == value; case GT -> actual > value; case LT -> actual < value; default -> true; };
    }
}
