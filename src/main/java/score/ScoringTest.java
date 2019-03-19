package score;

import org.junit.Test;
import profile.Profile;

import java.util.*;

import static org.junit.Assert.*;

public class ScoringTest {

    @Test
    public void calcScore() {

        List<Integer> first = Arrays.asList(1,1,2,2,2,3,1,1,1,2,2,2,3,1,1,1,2,2,2,3,1,1,1,2,2,2,3,1,1,1,2,2,2,3,1,1,1,2,2,2,3,1,1,1,2,2,2,3,1,1,1,2,2,2,3,1,1,1,2,2,2,3,1);
        List<Integer> second = new ArrayList<>(Collections.nCopies(1, first.size()));

        double score = Scoring.calcScore(first, second, 2, 1.,1.).getFirst();

        assertEquals(0.57, score, 0.05);
    }

    @Test
    public void getSma() {
        List<Number> data = new ArrayList<>(Arrays.asList(1,2,2,3,4,5,1,2,2,2,3,1,8,8,7,6,3));

        List<Double> result = Scoring.getSma(data, 5);

        List<Double> expected = Arrays.asList(0.6, 1.0, 1.6, 2.4, 3.2, 3., 3., 2.8, 2.4, 2.,2., 3.2, 4.4, 5.4, 6.,6.4);

        assertEquals(expected, result);
    }


    @Test
    public void fullScoringTest(){
        Profile test = new Profile("/home/menzel/Desktop/THM/promotion/projekte/nopeak/sabine/Profiledaten/MAX/ENCFF000YTY_profile_8_1000.csv");
        //TODO compare the scores with better_get_scores.py output

        for(String kmer: test.getResult().keySet()){
            double score = Scoring.calcScore(test.getResult().get(kmer), test.getResult().get(kmer), 116, 1000., 1000.).getFirst();
            if(score > 0 && kmer.startsWith("aattt"))
                System.out.println(kmer + "\t" + score);
        }
    }

    @Test
    public void mean() {
    }
}