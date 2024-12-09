package edu.nju.isefuzz.fuzzer;

import java.util.List;
import java.util.Set;

public class EvaluationComponent {

    private final List<Seed> seeds;
    private final Set<ExecutionResult> observedResults;

    public EvaluationComponent(List<Seed> seeds, Set<ExecutionResult> observedResults) {
        this.seeds = seeds;
        this.observedResults = observedResults;
    }

    // Evaluate the fuzzing progress
    public void evaluate() {
        int favoredSeeds = 0;
        int crashSeeds = 0;
        int newResults = 0;

        // Count the number of favored seeds, crash seeds, and new results
        for (Seed seed : seeds) {
            if (seed.isFavored()) favoredSeeds++;
            if (seed.isCrash()) crashSeeds++;
        }

        newResults = observedResults.size();

        System.out.printf("[EVALUATION] Favored Seeds: %d\n", favoredSeeds);
        System.out.printf("[EVALUATION] Crashed Seeds: %d\n", crashSeeds);
        System.out.printf("[EVALUATION] Unique Results: %d\n", newResults);
    }
}
