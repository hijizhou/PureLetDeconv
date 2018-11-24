package com.hijizhou.utilities;

import ij.ImagePlus;
import ij.process.ImageProcessor;

import java.awt.*;

public class ContrastAdjuster {

    public static void adjustContrast(ImagePlus imp, ImageProcessor ip, int cvalue) {
        double slope;
        double min, max;
        double defaultMin, defaultMax;
        int sliderRange = 256;
        min = imp.getDisplayRangeMin();
        max = imp.getDisplayRangeMax();
        defaultMin = imp.getDisplayRangeMin();
        defaultMax = imp.getDisplayRangeMax();

        double center = min + (max-min)/2.0;
        double range = defaultMax-defaultMin;
        double mid = sliderRange/2;
        if (cvalue<=mid)
            slope = cvalue/mid;
        else
            slope = mid/(sliderRange-cvalue);
        if (slope>0.0) {
            min = center-(0.5*range)/slope;
            max = center+(0.5*range)/slope;
        }
        setMinAndMax(imp, min, max);
        int type = imp.getType();
        boolean RGBImage = type==ImagePlus.COLOR_RGB;

        if (RGBImage) doMasking(imp, ip);
    }
    /** Restore image outside non-rectangular roi. */
    static void doMasking(ImagePlus imp, ImageProcessor ip) {
        ImageProcessor mask = imp.getMask();
        if (mask!=null) {
            Rectangle r = ip.getRoi();
            if (mask.getWidth()!=r.width||mask.getHeight()!=r.height) {
                ip.setRoi(imp.getRoi());
                mask = ip.getMask();
            }
            ip.reset(mask);
        }
    }
    static void setMinAndMax(ImagePlus imp, double min, double max) {
        boolean rgb = imp.getType()==ImagePlus.COLOR_RGB;
        int channels = imp.getNChannels();
        if (channels!=7 && rgb)
            imp.setDisplayRange(min, max, channels);
        else
            imp.setDisplayRange(min, max);
    }
}
