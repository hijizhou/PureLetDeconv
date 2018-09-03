package com.hijizhou.imageware;

final public class Operations extends Object {

    private static final boolean offsets[][] = {
        /* LLL */{false, false, false},
        /* LLH */ {false, false, true},
        /* LHL */ {false, true, false},
        /* LHH */ {false, true, true},
        /* HLL */ {true, false, false},
        /* HLH */ {true, false, true},
        /* HHL */ {true, true, false},
        /* HHH */ {true, true, true}
    };
    private static final double gdc[] = {-1, 0, 1};
    private static final double EPS = 1e-9;
    private static final int    Nmin = 16;
    private static final double[][] laplacian = {{2.0 / 6.0, -1.0 / 6.0, 2.0 / 6.0},
        {-1.0 / 6.0, -4.0 / 6.0, -1.0 / 6.0},
        {2.0 / 6.0, -1.0 / 6.0, 2.0 / 6.0}};

   

    /**
     * Perform the filtering in Fourier domain.
     *
     * @param ReIn		input signal 1D (real part)
     * @param ImIn	 	input signal 1D (imaginary part)
     * @param ReF		filter (real part)
     * @param ImF		filter (imaginary part)
     * @param ReOut		output signal 1D (real part)
     * @param ImOut	 	output signal 1D (imaginary part)
     * @param size		size of the vectors
     * @param iteration	value of the iteration in the loop
     */
    final static protected void multiplySpecial(
            final double[] ReIn, final double[] ImIn,
            final double[] ReF, final double[] ImF,
            double[] ReOut, double[] ImOut,
            final int size, final int iteration) {

        int j;
        int pow2 = (int) Math.pow(2, iteration - 1);

        for (int i = 0; i < size; i++) {
            j = i * pow2;
            ReOut[i] = ReIn[i] * ReF[j] - ImIn[i] * ImF[j];
            ImOut[i] = ReIn[i] * ImF[j] + ImIn[i] * ReF[j];
        }
    }

    /**
     * Perform the filtering in Fourier domain and conjugate.
     *
     * @param ReIn		input signal 1D (real part)
     * @param ImIn	 	input signal 1D (imaginary part)
     * @param ReF		filter (real part)
     * @param ImF		filter (imaginary part)
     * @param ReOut		output signal 1D (real part)
     * @param ImOut	 	output signal 1D (imaginary part)
     * @param size		size of the vectors
     * @param iteration	value of the iteration in the loop
     */
    final static protected void multiplyAndConjugateSpecial(
            final double[] ReIn, final double[] ImIn,
            final double[] ReF, final double[] ImF,
            double[] ReOut, double[] ImOut,
            final int size, final int iteration) {

        int j;
        int pow2 = (int) Math.pow(2, iteration - 1);

        for (int i = 0; i < size; i++) {
            j = i * pow2;
            ReOut[i] = ReIn[i] * ReF[j] + ImIn[i] * ImF[j];
            ImOut[i] = -ReIn[i] * ImF[j] + ImIn[i] * ReF[j];
        }
    }

    /**
     * Perform the downsampling in Fourier domain (real or imaginary parts).
     *
     * @param output		 result of downsampling
     * @param FilterH	 response of the lowpass H filter
     * @param FilterG	 response of the highpass G filter
     * @param offset		 halfsize of the vectors
     */
    final static private void downsampling(double[] output, double[] FilterH, double[] FilterG, final int offset) {

        for (int j = 0; j < offset; j++) {
            output[j] = 0.5 * (FilterH[j] + FilterH[j + offset]);
            output[j + offset] = 0.5 * (FilterG[j] + FilterG[j + offset]);
        }
    }

    /**
     * Perform the upsampling in Fourier domain (real or imaginary parts).
     *
     * @param in			input of upsampling
     * @param inH		input of the lowpass H filter
     * @param inG		input of the lowpass G filter
     * @param offset		 halfsize of the vectors
     */
    final static private void upsampling(final double[] in, double[] inH, double[] inG, final int offset) {

        for (int j = 0; j < offset; j++) {
            inH[j] = in[j];
            inH[j + offset] = in[j];
            inG[j] = in[j + offset];
            inG[j + offset] = in[j + offset];
        }
    }

    /**
     * Perform the addition of two arrays.
     *
     * @param in1		 first array
     * @param in2		 second array
     * @param out		 output array should be allocated before the call
     */
    final static protected void add(final double[] in1, final double[] in2, double[] out, final int size) {
        for (int j = 0; j < size; j++) {
            out[j] = in1[j] + in2[j];
        }
    }

    /**
     */
    final static protected void complexMultiplication(double[] Re1, double[] Im1, double[] Re2, double[] Im2) {
        int size = Re1.length;
        double aux;
        for (int i = 0; i < size; i++) {
            aux = Re1[i];
            Re1[i] = Re1[i] * Re2[i] - Im1[i] * Im2[i];
            Im1[i] = aux * Im2[i] + Im1[i] * Re2[i];
        }
    }

    /**
     */
    final static protected double computeMedian(ImageWare input) {
        double[] vector = getPixels(input);
        double med = getMedian(vector);

        return med;
    }

    final static protected double computeMode(double a[]) {
        int maxCount = 0, count;
        double maxValue = 0.0, M = getMax(a), dyn = 1 * a.length;
        multiplyAndRound(a, dyn / M, a);
        for (int i = 0; i < a.length; i++) {
            count = 0;
            for (int j = 0; j < a.length; j++) {
                if (a[j] == a[i]) {
                    count = count + 1;
                }
            }
            if (count > maxCount) {
                maxCount = count;
                maxValue = a[i];
            }
        }
        maxValue = maxValue * M / dyn;
        return maxValue;
    }

    /**
     */
    final static protected double[] getPixels(ImageWare input) {
        int[] size = input.getSize();
        int N = size[0] * size[1] * size[2], k;
        double[] vector = new double[N];
        for (int x = 0; x < size[0]; x++) {
            for (int y = 0; y < size[1]; y++) {
                for (int z = 0; z < size[2]; z++) {
                    k = z + y * size[2] + x * size[1] * size[2];
                    vector[k] = input.getPixel(x, y, z);
                }
            }
        }
        return vector;
    }

    /**
     */
    final static protected int[] getIndex(double[] input, double[] values, int I) {
        int[] index = new int[I];
        for (int i = 0; i < I; i++) {
            for (int n = 0; n < input.length; n++) {
                if (input[n] == values[i]) {
                    index[i] = n;
                }
            }
        }
        return index;
    }

    /**
     */
    final static protected double[] restrict(double[] array, double Tol) {
        int i = 0;
        double M = getMax(array) * Tol;
        for (i = 0; i < array.length; i++) {
            if (array[i] > M) {
                break;
            }
        }
        int I = i;
        double[] output = new double[I];
        for (i = 0; i < I; i++) {
            output[i] = array[i];
        }

        return output;
    }

    /**
     */
    final static protected void putIndexedValues(double[] input, double[] output, int[] index, int I) {
        for (int i = 0; i < I; i++) {
            output[i] = input[index[i]];
        }
    }

    /**
     */
    final static protected double getSum(double[] input) {
        double sum = 0.0;
        for (int i = 0; i < input.length; i++) {
            sum = sum + input[i];
        }

        return sum;
    }

    /**
     */
    final static protected double getMean(double[] input) {
        double mean = getSum(input) / input.length;

        return mean;
    }

    /**
     */
    final static protected double getMedian(double[] input) {
        int N = input.length;
        double[] sorted = new double[N];
        System.arraycopy(input, 0, sorted, 0, N);
        java.util.Arrays.sort(sorted);
        int n = (int) Math.floor((N + 1.0) / 2.0);
        int m = 2 * n == N ? n : n - 1;

        return (sorted[n - 1] + sorted[m]) / 2.0;
    }

    /**
     */
    final static protected double getMax(double[] input) {
        int N = input.length;
        double[] sorted = new double[N];
        System.arraycopy(input, 0, sorted, 0, N);
        java.util.Arrays.sort(sorted);

        return sorted[N - 1];
    }

    /**
     */
    final static protected double[] absolute(double[] input) {
        double[] output = new double[input.length];
        for (int i = 0; i < input.length; i++) {
            output[i] = Math.abs(input[i]);
        }

        return output;
    }

    /**
     */
    final static protected void absolute(double[] input, double[] output) {
        for (int i = 0; i < input.length; i++) {
            output[i] = Math.abs(input[i]);
        }
    }

    /**
     */
    final static protected void fill(double[] output, double value) {
        for (int i = 0; i < output.length; i++) {
            output[i] = value;
        }
    }

    /**
     */
    final static protected double[] subtract(double[] array1, double[] array2) {
        double[] output = new double[array1.length];
        for (int i = 0; i < array1.length; i++) {
            output[i] = array1[i] - array2[i];
        }

        return output;
    }

    /**
     */
    final static protected double[] subtract(double[] array, double value) {
        double[] output = new double[array.length];
        for (int i = 0; i < array.length; i++) {
            output[i] = array[i] - value;
        }

        return output;
    }

    /**
     */
    final static protected void subtract(double[] array1, double[] array2, double[] output) {
        for (int i = 0; i < array1.length; i++) {
            output[i] = array1[i] - array2[i];
        }
    }

    /**
     */
    final static protected void subtract(double[] array1, double value, double[] output) {
        for (int i = 0; i < array1.length; i++) {
            output[i] = array1[i] - value;
        }
    }

    /**
     */
    final static protected double[] add(double[] array1, double[] array2) {
        double[] output = new double[array1.length];
        for (int i = 0; i < array1.length; i++) {
            output[i] = array1[i] + array2[i];
        }

        return output;
    }

    /**
     */
    final static protected void add(double[] array1, double[] array2, double[] output) {
        for (int i = 0; i < array1.length; i++) {
            output[i] = array1[i] + array2[i];
        }
    }

    /**
     */
    final static protected void add(double[] array1, double value, double[] output) {
        for (int i = 0; i < array1.length; i++) {
            output[i] = array1[i] + value;
        }
    }

    /**
     */
    final static protected int[] add(int[] array1, int value) {
        int[] output = new int[array1.length];
        for (int i = 0; i < array1.length; i++) {
            output[i] = array1[i] + value;
        }

        return output;
    }

    /**
     */
    final static protected double[] add(double[] array1, double value) {
        double[] output = new double[array1.length];
        for (int i = 0; i < array1.length; i++) {
            output[i] = array1[i] + value;
        }

        return output;
    }

    /**
     */
    final static protected double[] multiply(double[] array1, double[] array2) {
        double[] output = new double[array1.length];
        for (int i = 0; i < array1.length; i++) {
            output[i] = array1[i] * array2[i];
        }

        return output;
    }

    /**
     */
    final static protected void multiply(double[] array1, double[] array2, double[] output) {
        for (int i = 0; i < array1.length; i++) {
            output[i] = array1[i] * array2[i];
        }
    }

    /**
     */
    final static protected void multiply(double[] array1, double value, double[] output) {
        for (int i = 0; i < array1.length; i++) {
            output[i] = array1[i] * value;
        }
    }

    /**
     */
    final static protected double[] multiply(double[] array1, double value) {
        double[] output = new double[array1.length];
        for (int i = 0; i < array1.length; i++) {
            output[i] = array1[i] * value;
        }

        return output;
    }

    /**
     */
    final static protected int[] multiply(int[] array1, int value) {
        int[] output = new int[array1.length];
        for (int i = 0; i < array1.length; i++) {
            output[i] = array1[i] * value;
        }

        return output;
    }

    /**
     */
    final static protected double multiplyAndSum(double[] array1, double[] array2) {
        double sum = 0.0;
        for (int i = 0; i < array1.length; i++) {
            sum = sum + array1[i] * array2[i];
        }

        return sum;
    }

    /**
     */
    final static protected void multiplyAndRound(double[] array1, double value, double[] output) {
        for (int i = 0; i < array1.length; i++) {
            output[i] = Math.round(array1[i] * value);
        }
    }

    /**
     */
    final static protected double[] divide(double[] array1, double[] array2) {
        double[] output = new double[array1.length];
        for (int i = 0; i < array1.length; i++) {
            output[i] = array1[i] / array2[i];
        }

        return output;
    }

    /**
     */
    final static protected double[] divide(double value, double[] array) {
        double[] output = new double[array.length];
        for (int i = 0; i < array.length; i++) {
            output[i] = value / array[i];
        }

        return output;
    }

    final static protected void computeBlkVar(ImageWare input, double[] output, int K1, int K2) {
        int[] size = input.getSize();
        int Nblk = size[0] * size[1] / (K1 * K2);
        ImageWare R = convolveFIR2(input, laplacian);
        double[][] array = new double[size[0]][size[1]];
        input.getBlockXY(0, 0, 0, array, ImageWare.PERIODIC);
        double[][] blks = getBlocks(R, K1, K2);
        for (int i = 0; i < Nblk; i++) {
            output[i] = robustVarEstimate(blks[i]);
        }
    }

    final static protected void computeBlkMean(double[][] input, double[] output, int K1, int K2, int wtype) {
        int nx = input.length;
        int ny = input[0].length;
        int Nblk = nx * ny / (K1 * K2);
        double[][] blks = getBlocks(input, K1, K2);
        for (int i = 0; i < Nblk; i++) {
            output[i] = robustMeanEstimate(blks[i], wtype);
            //output[i]  = getMedian(blks[i]);
            //output[i]  = getMean(blks[i]);
        }
    }

    /**
     */
    final static protected double[][] getBlocks(double[][] input, int K1, int K2) {
        int nx = input.length;
        int ny = input[0].length;
        int Nblk = nx * ny / (K1 * K2);
        double[][] blks = new double[Nblk][K1 * K2];
        int x = 0, y = 0, k = 0;
        while (x < nx) {
            y = 0;
            while (y < ny) {
                for (int i = 0; i < K1; i++) {
                    for (int j = 0; j < K2; j++) {
                        blks[k][i * K2 + j] = input[x + i][y + j];
                    }
                }
                y = y + K2;
                k++;
            }
            x = x + K1;
        }

        return blks;
    }

    /**
     */
    final static protected double[][] getBlocks(ImageWare input, int K1, int K2) {
        int[] size = input.getSize();
        int Nblk = size[0] * size[1] / (K1 * K2);
        double[][] blks = new double[Nblk][K1 * K2];
        int x = 0, y = 0, k = 0;
        while (x < size[0]) {
            y = 0;
            while (y < size[1]) {
                for (int i = 0; i < K1; i++) {
                    for (int j = 0; j < K2; j++) {
                        blks[k][i * K2 + j] = input.getPixel(x + i, y + j, 0);
                    }
                }
                y = y + K2;
                k++;
            }
            x = x + K1;
        }

        return blks;
    }

    /**
     * Test if n is odd.
     *
     * @param n 			the number of frames for denoising the current frame
     * @return true if n is odd, false otherwise
     */
    final static protected boolean isOdd(int n) {
        int m = 2 * (n / 2);
        return (m != n);
    }

    /**
     * Find the maximum number of dyadic iterations for a signal of size N.
     *
     * @param N 			the signal size
     * @return i 		the maximum number of dyadic iterations
     */
    final static protected int maxDyadicIters(int N) {
        int i = 0;
        while (!isOdd(N)) {
            N = N / 2;
            i++;
        }

        return i;
    }

    /**
     */
    final static protected int[] numOfDyadicIters(int nx, int ny) {
        int Ix = maxDyadicIters(nx), Iy = maxDyadicIters(ny);
        int[] J = new int[3];
        J[2] = 0;
        double log2 = Math.log(2.0);
        if (Ix == 0) {
            J[1] = (int) Math.min(Math.max(Math.floor(Math.log(ny / 256.0) / log2), 0), Iy);
        } else if (Iy == 0) {
            J[0] = (int) Math.min(Math.max(Math.floor(Math.log(nx / 256.0) / log2), 0), Ix);
        } else {
            J[0] = (int) Math.min(Math.max(Math.floor(Math.log(nx / 16.0) / log2), 0), Ix);
            J[1] = (int) Math.min(Math.max(Math.floor(Math.log(ny / 16.0) / log2), 0), Iy);
        }
        if (Math.max(J[0], J[1]) == 0) {
            return J;
        }
        double res = nx * ny / Math.pow(2.0, J[0] + J[1]);
        while (res >= 512) {
            if (!isOdd((int) (nx / Math.pow(2.0, J[0])))) {
                J[0] = J[0] + 1;
                res = res / 2.0;
            } else {
                if (!isOdd((int) (ny / Math.pow(2.0, J[1])))) {
                    J[1] = J[1] + 1;
                    res = res / 2.0;
                } else {
                    res = 0.0;
                }
            }
        }

        return J;
    }

    static protected void quickSort(double array[], int index[]) // pre: array is full, all elements are non-null integers
    // post: the array is sorted in ascending order
    {
        quickSort(array, index, 0, array.length - 1);              // quicksort all the elements in the array
    }

    static protected void quickSort(double array[], int index[], int start, int end) {
        int i = start;                          // index of left-to-right scan
        int k = end;                            // index of right-to-left scan

        if (end - start >= 1) // check that there are at least two elements to sort
        {
            double pivot = array[start];       // set the pivot as the first element in the partition

            while (k > i) // while the scan indices from left and right have not met,
            {
                while (array[i] <= pivot && i <= end && k > i) // from the left, look for the first
                {
                    i++;                                    // element greater than the pivot
                }
                while (array[k] > pivot && k >= start && k >= i) // from the right, look for the first
                {
                    k--;                                        // element not greater than the pivot
                }
                if (k > i) {                                       // if the left seekindex is still smaller than
                    swap(array, i, k);                      // the right index, swap the corresponding elements
                    swap(index, i, k);
                }
            }
            swap(array, start, k);          // after the indices have crossed, swap the last element in
            swap(index, start, k);		   // the left partition with the pivot
            quickSort(array, index, start, k - 1); // quicksort the left partition
            quickSort(array, index, k + 1, end);   // quicksort the right partition
        } else // if there is only one element in the partition, do not do any sorting
        {
            return;                     // the array is sorted, so exit
        }
    }

    static protected void swap(int array[], int index1, int index2) // pre: array is full and index1, index2 < array.length
    // post: the values at indices 1 and 2 have been swapped
    {
        int temp = array[index1];           // store the first value in a temp
        array[index1] = array[index2];      // copy the value of the second into the first
        array[index2] = temp;               // copy the value of the temp into the second
    }

    static protected void swap(double array[], int index1, int index2) // pre: array is full and index1, index2 < array.length
    // post: the values at indices 1 and 2 have been swapped
    {
        double temp = array[index1];           // store the first value in a temp
        array[index1] = array[index2];      // copy the value of the second into the first
        array[index2] = temp;               // copy the value of the temp into the second
    }

    /**
     */
    final static protected int[] createIndex(int I) {
        int[] index = new int[I];
        for (int i = 0; i < I; i++) {
            index[i] = i;
        }

        return index;
    }

    /**
     */
    final static protected int[] createIndex(int Is, int Ie) {
        int[] index = new int[Ie - Is + 1];
        for (int i = Is; i <= Ie; i++) {
            index[i - Is] = i;
        }

        return index;
    }

    /**
     */
    final static protected int[] restrictArray(int[] array, int Is, int Ie) {
        int[] output = new int[Ie - Is + 1];
        for (int i = Is; i <= Ie; i++) {
            output[i - Is] = array[i];
        }

        return output;
    }

    /**
     */
    final static protected double[] restrictArray(double[] array, int Is, int Ie) {
        double[] output = new double[Ie - Is + 1];
        for (int i = Is; i <= Ie; i++) {
            output[i - Is] = array[i];
        }

        return output;
    }

    /**
     */
    final static protected int[] getAdjacentIndex(int central, int total, int adjacent) {
        int[] index = new int[3];
        if (adjacent >= total) {
            index[0] = 0;
            index[1] = total - 1;
            index[2] = central;
            return index;
        }
        int half = (adjacent - 1) / 2;
        int[] temp = createIndex(central - half, central + half);
        index[2] = half;
        if (temp[0] < 0) {
            while (temp[0] < 0) {
                temp = add(temp, 1);
            }
            temp = restrictArray(temp, 0, central + half);
            index[2] = temp.length - half - 1;
        }
        if (temp[temp.length - 1] >= total) {
            while (temp[temp.length - 1] >= total) {
                temp = add(temp, -1);
            }
            temp = restrictArray(temp, temp.length - 1 - (total - central) - half + 1, temp.length - 1);
            index[2] = temp.length - (total - central);
        }
        index[0] = temp[0];
        index[1] = temp[temp.length - 1];

        return index;
    }

    /**
     */
    static final protected ImageWare convolveFIR2(ImageWare image, double[][] kernel) {
        int Nx = image.getSizeX();
        int Ny = image.getSizeY();
        int Kx = kernel.length;
        int Ky = kernel[0].length;
        double[][] neigh = new double[Kx][Ky];
        double pix = 0.0;
        ImageWare output = image.duplicate();
        for (int x = 0; x < Nx; x++) {
            for (int y = 0; y < Ny; y++) {
                image.getNeighborhoodXY(x, y, 0, neigh, ImageWare.PERIODIC);
                pix = 0.0;
                for (int i = 0; i < Kx; i++) {
                    for (int j = 0; j < Ky; j++) {
                        pix = pix + neigh[i][j] * kernel[i][j];
                    }
                }
                output.putPixel(x, y, 0, pix);
            }
        }

        return output;
    }
//
//    public static ImagePlus conv2ij(RealSignal signal) {
//		if (signal == null)
//			return null;
//
//		ImageStack stack = new ImageStack(signal.nx, signal.ny);
//		for (int k = 0; k < signal.nz; k++) {
//			ImageProcessor ip = new FloatProcessor(signal.nx, signal.ny, signal.getXY(k));
//			stack.addSlice(ip);
//		}
//		return new ImagePlus("", stack);
//	}

    
    /**
     */
    static final protected ImageWare createParent(ImageWare image, boolean[] offset) {
        int Nx = image.getSizeX();
        int Ny = image.getSizeY();
        int Nz = image.getSizeZ();
        ImageWare output = image.duplicate();
        double[] array = null;
        if (offset[0]) {
            array = new double[Nx];
            for (int y = 0; y < Ny; y++) {
                for (int z = 0; z < Nz; z++) {
                    output.getX(0, y, z, array);
                    array = convolve3(array, gdc);
                    output.putX(0, y, z, array);
                }
            }
        }
        if (offset[1]) {
            array = new double[Ny];
            for (int x = 0; x < Nx; x++) {
                for (int z = 0; z < Nz; z++) {
                    output.getY(x, 0, z, array);
                    array = convolve3(array, gdc);
                    output.putY(x, 0, z, array);
                }
            }
        }
        /*
        if(offset[2]){
        array = new double[Nz];
        for(int x=0;x<Nx;x++){
        for(int y=0;y<Ny;y++){
        output.getZ(x,y,0,array);
        array = convolve3(array,gdc);
        output.putZ(x,y,0,array);
        }
        }
        }
         */
        return output;
    }

    /**
     */
    static final protected double[] convolve3(double[] in, double[] kernel) {
        int N = in.length;
        double[] out = new double[N];
        out[0] = in[1] * kernel[0] + (kernel[1] + kernel[2]) * in[0];
        for (int n = 1; n < N - 1; n++) {
            out[n] = in[n - 1] * kernel[2] + in[n] * kernel[1] + in[n + 1] * kernel[0];
        }
        out[N - 1] = in[N - 2] * kernel[2] + (kernel[0] + kernel[1]) * in[N - 1];
        return out;
    }

    /**
     */
    static final protected ImageWare averageSubStack(ImageWare image, int zs, int ze) {
        int nx = image.getSizeX();
        int ny = image.getSizeY();
        ImageWare output = Builder.create(nx, ny, 1, ImageWare.DOUBLE);
        ImageWare buffer = Builder.create(nx, ny, 1, ImageWare.DOUBLE);
        for (int z = zs; z <= ze; z++) {
            image.getXY(0, 0, z, buffer);
            output.add(buffer);
        }
        output.divide(ze - zs + 1);

        return output;
    }

    /**
     */    
    static final public ImageWare symextend2D(ImageWare input, int Nx, int Ny) {
        int[] size = input.getSize();
        ImageWare output = Builder.create(Nx, Ny, 1, ImageWare.DOUBLE);
        int ex = (int)Math.IEEEremainder(size[0],2.0);
        int ey = (int)Math.IEEEremainder(size[1],2.0);
        int lx = (Nx-size[0])/2;
        int ly = (Ny-size[1])/2;
        output.putXY(lx, ly, 0, input);
        double[] array = new double[size[0]];
        //System.out.printf("ex=%d,ey=%d\n",ex,ey);
        for(int i=0;i<ly;i++){
            input.getX(0,ly-1-i,0,array);
            output.putX(lx,i,0,array);
            input.getX(0,size[1]-i-1,0,array);
            output.putX(lx,size[1]+ly+i,0,array);
        }
        if(ey!=0){
            input.getX(0,size[1]-ly-1,0,array);
            output.putX(lx,size[1]+2*ly,0,array);
        }
        array = new double[Ny];
        for(int i=0;i<lx;i++){
            output.getY(2*lx-1-i,0,0,array);
            output.putY(i,0,0,array);
            output.getY(size[0]+lx-i-1,0,0,array);
            output.putY(size[0]+lx+i,0,0,array);
        }
        if(ex!=0){
            output.getY(size[0]-1,0,0,array);
            output.putY(size[0]+2*lx,0,0,array);
        }
        //output.show("Extended Image");

        return output;
    }
    
    /**
     */
    static final public ImageWare symextend2D(ImageWare input, int Nx, int Ny, int[] Ext) {
        int[] size = input.getSize();
        ImageWare temp = Builder.create(size[0], size[1], 1, ImageWare.DOUBLE);
        ImageWare output = Builder.create(Nx, Ny, size[2], ImageWare.DOUBLE);
        for (int z = 0; z < size[2]; z++) {
            input.getXY(0, 0, z, temp);
            output.putXY(0, 0, z,Operations.symextend2D(temp, Nx, Ny));
        }
        Ext[0] = (Nx-size[0])/2;
        Ext[1] = (Ny-size[1])/2;
        
        return output;
    }

/**
     */
    static final public ImageWare crop2D(ImageWare input, int nx, int ny, int[] Ext) {
        int[] size = input.getSize();
        ImageWare output = Builder.create(nx, ny, size[2], ImageWare.DOUBLE);
        ImageWare temp = Builder.create(nx, ny, 1, ImageWare.DOUBLE);
        for (int z = 0; z < size[2]; z++) {
            input.getXY(Ext[0],Ext[1], z, temp);
            output.putXY(0, 0, z, temp);
        }
        return output;
    }

    public final static double[] estimateNoiseParams(ImageWare input, int CS) {
        int[] size = input.getSize();
        int Nx = (int)(Math.ceil((double)size[0]/Nmin)*Nmin);
        int Ny = (int)(Math.ceil((double)size[1]/Nmin)*Nmin);
        //System.out.printf("Nx=%d,Ny=%d\n",Nx,Ny);
        ImageWare in = symextend2D(input,Nx,Ny);
        int[] It = numOfDyadicIters(Nx,Ny);
        int K1 = (int) Math.pow(2.0, It[0] - 1), K2 = (int) Math.pow(2.0, It[1] - 1);
        //System.out.printf("K1=%d,K2=%d\n",K1,K2);
        int Nblk = Nx * Ny / (K1 * K2);
        //System.out.printf("Nblk=%d\n",Nblk);
        int wtype = 0; //0 = "huber", 1 = "bisquare";
        double Tol = 1;
        int L = (int) Math.floor(Tol * (Nblk));
        double alpha = 0.0, beta = 0.0, delta = 0.0, sigma = 0.0, sig = 0.0, d;
        int[] index = null;
        int[] offset = new int[2];
        double[] mean = new double[Nblk], sortedMean = new double[Nblk];
        double[] var = new double[Nblk];
        double[] rmean = new double[L], rvar = new double[L];
        double[] alphaBeta = new double[2];
        double[] noiseParams = new double[4];
        double[][] array = new double[Nx][Ny];
        java.util.Random rand = new java.util.Random(0);
        for (int cs = 0; cs < CS; cs++) {
            offset[0] = (int) (cs > 0 ? Math.floor(rand.nextDouble() * size[0]) : 0);
            offset[1] = (int) (cs > 0 ? Math.floor(rand.nextDouble() * size[1]) : 0);
            index = createIndex(Nblk);
            in.getBlockXY(offset[0], offset[1], 0, array, ImageWare.PERIODIC);
            computeBlkMean(array, mean, K1, K2, wtype);
            computeBlkVar(Builder.create(array, ImageWare.DOUBLE), var, K1, K2);
            System.arraycopy(mean, 0, sortedMean, 0, mean.length);
            quickSort(sortedMean, index);
            System.arraycopy(index, 0, index, 0, L);
            putIndexedValues(mean, rmean, index, L);
            putIndexedValues(var, rvar, index, L);
            alphaBeta = wlsFit(rmean, rvar, wtype);
            alpha = alpha + alphaBeta[0];
            beta = beta + alphaBeta[1];
            d = computeMode(restrictArray(rmean, 0, (int) Math.round(0.05 * L)));
            //d     = computeMode(rmean);
            delta = delta + d;
            d = computeMode(restrictArray(rvar, 0, (int) Math.round(0.05 * L)));
            //d     = computeMode(rvar);
            sig = Math.sqrt(d);
            //sig   = Math.sqrt(Math.max(d,Math.max(alphaBeta[1]+alphaBeta[0]*d,0)));
            sigma = sigma + sig;
        }
        noiseParams[0] = alpha / CS;
        noiseParams[1] = delta / CS;
        noiseParams[2] = sigma / CS;
        noiseParams[3] = beta / CS;

        return noiseParams;

    }

    final static protected double[] wlsFit(double[] x, double[] y, int wtype) {
        double[] params = new double[2];
        int I = (int) 5e3, N = x.length;
        double e = 0.0, Tol = 1e-6, d = 1.0, aux = 0.0, a0 = 1e9, b0 = 1e9;
        double sw2, sw2x, sw2y;
        double[] w = new double[N];
        double[] w2 = new double[N];
        double[] w2x = new double[N];
        double[] w2y = new double[N];
        double[] xy = new double[N];
        double[] f = new double[N];
        double[] r = new double[N];
        fill(w, 1.0);
        for (int i = 0; i < I; i++) {
            multiply(w, w, w2);
            multiply(w2, x, w2x);
            multiply(w2, y, w2y);
            multiply(x, y, xy);
            multiply(w2, xy, xy);
            sw2 = getSum(w2);
            sw2x = getSum(w2x);
            sw2y = getSum(w2y);
            params[0] = sw2 * getSum(xy) - sw2x * sw2y;
            multiply(x, x, xy);
            multiply(w2, xy, xy);
            aux = sw2 * getSum(xy) - sw2x * sw2x;
            params[0] = Math.abs(aux) < EPS ? a0 : params[0] / aux;
            params[1] = sw2y - params[0] * sw2x;
            params[1] = Math.abs(aux) < EPS ? b0 : params[1] / sw2;
            multiply(x, params[0], f);
            add(f, params[1], f);
            subtract(y, f, r);
            e = getMean(absolute(r));
            //IJ.write("Error = "+e+" a = "+params[0]+" b = "+params[1]+" delta = "+Math.abs(a0-params[0]));
            //-------------------------------------------------------
            if ((Math.abs(a0 - params[0]) < Tol && Math.abs(b0 - params[1]) < Tol) || e < Tol) {
                break;
            }
            a0 = params[0];
            b0 = params[1];
            //-------------------------------------------------------
            d = interqDist(r) + EPS;
            multiply(r, 1.0 / d, r);
            weightFun(r, wtype, w);
        }

        return params;
    }

    /**
     */
    final static protected double interqDist(double[] input) {
        int N = input.length;
        double[] sorted = new double[N];
        System.arraycopy(input, 0, sorted, 0, N);
        java.util.Arrays.sort(sorted);
        int m = (int) Math.floor((Math.floor((N + 1.0) / 2.0) + 1.0) / 2.0);
        double diq = sorted[N - m - 1] - sorted[m - 1];

        return diq;
    }

    /**
     */
    final static protected void weightFun(double[] x, int wtype, double[] w) {
        double p = 0.0;
        switch (wtype) {
            case 0:
                p = 0.75;
                for (int i = 0; i < x.length; i++) {
                    w[i] = Math.abs(x[i]) < p ? 1.0 : p / Math.abs(x[i]);
                }
                break;
            case 1:
            default:
                p = 3.5;
                for (int i = 0; i < x.length; i++) {
                    w[i] = Math.abs(x[i]) > p ? 0.0 : (p * p - x[i] * x[i]) * (p * p - x[i] * x[i]) / (p * p * p * p);
                }
                break;
        }
    }

    final static protected double robustVarEstimate(double[] array) {
        subtract(array, getMedian(array), array);
        absolute(array, array);
        double sig = 1.4826 * getMedian(array);

        return (sig * sig);
    }

    final static protected double robustMeanEstimate(double[] x, int wtype) {
        int I = (int) 5e3, N = x.length;
        double e = 0.0, Tol = 1e-3, d = 1.0;
        double[] w = new double[N];
        double[] r = new double[N];
        double m = 0.0, m0 = 1e9, aux = 0.0;
        fill(w, 1.0);
        for (int i = 0; i < I; i++) {
            multiply(w, x, r);
            m = getSum(r);
            aux = getSum(w);
            m = Math.abs(aux) < EPS ? m0 : m / aux;
            subtract(x, m, r);
            e = getMean(absolute(r));
            //-------------------------------------------------------
            if (Math.abs(m0 - m) < Tol || e < Tol) {
                break;
            }
            m0 = m;
            //-------------------------------------------------------
            d = interqDist(r) + EPS;
            multiply(r, 1.0 / d, r);
            weightFun(r, wtype, w);
        }

        return m;
    }
} // end of class

