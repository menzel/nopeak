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

import static org.junit.Assert.assertEquals;

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