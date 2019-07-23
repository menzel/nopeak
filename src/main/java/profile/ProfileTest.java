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