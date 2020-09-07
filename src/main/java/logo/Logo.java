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
package logo;

import score.Score;

import java.util.*;
import java.util.stream.Collectors;

public class Logo {

    private int[][] pwm; // ATGCN

    private static final Map<Character, Integer> mapping = new TreeMap<>();

    static {

        mapping.put('a', 0);
        mapping.put('t', 3);
        mapping.put('g', 2);
        mapping.put('c', 1);
        mapping.put('n', 4);
    }

    public Logo(List<String> kmers) {

        pwm = new int[kmers.get(0).length()][5];

        for(String kmer: kmers){
            char[] charArray = kmer.toCharArray();

            for (int i = 0; i < charArray.length; i++) {
                char b = charArray[i];
                pwm[i][mapping.get(b)] += 1;
            }
        }
    }


    public Logo(String base, List<Score> kmers) {
        base = base.replaceAll("n", "");

        List<Character> bases = new ArrayList<>(mapping.keySet());
        Map<String, Integer> known = kmers.stream().collect(Collectors.toMap(Score::getKmer, s -> (int) (s.getHeight())));
        pwm = new int[base.length()][5];

        for (int i = 0; i < base.length(); i++) {
            for (char c : bases) {
                char[] basearray = base.toCharArray();
                basearray[i] = c;
                int height = known.getOrDefault(String.valueOf(basearray),0);

                if(height > 2)
                    pwm[i][mapping.get(c)] = height;
                else pwm[i][mapping.get(c)] = 0;
            }
        }
    }

    @Override
    public String toString() {
        StringBuilder r = new StringBuilder();

        for (int i = 0; i < 4; i++) {
            for (int[] ints : pwm) {
                r.append(ints[i]);
                r.append(" ");
            }
            r.append("\n");
        }

        return r.toString();
    }


    public int[][] getPwm(){
        return pwm;
    }

    public void reverse_complement() {
        int[][] newpwm = new int[pwm.length][pwm[0].length];

        // ATGCN

        for (int pos = 1; pos <= pwm.length; pos++) {
            for (int l = 0; l < pwm[0].length; l++) {
                switch (l){
                    case 0:
                        newpwm[pwm.length - pos][3] = pwm[pos -1][0];
                        break;
                    case 1:
                        newpwm[pwm.length - pos][2] = pwm[pos -1][1];
                        break;
                    case 2:
                        newpwm[pwm.length - pos][1] = pwm[pos -1][2];
                        break;
                    case 3:
                        newpwm[pwm.length - pos][0] = pwm[pos -1][3];
                        break;
                    case 4:
                        newpwm[pwm.length - pos][4] = pwm[pos -1][4];
                        break;
                }
            }
        }

        this.pwm = newpwm;
    }


    public List<List<Integer>> getPwmWithoutN() {

        List<List<Integer>> tmp = new ArrayList<>();

        for (int[] pos : pwm) {

            List<Integer> t = new ArrayList<>();
            for (int base = 0; base < 4; base++)
                t.add(pos[base]);

            if (t.stream().mapToInt(i -> i).sum() > 0)
                tmp.add(t);
        }

        return tmp;
    }


    public String getJaspar() {
        StringBuilder builder = new StringBuilder();
        builder.append(">\n");


        int skip = 0; // count to skip columns with all zero
        int trunc = 0; // count to skip columns with all zero

        for (int[] pos : pwm) {
            if (Arrays.stream(pos).limit(3).sum() == 0) {
                skip += 1;
            } else break;
        }

        for (int i = pwm.length - 1; i > 0; i--) {
            int[] pos = pwm[i];

            if (Arrays.stream(pos).limit(3).sum() == 0) {
                trunc += 1;
            } else break;
        }

        for (Character base : new Character[]{'a', 'c', 'g', 't'}) {

            StringBuilder row = new StringBuilder();
            row.append(base.toString().toUpperCase());
            row.append(" [");

            for (int i1 = skip; i1 < pwm.length - trunc; i1++) {
                int[] pos = pwm[i1];
                String val = String.valueOf(pos[mapping.get(base)]);

                int i = 2;
                while (i - val.length() > 0) {
                    row.append(" ");
                    i--;
                }

                row.append(val);
            }

            row.append(" ]\n");
            builder.append(row.toString());
        }

        return builder.toString();
    }

}
