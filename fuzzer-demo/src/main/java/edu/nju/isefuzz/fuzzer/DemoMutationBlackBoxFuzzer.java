package edu.nju.isefuzz.fuzzer;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.TimeUnit;

import static edu.nju.isefuzz.fuzzer.FuzzUtils.*;

public class DemoMutationBlackBoxFuzzer {

    public static void main(String[] args) throws Exception {
        if (args.length != 5 || (!Objects.equals(args[2], "file") && !Objects.equals(args[2], "string"))) {
            System.out.println("DemoMutationBlackBoxFuzzer: <target_path> <out_dir> <file | string> <init_seed> <fuzz_second>");
            System.exit(0);
        }
        int seconds = 0;
        try{
            seconds = Integer.parseInt(args[4]);
            if(seconds <= 0){
                System.out.println("DemoMutationBlackBoxFuzzer: <target_path> <out_dir> <file | string> <init_seed> <fuzz_second>");
                System.out.println("fuzz_time should be positive.");
                System.exit(0);
            }
        }catch (NumberFormatException e){
            System.out.println("DemoMutationBlackBoxFuzzer: <target_path> <out_dir> <file | string> <init_seed> <fuzz_second>");
            System.out.println("fuzz_time should be a number.");
            System.exit(0);
        }
        String cp = args[0];
        File outDir = new File(args[1]);
        boolean input_by_file = Objects.equals(args[2], "file");
        Seed initSeed = new Seed(args[3],input_by_file);

        // Initialize components
        ExecutionComponent execComponent = new ExecutionComponent();
        ExecutionMonitorComponent monitorComponent = new ExecutionMonitorComponent();
        MutationComponent mutationComponent = input_by_file ?
                StreamMutationComponent.getInstance() : StringMutationComponent.getInstance();
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
        sharedMemoryManager.clearBitmap();
        FuzzUtils.clearDirectory("./mutated_inputs");
        FuzzUtils.createDirectory("./mutated_inputs");
        System.out.printf("Test will last for %s seconds.\n",seconds);
        TimeUnit.SECONDS.sleep(3);

        long startTime = System.currentTimeMillis();
        long endTime = startTime + seconds * 1000L;

        while (System.currentTimeMillis() < endTime) {
            Seed nextSeed = schedulingComponent.pickSeed(seedQueue, ++fuzzRnd, observedRes);
            int lastSeedCoverage = nextSeed.getCoverageRate();
            int energy = energySchedulingComponent.getMutationPower(nextSeed,lastSeedCoverage);

            Set<String> testInputs = mutationComponent.fuzzOne(nextSeed, new HashSet<Seed>(seedQueue),energy);

            for (String ti : testInputs) {
                try {
                    Seed newseed = new Seed(ti, input_by_file);
                    sharedMemoryManager.clearBitmap();
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
                    System.out.printf("Error: %s\n",e.getMessage());
                }
            }
            //if(nextSeed.isInputByFile()){
            //    boolean f = FuzzUtils.deleteFile(nextSeed.getContent());
            //    if(!f) System.out.printf("Failed to delete %s\n",nextSeed.getContent());
            //}
            //seedQueue.remove(nextSeed);

            // Seed retirement logic
            if (seedQueue.size() > 500) {
                shrinkQueueByCov(seedQueue);
            }

            if(seedQueue.isEmpty()) {
                System.out.println("SeedQueue is empty. Reset to original seed.");
                seedQueue = prepare(initSeed);
            }

            // Evaluation logic
            evaluationComponent.evaluate();

            //if (findCrash) break;
        }

        // Postprocess the seeds
        postprocess(outDir, seedQueue);
        sharedMemoryManager.destroySharedMemory();
    }
}
