package com.cobbletown.eggbadges;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class EggBadgeDiagnosticsTest {

    @Test void initialStateIsEmpty() {
        var d = new EggBadgeDiagnostics();
        assertEquals(0, d.containerOpenNs());
        assertEquals(0, d.slotCount());
        assertEquals(0, d.eggCount());
        assertEquals(0, d.ruleEvaluations());
        assertFalse(d.firstBadgeRecorded());
        assertEquals(-1, d.framesToFirstRender());
        assertEquals(-1, d.framesToStable());
    }

    @Test void resetClearsAllState() {
        var d = new EggBadgeDiagnostics();
        d.containerOpen(27, 3);
        d.slotClassified(true, 1000, 500);
        d.scanCompleted();
        d.firstBadgeRendered();
        d.recordRuleEvaluations(15);
        d.reset();
        assertEquals(0, d.containerOpenNs());
        assertEquals(0, d.slotCount());
        assertEquals(0, d.eggCount());
        assertEquals(0, d.ruleEvaluations());
        assertFalse(d.firstBadgeRecorded());
    }

    @Test void containerOpenRecordsSlotAndRuleCounts() {
        var d = new EggBadgeDiagnostics();
        d.containerOpen(54, 2);
        assertEquals(54, d.slotCount());
        assertEquals(2, d.enabledRuleCount());
        assertTrue(d.containerOpenNs() > 0);
    }

    @Test void ruleEvaluationsAccumulate() {
        var d = new EggBadgeDiagnostics();
        d.recordRuleEvaluations(5);
        d.recordRuleEvaluations(3);
        assertEquals(8, d.ruleEvaluations());
    }

    @Test void eggCountTracksOnlyEggs() {
        var d = new EggBadgeDiagnostics();
        d.slotClassified(true, 1000, 500);
        d.slotClassified(false, 800, 400);
        d.slotClassified(true, 1200, 600);
        assertEquals(2, d.eggCount());
    }

    @Test void timingAccumulatesClassifyAndMatchNs() {
        var d = new EggBadgeDiagnostics();
        d.containerOpen(2, 1);
        d.slotClassified(true, 1000, 500);
        d.slotClassified(true, 2000, 800);
        assertEquals(2000, d.maxSlotClassifyNs());
        assertTrue(d.avgNsPerSlotClassify() > 0);
        d.recordRuleEvaluations(2);
        assertTrue(d.avgNsPerRuleEval() > 0);
    }

    @Test void maxSlotClassifyTracksLargest() {
        var d = new EggBadgeDiagnostics();
        d.slotClassified(true, 1000, 500);
        d.slotClassified(true, 5000, 2000);
        d.slotClassified(true, 3000, 1000);
        assertEquals(5000, d.maxSlotClassifyNs());
    }

    @Test void firstBadgeRenderedRecordsOnce() {
        var d = new EggBadgeDiagnostics();
        d.frameTick();
        d.frameTick();
        d.firstBadgeRendered();
        long firstNs = d.firstBadgeRenderNs();
        assertTrue(firstNs > 0);
        assertEquals(2, d.framesToFirstRender());
        d.frameTick();
        d.firstBadgeRendered();
        assertEquals(firstNs, d.firstBadgeRenderNs());
        assertEquals(2, d.framesToFirstRender());
    }

    @Test void allBadgesStableRecordsFrameCount() {
        var d = new EggBadgeDiagnostics();
        d.frameTick(); d.frameTick(); d.frameTick();
        d.allBadgesStable();
        assertEquals(3, d.framesToStable());
        assertTrue(d.allBadgesStableNs() > 0);
    }

    @Test void elapsedScanNsComputesCorrectly() {
        var d = new EggBadgeDiagnostics();
        assertEquals(0, d.elapsedScanNs());
        d.containerOpen(10, 1);
        d.scanCompleted();
        assertTrue(d.elapsedScanNs() >= 0);
        assertTrue(d.scanCompletedNs() > 0);
    }

    @Test void elapsedFirstRenderNsZeroWhenNeverRendered() {
        var d = new EggBadgeDiagnostics();
        assertEquals(0, d.elapsedFirstRenderNs());
        d.containerOpen(10, 1);
        assertEquals(0, d.elapsedFirstRenderNs());
    }

    @Test void avgNsPerRuleEvalZeroWhenNoEvals() {
        var d = new EggBadgeDiagnostics();
        assertEquals(0, d.avgNsPerRuleEval());
    }

    @Test void avgNsPerSlotClassifyZeroWhenNoSlots() {
        var d = new EggBadgeDiagnostics();
        assertEquals(0, d.avgNsPerSlotClassify());
    }

    @Test void emitSummaryDoesNotThrowWhenEmpty() {
        var d = new EggBadgeDiagnostics();
        assertDoesNotThrow(d::emitSummary);
    }

    @Test void emitSummaryDoesNotThrowWithFullData() {
        var d = new EggBadgeDiagnostics();
        d.containerOpen(27, 3);
        d.scanStarted();
        for (int i = 0; i < 27; i++) d.slotClassified(i < 5, 1000, 500);
        d.scanCompleted();
        d.recordRuleEvaluations(81);
        d.firstBadgeRendered();
        d.allBadgesStable();
        assertDoesNotThrow(d::emitSummary);
    }

    @Test void configDefaultsAreFalse() {
        var config = new EggBadgesConfig();
        assertFalse(config.diagnostics);
        assertFalse(config.diagnosticsVerbose);
    }

    @Test void drainRuleEvalsResetsCounter() {
        EggClassifier.drainRuleEvals();
        BadgeRule r = new BadgeRule();
        r.enabled = true;
        r.preset = "custom";
        var attrs = new EggAttributes("Eevee", "", "Jolly", "Male", new int[]{31,31,31,31,31,31});
        EggClassifier.matches(attrs, r);
        EggClassifier.matches(attrs, r);
        long drained = EggClassifier.drainRuleEvals();
        assertEquals(2, drained);
        assertEquals(0, EggClassifier.drainRuleEvals());
    }
}
