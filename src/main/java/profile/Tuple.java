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

public class Tuple<A,T>{

    private final A first;
    private final T second;
    public static final Tuple<Double, Double> EMPTY_DOUBLE_TUPLE;
    public static Tuple EMPTY_TUPLE;

    static {
        EMPTY_DOUBLE_TUPLE = new Tuple<>(null, null);
        EMPTY_TUPLE = new Tuple<>(null, null);
    }

    public Tuple(A first, T second){

        this.first = first;
        this.second = second;
    }

    public A getFirst() {
        return first;
    }

    public T getSecond() {
        return second;
    }
}
