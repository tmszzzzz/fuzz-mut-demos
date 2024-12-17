package edu.nju.isefuzz.fuzzer;

import java.util.HashSet;
import java.util.Random;
import java.util.Set;

public class StringMutationComponent extends MutationComponent {
    private final Random random = new Random();


    /**
     * Singleton Pattern
     */
    private static StringMutationComponent instance;
    private StringMutationComponent() {}
    public static StringMutationComponent getInstance() {
        if(instance == null) {
            instance = new StringMutationComponent();
        }
        return instance;
    }


    /**
     * Bitflip mutation: flips a specific bit in the content.
     *
     * @param sCont the content to mutate
     * @return the mutated content
     */
    private String bitflip(String sCont) {
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
    private String arith(String sCont) {
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
    private String interest(String sCont) {
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
    private String havoc(String sCont) {
        String result = sCont;
        int numMutations = random.nextInt(5) + 1; // Perform 1-5 random mutations
        for (int i = 0; i < numMutations; i++) {
            int choice = random.nextInt(3); // Randomly choose a mutation
            switch (choice) {
                case 0 :
                    result = bitflip(result);
                    break;
                case 1 :
                    result = arith(result);
                    break;
                case 2 :
                    result = interest(result);
                    break;
                default:
                    break;
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
    private String splice(String sCont1, String sCont2) {
        int cut1 = random.nextInt(sCont1.length());
        int cut2 = random.nextInt(sCont2.length());
        return sCont1.substring(0, cut1) + sCont2.substring(cut2);
    }


    /**
     * Generates multiple mutated offspring from a seed using various AFL mutators.
     *
     * @param seed the parent seed input
     * @param otherSeeds other seed contents for splicing
     * @return a set of offspring test inputs.
     */
    @Override
    public Set<String> fuzzOne(Seed seed, Set<Seed> otherSeeds, int energy) {
        String sCont = seed.getContent();
        Set<String> mutatedInputs = new HashSet<>();

        for (int i = 0; i < energy; i++) {
            int choice = random.nextInt(5);
            switch (choice) {
                case 0 :
                    mutatedInputs.add(bitflip(sCont));
                    break;
                case 1 :
                    mutatedInputs.add(arith(sCont));
                    break;
                case 2 :
                    mutatedInputs.add(interest(sCont));
                    break;
                case 3 :
                    mutatedInputs.add(havoc(sCont));
                    break;
                case 4 :

                    if (!otherSeeds.isEmpty()) {
                        Seed other = otherSeeds.stream()
                                .skip(random.nextInt(otherSeeds.size()))
                                .findFirst()
                                .orElse(seed);
                        mutatedInputs.add(splice(sCont, other.getContent()));
                    }
                    break;
                default:
                    break;
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
    @Override
    public Set<String> fuzzOneOriginal(Seed seed) {
        String sCont = seed.getContent();
        Set<String> mutatedInputs = new HashSet<>();
        int mutations = random.nextInt(2) + 1;

        for (int i = 0; i < mutations; i++) {
            int choice = random.nextInt(3);
            switch (choice) {
                case 0 :
                    mutatedInputs.add(bitflip(sCont));
                    break;
                case 1 :
                    mutatedInputs.add(arith(sCont));
                    break;
                case 2 :
                    mutatedInputs.add(interest(sCont));
                    break;
                default:
                    break;
            }
        }

        return mutatedInputs;
    }
}
