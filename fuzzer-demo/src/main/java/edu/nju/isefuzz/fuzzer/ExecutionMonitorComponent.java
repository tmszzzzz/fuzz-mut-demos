package edu.nju.isefuzz.fuzzer;

import java.util.HashSet;
import java.util.Set;

public class ExecutionMonitorComponent {

    private final Set<ExecutionResult> observedResults = new HashSet<>();

    // Monitors execution results
    public void monitorExecution(ExecutionResult execRes, Seed seed) {
        // Log the execution result
        System.out.println("[MONITOR] " + execRes.getInfo());

        // Check if a crash was found
        if (execRes.isCrash()) {
            System.out.printf("[MONITOR] Crash detected for seed: `%s`\n", seed);
        }

        // Track new results
        if (!observedResults.contains(execRes)) {
            observedResults.add(execRes);
            System.out.printf("[MONITOR] New result found for seed: `%s`\n", seed);
        }
    }

    public Set<ExecutionResult> getObservedResults() {
        return observedResults;
    }
}
