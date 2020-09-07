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
package main;

import filter.GroupKMers;
import logo.Logo;
import score.Score;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Guesses the best values for score cutoff and height cutoff to seperate noise and signal value for k-mer profiles
 */
class Guesser {

    Guesser(List<Score> scores) {

        if (scores.size() == 0) {
            System.err.println("No valid profiles found. Please checke the profile shape using the plot_profile.py tool for an expected K-mer and try using the `--filter loose` option.");
        }

        double a = 0.1;
        double b = 0.95;
        int basematch = (int) Math.ceil(scores.get(0).getKmer().length() / 2.0);
        double score_cutoff = scores.stream().map(Score::getScore).mapToDouble(i -> i).sorted().skip((long) (scores.size() * a)).findAny().getAsDouble();
        int height_cutoff = (int) scores.stream().map(Score::getHeight).mapToDouble(i -> i).sorted().skip((long) (scores.size() * b)).findAny().getAsDouble();


        Map<String, List<String>> groupedKmers = GroupKMers.groupKMers(scores, basematch, score_cutoff, height_cutoff);

        while (groupedKmers.keySet().size() == 0) {
            a += 0.1;
            b -= 0.1;

            score_cutoff = scores.stream().map(Score::getScore).mapToDouble(i -> i).sorted().skip((long) (scores.size() * a)).findAny().getAsDouble();
            height_cutoff = (int) scores.stream().map(Score::getHeight).mapToDouble(i -> i).sorted().skip((long) (scores.size() * b)).findAny().getAsDouble();

            groupedKmers = GroupKMers.groupKMers(scores, basematch, score_cutoff, height_cutoff);
        }


        Map<String, List<String>> finalGroupedKmers = groupedKmers;

        System.out.println("Significant motifs:");

        groupedKmers.keySet().forEach(base -> {
            System.out.print("Motif:\t");
            System.out.println(profile.ProfileLib.reverse_complement(base).toUpperCase());
            System.out.print("K-mers count:\t");
            System.out.println(finalGroupedKmers.get(base).size());

            Logo logo = new Logo(finalGroupedKmers.get(base));

            logo.reverse_complement();
            System.out.println("\nLogo as Python array for the plot_pwm.py script:");
            System.out.println(Arrays.deepToString(logo.getPwm()).replace(" ", ""));

            System.out.println("\nLogo in JASPAR format:");
            System.out.println(logo.getJaspar());

            System.out.println("----");
        });
    }

    /**
     * Scores a given PWM for explicitness
     *
     * @param logo PWM as list of lists
     * @return score expressing the clearness of the PWM, used to guess the best cutoff values for k-mer lists
     */
    private double scorePwm(List<List<Integer>> logo) {

        double score = 1.0;

        for (int i = 0; i < logo.size(); i++) { //for each position

            int max = Collections.max(logo.get(i));
            int sum = logo.get(i).stream().mapToInt(j -> j).sum() - max;

            if (max - sum >= 0) {  // good position
                score -= 1.0 / logo.size();
            }
        }

        return score;
    }
}
