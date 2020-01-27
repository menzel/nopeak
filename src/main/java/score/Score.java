package score;

import java.util.Objects;

/**
 * Container class to store score k-mer to score information
 */
public class Score {
    private final String kmer;
    private final double score;
    private final double height;

    public Score(String kmer, double score, double height) {
        this.kmer = kmer;
        this.score = score;
        this.height = height;
    }

    public String getKmer() {
        return kmer;
    }

    public double getScore() {

        return score;
    }


    @Override
    public String toString() {
        return kmer + "\t" + score;
    }

    public double getHeight() {
        return height;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Score score = (Score) o;
        return Objects.equals(kmer, score.kmer);
    }

    @Override
    public int hashCode() {
        return Objects.hash(kmer);
    }
}
