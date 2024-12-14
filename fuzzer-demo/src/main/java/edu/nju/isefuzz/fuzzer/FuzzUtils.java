package edu.nju.isefuzz.fuzzer;

import org.apache.commons.io.FileUtils;

import java.io.*;
import java.util.*;

public class FuzzUtils {

    /**
     * An exemplified seed.
     */
    static Seed initSeed = new Seed("helln", true);


    /**
     * The preparation stage for fuzzing. At this stage, we tend to
     * collect seeds to build and corpus and minimize the corpus to
     * produce a selective seed queue for fuzzing
     */
    public static List<Seed> prepare() {
        return new ArrayList<>(Collections.singletonList(initSeed));
    }


    public static void postprocess(File outDir, List<Seed> seeds) throws IOException {
        // Delete old outDir
        if (outDir.exists()) {
            FileUtils.forceDelete(outDir);
            System.out.println("[FUZZER] Delete old output directory.");
        }
        boolean res = outDir.mkdirs();
        if (res)
            System.out.println("[FUZZER] Create output directory.");
        File queueDir = new File(outDir, "queue");
        File crashDir = new File(outDir, "crash");
        res = queueDir.mkdir();
        if (res)
            System.out.println("[FUZZER] Create queue directory: " + queueDir.getAbsolutePath());
        res = crashDir.mkdir();
        if (res)
            System.out.println("[FUZZER] Create crash directory: " + crashDir.getAbsolutePath());
        // Record seeds.
        for (Seed s : seeds) {
            File seedFile;
            if (s.isCrash())
                seedFile = new File(crashDir, s.getContent());
            else
                seedFile = new File(queueDir, s.getContent());
            FileWriter fw = new FileWriter(seedFile);
            fw.write(s.getContent());
            fw.close();
            System.out.println("[FUZZER] Write test input to: " + seedFile.getAbsolutePath());
        }
    }

    public static void shrinkQueue(List<Seed> seedQueue) {
        seedQueue.removeIf(seed -> !seed.isFavored() && !seed.isCrash());
    }

}
