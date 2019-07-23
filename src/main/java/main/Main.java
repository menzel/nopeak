package main;

import profile.Profile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

public class Main {

    public static void main(String[] args) {

        long startTime = System.currentTimeMillis();
        String path_sample = null; // path to read bed file

        if (args.length > 2) {
            path_sample = args[1];
        } else printusage(args);


        ////////////////////
        // Creates control csv files
        ////////////////////

        if ("PROFILE".equals(args[0])) {
            if (args.length < 4) {
                System.err.println("Expected params: PROFILE read_file path_to_genome qmer-length threads");
                System.err.println("Run example: java -jar noPeak.jar PROFILE reads.bed hg19/ 8 4");
                System.exit(1);
            }

            int threadsc = Integer.parseInt(args[4]);
            threadsc = threadsc > 24 ? 24 : threadsc; //limit max thread count to 24
            int radius = 500;


            System.out.println("[" + (System.currentTimeMillis() - startTime) + "] Building profiles for " + args[3] + "-mers for a radius of " + radius +  " bp around each read");

            ////////////////////
            // Create Profiles for reads
            ////////////////////

            Profile control = new Profile(path_sample, args[2], Integer.parseInt(args[3]), radius, threadsc);
            control.writeProfilesToFile("profile_" + path_sample.split("/")[path_sample.split("/").length - 1] + ".csv");

        } else if ("BATCH".equals(args[0])) {

            //read batch file
            String batchfile = args[1];
            List<BatchJob> jobs = new ArrayList<>();

            try (Stream<String> stream = Files.lines(Paths.get(batchfile))) {

                stream.forEach(line -> {
                    String[] parts = line.split(",");
                    jobs.add(new BatchJob(parts[0], parts[1], parts[2]));

                });
            } catch (IOException e) {
                e.printStackTrace();
            }

            for(BatchJob job: jobs)
                LogoHelper.logo(job.getControl().toString(), job.getData().toString(), job.getFraglen(), false);

        ////////////////////
            // Get logo from pre-build profile files
        ////////////////////
        } else if ("LOGO".equals(args[0]) || "LOGO_GUI".equals(args[0])) {

            String path_control;
            int fraglen = Integer.parseInt(args[args.length - 1]); // last argument is the fraglen for the signal reads
            boolean show_gui = args[0].contains("GUI");

            if (args.length <= 4) { // PROFILE reads 100

                if (args.length <= 3) {
                    System.err.println("Expected params: LOGO signal_profiles fragment_length");
                    System.err.println("Or with control file: LOGO signal_profiles control_profiles fragment_length");
                    System.err.println("Use estimate_fraglen.jar to estimate the fragment length");
                    System.err.println("Run example: java -jar NoPeak.jar LOGO profile.csv 100");
                    System.exit(1);
                }

                LogoHelper.logo(path_sample, fraglen, show_gui);

            } else {
                path_control = args[2];
                LogoHelper.logo(path_control, path_sample, fraglen, show_gui);
            }

        } else printusage(args);

    }

    private static void printusage(String[] args) {
       if(args.length > 0)
            System.err.println("Unknown command " + args[0]);
        System.err.println("Known modes: PROFILE, LOGO");
        System.err.println("Run example: java -jar noPeak.jar PROFILE reads.bed hg19/ 8 2 100");
        System.err.println("Run example: java -jar noPeak.jar LOGO reads.csv control.csv 100");
        System.exit(1);
    }
}
