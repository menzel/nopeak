package fraglen;

import profile.Profile;
import profile.ProfileLib;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class FraglenEstimate {

    public static int estimateFraglen(Map<String, List<Integer>> first, Map<String, List<Integer>> second){


        Set<String> keys = first.keySet().stream().filter(item -> second.containsKey(ProfileLib.reverse_complement(item))).collect(Collectors.toSet());
        int delta = -1;

        for(String key: keys){
            List<Integer> fw = first.get(key);
            List<Integer> bw = second.get(ProfileLib.reverse_complement(key));

            delta = modediff(fw, bw);
        }

        return delta;
    }


    private static int modediff(List<Integer> fw, List<Integer> bw){
        if(fw.size() != bw.size())
            System.err.println("Forward and backward profiles differ in length");
        return bw.indexOf(Collections.max(bw)) - fw.indexOf(Collections.max(fw));
    }
}
