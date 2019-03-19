package profile;

import org.junit.Before;
import org.testng.annotations.Test;

import java.util.*;

public class NoPeakTest {

    @Before
    public void setUp() throws Exception {
    }


    @Test
    public void testWrapper() throws Exception {

        String seq = "tgtaccacattttctctatccagtcatctgttgatggacatttaggtttc";

        Map<String, List<Integer>> profiles_p = Collections.synchronizedMap(new TreeMap<>());
        Map<String, List<Integer>> profiles_n = Collections.synchronizedMap(new TreeMap<>());

        Map<String, long[]> readstarts_p = new HashMap<>();

        readstarts_p.put("chr1", new long[]{10L, 20L});
        readstarts_p.put("chr2", new long[]{10L, 30L});

        ProfileLib lib = new ProfileLib();
        Profile noPeak = new Profile();
        int radius = 4;
        int q = 4;

        Map<String, List<Integer>> p = noPeak.get_profile("chr1", seq, readstarts_p, q, radius);
        lib.merge_chr(profiles_p, p);

        Map<String, List<Integer>> p2 = noPeak.get_profile("chr2", seq, readstarts_p, q, radius);
        lib.merge_chr(profiles_p, p2);

        profiles_p.keySet().forEach(key -> System.out.println(key +  " " + Arrays.toString(profiles_p.get(key).toArray())));

        Map<String, List<Integer>> result = lib.fold_profile(profiles_p);
        System.out.println();

        result.keySet().forEach(key -> System.out.println(key +  " " + Arrays.toString(result.get(key).toArray())));
    }
}