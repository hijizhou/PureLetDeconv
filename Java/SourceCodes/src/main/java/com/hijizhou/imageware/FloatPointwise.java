package com.hijizhou.imageware;

import ij.ImageStack;
import ij.process.FloatProcessor;

import java.awt.*;
import java.util.Random;

/**
 * Class FloatPointwise.
 *
 * @author	Daniel Sage
 *			Biomedical Imaging Group
 *			Ecole Polytechnique Federale de Lausanne, Lausanne, Switzerland
 */

public class FloatPointwise extends FloatAccess implements Pointwise {

	//------------------------------------------------------------------
	//
	//	Constructors section
	//
	//------------------------------------------------------------------
	protected FloatPointwise(int nx, int ny, int nz) 		{ super(nx, ny, nz); }
	protected FloatPointwise(Image image, int mode) 		{ super(image, mode); }

	protected FloatPointwise(ImageStack stack, int mode) 	{ super(stack, mode); }
	protected FloatPointwise(ImageStack stack, byte chan) 		{ super(stack, chan); }

	protected FloatPointwise(byte[] array, int mode) 		{ super(array, mode); }
	protected FloatPointwise(byte[][] array, int mode) 		{ super(array, mode); }
	protected FloatPointwise(byte[][][] array, int mode) 	{ super(array, mode); }
	protected FloatPointwise(short[] array, int mode) 		{ super(array, mode); }
	protected FloatPointwise(short[][] array, int mode) 	{ super(array, mode); }
	protected FloatPointwise(short[][][] array, int mode) 	{ super(array, mode); }
	protected FloatPointwise(float[] array, int mode) 		{ super(array, mode); }
	protected FloatPointwise(float[][] array, int mode) 	{ super(array, mode); }
	protected FloatPointwise(float[][][] array, int mode) 	{ super(array, mode); }
	protected FloatPointwise(double[] array, int mode) 		{ super(array, mode); }
	protected FloatPointwise(double[][] array, int mode) 	{ super(array, mode); }
	protected FloatPointwise(double[][][] array, int mode)	{ super(array, mode); }

	/**
	* Fill this imageware with a constant value.
	*
	* @param	value	the constant value
	*/
	@Override
	public void fillConstant(double value) {
		float typedValue = (float)value;
		float[] slice = null;
		for(int z=0; z<nz; z++) {
			slice = (float[])data[z];
			for(int k=0; k<nxy; k++)
				slice[k] = typedValue;
		}
	}

	/**
	* Fill this imageware with ramp.
	*/
	@Override
	public void fillRamp() {
		int off = 0;
		float[] slice = null;
		for(int z=0; z<nz; z++) {
			slice = (float[])data[z];
			for(int k=0; k<nxy; k++)
				slice[k] = off+k;
			off += nxy;
		}
	}
	/**
	* Generate a gaussian noise with a range [-amplitude..amplitude].
	*
	* @param amplitude		amplitude of the noise
	*/
	@Override
	public void fillGaussianNoise(double amplitude) {
		Random rnd = new Random();
		float[] slice = null;
		for(int z=0; z<nz; z++) {
			slice = (float[])data[z];
			for(int k=0; k<nxy; k++) {
				slice[k] = (float)((rnd.nextGaussian())*amplitude);
			}
		}
	}
	
	/**
	* Generate a uniform noise with a range [-amplitude..amplitude].
	*
	* @param amplitude		amplitude of the noise
	*/
	@Override
	public void fillUniformNoise(double amplitude) {
		Random rnd = new Random();
		float[] slice = null;
		amplitude *= 2.0;
		for(int z=0; z<nz; z++) {
			slice = (float[])data[z];
			for(int k=0; k<nxy; k++) {
				slice[k] = (float)((rnd.nextDouble()-0.5)*amplitude);
			}
		}
	}
	
	/**
	* Generate a salt and pepper noise.
	*
	* @param	amplitudeSalt		amplitude of the salt noise
	* @param	amplitudePepper		amplitude of the pepper noise
	* @param	percentageSalt		percentage of the salt noise
	* @param	percentagePepper	percentage of the pepper noise
	*/
	@Override
	public void fillSaltPepper(double amplitudeSalt, double amplitudePepper, double percentageSalt, double percentagePepper) {
		Random rnd = new Random();
		int index, z;
		if (percentageSalt > 0) {
			double nbSalt = nxy*nz/percentageSalt;
			for( int k=0; k < nbSalt; k++) {
				index = (int)(rnd.nextDouble() * nxy);
				z = (int)(rnd.nextDouble() * nz);
				((float[])data[z])[index] = (float)(rnd.nextDouble()*amplitudeSalt);
			}
		}
		if (percentagePepper > 0) {
			double nbPepper = nxy*nz/percentagePepper;
			for( int k=0; k < nbPepper; k++) {
				index = (int)(rnd.nextDouble() * nxy);
				z = (int)(rnd.nextDouble() * nz);
				((float[])data[z])[index] = (float)(-rnd.nextDouble()*amplitudeSalt);
			}
		}	
	}
	


	/**
	* Build an ImageStack of ImageJ.
	*/
	@Override
	public ImageStack buildImageStack() {
		ImageStack imagestack = new ImageStack(nx, ny);
		for (int z=0; z<nz; z++) {
			FloatProcessor ip = new FloatProcessor(nx, ny);
			float pix[] = (float[])ip.getPixels();
			for (int k=0; k<nxy; k++)
				pix[k] = (((float[])data[z])[k]);
			imagestack.addSlice("" + z, ip);
		}
		return imagestack;
	}


	/**
	* Invert the pixel intensity.
	*/
	@Override
	public void invert() {
		double max = -Double.MAX_VALUE;
		float[] slice;
		for(int z=0; z<nz; z++) {
			slice = (float[])data[z];
			for(int k=0; k<nxy; k++) {
				if ((slice[k]) > max) 
					max = slice[k];
			}
		}
		for(int z=0; z<nz; z++) {
			slice = (float[])data[z];
			for(int k=0; k<nxy; k++) {
				slice[k] = (float)(max-((slice[k])));
			}
		}
	}

	/**
	* Negate the pixel intensity.
	*/
	@Override
	public void negate() {
		float[] slice;
		for(int z=0; z<nz; z++) {
			slice = (float[])data[z];
			for(int k=0; k<nxy; k++) {
				slice[k] = (float)(-((double)(slice[k])));
			}
		}
	}

	/**
	* Clip the pixel intensity into [0..255].
	*/
	@Override
	public void clip() {
		clip(0.0, 255.0);
	}

	/**
	* Clip the pixel intensity into [minLevel..maxLevel].
	*
	* @param minLevel		double value given the threshold 
	* @param maxLevel		double value given the threshold 
	*/
	@Override
	public void clip(double minLevel, double maxLevel) {
		float[] slice;
		for(int z=0; z<nz; z++) {
			slice = (float[])data[z];
			float value;
			float min = (float)minLevel;
			float max = (float)maxLevel;
			for(int k=0; k<nxy; k++) {
				value = (slice[k]);
				if (value < min)
					slice[k] = min;
				if (value > max)
					slice[k] = max;
			}
		}
	}
	
	/**
	* Rescale the pixel intensity into [0..255].
	*/
	@Override
	public void rescale() {
		double maxImage = -Double.MAX_VALUE;
		double minImage =  Double.MAX_VALUE;
		float[] slice;
		for(int z=0; z<nz; z++) {
			slice = (float[])data[z];
			for(int k=0; k<nxy; k++) {
				if ((slice[k]) > maxImage) 
					maxImage = slice[k];
				if ((slice[k]) < minImage) 
					minImage = slice[k];
			}
		}
		double a;
		if (minImage-maxImage == 0) {
			a = 1.0;
			minImage = 128.0;
		}
		else {
			a = 255.0 / (maxImage-minImage);
		}
		for(int z=0; z<nz; z++) {
			slice = (float[])data[z];
			for(int k=0; k<nxy; k++) {
				slice[k] = (float)(a*(((slice[k]))-minImage));
			}
		}
	}
	
	/**
	* Rescale the pixel intensity into [minLevel..maxLevel].
	*
	* @param minLevel		double value given the threshold 
	* @param maxLevel		double value given the threshold 
	*/
	@Override
	public void rescale(double minLevel, double maxLevel) {
		double maxImage = -Double.MAX_VALUE;
		double minImage =  Double.MAX_VALUE;
		float[] slice;
		for(int z=0; z<nz; z++) {
			slice = (float[])data[z];
			for(int k=0; k<nxy; k++) {
				if ((slice[k]) > maxImage) 
					maxImage = slice[k];
				if ((slice[k]) < minImage) 
					minImage = slice[k];
			}
		}
		double a;
		if (minImage-maxImage == 0) {
			a = 1.0;
			minImage = (maxLevel-minLevel)/2.0;
		}
		else {
			a = (maxLevel-minLevel) / (maxImage-minImage);
		}
		for(int z=0; z<nz; z++) {
			slice = (float[])data[z];
			for(int k=0; k<nxy; k++) {
				slice[k] = (float)(a*(((slice[k]))-minImage) + minLevel);
			}
		}
	}

	/**
	* Rescale the pixel intensity with a linear curve passing through 
	* (maxLevel-minLevel)/2 at the 0 input intensity.
	*
	* @param minLevel		double value given the threshold 
	* @param maxLevel		double value given the threshold 
	*/
	@Override
	public void rescaleCenter(double minLevel, double maxLevel) {
		double maxImage = -Double.MAX_VALUE;
		double minImage =  Double.MAX_VALUE;
		float[] slice;
		for(int z=0; z<nz; z++) {
			slice = (float[])data[z];
			for(int k=0; k<nxy; k++) {
				if ((slice[k]) > maxImage) 
					maxImage = slice[k];
				if ((slice[k]) < minImage) 
					minImage = slice[k];
			}
		}
		double center = (maxLevel+minLevel)/2.0;
		double a;
		if ( minImage-maxImage == 0) {
			a = 1.0;
			minImage = (maxLevel-minLevel)/2.0;
		}
		else {
			if ( Math.abs(maxImage) > Math.abs(minImage) )
				a = (maxLevel-center) / Math.abs(maxImage);
			else
				a = (center-minLevel) / Math.abs(minImage);
		}
		for(int z=0; z<nz; z++) {
			slice = (float[])data[z];
			for(int k=0; k<nxy; k++) {
				slice[k] = (float)(a*(((slice[k]))-minImage) + center);
			}
		}
	}
	
	/**
	* Compute the absolute value of this imageware.
	*/
	@Override
	public void abs() {
		float zero = (float)0.0;
		float[] slice;
		for(int z=0; z<nz; z++) {
			slice = (float[])data[z];
			for(int k=0; k<nxy; k++) {
				if (slice[k] < zero)
					slice[k] = -slice[k];
			}
		}
	}

	/**
	* Compute the log of this imageware.
	*/
	@Override
	public void log() {
		float[] slice;
		for(int z=0; z<nz; z++) {
			slice = (float[])data[z];
			for(int k=0; k<nxy; k++) {
				slice[k] = (float)Math.log(slice[k]);
			}
		}
	}
	
	/**
	* Compute the exponential of this imageware.
	*/
	@Override
	public void exp() {
		float[] slice;
		for(int z=0; z<nz; z++) {
			slice = (float[])data[z];
			for(int k=0; k<nxy; k++) {
				slice[k] = (float)Math.exp(slice[k]);
			}
		}
	}

	/**
	* Compute the square root of this imageware.
	*/
	@Override
	public void sqrt() {
		float[] slice;
		for(int z=0; z<nz; z++) {
			slice = (float[])data[z];
			for(int k=0; k<nxy; k++) {
				slice[k] = (float)Math.sqrt(slice[k]);
			}
		}
	}

	/**
	* Compute the square of this imageware.
	*/
	@Override
	public void sqr() {
		float[] slice;
		for(int z=0; z<nz; z++) {
			slice = (float[])data[z];
			for(int k=0; k<nxy; k++) {
				slice[k] *= slice[k];
			}
		}
	}

	/**
	* Compute the power of a of this imageware.
	*
	* @param	a	exponent
	*/
	@Override
	public void pow(double a) {
		float[] slice;
		for(int z=0; z<nz; z++) {
			slice = (float[])data[z];
			for(int k=0; k<nxy; k++) {
				slice[k] = (float)Math.pow(slice[k], a);
			}
		}
	}
	
	/**
	* Add a constant value to this imageware.
	*/
	@Override
	public void add(double constant) {
		float cst = (float)constant;
		float[] slice;
		for(int z=0; z<nz; z++) {
			slice = (float[])data[z];
			for(int k=0; k<nxy; k++) {
				slice[k] += cst;
			}
		}
	}
	
	/**
	* Multiply a constant value to this imageware.
	*
	* @param	constant	the constant value
	*/
	@Override
	public void multiply(double constant) {
		float cst = (float)constant;
		float[] slice;
		for(int z=0; z<nz; z++) {
			slice = (float[])data[z];
			for(int k=0; k<nxy; k++) {
				slice[k] *= cst;
			}
		}
	}
	
	/**
	* Subtract a constant value to this imageware.
	*
	* @param	constant	the constant value
	*/
	@Override
	public void subtract(double constant) {
		float cst = (float)constant;
		float[] slice;
		for(int z=0; z<nz; z++) {
			slice = (float[])data[z];
			for(int k=0; k<nxy; k++) {
				slice[k] -= cst;
			}
		}
	}
	
	/**
	* Divide by a constant value to this imageware.
	*
	* @param	constant	the constant value
	*/
	@Override
	public void divide(double constant) {
		if (constant == 0.0)
			throw new ArrayStoreException(
				"\n-------------------------------------------------------\n" +
				"Error in imageware package\n" +
				"Unable to divide because the constant is 0.\n" +
				"-------------------------------------------------------\n"
			);
		float cst = (float)constant;
		float[] slice;
		for(int z=0; z<nz; z++) {
			slice = (float[])data[z];
			for(int k=0; k<nxy; k++) {
				slice[k] /= cst;
			}
		}
	}

	/**
	* Threshold a imageware in two levels 0 and 255.
	*
	* All the pixels values strictly greater than 'thresholdValue' and are set
	* to 0. The remaining values are set to 255.
	*
	* @param thresholdValue		double value given the threshold 
	*/
	@Override
	public void threshold(double thresholdValue) {
		threshold(thresholdValue, 0.0, 255.0);
	}

	/**
	* Threshold a imageware in two levels minLevel and maxLevel.
	*
	* All the pixels values strictly greater than 'thresholdValue' and are set
	* to maxLevel. The remaining values are set to minLevel.
	*
	* @param thresholdValue		double value given the threshold 
	* @param minLevel			double value given the minimum level 
	* @param maxLevel			double value given the maximum level 
	*/
	@Override
	public void threshold(double thresholdValue, double minLevel, double maxLevel) {
		float low = (float)(minLevel);
		float high = (float)(maxLevel);
		float[] slice;
		for(int z=0; z<nz; z++) {
			slice = (float[])data[z];
			for(int k=0; k<nxy; k++) {
				slice[k] = ((slice[k]) > thresholdValue ? high : low);
			}
		}
	}

	/**
	* Apply a soft thresholding.
	*
	* All the pixels values strictly greater than '-thresholdValue' and stricty 
	* lower than 'thresholdValue' set to 0. The remaining positive values are 
	* reduced by 'thresholdvalue'; the remaining negative values are 
	* augmented by 'thresholdValue'.
	*
	* @param thresholdValue		double value given the threshold 
	*/
	@Override
	public void thresholdSoft(double thresholdValue) {
		float zero = (float)(0.0);
		double pixel;
		float[] slice;
		for(int z=0; z<nz; z++) {
			slice = (float[])data[z];
			for(int k=0; k<nxy; k++) {
				pixel = (slice[k]);
				slice[k] = (pixel<=-thresholdValue ? 
					(float)(pixel+thresholdValue) : 
					(pixel>thresholdValue ? (float)(pixel-thresholdValue) : zero));
			}
		}
	}
	
	/**
	* Apply a hard thresholding.
	*
	* All the pixels values strictly greater than '-thresholdValue' and stricty 
	* lower than 'thresholdValue' are set to 0. The remaining values are 
	* unchanged.
	*
	* @param thresholdValue		double value given the threshold 
	*/
	@Override
	public void thresholdHard(double thresholdValue) {
		float zero = (float)(0.0);
		double pixel;
		float[] slice;
		for(int z=0; z<nz; z++) {
			slice = (float[])data[z];
			for(int k=0; k<nxy; k++) {
				pixel = (slice[k]);
				if (pixel > -thresholdValue && pixel < thresholdValue)
					slice[k] =  zero; 
			}
		}
	}
	
	/**
	* Add a gaussian noise with a range [-amplitude..amplitude].
	*
	* @param amplitude		amplitude of the noise
	*/
	@Override
	public void addGaussianNoise(double amplitude) {
		Random rnd = new Random();
		float[] slice = null;
		for(int z=0; z<nz; z++) {
			slice = (float[])data[z];
			for(int k=0; k<nxy; k++) {
				slice[k] += (float)((rnd.nextGaussian())*amplitude);
			}
		}
	}

	/**
	* Add a uniform noise with a range [-amplitude..amplitude].
	*
	* @param amplitude		amplitude of the noise
	*/
	@Override
	public void addUniformNoise(double amplitude) {
		Random rnd = new Random();
		float[] slice = null;
		amplitude *= 2.0;
		for(int z=0; z<nz; z++) {
			slice = (float[])data[z];
			for(int k=0; k<nxy; k++) {
				slice[k] += (float)((rnd.nextDouble()-0.5)*amplitude);
			}
		}
	}

	/**
	* Add a salt and pepper noise.
	*
	* @param	amplitudeSalt		amplitude of the salt noise
	* @param	amplitudePepper		amplitude of the pepper noise
	* @param	percentageSalt		percentage of the salt noise
	* @param	percentagePepper	percentage of the pepper noise
	*/
	@Override
	public void addSaltPepper(double amplitudeSalt, double amplitudePepper, double percentageSalt, double percentagePepper) {
		Random rnd = new Random();
		int index, z;
		if (percentageSalt > 0) {
			double nbSalt = nxy*nz/percentageSalt;
			for( int k=0; k < nbSalt; k++) {
				index = (int)(rnd.nextDouble() * nxy);
				z = (int)(rnd.nextDouble() * nz);
				((float[])data[z])[index] += (float)(rnd.nextDouble()*amplitudeSalt);
			}
		}
		if (percentagePepper > 0) {
			double nbPepper = nxy*nz/percentagePepper;
			for( int k=0; k < nbPepper; k++) {
				index = (int)(rnd.nextDouble() * nxy);
				z = (int)(rnd.nextDouble() * nz);
				((float[])data[z])[index] -= (float)(rnd.nextDouble()*amplitudeSalt);
			}
		}
	}
	
}	// end of class
