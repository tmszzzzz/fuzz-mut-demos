package edu.nju.isefuzz.fuzzer;

public class Seed {
    private String content;
    private boolean isFavored;
    private boolean isCrash;

    public Seed(String content, boolean isFavored) {
        this.content = content;
        this.isFavored = isFavored;
        this.isCrash = false;
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

    public boolean isFavored() {
        return isFavored;
    }

    public boolean isCrash() {
        return isCrash;
    }

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
