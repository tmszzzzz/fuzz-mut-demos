package edu.nju.isefuzz.fuzzer;

import java.util.HashSet;
import java.util.Random;
import java.util.Set;

public class MutationComponent {

    private final Random random = new Random();

    /**
     * Bitflip mutation: flips a specific bit in the content.
     *
     * @param sCont the content to mutate
     * @return the mutated content
     */
    public String bitflip(String sCont) {
        char[] charArr = sCont.toCharArray();
        int pos = random.nextInt(charArr.length);
        int bitPos = random.nextInt(8); // Random bit to flip (0-7)
        char flippedChar = (char) (charArr[pos] ^ (1 << bitPos));
        charArr[pos] = flippedChar;
        return new String(charArr);
    }


    /**
     * Arithmetic mutation: applies a small arithmetic operation to a character.
     *
     * @param sCont the content to mutate
     * @return the mutated content
     */
    public String arith(String sCont) {
        char[] charArr = sCont.toCharArray();
        int pos = random.nextInt(charArr.length);
        int delta = random.nextInt(5) - 2; // Random small value in [-2, 2]
        charArr[pos] = (char) (charArr[pos] + delta);
        return new String(charArr);
    }


    /**
     * Interest mutation: replaces a character with a predefined "interesting" value.
     *
     * @param sCont the content to mutate
     * @return the mutated content
     */
    public String interest(String sCont) {
        char[] charArr = sCont.toCharArray();
        int pos = random.nextInt(charArr.length);
        // Example interesting values for characters
        char[] interestingValues = {'a', 'z', 'A', 'Z', '0', '9'};
        charArr[pos] = interestingValues[random.nextInt(interestingValues.length)];
        return new String(charArr);
    }


    /**
     * Havoc mutation: performs random mutations.
     *
     * @param sCont the content to mutate
     * @return the mutated content
     */
    public String havoc(String sCont) {
        String result = sCont;
        int numMutations = random.nextInt(5) + 1; // Perform 1-5 random mutations
        for (int i = 0; i < numMutations; i++) {
            int choice = random.nextInt(3); // Randomly choose a mutation
            switch (choice) {
                case 0 -> result = bitflip(result);
                case 1 -> result = arith(result);
                case 2 -> result = interest(result);
            }
        }
        return result;
    }


    /**
     * Splice mutation: combines two inputs to create a new one.
     *
     * @param sCont1 the first input
     * @param sCont2 the second input
     * @return the spliced content
     */
    public String splice(String sCont1, String sCont2) {
        int cut1 = random.nextInt(sCont1.length());
        int cut2 = random.nextInt(sCont2.length());
        return sCont1.substring(0, cut1) + sCont2.substring(cut2);
    }


    /**
     * Generates multiple mutated offspring from a seed using various AFL mutators.
     *
     * @param seed   the parent seed input
     * @param otherSeeds other seed contents for splicing
     * @return a set of offspring test inputs.
     */
    public Set<String> fuzzOne(Seed seed, Set<Seed> otherSeeds) {
        String sCont = seed.getContent();
        Set<String> mutatedInputs = new HashSet<>();
        int mutations = random.nextInt(5) + 1; // 执行 1-5 次变异

        for (int i = 0; i < mutations; i++) {
            int choice = random.nextInt(5); // 随机选择一个变异算子
            switch (choice) {
                case 0 -> mutatedInputs.add(bitflip(sCont));
                case 1 -> mutatedInputs.add(arith(sCont));
                case 2 -> mutatedInputs.add(interest(sCont));
                case 3 -> mutatedInputs.add(havoc(sCont));
                case 4 -> {
                    if (!otherSeeds.isEmpty()) {
                        Seed other = otherSeeds.stream()
                                .skip(random.nextInt(otherSeeds.size()))
                                .findFirst()
                                .orElse(seed);
                        mutatedInputs.add(splice(sCont, other.getContent()));
                    }
                }
            }
        }

        return mutatedInputs;
    }


    /**
     * Also generates multiple mutated offspring, but less radical, based on fuzz_one_original().
     *
     * @param seed   the parent seed input
     * @return a set of offspring test inputs.
     */
    public Set<String> fuzzOneOriginal(Seed seed) {
        String sCont = seed.getContent();
        Set<String> mutatedInputs = new HashSet<>();
        int mutations = random.nextInt(2) + 1;

        for (int i = 0; i < mutations; i++) {
            int choice = random.nextInt(3);
            switch (choice) {
                case 0 -> mutatedInputs.add(bitflip(sCont));
                case 1 -> mutatedInputs.add(arith(sCont));
                case 2 -> mutatedInputs.add(interest(sCont));
            }
        }

        return mutatedInputs;
    }


    /* Methods below are legacies, and thus should NOT be invoked. */

    /**
     * Call (different flavors of) mutation methods/mutators several times
     * to produce a set of test inputs for subsequent test executions. This
     * method also showcases a simple power scheduling. The power, i.e., the
     * number of mutations, is affected by the flag {@link Seed#isFavored()}.
     * A favored seed is mutated 10 times as an unfavored seed.
     *
     * @param seed  the parent seed input
     * @return a set of offspring test inputs.
     */
    // Generates multiple mutated offspring from a seed
    public Set<String> generate(Seed seed) {
        String sCont = seed.getContent();
        int basePower = 5;
        int power = seed.isFavored() ? basePower * 10 : basePower;

        Set<String> testInputs = new HashSet<>(power);
        for (int i = 0; i < power; i++) {
            int pos = i % sCont.length();
            int step = i / sCont.length() + 1;
            testInputs.add(mutate(sCont, pos, step));
        }
        return testInputs;
    }


    /**
     * The essential component of a mutation-based fuzzer. This method
     * mutates the given seed once to produce an offspring test input.
     * Here the method implements a simple mutator by adding the character
     * at the given position by step. Besides, this method ensures the
     * mutated character is in [a-z];
     *
     * @param sCont the content of the parent seed input.
     * @param pos   the position of the character to be mutated
     * @param step  the step of character flipping.
     * @return an offspring test input
     */
    // Simple character-based mutation, can be extended to byte-level, path-based, etc.
    public String mutate(String sCont, int pos, int step) {
        char[] charArr = sCont.toCharArray();
        char oriChar = charArr[pos];

        // Mutate this char and make sure the result is in [a-z].
        char mutChar = (oriChar + step > 'z') ?
                (char) ((oriChar + step) % 'z' - 1 + 'a') :
                (char) (oriChar + step);

        // Replace the char and return offspring test input.
        charArr[pos] = mutChar;
        return new String(charArr);
    }

}
