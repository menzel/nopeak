package filter;

import profile.ProfileLib;
import profile.Tuple;
import score.Score;

import java.util.*;
import java.util.stream.Collectors;

public class GroupKMers {

    /**
     * Groups a given k-mer to score list accoring to k-mer similarity.
     *
     * @param scores - list of k-mer to scores to group
     * @param basematch - number of bases which still counts as the same group
     * @param score_cutoff - min score still considered for grouping
     * @param height_cutoff - min height still considered for grouping
     *
     * @return map of k-mer groups with the origin k-mers as keys and list of k-mers in this group as value
     */
    public static Map<String, List<String>> groupKMers(List<Score> scores, int basematch, double score_cutoff, int height_cutoff) {

        Map<String, List<String>> mers = new TreeMap<>();

        for(Score s: scores){ //iterate over each qmer/score combination
            String qmer = s.getKmer();
            double score = s.getScore();

            if(score < 0) continue; // ignore negative scores
            if(score > score_cutoff) continue;
            if(s.getHeight() < height_cutoff) continue;


            if(mers.size() == 0) { //always add the first qmer to the list

                qmer = shift(qmer.length(), qmer, qmer.length());
                ArrayList<String> tmp = new ArrayList<>();
                tmp.add(qmer);
                mers.put(qmer, tmp);

            } else {

                double qscore = 0; // highest known score
                int qshift = 0; // shift of the highest score
                String hbase = ""; // highest base qmer

                for(String base: mers.keySet()){ // iterate over the already known base qmers
                    Tuple<Integer, Integer> tmp = getshift(qmer, base);

                    if(tmp.getSecond() > qscore){ // keep the best matching base qmer
                        qshift = tmp.getFirst();
                        qscore = tmp.getSecond();
                        hbase = base;
                    }
                }

                for(String base: mers.keySet()){ // iterate over the already known base qmers
                    String rev = ProfileLib.reverse_complement(qmer);
                    Tuple<Integer, Integer> tmp = getshift(rev, base);

                    if(tmp.getSecond() > qscore){ // keep the best matching base qmer
                        qshift = tmp.getFirst();
                        qscore = tmp.getSecond();
                        hbase = base;
                        qmer = rev;
                    }
                }

                if(qscore >= basematch){ // add to list of known base kmer if the score is high enough
                    qmer = shift(qshift, qmer, qmer.length() * 2 - qshift);
                    List<String> tmp = mers.get(hbase);
                    tmp.add(qmer);
                    mers.put(hbase, tmp);

                } else { //open up new list with qmer as base when no known base qmer is similar
                    qmer = shift(qmer.length(), qmer, qmer.length());

                    ArrayList<String> tmp = new ArrayList<>();
                    tmp.add(qmer);

                    mers.put(qmer, tmp);
                }
            }
        }

        int n = mers.keySet().size();  // count of base kmers
        if(scores.isEmpty()) {
            System.err.println("No qmers left for clustering, you should adjust the filtering.");
            System.exit(1);
        }
        int q = scores.get(0).getKmer().length(); // length of any qmer
        int expected_random_list_length = (int) Math.ceil(Math.pow(0.25, basematch) * (1 + 2 * (q - basematch)) * n);

        List<String> toRemove = mers.keySet().stream()
                .filter(bqmer -> mers.get(bqmer).size() <= expected_random_list_length)
                .collect(Collectors.toList());

        toRemove.forEach(mers::remove);

        return mers;
    }

    /**
     * Scores a k-mer against another k-mer for all possible shifts, retuns the best one
     *
     * @param qmer first k-mer
     * @param base second k-mer
     *
     * @return shift and score for the top scoring shift between the k-mers
     */
    public static Tuple<Integer, Integer> getshift(String qmer, String base) {
        int l = qmer.length();
        int high = 0;
        int hshift = 0;

        for(int shift = -l; shift <= l; shift++){
            int score = score_qmer(qmer, base, shift);

            if(score > high){
                high = score;
                hshift = shift;
            }
        }

        return new Tuple<>(hshift + qmer.length(), high);
    }

    /**
     * Scores a k-mer to another k-mer
     *
     * @param qmer - k-mer to score
     * @param base - k-mer to score against
     * @param shift - offset of both k-mers
     *
     * @return score by matching base count
     */
    private static int score_qmer(String qmer, String base, int shift) {

        char q,b;
        int score = 0;
        base = base.substring(base.length()/3, 2*base.length()/3); //removes n's from base

        for(int i = 0; i < qmer.length(); i++){
            if(shift <= 0){
                q = get_base(qmer, i);
                b = get_base(base, i + shift);

            } else {
                q = get_base(qmer, i - shift);
                b = get_base(base, i);
            }

            if (b != 'X' && q == b)
                score += 1;
        }

        return score;
    }

    /**
     * Returns the base of a given qmer at a given position.
     * If the position is outside of the qmer range a 'X' is returned.
     *
     * @param qmer - qmer to get base from
     * @param pos - position to get char from
     * @return char from qmer from position pos, or 'X' if 0 <= pos < qmer.length()
     */
    private static char get_base(String qmer, int pos) {
        if(0 <= pos && pos < qmer.length()){
            return qmer.substring(pos, pos+1).toCharArray()[0];
        } else return 'X';
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
}
