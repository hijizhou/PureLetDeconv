
/*
 * DeconvolutionLab2
 * 
 * Conditions of use: You are free to use this software for research or
 * educational purposes. In addition, we expect you to include adequate
 * citations and acknowledgments whenever you present or publish results that
 * are based on it.
 * 
 * Reference: DeconvolutionLab2: An Open-Source Software for Deconvolution
 * Microscopy D. Sage, L. Donati, F. Soulez, D. Fortun, G. Schmit, A. Seitz,
 * R. Guiet, C. Vonesch, M Unser, Methods of Elsevier, 2017.
 */

/*
 * Copyright 2010-2017 Biomedical Imaging Group at the EPFL.
 * 
 * This file is part of DeconvolutionLab2 (DL2).
 * 
 * DL2 is free software: you can redistribute it and/or modify it under the
 * terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 * 
 * DL2 is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * DL2. If not, see <http://www.gnu.org/licenses/>.
 */

import ij.ImagePlus;
import ij.WindowManager;
import matlab.Converter;
import signal.RealSignal;
import bilib.tools.Files;
import deconvolution.Deconvolution;
import deconvolutionlab.Imager;
import deconvolutionlab.Lab;
import deconvolutionlab.LabDialog;

/**
 * This class allows Matlab interface for DeconvolutionLab2
 * 
 * A Matlab 3D variable in converted to a RealSignal and vice-versa.
 * 
 * @author Daniel Sage
 *
 */
public class DL2 {

	public static void lab() {
		Lab.init(Imager.Platform.MATLAB, Files.getWorkingDirectory() + "DeconvolutionLab2.config");
		LabDialog dlg = new LabDialog();
		Lab.setVisible(dlg, false);
	}

	public static void run(String command) {
		new Deconvolution("Matlab", command).deconvolve();
	}	
	
	public static void launch(String command) {
		new Deconvolution("Matlab", command).launch();
	}	

	public static Object get(String image) {
		ImagePlus imp = WindowManager.getCurrentImage();
		if (imp != null)
			return Converter.get(imp);
		return null;
	}	

	public static Object run(Object arrayImage, Object arrayPSF, String algo) {
		RealSignal image = Converter.createRealSignal(arrayImage);
		RealSignal psf = Converter.createRealSignal(arrayPSF);
		String command = " -image platform input -psf platform psf -algorithm " + algo;
		Deconvolution d = new Deconvolution("Matlab", command);
		RealSignal result = d.deconvolve(image, psf);
		return Converter.createObject(result);
	}
	
	public static void help() {
		Lab.help();
	}
	
	public static void clear() {
		int ids[] = WindowManager.getIDList();
		for(int id : ids) {
			ImagePlus imp = WindowManager.getImage(id);
			if (imp != null)
				imp.close();
		}
	}

	public static Object DIV(Object arrayImage, Object arrayPSF) {
		return DIV(arrayImage, arrayPSF, "");
	}
	
	public static Object DIV(Object arrayImage, Object arrayPSF, String options) {
		RealSignal image = Converter.createRealSignal(arrayImage);
		RealSignal psf = Converter.createRealSignal(arrayPSF);
		String command = " -algorithm DIV " + options;
		Deconvolution d = new Deconvolution("Matlab DIV", command);
		RealSignal result = d.deconvolve(image, psf);
		return Converter.createObject(result);
	}

	public static Object CONV(Object arrayImage, Object arrayPSF) {
		return CONV(arrayImage, arrayPSF, "");
	}
	
	public static Object CONV(Object arrayImage, Object arrayPSF, String options) {
		RealSignal image = Converter.createRealSignal(arrayImage);
		RealSignal psf = Converter.createRealSignal(arrayPSF);
		String command = " -algorithm CONV " + options;
		Deconvolution d = new Deconvolution("Matlab CONV", command);
		RealSignal result = d.deconvolve(image, psf);
		return Converter.createObject(result);
	}

	public static Object NIF(Object arrayImage, Object arrayPSF) {
		return NIF(arrayImage, arrayPSF, "");
	}
	
	public static Object NIF(Object arrayImage, Object arrayPSF, String options) {
		RealSignal image = Converter.createRealSignal(arrayImage);
		RealSignal psf = Converter.createRealSignal(arrayPSF);
		String command = " -algorithm NIF " + options;
		Deconvolution d = new Deconvolution("Matlab NIF", command);
		RealSignal result = d.deconvolve(image, psf);
		return Converter.createObject(result);
	}

	public static Object TRIF(Object arrayImage, Object arrayPSF, double regularizationFactor) {
		return TRIF(arrayImage, arrayPSF, regularizationFactor, "");
	}
	
	public static Object TRIF(Object arrayImage, Object arrayPSF, double regularizationFactor, String options) {
		RealSignal image = Converter.createRealSignal(arrayImage);
		RealSignal psf = Converter.createRealSignal(arrayPSF);
		String command = " -algorithm TRIF " + regularizationFactor + " " + options;
		Deconvolution d = new Deconvolution("Matlab TRIF", command);
		RealSignal result = d.deconvolve(image, psf);
		return Converter.createObject(result);
	}
	
	public static Object RIF(Object arrayImage, Object arrayPSF, double regularizationFactor) {
		return RIF(arrayImage, arrayPSF, regularizationFactor, "");
	}
	
	public static Object RIF(Object arrayImage, Object arrayPSF, double regularizationFactor, String options) {
		RealSignal image = Converter.createRealSignal(arrayImage);
		RealSignal psf = Converter.createRealSignal(arrayPSF);
		String command = " -algorithm RIF " + regularizationFactor + " " + options;
		Deconvolution d = new Deconvolution("Matlab RIF", command);
		RealSignal result = d.deconvolve(image, psf);
		return Converter.createObject(result);
	}	
	
	public static Object RL(Object arrayImage, Object arrayPSF, double itmax) {
		return RL(arrayImage, arrayPSF, itmax, "");
	}
	
	public static Object RL(Object arrayImage, Object arrayPSF, double itmax, String options) {
		RealSignal image = Converter.createRealSignal(arrayImage);
		RealSignal psf = Converter.createRealSignal(arrayPSF);
		String command = " -algorithm RL " + itmax + " " + options;
		Deconvolution d = new Deconvolution("Matlab RL", command);
		RealSignal result = d.deconvolve(image, psf);
		return Converter.createObject(result);
	}
	
	public static Object RLTV(Object arrayImage, Object arrayPSF, double itmax, double regularizationFactor) {
		return RLTV(arrayImage, arrayPSF, itmax, regularizationFactor, "");
	}
	
	public static Object RLTV(Object arrayImage, Object arrayPSF, double itmax, double regularizationFactor, String options) {
		RealSignal image = Converter.createRealSignal(arrayImage);
		RealSignal psf = Converter.createRealSignal(arrayPSF);
		String command = " -algorithm RLTV " + itmax + " " + regularizationFactor + " " + options;
		Deconvolution d = new Deconvolution("Matlab RLTV", command);
		RealSignal result = d.deconvolve(image, psf);
		return Converter.createObject(result);
	}	

	public static Object LW(Object arrayImage, Object arrayPSF, double itmax, double gamma) {
		return LW(arrayImage, arrayPSF, itmax, gamma, "");
	}
	
	public static Object LW(Object arrayImage, Object arrayPSF, double itmax, double gamma, String options) {
		RealSignal image = Converter.createRealSignal(arrayImage);
		RealSignal psf = Converter.createRealSignal(arrayPSF);
		String command = " -algorithm LW " + itmax + " " + gamma + " " + options;
		Deconvolution d = new Deconvolution("Matlab LW", command);
		RealSignal result = d.deconvolve(image, psf);
		return Converter.createObject(result);
	}

	public static Object NNLS(Object arrayImage, Object arrayPSF, double itmax, double gamma) {
		return NNLS(arrayImage, arrayPSF, itmax, gamma, "");
	}
	
	public static Object NNLS(Object arrayImage, Object arrayPSF, double itmax, double gamma, String options) {
		RealSignal image = Converter.createRealSignal(arrayImage);
		RealSignal psf = Converter.createRealSignal(arrayPSF);
		String command = " -algorithm NNLS " + itmax + " " + options;
		Deconvolution d = new Deconvolution("Matlab NNLS", command);
		RealSignal result = d.deconvolve(image, psf);
		return Converter.createObject(result);
	}
	
	public static Object BVLS(Object arrayImage, Object arrayPSF, double itmax, double gamma) {
		return BVLS(arrayImage, arrayPSF, itmax, gamma, "");
	}
	
	public static Object BVLS(Object arrayImage, Object arrayPSF, double itmax, double gamma, String options) {
		RealSignal image = Converter.createRealSignal(arrayImage);
		RealSignal psf = Converter.createRealSignal(arrayPSF);
		String command = " -algorithm BVLS " + itmax + " " + options;
		Deconvolution d = new Deconvolution("Matlab BVLS", command);
		RealSignal result = d.deconvolve(image, psf);
		return Converter.createObject(result);
	}

	public static Object TM(Object arrayImage, Object arrayPSF, double itmax, double gamma, double lambda) {
		return TM(arrayImage, arrayPSF, itmax, gamma, lambda, "");
	}
	
	public static Object TM(Object arrayImage, Object arrayPSF, double itmax, double gamma, double lambda, String options) {
		RealSignal image = Converter.createRealSignal(arrayImage);
		RealSignal psf = Converter.createRealSignal(arrayPSF);
		String command = " -algorithm TM " + itmax + " " + gamma + " " + lambda + " " + options;
		Deconvolution d = new Deconvolution("Matlab TM", command);
		RealSignal result = d.deconvolve(image, psf);
		return Converter.createObject(result);
	}

	public static Object ICTM(Object arrayImage, Object arrayPSF, double itmax, double gamma, double lambda) {
		return ICTM(arrayImage, arrayPSF, itmax, gamma, lambda, "");
	}
	
	public static Object ICTM(Object arrayImage, Object arrayPSF, double itmax, double gamma, double lambda, String options) {
		RealSignal image = Converter.createRealSignal(arrayImage);
		RealSignal psf = Converter.createRealSignal(arrayPSF);
		String command = " -algorithm ICTM " + itmax + " " + gamma + " " + lambda + " " + options;
		Deconvolution d = new Deconvolution("Matlab ICTM", command);
		RealSignal result = d.deconvolve(image, psf);
		return Converter.createObject(result);
	}
}
