package edu.nju.isefuzz.fuzzer;

import java.util.HashSet;
import java.util.Set;

public class ExecutionMonitorComponent {

    private final Set<ExecutionResult> observedResults = new HashSet<>();

    // Monitors execution results
    public void monitorExecution(ExecutionResult execRes, Seed seed, String ti, int seedEnergy) {
        // Log the execution result
        System.out.printf("\n[MONITOR] Monitoring Execution...\nExecution Info:\n"+ "----------\n" + /*execRes.getInfo()*/"Info output disabled.\n" + "----------" + "\nCoverage: " + execRes.getCoverageRate() +
                "\nSeed: `%s`\n" +
                "Seed Energy: `%d`\n" +
                "Test Input: `%s`\n", seed,seedEnergy,ti);

        // Check if a crash was found
        if (execRes.isCrash()) {
            System.out.printf("Crash detected in this input!\n");
        }

        // Track new results
        if (!observedResults.contains(execRes)) {
            observedResults.add(execRes);
            System.out.printf("New result found in this input!\n");
        }
        System.out.println("[MONITOR] END.\n" );
    }

    public Set<ExecutionResult> getObservedResults() {
        return observedResults;
    }
}
