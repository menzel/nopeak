package score;

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
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 *
 */
public class Scoring {

    private List<Score> scores = new ArrayList<>();

    /**
     * Scores a given map of profiles for sample and control.
     * Updates the scores variable with the result
     *
     * @param profiles_sample - profiles for all sample kmers
     * @param profiles_control - profiles for all control kmers
     * @param fraglen - fragment length of sample reads
     * @param readcount_s - count of reads used for the sample profiles
     * @param readcount_c - count of the reads used for control profiles
     */
    public Scoring(Map<String, List<Integer>> profiles_sample, Map<String, List<Integer>> profiles_control, int fraglen, int readcount_s, int readcount_c) {


        String some_s = profiles_sample.entrySet().iterator().next().getKey();
        String some_c = profiles_control.entrySet().iterator().next().getKey();

        if(some_c.length() != some_s.length() ||
                profiles_control.get(some_c).size() != profiles_sample.get(some_s).size()){
            System.err.println("K-mer length of profile and control or radius does not match. The results will not look good.");
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

            double rs = ProfileLib.calcFactor(readcount_s, readcount_c).getFirst();
            double rc = ProfileLib.calcFactor(readcount_s, readcount_c).getSecond();

            //TODO check profile
            Tuple<Double, Double> score = calcScore(profiles_sample.get(qmer), profiles_control.get(qmer_control), fraglen, rs, rc);

            if(score != Tuple.EMPTY_DOUBLE_TUPLE && score.getFirst() > 0)
                scores.add(new Score(qmer, score.getFirst(), score.getSecond()));
        }
    }

    /**
     * Calculates the score for a given sample profile and a control profile.
     *
     * @param profile_sample - profile of a sample kmer
     * @param profile_control - profile of the corresponding control kmer
     * @param fraglen - estimated fragment length of the sample profile k-mer
     * @param factor_s - normalization factor sample. Calculated using ProfileLib.calcFactor. Use 1 for no correction
     * @param factor_c - normalization factor control. Calculated using ProfileLib.calcFactor. Use 1 for no correction
     *
     * @return score for the given profile
     */
    static Tuple<Double, Double> calcScore(List<Integer> profile_sample, List<Integer> profile_control, int fraglen, double factor_s, double factor_c) {

        /*Tuple<List<Integer>, List<Integer>> normalized  = ProfileLib.normalize(profile_sample, profile_control, factor_s, factor_c);

        profile_control = normalized.getSecond();
        profile_sample = normalized.getFirst();
         */

        // Divide sample profile by control profile because it needs to
        List<Number> profile_sample_controlled = new ArrayList<>(Collections.nCopies(profile_sample.size(),0));

        for(int i = 0; i < profile_sample.size(); i++)
            profile_sample_controlled.set(i, profile_sample.get(i));
            //profile_sample_controlled.set(i, ((double) profile_sample.get(i)) / profile_control.get(i));

        List<Double> sma = getSma(profile_sample_controlled, 10);
        /*
        List<Double> smacopy = new ArrayList<>(sma);
        Collections.sort(smacopy);
        Collections.reverse(smacopy);
        double max = smacopy.get(3); //Collections.min(smacopy.subList(0,5));
         */
        double max = Collections.max(sma);
        double half_max = max / 2;
        int pos = profile_control.size()/2 + 1 - fraglen;

        double min;
        //double avg = sma.stream().mapToDouble(a->a).average().getAsDouble();

        if(pos - fraglen* 1.5 > 0 && sma.size() > (pos + fraglen*1.5)) {
            min = Collections.min(sma.subList((int) (pos - fraglen * 1.5), (int) (pos + fraglen * 1.5)));
        } else {
            min = Collections.min(sma);
        }


        //  check for correct fraglen position
        if(pos < 0 || pos > sma.size()){
            System.err.println("Fraglen position outside of fragment. Is the fragment length of " + fraglen + " correct?");
            return Tuple.EMPTY_DOUBLE_TUPLE;
        }

        //if(max - min < 1){ return Tuple.EMPTY_DOUBLE_TUPLE; }

        // max outside of fraglen window
        if(sma.indexOf(max) < pos || sma.indexOf(max) > pos + fraglen*1.3){
            return Tuple.EMPTY_DOUBLE_TUPLE;
        }

        //check min near max
        if(Math.abs(sma.indexOf(min) - sma.indexOf(max)) < fraglen){
            return Tuple.EMPTY_DOUBLE_TUPLE;
        }


        // check smoothness
        if(smooth_fc(sma) < 50){

            return Tuple.EMPTY_DOUBLE_TUPLE;
        }

        double delta  = sma.get(pos) - half_max;
        //return new Tuple<>(smooth_fc(sma), max - min);
        return new Tuple<>(Math.abs(delta / (max - half_max)), max - min);
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
    static Double mean(List<Number> current) {
        return current.stream().mapToDouble(Number::doubleValue).average().getAsDouble();
    }

    public List<Score> getScores() {

        scores.sort(Comparator.comparing(Score::getScore)); // sort by score
        Collections.reverse(scores);
        return scores;
    }

    public void writeToFile(String outPath) {
        Path path = Paths.get(outPath);

        scores.sort(Comparator.comparing(Score::getHeight)); // sort by score
        Collections.reverse(scores);

        try (BufferedWriter writer = Files.newBufferedWriter(path)) {

            for(Score score: scores) {
                writer.write(score.getQmer() + " " + score.getScore() + " " + score.getHeight() + "\n");
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
