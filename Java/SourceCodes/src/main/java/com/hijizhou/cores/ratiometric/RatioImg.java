package com.hijizhou.cores.ratiometric;

import ij.ImagePlus;
import ij.ImageStack;
import ij.process.ImageProcessor;

public class RatioImg {

    public static ImagePlus get(double b1, double t1, double b2, double t2, ImagePlus i1, ImagePlus i2) {

        double k1 = 1; //multiplication factor

        double v1, v2;
        int width = i1.getWidth();
        int height = i1.getHeight();
        ImageProcessor ip1, ip2, ipRatio;
        int slices1 = i1.getStackSize();
        int slices2 = i2.getStackSize();
        ImageStack stack1 = i1.getStack();
        ImageStack stack2 = i2.getStack();

        //create the ratio image
        ImageStack stackRatio = i1.createEmptyStack();
        for (int i = 1; i <= stack1.getSize(); i++) {
            ImageProcessor ipp1 = stack1.getProcessor(i);
            ImageProcessor ipp2 = ipp1.duplicate();
            ipp2 = ipp2.convertToFloat();
            stackRatio.addSlice(stack1.getSliceLabel(i), ipp2);
        }
        ImagePlus imgRatio = new ImagePlus("Ratio Image", stackRatio);

        for (int n = 1; n <= slices2; n++) {
            ip1 = stack1.getProcessor(n <= slices1 ? n : slices1);
            ip2 = stack2.getProcessor(n);
            ipRatio = stackRatio.getProcessor(n);
            for (int x = 0; x < width; x++) {
                for (int y = 0; y < height; y++) {
                    // Get intA
                    v1 = ip1.getPixelValue(x, y);
                    // Subtract bkgA
                    v1 = v1 - b1;
                    // Get intB
                    v2 = ip2.getPixelValue(x, y);
                    // Subtract bkgB
                    v2 = v2 - b2;
                    // Make sure no negative values are present
                    if (v1 < 0)
                        v1 = 0;
                    else if (v2 < 0)
                        v2 = 0;
                    // Clip values with T1
                    if (v1 < t1)
                        v1 = 0;
                    // Clip values with T2
                    if (v2 < t2)
                        v2 = 0;
                    // Correct zero/zero cases
                    if (v1 == 0 && v2 == 0) {
                        v1 = 0;
                        v2 = 0;
                    }
                    // Calculate the ratio
                    v2 = v1 / v2;
                    // Apply the multiplication factor
                    v2 = v2 * k1;
                    // Write the result
                    ipRatio.putPixelValue(x, y, v2);
                }
            }
            imgRatio.setProcessor(ipRatio);

        }
        return imgRatio;
    }
}
