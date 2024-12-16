package edu.nju.isefuzz.fuzzer;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
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
        FuzzUtils.clearDirectory("./mutated_inputs");
        FuzzUtils.createDirectory("./mutated_inputs");
        if(input_by_file) {
            File sourceFile = new File(initSeed.getContent()); // Get the source file
            if (!sourceFile.exists()) {
                System.err.println("[ERROR] Source file of initial seed does not exist: " + sourceFile.getAbsolutePath());
            }
            File seedFile = new File("./mutated_inputs/", sourceFile.getName());
            try {
                Files.copy(sourceFile.toPath(), seedFile.toPath());
                initSeed = new Seed(String.valueOf(seedFile.toPath()),true);
                System.out.println("[FUZZER] Copied file to: " + seedFile.getAbsolutePath());
            } catch (IOException e) {
                System.err.println("[ERROR] Failed to copy file: " + e.getMessage());
                System.exit(1);
            }
        }
        // Initialize components
        ExecutionComponent execComponent = new ExecutionComponent();
        ExecutionMonitorComponent monitorComponent = new ExecutionMonitorComponent();
        MutationComponent mutationComponent = input_by_file ?
                StreamMutationComponent.getInstance() : StringMutationComponent.getInstance();
        SeedSchedulingComponent schedulingComponent = new SeedSchedulingComponent();
        EnergySchedulingComponent energySchedulingComponent = new EnergySchedulingComponent();
        EvaluationComponent evaluationComponent = new EvaluationComponent(60);
        SharedMemoryManager sharedMemoryManager = new SharedMemoryManager();
        List<Seed> seedQueue = prepare(initSeed);
        Set<ExecutionResult> observedRes = new HashSet<>();

        // Fuzzing loop
        int fuzzRnd = 0;
        boolean findCrash = false;
        sharedMemoryManager.createSharedMemory(65536);
        sharedMemoryManager.clearBitmap();
        System.out.printf("Test will last for %s seconds.\n",seconds);
        TimeUnit.SECONDS.sleep(3);

        long startTime = System.currentTimeMillis();
        long nextInterval = startTime + evaluationComponent.intervalSeconds * 1000L; // 下一个记录的时间点
        long endTime = startTime + seconds * 1000L;
        int elapsedRounds = 0;

        while (System.currentTimeMillis() < endTime) {
            Seed nextSeed = schedulingComponent.pickSeed(seedQueue, ++fuzzRnd, observedRes);
            int energy = energySchedulingComponent.getMutationPower(nextSeed);

            Set<String> testInputs = mutationComponent.fuzzOne(nextSeed, new HashSet<Seed>(seedQueue),energy);

            for (String ti : testInputs) {
                try {
                    Seed newseed = new Seed(ti, input_by_file);
                    sharedMemoryManager.clearBitmap();
                    ExecutionResult execRes = execComponent.execute(cp, ti, sharedMemoryManager.getShmId(), sharedMemoryManager);
                    monitorComponent.monitorExecution(execRes, nextSeed, ti, energy);

                    int cov = sharedMemoryManager.getCoverageRate();
                    newseed.setCoverageRate(cov);
                    int lastSeedCoverage = nextSeed.getCoverageRate();
                    newseed.setPreviousCoverage(lastSeedCoverage);
                    evaluationComponent.updateCoverageData(cov);
                    evaluationComponent.addCoverage(cov);
                    evaluationComponent.addAllSeeds();
                    if(cov > lastSeedCoverage){
                        newseed.markFavored();
                        evaluationComponent.addFavored();
                    }

                    if (execRes.isCrash()) {
                        findCrash = true;
                        newseed.markCrashed();
                        evaluationComponent.addCrashed();
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
            long currentTime = System.currentTimeMillis();
            if (currentTime >= nextInterval) {
                evaluationComponent.recordAndReset(elapsedRounds * evaluationComponent.intervalSeconds);
                elapsedRounds++;
                nextInterval += evaluationComponent.intervalSeconds * 1000L; // 更新下一个时间点
            }
        }

        // Postprocess the seeds
        postprocess(outDir, seedQueue);
        evaluationComponent.saveCoverageWithBoundsToCSV("Coverage.csv");
        sharedMemoryManager.destroySharedMemory();
    }
}
