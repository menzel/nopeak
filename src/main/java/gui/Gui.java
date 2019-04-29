package gui;

import filter.GroupKMers;
import logo.LogoOld;
import profile.ProfileLib;
import score.Score;

import javax.swing.*;
import javax.swing.text.DefaultStyledDocument;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;
import java.awt.*;
import java.util.List;
import java.util.Map;

public class Gui extends Frame {

    private List<Score> scores;
    private int basematch = 3;
    private double score_cutoff = 0.1;
    private JTextPane seq;
    private StyledDocument doc;

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
        f.setSize(800, 500);

        final JPanel images = new JPanel();
        f.getContentPane().add(BorderLayout.EAST, images);

        final JPanel buttons = new JPanel();
        f.getContentPane().add(BorderLayout.WEST, buttons);

        // sequences
        doc = new DefaultStyledDocument();
        seq = new JTextPane(doc);
        seq.setFont(new Font("Verdana", Font.PLAIN, 25));
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

        buttons.add(cutoff_slider);

        // Slider
        // Slider

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

        buttons.add(basematch_slider);

        JButton continue_btn = new JButton("continue");
        buttons.add(continue_btn);



        continue_btn.addActionListener(e -> {

            f.dispose();

            System.out.println("Cluster kmers that overlap with at least x bases. Filter kmers with a score less than " + score_cutoff);
            Map<String, List<String>> groupedKmers = GroupKMers.groupKMers(scores, basematch, score_cutoff);

            groupedKmers.keySet().forEach(base -> {
                System.out.println(base.toUpperCase());
                System.out.println(groupedKmers.get(base).size());
            });

            System.out.println("Create sequence logos from clustered kmers:");

            groupedKmers.keySet().forEach(base -> {
                LogoOld logo = new LogoOld(base, scores);

                System.out.println("=====");
                System.out.println("file: ");
                System.out.println("-----");
                System.out.println(base);
                System.out.println(logo);
                System.out.println("-----");
                groupedKmers.get(base).stream().limit(500).forEach(System.out::println);
                System.out.println("=====");
            });
        });

        // Slider
        f.setVisible(true);
        update();
    }

    private void update(){
        Map<String, List<String>> groupedKmers = GroupKMers.groupKMers(scores, basematch, score_cutoff);

        StringBuilder alltext = new StringBuilder();

        for (String base : groupedKmers.keySet()) {
            alltext.append(ProfileLib.reverse_complement(base).toUpperCase().replace("N", "")).append("\t");
            alltext.append(base.toUpperCase().replace("N", "")).append("\n");
        }

        seq.setText(alltext.toString());

        int pos = 0;
        for (char letter: alltext.toString().toCharArray()){

            if(letter == 'A') doc.setCharacterAttributes(pos, 1, seq.getStyle("Red"), true);
            if(letter == 'T') doc.setCharacterAttributes(pos, 1, seq.getStyle("Green"), true);
            if(letter == 'G') doc.setCharacterAttributes(pos, 1, seq.getStyle("Yellow"), true);
            if(letter == 'C') doc.setCharacterAttributes(pos, 1, seq.getStyle("Blue"), true);

            pos++;
        }
    }
}
