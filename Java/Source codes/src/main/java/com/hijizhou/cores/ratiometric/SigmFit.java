package com.hijizhou.cores.ratiometric;

import ij.IJ;
import ij.ImageJ;
import ij.ImagePlus;
import ij.ImageStack;
import ij.io.Opener;
import ij.measure.Calibration;
import ij.measure.CurveFitter;
import ij.process.FloatProcessor;
import ij.process.ImageProcessor;
import ij.process.ImageStatistics;

import java.util.Random;

public class SigmFit {
    public double getA() {
        return a;
    }

    public void setA(double a) {
        this.a = a;
    }

    public double getB() {
        return b;
    }

    public void setB(double b) {
        this.b = b;
    }

    public double getC() {
        return c;
    }

    public void setC(double c) {
        this.c = c;
    }

    public double getD() {
        return d;
    }

    public void setD(double d) {
        this.d = d;
    }

    public double getGoodness() {
        return goodness;
    }
    public double[] getParameters() {
        return parameters;
    }

    public void setGoodness(double goodness) {
        this.goodness = goodness;
    }

    private double a = -1;
    private double b = 0;
    private double c = 1;
    private double d = 5;
    private double goodness;
    private double[] parameters;
    private int numpar;
    private int fitFun = 0;

    public void setFitFun(int selectIndex){
        this.fitFun = selectIndex;
    }

    public CurveFitter doFit(double[] x, double[] y) {
        CurveFitter fitter = new CurveFitter(x, y);
        double[] initialParams = new double[]{0.8, 1.19, 0.02, 6.5};//,regest.evaluate()[0]};
        fitter.setInitialParameters(initialParams);

        String equation = "";
        switch (fitFun)
        {
            case 0: //Boltzman Fun
                equation = "y=b+(c-b)/(1+exp((x-d)/a))";
                break;
            case 1: //Chapman-Richards
                equation = "y = a*(1-exp(-b*x))^c";
                break;
            case 2: //Rodbard
                equation = "y = d+(a-d)/(1+(x/c)^b)";
                break;
            case 3: // Straight Line
                equation = "y = a+bx";
                break;

        }

        fitter.doCustomFit(equation, initialParams, false);
        double[] params = fitter.getParams();
        a = (double)Math.round((params[0]) * 1000d) / 1000d;
        b = (double)Math.round((params[1]) * 1000d) / 1000d;
        c = (double)Math.round((params[2]) * 1000d) / 1000d;
        d = (double)Math.round((params[3]) * 1000d) / 1000d;
        goodness = (double)Math.round((fitter.getFitGoodness()) * 1000d) / 1000d;
        numpar = fitter.getNumParams();
        parameters = fitter.getParams();

// use the static plot function from the fitter class to produce an output plot.
//        ij.plugin.frame.Fitter.plot(fitter);

        return fitter;
    }

    public double[] evalFit(double[] x) {
        double[] y = new double[x.length];
        for (int i = 0; i < x.length; i++) {
            switch(fitFun){
                case 0: //Boltzman Fun
                    y[i] = (b + (c - b) / (1 + Math.exp((x[i] - d) / a)));
                    break;
                case 1: //Chapman-Richards
                    y[i] = a *Math.pow( 1- Math.exp(-b* x[i]), c);
                    break;
                case 2: //Rodbard
                    y[i] = d + (a-d) / (1 + Math.pow(x[i]/c, b));
                    break;
                case 3: // Straight Line
                    y[i] = a + b*x[i];
                    break;
            }

        }
        return y;
    }
    public double[] evalInvFit(double[] y) {
        double[] x = new double[y.length];
        for (int i = 0; i < y.length; i++) {

            switch(fitFun){
                case 0: //Boltzman Fun
                    if(((c-y[i])/(y[i]-b) <= 0) || (y[i]-b == 0) || Double.isInfinite(x[i])){
                        x[i] = Double.NEGATIVE_INFINITY;
                        continue;
                    }
                    x[i] = a * Math.log((c-y[i])/(y[i]-b)) + d;
                    break;
                case 1: //Chapman-Richards
                    if(a==0 || c==0){
                        x[i] = Double.POSITIVE_INFINITY;
                    }
                    double tmp = 1 - Math.pow(y[i]/a, 1/c);
                    if(b==0 || tmp<=0){
                        x[i] = Double.POSITIVE_INFINITY;
                    }
                    x[i] = - (Math.log(tmp))/b;
                    break;
                case 2: //Rodbard
                    if(b==0 || (d-y[i])==0){
                        x[i] = Double.POSITIVE_INFINITY;
                    }
                    x[i] = c* Math.pow((y[i]-a)/(d-y[i]), 1/b);
                    break;
                case 3: // Straight Line
                    if(b==0){
                        x[i] = Double.POSITIVE_INFINITY;
                    }
                    x[i] = (y[i] - a) / b;
                    break;

            }

        }
        return x;
    }

    public ImagePlus evalInvFit(ImagePlus ipOriginal) {


        ImageProcessor ip1;
        int slices1 = ipOriginal.getStackSize();
        ImageStack stack1 = ipOriginal.getStack();

        //create the ratio image
        ImageStack stackPH = ipOriginal.createEmptyStack();
        for (int i = 1; i <= slices1; i++) {
            ImageProcessor ipp1 = stack1.getProcessor(i);
            ImageProcessor ipp2 = ipp1.duplicate();
            ipp2 = ipp2.convertToFloat();
            stackPH.addSlice(stack1.getSliceLabel(i), ipp2);
        }

        ImagePlus imgPH = new ImagePlus("pH map", stackPH);

        for (int n = 1; n <= slices1; n++) {

            FloatProcessor fpCal = stack1.getProcessor(n).convertToFloatProcessor();

            float[] Yf = (float[]) fpCal.getPixels();
            double[] Y = new double[Yf.length];
            for (int i = 0; i < Y.length; i++) {
                Y[i] = (double) Yf[i];
            }
            double[] Xd = evalInvFit(Y);
            float[] X = new float[Xd.length];
            for (int i = 0; i < X.length; i++) {
                X[i] = (float) Xd[i];
            }

            fpCal.setPixels(X);

//            ImagePlus phImg = new ImagePlus("pH Map", fpCal);

            imgPH.setProcessor(fpCal);

        }
        return imgPH;

    }

    public static void main(String[] args) {
// make some test data
        new ImageJ();
        double[] x = new double[]{0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10};

        double Ga = -1;
        double Gb = 0;
        double Gc = 1;
        double Gd = 5;

        SigmFit sf = new SigmFit();
        sf.setA(Ga);
        sf.setB(Gb);
        sf.setC(Gc);
        sf.setD(Gd);

        double[] y = sf.evalFit(x);

        Random rand = new Random();
        for (int i = 1; i < y.length; i++) {
            y[i] = (float) (y[i] + rand.nextInt(5) * 0.02);
        }

        sf.doFit(x, y);

        //read the ratio image
        Opener opener = new Opener();

        String currentPath = System.getProperty("user.dir");
        String oriImgPath = currentPath + "/src/test/resources/" + "cameraman.tif";

        ImagePlus ipOriginal = opener.openImage(oriImgPath);
        ipOriginal.show();

        FloatProcessor fpCal = ipOriginal.getProcessor().convertToFloatProcessor();

        float[] Xf = (float[]) fpCal.getPixels();
        double[] X = new double[Xf.length];
        for (int i = 0; i < X.length; i++) {
            X[i] = (double) Xf[i];
        }
        double[] Yd = sf.evalFit(X);
        float[] Y = new float[Yd.length];
        for (int i = 0; i < Y.length; i++) {
            Y[i] = (float) Yd[i];
        }

        fpCal.setPixels(Y);

        ImagePlus phImg = new ImagePlus("Calibrated image", fpCal);

        Calibration cal = phImg.getCalibration();
        cal.setFunction(20, sf.parameters, "Inverted Gray Value", true);

        phImg.setCalibration(cal);

        //statistics
        ImageStatistics isOrg = ipOriginal.getStatistics();
        ImageStatistics isCal = phImg.getStatistics();

        System.out.println("Original: mean - " + isOrg.mean + ", std - " + isOrg.stdDev);
        System.out.println("Calibrated: mean - " + isCal.mean + ", std - " + isCal.stdDev);

        // display
        phImg.show();
        IJ.run(phImg, "Fire", null);  //lut
        String location = "[Upper Right]";
        String fil = "White";
        String label = "Black";
        String number = "5";
        String decimal = "0";
        String font = "12";
        String zoom = "1";

        IJ.run(phImg, "Calibration Bar...", "location=" + location + " fill=" + fil + " label=" + label + " number=" + number + " decimal=" + decimal + " font=" + font + " zoom=" + zoom + " overlay");

    }

}
