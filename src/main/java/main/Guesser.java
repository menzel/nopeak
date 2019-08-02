package main;

import filter.GroupKMers;
import logo.LogoOld;
import score.Score;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

class Guesser {

    Guesser(List<Score> scores) {

        int basematch = (int) Math.ceil(scores.get(0).getQmer().length() / 3.0);
        double score_cutoff = scores.stream().map(Score::getScore).mapToDouble(i -> i).skip((long) (scores.size() * 0.2)).findAny().getAsDouble();
        int height_cutoff = (int) scores.stream().map(Score::getHeight).mapToDouble(i -> i).skip((long) (scores.size() * 0.5)).findAny().getAsDouble();

        int max_height = (int) scores.stream().map(Score::getHeight).mapToDouble(i -> i).max().getAsDouble();

        double best = 100;

        for (double s = 0; s < 0.5; s += 0.1) { //scores
            for (double h = max_height; h > (max_height/10.0); h -= max_height / 10.0) { //heights

                double score = opt(scores, basematch, s, (int) h);

                if (score < best && score > 0.1) {
                    best = score;
                    score_cutoff = s;
                    height_cutoff = (int) h;
                }

            }
        }

        Map<String, List<String>> groupedKmers = GroupKMers.groupKMers(scores, basematch, score_cutoff, height_cutoff);
        //System.out.println("Basematch: " + basematch + " score cutoff: " + score_cutoff + " height cutoff: " + height_cutoff);

        if (groupedKmers.keySet().size() == 0) {
            System.err.println("No kmers found for these parameters. Adjust score and height cutoff: " + score_cutoff + " " + height_cutoff);
        }

        groupedKmers.keySet().forEach(base -> {
            //System.out.print(ProfileLib.reverse_complement(base).toUpperCase());
            //System.out.print("\t");
            //System.out.println(groupedKmers.get(base).size());

            LogoOld logo = new LogoOld(groupedKmers.get(base));

            logo.reverse_complement();
            System.out.println(Arrays.deepToString(logo.getPwm()).replace(" ", ""));

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
