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
import com.cern.jet.random.tdouble.engine.DoubleRandomEngine;
import com.edu.emory.mathcs.restoretools.spectral.DoubleCommon2D;
import com.hijizhou.cores.deconvolution.MW_PURE_LET2Dnew;
import com.hijizhou.utilities.GridPanel;
import com.hijizhou.utilities.ImageUtil;
import com.hijizhou.utilities.PSFUtil;
import com.hijizhou.utilities.WalkBar;
import ij.IJ;
import ij.ImageJ;
import ij.ImagePlus;
import ij.gui.GUI;
import ij.process.ImageProcessor;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.ColorModel;
import java.text.DecimalFormat;

public class TimeTesting2 extends JDialog implements ChangeListener, ActionListener,
        ItemListener, WindowListener, TextListener, Runnable {
    private static final long serialVersionUID = 1L;
    private JButton bnDemoRun = new JButton("Start");
    private JButton bnDemoRun2 = new JButton("Start (512*512)");
    private String defaultMessage = "(c) 2018 CUHK";
    private WalkBar walk = new WalkBar(this.defaultMessage, true, false, true);

    public TimeTesting2() {

        super(new Frame(), "Time Testing");
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

        addWindowListener(this);

        GridPanel pnControls = new GridPanel();

        pnControls.place(0, 0, this.bnDemoRun);
//        pnControls.place(1, 0, this.bnDemoRun2);
        pnControls.place(2, 0, this.walk);


        this.bnDemoRun.addActionListener(this);
//        this.bnDemoRun2.addActionListener(this);
        this.walk.getButtonClose().addActionListener(this);

        add(pnControls);
        pack();
        GUI.center(this);
        setVisible(true);
        IJ.wait(0);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == this.bnDemoRun) {
                    // open the Clown sample

            ImagePlus impInput = IJ.openImage("http://www.ee.cuhk.edu.hk/~jzli/cameraman.tif");
            impInput.show();


            int cores = Runtime.getRuntime().availableProcessors();
            IJ.log("Available processors: " + cores);

            IJ.log("Set processors: 12");

            for(int i=1; i<2; i++) {
                IJ.log("===== No. " + i + "=====");
                test(impInput);
            }


        }

    }
    public void test(ImagePlus impInput){
        int width = impInput.getWidth();
        int height = impInput.getHeight();

        ImageProcessor ipInput = impInput.getProcessor();
        ColorModel cmY = ipInput.getColorModel();

        DoubleMatrix2D PSF = new DenseDoubleMatrix2D(width, height);

        DoubleMatrix2D Input = new DenseDoubleMatrix2D(width, height);
        DoubleCommon2D.assignPixelsToMatrix(Input, ipInput);

        double psfsigma = 0.01;
        PSF = PSFUtil.getGaussPSF(width, height, psfsigma);

        // Noise
        double alphaPoisson = 10;
        double[] noisePar = {alphaPoisson, 0}; // alphaPoisson, sigmaGauss

        MW_PURE_LET2Dnew sl;
        sl = new MW_PURE_LET2Dnew(Input, PSF, noisePar, this.walk, true);

//        MW_PURE_LET2D sl;
//        sl = new MW_PURE_LET2D(Input, PSF, noisePar, this.walk, true);

        double startTime = System.nanoTime(); // start timing
        sl.doDeconvolution();
        double runningTime = (System.nanoTime() - startTime) / 1.0E9D;

        DoubleMatrix2D Output = sl.getOutputMatrix();
        DecimalFormat df2 = new DecimalFormat("#,###,###,##0.000");

        ImagePlus impOutput = ImageUtil.matrix2Plus(Output, cmY, "Deconvolved image");
        impOutput.show();

        IJ.log("[Total] " + runningTime + " sec");

        this.walk.reset();
        this.walk.setMessage(this.defaultMessage);
    }

    public static void main(String[] args) {
        Class<?> clazz = TimeTesting2.class;
        String url = clazz.getResource("/" + clazz.getName().replace('.', '/') + ".class").toString();
        String pluginsDir = url.substring("file:".length(), url.length() - clazz.getName().length() - ".class".length());
        System.setProperty("plugins.dir", pluginsDir);

        // start ImageJ
        new ImageJ();

        ImagePlus impInput = IJ.openImage("http://www.ee.cuhk.edu.hk/~jzli/cameraman.tif");
        impInput.show();

        IJ.log("Image loaded.");

        int cores = Runtime.getRuntime().availableProcessors();
        IJ.log("Available processors: " + cores);

        // run the plugin
        IJ.runPlugIn(clazz.getName(), "");
    }

    public static double uniform(DoubleRandomEngine prng, double min, double max) {
        return prng.nextDouble() * (max - min) + min;
    }
    @Override
    public void itemStateChanged(ItemEvent e) {

    }

    @Override
    public void textValueChanged(TextEvent e) {

    }

    @Override
    public void windowOpened(WindowEvent e) {

    }

    @Override
    public void windowClosing(WindowEvent e) {

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

    }

    @Override
    public void windowDeactivated(WindowEvent e) {

    }

    @Override
    public void run() {

    }

    @Override
    public void stateChanged(ChangeEvent e) {

    }
}
