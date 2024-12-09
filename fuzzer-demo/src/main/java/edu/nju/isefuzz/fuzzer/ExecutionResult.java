package edu.nju.isefuzz.fuzzer;

import java.util.Objects;

public class ExecutionResult {
    private String info;
    private int exitVal;

    public ExecutionResult(String info, int exitVal) {
        this.info = info;
        this.exitVal = exitVal;
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
