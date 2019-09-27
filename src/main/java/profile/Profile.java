package profile;


import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.*;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 *
 */
public class Profile {

    private String genomepath;

    private Map<String, long[]> readstarts_p = new TreeMap<>();
    private Map<String, long[]> readstarts_n = new TreeMap<>();

    private int q = 8;
    private int radius;

    private ProfileLib lib = new ProfileLib();

    private int readcount = 0;
    private String readPath;

    private Map<String, List<Integer>> result;

    private List<Integer> zeroes;

    Profile(){
        genomepath = null;
    }

    /**
     * Calculates the profiles for the given reads, the given genome, q-mer length, radius in n threads
     *
     * @param readPath - path for reads file in bed format
     * @param genomepath - genome as fasta format
     * @param q - kmer size
     * @param radius - radius around reads to look at
     * @param nThreads - thread count to use
     */
    public Profile(String readPath, String genomepath, int q, int radius, int nThreads) {


        zeroes = Collections.nCopies(radius * 2 + 1, 0);
        System.out.println("Reading reads from file " + readPath);

        this.genomepath = genomepath;
        this.q = q;
        this.radius = radius;
        this.readPath = readPath;

        readReads(readPath);

        readcount += readstarts_n.values().stream().map(l -> l.length).mapToInt(i -> i).sum();
        readcount += readstarts_p.values().stream().map(l -> l.length).mapToInt(i -> i).sum();

        ThreadPoolExecutor service = new ThreadPoolExecutor(nThreads, nThreads, 0L, TimeUnit.MILLISECONDS, new LinkedBlockingDeque<>());
        //Executors.newFixedThreadPool(nThreads);

        Runnable status = () -> {
            while(service.getActiveCount() > 0) {
                System.out.println(service.getCompletedTaskCount() + " of 24 chromosomes are finished. " + service.getActiveCount() + " threads are running");
                try {
                    Thread.sleep(1000 * 60);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        };


        Map<String, List<Integer>> profiles_p = Collections.synchronizedMap(new TreeMap<>());
        Map<String, List<Integer>> profiles_n = Collections.synchronizedMap(new TreeMap<>());

        for (int i : new int[]{10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 1, 20, 21, 22, 2, 3, 4, 5, 6, 7, 8, 9, 23, 24}) {

            String chr;
            if (i == 23) chr = "chrX";
            else if (i == 24) chr = "chrY";
            else chr = "chr" + i;

            service.execute(new chr_wrapper(chr, readstarts_p, readstarts_n, profiles_p, profiles_n));
        }

        status.run();

        service.shutdown();


        try {
            service.awaitTermination(1, TimeUnit.DAYS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        System.out.println("Folding and mergin profile (Single threaded)");

        profiles_n = lib.fold_profile(profiles_n);
        profiles_p = lib.fold_profile(profiles_p);

        result = lib.merge_fw_and_bw(profiles_n, profiles_p);
    }

    /**
     * Read only constructor
     *
     * @param path_control - path to result file
     */
    public Profile(String path_control) {

        result = new TreeMap<>();

        try (Stream<String> stream = Files.lines(Paths.get(path_control))) {

            stream.forEach(line -> {

                if (line.startsWith("#")) {
                    if(line.startsWith("# reads used:"))
                        readcount =  Integer.parseInt(line.split(": ")[1]);

                } else if (line.startsWith("qmer")) {

                    String[] parts = line.split("\t");
                    List<Integer> vals = Arrays.asList(parts).subList(1, parts.length).stream().map(Integer::parseInt).collect(Collectors.toList());

                    if(vals.get(vals.size() - 1) != vals.size()){
                        System.err.println("Profile file does not match in length. Filepath" + path_control);
                        System.err.println("Last val in head line is: " + vals.get(vals.size() - 1) + ". Length should be " + vals.size());
                    }

                } else {

                    String[] parts = line.split("\t");

                    String qmer = parts[0];
                    List<Integer> vals = Arrays.asList(parts).subList(1, parts.length).stream().map(Integer::parseInt).collect(Collectors.toList());

                    this.result.put(qmer.toLowerCase(), vals);
                }
            });

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public Map<String, List<Integer>> getResult() {
        return result;
    }

    /**
     *  Writes the results to given file
     *
     * @param outPath - path to write results to
     */
    public void writeProfilesToFile(String outPath){
        Path path = Paths.get(outPath);

        try (BufferedWriter writer = Files.newBufferedWriter(path, StandardOpenOption.CREATE)) {

            writer.write("# genome: " + genomepath + "\n");
            writer.write("# reads: " + readPath + "\n");
            writer.write("# radius: " + radius + "\n");
            writer.write("# q: " + q + "\n");
            writer.write("# reads used: " + readcount + "\n");


            // header
            writer.write("qmer\t");
            for(int i = 1; i <= radius * 2 + 1; i++){
                writer.write(i + "\t");
            }

            writer.write("\n");

            // data
            for(String qmer: result.keySet()){
                writer.write(qmer.toUpperCase() + "\t");
                writer.write(result.get(qmer).stream().map(i -> Integer.toString(i)).collect(Collectors.joining("\t")));
                writer.write("\n");
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

        System.out.println("Wrote profiles to " + path.toAbsolutePath());
    }

    int getReadcount() {
        return this.readcount;
    }

    /**
     *
     */
    class chr_wrapper implements Runnable{

        private final String chr;
        private final Map<String, List<Integer>> profiles_p;
        private final Map<String, List<Integer>> profiles_n;

        private Map<String, long[]> readstarts_n;
        private Map<String, long[]> readstarts_p;


        /**
         * Wrapper to call get_profile for one chromosome and merge the results into global map
         *
         * @param chr - chr name to look at
         * @param readstarts_p - readstarts for forward strand
         * @param readstarts_n - readstarts for backward strand
         * @param profiles_p - ref to global profiles map to merge result into (forward); should be thread safe
         * @param profiles_n - ref to global profiles map to merge result into (backward); should be thread safe
         */
        chr_wrapper(String chr, Map<String, long[]> readstarts_p, Map<String, long[]> readstarts_n, Map<String, List<Integer>> profiles_p, Map<String, List<Integer>> profiles_n) {

            this.profiles_p = profiles_p;
            this.profiles_n = profiles_n;

            this.chr = chr;

            this.readstarts_n = readstarts_n;
            this.readstarts_p = readstarts_p;
        }

        @Override
        public void run() {
            System.out.println("Working on " + chr);

            if (readstarts_n == null || readstarts_p == null) {
                System.err.println("Chromosome " + chr + " has no entries. NoPeak will ignore this chromosome");
                return;
            }

            if(readstarts_n.get(chr).length + readstarts_p.get(chr).length < 1)
               return;
            String seq = readChr(genomepath, chr);

            Map<String, List<Integer>> p = get_profile(chr, seq, readstarts_p, q, radius);
            lib.merge_chr(profiles_p, p);

            Map<String, List<Integer>> n = get_profile(chr, seq, readstarts_n, q , radius);
            lib.merge_chr(profiles_n, n);
        }
    }

    /**
     * Returns the profile for a given chr, the given sequence and readstarts
     *
     * @param chr - chr name
     * @param seq - sequende to look the kmer up
     * @param readstarts  - all readstarts to look up
     * @param q - length of q-mer
     * @param radius - radius to consider
     *
     * @return profiles for each found q-mer
     */
    Map<String,List<Integer>> get_profile(String chr, String seq, Map<String, long[]> readstarts, int q, int radius) {
        Map<String, List<Integer>> profile = new TreeMap<>();
        long[] reads = readstarts.get(chr);
        Arrays.sort(reads);

        if(readstarts.containsKey(chr)) for(long pos: reads){

            int left = Math.toIntExact(Math.max(0, pos - radius - q / 2));
            int right = Math.toIntExact(Math.min(pos + radius + q / 2, seq.length()) - q);

            int i = 0;

            for(int p = left; p <= right; p++, i++){
                String qmer = seq.substring(p, p + q).toLowerCase();

                if(qmer.contains("n"))
                    continue;

                if(!profile.containsKey(qmer))
                    profile.put(qmer, new ArrayList<>(zeroes));

                List<Integer> prof = profile.get(qmer);
                prof.set(i, prof.get(i) + 1);
                //System.out.println(qmer + " insert +1 at " + i + " " + Arrays.toString(prof.toArray()));
            }
        }

        return profile;
    }


    /**
     * Reads the sequence from path for the given chromosome chr
     *
     * @param path - path to genome (e.g. hg19 or hg38 fasta file)
     * @param chr - chromosome name, e.g. chr2, chr11, chrX
     *
     * @return sequence of given chr as String
     */
    private String readChr(String path, String chr){
        StringBuilder chromosome = new StringBuilder();

        try (BufferedReader br = new BufferedReader(new FileReader(Paths.get(path + "/" + chr + ".fa").toFile()))) {
            String line;

            while ((line = br.readLine()) != null)
                if(!line.startsWith(">chr")) chromosome.append(line.trim());

        } catch (IOException e) {
            e.printStackTrace();
        }

        return chromosome.toString(); //String.join("", chromosome);
    }


    /**
     * Reads the reads for the given path
     *
     * @param path - path to read file in bed format
     */
    private void readReads(String path){

        try (Stream<String> stream = Files.lines(Paths.get(path))) {

            final String[] oldchr = {""};

            List<Long> positions_p = new ArrayList<>();
            List<Long> positions_n = new ArrayList<>();

            stream.forEach(line -> {
                if (line.startsWith("chr")){
                    String[] parts = line.split("\\s");

                    if(!oldchr[0].equals(parts[0])) {

                        if (oldchr[0].length() > 1) {

                            if(readstarts_n.containsKey(oldchr[0]))
                                System.err.println("Read file " + path + " is not sorted.");

                            readstarts_p.put(oldchr[0], positions_p.stream().distinct().sorted().mapToLong(l -> l).toArray());
                            readstarts_n.put(oldchr[0], positions_n.stream().distinct().sorted().mapToLong(l -> l).toArray());
                        }

                        oldchr[0] = parts[0];

                        positions_p.clear();
                        positions_n.clear();
                    }

                    if(line.contains("+"))
                        positions_p.add(Long.valueOf(parts[1]));
                    else
                        positions_n.add(Long.valueOf(parts[2]) - 1);
                }
            });

            readstarts_p.put(oldchr[0], positions_p.stream().sorted().mapToLong(l -> l).toArray());
            readstarts_n.put(oldchr[0], positions_n.stream().sorted().mapToLong(l -> l).toArray());

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
