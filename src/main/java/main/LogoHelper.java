package main;

import gui.Gui;
import profile.Profile;
import score.Score;
import score.Scoring;

import java.util.List;
import java.util.Map;

class LogoHelper {

    static void logo(String path_sample, int fraglen, boolean gui, String kmerOutputFile) {

        Profile profile = new Profile(path_sample);

        Map<String, List<Integer>> profiles_control = profile.getResult();

        Scoring scoring = new Scoring(profiles_control, fraglen, profile.getReadcount());
        List<Score> scores = scoring.getScores();

        if (kmerOutputFile != null)
            scoring.writeToFile(kmerOutputFile);

        //prints the pwms
        if (gui) {
            Gui g = new Gui(scores);
        } else {
            Guesser guesser = new Guesser(scores);
        }
    }

    static void logo(String path_control, String path_sample, int fraglen, boolean gui, String kmerOutputFile) {

        ////////////////////
        // Get Profiles for both sample and control from files
        ////////////////////

        Profile profile_s = new Profile(path_sample);


        Profile profile_c = new Profile(path_control);

        Map<String, List<Integer>> profiles_sample = profile_s.getResult();
        Map<String, List<Integer>> profiles_control = profile_c.getResult();

        Scoring scoring = new Scoring(profiles_sample, profiles_control, fraglen, profile_s.getReadcount());
        List<Score> scores = scoring.getScores();

        if (kmerOutputFile != null)
            scoring.writeToFile(kmerOutputFile);

        //prints the pwms
        if (gui) {
            Gui g = new Gui(scores);
        } else {
            Guesser guesser = new Guesser(scores);
        }
    }
}
