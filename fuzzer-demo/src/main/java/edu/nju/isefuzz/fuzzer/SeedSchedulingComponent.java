package edu.nju.isefuzz.fuzzer;

import java.util.List;
import java.util.Set;

public class SeedSchedulingComponent {

    // Picks the next seed based on certain criteria (e.g., favoring seeds with more diversity)
    public Seed pickSeed(List<Seed> seeds, int round, Set<ExecutionResult> observedResults) {
        // Example of prioritizing seeds that lead to new results
        Seed nextSeed = seeds.get(round % seeds.size());

        for (Seed seed : seeds) {
            if (!observedResults.contains(seed)) {
                nextSeed = seed; // Prefer seeds that yield new execution results
                break;
            }
        }

        return nextSeed;
    }
}
