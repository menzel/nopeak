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
package score;

import org.junit.Test;
import profile.Profile;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class ScoringTest {

    @Test
    public void getSma() {
        List<Number> data = new ArrayList<>(Arrays.asList(1,2,2,3,4,5,1,2,2,2,3,1,8,8,7,6,3));

        List<Double> result = Scoring.getSma(data, 5);

        List<Double> expected = Arrays.asList(0.0, 1.0, 2.0, 2.0, 3., 3., 3., 2., 2., 2., 2., 2., 3., 7., 7., 7.);

        assertEquals(expected, result);
    }


    @Test
    public void fullScoringTest(){
        Profile test = new Profile("/home/menzel/Desktop/THM/promotion/projekte/nopeak/sabine/Profiledaten/MAX/ENCFF000YTY_profile_8_1000.csv");
        //TODO compare the scores with better_get_scores.py output

        for(String kmer: test.getResult().keySet()){
            double score = Scoring.calcScore(test.getResult().get(kmer), test.getResult().get(kmer), 116).getFirst();
            if(score > 0 && kmer.startsWith("aattt"))
                System.out.println(kmer + "\t" + score);
        }
    }
}