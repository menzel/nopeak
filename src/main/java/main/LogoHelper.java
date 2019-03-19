package main;

import filter.GroupKMers;
import logo.LogoOld;
import profile.Profile;
import score.Score;
import score.Scoring;

import java.util.List;
import java.util.Map;

public class LogoHelper {

    private static long startTime = System.currentTimeMillis();

    public static void logo(String[] args){
            if (args.length < 3) {
                System.err.println("Expected params: LOGO read_file control_file fraglen");
                System.err.println("Run example: java -jar NoPeak.jar PROFILE reads.csv control.csv 100");
                System.exit(1);
            }

        String path_sample = args[1];
        String path_control = args[2];
        double score_cutoff = 0.4;
        int fraglen = Integer.parseInt(args[4]);

        logo(path_control, path_sample, score_cutoff,fraglen);
    }

    public static void logo(String path_control, String path_sample, double score_cutoff, int fraglen){

            ////////////////////
            // Get Profiles for both sample and control from files
            ////////////////////

            System.out.println("[" + (System.currentTimeMillis() - startTime) + "] Reading sample profiles file " + path_sample);
            Profile profile_s = new Profile(path_sample);


            System.out.println("[" + (System.currentTimeMillis() - startTime) + "] Reading control profiles file " + path_control);
            Profile profile_c = new Profile(path_control);

            Map<String, List<Integer>> profiles_sample = profile_s.getResult();
            Map<String, List<Integer>> profiles_control = profile_c.getResult();

            int readcount_sample = profile_s.getReadcount();
            int readcount_control = profile_c.getReadcount();

            ////////////////////
            // Score each kmer from the profile by the shape of the curve
            ////////////////////

            System.out.println("[" + (System.currentTimeMillis() - startTime) + "] Scoring kmers by curves");
            Scoring scoring = new Scoring(profiles_sample, profiles_control, fraglen, readcount_sample, readcount_control);
            List<Score> scores = scoring.getScores();

            String scorefile = "Scores_" + path_sample.split("/")[path_sample.split("/").length - 1];
            System.out.println("[" + (System.currentTimeMillis() - startTime) + "] Writing scores to file ./" + scorefile);
            scoring.writeToFile(scorefile);


            ////////////////////
            // Cluster the found kmers to create sequence logos
            ////////////////////


            System.out.println("[" + (System.currentTimeMillis() - startTime) + "] Cluster kmers that overlap with at least x bases. Filter kmers with a score less than " + score_cutoff);
            Map<String, List<String>> groupedKmers = GroupKMers.groupKMers(scores, 2, score_cutoff);

            groupedKmers.keySet().forEach(base -> {
                System.out.println(base.toUpperCase());
                //System.out.println(Arrays.toString(groupedKmers.get(base).toArray()));
                System.out.println(groupedKmers.get(base).size());
            });

            System.out.println("[" + (System.currentTimeMillis() - startTime) + "] Create sequence logos from clustered kmers:");

            groupedKmers.keySet().forEach(base -> {
                LogoOld logo = new LogoOld(base, scores);

                System.out.println("=====");
                System.out.println("file: " + path_sample);
                System.out.println("-----");
                System.out.println(base);
                System.out.println(logo);
                System.out.println("-----");
                groupedKmers.get(base).forEach(System.out::println);
                System.out.println("=====");
            });

            System.out.println("Execution time " + (int) (System.currentTimeMillis() - startTime));


    }
}
