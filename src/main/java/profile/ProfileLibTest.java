package profile;

import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;

import static org.junit.Assert.*;

public class ProfileLibTest {

    @Test
    public void normalize() {
        //TODO test
    }

    @Test
    public void calcFactor() {
        Tuple<Double, Double> ex =  new Tuple<>(1.0, 0.6666666666666666);
        assertEquals(ex.getFirst(), ProfileLib.calcFactor(10, 15).getFirst());
        assertEquals(ex.getSecond(), ProfileLib.calcFactor(10, 15).getSecond());
    }
}