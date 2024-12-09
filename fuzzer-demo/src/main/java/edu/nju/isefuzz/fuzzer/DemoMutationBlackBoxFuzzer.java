package edu.nju.isefuzz.fuzzer;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static edu.nju.isefuzz.fuzzer.FuzzUtils.*;

public class DemoMutationBlackBoxFuzzer {

    public static void main(String[] args) throws Exception {
        if (args.length != 3) {
            System.out.println("DemoMutationBlackBoxFuzzer: <classpath> <target_name> <out_dir>");
            System.exit(0);
        }
        String cp = args[0];
        String tn = args[1];
        File outDir = new File(args[2]);

        // Initialize components
        ExecutionComponent execComponent = new ExecutionComponent();
        ExecutionMonitorComponent monitorComponent = new ExecutionMonitorComponent();
        MutationComponent mutationComponent = new MutationComponent();
        SeedSchedulingComponent schedulingComponent = new SeedSchedulingComponent();
        EnergySchedulingComponent energySchedulingComponent = new EnergySchedulingComponent();
        EvaluationComponent evaluationComponent = new EvaluationComponent(new ArrayList<>(), new HashSet<>());

        List<Seed> seedQueue = prepare();
        Set<ExecutionResult> observedRes = new HashSet<>();

        // Fuzzing loop
        int fuzzRnd = 0;
        boolean findCrash = false;
        while (true) {
            Seed nextSeed = schedulingComponent.pickSeed(seedQueue, ++fuzzRnd, observedRes);
            Set<String> testInputs = mutationComponent.generate(nextSeed);

            for (String ti : testInputs) {
                ExecutionResult execRes = execComponent.execute(cp, tn, ti);
                monitorComponent.monitorExecution(execRes, nextSeed);

                if (execRes.isCrash()) {
                    findCrash = true;
                    nextSeed.markCrashed();
                }

                if (!observedRes.contains(execRes)) {
                    observedRes.add(execRes);
                    nextSeed.markFavored();
                }
                seedQueue.add(nextSeed);
            }

            // Seed retirement logic
            if (seedQueue.size() > 500 || findCrash) {
                shrinkQueue(seedQueue);
            }

            // Evaluation logic
            evaluationComponent.evaluate();

            if (findCrash) break;
        }

        // Postprocess the seeds
        postprocess(outDir, seedQueue);
    }
}
