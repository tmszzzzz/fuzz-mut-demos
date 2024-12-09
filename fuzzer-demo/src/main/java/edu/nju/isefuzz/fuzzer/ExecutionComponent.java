package edu.nju.isefuzz.fuzzer;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class ExecutionComponent {

    /**
     * An execution method for Java-main fuzz targets/drivers. The method
     * execute the given fuzz target once and return the output of the
     * fuzz target.
     *
     * @param cp classpath to the fuzz target
     * @param tn target name, essentially the fully qualified name of a
     *           java class
     * @param ti (the content of) the test input
     * @return the output of the fuzz target.
     * @throws IOException if the executor starts wrongly.
     */
    // Executes the fuzz target and returns the result
    public ExecutionResult execute(String cp, String tn, String ti) throws IOException, InterruptedException {
        // Construct the executor
        ProcessBuilder pb = new ProcessBuilder("java", "-cp", cp, tn, ti);

        // Redirect execution result to here and execute.
        pb.redirectErrorStream(true);
        Process p = pb.start();
        BufferedReader br = new BufferedReader(new InputStreamReader(p.getInputStream()));

        // Wait for execution to finish, or we cannot get exit value.
        p.waitFor();

        // Read execution info
        StringBuilder infoBuilder = new StringBuilder();
        String line;
        while ((line = br.readLine()) != null) {
            infoBuilder.append(line).append('\n');
        }

        // Wrap and return execution result
        return new ExecutionResult(infoBuilder.toString(), p.exitValue());
    }
}
