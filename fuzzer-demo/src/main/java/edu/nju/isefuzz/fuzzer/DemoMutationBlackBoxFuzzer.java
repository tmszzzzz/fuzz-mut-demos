package edu.nju.isefuzz.fuzzer;

import java.io.File;
import java.io.IOException;
import java.util.*;

import static edu.nju.isefuzz.fuzzer.FuzzUtils.*;

public class DemoMutationBlackBoxFuzzer {

    public static void main(String[] args) throws Exception {
        if (args.length != 4 || (!Objects.equals(args[2], "file") && !Objects.equals(args[2], "string"))) {
            System.out.println("DemoMutationBlackBoxFuzzer: <target_path> <out_dir> <file | string> <init_seed>");
            System.exit(0);
        }
        String cp = args[0];
        File outDir = new File(args[1]);
        boolean input_by_file = Objects.equals(args[2], "file");
        Seed initSeed = new Seed(args[3]);

        // Initialize components
        ExecutionComponent execComponent = new ExecutionComponent();
        ExecutionMonitorComponent monitorComponent = new ExecutionMonitorComponent();
        MutationComponent mutationComponent = new MutationComponent();
        SeedSchedulingComponent schedulingComponent = new SeedSchedulingComponent();
        EnergySchedulingComponent energySchedulingComponent = new EnergySchedulingComponent();
        EvaluationComponent evaluationComponent = new EvaluationComponent(new ArrayList<>(), new HashSet<>());
        SharedMemoryManager sharedMemoryManager = new SharedMemoryManager();
        List<Seed> seedQueue = prepare(initSeed);
        Set<ExecutionResult> observedRes = new HashSet<>();

        // Fuzzing loop
        int fuzzRnd = 0;
        boolean findCrash = false;
        sharedMemoryManager.createSharedMemory(65536);
        while (true) {
            Seed nextSeed = schedulingComponent.pickSeed(seedQueue, ++fuzzRnd, observedRes);
            int lastSeedCoverage = nextSeed.getCoverageRate();
            int energy = energySchedulingComponent.getMutationPower(nextSeed,lastSeedCoverage);
            Set<String> testInputs = mutationComponent.fuzzOne(nextSeed, new HashSet<Seed>(seedQueue),energy);

            for (String ti : testInputs) {
                try {
                    Seed newseed = new Seed(ti, false);

                    ExecutionResult execRes = execComponent.execute(cp, ti, sharedMemoryManager.getShmId(), sharedMemoryManager);
                    monitorComponent.monitorExecution(execRes, nextSeed, ti, energy);

                    int cov = sharedMemoryManager.getCoverageRate();
                    newseed.setCoverageRate(cov);

                    if(cov > lastSeedCoverage){
                        newseed.markFavored();
                    }

                    if (execRes.isCrash()) {
                        findCrash = true;
                        newseed.markCrashed();
                    }

                    //if (!observedRes.contains(execRes)) {
                    //    observedRes.add(execRes);
                    //    newseed.markFavored();
                    //}
                    seedQueue.add(newseed);
                }catch (IOException e){
                    System.out.println("Invalid input with '\\0'.");
                }
            }
            seedQueue.remove(nextSeed);

            // Seed retirement logic
            if (seedQueue.size() > 500 || findCrash) {
                shrinkQueue(seedQueue);
                if(seedQueue.isEmpty()) {
                    System.out.println("SeedQueue is empty. Reset to original seed.");
                    seedQueue = prepare(initSeed);
                }
            }

            // Evaluation logic
            evaluationComponent.evaluate();

            if (findCrash) break;
        }

        // Postprocess the seeds
        postprocess(outDir, seedQueue);
        sharedMemoryManager.destroySharedMemory();
    }
}
