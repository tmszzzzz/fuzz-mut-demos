package edu.nju.isefuzz.fuzzer;

public class Seed {
    private String content;
    private boolean isFavored;
    private boolean isCrash;
    private int Coverage_rate;

    public Seed(String content, boolean isFavored) {
        this.content = content;
        this.isFavored = isFavored;
        this.isCrash = false;
        this.Coverage_rate = 0;
    }

    public Seed(String content) {
        this(content, false);
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

    public boolean isFavored() {
        return isFavored;
    }

    public boolean isCrash() {
        return isCrash;
    }

    public void setCoverageRate(int Coverage_rate) { this.Coverage_rate = Coverage_rate;}
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
}
