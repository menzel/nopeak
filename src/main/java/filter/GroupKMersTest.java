package filter;

import org.junit.Test;
import score.Score;

import java.util.*;

import static org.junit.Assert.*;

public class GroupKMersTest {

    @Test
    public void groupKMers() {
        List<Score> input = new ArrayList<>();

        input.add(new Score("aatt", 0.6, 0));
        input.add(new Score("aatc", 0.6, 0));
        input.add(new Score("aaat", 0.6, 0));

        input.add(new Score("tttt", 0.6, 0));
        input.add(new Score("ttta", 0.6, 0));

        input.add(new Score("ccaa", 0.6, 0)); // will be deleted

        Map<String, List<String>> groupedKmers = GroupKMers.groupKMers(input, 3, 1.0);

        Map<String, List<String>> expected = new TreeMap<>();
        expected.put("nnnnaattnnnn", Arrays.asList("nnnnaattnnnn", "nnnnaatcnnnn", "nnnaaatnnnnn"));
        expected.put("nnnnttttnnnn", Arrays.asList("nnnnttttnnnn", "nnnntttannnn"));

        assertEquals(expected, groupedKmers);
    }



    @Test
    public void groupKMers2() {

        List<Score> scores = Arrays.asList(new Score("atttccgg", 0.2548587488414547, 4.344152772070638),
            new Score("cgccggaa", 0.26179231735595443, 17.231991118343092),
            new Score("acttccgg", 0.26182033711147273, 12.546907255548936),
            new Score("ccggaagc", 0.26428145077468873, 7.438126697830183),
            new Score("ccggaaac", 0.28613768015472313, 5.637758460326013),
            new Score("cttccggc", 0.2890624295352217, 8.586536535345694),
            new Score("aaccggaa", 0.2916103274782697, 4.8013066615875495),
            new Score("ttccggaa", 0.30423889144748134, 4.384156633179203),
            new Score("aaaaaaaa", 0.3053256606807215, 1.3217188072539994),
            new Score("ccccggaa", 0.3063778562960898, 5.363331584184147),
            new Score("accggaaa", 0.314839167689918, 4.100247105343072),
            new Score("cgcttccg", 0.3186229489922326, 11.642492342409872),
            new Score("gaccggaa", 0.32541596040129994, 6.378267534622394),
            new Score("cacttccg", 0.34556069470590994, 9.190023770007226),
            new Score("agccggaa", 0.34755696459240404, 4.054301894596139),
            new Score("gccggaaa", 0.35240471436810666, 4.451332139263475),
            new Score("ggccggaa", 0.35981559533357244, 5.258343682152474),
            new Score("cccggaaa", 0.3645697282354395, 3.790029518737596),
            new Score("tcccggaa", 0.3750032637391518, 3.968290195512872),
            new Score("accggaag", 0.37632329942984166, 7.337994018381341),
            new Score("cggcggaa", 0.38571893626846626, 8.111627266771684),
            new Score("cttccgga", 0.38816200806218, 4.310486826107591),
            new Score("ccgccgga", 0.3892126906512687, 7.942062943522496),
            new Score("cggaagta", 0.3928835754250104, 5.1255113068338165),
            new Score("gcccggaa", 0.39433196055085473, 4.162230188888847),
            new Score("cggaaacg", 0.39484368228141953, 7.316477592160689),
            new Score("ccggaaaa", 0.39722424041339904, 2.83035343447041),
            new Score("cggaagtc", 0.40173218746685313, 6.154116818306438),
            new Score("tccggaaa", 0.40546562508555073, 3.086972031915101),
            new Score("caccggaa", 0.4261814068691585, 4.678398035378959),
            new Score("cggaagcc", 0.43229520445243863, 3.945876093493084),
            new Score("aaaaattt", 0.4379326628691145, 1.155119007556758),
            new Score("cccggaag", 0.4398820871620349, 5.32081258089541),
            new Score("ccggaacc", 0.44527494206390383, 4.879729279058393),
            new Score("agttccgg", 0.4456346123812068, 3.461322491966845),
            new Score("cgaccgga", 0.4528204597118029, 6.826538714353681),
            new Score("ccggaaga", 0.45445636115908306, 3.80411707634185),
            new Score("gcgccgga", 0.45858110634083954, 7.752043448804764),
            new Score("aaaccgga", 0.46175874978874065, 2.386476555479793),
            new Score("gacccgga", 0.4629055994137197, 4.1853094466906295),
            new Score("taaaaaaa", 0.4638320357937325, 1.1475352842523903),
            new Score("acatccgg", 0.4643457458070319, 2.696832772096347),
            new Score("aaaaaatt", 0.4647676927499453, 1.1225769539216972),
            new Score("aaaatttt", 0.46923458823454106, 1.1257294964424382),
            new Score("gcggaagc", 0.4748467260652559, 4.142333310482411),
            new Score("atccggaa", 0.47566237097636327, 2.661991304086473),
            new Score("aaaaaaat", 0.4759765903078162, 1.1372746744574704),
            new Score("ctccggaa", 0.4778961938296294, 3.098746446934537),
            new Score("tgccggaa", 0.4782395414004568, 3.6436685955139625),
            new Score("gccggaac", 0.492045307109238, 4.344630351159262),
            new Score("ataaataa", 0.4987989282549088, 1.06629383398229),
            new Score("gaaccgga", 0.5016494628969382, 4.0023309089611345),
            new Score("catttccg", 0.5024821695871826, 2.8694690042493622),
            new Score("accggaac", 0.5058208463192789, 3.9711670568848034),
            new Score("agcttccg", 0.5085260637954955, 3.0693652876584103),
            new Score("taaataaa", 0.5120606404137872, 1.0741755038552319),
            new Score("caaaaaaa", 0.5134119527165741, 1.128630041449456),
            new Score("gagcggaa", 0.5149878753878047, 3.497318909676695),
            new Score("agcggaag", 0.5160990913794585, 4.293634298513607),
            new Score("ttaaaaaa", 0.5166220283225172, 1.1213717467587456),
            new Score("cggaaccg", 0.5191115618334889, 7.028145374327703),
            new Score("aagcggaa", 0.5195785078135093, 3.565960633612046),
            new Score("aataaata", 0.5197147119662673, 1.0623208310823433),
            new Score("tagccgga", 0.5213742774861645, 1.5276900172841261),
            new Score("cccccgga", 0.5221804934400686, 3.0954631684690095),
            new Score("cggaaata", 0.5241319670277079, 1.9097952759151693),
            new Score("cggccgga", 0.5265429829375795, 4.760574015780151),
            new Score("cccggaac", 0.5268480796717552, 3.442106633304873),
            new Score("cggaaacc", 0.5269452307273804, 3.070166485318816),
            new Score("aaaaaaag", 0.5296156299592, 1.1309352058833924),
            new Score("atccggat", 0.5316359689556011, 1.7950315468155766),
            new Score("atttccgc", 0.534067742852348, 2.7310772037174758),
            new Score("ggaccgga", 0.5355595321572342, 3.954958714160112),
            new Score("gcggaaac", 0.5373261057456148, 3.312067613196165),
            new Score("aattttta", 0.5393969498006208, 1.0686848351817255),
            new Score("aatttccg", 0.5397678200609061, 1.9241492361047348),
            new Score("acgtccgg", 0.5398108917911459, 2.639985521525141),
            new Score("aaaaaata", 0.5403483090225677, 1.0675585526588662),
            new Score("atatccgg", 0.5501319350120945, 1.889011269613178),
            new Score("cggaagag", 0.5510312826933195, 3.0068534981630206),
            new Score("ccggaaag", 0.5523812014639373, 2.8977996287198424),
            new Score("gggcggaa", 0.5549057463691055, 4.680731204917894),
            new Score("gccccgga", 0.555200480725455, 3.7582796628266224),
            new Score("atccggcg", 0.5563462749401468, 4.963493456352431),
            new Score("taccggaa", 0.557297318774683, 3.010121735609683),
            new Score("aaaataaa", 0.5584223513797849, 1.0610432949017397),
            new Score("agaccgga", 0.5606478587929098, 2.635840977708226),
            new Score("aaataaat", 0.5615884487192928, 1.0431024745515871),
            new Score("tccggcga", 0.5626685112372315, 5.77773258283289),
            new Score("tcaaaaaa", 0.5644038293898821, 1.0957281445342115),
            new Score("tttaaaaa", 0.5660241750926664, 1.0818041674535326),
            new Score("ccggctaa", 0.566194388919476, 1.1168280692915729),
            new Score("aaaaataa", 0.5674788394492615, 1.0577308061180093),
            new Score("cggcgccg", 0.5677698791958072, 5.839141743607988),
            new Score("acggaagt", 0.5679700517189515, 2.7556107074627896),
            new Score("aataaaaa", 0.5679905896145092, 1.0470741257861027),
            new Score("accggata", 0.5689336624320422, 1.7414554873419468),
            new Score("aaagccgg", 0.5745115948570929, 1.8891246595867262),
            new Score("aagccgga", 0.5756086547596869, 2.5908850394221234),
            new Score("aaataaaa", 0.5764872865147653, 1.042137641452464),
            new Score("aaaattta", 0.576930667068221, 1.0491377781418176),
            new Score("ggcggaaa", 0.5778659220655623, 3.2557459903040256),
            new Score("cggaaaca", 0.5778772353744271, 2.2702758927578945),
            new Score("aaatttta", 0.578281929526121, 1.0438529909848364),
            new Score("aaaaaaac", 0.5788330877551902, 1.0990756236302035),
            new Score("caaccgga", 0.5812977523767738, 2.7698739933358723),
            new Score("cgggcgcc", 0.5848675780367333, 1.7415257327157718),
            new Score("ttttaaaa", 0.5856441608007164, 1.0619599014504089),
            new Score("atttttta", 0.5864021367932307, 1.0244355061954893),
            new Score("acccggat", 0.5878722764111349, 2.0702990317853307),
            new Score("gatccgga", 0.5891851033896517, 2.761316879864164),
            new Score("aattccgg", 0.5901763112870047, 1.8096167122435702),
            new Score("agcggaaa", 0.5910285236499919, 2.523179241528739),
            new Score("cggaaaaa", 0.593246661066956, 1.6989970545142024),
            new Score("aaaaaaga", 0.5956534168195159, 1.0627480182349085),
            new Score("aaaacccg", 0.5987381022463312, 1.516381807532114),
            new Score("cgggtttc", 0.5997451837158875, 1.7002241618620264),
            new Score("cggaagca", 0.5997559947903023, 2.6231795295327696));

        Map<String, List<String>> groupedKmers = GroupKMers.groupKMers(scores, 4, 0.6);

        assertEquals(3, groupedKmers.keySet().size());
    }
}