package logo;

import score.Score;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

public class LogoOld {

    private int[][] pwm; // ATGCN

    private static Map<Character, Integer> mapping = new TreeMap<>();

    static {
        /*
        mapping.put('a', 3);
        mapping.put('t', 0);
        mapping.put('g', 1);
        mapping.put('c', 2);
        mapping.put('n', 4);
         */

        mapping.put('a', 0);
        mapping.put('t', 3);
        mapping.put('g', 2);
        mapping.put('c', 1);
        mapping.put('n', 4);
    }

    public LogoOld(List<String> kmers) {

        pwm = new int[kmers.get(0).length()][5];

        for(String kmer: kmers){
            char[] charArray = kmer.toCharArray();

            for (int i = 0; i < charArray.length; i++) {
                char b = charArray[i];
                pwm[i][mapping.get(b)] += 1;
            }
        }
    }


    public LogoOld(String base, List<Score> kmers) {
        base = base.replaceAll("n", "");

        List<Character> bases = new ArrayList<>(mapping.keySet());
        Map<String, Integer> known = kmers.stream().collect(Collectors.toMap(Score::getQmer, s -> (int) (s.getHeight())));
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
    public String toString(){

        StringBuilder r = new StringBuilder("[");

        for (int[] aPwm : pwm) {
            r.append("[");

            for (int i = 0; i < 5; i++) {
                r.append(aPwm[i]);
                r.append(",");
            }

            r.append("],");
        }

        r.append("]");

        return r.toString();
    }

    public String otherToString(){
        StringBuilder r = new StringBuilder();

        r.append("a\tt\tg\tc\tn\n");

        for(int j = 0; j < pwm.length; j++) {
            r.append(j).append("\t");
            for (int i = 0; i < 5; i++) {
                r.append(pwm[j][i]);
                r.append("\t");
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
}
