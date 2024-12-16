package edu.nju.isefuzz.fuzzer;

import java.io.File;

public class Seed {
    private boolean input_by_file;
    private String content;
    private boolean isFavored;
    private boolean isCrash;
    private int Coverage_rate;
    private int previousCoverage;

    public Seed(String content, boolean isFavored, boolean input_by_file) {
        this.input_by_file = input_by_file;
        this.content = content;
        this.isFavored = isFavored;
        this.isCrash = false;
        this.Coverage_rate = 0;
        this.previousCoverage = 0;
    }

    public Seed(String content) {
        this(content, false,false);
    }

    public Seed(String content,boolean input_by_file) {
        this(content, false,input_by_file);
    }

    public void markFavored() {
        this.isFavored = true;
    }

    public void markCrashed() {
        this.isCrash = true;
    }

    public String getContent() {
        return content;
    }

    public int getCoverageRate() {
        return Coverage_rate;
    }

    public int getPreviousCoverage() {
        return previousCoverage;
    }

    public boolean isFavored() {
        return isFavored;
    }

    public boolean isCrash() {
        return isCrash;
    }

    public void setCoverageRate(int Coverage_rate) { this.Coverage_rate = Coverage_rate;}
    public void setPreviousCoverage(int Coverage_rate) { this.previousCoverage = Coverage_rate;}
    @Override
    public boolean equals(Object that) {
        if (that instanceof Seed)
            return ((Seed) that).content.equals(this.content);
        return false;
    }

    @Override
    public String toString() {
        String suffix = this.isFavored ? "@favored" : "@unfavored";
        return this.content + suffix;
    }

    public boolean isInputByFile() {
        return input_by_file;
    }

}
