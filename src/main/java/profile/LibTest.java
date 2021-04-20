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


import org.junit.Before;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;

public class LibTest {
    private ProfileLib lib;

    @Before
    public void setup(){
        lib = new ProfileLib();
    }

    @org.junit.Test
    public void reverse_complement() {

        List<String> in = Arrays.asList("aatt", "ttaa", "actg", "aaaa");
        List<String> out = Arrays.asList("aatt", "ttaa", "cagt", "tttt");

        for (int i = 0; i < in.size(); i++) {
            assertEquals(ProfileLib.reverse_complement(in.get(i)), out.get(i));
        }
    }

    @org.junit.Test
    public void reverse_keys() {

        Map<String, List<Integer>> newProfiles = new HashMap<>();
        newProfiles.put("aaaa", Arrays.asList(1,2,3,4));
        newProfiles.put("actg", Arrays.asList(1,2,3,4));

        Map<String, List<Integer>> result = lib.reverse_keys(newProfiles);

        Map<String, List<Integer>> expected = new HashMap<>();
        expected.put("tttt", Arrays.asList(1,2,3,4));
        expected.put("cagt", Arrays.asList(1,2,3,4));

        assertEquals(result, expected);
    }

    @org.junit.Test
    public void merge_chr() {

        Map<String, List<Integer>> first = new HashMap<>();
        first.put("aaaa", Arrays.asList(1,2,3,4));
        first.put("actg", Arrays.asList(1,2,3,4));

        Map<String, List<Integer>> second = new HashMap<>();
        second.put("aaaa", Arrays.asList(1,2,3,4));
        second.put("actg", Arrays.asList(1,2,3,4));

        lib.merge_chr(first, second);

        Map<String, List<Integer>> expected = new HashMap<>();
        expected.put("aaaa", Arrays.asList(2,4,6,8));
        expected.put("actg", Arrays.asList(2,4,6,8));

        assertEquals(first, expected);
    }

    @org.junit.Test
    public void merge_profiles() {

        Map<String, List<Integer>> first = new HashMap<>();
        first.put("aaaa", Arrays.asList(1,2,3,4));
        first.put("aatt", Arrays.asList(1,2,3,4));
        first.put("tttt", Arrays.asList(10,10,10,10));

        Map<String, List<Integer>> result = lib.fold_profile(first);

        Map<String, List<Integer>> expected = new HashMap<>();
        expected.put("aaaa", Arrays.asList(11,12,13,14));
        expected.put("aatt", Arrays.asList(1,2,3,4));

        assertEquals(expected, result);
    }

    @org.junit.Test
    public void merge_fw_and_bw() {

        Map<String, List<Integer>> first = new HashMap<>();
        first.put("aaaa", Arrays.asList(1,2,3,4));
        first.put("actg", Arrays.asList(2,2,2,2));
        first.put("atat", Arrays.asList(2,2,2,2));

        Map<String, List<Integer>> second = new HashMap<>();
        second.put("aaaa", Arrays.asList(4,3,2,1));
        second.put(ProfileLib.reverse_complement("actg"), Arrays.asList(2, 2, 2, 2));

        Map<String, List<Integer>> result = lib.merge_fw_and_bw(first, second);

        Map<String, List<Integer>> expected = new HashMap<>();
        expected.put("aaaa", Arrays.asList(2,4,6,8));
        expected.put("actg", Arrays.asList(4,4,4,4));
        expected.put("atat", Arrays.asList(2,2,2,2));

        assertEquals(expected, result);
    }
}