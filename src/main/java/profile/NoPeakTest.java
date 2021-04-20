// Copyright (C) 2020 Michael Menzel
// 
// This file is part of NoPeak. <https://github.com/menzel/nopeak>.
// 
// NoPeak is free software: you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation, either version 3 of the License, or
// (at your option) any later version.
// 
// NoPeak is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.
// 
// You should have received a copy of the GNU General Public License
// along with NoPeak.  If not, see <https://www.gnu.org/licenses/>.  
package profile;

import org.testng.annotations.Test;

import java.util.*;

public class NoPeakTest {

    @org.testng.annotations.AfterTest
    public void testWrapper() throws Exception {

        String seq = "tgtaccacattttctctatccagtcatctgttgatggacatttaggtttc";

        Map<String, List<Integer>> profiles_p = Collections.synchronizedMap(new TreeMap<>());

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