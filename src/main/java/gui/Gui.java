package gui;

import filter.GroupKMers;
import logo.LogoOld;
import profile.ProfileLib;
import score.Score;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.DefaultStyledDocument;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;
import java.awt.*;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class Gui extends Frame {

    private String filter = "^";
    private List<Score> scores;
    private int basematch = 3;
    private double score_cutoff = 0.1;
    private JTextPane seq;
    private StyledDocument doc;
    private boolean colorall = true;
    private Map<String, List<String>> groupedKmers;
    private String hash;

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

        JLabel cutoff_slider_label = new JLabel("Score cutoff:");
        buttons.add(cutoff_slider_label);
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

        buttons.add(new Box.Filler(new Dimension(5,5),new Dimension(10,10),new Dimension(1000,1000)));


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

        if(!(score_cutoff +  ":" + basematch).equals(hash)) {
            groupedKmers = GroupKMers.groupKMers(scores, basematch, score_cutoff);
            hash = score_cutoff + ":" + basematch;
        }

        StringBuilder alltext = new StringBuilder();

        for (String base : groupedKmers.keySet()) {
            alltext.append(ProfileLib.reverse_complement(base).toUpperCase().replace("N", "")).append("\t");
            alltext.append(base.toUpperCase().replace("N", "")).append("\n");
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
}
