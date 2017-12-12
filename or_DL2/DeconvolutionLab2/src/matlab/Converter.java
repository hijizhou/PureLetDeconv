package matlab;

import ij.ImagePlus;
import ij.ImageStack;
import ij.process.ByteProcessor;
import ij.process.ColorProcessor;
import ij.process.FloatProcessor;
import ij.process.ShortProcessor;
import signal.RealSignal;

public class Converter {

	public static boolean verbose;

	/**
	 * Get an image.
	 *
	 * @param imageplus	image
	 * @return an N x M array representing the input image
	 */
	public static Object get(ImagePlus imageplus) {
		if (imageplus == null)
			return null;
		int width = imageplus.getWidth();
		int height = imageplus.getHeight();
		int stackSize = imageplus.getStackSize();
		int counter = 0;
		ImageStack imagestack = imageplus.getStack();
		switch (imageplus.getType()) {

		case ImagePlus.COLOR_256: {
			;
		}
		case ImagePlus.GRAY8: {
			short[][][] is = new short[height][width][stackSize];
			for (int sz = 0; sz < stackSize; sz++) {
				ByteProcessor byteprocessor = (ByteProcessor) imagestack.getProcessor(sz + 1);
				byte[] pixels = (byte[]) byteprocessor.getPixels();
				counter = 0;
				int h = 0;
				while (h < height) {
					int w = 0;
					while (w < width) {
						is[h][w][sz] = (short)(pixels[counter]&0xff);
						w++;
						counter++;
					}
					counter = ++h * width;
				}
			}
			return is;
		}
		case ImagePlus.GRAY16: {
			int[][][] is = new int[height][width][stackSize];
			for (int sz = 0; sz < stackSize; sz++) {
				counter = 0;
				ShortProcessor shortprocessor = (ShortProcessor) imagestack.getProcessor(sz + 1);
				short[] spixels = (short[]) shortprocessor.getPixels();
				int h = 0;
				while (h < height) {
					int w = 0;
					while (w < width) {
						is[h][w][sz] = (int)(spixels[counter]&0xffff);
						w++;
						counter++;
					}
					counter = ++h * width;
				}
			}
			return is;
		}
		case ImagePlus.GRAY32: {
			double[][][] fs = new double[height][width][stackSize];
			for (int sz = 0; sz < stackSize; sz++) {
				FloatProcessor floatprocessor = (FloatProcessor) imagestack.getProcessor(sz + 1);
				float[] fpixels = (float[]) floatprocessor.getPixels();
				counter = 0;
				int i = 0;
				while (i < height) {
					int j = 0;
					while (j < width) {
						fs[i][j][sz] = (double) fpixels[counter];
						j++;
						counter++;
					}
					counter = ++i * width;
				}
			}
			return fs;
		}
		case ImagePlus.COLOR_RGB: {
			if (stackSize == 1) {
				short[][][] is = new short[height][width][3];
				ColorProcessor colorprocessor = (ColorProcessor) imagestack.getProcessor(1);
				byte[] red = new byte[width * height];
				byte[] green = new byte[width * height];
				byte[] blue = new byte[width * height];
				colorprocessor.getRGB(red, green, blue);
				counter = 0;
				int h = 0;
				while (h < height) {
					int w = 0;
					while (w < width) {
						is[h][w][0] = (short)(red[counter]&0xff);
						is[h][w][1] = (short)(green[counter]&0xff);
						is[h][w][2] = (short)(blue[counter]&0xff);
						w++;
						counter++;
					}
					counter = ++h * width;
				}
				return is;
			}
			short[][][][] is = new short[height][width][stackSize][3];
			for (int sz = 0; sz < stackSize; sz++) {
				ColorProcessor colorprocessor  = (ColorProcessor) imagestack.getProcessor(sz + 1);
				byte[] red = new byte[width * height];
				byte[] green = new byte[width * height];
				byte[] blue = new byte[width * height];
				colorprocessor.getRGB(red, green, blue);
				counter = 0;
				int h = 0;
				while (h < height) {
					int w = 0;
					while (w < width) {
						is[h][w][sz][0] = (short)red[counter];
						is[h][w][sz][1] = (short)green[counter];
						is[h][w][sz][2] = (short)blue[counter];
						w++;
						counter++;
					}
					counter = ++h * width;
				}
			}
			return is;
		}
		default:
			System.out.println("MIJ Error message: Unknow type of volumes.");
			return null;
		}
	}
	
	/**
	 * Create a new image in ImageJ from a Matlab variable with a specified
	 * title.
	 * 
	 * This method try to create a image (ImagePlus of ImageJ) from a Matlab's
	 * variable which should be an 2D or 3D array The recognize type are byte,
	 * short, int, float and double. The dimensionality of the 2 (image) or 3
	 * (stack of images)
	 * 
	 * @param title
	 *            title of the new image
	 * @param object
	 *            Matlab variable
	 * @param showImage
	 *            Whether to display the newly created image or not
	 * @return the resulting ImagePlus instance
	 */
	public static ImagePlus createImage(String title, Object object, boolean showImage) {
		ImagePlus imp = null;
		int i = 0;
		if (object instanceof byte[][]) {
			byte[][] is = (byte[][]) object;
			int height = is.length;
			int width = is[0].length;
			ByteProcessor byteprocessor = new ByteProcessor(width, height);
			byte[] bp = (byte[]) byteprocessor.getPixels();
			int h = 0;
			while (h < height) {
				int w = 0;
				while (w < width) {
					bp[i] = is[h][w];
					w++;
					i++;
				}
				i = ++h * width;
			}
			imp = new ImagePlus(title, byteprocessor);

		}
		else if (object instanceof short[][]) {
			short[][] is = (short[][]) object;
			int height = is.length;
			int width = is[0].length;
			ShortProcessor shortprocessor = new ShortProcessor(width, height);
			short[] sp = (short[]) shortprocessor.getPixels();
			int h = 0;
			while (h < height) {
				int w = 0;
				while (w < width) {
					sp[i] = is[h][w];
					w++;
					i++;
				}
				i = ++h * width;
			}
			imp = new ImagePlus(title, shortprocessor);

		}
		else if (object instanceof int[][]) {
			if (verbose)
				System.out.println("MIJ warning message: Loss of precision: convert int 32-bit to short 16-bit");
			int[][] is = (int[][]) object;
			int height = is.length;
			int width = is[0].length;
			ShortProcessor shortprocessor = new ShortProcessor(width, height);
			short[] sp = (short[]) shortprocessor.getPixels();
			int h = 0;
			while (h < height) {
				int w = 0;
				while (w < width) {
					sp[i] = (short) is[h][w];
					w++;
					i++;
				}
				i = ++h * width;
			}
			imp = new ImagePlus(title, shortprocessor);
		}
		else if (object instanceof float[][]) {
			float[][] fs = (float[][]) object;
			int height = fs.length;
			int width = fs[0].length;
			FloatProcessor floatprocessor = new FloatProcessor(width, height);
			float[] fp = (float[]) floatprocessor.getPixels();
			int h = 0;
			while (h < height) {
				int w = 0;
				while (w < width) {
					fp[i] = fs[h][w];
					w++;
					i++;
				}
				i = ++h * width;
			}
			floatprocessor.resetMinAndMax();
			imp = new ImagePlus(title, floatprocessor);

		}
		else if (object instanceof double[][]) {
			if (verbose)
				System.out.println("MIJ warning message: Loss of precision: convert double 32-bit to float 32-bit");
			double[][] ds = (double[][]) object;
			int height = ds.length;
			int width = ds[0].length;
			FloatProcessor floatprocessor = new FloatProcessor(width, height);
			float[] fp = (float[]) floatprocessor.getPixels();
			int h = 0;
			while (h < height) {
				int w = 0;
				while (w < width) {
					fp[i] = (float) ds[h][w];
					w++;
					i++;
				}
				i = ++h * width;
			}
			floatprocessor.resetMinAndMax();
			imp = new ImagePlus(title, floatprocessor);

		}
		else if (object instanceof byte[][][]) {
			byte[][][] is = (byte[][][]) object;
			int height = is.length;
			int width = is[0].length;
			int stackSize = is[0][0].length;
			ImageStack imagestack = new ImageStack(width, height);
			for (int sz = 0; sz < stackSize; sz++) {
				ByteProcessor byteprocessor = new ByteProcessor(width, height);
				byte[] bp = (byte[]) byteprocessor.getPixels();
				i = 0;
				int h = 0;
				while (h < height) {
					int w = 0;
					while (w < width) {
						bp[i] = is[h][w][sz];
						w++;
						i++;
					}
					i = ++h * width;
				}
				imagestack.addSlice("", byteprocessor);
			}
			imp = new ImagePlus(title, imagestack);

		}
		else if (object instanceof short[][][]) {
			short[][][] is = (short[][][]) object;
			int height = is.length;
			int width = is[0].length;
			int stackSize = is[0][0].length;
			ImageStack imagestack = new ImageStack(width, height);
			for (int sz = 0; sz < stackSize; sz++) {
				ShortProcessor shortprocessor = new ShortProcessor(width, height);
				short[] sp = (short[]) shortprocessor.getPixels();
				i = 0;
				int h = 0;
				while (h < height) {
					int w = 0;
					while (w < width) {
						sp[i] = is[h][w][sz];
						w++;
						i++;
					}
					i = ++h * width;
				}
				imagestack.addSlice("", shortprocessor);
			}
			imp = new ImagePlus(title, imagestack);

		}
		else if (object instanceof int[][][]) {
			if (verbose)
				System.out.println("MIJ warning message: Loss of precision: convert int 32 bits to short 16 bits");
			int[][][] is = (int[][][]) object;
			int height = is.length;
			int width = is[0].length;
			int stackSize = is[0][0].length;
			ImageStack imagestack = new ImageStack(width, height);
			for (int sz = 0; sz < stackSize; sz++) {
				ShortProcessor shortprocessor = new ShortProcessor(width, height);
				short[] sp = (short[]) shortprocessor.getPixels();
				i = 0;
				int h = 0;
				while (h < height) {
					int w = 0;
					while (w < width) {
						sp[i] = (short) is[h][w][sz];
						w++;
						i++;
					}
					i = ++h * width;
				}
				if (sz == 0)
					shortprocessor.resetMinAndMax();
				imagestack.addSlice("", shortprocessor);

			}
			imp = new ImagePlus(title, imagestack);

		}
		else if (object instanceof float[][][]) {
			float[][][] fs = (float[][][]) object;
			int height = fs.length;
			int width = fs[0].length;
			int stackSize = fs[0][0].length;
			ImageStack imagestack = new ImageStack(width, height);
			for (int sz = 0; sz < stackSize; sz++) {
				FloatProcessor floatprocessor = new FloatProcessor(width, height);
				float[] fp = (float[]) floatprocessor.getPixels();
				i = 0;
				int h = 0;
				while (h < height) {
					int w = 0;
					while (w < width) {
						fp[i] = fs[h][w][sz];
						w++;
						i++;
					}
					i = ++h * width;
				}
				if (sz == 0)
					floatprocessor.resetMinAndMax();
				imagestack.addSlice("", floatprocessor);
			}
			imp = new ImagePlus(title, imagestack);

		}
		else if (object instanceof double[][][]) {
			if (verbose)
				System.out.println("MIJ warning message: Loss of precision: convert double 32-bit to float 32-bit");
			double[][][] ds = (double[][][]) object;
			int height = ds.length;
			int width = ds[0].length;
			int stackSize = ds[0][0].length;
			ImageStack imagestack = new ImageStack(width, height);
			for (int sz = 0; sz < stackSize; sz++) {
				FloatProcessor floatprocessor = new FloatProcessor(width, height);
				float[] fp = (float[]) floatprocessor.getPixels();
				i = 0;
				int h = 0;
				while (h < height) {
					int w = 0;
					while (w < width) {
						fp[i] = (float) ds[h][w][sz];
						w++;
						i++;
					}
					i = ++h * width;
				}
				if (sz == 0)
					floatprocessor.resetMinAndMax();
				imagestack.addSlice("", floatprocessor);
			}
			imp = new ImagePlus(title, imagestack);

		}
		else {
			System.out.println("MIJ Error message: Unknow type of images or volumes.");
			return null;
		}

		if (showImage) {
			imp.show();
			imp.updateAndDraw();
		}
		return imp;
	}
	
	/**
	 * Create a new RealSignal from a Matlab variable with a specified
	 * title.
	 */
	public static RealSignal createRealSignal(Object object) {
		RealSignal signal = null;
		if (object instanceof byte[][]) {
			byte[][] data = (byte[][]) object;
			int h = data.length;
			int w = data[0].length;
			signal = new RealSignal("Matlab-byte2D", h, w, 1);
			for(int i=0; i<h; i++)
			for(int j=0; j<w; j++)
				signal.data[0][i+j*h]= data[i][j];
			return signal;
		}
		if (object instanceof short[][]) {
			short[][] data = (short[][]) object;
			int h = data.length;
			int w = data[0].length;
			signal = new RealSignal("Matlab-short2D", h, w, 1);
			for(int i=0; i<h; i++)
			for(int j=0; j<w; j++)
				signal.data[0][i+j*h]= data[i][j];
			return signal;
		}
		
		if (object instanceof int[][]) {
			int[][] data = (int[][]) object;
			int h = data.length;
			int w = data[0].length;
			signal = new RealSignal("Matlab-int2D", h, w, 1);
			for(int i=0; i<h; i++)
			for(int j=0; j<w; j++)
				signal.data[0][i+j*h]= data[i][j];
			return signal;
		}
		
		if (object instanceof float[][]) {
			float[][] data = (float[][]) object;
			int h = data.length;
			int w = data[0].length;
			signal = new RealSignal("Matlab-float3D", h, w, 1);
			for(int i=0; i<h; i++)
			for(int j=0; j<w; j++)
				signal.data[0][i+j*h]= data[i][j];
			return signal;
		}
		
		if (object instanceof double[][]) {
			double[][] data = (double[][]) object;
			int h = data.length;
			int w = data[0].length;
			signal = new RealSignal("Matlab-double2D", h, w, 1);
			for(int i=0; i<h; i++)
			for(int j=0; j<w; j++)
				signal.data[0][i+j*h]= (float)data[i][j];
			return signal;
		}
		
		if (object instanceof byte[][][]) {
			byte[][][] data = (byte[][][]) object;
			int h = data.length;
			int w = data[0].length;
			int d = data[0][0].length;
			signal = new RealSignal("Matlab-byte3D", h, w, d);
			for(int k=0; k<d; k++)
			for(int i=0; i<h; i++)
			for(int j=0; j<w; j++)
				signal.data[k][i+j*h]= (float)data[i][j][k];
			return signal;
		}
			
		if (object instanceof short[][][]) {
			short[][][] data = (short[][][]) object;
			int h = data.length;
			int w = data[0].length;
			int d = data[0][0].length;
			signal = new RealSignal("Matlab-short3D", h, w, d);
			for(int k=0; k<d; k++)
			for(int i=0; i<h; i++)
			for(int j=0; j<w; j++)
				signal.data[k][i+j*h]= (float)data[i][j][k];
			return signal;
		}
		
		if (object instanceof int[][][]) {
			int[][][] data = (int[][][]) object;
			int h = data.length;
			int w = data[0].length;
			int d = data[0][0].length;
			signal = new RealSignal("Matlab-int3D", h, w, d);
			for(int k=0; k<d; k++)
			for(int i=0; i<h; i++)
			for(int j=0; j<w; j++)
				signal.data[k][i+j*h]= (float)data[i][j][k];
			return signal;
		}
		
		if (object instanceof float[][][]) {
			float[][][] data = (float[][][]) object;
			int h = data.length;
			int w = data[0].length;
			int d = data[0][0].length;
			signal = new RealSignal("Matlab-float3D", h, w, d);
			for(int k=0; k<d; k++)
			for(int i=0; i<h; i++)
			for(int j=0; j<w; j++)
				signal.data[k][i+j*h]= data[i][j][k];
			return signal;
		}
		
		if (object instanceof double[][][]) {
			double[][][] data = (double[][][]) object;
			int h = data.length;
			int w = data[0].length;
			int d = data[0][0].length;
			signal = new RealSignal("Matlab-double3D", h, w, d);
			for(int k=0; k<d; k++)
			for(int i=0; i<h; i++)
			for(int j=0; j<w; j++)
				signal.data[k][i+j*h]= (float)data[i][j][k];
			return signal;
		}
		
		return null;
	}

	public static Object createObject(RealSignal signal) {
		if (signal == null)
			return null;
		int nx = signal.nx;
		int ny = signal.ny;
		int nz = signal.nz;
		double[][][] object = new double[ny][nx][nz];
		for(int k=0; k<nz; k++)
		for(int i=0; i<nx; i++)
		for(int j=0; j<ny; j++)
			object[j][i][k] = signal.data[k][i+j*nx];
		return object;
	}
	
}
