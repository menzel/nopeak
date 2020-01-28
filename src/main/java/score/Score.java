// Copyright (C) 2020 Michael Menzel
// 
// This file is part of NoPeak. <https://github.com/menzel/nopeak>.
// 
// NoPeak is free software: you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation, either version 3 of the License, or
// (at your option) any later version.
// 
// NoPeak is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.
// 
// You should have received a copy of the GNU General Public License
// along with NoPeak.  If not, see <https://www.gnu.org/licenses/>.  
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
