package logo;

import org.junit.Test;
import score.Score;
import sun.rmi.runtime.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.Assert.*;

public class LogoTest {

    @Test
    public void createLogoTest() throws Exception{
        List<String> seq  = Arrays.asList(  "aatt",
                                            "atta",
                                            "aagt",
                                            "annn");

        new Logo(seq);

    }

    @Test
    public void footest() throws Exception{
        System.out.println("foo");

    }

    @Test
    public void gabpaTest() throws Exception{
        List<Score> seq = new ArrayList<>();

		try (BufferedReader br = Files.newBufferedReader(Paths.get("/home/menzel/Desktop/THM/promotion/projekte/nopeak/Scores_profile_ENCFF000YTY.500.csv_1001"))) {

			String line;
			while ((line = br.readLine()) != null) {
                String[] parts = line.split(" ");
                seq.add(new Score(parts[0],Double.parseDouble(parts[1]),Double.parseDouble(parts[2])));
			}

		} catch (IOException e) {
			e.printStackTrace();
		}

        Logo logo = new Logo(seq.get(0).getQmer(),seq);
        System.out.println(logo);

    }


    @Test
    public void createLogoVarTest() throws Exception{
        List<Score> seq  = Arrays.asList(  new Score("aatt", 0.5, 2.9),
                                            new Score("actt", 0.5, 2.3),
                                            new Score("gatt", 0.5, 0.5));

        Logo logo = new Logo("aatt",seq);
    }
}