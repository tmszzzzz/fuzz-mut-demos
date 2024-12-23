package edu.nju.isefuzz.fuzzer;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;
import java.util.UUID;

public class StreamMutationComponent extends MutationComponent {
    /**
     * Singleton Pattern
     */
    private static StreamMutationComponent instance;
    private StreamMutationComponent() {}
    public static StreamMutationComponent getInstance() {
        if(instance == null) {
            instance = new StreamMutationComponent();
        }
        return instance;
    }


    /**
     * HEADER_SIZE: the first part of file stream to be protected from mutation
     */
    private static final int HEADER_SIZE = 0;


    private static byte[] readFile(String filePath) throws IOException {
        return Files.readAllBytes(Paths.get(filePath));
    }
    private static void writeFile(String filePath, byte[] data) throws IOException {
        Files.write(Paths.get(filePath), data);
    }

    /**
     * Bitflip mutation: flips some specific bits in the content.
     *
     * @param data the content to mutate
     * @param numFlips the times to mutate
     * @return the mutated content
     */
    private static byte[] bitflip(byte[] data, int numFlips) {
        Random rand = new Random();
        byte[] mutatedData = data.clone();

        for (int i = 0; i < numFlips; i++) {
            int pos = rand.nextInt(data.length - HEADER_SIZE) + HEADER_SIZE;
            int bitPos = rand.nextInt(8);
            mutatedData[pos] ^= (byte)(1 << bitPos);
        }
        return mutatedData;
    }


    /**
     * ByteReplacement mutation: replaces some specific bytes in the content.
     *
     * @param data the content to replace
     * @param numReplacements the times to replace
     * @return the mutated content
     */
    private static byte[] byteReplacement(byte[] data, int numReplacements) {
        Random rand = new Random();
        byte[] mutatedData = data.clone();

        for (int i = 0; i < numReplacements; i++) {
            int pos = rand.nextInt(data.length - HEADER_SIZE) + HEADER_SIZE;
            mutatedData[pos] = (byte) rand.nextInt(256);
        }
        return mutatedData;
    }


    /**
     * ByteInsertion mutation: replaces some specific bytes in the content.
     *
     * @param data the content to insert
     * @param numInsertions the times to insert
     * @return the mutated content
     */
    private static byte[] byteInsertion(byte[] data, int numInsertions) {
        Random rand = new Random();
        byte[] mutatedData = data.clone();

        for (int i = 0; i < numInsertions; i++) {
            int pos = rand.nextInt(mutatedData.length - HEADER_SIZE) + HEADER_SIZE;
            byte[] newData = new byte[mutatedData.length + 1];
            System.arraycopy(mutatedData, 0, newData, 0, pos);
            newData[pos] = (byte) rand.nextInt(256);
            System.arraycopy(mutatedData, pos, newData, pos + 1, mutatedData.length - pos);
            mutatedData = newData;
        }
        return mutatedData;
    }


    /**
     * ByteDeletion mutation: deletes some specific bytes in the content.
     *
     * @param data the content to delete
     * @param numDeletions the times to delete
     * @return the mutated content
     */
    private static byte[] byteDeletion(byte[] data, int numDeletions) {
        Random rand = new Random();
        byte[] mutatedData = data.clone();

        for (int i = 0; i < numDeletions; i++) {
            if (mutatedData.length <= HEADER_SIZE) break;
            int pos = rand.nextInt(mutatedData.length - HEADER_SIZE) + HEADER_SIZE;
            byte[] newData = new byte[mutatedData.length - 1];

            System.arraycopy(mutatedData, 0, newData, 0, pos);
            System.arraycopy(mutatedData, pos + 1, newData, pos, mutatedData.length - pos - 1);
            mutatedData = newData;
        }
        return mutatedData;
    }


    /**
     * BlockSwap mutation: randomly swaps two blocks in the content.
     *
     * @param data the content to apply mutation
     * @param blockSize the size of blocks to be swapped
     * @return the mutated content
     */
    private static byte[] blockSwap(byte[] data, int blockSize) {
        Random rand = new Random();
        byte[] mutatedData = data.clone();

        if (blockSize >= data.length - HEADER_SIZE) return mutatedData;
        int pos1 = rand.nextInt(data.length - HEADER_SIZE - blockSize) + HEADER_SIZE;
        int pos2 = rand.nextInt(data.length - HEADER_SIZE - blockSize) + HEADER_SIZE;

        byte[] tempBlock = new byte[blockSize];
        System.arraycopy(mutatedData, pos1, tempBlock, 0, blockSize);
        System.arraycopy(mutatedData, pos2, mutatedData, pos1, blockSize);
        System.arraycopy(tempBlock, 0, mutatedData, pos2, blockSize);

        return mutatedData;
    }


    /**
     * Generates multiple mutated offspring from a seed using various AFL mutators.
     *
     * @param seed   the parent seed input
     * @param otherSeeds other seed contents for splicing
     * @return a set of offspring test inputs.
     */
    @Override
    public Set<String> fuzzOne(Seed seed, Set<Seed> otherSeeds, int energy) {
        byte[] sCont = loadSeed(seed.getContent());
        String sExt = getSeedExt(seed.getContent());

        Random rand = new Random();
        Set<byte[]> mutatedInputs = new HashSet<>();
        Set<String> outs = new HashSet<>();

        for (int i = 0; i < energy; i++) {
            int choice = rand.nextInt(5);
            switch (choice) {
                case 0 :
                    mutatedInputs.add(bitflip(sCont, 32 + rand.nextInt(64)));
                    break;
                case 1 :
                    mutatedInputs.add(byteReplacement(sCont, 4 + rand.nextInt(8)));
                    break;
                case 2 :
                    mutatedInputs.add(byteInsertion(sCont, 2 + rand.nextInt(4)));
                    break;
                case 3 :
                    mutatedInputs.add(byteDeletion(sCont, 2 + rand.nextInt(4)));
                    break;
                case 4 :
                    mutatedInputs.add(blockSwap(sCont, 4 + rand.nextInt(8)));
                    break;
                default:
                    break;
            }
        }

        for (byte[] mutatedInput : mutatedInputs) {
            String out = "./mutated_inputs/" + UUID.randomUUID() + "." + sExt;
            try {
                outs.add(out);
                writeFile(out, mutatedInput);
            } catch (IOException e) {
                System.err.println("Error processing file: " + e.getMessage());
            }
        }

        return outs;
    }


    /**
     * Also generates multiple mutated offspring, but less radical, based on fuzz_one_original().
     *
     * @param seed   the parent seed input
     * @return a set of offspring test inputs.
     */
    @Override
    public Set<String> fuzzOneOriginal(Seed seed) {
        return fuzzOne(seed, null, -1);
    }


    /**
     *
     * @param in Path of a seed
     * @return The extension name of the seed file
     */
    private String getSeedExt(String in) {
        String[] tmp = in.split("\\.");
        return tmp[tmp.length - 1];
    }


    /**
     *
     * @param in Path of a seed
     * @return The byte stream representation of the seed file
     */
    private byte[] loadSeed(String in) {
        try {
            return readFile(in);
        } catch (IOException e) {
            System.err.println("Error processing file: " + e.getMessage());
        }
        return "".getBytes();
    }


    /**
     *
     * This is only for testing purpose, and is subjected to be removed!!!
     */
    public static void main(String[] args) {
        Seed seed = new Seed("fuzzer-demo/src/main/java/edu/nju/isefuzz/SEEDS/djpeg/not_kitty.jpg", false, true);
        StreamMutationComponent.getInstance().fuzzOneOriginal(seed);
    }

}
