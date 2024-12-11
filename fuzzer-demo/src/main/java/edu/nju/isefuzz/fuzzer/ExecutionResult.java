package edu.nju.isefuzz.fuzzer;

import java.util.Objects;

public class ExecutionResult {
    private String info;
    private int exitVal;

    private int coverageRate;

    public ExecutionResult(String info, int exitVal,int coverageRate) {
        this.info = info;
        this.exitVal = exitVal;
        this.coverageRate = coverageRate;
    }

    public String getInfo() {
        return info;
    }

    public int getExitVal() {
        return exitVal;
    }

    public boolean isCrash() {
        return exitVal != 0;
    }
    public int getCoverageRate() {
        return coverageRate;
    }

    @Override
    public boolean equals(Object that) {
        if (that instanceof ExecutionResult)
            return ((ExecutionResult) that).info.equals(this.info);
        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hash(info);
    }
}
