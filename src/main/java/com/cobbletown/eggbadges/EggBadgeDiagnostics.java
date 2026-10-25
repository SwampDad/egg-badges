package com.cobbletown.eggbadges;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Bounded development instrumentation for the egg-badge pipeline.
 *
 * <p>Two modes, controlled by {@link EggBadgesConfig#diagnosticsVerbose}:
 * <ul>
 *   <li><b>Summary</b> (default): one log line per container session with key latency metrics.</li>
 *   <li><b>Verbose</b>: per-slot classification timing in addition to the summary.</li>
 * </ul>
 *
 * <p>Normally invisible. Enable with {@code diagnostics=true} in egg-badges config.
 * Uses {@link System#nanoTime()} for all elapsed measurements.
 */
final class EggBadgeDiagnostics {
    private static final Logger LOG = LoggerFactory.getLogger("egg-badges/Diagnostics");

    enum Mode { OFF, SUMMARY, VERBOSE }

    // Lifecycle timestamps (nanoTime)
    private long containerOpenNs, slotsDiscoveredNs, scanStartedNs, scanCompletedNs;
    private long firstBadgeRenderNs, allBadgesStableNs;

    // Counts
    private int slotCount, eggCount, enabledRuleCount;
    private long ruleEvaluations, totalClassifyNs, totalMatchNs;
    private int framesToFirstRender = -1, framesToStable = -1;
    private int frameCount;
    private boolean firstBadgeRecorded;

    // Per-slot detail (verbose only)
    private long maxSlotClassifyNs;

    void reset() {
        containerOpenNs = slotsDiscoveredNs = scanStartedNs = scanCompletedNs = 0;
        firstBadgeRenderNs = allBadgesStableNs = 0;
        slotCount = eggCount = enabledRuleCount = 0;
        ruleEvaluations = totalClassifyNs = totalMatchNs = 0;
        framesToFirstRender = framesToStable = -1;
        frameCount = 0;
        firstBadgeRecorded = false;
        maxSlotClassifyNs = 0;
    }

    void containerOpen(int slotCount, int enabledRuleCount) {
        reset();
        this.slotCount = slotCount;
        this.enabledRuleCount = enabledRuleCount;
        containerOpenNs = System.nanoTime();
    }

    void slotsDiscovered() { slotsDiscoveredNs = System.nanoTime(); }

    void scanStarted() { scanStartedNs = System.nanoTime(); }

    void slotClassified(boolean isEgg, long classifyNs, long matchNs) {
        totalClassifyNs += classifyNs;
        totalMatchNs += matchNs;
        if (isEgg) eggCount++;
        if (classifyNs > maxSlotClassifyNs) maxSlotClassifyNs = classifyNs;
    }

    void scanCompleted() { scanCompletedNs = System.nanoTime(); }

    void recordRuleEvaluations(long count) { ruleEvaluations += count; }

    void frameTick() { frameCount++; }

    void firstBadgeRendered() {
        if (!firstBadgeRecorded) {
            firstBadgeRenderNs = System.nanoTime();
            framesToFirstRender = frameCount;
            firstBadgeRecorded = true;
        }
    }

    void allBadgesStable() {
        allBadgesStableNs = System.nanoTime();
        framesToStable = frameCount;
    }

    // --- Accessors for tests and external readers ---

    long containerOpenNs() { return containerOpenNs; }
    long scanCompletedNs() { return scanCompletedNs; }
    long firstBadgeRenderNs() { return firstBadgeRenderNs; }
    long allBadgesStableNs() { return allBadgesStableNs; }
    int slotCount() { return slotCount; }
    int eggCount() { return eggCount; }
    int enabledRuleCount() { return enabledRuleCount; }
    long ruleEvaluations() { return ruleEvaluations; }
    int framesToFirstRender() { return framesToFirstRender; }
    int framesToStable() { return framesToStable; }
    long maxSlotClassifyNs() { return maxSlotClassifyNs; }
    boolean firstBadgeRecorded() { return firstBadgeRecorded; }

    /** Elapsed ns from container open to scan complete. 0 if scan never completed. */
    long elapsedScanNs() { return scanCompletedNs != 0 ? scanCompletedNs - containerOpenNs : 0; }

    /** Elapsed ns from container open to first badge render. 0 if never rendered. */
    long elapsedFirstRenderNs() { return firstBadgeRenderNs != 0 ? firstBadgeRenderNs - containerOpenNs : 0; }

    /** Elapsed ns from container open to all badge classifications finalized.
     *  This is when the scan completes and the active buffer is promoted;
     *  the next render frame draws all badges. */
    long elapsedStableNs() { return allBadgesStableNs != 0 ? allBadgesStableNs - containerOpenNs : 0; }

    /** Elapsed ns from scan complete to first badge render. 0 if never rendered. */
    long elapsedScanToRenderNs() {
        return scanCompletedNs != 0 && firstBadgeRenderNs != 0
                ? firstBadgeRenderNs - scanCompletedNs : 0;
    }

    /** Average ns per rule evaluation. 0 if none. */
    long avgNsPerRuleEval() { return ruleEvaluations > 0 ? totalMatchNs / ruleEvaluations : 0; }

    /** Average ns per slot classification. 0 if none. */
    long avgNsPerSlotClassify() { return slotCount > 0 ? totalClassifyNs / slotCount : 0; }

    void emitSummary() {
        if (containerOpenNs == 0) return;
        long now = System.nanoTime();
        long totalElapsed = now - containerOpenNs;
        LOG.info("[egg-badges] container | slots={} eggs={} rules={} | scan={}ms firstRender={}ms stable={}ms total={}ms | frames:first={} stable={} | classify:total={}ms match={}ms avgPerRule={}ns maxSlot={}ms | ruleEvals={}",
                slotCount, eggCount, enabledRuleCount,
                ms(elapsedScanNs()), ms(elapsedFirstRenderNs()), ms(elapsedStableNs()), ms(totalElapsed),
                framesToFirstRender, framesToStable,
                ms(totalClassifyNs), ms(totalMatchNs), avgNsPerRuleEval(), ms(maxSlotClassifyNs),
                ruleEvaluations);
    }

    private static long ms(long ns) { return ns / 1_000_000; }
}
