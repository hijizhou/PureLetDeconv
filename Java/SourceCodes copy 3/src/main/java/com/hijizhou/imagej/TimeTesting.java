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

import com.cern.colt.matrix.tdcomplex.DComplexMatrix2D;
import com.cern.colt.matrix.tdcomplex.impl.DenseDComplexMatrix2D;
import com.cern.colt.matrix.tdouble.DoubleMatrix2D;
import com.cern.colt.matrix.tdouble.impl.DenseDoubleMatrix2D;
import com.cern.jet.math.tdcomplex.DComplexFunctions;
import com.cern.jet.random.tdouble.engine.DoubleMersenneTwister;
import com.cern.jet.random.tdouble.engine.DoubleRandomEngine;
import com.edu.emory.mathcs.restoretools.spectral.DoubleCommon2D;
import com.hijizhou.cores.deconvolution.MW_PURE_LET2D;
import com.hijizhou.imageware.Builder;
import com.hijizhou.imageware.ImageWare;
import com.hijizhou.imageware.Operations;
import com.hijizhou.utilities.*;
import ij.IJ;
import ij.ImageJ;
import ij.ImagePlus;
import ij.WindowManager;
import ij.gui.GUI;
import ij.process.ImageConverter;
import ij.process.ImageProcessor;
import org.jblas.ComplexDoubleMatrix;
import org.jblas.DoubleMatrix;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.ColorModel;
import java.text.DecimalFormat;
import java.util.Hashtable;

public class TimeTesting extends JDialog implements ChangeListener, ActionListener,
        ItemListener, WindowListener, TextListener, Runnable {
    private static final long serialVersionUID = 1L;
    private JButton bnDemoRun = new JButton("Start");
    private String defaultMessage = "(c) 2018 CUHK";
    private WalkBar walk = new WalkBar(this.defaultMessage, true, false, true);

    public TimeTesting() {

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
        pnControls.place(1, 0, this.walk);


        this.bnDemoRun.addActionListener(this);
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
            Thread t = new Thread(new Runnable() {
                public void run() {
                    int m = 65536; int n = 64;
                    runtesting(m, n, 5);
                    m = 262144; n = 100;
                    runtesting(m, n, 3);
                }
            });
            t.start();
        }
    }

    public static void main(String[] args) {
        // set the plugins.dir property to make the plugin appear in the Plugins menu
        Class<?> clazz = TimeTesting.class;
        String url = clazz.getResource("/" + clazz.getName().replace('.', '/') + ".class").toString();
        String pluginsDir = url.substring("file:".length(), url.length() - clazz.getName().length() - ".class".length());
        System.setProperty("plugins.dir", pluginsDir);

        // start ImageJ
        new ImageJ();


        // run the plugin
        IJ.runPlugIn(clazz.getName(), "");
    }

    public void runtesting(int m, int n, int iter){

        IJ.log("------------------------------");
        IJ.log("Complex Matrix multiplication followed by addition");
        IJ.log("(C = At * A + B), realization num: "+ iter);
        IJ.log("size of At: " + n + "*" + m + ", size of C: " + n + "*" + n);
        IJ.log("------------------------------");


        double time1 = 0;
        double time2 = 0;

        for(int i=0; i<iter; i++) {
            //Colt
            DComplexMatrix2D A = randomMatrix(n, m, 0, 255);
            DComplexMatrix2D B = randomMatrix(n, n, 0, 255);
            DComplexMatrix2D C = new DenseDComplexMatrix2D(n, n);

            long startTime = System.nanoTime();
            A.zMult(A.copy().getConjugateTranspose(), C);
            C.assign(B, DComplexFunctions.plus);
            long endTime = System.nanoTime();

            double estTime = (endTime - startTime) / 1000000000.0;

            time2 = time2 + estTime/iter;


            IJ.log("[" + (i+1) + "] Colt time: " + estTime + " sec.");

            DoubleMatrix real = DoubleMatrix.rand(n, m);
            DoubleMatrix img = DoubleMatrix.rand(n, m);

            DoubleMatrix real2 = DoubleMatrix.rand(n, n);
            DoubleMatrix img2 = DoubleMatrix.rand(n, n);

            ComplexDoubleMatrix matrix = new ComplexDoubleMatrix(real, img);
            ComplexDoubleMatrix matrix2 = new ComplexDoubleMatrix(real2, img2);

             startTime = System.nanoTime();
            ComplexDoubleMatrix result = new ComplexDoubleMatrix(n, n);
            matrix.mmuli(matrix.transpose().conji(), result);
            result.add(matrix2);
             endTime = System.nanoTime();

             estTime = (endTime - startTime) / 1000000000.0;

            time1 = time1 + estTime/10;

            IJ.log("[" + (i+1) + "] JBLAS time: " + estTime + " sec.");

//            IJ.log("[" + i + "] Colt time: " + estTime2 + " sec.");
        }
        IJ.log("----------------------------");
        IJ.log("[Average] Colt time: " + time2 + " sec.");
        IJ.log("[Average] JBLAS time: " + time1 + " sec.");

    }

    public static DComplexMatrix2D randomMatrix(int rows, int cols, double min, double max) {
        DComplexMatrix2D M = new DenseDComplexMatrix2D(rows, cols);
        DoubleRandomEngine prng = new DoubleMersenneTwister(0);
        for (int r = 0; r < rows; ++r) {
            for (int c = 0; c < cols; ++c) {
                double val = uniform(prng, min, max);
                double val2 = uniform(prng, min, max);
                M.setQuick(r, c, val, val2);
            }
        }
        return M;
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
