package logo;

import org.apache.commons.math3.linear.*;
import score.Score;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static filter.GroupKMers.getshift;
import static profile.ProfileLib.reverse_complement;

public class Logo {
    private double[][] pwm; // ATGCN

    private static Map<Character, Integer> mapping = new TreeMap<>();
    private List<String> vars = new ArrayList<>();

    static {
        mapping.put('a', 3);
        mapping.put('t', 0);
        mapping.put('g', 1);
        mapping.put('c', 2);
        //mapping.put('n', 4);
    }

    public Logo(List<String> kmers) {


        pwm = new double[kmers.get(0).length()][4];


        for(String kmer: kmers){
            char[] charArray = kmer.toCharArray();

            for (int i = 0; i < charArray.length; i++) {
                char b = charArray[i];
                pwm[i][mapping.get(b)] += 1;
            }
        }
    }


    public Logo(String seed, List<Score> kmers) {

        setPWM(seed, kmers);
    }


    public void setPWM(String base, List<Score> kmers) {
        //base = base.replaceAll("n", "");
        //base = "nn" + base + "nn";

        List<Character> bases = new ArrayList<>(mapping.keySet());

        for (int i = 0; i < base.length(); i++) {
            for (char c : bases) {
                //for (int i1 = 0; i1 < base.length(); i1++) { // do not set a 'n' base
                    //for (char c1: bases) {

                        char[] basearray = base.toCharArray();

                        basearray[i] = c;
                        //basearray[i1] = c1;

                        String var = String.valueOf(basearray);
                        String seq = var;

                        //seq = seq.replaceAll("^n*", "");
                        //seq = seq.replaceAll("n*$", "");

                        if(seq.length() == 8 && !vars.contains(var)) //TODO fix loops
                            vars.add(var);
                    //}
               // }
            }
        }

        double[][] nums = new double[vars.size()][4 * base.length()];

        int pos = 0;
        for(String var: vars) {
            char[] basearray = var.toCharArray();

            for (int i2 = 0; i2 < basearray.length; i2++) {
                char v = basearray[i2];
                nums[pos][(i2 * 4) + mapping.get(v)] = 1;
            }
            pos++;
        }




        double[] scores =  new double[vars.size()];
        for (int i = 0; i < vars.size(); i++) {

            // use best shift instead of equals
            // delete rows without a score


            String seq = vars.get(i);

            //seq = seq.replaceAll("^n*", "");
            //seq = seq.replaceAll("n*$", "");

            if (seq.length() == 8 && kmers.contains(new Score(seq, 0, 0)))
                scores[i] = Math.log(kmers.get(kmers.indexOf(new Score(seq, 0, 0))).getHeight());
            else
                scores[i] = 0;
        }

        int deletecount = (int) Arrays.stream(scores).filter(d -> d == 0).count();
        double [][] newnums  = new double[nums.length - deletecount][4 * base.length()];

        int x = 0;
        for (int i = 0; i < scores.length; i++) {

            if(scores[i] != 0){
                newnums[x] = nums[i];
                x++;
            }
        }

        scores = Arrays.stream(scores).filter(d -> d != 0).toArray();

        RealMatrix coefficients = new Array2DRowRealMatrix(newnums);
        DecompositionSolver solver = new SingularValueDecomposition(coefficients).getSolver();

        RealVector constants = new ArrayRealVector(scores, false);
        RealVector solution = solver.solve(constants);

        int j = 0;
        double[][] M;
        M = new double[solution.toArray().length/4][4];

        for(int i = 0; i < solution.toArray().length; i++){
            M[j][i%4] = solution.toArray()[i];

            if((i+1) % 4 == 0 )
                j++;
        }

        int top =  0;

        try(BufferedWriter writer = Files.newBufferedWriter(Paths.get("/home/menzel/tmp/nopeak_corr_plot/dat"))) {
            for (Score score : kmers) {
                writer.write(score.getQmer() + " " + score.getHeight() + " " + Math.exp(get_score(M, score.getQmer())) + "\n");

                if (top++ > 5000) break;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private double get_score(double[][] M, String seq) {

        double maxval = -Double.MAX_VALUE;
        String[] seqs = new String[2];
        seqs[0] = seq;
        seqs[1] = reverse_complement(seq);

        for(int s = 0; s < 2; s++) {


            for (int shift = -3; shift < 4; shift++) {

                char[] charArray;
                double val = 0;

                if(shift < 0) {
                    charArray = seqs[s].substring(Math.abs(shift)).toCharArray();
                } else {
                    charArray = seqs[s].substring(0,seq.length() - shift).toCharArray();
                }

                for (int i = 0; i < charArray.length; i++) {

                    char c = charArray[i];
                    if(shift < 0)
                        val += M[i][mapping.get(c)];
                    else {
                        val += M[i + shift][mapping.get(c)];
                    }
                }

                double penalty = 0;

                if(shift > 0){
                    for(int i = 0; i < shift; i++){
                        double p = Arrays.stream(M[i]).max().getAsDouble();

                        if(p < 0)
                            penalty += p;
                    }
                }

                val += penalty;

                if (val > maxval)
                    maxval = val;
            }
        }
        return maxval;

    }


    @Override
    public String toString(){

        StringBuilder r = new StringBuilder("[");

        for (double[] aPwm : pwm) {
            r.append("[");

            for (int i = 0; i < 5; i++) {
                r.append(aPwm[i]);
                r.append(",");
            }

            r.append("],");
        }

        r.append("]");

        return r.toString();
    }

    public String otherToString(){
        String r = "";

        r += ("a\tt\tg\tc\tn\n");

        for(int j = 0; j < pwm.length; j++) {
            r += j + "\t";
            for (int i = 0; i < 5; i++) {
                r += pwm[j][i];
                r += "\t";
            }
            r += "\n";
        }

        return r;
    }


    public double[][] getPwm(){
        return pwm;
    }

    /**
     * Returns a shifted version of the given qmer.
     * E.g. (2,"aaaa",2) returns "nnaaaann"
     *
     * @param prev - count of n's before the qmer
     * @param qmer - qmer sequence
     * @param post - count of n's after the qmer
     *
     * @return qmer with prev n's before the qmer String and pos n's after
     */
    private static String shift(int prev, String qmer, int post) {
        return String.join("", Collections.nCopies(prev, "n")) + qmer + String.join("", Collections.nCopies(post, "n"));
    }


    public static double getMax(double[] vals){
        final double[] max = {Double.NEGATIVE_INFINITY};

        IntStream.of(new Random().ints((int) Math.ceil(Math.log(0.01) / Math.log(1.0 - (1.0/vals.length))),0,vals.length).toArray())
                .forEach(r -> max[0] = (max[0] < vals[r])? vals[r]: max[0]);

        return max[0];
    }


}
