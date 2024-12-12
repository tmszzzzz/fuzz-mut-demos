package edu.nju.isefuzz.fuzzer;

import java.util.HashSet;
import java.util.Set;

public class ExecutionMonitorComponent {

    private final Set<ExecutionResult> observedResults = new HashSet<>();

    // Monitors execution results
    public void monitorExecution(ExecutionResult execRes, Seed seed, String ti) {
        // Log the execution result
        System.out.printf("\n[MONITOR] Monitoring Execution...\n[MONITOR] Execution Info:\n" + execRes.getInfo() + "\n[MONITOR] Coverage: " + execRes.getCoverageRate() +
                "[MONITOR] Seed: `%s`\n" +
                "[MONITOR] Test Input: `%s`\n", seed,ti);

        // Check if a crash was found
        if (execRes.isCrash()) {
            System.out.printf("[MONITOR] Crash detected in this input!\n");
        }

        // Track new results
        if (!observedResults.contains(execRes)) {
            observedResults.add(execRes);
            System.out.printf("[MONITOR] New result found in this input!\n");
        }
    }

    public Set<ExecutionResult> getObservedResults() {
        return observedResults;
    }
}
