package score;

import java.util.Objects;

public class Score {
    private final String qmer;
    private final double score;
    private final double height;

    public Score(String qmer, double score, double height) {
        this.qmer = qmer;
        this.score = score;
        this.height = height;
    }

    public String getQmer() {
        return qmer;
    }

    public double getScore() {

        return score;
    }


    @Override
    public String toString() {
        return qmer + "\t" + score;
    }

    public double getHeight() {
        return height;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Score score = (Score) o;
        return Objects.equals(qmer, score.qmer);
    }

    @Override
    public int hashCode() {
        return Objects.hash(qmer);
    }
}
