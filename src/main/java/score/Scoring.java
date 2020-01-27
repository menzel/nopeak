package score;

import main.Main;
import org.apache.commons.math3.stat.StatUtils;
import org.apache.commons.math3.stat.descriptive.moment.StandardDeviation;
import profile.ProfileLib;
import profile.Tuple;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.IntStream;

/**
 * Scores a given list of profiles
 *
 */
public class Scoring {

    private List<Score> scores = new ArrayList<>();
    private static int smooth_cutoff;


    /**
     * Scores a given map of profiles for sample and control.
     * Updates the scores variable with the result
     *  @param profiles_sample - profiles for all sample kmers
     * @param fraglen         - fragment length of sample reads
     * @param readcount - count of reads used to build the profiles
     */
    public Scoring(Map<String, List<Integer>> profiles_sample, int fraglen, long readcount) {

        if (readcount > 300000 || Main.getFilter() == 1)
            smooth_cutoff = 50;
        else if (readcount > 100000)
            smooth_cutoff = 20;
        else
            smooth_cutoff = 5;


        for (String qmer : profiles_sample.keySet()) {

            Tuple<Double, Double> score = calcScore(profiles_sample.get(qmer), null, fraglen);

            if (score != Tuple.EMPTY_DOUBLE_TUPLE && score.getFirst() > 0)
                scores.add(new Score(qmer, score.getFirst(), score.getSecond()));
        }
    }



    /**
     * Scores a given map of profiles for sample and control.
     * Updates the scores variable with the result
     *
     * @param profiles_sample - profiles for all sample kmers
     * @param profiles_control - profiles for all control kmers
     * @param fraglen - fragment length of sample reads
     * @param readcount - count of reads used to build the sample profiles
     */
    public Scoring(Map<String, List<Integer>> profiles_sample, Map<String, List<Integer>> profiles_control, int fraglen, long readcount) {
        if (readcount > 300000 || Main.getFilter() == 1)
            smooth_cutoff = 50;
        else if (readcount > 100000)
            smooth_cutoff = 20;
        else
            smooth_cutoff = 5;

        try {
            String some_s = profiles_sample.entrySet().iterator().next().getKey();
            String some_c = profiles_control.entrySet().iterator().next().getKey();

            if (some_c.length() != some_s.length() ||
                    profiles_control.get(some_c).size() != profiles_sample.get(some_s).size()){
                System.err.println("K-mer length of profile and control or radius does not match. The results will not look good.");
            }

        } catch (Exception e) {
            System.err.println("Missing content in control or signal profile file. Please check if the profile files are build correctly.");
        }

        for(String qmer: profiles_sample.keySet()){

            String qmer_rc = ProfileLib.reverse_complement(qmer);
            String qmer_control;

            if(profiles_control.containsKey(qmer))
                qmer_control =  qmer;
            else if(profiles_control.containsKey(qmer_rc))
                qmer_control = qmer_rc;
            else{
                System.out.println("No control data for " + qmer);
                continue;
            }


            Tuple<Double, Double> score = calcScore(profiles_sample.get(qmer), profiles_control.get(qmer_control), fraglen);

            if(score != Tuple.EMPTY_DOUBLE_TUPLE && score.getFirst() > 0)
                scores.add(new Score(qmer, score.getFirst(), score.getSecond()));
        }

        scores.sort(Comparator.comparing(Score::getHeight));
        Collections.reverse(scores);

        // check if top profiles are still intact, rerun the profiles without control otherwise
        // test: all of the top three profiles (bad scored ones ignored) are be below 5
        if (scores.stream().filter(s -> s.getScore() < 0.9).limit(3).allMatch(top -> top.getHeight() < 2)) {
            scores = new ArrayList<>();

            for (String qmer : profiles_sample.keySet()) {

                Tuple<Double, Double> score = calcScore(profiles_sample.get(qmer), null, fraglen);

                if (score != Tuple.EMPTY_DOUBLE_TUPLE && score.getFirst() > 0)
                    scores.add(new Score(qmer, score.getFirst(), score.getSecond()));
            }
        }

    }


    /**
     * Calculates the score for a given sample profile and a control profile.
     *
     * @param profile_sample - profile of a sample kmer
     * @param profile_control - profile of the corresponding control kmer
     * @param fraglen - estimated fragment length of the sample profile k-mer
     *
     * @return score for the given profile
     */
    static Tuple<Double, Double> calcScore(List<Integer> profile_sample, List<Integer> profile_control, int fraglen) {


        List<Number> profile_sample_controlled = apply_control(profile_sample, profile_control);

        List<Double> sma = getSma(profile_sample_controlled, 10);
        double max = Collections.max(sma);
        double half_max = max / 2;
        int pos = profile_sample.size() / 2 + 1 - fraglen;


        double min;

        if(pos - fraglen* 1.5 > 0 && sma.size() > (pos + fraglen*1.5)) {
            min = Collections.min(sma.subList((int) (pos - fraglen * 1.5), (int) (pos + fraglen * 1.5)));
        } else {
            min = Collections.min(sma);
        }


        //  check for correct fraglen position
        if (pos < 0 || pos > sma.size()) { //TODO why > sma.size?
            System.err.println("Fraglen position outside of fragment. Is the fragment length of " + fraglen + " correct?");
            return Tuple.EMPTY_DOUBLE_TUPLE;
        }

        //if(max - min < 1){ return Tuple.EMPTY_DOUBLE_TUPLE; }

        // max outside of fraglen window
        if(sma.indexOf(max) < pos || sma.indexOf(max) > pos + fraglen*1.3){
            return Tuple.EMPTY_DOUBLE_TUPLE;
        }

        //check min near max
        if (Main.getFilter() == 3 && Math.abs(sma.indexOf(Collections.min(sma)) - sma.indexOf(max)) < fraglen * 1.5) {
            return Tuple.EMPTY_DOUBLE_TUPLE;
        }

        double mean = sma.stream().sorted().skip(sma.size() / 2).findFirst().get();
        if (mean - min > (max - mean) / 2) {
            return Tuple.EMPTY_DOUBLE_TUPLE;
        }

        // check smoothness
        if (Main.getFilter() >= 2 && smooth_fc(sma) < smooth_cutoff) {
            return Tuple.EMPTY_DOUBLE_TUPLE;
        }

        double delta  = sma.get(pos) - half_max;
        //return new Tuple<>(smooth_fc(sma), max - min);
        //double height =  max - min;
        double border_mean = sma.subList(sma.size() - 101, sma.size()).stream().sorted().skip(50).findFirst().get();
        double height = max - border_mean;
        return new Tuple<>(Math.abs(delta / (max - half_max)), height);
    }

    /**
     * Apply control profiles to singnal/sample profiles. Control can be null, then the sample is returned unaltered.
     *
     * @param profile_sample  - signal profiles
     * @param profile_control -  control profiles; can be null
     * @return adjusted signal profiles
     */
    private static List<Number> apply_control(List<Integer> profile_sample, List<Integer> profile_control) {


        List<Number> profile_sample_controlled = new ArrayList<>(Collections.nCopies(profile_sample.size(), 0));

        if (profile_control == null) {

            for (int i = 0; i < profile_sample.size(); i++) {
                profile_sample_controlled.set(i, profile_sample.get(i));
            }
            return profile_sample_controlled;
        }

        // border mean normalization (only if more than 100 values are present, which should always be.

        double mean_control_border;
        double mean_signal_border;

        if (profile_control.size() > 100 && profile_sample.size() > 100) {
            // get the mean of the last 100 values for control and signal
            mean_control_border = profile_control.stream().skip(Math.max(0, profile_control.size() - 100)).sorted().skip(50).findFirst().get();
            mean_signal_border = profile_sample.stream().skip(Math.max(0, profile_control.size() - 100)).sorted().skip(50).findFirst().get();

            double factor_c, factor_s;

            if (mean_control_border > mean_signal_border) {
                factor_c = mean_signal_border / mean_control_border;
                factor_s = 1;
            } else {
                factor_s = mean_control_border / mean_signal_border;
                factor_c = 1;
            }

            // border mean normalization
            Tuple<List<Integer>, List<Integer>> normalized = ProfileLib.normalize(profile_sample, profile_control, factor_s, factor_c);

            profile_control = normalized.getSecond();
            profile_sample = normalized.getFirst();
        }

        for (int i = 0; i < profile_sample.size(); i++) {
            profile_sample_controlled.set(i, ((double) profile_sample.get(i)) / profile_control.get(i));
        }

        return profile_sample_controlled;
    }


    private static double smooth_fc(List<Double> sma) {
        int d = 20;
        double[] diff = IntStream.range(d, sma.size()).mapToObj(i -> sma.get(i) - sma.get(i - d)).mapToDouble(v->v).toArray();

        StandardDeviation sd = new StandardDeviation();
        return sd.evaluate(diff) / Math.abs(StatUtils.mean(diff));
    }

    /**
     * Calculates the simple moving average for a given list of int's and a window of size n
     *
     * @param profile - list of integers for input
     * @param n - window size
     * @return sma for the given list of integers
     */
    static List<Double> getSma(List<Number> profile, int n) {

        profile.addAll(0,new ArrayList<>(Collections.nCopies((int) Math.round(((double) n) / 2), 0)));
        List<Double> sma = new ArrayList<>();
        List<Number> current = profile.subList(0, n);

        for(Number val: profile.subList(current.size(), profile.size())){
            sma.add(mean(current));
            current = new ArrayList<>(current.subList(1, current.size()));
            current.add(val);
        }

        sma.add(mean(current));

        return sma;
    }

    /**
     * Calculates the average for a given list of integers
     *
     * @param current - list of integers to calc average from
     * @return average of current input as double
     */
    private static Double mean(List<Number> current) {
        return current.stream().mapToDouble(Number::doubleValue).sorted().skip(current.size() / 2).findFirst().getAsDouble();
    }

    public List<Score> getScores() {

        scores.sort(Comparator.comparing(Score::getScore)); // sort by score
        Collections.reverse(scores);
        return scores;
    }

    public void writeToFile(String outPath) {
        Path path = Paths.get(outPath);
        System.err.println("Writing scores to file: " + outPath);

        scores.sort(Comparator.comparing(Score::getHeight)); // sort by score
        Collections.reverse(scores);

        try (BufferedWriter writer = Files.newBufferedWriter(path)) {

            for(Score score: scores) {

                if (Main.getFilter() == 1)
                    if (score.getScore() < 0.9)
                        writer.write(score.getKmer() + "\t" + score.getHeight() + "\n");
                if (Main.getFilter() == 2)
                    if (score.getScore() < 0.7)
                        writer.write(score.getKmer() + "\t" + score.getHeight() + "\n");
                if (Main.getFilter() == 3)
                    if (score.getScore() < 0.4)
                        writer.write(score.getKmer() + "\t" + score.getHeight() + "\n");
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void setSmooth_cutoff(int cutoff) {
        smooth_cutoff = cutoff;
    }

}
