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
package gui;

import filter.GroupKMers;
import profile.ProfileLib;
import score.Score;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.DefaultStyledDocument;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;
import java.util.*;
import java.util.stream.Collectors;

/**
 * User interface class to test results of custom score and height cutoff values
 */
public class Gui extends Frame {

    private final int sliderwidth;
    private int height_cutoff = 20;
    private String filter = "^";
    private final List<Score> scores;
    private int basematch = 3;
    private double score_cutoff = 0.2;
    private final JTextPane seq;
    private final StyledDocument doc;
    private boolean colorall = true;
    private Map<String, List<String>> groupedKmers;
    private String hash;
    private final List<String> createdfiles = new ArrayList<>();

    public int getBasematch() {
        return basematch;
    }

    public double getScore_cutoff() {
        return score_cutoff;
    }

    // Constructor to setup GUI components and event handlers
    public Gui(List<Score> scores){

        this.scores = scores;

        JFrame f = new JFrame("NoPeak");
        f.setSize(900, 700);

        f.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent we) {

                createdfiles.forEach(f -> {
                    try {
                        Files.deleteIfExists(new File(f).toPath());
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                });


                groupedKmers.keySet().forEach(base -> {
                    System.out.println(base.toUpperCase());
                    System.out.println(groupedKmers.get(base).size());
                });


                groupedKmers.keySet().forEach(base -> {
                    java.logo.Logo logo = new java.logo.Logo(base, scores);

                    System.out.println("=====");
                    System.out.println("-----");
                    System.out.println(base);
                    System.out.println(logo);
                    System.out.println("-----");
                    groupedKmers.get(base).forEach(System.out::println);
                    System.out.println("=====");
                });


            }
        });


        final JPanel images = new JPanel();
        f.getContentPane().add(BorderLayout.EAST, images);


        JLabel info = new JLabel("Adjust the sliders for basematch, score and height cutoff to see the effects on the sequence logo:");
        f.getContentPane().add(BorderLayout.WEST, info);

        final JPanel buttons = new JPanel();
        buttons.setLayout(new BoxLayout(buttons, BoxLayout.Y_AXIS));
        f.getContentPane().add(BorderLayout.WEST, buttons);

        // sequences
        doc = new DefaultStyledDocument();
        seq = new JTextPane(doc);
        seq.setFont(new Font("Verdana", Font.PLAIN, 25));
        seq.setEditable(false);
        images.add(seq);


        //create necessary styles for various characters
        javax.swing.text.Style style = seq.addStyle("Red", null);
        StyleConstants.setForeground(style, Color.RED);
        javax.swing.text.Style style2 = seq.addStyle("Blue", null);
        StyleConstants.setForeground(style2, Color.BLUE);
        javax.swing.text.Style style3 = seq.addStyle("Green", null);
        StyleConstants.setForeground(style3, Color.GREEN);
        javax.swing.text.Style style4 = seq.addStyle("Yellow", null);
        StyleConstants.setForeground(style4, Color.ORANGE);


        // sequences

        // Slider

        JSlider height_slider = new JSlider(0, 100, height_cutoff);
        sliderwidth = (int) height_slider.getPreferredSize().getWidth();

        Hist height_hist = new Hist(scores.stream().map(Score::getHeight).collect(Collectors.toList()), 50, sliderwidth);

        double height_factor = (int) scores.stream().sorted(Comparator.comparing(Score::getHeight)).skip((long) scores.size() - 1).findAny().get().getHeight();

        height_slider.addChangeListener(e -> {

            height_cutoff = (int) ((((JSlider) e.getSource()).getValue()/100.0) * height_factor);

            if (!(((JSlider) e.getSource()).getValueIsAdjusting())) {
                update();
            }
        });

        JLabel height_slider_label = new JLabel("Height cutoff:");
        height_slider.setPaintTicks(true);
        height_slider.setPaintLabels(true);

        buttons.add(height_slider_label);
        buttons.add(height_hist);
        buttons.add(height_slider);

        // Slider
        // Slider

        JSlider cutoff_slider = new JSlider(0,100,10);

        java.util.Hashtable<Integer,JLabel> labelTable = new java.util.Hashtable<>();
        for(int i = 0; i < 10; i++){
            labelTable.put(i*10, new JLabel(Double.toString(((double)i)/10).replace("0", "")));
        }
        cutoff_slider.setLabelTable( labelTable );

        cutoff_slider.setMinorTickSpacing(5);
        cutoff_slider.setMajorTickSpacing(10);
        cutoff_slider.setPaintTicks(true);
        cutoff_slider.setPaintLabels(true);

        cutoff_slider.addChangeListener(e -> {
            score_cutoff = ((double)((JSlider) e.getSource()).getValue())/100;

            if (!(((JSlider) e.getSource()).getValueIsAdjusting()))
                update();
        });


        Hist cutoff_hist = new Hist(scores.stream().map(Score::getScore).collect(Collectors.toList()), 50, sliderwidth);

        JLabel cutoff_slider_label = new JLabel("Score cutoff:");
        buttons.add(cutoff_slider_label);
        buttons.add(cutoff_hist);
        buttons.add(cutoff_slider);

        // Slider
        // Slider

        JLabel basematch_slider_label = new JLabel("Basematch:");
        JSlider basematch_slider = new JSlider(1,8,basematch);
        basematch_slider.setMinorTickSpacing(1);
        basematch_slider.setMajorTickSpacing(2);
        basematch_slider.setPaintTicks(true);
        basematch_slider.setPaintLabels(true);

        basematch_slider.addChangeListener(e -> {
            basematch = ((JSlider) e.getSource()).getValue();

            if (!(((JSlider) e.getSource()).getValueIsAdjusting()))
                update();
        });

        buttons.add(basematch_slider_label);
        buttons.add(basematch_slider);


        // Input expected
        JLabel inputlabel = new JLabel("Filter kmers:");
        JTextField input = new JTextField();
        input.setMaximumSize(new Dimension(100,input.getPreferredSize().height));
        buttons.add(inputlabel);
        buttons.add(input);

        input.getDocument().addDocumentListener(new DocumentListener() {

            private void updatecol() {
                colorall = false;
                filter = input.getText();
                if (filter.length() == 0) {
                    filter = "^";
                    colorall = true;
                }
                update();
            }

            @Override
            public void insertUpdate(DocumentEvent e) {
                updatecol();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                updatecol();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                updatecol();
            }
        });

        //pwms
        JTextArea pwm = new JTextArea(10,100);
        pwm.setFont(new Font("Verdana", Font.PLAIN, 9));
        pwm.setEditable(false);
        f.getContentPane().add(BorderLayout.SOUTH, pwm);

        JLabel revcomp_label= new JLabel("Reverse complement:");
        JCheckBox reverse_complement = new JCheckBox();
        buttons.add(revcomp_label);
        buttons.add(reverse_complement);

        buttons.add(new Box.Filler(new Dimension(5,5),new Dimension(10,10),new Dimension(1000,1000)));


        JButton continue_btn = new JButton("Print");
        buttons.add(continue_btn);

        continue_btn.addActionListener(e -> {
            //f.dispose();

            Map<String, List<String>> groupedKmers = GroupKMers.groupKMers(scores, basematch, score_cutoff, height_cutoff);
            System.out.println("Basematch: " + basematch + " score cutoff: " + score_cutoff + " height cutoff: " + height_cutoff);

            for (String s : groupedKmers.keySet()) {
                java.logo.Logo logo = new java.logo.Logo(groupedKmers.get(s));

                if (!reverse_complement.isSelected())
                    logo.reverse_complement();

                //try {
                //    Process process = Runtime.getRuntime().exec("python3 plot_pwm.py " + Arrays.deepToString(logo.getPwm()).replace(" ", "") + " " + s);
                //    process.waitFor();
                //} catch (InterruptedException | IOException e1) {
                //    e1.printStackTrace();
                //}
            }

            //pwm.setText(pwmtext.toString());

            groupedKmers.keySet().forEach(base -> {
                try {
                    File pdfFile = new File("/tmp/nopeak_logo_" + base + ".pdf");
                    if (pdfFile.exists()) {

                        createdfiles.add("/tmp/nopeak_logo_" + base + ".pdf");

                        if (Desktop.isDesktopSupported()) {
                            Desktop.getDesktop().open(pdfFile);
                        } else {
                            System.out.println("Awt Desktop is not supported!");
                        }

                    } else {
                        System.out.println("File is not exists!");
                    }

                    System.out.println("Done");

                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            });
        });

        // Slider
        f.setVisible(true);
        update();
    }

    private void update(){

        if(!(score_cutoff +  ":" + basematch + ":" + height_cutoff).equals(hash)) {
            groupedKmers = GroupKMers.groupKMers(scores, basematch, score_cutoff, height_cutoff);
            hash = score_cutoff + ":" + basematch + ":" + height_cutoff;
        }

        StringBuilder alltext = new StringBuilder();

        for (String base : groupedKmers.keySet()) {
            alltext.append(ProfileLib.reverse_complement(base).toUpperCase().replace("N", "")).append("\t");
            alltext.append(base.toUpperCase().replace("N", ""));
            alltext.append("\t").append(groupedKmers.get(base).size()).append("\n");
        }

        seq.setText(alltext.toString());

        char[] tmp = new char[filter.length()];

        int pos = 0;
        char[] chars = alltext.toString().toCharArray();

        if(colorall){
            for(char l1: chars){
                if(l1 == 'A') doc.setCharacterAttributes(pos, 1, seq.getStyle("Red"), true);
                if(l1 == 'T') doc.setCharacterAttributes(pos, 1, seq.getStyle("Green"), true);
                if(l1 == 'G') doc.setCharacterAttributes(pos, 1, seq.getStyle("Yellow"), true);
                if(l1 == 'C') doc.setCharacterAttributes(pos, 1, seq.getStyle("Blue"), true);

                pos++;
            }

        } else for (char letter: chars){
            //shift
            if (tmp.length - 1 >= 0) System.arraycopy(tmp, 1, tmp, 0, tmp.length - 1);
            tmp[tmp.length-1] = letter;

            if(Arrays.equals(tmp, filter.toCharArray()) || Arrays.equals(tmp, new StringBuilder(filter).reverse().toString().toCharArray()) ){
                for(int i = pos - filter.length() + 1; i < pos +1; i++){
                    char l2 =  chars[i];

                    if(l2 == 'A') doc.setCharacterAttributes(i, 1, seq.getStyle("Red"), true);
                    if(l2 == 'T') doc.setCharacterAttributes(i, 1, seq.getStyle("Green"), true);
                    if(l2 == 'G') doc.setCharacterAttributes(i, 1, seq.getStyle("Yellow"), true);
                    if(l2 == 'C') doc.setCharacterAttributes(i, 1, seq.getStyle("Blue"), true);
                }
            }

            pos++;
        }
    }

    static class Hist extends JComponent{

        ArrayList<Integer> bars;
        private final int width;
        int height = 100;

        Hist(List<Double> values, int bins, int width) {
            bars = new ArrayList<>(Collections.nCopies(bins, 0));
            this.width = width;
            double binsize = (Collections.max(values)+0.00000001 - Collections.min(values))/bins;

            int bincounter = 0;
            for(double i = Collections.min(values); i < Collections.max(values); i+= binsize){
                for(Double val: values){
                    if (val >= i && val < i + binsize)
                        bars.set(bincounter, bars.get(bincounter) + 1);
                }
                bincounter += 1;
            }
        }

        public void paint(Graphics g) {
            int barwidth = width/bars.size();
            double barmax = Collections.max(bars);
            g.setColor(Color.ORANGE);

            for(int i = 0; i < bars.size(); i++){
                int barheight = (int) (height * (Math.log10(bars.get(i)) / Math.log10(barmax)));

                g.fillRect(i*barwidth,height - barheight, barwidth, barheight);
            }
        }
    }
}
