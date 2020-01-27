package main;

import filter.GroupKMers;
import logo.Logo;
import score.Score;

import java.util.*;

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

        List<String> keys = new ArrayList<>(groupedKmers.keySet());

        Map<String, List<String>> finalGroupedKmers1 = groupedKmers;
        keys.sort(Comparator.comparingInt(o -> finalGroupedKmers1.get(o).size()));
        Collections.reverse(keys);

        //System.out.println("Basematch: " + basematch + " score cutoff: " + score_cutoff + " height cutoff: " + height_cutoff);

        Logo top = new Logo(groupedKmers.get(keys.get(0)));
        top.reverse_complement();
        System.out.println(top);
    }


    private double opt(List<Score> scores, int basematch, double score_cutoff, int height_cutoff) {
        Map<String, List<String>> groupedKmers = GroupKMers.groupKMers(scores, basematch, score_cutoff, height_cutoff);
        //String base = groupedKmers.entrySet().stream().limit(1).map(Map.Entry::getKey).collect(Collectors.joining());

        double score = groupedKmers.entrySet().stream().limit(1).map(k -> scorePwm(new Logo(k.getValue()).getPwmWithoutN())).findAny().orElse(100.0);
        //System.out.println(ProfileLib.reverse_complement(base).toUpperCase() +  "\t" + score);

        return score;
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
