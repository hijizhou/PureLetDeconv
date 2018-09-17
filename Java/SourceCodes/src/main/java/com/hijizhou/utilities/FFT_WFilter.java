package com.hijizhou.utilities;

/**
 * FFT_WFILTER: generate the waveletfilter matrix
 *
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
public class FFT_WFilter {
	private  double[] Dx;
	private  double[] Dy;
	private  double[] Dz;

	public FFT_WFilter() {

	}

	public double[][] fft_wavefilters(int M) {
		// Haar wavelet filter
		double[] L0_D = { Math.sqrt(2) / 2, Math.sqrt(2) / 2 };
		double[] HI_D = { 0 - Math.sqrt(2) / 2, Math.sqrt(2) / 2 };
		double[] nu = new double[M];
		double[] zR = new double[M];
		double[] zI = new double[M];
		double[] lowaR = new double[M];
		double[] lowaI = new double[M];
		double[] highaR = new double[M];
		double[] highaI = new double[M];
		int ki = 0;
		for (double i = 0; i < (1 - 1 / M); i += 1.0 / M) {
			nu[ki] = i;
			zR[ki] = Math.cos(-2 * Math.PI * i);
			zI[ki] = Math.sin(-2 * Math.PI * i);
			ki += 1;
		}
		for (int i = 0; i < ki; i++) {
			lowaR[i] = polyval(L0_D, zR[i], true);
			lowaI[i] = polyval(L0_D, zI[i], false);
			highaR[i] = polyval(HI_D, zR[i], true);
			highaI[i] = polyval(HI_D, zI[i], false);
		}
		double[][] Fa = new double[2][M * 2];

		for (int i = 0; i < M; i++) {
			Fa[0][i * 2] = lowaR[i];
			Fa[0][i * 2 + 1] = lowaI[i];
		}
		for (int i = 0; i < M; i++) {
			Fa[1][i * 2] = highaR[i];
			Fa[1][i * 2 + 1] = highaI[i];
		}

		return Fa;
	}

	public void fft_wfilters2D(int nx, int ny, int ori, int scale) {

		double[][] FaX = fft_wavefilters(nx);
		double[] HaX = FaX[0];
		double[] GaX = FaX[1];
		double[][] FaY = fft_wavefilters(ny);

		// conjugate
		double[] HaY = new double[ny * 2];
		double[] GaY = new double[ny * 2];
		for (int i = 1; i < ny * 2; i += 2) {
			HaX[i - 1] = FaX[0][i - 1];
			HaX[i] = 0 - FaX[0][i];
			GaX[i - 1] = FaX[1][i - 1];
			GaX[i] = 0 - FaX[1][i];
			HaY[i - 1] = FaY[0][i - 1];
			HaY[i] = 0 - FaY[0][i];
			GaY[i - 1] = FaY[1][i - 1];
			GaY[i] = 0 - FaY[1][i];
		}

		double[] Dx = new double[nx * 2];
		double[] Dy = new double[ny * 2];

		for (int i = 0; i < Dx.length; i += 2) {
			Dx[i] = 1;
			Dy[i] = 1;
		}

		switch (ori) {
		case 3: // LL
			// Arrays.fill(Dx, 1);
			// Arrays.fill(Dy, 1);
			for (int s = 0; s < scale; s++) {
				lowOper(Dx, HaX);
				lowOper(Dy, HaY);
			}

			break;
		case 2: // LH
			// Arrays.fill(Dx, 1);
			// Arrays.fill(Dy, 1);
			for (int s = 0; s < scale - 1; s++) {
				lowOper(Dx, HaX);
				highOper(Dy, HaY, GaY);
			}

			// Dx = Dx.*HaX;
			dotProComplex(Dx, HaX);
			// Dy = Dy.*GaY;
			dotProComplex(Dy, GaY);
			break;
		case 1: // HL
			// Arrays.fill(Dx, 1);
			// Arrays.fill(Dy, 1);
			for (int s = 0; s < scale - 1; s++) {
				highOper(Dx, HaX, GaX);
				lowOper(Dy, HaY);
			}

			// Dx = Dx.*GaX;
			dotProComplex(Dx, GaX);
			// Dy = Dy.*GaY;
			dotProComplex(Dy, HaY);
			break;
		case 0: // HH
			// Arrays.fill(Dx, 1);
			// Arrays.fill(Dy, 1);
			for (int s = 0; s < scale - 1; s++) {
				highOper(Dx, HaX, GaX);
				highOper(Dy, HaY, GaY);
			}
			// Dx = Dx.*GaX;
			dotProComplex(Dx, GaX);
			// Dy = Dy.*GaY;
			dotProComplex(Dy, GaY);
			break;
		}

		this.Dx = Dx;
		this.Dy = Dy;

	}

	public void fft_wfilters3D(int nx, int ny, int nz, int ori, int scale) {

		double[][] FaX = fft_wavefilters(nx);
		double[] HaX = FaX[0];
		double[] GaX = FaX[1];
		double[][] FaY = fft_wavefilters(ny);

		// conjugate
		double[] HaY = new double[ny * 2];
		double[] GaY = new double[ny * 2];
		for (int i = 1; i < ny * 2; i += 2) {
			HaX[i - 1] = FaX[0][i - 1];
			HaX[i] = 0 - FaX[0][i];
			GaX[i - 1] = FaX[1][i - 1];
			GaX[i] = 0 - FaX[1][i];
			HaY[i - 1] = FaY[0][i - 1];
			HaY[i] = 0 - FaY[0][i];
			GaY[i - 1] = FaY[1][i - 1];
			GaY[i] = 0 - FaY[1][i];
		}

		double[][] FaZ = fft_wavefilters(nz);
		double[] HaZ = FaZ[0];
		double[] GaZ = FaZ[1];

		//note the conjugate
		for (int i = 1; i < nz * 2; i += 2) {
			HaZ[i - 1] = FaZ[0][i - 1];
			HaZ[i] = 0 - FaZ[0][i];
			GaZ[i - 1] = FaZ[1][i - 1];
			GaZ[i] = 0 - FaZ[1][i];
		}

		double[] Dx = new double[nx * 2];
		double[] Dy = new double[ny * 2];
		double[] Dz = new double[nz * 2];

		// nx == ny
		for (int i = 0; i < Dx.length; i += 2) {
			Dx[i] = 1;
			Dy[i] = 1;
		}
		for (int i = 0; i < Dz.length; i += 2) {
			Dz[i] = 1;
		}

		switch (ori) {
			case 7: // LLL
				// Arrays.fill(Dx, 1);
				// Arrays.fill(Dy, 1);
				for (int s = 0; s < scale; s++) {
					lowOper(Dx, HaX);
					lowOper(Dy, HaY);
					lowOper(Dz, HaZ);
				}

				break;
			case 6: // LLH
				// Arrays.fill(Dx, 1);
				// Arrays.fill(Dy, 1);
				for (int s = 0; s < scale - 1; s++) {
					lowOper(Dx, HaX);
					lowOper(Dy, HaY);
					highOper(Dz, HaZ, GaZ);
				}

				// Dx = Dx.*HaX;
				dotProComplex(Dx, HaX);
				// Dy = Dy.*GaY;
				dotProComplex(Dy, HaY);
				// Dz = Dz.*GaZ;
				dotProComplex(Dz, GaZ);
				break;
			case 5: // HLL
				// Arrays.fill(Dx, 1);
				// Arrays.fill(Dy, 1);
				for (int s = 0; s < scale - 1; s++) {
					highOper(Dx, HaX, GaX);
					lowOper(Dy, HaY);
					lowOper(Dz, HaZ);
				}

				// Dx = Dx.*HaX;
				dotProComplex(Dx, GaX);
				// Dy = Dy.*GaY;
				dotProComplex(Dy, HaY);
				// Dz = Dz.*GaZ;
				dotProComplex(Dz, HaZ);
				break;
			case 4: // HLH
				// Arrays.fill(Dx, 1);
				// Arrays.fill(Dy, 1);
				for (int s = 0; s < scale - 1; s++) {
					highOper(Dx, HaX, GaX);
					lowOper(Dy, HaY);
					highOper(Dz, HaZ, GaZ);
				}

				// Dx = Dx.*HaX;
				dotProComplex(Dx, GaX);
				// Dy = Dy.*GaY;
				dotProComplex(Dy, HaY);
				// Dz = Dz.*GaZ;
				dotProComplex(Dz, GaZ);
				break;
			case 3: // LHL
				// Arrays.fill(Dx, 1);
				// Arrays.fill(Dy, 1);
				for (int s = 0; s < scale - 1; s++) {
					lowOper(Dx, HaX);
					highOper(Dy, HaY, GaY);
					lowOper(Dz, HaZ);
				}

				// Dx = Dx.*HaX;
				dotProComplex(Dx, HaX);
				// Dy = Dy.*GaY;
				dotProComplex(Dy, GaY);
				// Dz = Dz.*GaZ;
				dotProComplex(Dz, HaZ);
				break;
			case 2: // LHH
				// Arrays.fill(Dx, 1);
				// Arrays.fill(Dy, 1);
				for (int s = 0; s < scale - 1; s++) {
					lowOper(Dx, HaX);
					highOper(Dy, HaY, GaY);
					highOper(Dz, HaZ, GaZ);
				}

				// Dx = Dx.*HaX;
				dotProComplex(Dx, HaX);
				// Dy = Dy.*GaY;
				dotProComplex(Dy, GaY);
				// Dz = Dz.*GaZ;
				dotProComplex(Dz, GaZ);
				break;

			case 1: // HHL
				// Arrays.fill(Dx, 1);
				// Arrays.fill(Dy, 1);
				for (int s = 0; s < scale - 1; s++) {
					highOper(Dx, HaX, GaX);
					highOper(Dy, HaY, GaY);
					lowOper(Dz, HaZ);
				}

				// Dx = Dx.*GaX;
				dotProComplex(Dx, GaX);
				// Dy = Dy.*GaY;
				dotProComplex(Dy, GaY);
				// Dz = Dz.*GaZ;
				dotProComplex(Dz, HaZ);
				break;
			case 0: // HHH
				// Arrays.fill(Dx, 1);
				// Arrays.fill(Dy, 1);
				for (int s = 0; s < scale - 1; s++) {
					highOper(Dx, HaX, GaX);
					highOper(Dy, HaY, GaY);
					highOper(Dz, HaZ, GaZ);
				}
				// Dx = Dx.*GaX;
				dotProComplex(Dx, GaX);
				// Dy = Dy.*GaY;
				dotProComplex(Dy, GaY);
				// Dz = Dz.*GaZ;
				dotProComplex(Dz, GaZ);
				break;
		}

		this.Dx = Dx;
		this.Dy = Dy;
		this.Dz = Dz;

	}

	public static void lowOper(double[] Dx, double[] HaX) {
		int nx = Dx.length / 2;
		// Dx = Dx.*HaX;
		// for (int i = 0; i < nx * 2; i++) {
		// Dx[i] = Dx[i] * HaX[i];
		// }
		dotProComplex(Dx, HaX);
		// HaX = [HaX(1:2:end); HaX(1:2:end)]
		double[] aux = new double[nx * 2];
		int auxi = 0;
		for (int t = 0; t < nx * 2; t += 4) {
			aux[auxi] = HaX[t];
			aux[auxi + 1] = HaX[t + 1];
			auxi += 2;
		}
		for (int t = 0; t < nx * 2; t += 4) {
			aux[auxi] = HaX[t];
			aux[auxi + 1] = HaX[t + 1];
			auxi += 2;
		}
		for (int i = 0; i < nx * 2; i++) {
			HaX[i] = aux[i];
		}
	}

	public static void dotProComplex(double[] A, double[] B) {
		int nx = A.length / 2;
		for (int i = 0; i < nx * 2 - 1; i += 2) {
			double aux = A[i];
			A[i] = A[i] * B[i] - A[i + 1] * B[i + 1];
			A[i + 1] = A[i + 1] * B[i] + aux * B[i + 1];
		}
	}

	public static void highOper(double[] Dx, double[] HaX, double[] GaX) {
		int nx = Dx.length / 2;
		// Dx = Dx.*HaX;
		// for (int i = 0; i < nx * 2; i++) {
		// Dx[i] = Dx[i] * HaX[i];
		// }
		dotProComplex(Dx, HaX);

		// HaX = [HaX(1:2:end); HaX(1:2:end)]
		double[] aux = new double[nx * 2];
		int auxi = 0;
		for (int t = 0; t < nx * 2; t += 4) {
			aux[auxi] = HaX[t];
			aux[auxi + 1] = HaX[t + 1];
			auxi += 2;
		}
		for (int t = 0; t < nx * 2; t += 4) {
			aux[auxi] = HaX[t];
			aux[auxi + 1] = HaX[t + 1];
			auxi += 2;
		}
		for (int i = 0; i < nx * 2; i++) {
			HaX[i] = aux[i];
		}
		// GaX = [GaX(1:2:end); GaX(1:2:end)]
		double[] aux2 = new double[nx * 2];
		int auxi2 = 0;
		for (int t = 0; t < nx * 2; t += 4) {
			aux2[auxi2] = GaX[t];
			aux2[auxi2 + 1] = GaX[t + 1];
			auxi2 += 2;
		}
		for (int t = 0; t < nx * 2; t += 4) {
			aux2[auxi2] = GaX[t];
			aux2[auxi2 + 1] = GaX[t + 1];
			auxi2 += 2;
		}
		for (int i = 0; i < nx * 2; i++) {
			GaX[i] = aux2[i];
		}
	}

	public static double[] fliplr(double[] arr) {
		double[] flipArr = new double[arr.length];
		for (int i = 0; i < arr.length; i++) {
			flipArr[i] = arr[arr.length - i - 1];
		}
		return flipArr;
	}

	public static double polyval(double p[], double x, boolean isreal) // return
	{ // using Horner's rule
		double y;
		y = p[0]; // p one larger than order
		int nc = p.length;
		for (int i = 1; i < nc; i++) {
			if (isreal) {
				y = x * y + p[i];
			} else {
				y = x * y;
			}
		}

		return y;
	} // end polyval

	public double[] getDx() {
		return this.Dx;
	}

	public double[] getDy() {
		return this.Dy;
	}

	public double[] getDz(){return this.Dz; }

	public static void main(String[] args) {
		FFT_WFilter hf = new FFT_WFilter();
		hf.fft_wfilters2D(256, 256, 0, 2);
		System.out.println(hf.Dx[2]);
	}

}
