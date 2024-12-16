package edu.nju.isefuzz.fuzzer;

import java.util.HashSet;
import java.util.Random;
import java.util.Set;

public abstract class MutationComponent {
    public abstract Set<String> fuzzOne(Seed seed, Set<Seed> otherSeeds, int energy);
    public abstract Set<String> fuzzOneOriginal(Seed seed);


    /* Methods below are legacies, and thus should NOT be invoked. */


    /*
      Call (different flavors of) mutation methods/mutators several times
      to produce a set of test inputs for subsequent test executions. This
      method also showcases a simple power scheduling. The power, i.e., the
      number of mutations, is affected by the flag {@link Seed#isFavored()}.
      A favored seed is mutated 10 times as an unfavored seed.

      @param seed  the parent seed input
     * @return a set of offspring test inputs.
     */
    // Generates multiple mutated offspring from a seed
//    public Set<String> generate(Seed seed) {
//        String sCont = seed.getContent();
//        int basePower = 5;
//        int power = seed.isFavored() ? basePower * 10 : basePower;
//
//        Set<String> testInputs = new HashSet<>(power);
//        for (int i = 0; i < power; i++) {
//            int pos = i % sCont.length();
//            int step = i / sCont.length() + 1;
//            testInputs.add(mutate(sCont, pos, step));
//        }
//        return testInputs;
//    }


    /*
      The essential component of a mutation-based fuzzer. This method
      mutates the given seed once to produce an offspring test input.
      Here the method implements a simple mutator by adding the character
      at the given position by step. Besides, this method ensures the
      mutated character is in [a-z];

      @param sCont the content of the parent seed input.
     * @param pos   the position of the character to be mutated
     * @param step  the step of character flipping.
     * @return an offspring test input
     */
    // Simple character-based mutation, can be extended to byte-level, path-based, etc.
//    public String mutate(String sCont, int pos, int step) {
//        char[] charArr = sCont.toCharArray();
//        char oriChar = charArr[pos];
//
//        // Mutate this char and make sure the result is in [a-z].
//        char mutChar = (oriChar + step > 'z') ?
//                (char) ((oriChar + step) % 'z' - 1 + 'a') :
//                (char) (oriChar + step);
//
//        // Replace the char and return offspring test input.
//        charArr[pos] = mutChar;
//        return new String(charArr);
//    }

}
