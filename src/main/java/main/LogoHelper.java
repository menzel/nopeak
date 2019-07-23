package main;

import gui.Gui;
import profile.Profile;
import score.Score;
import score.Scoring;

import java.util.List;
import java.util.Map;

class LogoHelper {

    private static long startTime = System.currentTimeMillis();

    static void logo(String path_sample, int fraglen, boolean gui) {

        Profile profile_c = new Profile(path_sample);

        Map<String, List<Integer>> profiles_control = profile_c.getResult();

        Scoring scoring = new Scoring(profiles_control, fraglen);
        List<Score> scores = scoring.getScores();

        //prints the pwms
        if (gui) {
            Gui g = new Gui(scores);
        } else {
            Guesser guesser = new Guesser(scores);
        }
    }

    static void logo(String path_control, String path_sample, int fraglen, boolean gui) {

            ////////////////////
            // Get Profiles for both sample and control from files
            ////////////////////

            System.out.println("[" + (System.currentTimeMillis() - startTime) + "] Reading sample profiles file " + path_sample);
            Profile profile_s = new Profile(path_sample);


            System.out.println("[" + (System.currentTimeMillis() - startTime) + "] Reading control profiles file " + path_control);
            Profile profile_c = new Profile(path_control);

            Map<String, List<Integer>> profiles_sample = profile_s.getResult();
            Map<String, List<Integer>> profiles_control = profile_c.getResult();

            System.out.println("[" + (System.currentTimeMillis() - startTime) + "] Scoring kmers by curves");
        Scoring scoring = new Scoring(profiles_sample, profiles_control, fraglen);
            List<Score> scores = scoring.getScores();

            String scorefile = "Scores_" + path_sample.split("/")[path_sample.split("/").length - 1];
            System.out.println("[" + (System.currentTimeMillis() - startTime) + "] Writing scores to file ./" + scorefile);
            scoring.writeToFile(scorefile);


        //prints the pwms
        if (gui) {
            Gui g = new Gui(scores);
        } else {
            Guesser guesser = new Guesser(scores);
        }
    }
}
