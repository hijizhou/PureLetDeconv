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
import com.cern.colt.matrix.tdouble.DoubleMatrix2D;
import com.cern.colt.matrix.tdouble.impl.DenseDoubleMatrix2D;
import com.hijizhou.cores.deconvolution.MW_PURE_LET2D;
import com.hijizhou.imageware.Builder;
import com.hijizhou.imageware.ImageWare;
import com.hijizhou.imageware.Operations;
import com.hijizhou.utilities.*;
import com.edu.emory.mathcs.restoretools.spectral.DoubleCommon2D;
import com.edu.emory.mathcs.utils.ConcurrencyUtils;
import ij.IJ;
import ij.ImageJ;
import ij.ImagePlus;
import ij.WindowManager;
import ij.gui.GUI;
import ij.process.ImageConverter;
import ij.process.ImageProcessor;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.ColorModel;
import java.text.DecimalFormat;
import java.util.Hashtable;

public class PureLetDeconv2D extends JDialog implements ChangeListener, ActionListener,
        ItemListener, WindowListener, TextListener, Runnable {
    private static final long serialVersionUID = 1L;
    JSlider sldDemoPSFsize = new JSlider(0, 0, 500, 300);
    JSlider sldDemoAlpha = new JSlider(0, 0, 1000, 10);
    JSlider sldDemoStd = new JSlider(0, 0, 1000, 0);
    JSlider sldRunPSFsize = new JSlider(0, 0, 1000, 100);
    JSlider sldRunAlpha = new JSlider(0, 0, 1000, 1000);
    JSlider sldRunStd = new JSlider(0, 0, 1000, 100);
    JSlider sldRunNA = new JSlider(0, 50, 200, 140);
    JSlider sldRunLambda = new JSlider(0, 250, 750, 605);
    JSlider sldRunPixelSize = new JSlider(0, 100, 2000, 100);
    private ColorModel cmY;
    private JTabbedPane tab = new JTabbedPane();
    private GridBagLayout layout = new GridBagLayout();
    private GridBagConstraints constraint = new GridBagConstraints();
    //Part-Demo
    private int width;
    private int height;
    private Thread thread = null;
    private boolean isrunning = false;
    private int alphaPoisson = 1;
    private int varianceG = 3;
    private String defaultMessage = "(c) 2018 CUHK";
    private WalkBar walk = new WalkBar(this.defaultMessage, true, false, true);
    private ImagePlus impOriginal = null;
    private ImageProcessor ipOriginal = null;
    private ImagePlus impInput = null;
    private ImageProcessor ipInput = null;
    private ImagePlus impPSF = null;
    private ImagePlus impOutput = null;
    private ImageProcessor ipOutput = null;
    private JTextField txtOriginal;
    private JTextField txtInput;
//    private JRadioButton checkShowImg = new JRadioButton("Show Images?", true);
//    private JRadioButton checkMSELET = new JRadioButton("MSE-LET?", false);
    private JTextField txtPSF;
    private ImageWare estInput;
    private JButton bnDemoRun = new JButton("Start Deconvolution");
    private JButton bnDemoSim = new JButton("Simulate");
    private JButton bnRunEstNoise = new JButton("Estimate");
    private JButton bnRunRun = new JButton("Start Deconvolution");
    private JComboBox cmbPSF = new JComboBox(new String[]{"Gaussian"});
    private JComboBox cmbPSFRun = new JComboBox(new String[]{"Confocal"});
    private ButtonGroup bgConvolution;
    private ButtonGroup bgResults;
    private JLabel lblDemoAlpha = new JLabel("<html>Level <br/>(alpha)</html>");
    private JLabel lblDemoStd = new JLabel("<html>Gaussian noise<br/>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;std.</html>");
    private JLabel lblDemoPSFsize = new JLabel("PSF Variance");
    private JLabel lblRunNA = new JLabel("<html>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;NA</html>");
    private JLabel lblRunLambda = new JLabel("<html>Wavelength <br/>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;(nm)</html>");
    private JLabel lblRunPixelSize = new JLabel("<html>Pixelsize <br/>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;(nm)</html>");
    private JLabel lblPSF = new JLabel("Choose PSF");
    private JTextField txtDemoAlpha = new JTextField("1.00", 3);

    GridPanel jplDemoAlpha = addSliderValue(sldDemoAlpha, txtDemoAlpha, 50);
    private JTextField txtDemoStd = new JTextField("0.00", 3);
    GridPanel jplDemoStd = addSliderValue(sldDemoStd, txtDemoStd, 50);
    private JTextField txtDemoPSFsize = new JTextField("3.00", 3);
    GridPanel jplDemoPSFsize = addSliderValue(sldDemoPSFsize, txtDemoPSFsize, 50);
    private JLabel lblInputPSNR = new JLabel("<html>Input PSNR (dB)</html>");
    private JLabel lblOutputPSNR = new JLabel("<html>Output PSNR (dB)</html>");
    private JLabel lblRunningTime = new JLabel("<html>Running Time (sec.)</html>");
    private JLabel lblSimulationTime = new JLabel("<html>Simulation Time (sec.)</html>");
    private JTextField txtInputPSNR = new JTextField("", 6);
    private JTextField txtOutputPSNR = new JTextField("", 6);
    private JTextField txtRunningTime = new JTextField("", 6);
    private JTextField txtSimuTime = new JTextField("", 6);
    //    private JRadioButton checkDemoShowPSF = new JRadioButton("Show PSF?", false);
    private JRadioButton checkDemoPostFilter = new JRadioButton("Post-Filtering?", true);
    //Part-Run
    private JLabel lblRunAlpha = new JLabel("<html>Level <br/>(alpha)</html>");
    private JLabel lblRunStd = new JLabel("<html>Gaussian noise<br/>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;std.</html>");
    private JLabel lblRunPSFsize = new JLabel("PSF Size");
    private JLabel lblRunPSF = new JLabel("Choose PSF");
    private JTextField txtRunAlpha = new JTextField("10.0", 3);
    GridPanel jplRunAlpha = addSliderValue(sldRunAlpha, txtRunAlpha, 50);
    private JTextField txtRunNA = new JTextField("1.4", 3);
    private JTextField txtRunLambda = new JTextField("605", 3);
    private JTextField txtRunPixelSize = new JTextField("100", 3);

    GridPanel jplRunNA = addSliderValue(sldRunNA, txtRunNA, 10);
    GridPanel jplRunLambda = addSliderValue(sldRunLambda, txtRunLambda, 50);
    GridPanel jplRunPixelSize = addSliderValue(sldRunPixelSize, txtRunPixelSize, 100);

    private JTextField txtRunStd = new JTextField("1.00", 3);
    GridPanel jplRunStd = addSliderValue(sldRunStd, txtRunStd, 50);
    private JTextField txtRunPSFsize = new JTextField("1", 3);
    GridPanel jplRunPSFsize = addSliderValue(sldRunPSFsize, txtRunPSFsize, 50);
    private JLabel lblRunRunningTime = new JLabel("<html>Running Time (sec.)</html>");
    private JTextField txtRunTime = new JTextField("", 6);
    //    private JRadioButton checkRunShowPSF = new JRadioButton("Show PSF?", false);
    private JRadioButton checkRunPostFilter = new JRadioButton("Post-Filtering?", true);

    private JRadioButton runCheckPhysical = new JRadioButton("Physical settings", false);
    private JRadioButton runCheckDefine = new JRadioButton("Manual", true);


    public PureLetDeconv2D() {
        super(new Frame(), "PURE-LET Image Deconvolution");
        this.walk
                .fillAbout(
                        "PURE-LET Image Deconvolution",
                        "Version 17/09/2018",
                        "PURE-LET Image Deconvolution",
                        "Department of Electronic Engineering<br/>The Chinese University of Hong Kong",
                        "Jizhou Li (hijizhou@gmail.com)",
                        "2018",
                        "<p style=\"text-align:left\"><b>References:</b><br>[1] J. Li, F. Luisier and T. Blu, PURE-LET image deconvolution, IEEE Trans. Image Process., vol. 27, no. 1, pp. 92-105, 2018.<br>[2] J. Li, F. Luisier and T. Blu, Deconvolution of Poissonian images with the PURE-LET approach, 2016 23rd Proc. IEEE Int. Conf. on Image Processing (ICIP 2016), Phoenix, Arizona, USA, 2016, pp.2708-2712.<br>" +
                                "[3] J. Li, F. Luisier and T. Blu, PURE-LET deconvolution of 3D fluorescence microscopy images, 2017 14th Proc. IEEE Int. Symp. Biomed. Imaging (ISBI 2017), Melbourne, Australia, 2017, pp. 723-727.<br><br><b>Acknowledgements:</b><br>Prof. Thierry Blu<br>Dr. Florian Luisier");
        this.txtOriginal = new JTextField("", 15);
        this.txtOriginal.setEditable(false);
        this.txtOriginal.setEnabled(false);


        GridPanel pnControls = new GridPanel();
        pnControls.place(0, 0, new JLabel("Original image"));
        pnControls.place(0, 1, this.txtOriginal);

        tab.addChangeListener(this);

        this.bgConvolution = new ButtonGroup();
        GridPanel pnConvolution = new GridPanel("Convolution & Noise");

        this.txtRunNA.addActionListener(this);
        this.txtRunLambda.addActionListener(this);
        this.txtRunPixelSize.addActionListener(this);

        this.txtRunPSFsize.addActionListener(this);
        this.txtRunAlpha.addActionListener(this);
        this.txtRunStd.addActionListener(this);

        this.txtDemoPSFsize.addActionListener(this);
        this.txtDemoAlpha.addActionListener(this);
        this.txtDemoStd.addActionListener(this);

        jplDemoPSFsize.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
        jplDemoAlpha.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
        jplDemoStd.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));

        pnConvolution.place(0, 0, this.lblPSF);
        pnConvolution.place(0, 1, this.cmbPSF);
        pnConvolution.place(1, 0, this.lblDemoPSFsize);
        pnConvolution.place(1, 1, jplDemoPSFsize);
        pnConvolution.place(2, 0, this.lblDemoAlpha);
        pnConvolution.place(2, 1, jplDemoAlpha);
//        pnConvolution.place(3, 0, this.lblDemoStd);
//        pnConvolution.place(3, 1, jplDemoStd);

        GridPanel pnResults = new GridPanel("Deconvolution");

        GridPanel gpDemoLeft = new GridPanel();
        gpDemoLeft.place(0, 0, this.lblInputPSNR);
        gpDemoLeft.place(0, 1, this.txtInputPSNR);
        gpDemoLeft.place(1, 0, this.lblOutputPSNR);
        gpDemoLeft.place(1, 1, this.txtOutputPSNR);
        gpDemoLeft.place(2, 0, this.lblSimulationTime);
        gpDemoLeft.place(2, 1, this.txtSimuTime);
        gpDemoLeft.place(3, 0, this.lblRunningTime);
        gpDemoLeft.place(3, 1, this.txtRunningTime);
        gpDemoLeft.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));

        GridPanel gpDemoRight = new GridPanel();
//        gpDemoRight.place(0, 0, this.checkDemoShowPSF);
        gpDemoRight.place(0, 0, this.checkDemoPostFilter);
        gpDemoRight.place(1, 0, this.bnDemoSim);
        gpDemoRight.place(2, 0, this.bnDemoRun);


        pnResults.place(0, 0, gpDemoLeft);
        pnResults.place(0, 1, gpDemoRight);

        GridPanel pnDemo = new GridPanel(false, 7);
        int row = 0;
        pnDemo.place(row++, 0, pnControls);
        pnDemo.place(row++, 0, pnConvolution);
        pnDemo.place(row++, 0, pnResults);

        this.bnDemoSim.setPreferredSize(new Dimension(50, 40));
        this.bnDemoRun.setPreferredSize(new Dimension(50, 40));
        this.bnDemoSim.addActionListener(this);
        this.bnDemoRun.addActionListener(this);
        this.bnDemoRun.setEnabled(false);
        this.bnDemoRun.setForeground(Color.gray);
        this.cmbPSF.addItemListener(this);
        addWindowListener(this);

        changeStatusDemo(false);
        this.txtSimuTime.setEditable(false);
        this.txtInputPSNR.setEditable(false);
        this.txtOutputPSNR.setEditable(false);
        this.txtRunningTime.setEditable(false);

        //// Part-Run ///////////////////////
        this.txtInput = new JTextField("", 15);
        this.txtInput.setEditable(false);
        this.txtInput.setEnabled(false);

        GridPanel pnControlsRun = new GridPanel();
        pnControlsRun.place(0, 0, new JLabel("Input image"));
        pnControlsRun.place(0, 1, this.txtInput);

        //change the label of sldRunNA
        java.util.Hashtable<Integer,JLabel> labelTableNA = new java.util.Hashtable<Integer,JLabel>();
        labelTableNA.put(new Integer(200), new JLabel("2.0"));
        labelTableNA.put(new Integer(150), new JLabel("1.5"));
        labelTableNA.put(new Integer(100), new JLabel("1.0"));
        labelTableNA.put(new Integer(50), new JLabel("0.5"));
        sldRunNA.setLabelTable( labelTableNA );
        sldRunNA.setPaintLabels(true);
        //change the label of sldRunLambda
        java.util.Hashtable<Integer,JLabel> labelTableLambda = new java.util.Hashtable<Integer,JLabel>();
        labelTableLambda.put(new Integer(750), new JLabel("750"));
        labelTableLambda.put(new Integer(500), new JLabel("500"));
        labelTableLambda.put(new Integer(250), new JLabel("250"));
        sldRunLambda.setLabelTable( labelTableLambda );
        sldRunLambda.setPaintLabels(true);
        //change the label of sldRunPixelSize
        java.util.Hashtable<Integer,JLabel> labelTablePixelSize = new java.util.Hashtable<Integer,JLabel>();
        labelTablePixelSize.put(new Integer(2000), new JLabel("2000"));
        labelTablePixelSize.put(new Integer(1000), new JLabel("1000"));
        labelTablePixelSize.put(new Integer(100), new JLabel("100"));
        sldRunPixelSize.setLabelTable( labelTablePixelSize );
        sldRunPixelSize.setPaintLabels(true);

        GridPanel pnRunConvolution = new GridPanel("Convolution");
        GridPanel pnRunConvolutionPhysical= new GridPanel("");
        GridPanel pnRunConvolutionManual = new GridPanel("");


        pnRunConvolutionPhysical.setForeground(Color.gray);
        pnRunConvolutionPhysical.place(0,1, this.runCheckPhysical);
        pnRunConvolutionPhysical.place(1, 0, this.lblRunPSF);
        pnRunConvolutionPhysical.place(1, 1, this.cmbPSFRun);
        pnRunConvolutionPhysical.place(2, 0, this.lblRunNA);
        pnRunConvolutionPhysical.place(2, 1, this.jplRunNA);
        pnRunConvolutionPhysical.place(3, 0, this.lblRunLambda);
        pnRunConvolutionPhysical.place(3, 1, this.jplRunLambda);
        pnRunConvolutionPhysical.place(4,0, this.lblRunPixelSize);
        pnRunConvolutionPhysical.place(4,1,this.jplRunPixelSize);
        changeStatusRunConvPhysical(false);


        pnRunConvolutionManual.place(5,1, this.runCheckDefine);
        pnRunConvolutionManual.place(6, 0, this.lblRunPSFsize);
        pnRunConvolutionManual.place(6, 1, jplRunPSFsize);

        pnRunConvolution.place(0,0, pnRunConvolutionPhysical);
        pnRunConvolution.place(1,0, pnRunConvolutionManual);

        this.runCheckPhysical.addActionListener(this);
        this.runCheckDefine.addActionListener(this);
        this.cmbPSFRun.addActionListener(this);

        GridPanel pnRunNoise = new GridPanel("Noise");
        pnRunNoise.place(0, 0, this.lblRunAlpha);
        pnRunNoise.place(0, 1, jplRunAlpha);
        pnRunNoise.place(0, 2, this.bnRunEstNoise);
//        pnRunNoise.place(1, 0, this.lblRunStd);
//        pnRunNoise.place(1, 1, jplRunStd);



        jplRunNA.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
        jplRunLambda.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
        jplRunPixelSize.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
        jplRunPSFsize.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
        jplRunAlpha.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
        jplRunStd.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));


        GridPanel pnRunResult = new GridPanel("Deconvolution");
        pnRunResult.place(0, 0, this.lblRunRunningTime);
        pnRunResult.place(0, 1, txtRunTime);
//        pnRunResult.place(0, 2, checkRunShowPSF);
        pnRunResult.place(0, 2, checkRunPostFilter);
        this.txtRunTime.setEnabled(false);

        GridPanel pnRunButtons = new GridPanel("");

        pnRunButtons.place(0, 1, bnRunRun);
        pnRunButtons.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));

        this.bnRunEstNoise.setPreferredSize(new Dimension(80, 40));
        this.bnRunEstNoise.addActionListener(this);
        this.bnRunRun.addActionListener(this);

        GridPanel pnRun = new GridPanel(false, 7);
        row = 0;
        pnRun.place(row++, 0, pnControlsRun);
        pnRun.place(row++, 0, pnRunConvolution);
        pnRun.place(row++, 0, pnRunNoise);
        pnRun.place(row++, 0, pnRunResult);
        pnRun.place(row++, 0, pnRunButtons);


        this.tab.setBorder(BorderFactory.createEmptyBorder());
        this.tab.addTab("Run", pnRun);
        this.tab.addTab("Demo", pnDemo);

        GridPanel pnMain = new GridPanel(false, 7);
        row = 0;
        pnMain.place(row++, 0, this.tab);
        pnMain.place(row++, 0, this.walk);

        this.walk.getButtonClose().addActionListener(this);

        add(pnMain);
        pack();
        GUI.center(this);
        setVisible(true);
        IJ.wait(0);
    }

    /**
     * Main method for debugging.
     * <p>
     * For debugging, it is convenient to have a method that starts ImageJ, loads
     * an image and calls the plugin, e.g. after setting breakpoints.
     *
     * @param args unused
     */
    public static void main(String[] args) {
        // set the plugins.dir property to make the plugin appear in the Plugins menu
        Class<?> clazz = PureLetDeconv2D.class;
        String url = clazz.getResource("/" + clazz.getName().replace('.', '/') + ".class").toString();
        String pluginsDir = url.substring("file:".length(), url.length() - clazz.getName().length() - ".class".length());
        System.setProperty("plugins.dir", pluginsDir);

        // start ImageJ
        new ImageJ();

        // open the Clown sample
        ImagePlus image = IJ.openImage("/Users/hijizhou/Documents/Research/A Projects/A01 PURE-LET Deconv/Sharing/PureLetDeconv/Java/SourceCodes/src/main/resources/fluocells.tif");
        image.show();

        // run the plugin
        IJ.runPlugIn(clazz.getName(), "");
    }

    public void changeStatusRunConvManual(boolean isready) {
        Color color;
        if (isready) {
            color = Color.black;
        } else {
            color = Color.gray;
        }

        this.sldRunPSFsize.setForeground(color); this.sldRunPSFsize.setEnabled(isready);
        this.lblRunPSFsize.setForeground(color); this.lblRunPSFsize.setEnabled(isready);
        this.txtRunPSFsize.setForeground(color); this.txtRunPSFsize.setEnabled(isready);

    }
    public void changeStatusRunConvPhysical(boolean isready){
        Color color;
        if (isready) {
            color = Color.black;
        } else {
            color = Color.gray;
        }

        this.lblRunNA.setEnabled(isready);
        this.sldRunNA.setForeground(color); this.sldRunNA.setEnabled(isready);
        this.txtRunNA.setForeground(color); this.txtRunNA.setEnabled(isready);
        this.lblRunPSF.setForeground(color);
        this.cmbPSFRun.setEnabled(isready); this.cmbPSFRun.setForeground(color);

        this.lblRunLambda.setForeground(color);
        this.sldRunLambda.setForeground(color); this.sldRunLambda.setEnabled(isready);
        this.txtRunLambda.setForeground(color); this.txtRunLambda.setEnabled(isready);
        this.lblRunPixelSize.setForeground(color);
        this.sldRunPixelSize.setForeground(color); this.sldRunPixelSize.setEnabled(isready);
        this.txtRunPixelSize.setForeground(color); this.txtRunPixelSize.setEnabled(isready);


    }

    public void changeStatusDemo(boolean isready) {
        Color color;
        if (isready) {
            color = Color.black;
        } else {
            color = Color.gray;
            this.bnDemoRun.setEnabled(false);
            this.bnDemoRun.setForeground(color);
        }

        this.jplDemoPSFsize.setEnabled(isready);
        this.jplDemoAlpha.setEnabled(isready);
        this.jplDemoStd.setEnabled(isready);
        this.bnDemoSim.setEnabled(isready);
        this.bnDemoRun.setEnabled(isready);
        this.cmbPSF.setEnabled(isready);
        this.cmbPSF.setForeground(color);
        this.txtDemoAlpha.setEditable(isready);
        this.txtDemoAlpha.setForeground(color);
        this.txtDemoStd.setEditable(isready);
        this.txtDemoStd.setForeground(color);
        this.txtInputPSNR.setForeground(color);
        this.txtDemoPSFsize.setEditable(isready);
        this.txtDemoPSFsize.setForeground(color);
        this.txtSimuTime.setForeground(color);
        this.txtOriginal.setEditable(isready);
        this.txtOriginal.setForeground(color);
        this.txtOutputPSNR.setForeground(color);
        this.txtRunningTime.setForeground(color);
        this.sldDemoPSFsize.setEnabled(isready);
        this.sldDemoPSFsize.setForeground(color);
        this.sldDemoAlpha.setEnabled(isready);
        this.sldDemoAlpha.setForeground(color);
        this.sldDemoStd.setEnabled(isready);
        this.sldDemoStd.setForeground(color);

    }

    public static double getPSFsize(double NA, double lambda, double pixelsize){
        //Reference: ﻿Zhang, Bo, Josiane Zerubia, and Jean-Christophe Olivo-Marin. “Gaussian Approximations of Fluorescence Microscope Point-Spread Function Models.” Applied Optics 46.10 (2007): 1819. Web.

        DecimalFormat df2 = new DecimalFormat("#,###,###,##0.00");

        double rv = 0.25*lambda/(NA*pixelsize);
        if(rv>100){
            IJ.showMessage("PSF size is unsupported");
            return 0;
        }
        return new Double(df2.format(rv));
    }

    public void changeStatusRun(boolean isready) {
        Color color;
        if (isready) {
            color = Color.black;
            this.bnRunRun.setEnabled(true);
            this.bnRunRun.setForeground(color);
        } else {
            color = Color.gray;
            this.bnRunRun.setEnabled(false);
            this.bnRunRun.setForeground(color);
        }

        this.jplRunPSFsize.setEnabled(isready);
        this.jplRunAlpha.setEnabled(isready);
        this.jplRunStd.setEnabled(isready);
        this.bnRunEstNoise.setEnabled(isready);
        this.bnRunRun.setEnabled(isready);
        this.txtRunAlpha.setEditable(isready);
        this.txtRunAlpha.setForeground(color);
        this.txtRunStd.setEditable(isready);
        this.txtRunStd.setForeground(color);
        this.txtRunPSFsize.setEditable(isready);
        this.txtRunPSFsize.setForeground(color);
        this.txtRunTime.setForeground(color);
        this.txtInput.setEditable(isready);
        this.txtInput.setForeground(color);
        this.sldRunPSFsize.setEnabled(isready);
        this.sldRunPSFsize.setForeground(color);
        this.sldRunAlpha.setEnabled(isready);
        this.sldRunAlpha.setForeground(color);
        this.sldRunStd.setEnabled(isready);
        this.sldRunStd.setForeground(color);
//        this.checkRunShowPSF.setEnabled(isready);
        this.checkRunPostFilter.setEnabled(isready);
        this.bnRunEstNoise.setEnabled(isready);
//        this.bnRunRun.setEnabled(isready);
    }

    public GridPanel addSliderValue(JSlider sld, JTextField txt, int space) {
        GridPanel jpl = new GridPanel();
        Hashtable<Integer, JLabel> labelTable = new Hashtable<Integer, JLabel>();
        labelTable.put(new Integer(1000), new JLabel("10"));
        labelTable.put(new Integer(750), new JLabel("7.5"));
        labelTable.put(new Integer(500), new JLabel("5.0"));
        labelTable.put(new Integer(250), new JLabel("2.5"));
        labelTable.put(new Integer(0), new JLabel("0"));
        sld.setLabelTable(labelTable);
        sld.setPaintLabels(true);
        sld.setPaintTicks(true);
        sld.setMajorTickSpacing(space);
        sld.addChangeListener(this);
        sld.setPreferredSize(new Dimension(150, 40));

        jpl.setLayout(this.layout);
        jpl.place(0, 0, sld);
        jpl.place(0, 1, txt);
        return jpl;
    }

    private void selectInputImage() {

        this.impOriginal = WindowManager.getCurrentImage();
        if (this.impOriginal == null) {
            this.txtOriginal.setText("Original image is missing");
            changeStatusDemo(false);
            return;
        }

        if (this.impOriginal.getStackSize() > 1) {
            IJ.showMessage("Only 2D grayscale image is supported.");
            this.thread = null;
            changeStatusDemo(false);
            return;
        }
        if (this.impOriginal.getChannel() > 1) {
            IJ.showMessage("Only 2D grayscale image is supported.");
            this.thread = null;
            changeStatusDemo(false);
            return;
        }


        this.width = impOriginal.getWidth();
        this.height = impOriginal.getHeight();


        if (this.width != this.height) {
            IJ.showMessage("Only square image is supported currently.");
            this.thread = null;
            changeStatusRun(false);
            return;
        }

        if (this.width % 2 != 0) {
            this.txtOriginal.setText("Only 2^n size image is supported currently.");
            changeStatusDemo(false);
            return;
        }

        if (this.impOriginal.isComposite()) {
            this.txtOriginal.setText("This plugin does not handle composite yet.");
            changeStatusDemo(false);
            return;
        }

        this.txtOriginal.setText(this.impOriginal.getTitle());
        this.txtOriginal.setCaretPosition(0);

        changeStatusDemo(true);
    }

    private void selectInputImageRun() {

        this.impInput = WindowManager.getCurrentImage();

        if (this.impInput == null) {
            this.txtInput.setText("Input image is missing");
            changeStatusRun(false);
            return;
        }

        this.estInput = Builder.create(this.impInput);

        if (this.impInput.getStackSize() > 1) {
            IJ.showMessage("Only 2D grayscale image is supported.");
            this.thread = null;
            changeStatusRun(false);
            return;
        }
        if (this.impInput.getChannel() > 1) {
            IJ.showMessage("Only 2D grayscale image is supported.");
            this.thread = null;
            changeStatusRun(false);
            return;
        }


        this.width = impInput.getWidth();
        this.height = impInput.getHeight();

        if (this.width != this.height) {
            IJ.showMessage("Only square image is supported currently.");
            this.thread = null;
            changeStatusRun(false);
            return;
        }

        if (this.width % 2 != 0) {
            this.txtOriginal.setText("Only 2^n size image is supported currently.");
            changeStatusRun(false);
            return;
        }

        if (this.impInput.isComposite()) {
            this.txtInput.setText("This plugin does not handle composite yet.");
            changeStatusRun(false);
            return;
        }

        this.txtInput.setText(this.impInput.getTitle());
        this.txtInput.setCaretPosition(0);

        changeStatusRun(true);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == this.walk.getButtonClose()) {
            dispose();
        }

        if (e.getSource() == this.bnRunEstNoise) {
            Thread t = new Thread(new Runnable() {
                public void run() {
                    noiseEstimation();
                }
            });
            t.start();
        }

        if (e.getSource() == this.bnDemoRun) {
            if (this.isrunning) {
                IJ.showStatus("Process aborted.");
                setCursor(new Cursor(0));
                this.walk.reset();
                this.walk.setMessage(this.defaultMessage);
                this.thread = null;
                this.bnDemoRun.setText("Progressing....");
                this.bnDemoRun.setEnabled(true);
                selectInputImage();
                return;
            } else {
                this.isrunning = true;
                this.walk.reset();
                this.walk.setMessage("In progress...");
                IJ.showStatus("Processing...");
                this.bnDemoRun.setText("Stop");
//                revalidate();

                Thread t = new Thread(new Runnable() {
                    public void run() {
                        runDemo();
                    }
                });
                t.start();


                return;
            }
        }

        if (e.getSource() == this.bnRunRun) {
            if (this.isrunning) {
                IJ.showStatus("Process aborted.");
                setCursor(new Cursor(0));
                this.walk.reset();
                this.walk.setMessage(this.defaultMessage);
                this.thread = null;
                this.bnRunRun.setText("Start Deconvolution");
                this.bnRunRun.setEnabled(true);
                selectInputImageRun();
                return;
            } else {
                this.isrunning = true;
//
                this.walk.setMessage("In progress...");
                IJ.showStatus("Processing...");
                this.bnRunRun.setText("Stop");
//                revalidate();

//                showMessageDialog(null, "This is even shorter");

                Thread t = new Thread(new Runnable() {
                    public void run() {
                        runRun();
                    }
                });
                t.start();
                return;
            }
        }

        if (e.getSource() == this.bnDemoSim) {
            doSimulation();
            return;
        }

        if (e.getSource() == this.txtDemoPSFsize) {
            this.sldDemoPSFsize.setValue((int) (Double.parseDouble(this.txtDemoPSFsize.getText()) * 100.0));
        }
        if (e.getSource() == this.txtDemoAlpha) {
            this.sldDemoAlpha.setValue((int) (Double.parseDouble(this.txtDemoAlpha.getText()) * 100.0));
        }
        if (e.getSource() == this.txtDemoStd) {
            this.sldDemoStd.setValue((int) (Double.parseDouble(this.txtDemoStd.getText()) * 100.0));
        }

        if (e.getSource() == this.txtRunPSFsize) {
            this.sldRunPSFsize.setValue((int) (Double.parseDouble(this.txtRunPSFsize.getText()) * 100.0));
        }

        if (e.getSource() == this.txtRunNA) {
            this.sldRunNA.setValue((int) (Double.parseDouble(this.txtRunNA.getText()) * 100.0));
            updatePSFsizze();
        }
        if (e.getSource() == this.txtRunLambda) {
            this.sldRunLambda.setValue((int) (Double.parseDouble(this.txtRunLambda.getText())));
            updatePSFsizze();
        }
        if (e.getSource() == this.txtRunPixelSize) {
            this.sldRunPixelSize.setValue((int) (Double.parseDouble(this.txtRunPixelSize.getText())));
            updatePSFsizze();
        }

        if (e.getSource() == this.txtRunAlpha) {
            this.sldRunAlpha.setValue((int) (Double.parseDouble(this.txtRunAlpha.getText()) * 100.0));
        }
        if (e.getSource() == this.txtRunStd) {
            this.sldRunStd.setValue((int) (Double.parseDouble(this.txtRunStd.getText()) * 100.0));
        }

        if (e.getSource() ==  this.runCheckPhysical){
            if(this.runCheckDefine.isSelected()){
                this.runCheckDefine.setSelected(false);
                this.runCheckPhysical.setSelected(true);
                changeStatusRunConvManual(false);
                changeStatusRunConvPhysical(true);
            }
        }
        if (e.getSource() ==  this.runCheckDefine){
            if(this.runCheckPhysical.isSelected()){
                this.runCheckDefine.setSelected(true);
                this.runCheckPhysical.setSelected(false);
                changeStatusRunConvManual(true);
                changeStatusRunConvPhysical(false);
            }
        }
    }

    public void noiseEstimation() {
        this.walk.setMessage("Starting noise estimation...");
        long startTime = System.nanoTime();
        double[] fitRs = Operations.estimateNoiseParams(estInput, 100);

        long endTime = System.nanoTime();

        double estTime = (endTime - startTime) / 1000000000.0;
        System.out.println("Noise estimation time: " + estTime);

        double alpha = fitRs[0] >= 0 ? fitRs[0] : 0.001;
        double variance = fitRs[2] * fitRs[2];

        DecimalFormat df2 = new DecimalFormat("#,###,###,##0.00");

        this.txtRunAlpha.setText(new Double(df2.format(alpha)) + "");
        this.txtRunStd.setText(new Double(df2.format(variance)) + "");

        this.sldRunAlpha.setValue((int) alpha * 100);
        this.sldRunStd.setValue((int) variance * 100);
        this.walk.setMessage("Finished [" + new Double(df2.format(estTime)) + "s], press Start Deconvolution");
    }

    public void doSimulation() {

        this.isrunning = true;
        ipOriginal = impOriginal.getProcessor();
        this.cmY = ipOriginal.getColorModel();

        new ImageConverter(this.impOriginal).convertToGray16();
        this.impOriginal.updateAndDraw();

        this.walk.reset();
        this.walk.setMessage("In progress...");
        IJ.showStatus("Processing...");

        this.width = this.impOriginal.getWidth();
        this.height = this.impOriginal.getHeight();

        if (this.width != this.height) {
            IJ.showMessage("Only squared image is supported.");
            this.walk.reset();
            IJ.showStatus("Finished");
            return;
        }

        DoubleMatrix2D PSF = new DenseDoubleMatrix2D(width, height);
        DoubleMatrix2D Original = new DenseDoubleMatrix2D(width, height);
        DoubleCommon2D.assignPixelsToMatrix(Original, ipOriginal);

        int strPSF = this.cmbPSF.getSelectedIndex();

        switch (strPSF) {
            case 0: //Gaussian
                PSF = PSFUtil.getGaussPSF(width, height, Math.sqrt(new Double(txtDemoPSFsize.getText()).doubleValue()));
                break;
            case 1: // Uniform:
                break;
            case 2: // SeparableFilter:
                break;
            default:
                PSF = PSFUtil.getGaussPSF(width, height, Math.sqrt(new Double(txtDemoPSFsize.getText()).doubleValue()));
                break;
        }

//        this.impPSF = ImageUtil.matrix2Plus(PSF, cmY, "PSF");
//        if (this.checkDemoShowPSF.isSelected()) {
//            this.impPSF.show();
//        }

        // Noise
        double alphaPoisson = Double.parseDouble(this.txtDemoAlpha.getText());
        double sigmaGauss = Double.parseDouble(this.txtDemoStd.getText());
        double[] noisePar = {alphaPoisson, sigmaGauss}; // alphaPoisson, sigmaGauss

        double startTime = System.nanoTime(); // start timing
        DoubleMatrix2D Input = Simulation.getInput(Original, PSF, noisePar);
        double simulationTime = (System.nanoTime() - startTime) / 1.0E9D;

        DecimalFormat df2 = new DecimalFormat("#,###,###,##0.000");
        // // input PSNR
        double inputPSNR = Evaluation.psnr(Input, Original);
        this.txtSimuTime.setText(df2.format(simulationTime));
        this.txtInputPSNR.setText(df2.format(inputPSNR));
        this.impInput = ImageUtil.matrix2Plus(Input, cmY, "Simulated blurred-noisy image (alpha: " + alphaPoisson + ", std: " + sigmaGauss + ")");
        this.impInput.show();
        this.ipInput = this.impInput.getProcessor();

        this.bnDemoRun.setEnabled(true);
        this.bnDemoRun.setText("Start Deconvolution");
        this.bnDemoRun.setForeground(Color.black);
        this.txtInputPSNR.setForeground(Color.black);
        this.txtSimuTime.setForeground(Color.black);
        this.txtOutputPSNR.setText("");
        this.txtRunningTime.setText("");
        this.walk.reset();
        IJ.showStatus("Finished simulation");
        this.bnDemoSim.setText("Simulation");
        this.isrunning = false;
    }

    @Override
    public void textValueChanged(TextEvent e) {

    }

    @Override
    public void windowOpened(WindowEvent e) {

    }

    @Override
    public void windowClosing(WindowEvent e) {
        dispose();
    }

    @Override
    public void windowClosed(WindowEvent e) {

    }

    @Override
    public void windowIconified(WindowEvent e) {

    }

    @Override
    public void windowDeiconified(WindowEvent e) {

    }

    @Override
    public void windowActivated(WindowEvent e) {

        if (this.tab.getSelectedIndex() == 0) {
            selectInputImageRun();

        } else {
            selectInputImage();
        }


    }

    @Override
    public void windowDeactivated(WindowEvent e) {
        if (this.tab.getSelectedIndex() == 0) {
            selectInputImageRun();

        } else {
            selectInputImage();
        }
    }

    @Override
    public void run() {

    }

    public void runDemo() {
        this.isrunning = true;
//        ConcurrencyUtils.setNumberOfThreads(1);
        this.walk.reset();
        this.walk.setMessage("In progress...");
        IJ.showStatus("Processing...");
        this.bnDemoRun.setText("Stop");

        this.width = this.impInput.getWidth();
        this.height = this.impInput.getHeight();

        DoubleMatrix2D PSF = new DenseDoubleMatrix2D(width, height);

        DoubleMatrix2D Input = new DenseDoubleMatrix2D(width, height);
        DoubleCommon2D.assignPixelsToMatrix(Input, ipInput);

        int strPSF = this.cmbPSF.getSelectedIndex();

        switch (strPSF) {
            case 0: //Gaussian
                PSF = PSFUtil.getGaussPSF(width, height, Math.sqrt(new Double(txtDemoPSFsize.getText()).doubleValue()));
                break;
            case 1: // Uniform:
                break;
            case 2: // SeparableFilter:
                break;
            default:
                PSF = PSFUtil.getGaussPSF(width, height, Math.sqrt(new Double(txtDemoPSFsize.getText()).doubleValue()));
                break;
        }

//        this.impPSF = ImageUtil.matrix2Plus(PSF, cmY, "PSF");
//        if (this.checkDemoShowPSF.isSelected()) {
//            this.impPSF.show();
//        }

        // Noise
        double alphaPoisson = Double.parseDouble(this.txtDemoAlpha.getText());
        double sigmaGauss = Double.parseDouble(this.txtDemoStd.getText());
        double[] noisePar = {alphaPoisson, sigmaGauss}; // alphaPoisson, sigmaGauss

        MW_PURE_LET2D sl;
        sl = new MW_PURE_LET2D(Input, PSF, noisePar, this.walk);

        double startTime = System.nanoTime(); // start timing
        sl.doDeconvolution();
        double runningTime = (System.nanoTime() - startTime) / 1.0E9D;

        DoubleMatrix2D Output = sl.getOutputMatrix();

        if (this.checkDemoPostFilter.isSelected()) {
            walk.setMessage("Starting post-filtering...");

            Output = ImageUtil.postfiltering(Output, 30 / alphaPoisson);

            walk.setMessage("Post-filtering finished");
        }

        DecimalFormat df2 = new DecimalFormat("#,###,###,##0.000");

        DoubleMatrix2D Original = new DenseDoubleMatrix2D(width, height);
        DoubleCommon2D.assignPixelsToMatrix(Original, ipOriginal);
        // // output PSNR
        double outputPSNR = Evaluation.psnr(Output, Original);
        this.txtRunningTime.setText(df2.format(runningTime));
        this.txtOutputPSNR.setText(df2.format(outputPSNR));
        this.impOutput = ImageUtil.matrix2Plus(Output, cmY, "Deconvolved image");
        this.impOutput.show();

        this.bnDemoRun.setEnabled(true);
        this.bnDemoRun.setText("Start Deconvolution");
        this.bnDemoRun.setForeground(Color.black);
        this.txtOutputPSNR.setForeground(Color.black);
        this.txtRunningTime.setForeground(Color.black);
        this.walk.reset();
        this.walk.setMessage(this.defaultMessage);
        IJ.showStatus("Deconvolution finished");
        this.isrunning = false;

    }

    public void runRun() {

//        ConcurrencyUtils.setNumberOfThreads(4);
        this.width = this.impInput.getWidth();
        this.height = this.impInput.getHeight();

        this.ipInput = this.impInput.getProcessor();
        this.cmY = ipInput.getColorModel();

        DoubleMatrix2D PSF = new DenseDoubleMatrix2D(width, height);

        DoubleMatrix2D Input = new DenseDoubleMatrix2D(width, height);
        DoubleCommon2D.assignPixelsToMatrix(Input, ipInput);

        int strPSF = this.cmbPSF.getSelectedIndex();

        double NA = Double.parseDouble(this.txtRunNA.getText());
        double lambda = Double.parseDouble(this.txtRunLambda.getText());
        double pixelsize = Double.parseDouble(this.txtRunPixelSize.getText());

        double psfsigma =1/2.355*Double.parseDouble(this.txtRunPSFsize.getText());

        System.out.println("Gaussian PSF size:" + psfsigma);
        switch (strPSF) {
            case 0: //Gaussian
                PSF = PSFUtil.getGaussPSF(width, height, psfsigma);
                break;
            case 1: // Uniform:
                break;
            case 2: // SeparableFilter:
                break;
            default:
                PSF = PSFUtil.getGaussPSF(width, height, psfsigma);
                break;
        }

//        this.impPSF = ImageUtil.matrix2Plus(PSF, cmY, "PSF");
//        if (this.checkRunShowPSF.isSelected()) {
//            this.impPSF.show();
//        }

        // Noise
        double alphaPoisson = Double.parseDouble(this.txtRunAlpha.getText());
        double sigmaGauss = Double.parseDouble(this.txtRunStd.getText());
//        sigmaGauss = 0;
        double[] noisePar = {alphaPoisson, sigmaGauss}; // alphaPoisson, sigmaGauss

        //rescale
//        Input = rescale(Input, 1, 255);

        MW_PURE_LET2D sl;
        sl = new MW_PURE_LET2D(Input, PSF, noisePar, this.walk);

        double startTime = System.nanoTime(); // start timing
        sl.doDeconvolution();
        double runningTime = (System.nanoTime() - startTime) / 1.0E9D;

        DoubleMatrix2D Output = sl.getOutputMatrix();
        DecimalFormat df2 = new DecimalFormat("#,###,###,##0.000");

        if (this.checkRunPostFilter.isSelected()) {
            //Post-filtering
            walk.setMessage("Starting post-filtering...");

            Output = ImageUtil.postfiltering(Output, 30 / alphaPoisson);


            walk.setMessage("Post-filtering finished");
        }

        //rescale
//        Output = rescale(Output, 0, 255);

        this.txtRunTime.setText(df2.format(runningTime));
        this.impOutput = ImageUtil.matrix2Plus(Output, cmY, "Deconvolved image");
        this.impOutput.show();

        this.bnRunRun.setEnabled(true);
        this.bnRunRun.setText("Start Deconvolution");
        this.bnRunRun.setForeground(Color.black);
        this.txtRunTime.setForeground(Color.black);
        this.walk.reset();
        this.walk.setMessage(this.defaultMessage);
        IJ.showStatus("Deconvolution finished");
        this.isrunning = false;

    }

    public void updatePSFsizze(){
                double na = Double.parseDouble(this.txtRunNA.getText());

                double lambda = Double.parseDouble(txtRunLambda.getText());
                double pixelsize = Double.parseDouble(txtRunPixelSize.getText());
                double psfsize = getPSFsize(na, lambda, pixelsize);

                txtRunPSFsize.setText(psfsize + "");
                sldRunPSFsize.setValue((int)psfsize * 100);
    }
    @Override
    public void stateChanged(ChangeEvent e) {

        if (e.getSource() == this.tab) {
            if (this.tab.getSelectedIndex() == 0) {
                selectInputImageRun();

            } else {
                selectInputImage();
            }
        }

        if (e.getSource() == this.sldDemoPSFsize) {
            double psfsize = this.sldDemoPSFsize.getValue();
            if (psfsize == 0) {
                psfsize = psfsize + 1;
            }
            this.txtDemoPSFsize.setText(psfsize / 100.00 + "");
        }
        if (e.getSource() == this.sldDemoAlpha) {
            double alpha = this.sldDemoAlpha.getValue();
            if (alpha == 0) {
                alpha = alpha + 1;
            }
            this.txtDemoAlpha.setText(alpha / 100.00 + "");
        }
        if (e.getSource() == this.sldDemoStd) {
            this.txtDemoStd.setText(this.sldDemoStd.getValue() / 100.00 + "");
        }

        if (e.getSource() == this.sldRunNA) {
            double na = this.sldRunNA.getValue();
            this.txtRunNA.setText(na / 100.00 + "");

            Thread t = new Thread(new Runnable() {
                public void run() {
                    double lambda = Double.parseDouble(txtRunLambda.getText());
                    double pixelsize = Double.parseDouble(txtRunPixelSize.getText());
                    double psfsize = getPSFsize(na/100, lambda, pixelsize);

                    txtRunPSFsize.setText(psfsize + "");
                    sldRunPSFsize.setValue((int)psfsize * 100);
                }
            });
            t.start();

        }
        if (e.getSource() == this.sldRunLambda) {
            int lamda = this.sldRunLambda.getValue();
            this.txtRunLambda.setText(lamda + "");

            Thread t = new Thread(new Runnable() {
                public void run() {
                    double na = Double.parseDouble(txtRunNA.getText());
                    double pixelsize = Double.parseDouble(txtRunPixelSize.getText());
                    double psfsize = getPSFsize(na, lamda+0.0d, pixelsize);

                    txtRunPSFsize.setText(psfsize + "");
                    sldRunPSFsize.setValue((int)psfsize * 100);
                }
            });
            t.start();
        }
        if (e.getSource() == this.sldRunPixelSize) {
            int pixelsize = this.sldRunPixelSize.getValue();
            this.txtRunPixelSize.setText(pixelsize + "");

            Thread t = new Thread(new Runnable() {
                public void run() {
                    double lambda = Double.parseDouble(txtRunLambda.getText());
                    double na = Double.parseDouble(txtRunNA.getText());
                    double psfsize = getPSFsize(na, lambda+0.0d, pixelsize);

                    txtRunPSFsize.setText(psfsize + "");
                    sldRunPSFsize.setValue((int)psfsize * 100);
                }
            });
            t.start();
        }
        if (e.getSource() == this.sldRunPSFsize) {
            double psfsize = this.sldRunPSFsize.getValue();
            if (psfsize == 0) {
                psfsize = psfsize + 1;
            }
            this.txtRunPSFsize.setText(psfsize / 100.00 + "");
        }
        if (e.getSource() == this.sldRunAlpha) {
            double alpha = this.sldRunAlpha.getValue();
            if (alpha == 0) {
                alpha = alpha + 1;
            }
            this.txtRunAlpha.setText(alpha / 100.00 + "");
        }
        if (e.getSource() == this.sldRunStd) {
            this.txtRunStd.setText(this.sldRunStd.getValue() / 100.00 + "");
        }

    }
//    public static void main(String[] args) {
//
//        new ImageJ();
//        ImagePlus impInput = IJ.openImage("/Users/hijizhou/Dropbox/A Documents/Research/A Projects/E2 PURE-LET Java/PureLetDeconv_Icy/ImageJ/src/main/resources/cameraman.tif");
//        DoubleMatrix2D original = new DenseDoubleMatrix2D(256, 256);
//        DoubleCommon2D.assignPixelsToMatrix(original, impInput.getProcessor());
//
//        int width = impInput.getWidth();
//        int height = impInput.getHeight();
//
//        DoubleMatrix2D PSF = PSFUtil.getGaussPSF(width, height, Math.sqrt(3));
//
//        // Noise
//        double alphaPoisson = 2;
//        double sigmaGauss = 0;
//        double[] noisePar = {alphaPoisson, sigmaGauss}; // alphaPoisson, sigmaGauss
//
//        DoubleMatrix2D Input = Simulation.getInput(original, PSF, noisePar);
//
//
//        MW_PURE_LET2D sl;
//        sl = new MW_PURE_LET2D(Input, PSF, noisePar);
//
//        double startTime = System.nanoTime(); // start timing
//        sl.doDeconvolution();
//        double runningTime = (System.nanoTime() - startTime) / 1.0E9D;
//
//        DoubleMatrix2D Output = sl.getOutputMatrix();
//
////        if(this.checkDemoPostFilter.isSelected()) {
////            //Post-filtering
////            DDIDstep ddid = new DDIDstep();
////
////            ddid.setR(5);
////            ddid.setSigma2(50/alphaPoisson);
////            Output = ddid.run(Output, Output);
//
////        ddid.setR(5);
////        ddid.setGamma_r(8.7f);
////        ddid.setGamma_f(0.4f);
////        ddid.setSigma2(50/alphaPoisson);
////        Output = ddid.run(Output, Output);
////        }
//
//        DecimalFormat df2 = new DecimalFormat("#,###,###,##0.000");
//        // // output PSNR
//        double outputPSNR = Evaluation.psnr(Output, original);
//
//        System.out.println("Running time: " + df2.format(runningTime));
//        System.out.println("PSNR: " + df2.format(outputPSNR));
//
//        ImagePlus imgOut = ImageUtil.matrix2Plus(Output, impInput.getProcessor().getColorModel(), "Output");
//        imgOut.show();
//    }

    @Override
    public void itemStateChanged(ItemEvent e) {
        if (this.cmbPSF.getSelectedIndex() == 0) {
            this.lblDemoPSFsize.setText("PSF Variance");
            this.txtDemoPSFsize.setEnabled(true);
            this.txtDemoPSFsize.setForeground(Color.black);
            this.lblDemoPSFsize.setForeground(Color.black);
        }
        if (this.cmbPSF.getSelectedIndex() == 1) {
            this.lblDemoPSFsize.setText("PSF Size");
            this.txtDemoPSFsize.setEnabled(true);
            this.txtDemoPSFsize.setForeground(Color.black);
            this.lblDemoPSFsize.setForeground(Color.black);
        }
        if (this.cmbPSF.getSelectedIndex() == 2) {
            this.lblDemoPSFsize.setForeground(Color.gray);
            this.txtDemoPSFsize.setEnabled(false);
            this.txtDemoPSFsize.setForeground(Color.gray);
        }

        if (this.cmbPSFRun.getSelectedIndex() == 0) {
            this.lblRunPSFsize.setText("PSF Size");
            this.txtRunPSFsize.setEnabled(true);
            this.txtRunPSFsize.setForeground(Color.black);
            this.lblRunPSFsize.setForeground(Color.black);
        }
        if (this.cmbPSFRun.getSelectedIndex() == 1) {
            this.lblRunPSFsize.setText("PSF Size");
            this.txtRunPSFsize.setEnabled(true);
            this.txtRunPSFsize.setForeground(Color.black);
            this.lblRunPSFsize.setForeground(Color.black);
        }
        if (this.cmbPSFRun.getSelectedIndex() == 2) {
            this.lblRunPSFsize.setForeground(Color.gray);
            this.txtRunPSFsize.setEnabled(false);
            this.txtRunPSFsize.setForeground(Color.gray);
        }

    }
}
