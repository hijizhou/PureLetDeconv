package com.hijizhou.imagej;

/**
 * @reference
 *       [1] J. Li, F. Luisier and T. Blu, PURE-LET image deconvolution,
 *             IEEE Trans. Image Process., vol. 27, no. 1, pp. 92-105, 2018.
 *       [2] J. Li, F. Luisier and T. Blu, Deconvolution of Poissonian images with the PURE-LET approach, 2016 23rd Proc. IEEE Int. Conf. on Image Processing (ICIP 2016), Phoenix, Arizona, USA, 2016, pp.2708-2712.
 *       [3] J. Li, F. Luisier and T. Blu, PURE-LET deconvolution of 3D fluorescence microscopy images, 2017 14th Proc. IEEE Int. Symp. Biomed. Imaging (ISBI 2017), Melbourne, Australia, 2017, pp. 723-727.
 *
 * @author	Jizhou Li
 *			The Chinese University of Hong Kong
 *
 */
import com.hijizhou.cores.ratiometric.MyRoiManager;
import com.hijizhou.cores.ratiometric.RatioImg;
import com.hijizhou.cores.ratiometric.SigmFit;
import com.hijizhou.utilities.GridPanel;
import com.hijizhou.utilities.WalkBar;
import ij.IJ;
import ij.ImageJ;
import ij.ImagePlus;
import ij.WindowManager;
import ij.gui.GUI;
import ij.gui.Overlay;
import ij.gui.Roi;
import ij.measure.CurveFitter;
import ij.plugin.frame.Fitter;
import ij.process.ImageProcessor;
import ij.process.ImageStatistics;
import ij.process.LUT;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.awt.event.*;
import java.util.Hashtable;

public class RatioMetric extends JDialog implements ChangeListener, ActionListener,
        WindowListener, TextListener, Runnable {

    private static final long serialVersionUID = 1L;
    SigmFit sf = new SigmFit();
    /*
    Main GUI
     */
    private Thread thread = null;
    private String defaultMessage = "(c) 2018 CUHK";
    private WalkBar walk = new WalkBar(this.defaultMessage, true, false, true);
    private JTabbedPane tab = new JTabbedPane();
    private GridBagLayout layout = new GridBagLayout();
    private GridBagConstraints constraint = new GridBagConstraints();
    /*
    Part - Calibration
     */
    private JButton btnCalAdd = new JButton("Add");
    private JButton btnCalDef = new JButton("Default");
    private JButton btnCalRun = new JButton("Run");
    private JSlider sldCalpH = new JSlider(0, 0, 100, 50);
    private JTextField txtCalpH = new JTextField("pH = 5.0", 3);
    private JTextArea taCalPH = new JTextArea(10, 10);
    private JTextArea taCalData = new JTextArea(10, 10);
    private JTextArea tfCalEqu = new JTextArea(3, 15);
    private JTextField txtCalR2 = new JTextField("", 15);
    private JComboBox choiceFun = new JComboBox();
    /*
    Part - Process
     */
    private int[] wList;
    private ImagePlus i1;
    private ImagePlus i2;
    private ImagePlus imgRatio;
    private ImagePlus imgPH;

    private JComboBox choiceProCh1 = new JComboBox();
    private JComboBox choiceProCh2 = new JComboBox();
    private JLabel lblProImg1 = new JLabel("Image 1");
    private JLabel lblProBg1 = new JLabel("Background:");
    private JLabel lblProClip1 = new JLabel("Clipping: ");
    private JTextField txtProBg1 = new JTextField("0", 5);
    private JButton btnProBg1 = new JButton("Get");
    private JTextField txtProClip1 = new JTextField("0", 5);
    private JButton btnProClip1 = new JButton("Get");

    private JLabel lblProImg2 = new JLabel("Image 2");
    private JLabel lblProBg2 = new JLabel("Background:");
    private JLabel lblProClip2 = new JLabel("Clipping: ");
    private JTextField txtProBg2 = new JTextField("0", 5);
    private JButton btnProBg2 = new JButton("Get");
    private JTextField txtProClip2 = new JTextField("0", 5);

    private JCheckBox cbProRatio = new JCheckBox("show ratio image?");
    private JButton btnProClip2 = new JButton("Get");
    private JButton btnProcUpdate = new JButton("Update");
    private JButton btnProcRun = new JButton("Run");

    private JTextField txtProMean = new JTextField("", 3);
    private JTextField txtProMin = new JTextField("", 3);
    private JTextField txtProMax = new JTextField("", 3);

    private JTextField txtProDisMin = new JTextField("5", 3);
    private JTextField txtProDisMax = new JTextField("9", 3);

    private JTextField txtProRatioMean = new JTextField("", 3);
    private JTextField txtProRatioMin = new JTextField("", 3);
    private JTextField txtProRatioMax = new JTextField("", 3);

    private JButton btnProAll = new JButton("Analysis");
    private JButton btnProROI = new JButton("Enlarge ROI");
    private JButton btnProPlot = new JButton("Plot");
    private JButton btnProRegion = new JButton("Region");

    public RatioMetric() {

        super(new Frame(), "RatioMetric Analysis");
        this.walk
                .fillAbout(
                        "RatioMetric Analysis",
                        "Version 13/04/2018",
                        "Ratio imaging and pH map",
                        "Department of Electronic Engineering<br/>The Chinese University of Hong Kong",
                        "Jizhou Li (hijizhou@gmail.com)",
                        "2018",
                        "<p style=\"text-align:left\"><b>References:</b><br>[1] Miesenböck, G., De Angelis, D. A., & Rothman, J. E. (1998). Visualizing secretion and synaptic transmission with pH-sensitive green fluorescent proteins. Nature, 394(6689), 192.<br><br><b>Acknowledgements:</b><br>Prof. Thierry Blu<br>Prof. Liwen Jiang<br>Dr. Jinbo Shen");


        /////////////////////////////////////////////////////
        /////////////////  Part - Calibration ///////////////
        /////////////////////////////////////////////////////

		/*
		pH-Intensity
		 */

        txtCalpH.setEditable(false);
        Hashtable<Integer, JLabel> labelCalTable = new Hashtable<>();
        labelCalTable.put(new Integer(100), new JLabel("10"));
        labelCalTable.put(new Integer(75), new JLabel("7.5"));
        labelCalTable.put(new Integer(50), new JLabel("5.0"));
        labelCalTable.put(new Integer(25), new JLabel("2.5"));
        labelCalTable.put(new Integer(0), new JLabel("0"));
        sldCalpH.setLabelTable(labelCalTable);
        sldCalpH.setPaintLabels(true);
        sldCalpH.setPaintTicks(true);
        sldCalpH.setMajorTickSpacing(5);
        sldCalpH.addChangeListener(this);

        JPanel btnCalButtons = new JPanel();
        btnCalButtons.setLayout(this.layout);
        addComponent(btnCalButtons, 0, 0, 1, 1, 4, btnCalAdd);
        addComponent(btnCalButtons, 1, 0, 1, 1, 4, btnCalDef);
        btnCalAdd.addActionListener(this);
        btnCalDef.addActionListener(this);

        add("Center", taCalPH);
        JScrollPane spCalPH = new JScrollPane(taCalPH, 22, 31);
        add("Center", taCalData);
        JScrollPane spCalData = new JScrollPane(taCalData, 22, 31);
        spCalData.getVerticalScrollBar().setModel(spCalPH.getVerticalScrollBar().getModel());
        spCalPH.getVerticalScrollBar().setModel(spCalData.getVerticalScrollBar().getModel());

        JPanel pnCalSetting = new JPanel();
        pnCalSetting.setLayout(this.layout);

        addComponent(pnCalSetting, 0, 0, 1, 1, 4, txtCalpH);
        addComponent(pnCalSetting, 0, 1, 2, 1, 4, sldCalpH);
        addComponent(pnCalSetting, 1, 0, 1, 1, 4, btnCalButtons);
        addComponent(pnCalSetting, 1, 1, 1, 1, 4, spCalPH);
        addComponent(pnCalSetting, 1, 2, 1, 1, 4, spCalData);
        pnCalSetting.setBorder(BorderFactory.createTitledBorder("pH-Intensity"));


		/*
		Fitting
		 */
        JScrollPane spCalEqu = new JScrollPane(tfCalEqu, 22, 30);
        spCalEqu.setSize(new Dimension(2, 5));
        tfCalEqu.setLineWrap(true);

        JPanel pnCalFitting = new JPanel();
        pnCalFitting.setLayout(this.layout);

        tfCalEqu.setEnabled(false);
        txtCalR2.setEnabled(false);

        this.choiceFun.addItem("Boltzmann Fun"); //
        this.choiceFun.addItem("Chapman-Richards"); //y = a*(1-exp(-b*x))^c
        this.choiceFun.addItem("Rodbard"); //y = d+(a-d)/(1+(x/c)^b)
        this.choiceFun.addItem("Straight Line");
        this.choiceFun.addItem("2nd Degree Polynomial");
        this.choiceFun.addItem("3rd Degree Polynomial");

        addComponent(pnCalFitting, 0, 0, 1, 1, 4, new JLabel("Function:"));
        addComponent(pnCalFitting, 0, 1, 1, 1, 4, choiceFun);

        //////////////// remove the equation display //////////////
//        addComponent(pnCalFitting, 1, 0, 1, 1, 4, new JLabel("Equation:"));
//        addComponent(pnCalFitting, 1, 1, 1, 1, 4, spCalEqu);
//        addComponent(pnCalFitting, 2, 0, 1, 1, 4, new JLabel("Goodness:"));
//        addComponent(pnCalFitting, 2, 1, 1, 1, 4, txtCalR2);
        addComponent(pnCalFitting, 3, 0, 2, 1, 4, btnCalRun);
        this.btnCalRun.addActionListener(this);
        pnCalFitting.setBorder(BorderFactory.createTitledBorder("Fitting"));

        JPanel tabCal = new JPanel();
        tabCal.setLayout(this.layout);
        addComponentFree(tabCal, 0, 0, 1, 1, 4, pnCalSetting);
        addComponentFree(tabCal, 1, 0, 1, 1, 4, pnCalFitting);


        /////////////////////////////////////////////////////
        /////////////////  Part - Process ///////////////
        /////////////////////////////////////////////////////

        this.choiceProCh1.addItem("Choose Image 1");
        this.choiceProCh2.addItem("Choose Image 2");
        this.choiceProCh1.setPrototypeDisplayValue("XXXXXXXXXXXX");
        this.choiceProCh2.setPrototypeDisplayValue("XXXXXXXXXXXX");


        JPanel plBg1 = new JPanel();
        plBg1.setLayout(this.layout);
        addComponentFree(plBg1, 0, 0, 1, 1, 0, this.txtProBg1);
        addComponentFree(plBg1, 0, 1, 1, 1, 0, this.btnProBg1);

        JPanel plBg2 = new JPanel();
        plBg2.setLayout(this.layout);
        addComponentFree(plBg2, 0, 0, 1, 1, 0, this.txtProBg2);
        addComponentFree(plBg2, 0, 1, 1, 1, 0, this.btnProBg2);

        JPanel plClip1 = new JPanel();
        plClip1.setLayout(this.layout);
        addComponentFree(plClip1, 0, 0, 1, 1, 0, this.txtProClip1);
        addComponentFree(plClip1, 0, 1, 1, 1, 0, this.btnProClip1);

        JPanel plClip2 = new JPanel();
        plClip2.setLayout(this.layout);
        addComponentFree(plClip2, 0, 0, 1, 1, 0, this.txtProClip2);
        addComponentFree(plClip2, 0, 1, 1, 1, 0, this.btnProClip2);

        JPanel plDisplay = new JPanel();
        addComponentFree(plDisplay, 0, 0, 1, 1, 0, new JLabel("Min"));
        addComponentFree(plDisplay, 0, 1, 1, 1, 0, this.txtProDisMin);
        addComponentFree(plDisplay, 0, 2, 1, 1, 0, new JLabel("Max"));
        addComponentFree(plDisplay, 0, 3, 1, 1, 0, this.txtProDisMax);
        addComponentFree(plDisplay, 1, 0, 1, 1, 0, this.cbProRatio);
        plDisplay.setBorder(BorderFactory.createTitledBorder("Display"));


        JPanel tabPro = new JPanel();
        tabPro.setLayout(this.layout);
        addComponentFree(tabPro, 0, 0, 1, 1, 0, this.lblProImg1);
        addComponentFree(tabPro, 0, 1, 1, 1, 0, this.choiceProCh1);
        addComponentFree(tabPro, 1, 0, 1, 1, 0, this.lblProBg1);
        addComponentFree(tabPro, 1, 1, 1, 1, 0, plBg1);
        addComponentFree(tabPro, 2, 0, 1, 1, 0, this.lblProClip1);
        addComponentFree(tabPro, 2, 1, 1, 1, 0, plClip1);

        addComponentFree(tabPro, 3, 0, 1, 1, 0, this.lblProImg2);
        addComponentFree(tabPro, 3, 1, 1, 1, 0, this.choiceProCh2);
        addComponentFree(tabPro, 4, 0, 1, 1, 0, this.lblProBg2);
        addComponentFree(tabPro, 4, 1, 1, 1, 0, plBg2);
        addComponentFree(tabPro, 5, 0, 1, 1, 0, this.lblProClip2);
        addComponentFree(tabPro, 5, 1, 1, 1, 0, plClip2);

        addComponentFree(tabPro, 6, 0, 4, 2, 0, plDisplay);
        addComponentFree(tabPro, 8, 0, 1, 1, 0, this.btnProcUpdate);
        addComponentFree(tabPro, 8, 1, 1, 1, 0, this.btnProcRun);
        this.btnProcUpdate.addActionListener(this);
        this.btnProcRun.addActionListener(this);
        this.btnProBg1.addActionListener(this);
        this.btnProClip1.addActionListener(this);
        this.btnProBg2.addActionListener(this);
        this.btnProClip2.addActionListener(this);


//        JPanel ProStatisLeft = new JPanel();
//        ProStatisLeft.setLayout(this.layout);
//
//        addComponent(ProStatisLeft, 0, 1, 1, 1, 4, new JLabel("Ratio"));
//        addComponent(ProStatisLeft, 0, 2, 1, 1, 4, new JLabel("pH"));
//
//        addComponent(ProStatisLeft, 1, 0, 1, 1, 4, new JLabel("Mean: "));
//        addComponent(ProStatisLeft, 1, 1, 1, 1, 4, txtProRatioMean);
//        addComponent(ProStatisLeft, 1, 2, 1, 1, 4, txtProMean);
//
//        addComponent(ProStatisLeft, 2, 0, 1, 1, 4, new JLabel("Min: "));
//        addComponent(ProStatisLeft, 2, 1, 1, 1, 4, txtProRatioMin);
//        addComponent(ProStatisLeft, 2, 2, 1, 1, 4, txtProMin);
//
//        addComponent(ProStatisLeft, 3, 0, 1, 1, 4, new JLabel("Max: "));
//        addComponent(ProStatisLeft, 3, 1, 1, 1, 4, txtProRatioMax);
//        addComponent(ProStatisLeft, 3, 2, 1, 1, 4, txtProMax);
//        this.txtProMean.setEditable(false);
//        this.txtProMin.setEditable(false);
//        this.txtProMax.setEditable(false);
//        this.txtProRatioMean.setEditable(false);
//        this.txtProRatioMin.setEditable(false);
//        this.txtProRatioMax.setEditable(false);

        JPanel ProStatisRight = new JPanel();
        ProStatisRight.setLayout(this.layout);
        addComponent(ProStatisRight, 0, 1, 1, 1, 4, this.btnProAll);
        addComponent(ProStatisRight, 0, 0, 1, 1, 4, this.btnProROI);
//        addComponent(ProStatisRight, 2, 0, 1, 1, 4, this.btnProPlot);
        this.btnProAll.addActionListener(this);
        this.btnProROI.addActionListener(this);
        this.btnProPlot.addActionListener(this);
        this.btnProRegion.addActionListener(this);

        JPanel ProStatis = new JPanel();
        ProStatis.setLayout(this.layout);
//        addComponent(ProStatis, 0, 0, 1, 1, 4, ProStatisLeft);
        addComponent(ProStatis, 0, 0, 1, 1, 4, ProStatisRight);
        ProStatis.setBorder(BorderFactory.createTitledBorder("Results"));

        addComponentFree(tabPro, 9, 0, 2, 1, 4, ProStatis);

		/*
		Main GUI
		 */

        this.tab.setBorder(BorderFactory.createEmptyBorder());
        this.tab.addTab("Calibration", tabCal);
        this.tab.addTab("Process", tabPro);
//		this.tab.addTab("Analysis", pn[2]);
        this.tab.addChangeListener(this);

        GridPanel pnMain = new GridPanel(false, 7);
        int row = 0;
        pnMain.place(row++, 0, this.tab);
        pnMain.place(row++, 0, this.walk);

        this.walk.getButtonClose().addActionListener(this);
        addWindowListener(this);

        add(pnMain);
        pack();
        GUI.center(this);

        updateChoices();

        setVisible(true);
        IJ.wait(0);
    }

    public static void main(String[] args) {

        Class<?> clazz = RatioMetric.class;

// start ImageJ
        new ImageJ();

        // open the Clown sample
        ImagePlus image1 = IJ
                .openImage("/Users/hijizhou/Dropbox/A Documents/Research/A Projects/C6 pHfit/AoEtools/Github/src/main/resources/Channel 1.tif");
        image1.show();
        ImagePlus image2 = IJ
                .openImage("/Users/hijizhou/Dropbox/A Documents/Research/A Projects/C6 pHfit/AoEtools/Github/src/main/resources/Channel 2.tif");
        image2.show();

        ImagePlus image3 = IJ
                .openImage("/Users/hijizhou/Dropbox/A Documents/Research/A Projects/C6 pHfit/AoEtools/Github/src/main/resources/mitosis-ch0.tif");
        image3.show();
        ImagePlus image4 = IJ
                .openImage("/Users/hijizhou/Dropbox/A Documents/Research/A Projects/C6 pHfit/AoEtools/Github/src/main/resources/mitosis-ch1.tif");
        image4.show();

        // run the plugin
        // System.out.println(clazz.getName());
        IJ.runPlugIn(clazz.getName(), "");
    }

    private void addComponentFree(JPanel pn, int row, int col, int width, int height, int space, JComponent comp) {
        this.constraint.gridx = col;
        this.constraint.gridy = row;
        this.constraint.gridwidth = width;
        this.constraint.gridheight = height;
        this.constraint.insets = new Insets(space, space, space, space);
        this.constraint.weightx = (IJ.isMacintosh() ? 90 : 100);
        this.layout.setConstraints(comp, this.constraint);
        pn.add(comp);
    }

    private void addComponent(JPanel pn, int row, int col, int width, int height, int space, JComponent comp) {
        this.constraint.gridx = col;
        this.constraint.gridy = row;
        this.constraint.gridwidth = width;
        this.constraint.gridheight = height;
        this.constraint.anchor = 18;
        this.constraint.insets = new Insets(space, space, space, space);
        this.constraint.weightx = (IJ.isMacintosh() ? 90 : 100);
        this.constraint.fill = 2;
        this.layout.setConstraints(comp, this.constraint);
        pn.add(comp);
    }

    public void run() {
    }

    public void textValueChanged(TextEvent e) {
        // TODO Auto-generated method stub

    }

    public void windowOpened(WindowEvent e) {
        // TODO Auto-generated method stub

    }

    public void windowClosing(WindowEvent e) {
        dispose();
    }

    public void windowClosed(WindowEvent e) {
        // TODO Auto-generated method stub

    }

    public void windowIconified(WindowEvent e) {
        // TODO Auto-generated method stub

    }

    public void windowDeiconified(WindowEvent e) {
        // TODO Auto-generated method stub

    }

    public void windowActivated(WindowEvent e) {
//		selectInputImage();
    }

    public void windowDeactivated(WindowEvent e) {
        // TODO Auto-generated method stub

    }

    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == this.walk.getButtonClose()) {
            dispose();
        }
        if (e.getSource() == this.btnCalRun) {
            IJ.showStatus("Fitting start.");
            getCalFitting(this.choiceFun.getSelectedIndex());
            return;
        }
        if (e.getSource() == this.btnCalAdd) {
            IJ.showStatus("Add calibration point.");
            setCursor(new Cursor(0));
            this.walk.reset();
            this.walk.setMessage(this.defaultMessage);
            this.thread = null;
            this.btnCalRun.setText("Run");
            getCalRoiPixel();
            return;
        }
        if (e.getSource() == this.btnProBg1) {
            getProBg1();
        }
        if (e.getSource() == this.btnProBg2) {
            getProBg2();
        }
        if (e.getSource() == this.btnProClip1) {
            getProClip1();
        }
        if (e.getSource() == this.btnProClip2) {
            getProClip2();
        }

        if (e.getSource() == this.btnProcUpdate) {
            updateChoices();
        }
        if (e.getSource() == this.btnProcRun) {
            getProRatioImage();
        }
        if (e.getSource() == this.btnProROI) {
            pHvalues("ROI");
        }
        if (e.getSource() == this.btnProAll) {
//            pHvalues("All");

            MyRoiManager rm = new MyRoiManager();
            rm.runCommand("show all with labels");

        }


        if (e.getSource() == this.btnCalDef) {
            this.taCalPH.setText("5\n5.5\n6\n6.5\n7\n7.5\n8\n8.5\n");
            this.taCalData.setText("0.2\n0.28\n0.4\n0.6\n0.8\n0.85\n1.0\n1.1\n");
        }
    }

    public void getProRatioImage() {

        int i1Index = this.choiceProCh1.getSelectedIndex() - 1;
        int i2Index = this.choiceProCh2.getSelectedIndex() - 1;

        i1 = WindowManager.getImage(wList[i1Index]);
        i2 = WindowManager.getImage(wList[i2Index]);

        double b1 = Double.parseDouble(this.txtProBg1.getText());
        double b2 = Double.parseDouble(this.txtProBg2.getText());
        double t1 = Double.parseDouble(this.txtProClip1.getText());
        double t2 = Double.parseDouble(this.txtProClip2.getText());

        imgRatio = RatioImg.get(b1, t1, b2, t2, i1, i2);

//        if (imgRatio.getStatistics().max > 1) {
//            IJ.showMessage("Ratio>1, may not stable (inverse order?)");
//            return;
//        }

        //statistics
        // display
        pHvalues("All");

    }

    public void pHvalues(String tag) {

        ImagePlus pHimg = new ImagePlus();

        ImageStatistics statPh = new ImageStatistics();
        ImageStatistics statRatio = new ImageStatistics();

        String location = "[Upper Right]";
        String fil = "Black";
        String label = "White";
        String number = "9";
        String decimal = "1";
        String font = "10";
        String zoom = "1.5";

//        ImageJ ij = IJ.getInstance();
//        java.net.URL url = ij.getClass().getResource("/pHMap.lut");
//        LUT lut = null;
//        if (url != null)
//        {
//            try
//            {
//                String lutpath = url.getPath();
//                lutpath = lutpath.replace("%20"," ");
//
//                lut = LutLoader.openLut(lutpath);
//            }
//            catch (Exception e) {
//                System.out.println("LUT file is not ready.");
//            }
//        }

        //creat desired LUT
        int[] R = {0, 198, 199, 200, 202, 203, 205, 206, 208, 209, 211, 212, 214, 215, 217, 218, 219, 221, 222, 224, 225, 227, 228, 230, 231, 233, 234, 236, 237, 238, 240, 241, 243, 244, 246, 247, 249, 250, 252, 253, 255, 255, 255, 252, 246, 240, 234, 228, 222, 216, 210, 204, 198, 192, 186, 180, 174, 168, 162, 156, 150, 144, 138, 132, 126, 120, 114, 108, 102, 96, 90, 84, 78, 72, 66, 60, 54, 48, 42, 36, 30, 24, 18, 12, 6, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 6, 12, 18, 24, 30, 36, 42, 48, 54, 60, 66, 72, 78, 84, 90, 96, 102, 108, 114, 120, 126, 132, 138, 144, 150, 156, 162, 168, 174, 180, 186, 192, 198, 204, 210, 216, 222, 228, 234, 240, 246, 252, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 0};
        int[] G = {0, 85, 82, 80, 78, 76, 74, 71, 69, 67, 65, 63, 61, 58, 56, 54, 52, 50, 47, 45, 43, 41, 39, 37, 34, 32, 30, 28, 26, 23, 21, 19, 17, 15, 13, 10, 8, 6, 4, 2, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 6, 12, 18, 24, 30, 36, 42, 48, 54, 60, 66, 72, 78, 84, 90, 96, 102, 108, 114, 120, 126, 132, 138, 144, 150, 156, 162, 168, 174, 180, 186, 192, 198, 204, 210, 216, 222, 228, 234, 240, 246, 252, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 252, 246, 240, 234, 228, 222, 216, 210, 204, 198, 192, 186, 180, 174, 168, 162, 156, 150, 144, 138, 132, 126, 120, 114, 108, 102, 96, 90, 84, 78, 72, 66, 60, 54, 48, 42, 36, 30, 24, 18, 12, 6, 0};
        int[] B = {0, 237, 236, 236, 236, 236, 236, 236, 236, 236, 236, 236, 236, 236, 236, 235, 235, 235, 235, 235, 235, 235, 235, 235, 235, 235, 235, 235, 234, 234, 234, 234, 234, 234, 234, 234, 234, 234, 234, 234, 240, 246, 252, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 252, 246, 240, 234, 228, 222, 216, 210, 204, 198, 192, 186, 180, 174, 168, 162, 156, 150, 144, 138, 132, 126, 120, 114, 108, 102, 96, 90, 84, 78, 72, 66, 60, 54, 48, 42, 36, 30, 24, 18, 12, 6, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
        byte[] reds = new byte[256];
        byte[] greens = new byte[256];
        byte[] blues = new byte[256];

        for (int i = 0; i < R.length; i++) {
            reds[i] = (byte) R[i];
            greens[i] = (byte) G[i];
            blues[i] = (byte) B[i];
        }
        LUT lut = new LUT(reds, greens, blues);


        double lowb = Double.parseDouble(this.txtProDisMin.getText());
        double upb = Double.parseDouble(this.txtProDisMax.getText());


        switch (tag) {
            case "All":
                imgPH = sf.evalInvFit(imgRatio);
                statPh = imgPH.getStatistics();
                statRatio = imgRatio.getStatistics();

                IJ.setThreshold(imgPH, lowb, upb);

                imgPH.setDisplayRange(lowb, upb);
                imgPH.getProcessor().invertLut();

                imgPH.setLut(lut);
                imgPH.show();

                IJ.run(imgPH, "Calibration Bar...", "location=" + location + " fill=" + fil + " label=" + label + " number=" + number + " decimal=" + decimal + " font=" + font + " zoom=" + zoom + " overlay");

                if (cbProRatio.isSelected()) {

                    imgRatio.setDisplayRange(0, 1);
                    imgRatio.getProcessor().invertLut();
                    imgRatio.setLut(lut);
                    imgRatio.show();

                    IJ.run(imgRatio, "Calibration Bar...", "location=" + location + " fill=" + fil + " label=" + label + " number=" + number + " decimal=" + decimal + " font=" + font + " zoom=" + zoom + " overlay");
                }

                break;

            case "ROI":
                ImagePlus imgPHcopy = new ImagePlus("pH map with ROI", imgPH.getProcessor());
                ImageProcessor roi = imgPH.getProcessor().crop();
                if (roi == null) {
                    IJ.showMessage("Please draw a ROI in the stack.");
                    return;
                }

                ImagePlus ipRoi = new ImagePlus("ROI", roi);
                statPh = ipRoi.getStatistics();

                IJ.setThreshold(ipRoi, lowb, upb);

                ImageProcessor scaled = ipRoi.getProcessor().resize(ipRoi.getProcessor().getWidth() * 5, ipRoi.getProcessor().getHeight() * 5);  //magnified image
                ImagePlus ipScaled = new ImagePlus("ROI of pH map", scaled);

                ipScaled.setDisplayRange(lowb, upb);
                ipScaled.getProcessor().invertLut();
                ipScaled.setLut(lut);
                ipScaled.show();

                imgPHcopy.setDisplayRange(lowb, upb);
                imgPHcopy.getProcessor().invertLut();
                imgPHcopy.setLut(lut);
                imgPHcopy.show();

                //Ratio image ROI
                Roi roiimg = imgPH.getRoi();
                roiimg.setStrokeColor(Color.white);

                Rectangle r = roiimg.getBounds();

                ImagePlus ipRatioRoi = imgRatio.duplicate();
                ipRatioRoi.setRoi(roiimg);

                statRatio = ipRatioRoi.getStatistics();
                if (this.cbProRatio.isSelected()) {
                    ipRatioRoi.setDisplayRange(0, 1);
                    ipRatioRoi.getProcessor().invertLut();
                    ipRatioRoi.setLut(lut);
                    ipRatioRoi.show();

                    IJ.run(ipRatioRoi, "Calibration Bar...", "location=" + location + " fill=" + fil + " label=" + label + " number=" + number + " decimal=" + decimal + " font=" + font + " zoom=" + zoom + " overlay");
                }

                IJ.run(imgPHcopy, "Calibration Bar...", "location=" + location + " fill=" + fil + " label=" + label + " number=" + number + " decimal=" + decimal + " font=" + font + " zoom=" + zoom + " overlay");


                Roi roi1 = new Roi(5, imgPHcopy.getProcessor().getHeight() - scaled.getHeight() - 5, ipScaled);
                roi1.setStrokeColor(Color.white);

                Overlay overlayList = imgPHcopy.getOverlay();
                if (overlayList == null) {
                    overlayList = new Overlay();
                }
                overlayList.add(roiimg);
                overlayList.add(roi1);
                imgPHcopy.setOverlay(overlayList);
                imgPHcopy.updateAndDraw();

                break;
        }

        //statistics
        this.txtProMean.setText((double) Math.round((statPh.mean) * 1000d) / 1000d + "");
        this.txtProMin.setText((double) Math.round((statPh.min) * 1000d) / 1000d + "");
        this.txtProMax.setText((double) Math.round((statPh.max) * 1000d) / 1000d + "");

        this.txtProRatioMean.setText((double) Math.round((statRatio.mean) * 1000d) / 1000d + "");
        this.txtProRatioMin.setText((double) Math.round((statRatio.min) * 1000d) / 1000d + "");
        this.txtProRatioMax.setText((double) Math.round((statRatio.max) * 1000d) / 1000d + "");
    }

    public void getCalFitting(int selectIndex) {
        //read data
        String txtPH = taCalPH.getText();
        String[] arrayOfLinesPH = txtPH.split("\n");
        String txtData = taCalData.getText();
        String[] arrayOfLinesData = txtData.split("\n");
        double[] aryPH = new double[arrayOfLinesPH.length];
        double[] aryData = new double[arrayOfLinesData.length];

        if (aryPH.length != aryData.length) {
            IJ.showMessage("The number of pH values should be the same as the ratio values.");
            return;
        }
        if (aryPH.length < 2) {
            IJ.showMessage("Please input the calibration values (at least 2 required)");
        }

        for (int i = 0; i < arrayOfLinesPH.length; i++) {
            aryPH[i] = Double.parseDouble(arrayOfLinesPH[i]);
            aryData[i] = Double.parseDouble(arrayOfLinesData[i]);
        }

        sf.setFitFun(selectIndex);
        sf.setA(-1);
        sf.setB(0);
        sf.setC(1);
        sf.setD(5);
        CurveFitter fit = sf.doFit(aryPH, aryData);

        Fitter.plot(fit);

        ////////////////////////// remove the equation display ////////////
//        String strEqu = "";
//        if (sf.getB() > 0 && sf.getD() > 0) {
//            strEqu = "y=" + sf.getB() + "+(" + sf.getC()
//                    + "-" + sf.getB() + ")/(1+exp((x-" + sf.getD() + ")/" + sf.getA() + "))";
//        } else if (sf.getB() > 0 && sf.getD() < 0) {
//            strEqu = "y=" + sf.getB() + "+(" + sf.getC()
//                    + "-" + sf.getB() + ")/(1+exp((x + " + Math.abs(sf.getD()) + ")/" + sf.getA() + "))";
//        } else if (sf.getB() < 0 && sf.getD() > 0) {
//            strEqu = "y=" + sf.getB() + "+(" + sf.getC()
//                    + "+" + Math.abs(sf.getB()) + ")/(1+exp((x-" + sf.getD() + ")/" + sf.getA() + "))";
//        } else {
//            strEqu = "y=" + sf.getB() + "+(" + sf.getC()
//                    + "+" + Math.abs(sf.getB()) + ")/(1+exp((x+" + Math.abs(sf.getD()) + ")/" + sf.getA() + "))";
//        }
//        tfCalEqu.setText(strEqu);
//        txtCalR2.setText("R^2 = " + String.valueOf((double) Math.round((sf.getGoodness()) * 1000d) / 1000d));

//        ImagePlus phImg = WindowManager.getCurrentImage();
//        Calibration cal = phImg.getCalibration();
//        cal.setFunction(22, sf.getParameters(), "Gray Value", true);
//        phImg.setGlobalCalibration(cal);

    }

    public void getCalRoiPixel() {
        ImagePlus imp = WindowManager.getCurrentImage();
        if (imp == null) {
            IJ.showMessage("Please open a stack of images");
            return;
        }
        if (imp.getStackSize() != 2) {
            IJ.showMessage("The input stack size should be equal to 2.");
            return;
        }
        Roi roi = imp.getRoi();
        if (roi == null) {
            IJ.showMessage("Please draw a ROI in the stack.");
            return;
        }
        Rectangle rect = roi.getBounds();
        float meanRoi = 0;
        int bCount = 0;
        for (int y = 0; y < rect.height; y++) {
            for (int x = 0; x < rect.width; x++) {
                double ch1 = imp.getStack().getProcessor(1).getPixel(rect.x + x, rect.y + y);
                double ch2 = imp.getStack().getProcessor(2).getPixel(rect.x + x, rect.y + y);
                if (ch2 == 0) {
                    continue;
                }
                meanRoi += ch1 / ch2;
                bCount++;
            }
        }
        if (bCount > 0) {
            meanRoi /= bCount;
        }

        taCalPH.append(this.sldCalpH.getValue() / 10.0 + "\n");
        taCalData.append((double) Math.round(meanRoi * 1000d) / 1000d + "\n");

        btnCalRun.setEnabled(true);
    }

    public void getProBg1() {

        int i1Index = this.choiceProCh1.getSelectedIndex() - 1;
        i1 = WindowManager.getImage(wList[i1Index]);

        double meanRoi = getMeanValue(i1);

        this.txtProBg1.setText(meanRoi + "");
    }

    public void getProBg2() {

        int i2Index = this.choiceProCh2.getSelectedIndex() - 1;
        i2 = WindowManager.getImage(wList[i2Index]);

        double meanRoi = getMeanValue(i2);

        this.txtProBg2.setText(meanRoi + "");
    }

    public void getProClip1() {

        int i1Index = this.choiceProCh1.getSelectedIndex() - 1;
        i1 = WindowManager.getImage(wList[i1Index]);

        double meanRoi = getMeanValue(i1);

        this.txtProClip1.setText(meanRoi + "");
    }

    public void getProClip2() {

        int i2Index = this.choiceProCh2.getSelectedIndex() - 1;
        i2 = WindowManager.getImage(wList[i2Index]);

        double meanRoi = getMeanValue(i2);

        this.txtProClip2.setText(meanRoi + "");
    }

    public double getMeanValue(ImagePlus imp) {
        if (imp == null) {
            IJ.showMessage("Please open a stack of images");
            return 0;
        }
        if (imp.getStackSize() != 1) {
            IJ.showMessage("Only single image is supported.");
            return 0;
        }
        Roi roi = imp.getRoi();
        if (roi == null) {
            IJ.showMessage("Please draw a ROI in the stack.");
            return 0;
        }
        Rectangle rect = roi.getBounds();
        double meanRoi = 0;
        int bCount = 0;
        for (int y = 0; y < rect.height; y++) {
            for (int x = 0; x < rect.width; x++) {
                double ch1 = imp.getStack().getProcessor(1).getPixel(rect.x + x, rect.y + y);
                meanRoi += ch1;
                bCount++;
            }
        }
        if (bCount > 0) {
            meanRoi /= bCount;
        }
        meanRoi = (double) Math.round((meanRoi) * 1000d) / 1000d;
        return meanRoi;
    }

    public void stateChanged(ChangeEvent e) {
        if (e.getSource() == this.sldCalpH) {
            if (this.sldCalpH.getValue() == 50) {
                this.txtCalpH.setText("pH = 5.0");
            } else {
                this.txtCalpH.setText("pH = " + this.sldCalpH.getValue() / 10.0);
            }
        }

    }

    public void updateChoices() {
        String[] titles;
        this.txtProBg1.setText("0");
        this.txtProBg2.setText("0");
        this.txtProClip1.setText("0");
        this.txtProClip2.setText("0");
        wList = WindowManager.getIDList();
        if (wList != null) {
            this.choiceProCh1.removeAllItems();
            this.choiceProCh2.removeAllItems();
            this.choiceProCh1.addItem("Choose Image 1");
            this.choiceProCh2.addItem("Choose Image 2");

            titles = new String[wList.length];
            for (int i = 0; i < wList.length; i++) {
                ImagePlus imp = WindowManager.getImage(wList[i]);
                if (imp != null)
                    titles[i] = imp.getTitle();
                else
                    titles[i] = "";
                this.choiceProCh1.addItem(titles[i]);
                this.choiceProCh2.addItem(titles[i]);
            }
        }
    }

}
