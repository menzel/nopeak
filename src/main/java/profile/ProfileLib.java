package profile;

import java.util.*;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;

/**
 * Contains profile functions
 */
public class ProfileLib {

    private final Lock updateLock = new ReentrantLock();

    /**
     * Creates the reverse complement of a given sequence
     *
     * @param key - sequence to reverse complement
     * @return reverse complement of given sequence in lowercase
     */
    public static String reverse_complement(String key) {
        String result = "";

        key = key.toLowerCase();

        for(int i = key.length() - 1; i >= 0; --i)
            result += key.charAt(i);

        result = result.replace("a", "x");
        result = result.replace("t", "a");
        result = result.replace("x", "t");

        result = result.replace("g", "x");
        result = result.replace("c", "g");
        result = result.replace("x", "c");

        return result;
    }

    public static Tuple<List<Integer>, List<Integer>> normalize(List<Integer> profile_control, List<Integer> profile_sample, double rc, double rs) {

        List<Integer> c = profile_control.stream().map(val -> val * rc).map(Double::intValue).collect(Collectors.toList());
        List<Integer> s = profile_sample.stream().map(val -> val * rs).map(Double::intValue).collect(Collectors.toList());

        return new Tuple<>(c, s);
    }

    public static Tuple<Double, Double> calcFactor(double rs, double rc) {
        double norm_s = 1;
        double norm_c = 1;

        if(rs > rc){
            norm_s = rc / rs;
        } else if (rs < rc){
            norm_c = rs / rc;
        }

        return new Tuple<>(norm_s, norm_c);
    }


    /**
     * Reverses all keys in given profile map
     *
     * @param profiles - map of profiles
     * @return given profiles with reversed keys
     */
    Map<String,List<Integer>> reverse_keys(Map<String, List<Integer>> profiles) {
        Map<String, List<Integer>> newProfiles = new HashMap<>();

        for(String key: profiles.keySet()){
            newProfiles.put(reverse_complement(key), profiles.get(key));
        }

        return newProfiles;
    }

    /**
     * Merges the second map of profiles into the first map of profiles
     * (Threadsafe)
     * Used to merge the results from two differen chromosomes
     *
     * @param profiles - profiles to be merged into
     * @param new_profiles - profiles to be merged
     */
    void merge_chr(Map<String, List<Integer>> profiles, Map<String, List<Integer>> new_profiles) {

            for (String qmer : new_profiles.keySet()){

                if (profiles.containsKey(qmer)) {

                    synchronized (updateLock) {
                        List<Integer> profile2 = new_profiles.get(qmer);
                        List<Integer> profile1 = profiles.get(qmer);

                        for (int i = 0; i < profile1.size(); i++) {
                            profile1.set(i, profile1.get(i) + profile2.get(i));
                        }
                    }

                } else {
                    profiles.put(qmer, new_profiles.get(qmer));
                }
        }
    }


    /**
     * Combines the profiles of the reverse complement key of each profile with the profile of the key.
     * For example key AAAA and TTTT are merged into one profile
     *
     * @param profiles - profiles to combine
     * @return map with combined profiles
     */
    Map<String, List<Integer>> fold_profile(Map<String, List<Integer>> profiles) {
        Map<String, List<Integer>> result = new TreeMap<>();


        for (String qmer : profiles.keySet()) {
            String qmer_rc = reverse_complement(qmer);

            if (qmer.compareTo(qmer_rc) > 0 && profiles.containsKey(qmer_rc))
                continue;

            if (!qmer_rc.equals(qmer) && profiles.containsKey(qmer_rc)) {

                List<Integer> profile1 = profiles.get(qmer);
                List<Integer> profile2 = profiles.get(qmer_rc);
                List<Integer> tmp = new ArrayList<>(Collections.nCopies(profile1.size(), 0));

                for (int i = 0; i < profile1.size(); i++) {
                    tmp.set(i, profile1.get(i) + profile2.get(i));
                }

                result.put(qmer, tmp);

            } else {
                result.put(qmer, profiles.get(qmer));
            }
        }

        return result;
    }

    /**
     * Merges forward and backward strand profiles
     *
     * @param profiles_p - forward profiles
     * @param profiles_n - backward profiles
     *
     * @return merged profiles
     */
    Map<String, List<Integer>> merge_fw_and_bw(Map<String, List<Integer>> profiles_p, Map<String, List<Integer>> profiles_n) {
        Map<String, List<Integer>> result = new TreeMap<>();

        for (String qmer : profiles_p.keySet()) {
            String qmer_n;

            if (profiles_n.containsKey(qmer)) {
                qmer_n = qmer;
            } else if (profiles_n.containsKey(reverse_complement(qmer))) {
                qmer_n = reverse_complement(qmer);
            } else {
                result.put(qmer, new ArrayList<>(profiles_p.get(qmer)));
                continue;
            }

            List<Integer> profile1 = profiles_n.get(qmer_n);
            List<Integer> profile2 = profiles_p.get(qmer);
            List<Integer> newprofile = new ArrayList<>(profiles_p.get(qmer));

            profiles_n.remove(qmer_n);

            for (int i = 0; i < profile2.size(); i++) {
                newprofile.set(i, profile2.get(i) + profile1.get(profile1.size() - 1 - i));
            }

            result.put(qmer, newprofile);
        }

        for(String key: profiles_n.keySet()){
            List<Integer> tmp = new ArrayList<>(profiles_n.get(key));
            Collections.reverse(tmp);
            result.put(key, tmp);
        }

        return result;

    }
}
