/*
 *  Copyright (C) 2008-2009 Piotr Wendykier
 *  
 *  This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 3 of the License, or
 *  (at your option) any later version.
 *  
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *  
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.edu.emory.mathcs.restoretools.spectral;

import ij.IJ;
import ij.ImageJ;
import ij.ImageListener;
import ij.ImagePlus;
import ij.WindowManager;
import ij.plugin.PlugIn;
import ij.process.ColorProcessor;
import ij.process.ImageProcessor;

import java.awt.Component;
import java.awt.Container;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.PrintWriter;
import java.io.StringWriter;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import com.cern.colt.Arrays;
import com.cern.colt.Timer;
import com.edu.emory.mathcs.restoretools.Enums.OutputType;
import com.edu.emory.mathcs.restoretools.Enums.PrecisionType;
import com.edu.emory.mathcs.restoretools.spectral.SpectralEnums.DoubleStencil3DType;
import com.edu.emory.mathcs.restoretools.spectral.SpectralEnums.FloatStencil3DType;
import com.edu.emory.mathcs.restoretools.spectral.SpectralEnums.MethodType;
import com.edu.emory.mathcs.restoretools.spectral.SpectralEnums.ResizingType;
import com.edu.emory.mathcs.restoretools.spectral.gtik.DoublePeriodicGeneralizedTikhonov3D;
import com.edu.emory.mathcs.restoretools.spectral.gtik.DoubleReflexiveGeneralizedTikhonov3D;
import com.edu.emory.mathcs.restoretools.spectral.gtik.FloatPeriodicGeneralizedTikhonov3D;
import com.edu.emory.mathcs.restoretools.spectral.gtik.FloatReflexiveGeneralizedTikhonov3D;
import com.edu.emory.mathcs.restoretools.spectral.tik.DoublePeriodicTikhonov3D;
import com.edu.emory.mathcs.restoretools.spectral.tik.DoubleReflexiveTikhonov3D;
import com.edu.emory.mathcs.restoretools.spectral.tik.FloatPeriodicTikhonov3D;
import com.edu.emory.mathcs.restoretools.spectral.tik.FloatReflexiveTikhonov3D;
import com.edu.emory.mathcs.restoretools.spectral.tsvd.DoublePeriodicTruncatedSVD3D;
import com.edu.emory.mathcs.restoretools.spectral.tsvd.DoubleReflexiveTruncatedSVD3D;
import com.edu.emory.mathcs.restoretools.spectral.tsvd.FloatPeriodicTruncatedSVD3D;
import com.edu.emory.mathcs.restoretools.spectral.tsvd.FloatReflexiveTruncatedSVD3D;
import com.edu.emory.mathcs.restoretools.utils.DoubleSlider;
import com.edu.emory.mathcs.utils.ConcurrencyUtils;

/**
 * Parallel Spectral Deconvolution 3D GUI
 * 
 * @author Piotr Wendykier (piotr.wendykier@gmail.com)
 * 
 */

public class ParallelSpectralDeconvolution3D implements PlugIn, ImageListener {

    /**
     * Method used to call the plugin from a macro.
     * 
     * @param pathToBlurredImage
     *            path to a blurred image file
     * @param pathToPsf
     *            path to a PSF image file
     * @param pathToDeblurredImage
     *            path to a restored image file
     * @param methodStr
     *            type of an algorithm
     * @param stencilStr
     *            type of stencil for Generalized Tikhonov
     * @param resizingStr
     *            type of resizing
     * @param outputStr
     *            type of output image
     * @param precisionStr
     *            type of precision
     * @param thresholdStr
     *            threshold. Set thresholdStr=-1 to disable the thresholding.
     * @param regParamStr
     *            regularization parameter. If regParamStr==-1, then the value
     *            of the regularization parameter is computed automatically.
     * @param nOfThreadsStr
     *            maximal number of threads
     * @param showPaddedStr
     *            if true, then a padded image is displayed
     * @return path to deblurred image or error message
     */
    public static String deconvolve(String pathToBlurredImage, String pathToPsf, String pathToDeblurredImage, String methodStr, String stencilStr, String resizingStr, String outputStr, String precisionStr, String thresholdStr, String regParamStr, String nOfThreadsStr, String showPaddedStr) {
        boolean showPadded;
        double threshold, regParam;
        int nOfThreads;
        MethodType method = null;
        ResizingType resizing = null;
        OutputType output = null;
        String stencil = null;
        PrecisionType precision = null;
        ImagePlus imX = null;
        ImagePlus imB = IJ.openImage(pathToBlurredImage);
        if (imB == null) {
            return "Cannot open image " + pathToBlurredImage;
        }
        ImagePlus imPSF = IJ.openImage(pathToPsf);
        if (imPSF == null) {
            return "Cannot open image " + pathToPsf;
        }
        ImageProcessor ipB = imB.getProcessor();
        if (ipB instanceof ColorProcessor) {
            return "RGB images are not currently supported.";
        }
        if (imB.getStackSize() == 1) {
            return "For 2D images use Parallel Iterative Deconvolution 2D";
        }
        ImageProcessor ipPSF = imPSF.getProcessor();
        if (ipPSF instanceof ColorProcessor) {
            return "RGB images are not currently supported.";
        }
        if (imPSF.getStackSize() == 1) {
            return "For 2D images use Parallel Iterative Deconvolution 2D.";
        }
        for (MethodType elem : MethodType.values()) {
            if (elem.toString().equals(methodStr)) {
                method = elem;
                break;
            }
        }
        if (method == null) {
            return "method must be in " + Arrays.toString(MethodType.values());
        }
        for (ResizingType elem : ResizingType.values()) {
            if (elem.toString().equals(resizingStr)) {
                resizing = elem;
                break;
            }
        }
        if (resizing == null) {
            return "resizing must be in " + Arrays.toString(ResizingType.values());
        }
        for (OutputType elem : OutputType.values()) {
            if (elem.toString().equals(outputStr)) {
                output = elem;
                break;
            }
        }
        if (output == null) {
            return "output must be in " + Arrays.toString(OutputType.values());
        }
        for (DoubleStencil3DType elem : DoubleStencil3DType.values()) {
            if (elem.toString().equals(stencilStr)) {
                stencil = elem.toString();
                break;
            }
        }
        if (stencil == null) {
            return "stencil must be in " + Arrays.toString(DoubleStencil3DType.values());
        }
        for (PrecisionType elem : PrecisionType.values()) {
            if (elem.toString().equals(precisionStr)) {
                precision = elem;
                break;
            }
        }
        if (precision == null) {
            return "precision must be in " + Arrays.toString(PrecisionType.values());
        }
        try {
            threshold = Double.parseDouble(thresholdStr);
        } catch (Exception ex) {
            return "threshold must be a nonnegative number or -1 to disable";
        }
        if ((threshold != -1) && (threshold < 0)) {
            return "threshold must be a nonnegative number or -1 to disable";
        }
        try {
            regParam = Double.parseDouble(regParamStr);
        } catch (Exception ex) {
            return "regParam must be a nonnegative number or -1 for auto";
        }
        if ((regParam != -1) && (regParam < 0)) {
            return "regParam must be a nonnegative number or -1 for auto";
        }
        try {
            nOfThreads = Integer.parseInt(nOfThreadsStr);
        } catch (Exception ex) {
            return "nOfThreads must be power-of-two number";
        }
        if (nOfThreads < 1) {
            return "nOfThreads must be power-of-two number";
        }
        if (!ConcurrencyUtils.isPowerOf2(nOfThreads)) {
            return "nOfThreads must be power-of-two number";
        }
        try {
            showPadded = Boolean.parseBoolean(showPaddedStr);
        } catch (Exception ex) {
            return "showPadded must be a boolean value (true or false)";
        }
        ConcurrencyUtils.setNumberOfThreads(nOfThreads);
        switch (precision) {
        case DOUBLE:
            switch (method) {
            case GTIK_REFLEXIVE:
                DoubleReflexiveGeneralizedTikhonov3D dgtik_dct = new DoubleReflexiveGeneralizedTikhonov3D(imB, imPSF, DoubleStencil3DType.valueOf(stencil).stencil, resizing, output, showPadded, regParam, threshold);
                imX = dgtik_dct.deconvolve();
                break;
            case GTIK_PERIODIC:
                DoublePeriodicGeneralizedTikhonov3D dgtik_fft = new DoublePeriodicGeneralizedTikhonov3D(imB, imPSF, DoubleStencil3DType.valueOf(stencil).stencil, resizing, output, showPadded, regParam, threshold);
                imX = dgtik_fft.deconvolve();
                break;
            case TIK_REFLEXIVE:
                DoubleReflexiveTikhonov3D dtik_dct = new DoubleReflexiveTikhonov3D(imB, imPSF, resizing, output, showPadded, regParam, threshold);
                imX = dtik_dct.deconvolve();
                break;
            case TIK_PERIODIC:
                DoublePeriodicTikhonov3D dtik_fft = new DoublePeriodicTikhonov3D(imB, imPSF, resizing, output, showPadded, regParam, threshold);
                imX = dtik_fft.deconvolve();
                break;
            case TSVD_REFLEXIVE:
                DoubleReflexiveTruncatedSVD3D dtsvd_dct = new DoubleReflexiveTruncatedSVD3D(imB, imPSF, resizing, output, showPadded, regParam, threshold);
                imX = dtsvd_dct.deconvolve();
                break;
            case TSVD_PERIODIC:
                DoublePeriodicTruncatedSVD3D dtsvd_fft = new DoublePeriodicTruncatedSVD3D(imB, imPSF, resizing, output, showPadded, regParam, threshold);
                imX = dtsvd_fft.deconvolve();
                break;
            }
            break;
        case SINGLE:
            switch (method) {
            case GTIK_REFLEXIVE:
                FloatReflexiveGeneralizedTikhonov3D dgtik_dct = new FloatReflexiveGeneralizedTikhonov3D(imB, imPSF, FloatStencil3DType.valueOf(stencil).stencil, resizing, output, showPadded, (float) regParam, (float) threshold);
                imX = dgtik_dct.deconvolve();
                break;
            case GTIK_PERIODIC:
                FloatPeriodicGeneralizedTikhonov3D dgtik_fft = new FloatPeriodicGeneralizedTikhonov3D(imB, imPSF, FloatStencil3DType.valueOf(stencil).stencil, resizing, output, showPadded, (float) regParam, (float) threshold);
                imX = dgtik_fft.deconvolve();
                break;
            case TIK_REFLEXIVE:
                FloatReflexiveTikhonov3D dtik_dct = new FloatReflexiveTikhonov3D(imB, imPSF, resizing, output, showPadded, (float) regParam, (float) threshold);
                imX = dtik_dct.deconvolve();
                break;
            case TIK_PERIODIC:
                FloatPeriodicTikhonov3D dtik_fft = new FloatPeriodicTikhonov3D(imB, imPSF, resizing, output, showPadded, (float) regParam, (float) threshold);
                imX = dtik_fft.deconvolve();
                break;
            case TSVD_REFLEXIVE:
                FloatReflexiveTruncatedSVD3D dtsvd_dct = new FloatReflexiveTruncatedSVD3D(imB, imPSF, resizing, output, showPadded, (float) regParam, (float) threshold);
                imX = dtsvd_dct.deconvolve();
                break;
            case TSVD_PERIODIC:
                FloatPeriodicTruncatedSVD3D dtsvd_fft = new FloatPeriodicTruncatedSVD3D(imB, imPSF, resizing, output, showPadded, (float) regParam, (float) threshold);
                imX = dtsvd_fft.deconvolve();
                break;
            }
            break;
        }
        IJ.save(imX, pathToDeblurredImage);
        return pathToDeblurredImage;
    }

    private final static String version = "1.12";

    private final String[] methodNames = { "Generalized Tikhonov (reflexive)", "Generalized Tikhonov (periodic)", "Tikhonov (reflexive)", "Tikhonov (periodic)", "Truncated SVD (reflexive)", "Truncated SVD (periodic)" };

    private final String[] methodShortNames = { "gtik_ref", "gtik_per", "tik_ref", "tik_per", "tsvd_tef", "tsvd_per" };

    private final String[] precisionNames = { "Single", "Double" };

    private final String[] stencilNames = { "Identity", "First derivative (slices)", "Second derivative (slices)", "First derivative (rows)", "Second Derivative (rows)", "First derivative (columns)", "Second Derivative (columns)", "Laplacian" };

    private final String[] stencilShortNames = { "identity", "first_deriv_slices", "second_deriv_slices", "first_deriv_rows", "second_deriv_rows", "first_deriv_cols", "second_deriv_cols", "laplacian" };

    private static final String[] resizingNames = { "None", "Next power of two" };

    private static final String[] outputNames = { "Same as source", "Byte (8-bit)", "Short (16-bit)", "Float (32-bit)" };

    private JFrame mainPanel;

    private DoubleReflexiveGeneralizedTikhonov3D dgtik_dct;

    private DoublePeriodicGeneralizedTikhonov3D dgtik_fft;

    private DoublePeriodicTikhonov3D dtik_fft;

    private DoubleReflexiveTikhonov3D dtik_dct;

    private DoublePeriodicTruncatedSVD3D dtsvd_fft;

    private DoubleReflexiveTruncatedSVD3D dtsvd_dct;

    private FloatReflexiveGeneralizedTikhonov3D fgtik_dct;

    private FloatPeriodicGeneralizedTikhonov3D fgtik_fft;

    private FloatPeriodicTikhonov3D ftik_fft;

    private FloatReflexiveTikhonov3D ftik_dct;

    private FloatPeriodicTruncatedSVD3D ftsvd_fft;

    private FloatReflexiveTruncatedSVD3D ftsvd_dct;

    private ImagePlus imB, imPSF, imX;

    private int[] windowIDs;

    private String[] imageTitles;

    private boolean prevImageClosed = false;

    private String oldImageTitle = "";

    private String oldPSFTitle = "";

    private int oldMethodIndex = -1;

    private int oldResizingIndex = -1;

    private int oldOutputIndex = -1;

    private int oldStencilIndex = -1;

    private int oldPrecisionIndex = -1;

    private int oldNumberThreads = -1;

    private JComboBox blurChoice, psfChoice, methodChoice, resizingChoice, outputChoice, stencilChoice, precisionChoice;

    private DoubleSlider regSlider;

    private JTextField regField, threadsField, thresholdField;

    private JCheckBox regCheck, paddedCheck, thresholdCheck;

    private JButton deconvolveButton, updateButton, cancelButton;

    private ImageListener getImageListener() {
        return this;
    }

    public void run(String arg) {
        if (IJ.versionLessThan("1.35l")) {
            IJ.showMessage("This plugin requires ImageJ 1.35l+");
            return;
        }

        if (!IJ.isJava15()) {
            IJ.showMessage("This plugin requires Sun Java 1.5+");
            return;
        }
        WindowManager.checkForDuplicateName = true;
        ImagePlus.addImageListener(this);
        mainPanel = new MainPanel("Parallel Spectral Deconvolution 3D " + version + " ");
    }

    public void imageClosed(ImagePlus imp) {
        blurChoice.removeItem(imp.getTitle());
        blurChoice.revalidate();
        psfChoice.removeItem(imp.getTitle());
        psfChoice.revalidate();
        if ((imX != null) && (updateButton.isEnabled())) {
            if (imp.getTitle().equals(imX.getTitle())) {
                cleanOldData();
                prevImageClosed = true;
                updateButton.setEnabled(false);
            }
        }
    }

    public void imageOpened(ImagePlus imp) {
        blurChoice.addItem(imp.getTitle());
        blurChoice.revalidate();
        psfChoice.addItem(imp.getTitle());
        psfChoice.revalidate();

    }

    public void imageUpdated(ImagePlus imp) {
    }

    private void cleanOldData() {
        dgtik_dct = null;
        dgtik_fft = null;
        dtik_dct = null;
        dtik_fft = null;
        dtsvd_dct = null;
        dtsvd_fft = null;
        fgtik_dct = null;
        fgtik_fft = null;
        ftik_dct = null;
        ftik_fft = null;
        ftsvd_dct = null;
        ftsvd_fft = null;
        
    }

    private void cleanAll() {
        cleanOldData();
        imB = null;
        imPSF = null;
        imX = null;
        windowIDs = null;
        imageTitles = null;
        
    }

    private class DefaultExceptionHandler implements Thread.UncaughtExceptionHandler {

        public void uncaughtException(Thread t, Throwable e) {
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw, true);
            e.printStackTrace(pw);
            pw.flush();
            sw.flush();
            IJ.log(sw.toString());
        }

    }

    private class MainPanel extends JFrame {

        private static final long serialVersionUID = 3975356344081858245L;

        private final Cursor defaultCursor = new Cursor(Cursor.DEFAULT_CURSOR);

        private final Cursor waitCursor = new Cursor(Cursor.WAIT_CURSOR);

        private static final int width = 355;

        public MainPanel(String name) {
            super(name);
            windowIDs = WindowManager.getIDList();
            if (windowIDs != null) {
                imageTitles = new String[windowIDs.length];
                for (int i = 0; i < windowIDs.length; i++) {
                    ImagePlus im = WindowManager.getImage(windowIDs[i]);
                    if (im != null)
                        imageTitles[i] = im.getTitle();
                    else
                        imageTitles[i] = "";
                }
            }
            init();
        }

        private void init() {
            Thread.setDefaultUncaughtExceptionHandler(new DefaultExceptionHandler());
            Container pane = getContentPane();
            pane.setLayout(new BoxLayout(pane, BoxLayout.Y_AXIS));
            // --------------------------------------------------------------
            JPanel blurPanel = new JPanel();
            blurPanel.setLayout(new FlowLayout(FlowLayout.LEADING));
            JLabel blurLabel = new JLabel("Image:");
            blurLabel.setPreferredSize(new Dimension(60, blurLabel.getPreferredSize().height));
            blurPanel.add(blurLabel);
            if (windowIDs != null) {
                blurChoice = new JComboBox(imageTitles);
                blurChoice.setSelectedIndex(0);
            } else {
                blurChoice = new JComboBox();
            }
            blurChoice.setPreferredSize(new Dimension(width, blurChoice.getPreferredSize().height));
            blurChoice.setAlignmentX(Component.LEFT_ALIGNMENT);
            blurChoice.addActionListener(new BlurChoiceActionListener());
            blurPanel.add(blurChoice);
            // --------------------------------------------------------------
            JPanel psfPanel = new JPanel();
            psfPanel.setLayout(new FlowLayout(FlowLayout.LEADING));
            JLabel psfLabel = new JLabel("PSF:");
            psfLabel.setPreferredSize(new Dimension(60, psfLabel.getPreferredSize().height));
            psfPanel.add(psfLabel);
            if (windowIDs != null) {
                psfChoice = new JComboBox(imageTitles);
            } else {
                psfChoice = new JComboBox();
            }
            psfChoice.setPreferredSize(new Dimension(width, psfChoice.getPreferredSize().height));
            if (windowIDs != null) {
                if (windowIDs.length > 1) {
                    psfChoice.setSelectedIndex(1);
                } else {
                    psfChoice.setSelectedIndex(0);
                }
            }
            psfChoice.setAlignmentX(Component.LEFT_ALIGNMENT);
            psfChoice.addActionListener(new PsfChoiceActionListener());
            psfPanel.add(psfChoice);
            // --------------------------------------------------------------
            JPanel methodPanel = new JPanel();
            methodPanel.setLayout(new FlowLayout(FlowLayout.LEADING));
            JLabel methodLabel = new JLabel("Method:");
            methodLabel.setPreferredSize(new Dimension(60, methodLabel.getPreferredSize().height));
            methodPanel.add(methodLabel);
            methodChoice = new JComboBox(methodNames);
            methodChoice.setSelectedIndex(0);
            methodChoice.setAlignmentX(Component.LEFT_ALIGNMENT);
            methodChoice.addActionListener(new MethodChoiceActionListener());
            methodPanel.add(methodChoice);
            // --------------------------------------------------------------
            JPanel resizingPanel = new JPanel();
            resizingPanel.setLayout(new FlowLayout(FlowLayout.LEADING));
            JLabel resizingLabel = new JLabel("Resizing:");
            resizingLabel.setPreferredSize(new Dimension(60, resizingLabel.getPreferredSize().height));
            resizingPanel.add(resizingLabel);
            resizingChoice = new JComboBox(resizingNames);
            resizingChoice.setSelectedIndex(0);
            resizingChoice.setAlignmentX(Component.LEFT_ALIGNMENT);
            resizingChoice.setToolTipText("<html>Choose resizing.</html>");
            resizingChoice.addActionListener(new ResizingChoiceActionListener());
            resizingPanel.add(resizingChoice);
            // --------------------------------------------------------------
            JPanel outputPanel = new JPanel();
            outputPanel.setLayout(new FlowLayout(FlowLayout.LEADING));
            JLabel outputLabel = new JLabel("Output:");
            outputLabel.setPreferredSize(new Dimension(60, outputLabel.getPreferredSize().height));
            outputPanel.add(outputLabel);
            outputChoice = new JComboBox(outputNames);
            outputChoice.setSelectedIndex(0);
            outputChoice.setAlignmentX(Component.LEFT_ALIGNMENT);
            outputChoice.setToolTipText("<html>Choose format of deblurred image.</html>");
            outputChoice.addActionListener(new OutputChoiceActionListener());
            outputPanel.add(outputChoice);
            // --------------------------------------------------------------
            JPanel stencilPanel = new JPanel();
            stencilPanel.setLayout(new FlowLayout(FlowLayout.LEADING));
            JLabel stencilLabel = new JLabel("Stencil:");
            stencilLabel.setPreferredSize(new Dimension(60, stencilLabel.getPreferredSize().height));
            stencilPanel.add(stencilLabel);
            stencilChoice = new JComboBox(stencilNames);
            stencilChoice.setSelectedIndex(stencilNames.length - 1);
            stencilChoice.setAlignmentX(Component.LEFT_ALIGNMENT);
            stencilChoice.addActionListener(new StencilChoiceActionListener());
            stencilPanel.add(stencilChoice);
            // --------------------------------------------------------------
            JPanel precisionPanel = new JPanel();
            precisionPanel.setLayout(new FlowLayout(FlowLayout.LEADING));
            JLabel precisionLabel = new JLabel("Precision:");
            precisionLabel.setPreferredSize(new Dimension(60, precisionLabel.getPreferredSize().height));
            precisionPanel.add(precisionLabel);
            precisionChoice = new JComboBox(precisionNames);
            precisionChoice.setSelectedIndex(0);
            precisionChoice.setAlignmentX(Component.LEFT_ALIGNMENT);
            precisionChoice.addActionListener(new PrecisionChoiceActionListener());
            precisionPanel.add(precisionChoice);
            thresholdCheck = new JCheckBox("Threshold:  ");
            thresholdCheck.setSelected(true);
            thresholdCheck.addItemListener(new ThresholdCheckItemListener());
            precisionPanel.add(thresholdCheck);
            thresholdField = new JTextField("0.0", 5);
            thresholdField.setEnabled(true);
            thresholdField.addActionListener(new ThresholdFieldActionListener());
            precisionPanel.add(thresholdField);

            // --------------------------------------------------------------
            JPanel regPanel = new JPanel();
            regPanel.setLayout(new FlowLayout(FlowLayout.LEADING));
            JLabel regLabel = new JLabel("Regularization parameter:  ");
            regPanel.add(regLabel);
            regField = new JTextField("0.0", 5);
            regField.setEnabled(false);
            regField.addActionListener(new RegFieldActionListener());
            regPanel.add(regField);
            regSlider = new DoubleSlider(0, 0, 0, 1, 5);
            regSlider.setPaintLabels(false);
            regSlider.setEnabled(false);
            regSlider.addChangeListener(new RegSliderChangeListener());
            regPanel.add(regSlider);
            // --------------------------------------------------------------
            JPanel threadsPanel = new JPanel();
            threadsPanel.setLayout(new FlowLayout(FlowLayout.LEADING));
            JLabel threadsLabel = new JLabel("Max number of threads (power of 2):  ");
            threadsPanel.add(threadsLabel);
            ConcurrencyUtils.setNumberOfThreads(ConcurrencyUtils.getNumberOfThreads());
            threadsField = new JTextField(Integer.toString(ConcurrencyUtils.getNumberOfThreads()), 3);
            oldNumberThreads = ConcurrencyUtils.getNumberOfThreads();
            threadsField.addActionListener(new ThreadsFieldActionListener());
            threadsPanel.add(threadsField);
            // --------------------------------------------------------------

            JPanel checkPanel = new JPanel();
            checkPanel.setLayout(new FlowLayout(FlowLayout.LEADING));
            regCheck = new JCheckBox("Auto regularization parameter");
            regCheck.setSelected(true);
            regCheck.addItemListener(new RegCheckItemListener());
            checkPanel.add(regCheck);
            paddedCheck = new JCheckBox("Show padded image");
            paddedCheck.setSelected(false);
            checkPanel.add(paddedCheck);
            // --------------------------------------------------------------
            JPanel buttonPanel = new JPanel();
            buttonPanel.setLayout(new FlowLayout(FlowLayout.TRAILING));
            deconvolveButton = new JButton("Deconvolve");
            deconvolveButton.addActionListener(new DeconvolveButtonActionListener());
            if (windowIDs == null) {
                deconvolveButton.setEnabled(false);
            }
            buttonPanel.add(deconvolveButton);
            updateButton = new JButton("Update");
            updateButton.setEnabled(false);
            updateButton.addActionListener(new UpdateButtonActionListener());
            buttonPanel.add(updateButton);
            cancelButton = new JButton("Cancel");
            cancelButton.addActionListener(new CancelButtonActionListener());
            buttonPanel.add(cancelButton);
            // --------------------------------------------------------------
            pane.add(blurPanel);
            pane.add(psfPanel);
            pane.add(methodPanel);
            pane.add(stencilPanel);
            pane.add(resizingPanel);
            pane.add(outputPanel);
            pane.add(precisionPanel);
            pane.add(regPanel);
            pane.add(threadsPanel);
            pane.add(checkPanel);
            pane.add(buttonPanel);
            validate();
            setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
            setResizable(false);
            pack();
            setLocationRelativeTo(null);
            setVisible(true);
        }

        private class BlurChoiceActionListener implements ActionListener {
            public void actionPerformed(ActionEvent e) {
                windowIDs = WindowManager.getIDList();
                if (windowIDs != null) {
                    deconvolveButton.setEnabled(true);
                    enableUpdateButton();
                } else {
                    deconvolveButton.setEnabled(false);
                }
            }
        }

        private class PsfChoiceActionListener implements ActionListener {
            public void actionPerformed(ActionEvent e) {
                windowIDs = WindowManager.getIDList();
                if (windowIDs != null) {
                    enableUpdateButton();
                } else {
                    deconvolveButton.setEnabled(false);
                }
            }
        }

        private class MethodChoiceActionListener implements ActionListener {
            public void actionPerformed(ActionEvent e) {
                MethodType selMethod = MethodType.values()[methodChoice.getSelectedIndex()];
                switch (selMethod) {
                case GTIK_REFLEXIVE:
                    stencilChoice.setEnabled(true);
                    break;
                case GTIK_PERIODIC:
                    stencilChoice.setEnabled(true);
                    break;
                case TIK_REFLEXIVE:
                    stencilChoice.setEnabled(false);
                    break;
                case TIK_PERIODIC:
                    stencilChoice.setEnabled(false);
                    break;
                case TSVD_REFLEXIVE:
                    stencilChoice.setEnabled(false);
                    break;
                case TSVD_PERIODIC:
                    stencilChoice.setEnabled(false);
                    break;
                }
                enableUpdateButton();
            }
        }

        private class StencilChoiceActionListener implements ActionListener {
            public void actionPerformed(ActionEvent e) {
                enableUpdateButton();
            }
        }

        private class ResizingChoiceActionListener implements ActionListener {
            public void actionPerformed(ActionEvent e) {
                enableUpdateButton();
            }
        }

        private class OutputChoiceActionListener implements ActionListener {
            public void actionPerformed(ActionEvent e) {
                enableUpdateButton();
            }
        }

        private class PrecisionChoiceActionListener implements ActionListener {
            public void actionPerformed(ActionEvent e) {
                enableUpdateButton();
            }
        }

        private class RegFieldActionListener implements ActionListener {
            public void actionPerformed(ActionEvent e) {
                if (checkRegFieldText()) {
                    regSlider.setValue(Double.parseDouble(regField.getText()));
                }
            }
        }

        private class ThresholdFieldActionListener implements ActionListener {
            public void actionPerformed(ActionEvent e) {
                checkThresholdFieldText();
            }
        }

        private class RegSliderChangeListener implements ChangeListener {
            public void stateChanged(ChangeEvent e) {
                double val = regSlider.getDoubleValue();
                regField.setText(Double.toString(val));
            }
        }

        private class ThreadsFieldActionListener implements ActionListener {
            public void actionPerformed(ActionEvent e) {
                if (checkThreadsFieldText()) {
                    int val = Integer.parseInt(threadsField.getText());
                    ConcurrencyUtils.setNumberOfThreads(val);
                }
            }
        }

        private class ThresholdCheckItemListener implements ItemListener {
            public void itemStateChanged(ItemEvent e) {
                if (thresholdCheck.isSelected()) {
                    thresholdField.setEnabled(true);
                } else {
                    thresholdField.setEnabled(false);
                }
            }
        }

        private class RegCheckItemListener implements ItemListener {
            public void itemStateChanged(ItemEvent e) {
                if (regCheck.isSelected()) {
                    regField.setEnabled(false);
                    regSlider.setEnabled(false);
                    updateButton.setEnabled(false);

                } else {
                    regField.setEnabled(true);
                    regSlider.setEnabled(true);
                    enableUpdateButton();
                }
            }
        }

        private class DeconvolveButtonActionListener implements ActionListener {
            private final Timer timer = new Timer();

            public void actionPerformed(ActionEvent e) {
                Thread thread = new Thread(new Runnable() {
                    public void run() {
                        imB = WindowManager.getImage((String) blurChoice.getSelectedItem());
                        if (imB == null) {
                            IJ.error("Image " + (String) blurChoice.getSelectedItem() + " was renamed. Please choose the blurred image again.");
                            windowIDs = WindowManager.getIDList();
                            if (windowIDs != null) {
                                imageTitles = new String[windowIDs.length];
                                for (int i = 0; i < windowIDs.length; i++) {
                                    ImagePlus im = WindowManager.getImage(windowIDs[i]);
                                    if (im != null)
                                        imageTitles[i] = im.getTitle();
                                    else
                                        imageTitles[i] = "";
                                }
                            }
                            blurChoice.removeAllItems();
                            for (int i = 0; i < imageTitles.length; i++) {
                                blurChoice.addItem(imageTitles[i]);
                            }
                            blurChoice.revalidate();
                            return;
                        }
                        ImageProcessor ipB = imB.getProcessor();
                        if (ipB instanceof ColorProcessor) {
                            IJ.showMessage("RGB images are not currently supported.");
                            return;
                        }
                        if (imB.getStackSize() == 1) {
                            IJ.showMessage("For 2D images use Parallel Spectral Deconvolve 2D.");
                            return;
                        }
                        imPSF = WindowManager.getImage((String) psfChoice.getSelectedItem());
                        if (imPSF == null) {
                            IJ.error("Image " + (String) psfChoice.getSelectedItem() + " was renamed. Please choose the blurred image again.");
                            windowIDs = WindowManager.getIDList();
                            if (windowIDs != null) {
                                imageTitles = new String[windowIDs.length];
                                for (int i = 0; i < windowIDs.length; i++) {
                                    ImagePlus im = WindowManager.getImage(windowIDs[i]);
                                    if (im != null)
                                        imageTitles[i] = im.getTitle();
                                    else
                                        imageTitles[i] = "";
                                }
                            }
                            psfChoice.removeAllItems();
                            for (int i = 0; i < imageTitles.length; i++) {
                                psfChoice.addItem(imageTitles[i]);
                            }
                            psfChoice.revalidate();
                            return;
                        }
                        ImageProcessor ipPSF = imPSF.getProcessor();
                        if (ipPSF instanceof ColorProcessor) {
                            IJ.showMessage("RGB images are not currently supported.");
                            return;
                        }
                        if (imPSF.getStackSize() == 1) {
                            IJ.showMessage("For 2D images use Parallel Spectral Deconvolve 2D.");
                            return;
                        }
                        if (!checkRegFieldText())
                            return;
                        if (!checkThresholdFieldText())
                            return;
                        if (!checkThreadsFieldText())
                            return;

                        oldNumberThreads = Integer.parseInt(threadsField.getText());
                        oldImageTitle = (String) blurChoice.getSelectedItem();
                        oldPSFTitle = (String) psfChoice.getSelectedItem();
                        oldMethodIndex = methodChoice.getSelectedIndex();
                        oldResizingIndex = resizingChoice.getSelectedIndex();
                        oldOutputIndex = outputChoice.getSelectedIndex();
                        oldStencilIndex = stencilChoice.getSelectedIndex();
                        oldPrecisionIndex = precisionChoice.getSelectedIndex();
                        ConcurrencyUtils.setNumberOfThreads(oldNumberThreads);
                        setCursor(waitCursor);
                        deconvolveButton.setEnabled(false);
                        updateButton.setEnabled(false);
                        cancelButton.setEnabled(false);
                        MethodType selMethod = MethodType.values()[methodChoice.getSelectedIndex()];
                        PrecisionType selPrecision = PrecisionType.values()[precisionChoice.getSelectedIndex()];
                        double threshold = -1.0;
                        if (thresholdCheck.isSelected()) {
                            threshold = Double.parseDouble(thresholdField.getText());
                        }
                        double regParam = -1.0;
                        if (!regCheck.isSelected()) {
                            regParam = Double.parseDouble(regField.getText());
                        }
                        cleanOldData();
                        timer.reset().start();
                        switch (selPrecision) {
                        case DOUBLE:
                            switch (selMethod) {
                            case GTIK_REFLEXIVE:
                                dgtik_dct = new DoubleReflexiveGeneralizedTikhonov3D(imB, imPSF, DoubleStencil3DType.values()[stencilChoice.getSelectedIndex()].stencil, ResizingType.values()[resizingChoice.getSelectedIndex()], OutputType.values()[outputChoice.getSelectedIndex()], paddedCheck
                                        .isSelected(), regParam, threshold);
                                imX = dgtik_dct.deconvolve();
                                timer.stop();
                                regField.setText(String.format("%.6f", imX.getProperty("regParam")));
                                regSlider.setValue((Double) imX.getProperty("regParam"));
                                regCheck.setSelected(false);
                                break;
                            case GTIK_PERIODIC:
                                dgtik_fft = new DoublePeriodicGeneralizedTikhonov3D(imB, imPSF, DoubleStencil3DType.values()[stencilChoice.getSelectedIndex()].stencil, ResizingType.values()[resizingChoice.getSelectedIndex()], OutputType.values()[outputChoice.getSelectedIndex()], paddedCheck
                                        .isSelected(), regParam, threshold);
                                imX = dgtik_fft.deconvolve();
                                timer.stop();
                                regField.setText(String.format("%.6f", imX.getProperty("regParam")));
                                regSlider.setValue((Double) imX.getProperty("regParam"));
                                regCheck.setSelected(false);
                                break;
                            case TIK_REFLEXIVE:
                                dtik_dct = new DoubleReflexiveTikhonov3D(imB, imPSF, ResizingType.values()[resizingChoice.getSelectedIndex()], OutputType.values()[outputChoice.getSelectedIndex()], paddedCheck.isSelected(), regParam, threshold);
                                imX = dtik_dct.deconvolve();
                                timer.stop();
                                regField.setText(String.format("%.6f", imX.getProperty("regParam")));
                                regSlider.setValue((Double) imX.getProperty("regParam"));
                                regCheck.setSelected(false);
                                break;
                            case TIK_PERIODIC:
                                dtik_fft = new DoublePeriodicTikhonov3D(imB, imPSF, ResizingType.values()[resizingChoice.getSelectedIndex()], OutputType.values()[outputChoice.getSelectedIndex()], paddedCheck.isSelected(), regParam, threshold);
                                imX = dtik_fft.deconvolve();
                                timer.stop();
                                regField.setText(String.format("%.6f", imX.getProperty("regParam")));
                                regSlider.setValue((Double) imX.getProperty("regParam"));
                                regCheck.setSelected(false);
                                break;
                            case TSVD_REFLEXIVE:
                                dtsvd_dct = new DoubleReflexiveTruncatedSVD3D(imB, imPSF, ResizingType.values()[resizingChoice.getSelectedIndex()], OutputType.values()[outputChoice.getSelectedIndex()], paddedCheck.isSelected(), regParam, threshold);
                                imX = dtsvd_dct.deconvolve();
                                timer.stop();
                                regField.setText(String.format("%.6f", imX.getProperty("regParam")));
                                regSlider.setValue((Double) imX.getProperty("regParam"));
                                regCheck.setSelected(false);
                                break;
                            case TSVD_PERIODIC:
                                dtsvd_fft = new DoublePeriodicTruncatedSVD3D(imB, imPSF, ResizingType.values()[resizingChoice.getSelectedIndex()], OutputType.values()[outputChoice.getSelectedIndex()], paddedCheck.isSelected(), regParam, threshold);
                                imX = dtsvd_fft.deconvolve();
                                timer.stop();
                                regField.setText(String.format("%.6f", imX.getProperty("regParam")));
                                regSlider.setValue((Double) imX.getProperty("regParam"));
                                regCheck.setSelected(false);
                                break;
                            }
                            break;
                        case SINGLE:
                            switch (selMethod) {
                            case GTIK_REFLEXIVE:
                                fgtik_dct = new FloatReflexiveGeneralizedTikhonov3D(imB, imPSF, FloatStencil3DType.values()[stencilChoice.getSelectedIndex()].stencil, ResizingType.values()[resizingChoice.getSelectedIndex()], OutputType.values()[outputChoice.getSelectedIndex()], paddedCheck
                                        .isSelected(), (float) regParam, (float) threshold);
                                imX = fgtik_dct.deconvolve();
                                timer.stop();
                                regField.setText(String.format("%.6f", imX.getProperty("regParam")));
                                regSlider.setValue((Float) imX.getProperty("regParam"));
                                regCheck.setSelected(false);
                                break;
                            case GTIK_PERIODIC:
                                fgtik_fft = new FloatPeriodicGeneralizedTikhonov3D(imB, imPSF, FloatStencil3DType.values()[stencilChoice.getSelectedIndex()].stencil, ResizingType.values()[resizingChoice.getSelectedIndex()], OutputType.values()[outputChoice.getSelectedIndex()], paddedCheck
                                        .isSelected(), (float) regParam, (float) threshold);
                                imX = fgtik_fft.deconvolve();
                                timer.stop();
                                regField.setText(String.format("%.6f", imX.getProperty("regParam")));
                                regSlider.setValue((Float) imX.getProperty("regParam"));
                                regCheck.setSelected(false);
                                break;
                            case TIK_REFLEXIVE:
                                ftik_dct = new FloatReflexiveTikhonov3D(imB, imPSF, ResizingType.values()[resizingChoice.getSelectedIndex()], OutputType.values()[outputChoice.getSelectedIndex()], paddedCheck.isSelected(), (float) regParam, (float) threshold);
                                imX = ftik_dct.deconvolve();
                                timer.stop();
                                regField.setText(String.format("%.6f", imX.getProperty("regParam")));
                                regSlider.setValue((Float) imX.getProperty("regParam"));
                                regCheck.setSelected(false);
                                break;
                            case TIK_PERIODIC:
                                ftik_fft = new FloatPeriodicTikhonov3D(imB, imPSF, ResizingType.values()[resizingChoice.getSelectedIndex()], OutputType.values()[outputChoice.getSelectedIndex()], paddedCheck.isSelected(), (float) regParam, (float) threshold);
                                imX = ftik_fft.deconvolve();
                                timer.stop();
                                regField.setText(String.format("%.6f", imX.getProperty("regParam")));
                                regSlider.setValue((Float) imX.getProperty("regParam"));
                                regCheck.setSelected(false);
                                break;
                            case TSVD_REFLEXIVE:
                                ftsvd_dct = new FloatReflexiveTruncatedSVD3D(imB, imPSF, ResizingType.values()[resizingChoice.getSelectedIndex()], OutputType.values()[outputChoice.getSelectedIndex()], paddedCheck.isSelected(), (float) regParam, (float) threshold);
                                imX = ftsvd_dct.deconvolve();
                                timer.stop();
                                regField.setText(String.format("%.6f", imX.getProperty("regParam")));
                                regSlider.setValue((Float) imX.getProperty("regParam"));
                                regCheck.setSelected(false);
                                break;
                            case TSVD_PERIODIC:
                                ftsvd_fft = new FloatPeriodicTruncatedSVD3D(imB, imPSF, ResizingType.values()[resizingChoice.getSelectedIndex()], OutputType.values()[outputChoice.getSelectedIndex()], paddedCheck.isSelected(), (float) regParam, (float) threshold);
                                imX = ftsvd_fft.deconvolve();
                                timer.stop();
                                regField.setText(String.format("%.6f", imX.getProperty("regParam")));
                                regSlider.setValue((Float) imX.getProperty("regParam"));
                                regCheck.setSelected(false);
                                break;
                            }
                            break;
                        }
                        if (stencilChoice.isEnabled() == true) {
                            imX.setTitle(WindowManager.makeUniqueName(imB.getShortTitle() + "_deblurred_" + methodShortNames[methodChoice.getSelectedIndex()] + "_" + stencilShortNames[stencilChoice.getSelectedIndex()] + "_" + regField.getText()));
                        } else {
                            imX.setTitle(WindowManager.makeUniqueName(imB.getShortTitle() + "_deblurred_" + methodShortNames[methodChoice.getSelectedIndex()] + "_" + regField.getText()));
                        }
                        imX.show();
                        prevImageClosed = false;
                        enableUpdateButton();
                        IJ.showStatus(timer.toString());
                        setCursor(defaultCursor);
                        deconvolveButton.setEnabled(true);
                        cancelButton.setEnabled(true);
                    }
                });
                thread.setUncaughtExceptionHandler(new DefaultExceptionHandler());
                thread.start();
            }
        }

        private class UpdateButtonActionListener implements ActionListener {
            private final Timer timer = new Timer();

            public void actionPerformed(ActionEvent e) {
                if (!checkRegFieldText())
                    return;
                if (!checkThresholdFieldText())
                    return;
                Thread thread = new Thread(new Runnable() {
                    public void run() {
                        setCursor(waitCursor);
                        deconvolveButton.setEnabled(false);
                        updateButton.setEnabled(false);
                        cancelButton.setEnabled(false);
                        MethodType selMethod = MethodType.values()[methodChoice.getSelectedIndex()];
                        PrecisionType selPrecision = PrecisionType.values()[precisionChoice.getSelectedIndex()];
                        double threshold = -1.0;
                        if (thresholdCheck.isSelected()) {
                            threshold = Double.parseDouble(thresholdField.getText());
                        }
                        timer.reset().start();
                        switch (selPrecision) {
                        case DOUBLE:
                            switch (selMethod) {
                            case GTIK_REFLEXIVE:
                                dgtik_dct.update(Double.parseDouble(regField.getText()), threshold, imX);
                                break;
                            case GTIK_PERIODIC:
                                dgtik_fft.update(Double.parseDouble(regField.getText()), threshold, imX);
                                break;
                            case TIK_REFLEXIVE:
                                dtik_dct.update(Double.parseDouble(regField.getText()), threshold, imX);
                                break;
                            case TIK_PERIODIC:
                                dtik_fft.update(Double.parseDouble(regField.getText()), threshold, imX);
                                break;
                            case TSVD_REFLEXIVE:
                                dtsvd_dct.update(Double.parseDouble(regField.getText()), threshold, imX);
                                break;
                            case TSVD_PERIODIC:
                                dtsvd_fft.update(Double.parseDouble(regField.getText()), threshold, imX);
                                break;
                            }
                            break;
                        case SINGLE:
                            switch (selMethod) {
                            case GTIK_REFLEXIVE:
                                fgtik_dct.update(Float.parseFloat(regField.getText()), (float) threshold, imX);
                                break;
                            case GTIK_PERIODIC:
                                fgtik_fft.update(Float.parseFloat(regField.getText()), (float) threshold, imX);
                                break;
                            case TIK_REFLEXIVE:
                                ftik_dct.update(Float.parseFloat(regField.getText()), (float) threshold, imX);
                                break;
                            case TIK_PERIODIC:
                                ftik_fft.update(Float.parseFloat(regField.getText()), (float) threshold, imX);
                                break;
                            case TSVD_REFLEXIVE:
                                ftsvd_dct.update(Float.parseFloat(regField.getText()), (float) threshold, imX);
                                break;
                            case TSVD_PERIODIC:
                                ftsvd_fft.update(Float.parseFloat(regField.getText()), (float) threshold, imX);
                                break;
                            }
                            break;
                        }
                        timer.stop();
                        if (stencilChoice.isEnabled() == true) {
                            imX.setTitle(WindowManager.makeUniqueName(imB.getShortTitle() + "_deblurred_" + methodShortNames[methodChoice.getSelectedIndex()] + "_" + stencilShortNames[stencilChoice.getSelectedIndex()] + "_" + regField.getText()));
                        } else {
                            imX.setTitle(WindowManager.makeUniqueName(imB.getShortTitle() + "_deblurred_" + methodShortNames[methodChoice.getSelectedIndex()] + "_" + regField.getText()));
                        }
                        imX.updateAndDraw();
                        blurChoice.removeItemAt(blurChoice.getItemCount() - 1);
                        psfChoice.removeItemAt(psfChoice.getItemCount() - 1);
                        blurChoice.addItem(imX.getTitle());
                        psfChoice.addItem(imX.getTitle());
                        blurChoice.revalidate();
                        psfChoice.revalidate();
                        IJ.showStatus(timer.toString());
                        setCursor(defaultCursor);
                        enableUpdateButton();
                        deconvolveButton.setEnabled(true);
                        cancelButton.setEnabled(true);
                    }
                });
                thread.setUncaughtExceptionHandler(new DefaultExceptionHandler());
                thread.start();
            }

        }

        private class CancelButtonActionListener implements ActionListener {
            public void actionPerformed(ActionEvent e) {
                mainPanel.dispose();
                ImagePlus.removeImageListener(getImageListener());
                cleanAll();
                ConcurrencyUtils.shutdown();
            }
        }

        private boolean checkThresholdFieldText() {
            double val = 0.0;
            try {
                val = Double.parseDouble(thresholdField.getText());
            } catch (Exception ex) {
                IJ.error("Threshold must be a nonnegative value.");
                return false;
            }
            if (val < 0.0) {
                IJ.error("Threshold must be a nonnegative value.");
                return false;
            }
            return true;
        }

        private boolean checkRegFieldText() {
            double val = 0.0;
            try {
                val = Double.parseDouble(regField.getText());
            } catch (Exception ex) {
                IJ.error("Regularization parameter must be between 0 and 1.");
                return false;
            }
            if ((val < 0.0) || (val > 1.0)) {
                IJ.error("Regularization parameter must be between 0 and 1.");
                return false;
            }
            return true;
        }

        private boolean checkThreadsFieldText() {
            int val = 0;
            try {
                val = Integer.parseInt(threadsField.getText());
            } catch (Exception ex) {
                IJ.error("Number of threads must be power-of-two number.");
                return false;
            }
            if (val < 1) {
                IJ.error("Number of threads must be power-of-two number.");
                return false;
            }
            if (!ConcurrencyUtils.isPowerOf2(val)) {
                IJ.error("Number of threads must be power-of-two number.");
                return false;
            }
            return true;
        }

        private void enableUpdateButton() {
            if ((prevImageClosed == false) && (oldImageTitle.equals(blurChoice.getSelectedItem())) && (oldPSFTitle.equals(psfChoice.getSelectedItem())) && (oldMethodIndex == methodChoice.getSelectedIndex()) && (oldResizingIndex == resizingChoice.getSelectedIndex())
                    && (oldOutputIndex == outputChoice.getSelectedIndex()) && (oldStencilIndex == stencilChoice.getSelectedIndex()) && (oldPrecisionIndex == precisionChoice.getSelectedIndex()) && (!regCheck.isSelected())) {
                updateButton.setEnabled(true);
                regCheck.setSelected(false);
            } else {
                updateButton.setEnabled(false);
            }
        }
    }

    public static void main(String args[]) {

        new ImageJ();
        IJ.open("D:\\Research\\Images\\head\\head_blur.tif");
        IJ.open("D:\\Research\\Images\\head\\head_psf.tif");
        IJ.runPlugIn("com.edu.emory.mathcs.restoretools.spectral.ParallelSpectralDeconvolution3D", null);

    }

}
