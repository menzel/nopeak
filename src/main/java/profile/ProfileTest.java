package profile;

import org.junit.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.*;

public class ProfileTest {

    @Test
    public void testReadControl() {
        String path = "/home/menzel/Desktop/THM/promotion/projekte/nopeak/tmp";
        Profile profile = new Profile(path);

        Map<String, List<Integer>> result = profile.getResult();

        assertEquals(9974041,profile.getReadcount());

        assertEquals(new Integer(38), result.get("CCGAAACC").get(0));
        assertEquals(new Integer(144), result.get("GAGCACTA").get(2000));

    }
}