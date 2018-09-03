package com.hijizhou.utilities;
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
import com.cern.colt.matrix.tdcomplex.DComplexMatrix3D;
import com.cern.colt.matrix.tdcomplex.impl.DenseDComplexMatrix3D;
import com.cern.colt.matrix.tdouble.DoubleMatrix2D;
import com.cern.colt.matrix.tdouble.DoubleMatrix3D;
import com.cern.colt.matrix.tfloat.FloatMatrix2D;
import com.cern.jet.math.tdouble.DoubleFunctions;
import com.hijizhou.cores.denoising.DDIDstep;
import com.edu.emory.mathcs.restoretools.spectral.DoubleCommon2D;
import com.edu.emory.mathcs.restoretools.spectral.DoubleCommon3D;
import com.edu.emory.mathcs.restoretools.spectral.FloatCommon2D;
import ij.ImagePlus;
import ij.ImageStack;
import ij.io.FileSaver;
import ij.process.FloatProcessor;

import java.awt.image.ColorModel;

public class ImageUtil {

    public static DoubleMatrix2D postfiltering(DoubleMatrix2D input, double sigma2){
        DDIDstep ddid = new DDIDstep();

        ddid.setR(9);
        ddid.setSigma2(sigma2);

        input = ddid.run(input, input);
        return input;
    }

    public static DoubleMatrix2D rescale(DoubleMatrix2D input, double low,double high){

        DoubleMatrix2D inputT = input.copy();
        double[] minV = inputT.getMinLocation();
        inputT.assign(DoubleFunctions.minus(minV[0]));
        // Obtain and appling the scale:
        double[] maxV = inputT.getMaxLocation();
        if(maxV[0]>0){
            inputT.assign(DoubleFunctions.div(maxV[0]));
        }

        if(Math.abs(high-low)!=1){
            inputT.assign(DoubleFunctions.mult(high-low));
        }
        if(high!=0){
            inputT.assign(DoubleFunctions.plus(high));
        }

        return inputT;

    }

    public static void preview(DoubleMatrix2D input){
        FloatProcessor ip = new FloatProcessor(input.rows(), input.columns());
        DoubleCommon2D.assignPixelsToProcessor(ip,
                input, null);

        ImagePlus tmpIP = new ImagePlus("Preview", ip);
        tmpIP.show();
//        new FileSaver(tmpIP).save();
    }

    public static void preview(DoubleMatrix3D input){
        DoubleMatrix3D aux = input.copy();
        ImageStack tmpOut = new ImageStack(aux.rows(), aux.columns());
        DoubleCommon3D.assignPixelsToStack(tmpOut,
                aux, null);

        ImagePlus tmpIP = new ImagePlus("Preview", tmpOut);
        tmpIP.show();
//        new FileSaver(tmpIP).save();
    }


    public static void previewSave(DoubleMatrix3D input){
        DoubleMatrix3D aux = input.copy();
        ImageStack tmpOut = new ImageStack(aux.rows(), aux.columns());
        DoubleCommon3D.assignPixelsToStack(tmpOut,
                aux, null);

        ImagePlus tmpIP = new ImagePlus("Preview", tmpOut);
        tmpIP.show();
        new FileSaver(tmpIP).save();
    }

    public static void previewSave(DComplexMatrix3D input){
        DComplexMatrix3D inputF=  input.copy();
        ((DenseDComplexMatrix3D)inputF).ifft3(true);
        ImageStack tmpOut = new ImageStack(inputF.rows(), inputF.columns());
        DoubleMatrix3D MMa = inputF.getRealPart();
        DoubleCommon3D.assignPixelsToStack(tmpOut,
                MMa, null);

        ImagePlus tmpIP = new ImagePlus("Preview", tmpOut);
        tmpIP.show();
    }

    public static void preview(DComplexMatrix3D input){
        DComplexMatrix3D inputF=  input.copy();
        ((DenseDComplexMatrix3D)inputF).ifft3(true);
        ImageStack tmpOut = new ImageStack(inputF.rows(), inputF.columns());
        DoubleMatrix3D MMa = inputF.getRealPart();
        DoubleCommon3D.assignPixelsToStack(tmpOut,
                MMa, null);

        ImagePlus tmpIP = new ImagePlus("Preview", tmpOut);
        tmpIP.show();
    }

    public static ImagePlus matrix2Plus(DoubleMatrix2D input, ColorModel cmY, String title) {
        int width = input.rows();
        int height = input.columns();
        FloatProcessor ip = new FloatProcessor(height, width);
        DoubleCommon2D.assignPixelsToProcessor(ip,
                input, cmY);
        ImagePlus imOutput = new ImagePlus(title, ip);
        return imOutput;

    }

    public static ImagePlus matrix2Plus(FloatMatrix2D input, ColorModel cmY, String title) {
        int width = input.rows();
        int height = input.columns();
        FloatProcessor ip = new FloatProcessor(height, width);
        FloatCommon2D.assignPixelsToProcessor(ip,
                input, cmY);
        ImagePlus imOutput = new ImagePlus(title, ip);
        return imOutput;

    }

    public static ImagePlus matrix2Plus(DoubleMatrix3D input, ColorModel cmY, String title){
        int width = input.rows();
        int height = input.columns();

        ImageStack stackOut = new ImageStack(width, height);

        DoubleCommon3D.assignPixelsToStack(stackOut, input, cmY);

        ImagePlus imX = new ImagePlus(title, stackOut);

        return imX;
    }

}
