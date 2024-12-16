package edu.nju.isefuzz.fuzzer;

import java.util.List;
import java.util.Set;

public class EvaluationComponent {
    private int allSeeds = 0;
    private int favoredSeeds = 0;
    private int crashSeeds = 0;
    private int max_Coverage_rate = 0;

    public void addAllSeeds() {allSeeds++;}
    public void addFavored() {favoredSeeds++;}
    public void addCrashed() {crashSeeds++;}
    public void addCoverage(int cov) {
        max_Coverage_rate = Math.max(max_Coverage_rate, cov);
    }

    // Evaluate the fuzzing progress
    public void evaluate() {
        System.out.printf("[EVALUATION] Seeds Num: %d\n", allSeeds);
        System.out.printf("[EVALUATION] Favored Seeds: %d\n", favoredSeeds);
        System.out.printf("[EVALUATION] Crashed Seeds: %d\n", crashSeeds);
        System.out.printf("[EVALUATION] Max Code Coverage: %d\n", max_Coverage_rate);
    }


}
