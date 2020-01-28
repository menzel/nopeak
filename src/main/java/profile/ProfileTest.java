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

import org.junit.Test;

import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;

public class ProfileTest {

    @Test
    public void testReadControl() {
        String path = "/home/menzel/Desktop/THM/promotion/projekte/nopeak/test.bed";
        Profile profile = new Profile(path);

        Map<String, List<Integer>> result = profile.getResult();

        assertEquals(9974041,profile.getReadcount());

        assertEquals(new Integer(38), result.get("CCGAAACC").get(0));
        assertEquals(new Integer(144), result.get("GAGCACTA").get(2000));

    }


    @Test
    public void testProfileGen() {

        String path = "/home/menzel/Desktop/THM/promotion/projekte/nopeak/test.bed";
        int q = 8;
        int radius = 500;
        int threadsc = 2;
        String genpath = "/home/menzel/chr/";

        Profile any = new Profile(path, genpath, q, radius, threadsc);

        System.out.println(any.getResult().keySet().size());
    }
}