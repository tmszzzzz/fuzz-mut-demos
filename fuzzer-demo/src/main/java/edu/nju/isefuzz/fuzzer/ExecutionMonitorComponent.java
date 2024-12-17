package edu.nju.isefuzz.fuzzer;

import java.util.HashSet;
import java.util.Set;

public class ExecutionMonitorComponent {

    private final Set<ExecutionResult> observedResults = new HashSet<>();

    // Monitors execution results
    public void monitorExecution(ExecutionResult execRes, Seed seed, String ti, int seedEnergy) {
        // Log the execution result
        System.out.print("\n[MONITOR] Monitoring Execution...\nExecution Info:\n"+ "----------\n");
        try{
            System.out.printf(execRes.getInfo().length()>100?execRes.getInfo().substring(0,100) + "..." : execRes.getInfo());
        }catch (Exception e){
            System.out.print("Because of some unknown error, execution info can't be shown.");
        }
        System.out.printf(
                "\n----------" + "\nCoverage: " + execRes.getCoverageRate() +
                        "\nPrevious Coverage: " + seed.getCoverageRate() +
                "\nSeed: `%s`\n" +
                "Seed Energy: `%d`\n" +
                "Test Input: `%s`\n", seed.toString().length()>100?seed.toString().substring(0,100) + "...":seed,seedEnergy,ti.length()>100?ti.substring(0,100) + "...":ti);

        // Check if a crash was found
        if (execRes.isCrash()) {
            System.out.printf("Crash detected in this input!\n");
        }

        //// Track new results
        //if (!observedResults.contains(execRes)) {
        //    observedResults.add(execRes);
        //    System.out.printf("New result found in this input!\n");
        //}
        System.out.println("[MONITOR] END.\n" );
    }

    public Set<ExecutionResult> getObservedResults() {
        return observedResults;
    }
}
