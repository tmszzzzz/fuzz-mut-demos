package edu.nju.isefuzz.fuzzer;

import org.apache.commons.io.FileUtils;

import java.io.*;
import java.nio.file.Files;
import java.util.*;

public class FuzzUtils {

    /**
     * An exemplified seed.
     */


    /**
     * The preparation stage for fuzzing. At this stage, we tend to
     * collect seeds to build and corpus and minimize the corpus to
     * produce a selective seed queue for fuzzing
     */
    public static List<Seed> prepare(Seed initSeed) {
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

            if (s.isInputByFile()) {
                // Handle case where content is a file path
                File sourceFile = new File(s.getContent()); // Get the source file
                if (!sourceFile.exists()) {
                    System.err.println("[ERROR] Source file does not exist: " + sourceFile.getAbsolutePath());
                    continue;
                }

                if (s.isCrash()) {
                    seedFile = new File(crashDir, sourceFile.getName()); // Copy to crashDir
                } else {
                    seedFile = new File(queueDir, sourceFile.getName()); // Copy to queueDir
                }

                try {
                    Files.copy(sourceFile.toPath(), seedFile.toPath());
                    System.out.println("[FUZZER] Copied file to: " + seedFile.getAbsolutePath());
                } catch (IOException e) {
                    System.err.println("[ERROR] Failed to copy file: " + e.getMessage());
                }

            } else {
                // Handle case where content is written directly to a new file
                if (s.isCrash())
                    seedFile = new File(crashDir, s.getContent());
                else
                    seedFile = new File(queueDir, s.getContent());

                try (FileWriter fw = new FileWriter(seedFile)) {
                    fw.write(s.getContent());
                    System.out.println("[FUZZER] Write test input to: " + seedFile.getAbsolutePath());
                } catch (IOException e) {
                    System.err.println("[ERROR] Failed to write content: " + e.getMessage());
                }
            }
        }
    }

    public static void shrinkQueue(List<Seed> seedQueue) {
        List<Seed> needToBeDeleted = new ArrayList<>();
        for (Seed seed : seedQueue) {
            if (!seed.isFavored() && !seed.isCrash()) {
                needToBeDeleted.add(seed);
            }
        }
        for (Seed seed : needToBeDeleted) {
            if (seed.isInputByFile()) {
                boolean f = FuzzUtils.deleteFile(seed.getContent());
                if (!f) System.out.printf("Failed to delete %s\n", seed.getContent());
            }
            seedQueue.remove(seed);
        }
    }

    public static void clearDirectory(String path) {
        File directory = new File(path);

        // Check if the path is a directory
        if (!directory.exists() || !directory.isDirectory()) {
            return;
        }

        // List all files and directories in the given directory
        File[] files = directory.listFiles();
        if (files != null) { // Null check to avoid NPE
            for (File file : files) {
                if (file.isFile()) {
                    // Delete file
                    file.delete();
                }
            }
        }
    }

    public static boolean deleteFile(String filePath) {
        File file = new File(filePath);

        // Check if the file exists
        if (!file.exists()) {
            return false;
        }

        // Attempt to delete the file
        if (file.delete()) {
            return true;
        } else {
            return false;
        }
    }

    public static boolean createDirectory(String dirPath) {
        File directory = new File(dirPath);

        // Check if the directory already exists
        if (directory.exists()) {
            if (directory.isDirectory()) {
                System.out.println("[INFO] Directory already exists: " + dirPath);
                return true; // Directory exists
            } else {
                System.err.println("[ERROR] A file with the same name exists: " + dirPath);
                return false; // A file with the same name exists
            }
        }

        // Attempt to create the directory
        if (directory.mkdirs()) {
            System.out.println("[INFO] Successfully created directory: " + dirPath);
            return true;
        } else {
            System.err.println("[ERROR] Failed to create directory: " + dirPath);
            return false;
        }
    }
}
