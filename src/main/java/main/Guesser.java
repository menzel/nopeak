package main;

import filter.GroupKMers;
import logo.LogoOld;
import score.Score;

import java.util.Collections;
import java.util.List;
import java.util.Map;

class Guesser {

    Guesser(List<Score> scores) {

        double a = 0.1;
        double b = 0.9;
        int basematch = (int) Math.ceil(scores.get(0).getQmer().length() / 2.0);
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
        groupedKmers.keySet().forEach(base -> {
            System.out.print(base.toUpperCase());
            System.out.print("\t");
            System.out.println(finalGroupedKmers.get(base).size());

            LogoOld logo = new LogoOld(finalGroupedKmers.get(base));

            logo.reverse_complement();
            System.out.println(logo);
            System.out.println("\n");

        });
    }


    private double opt(List<Score> scores, int basematch, double score_cutoff, int height_cutoff) {
        Map<String, List<String>> groupedKmers = GroupKMers.groupKMers(scores, basematch, score_cutoff, height_cutoff);
        //String base = groupedKmers.entrySet().stream().limit(1).map(Map.Entry::getKey).collect(Collectors.joining());

        double score = groupedKmers.entrySet().stream().limit(1).map(k -> scorePwm(new LogoOld(k.getValue()).getPwmWithoutN())).findAny().orElse(100.0);
        //System.out.println(ProfileLib.reverse_complement(base).toUpperCase() +  "\t" + score);

        return score;
    }

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
