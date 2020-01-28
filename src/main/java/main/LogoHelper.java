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
package main;

import gui.Gui;
import profile.Profile;
import score.Score;
import score.Scoring;

import java.util.List;
import java.util.Map;

/**
 * Helper class for Logos.
 */
class LogoHelper {

    /**
     * Creates a logo from a given path to a profile
     *
     * @param path_sample    - path to profile file
     * @param fraglen        - fragment length from estimateFraglen.jar program
     * @param gui            - show GUI after calculation. If 'false' height and score cutoffs are estimated
     * @param kmerOutputFile - if not null, a file containing the k-mer to score list will be written to this path
     */
    static void logo(String path_sample, int fraglen, boolean gui, String kmerOutputFile) {

        Profile profile = new Profile(path_sample);

        Map<String, List<Integer>> profiles_control = profile.getResult();

        Scoring scoring = new Scoring(profiles_control, fraglen, profile.getReadcount());
        List<Score> scores = scoring.getScores();

        if (kmerOutputFile != null)
            scoring.writeToFile(kmerOutputFile);

        if (gui) {
            new Gui(scores);
        } else {
            new Guesser(scores);
        }
    }

    /**
     * Creates a logo from a given path to a profile
     *
     * @param path_control - path to control profiles
     * @param path_sample - path to profile file
     * @param fraglen - fragment length from estimateFraglen.jar program
     * @param gui - show GUI after calculation. If 'false' height and score cutoffs are estimated
     * @param kmerOutputFile - if not null, a file containing the k-mer to score list will be written to this path
     */
    static void logo(String path_control, String path_sample, int fraglen, boolean gui, String kmerOutputFile) {

        ////////////////////
        // Get Profiles for both sample and control from files
        ////////////////////

        Profile profile_s = new Profile(path_sample);


        Profile profile_c = new Profile(path_control);

        Map<String, List<Integer>> profiles_sample = profile_s.getResult();
        Map<String, List<Integer>> profiles_control = profile_c.getResult();

        Scoring scoring = new Scoring(profiles_sample, profiles_control, fraglen, profile_s.getReadcount());
        List<Score> scores = scoring.getScores();

        if (kmerOutputFile != null)
            scoring.writeToFile(kmerOutputFile);

        if (gui) {
            new Gui(scores);
        } else {
            new Guesser(scores);
        }
    }
}
