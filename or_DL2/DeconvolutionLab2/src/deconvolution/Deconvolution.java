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

package deconvolution;

import java.io.File;

import signal.RealSignal;
import signal.SignalCollector;
import bilib.tools.NumFormat;
import deconvolution.algorithm.Algorithm;
import deconvolution.algorithm.Controller;
import deconvolutionlab.Lab;
import deconvolutionlab.monitor.AbstractMonitor;
import deconvolutionlab.monitor.Monitors;
import deconvolutionlab.monitor.TableMonitor;
import deconvolutionlab.output.Output;

/**
 * This class is the main class to run deconvolution with or without user interface.
 * 
 * All the parameters are given in the command line (String variable command).
 * 
 * @author Daniel Sage
 *
 */
public class Deconvolution implements Runnable {

	public enum Finish {
		DIE, ALIVE, KILL
	};

	private Algorithm					algo				= null;
	private Controller					controller 			= new Controller();
	private String						command				= "";
	private Features					report				= new Features();
	private String						name				= "";
	public RealSignal					image;
	public RealSignal					psf;
	private RealSignal					deconvolvedImage;
	private Finish						finish				= Finish.DIE;
	private DeconvolutionDialog			dialog;
	private boolean 					embeddedStats 		= false;
	
	public Deconvolution(String name, String command) {
		this.name = name;
		this.finish = Finish.DIE;
		setCommand(command);
	}

	public Deconvolution(String name, String command, Finish finish) {
		this.name = name;
		this.finish = finish;
		setCommand(command);
	}

	public void setCommand(String command) {
		this.command = command;
		controller = Command.decodeController(command);
		algo = Command.decodeAlgorithm(command);
		algo.setController(controller);
		this.command = command;
		if (name.equals("") && algo != null)
			name = algo.getShortnames()[0];
	}

	public RealSignal deconvolve() {
		return deconvolve(image, psf);
	}
	
	public RealSignal deconvolve(RealSignal image, RealSignal psf) {
		this.image = image;
		this.psf = psf;
		
		for(AbstractMonitor monitor : controller.getMonitors())
			if (monitor instanceof TableMonitor)
				Lab.setVisible(((TableMonitor)monitor).getPanel(), "Monitor of " + name, 10, 10);

		if (controller.getFFT() == null) {
			run();
			return deconvolvedImage;
		}
		
		if (!controller.getFFT().isMultithreadable()) {
			run();
			return deconvolvedImage;
		}

		if (controller.isMultithreading()) {
			Thread thread = new Thread(this);
			thread.setPriority(Thread.MIN_PRIORITY);
			thread.start();
		}
		else {
			run();
		}	
		return deconvolvedImage;
	}

	/**
	 * This method runs the deconvolution with a graphical user interface.
	 */
	public void launch() {
		embeddedStats = true;
		dialog = new DeconvolutionDialog(DeconvolutionDialog.Module.ALL, this);
		Lab.setVisible(dialog, false);
	}

	@Override
	public void run() {
		double chrono = System.nanoTime();
		Monitors monitors = controller.getMonitors();
		
		report.add("Path", controller.toStringPath());

		if (image == null)
			image = openImage();

		if (image == null) {
			monitors.error("Image: Not valid " + command);
			report.add("Image", "Not valid");
			if (finish == Finish.KILL)
				System.exit(-101);
			return;
		}
		report.add("Image", image.dimAsString());
		monitors.log("Image: " + image.dimAsString());

		if (psf == null)
			psf = openPSF();
		
		if (psf == null) {
			monitors.error("PSF: not valid");
			report.add("PSF", "Not valid");
			if (finish == Finish.KILL)
				System.exit(-102);
			return;
		}
		report.add("PSF", psf.dimAsString());

		if (algo == null) {
			monitors.error("Algorithm: not valid");
			if (finish == Finish.KILL)
				System.exit(-103);
			return;
		}

		report.add("FFT", controller.getFFT().getName());
		report.add("Algorithm", algo.getName());

		if (embeddedStats) {
			TableMonitor tableMonitor = null;
			for(AbstractMonitor monitor : controller.getMonitors())
				if (monitor instanceof TableMonitor)
					tableMonitor = (TableMonitor)monitor;
			if (controller.getStats().getMode() == Stats.Mode.SHOW || controller.getStats().getMode() == Stats.Mode.SHOWSAVE) {
				controller.getStats().setEmbeddedInFrame(embeddedStats);
				dialog.addStats(controller.getStats());
			}
			if (tableMonitor != null) {
				dialog.addMonitor(tableMonitor);
			}
		}
		algo.setController(controller);
		deconvolvedImage = algo.run(image, psf);

		report.add("End", NumFormat.time(System.nanoTime() - chrono));


		if (finish == Finish.KILL) {
			System.out.println("End");
			System.exit(0);
		}

		if (finish == Finish.DIE)
			die();
	}

	public void close() {
		if (dialog != null)
			dialog.dispose();
		SignalCollector.free(image);
		SignalCollector.free(psf);
		SignalCollector.free(deconvolvedImage);
		algo = null;
		image = null;
		psf = null;
		deconvolvedImage = null;
		System.gc();
	}

	public void die() {
		SignalCollector.free(image);
		SignalCollector.free(psf);
	}

	/**
	 * This methods make a recap of the deconvolution. Useful before starting
	 * the processing.
	 * 
	 * @return list of messages to print
	 */
	public Features recap() {
		Features features = new Features();
		CommandToken image = Command.extract(command, "-image");
		features.add("Image", image == null ? "keyword -image not found" : image.parameters);
		double norm = controller.getNormalizationPSF();
		String normf = (norm < 0 ? " (no normalization)" : " (normalization to " + norm + ")");
		CommandToken psf = Command.extract(command, "-psf");
		features.add("PSF", psf == null ? "keyword -psf not found" : psf.parameters + " norm:" + normf);

		if (algo == null) {
			features.add("Algorithm", "not valid>");
		}
		else {
			Controller controller = algo.getController();
			features.add("Algorithm", algo.toString());
			features.add("Stopping Criteria", controller.getStoppingCriteriaAsString(algo));
			features.add("Reference", controller.getReferenceName());
			features.add("Constraint", controller.getConstraintAsString());
			features.add("Padding", controller.getPadding().toString());
			features.add("Apodization", controller.getApodization().toString());
			features.add("FFT", controller.getFFT() == null ? "null" : controller.getFFT().getName());
		}
		features.add("Path", controller.getPath());

		String s = "[" + controller.getVerbose().name() + "] ";
		for(AbstractMonitor monitor : controller.getMonitors())
			s+= monitor.getName() + " ";
		features.add("Monitor", s);
		if (controller.getStats() != null)
			features.add("Stats", controller.getStats().toStringStats());
		
		for (Output out : controller.getOuts())
			features.add("Output " + out.getName(), out.toString());
		
		controller.getMonitors().log("Recap deconvolution parameters");
		return features;
	}

	public Features checkOutput() {
		Features features = new Features();
		if (deconvolvedImage == null) {
			features.add("Image", "No valid output image");
			return features;
		}
		float stati[] = deconvolvedImage.getStats();
		int sizi = deconvolvedImage.nx * deconvolvedImage.ny * deconvolvedImage.nz;
		float totali = stati[0] * sizi;
		features.add("<html><b>Deconvolved Image</b></html>", "");
		features.add("Size", deconvolvedImage.dimAsString() + " " + NumFormat.bytes(sizi * 4));
		features.add("Mean (stdev)", NumFormat.nice(stati[0]) + " (" + NumFormat.nice(stati[3]) + ")");
		features.add("Min ... Max", NumFormat.nice(stati[1]) + " ... " + NumFormat.nice(stati[2]));
		features.add("Energy (int)", NumFormat.nice(stati[5]) + " (" + NumFormat.nice(totali) + ")");
		return features;
	}

	public void abort() {
		algo.getController().abort();
	}

	public RealSignal openImage() {
		CommandToken token = Command.extract(command, "-image");

		if (token == null)
			return null;

		if (token.parameters.startsWith(">>>"))
			return null;
		String arg = token.option.trim();

		String cmd = token.parameters.substring(arg.length(), token.parameters.length()).trim();

		image = createRealSignal(controller.getMonitors(), arg, cmd, controller.getPath());
		controller.getMonitors().log("Open image " + arg + " " + cmd);

		return image;
	}

	public RealSignal openPSF() {
		
		CommandToken token = Command.extract(command, "-psf");
		if (token == null)
			return null;
		if (token.parameters.startsWith(">>>"))
			return null;
		String arg = token.option.trim();
		String cmd = token.parameters.substring(arg.length(), token.parameters.length()).trim();
		psf = createRealSignal(controller.getMonitors(), arg, cmd, controller.getPath());
		controller.getMonitors().log("Open PSF " + arg + " " + cmd);
		return psf;
	}

	private static RealSignal createRealSignal(Monitors monitors, String arg, String cmd, String path) {
		RealSignal signal = null;
		if (arg.equalsIgnoreCase("synthetic")) {
			signal = Lab.createSynthetic(monitors, cmd);
		}

		if (arg.equalsIgnoreCase("platform")) {
			signal = Lab.getImage(monitors, cmd);
		}

		if (arg.equalsIgnoreCase("file")) {
			File file = new File(path + File.separator + cmd);
			if (file != null) {
				if (file.isFile())
					signal = Lab.openFile(monitors, path + File.separator + cmd);
			}
			if (signal == null) {
				File local = new File(cmd);
				if (local != null) {
					if (local.isFile())
						signal = Lab.openFile(monitors, cmd);
				}
			}
		}

		if (arg.equalsIgnoreCase("dir") || arg.equalsIgnoreCase("directory")) {
			File file = new File(path + File.separator + cmd);
			if (file != null) {
				if (file.isDirectory())
					signal = Lab.openDir(monitors, path + File.separator + cmd);
			}
			if (signal == null) {
				File local = new File(cmd);
				if (local != null) {
					if (local.isDirectory())
						signal = Lab.openDir(monitors, cmd);
				}
			}
		}
		return signal;
	}

	public void setAlgorithm(Algorithm algo) {
		this.algo = algo;
	}

	public Algorithm getAlgorithm() {
		return algo;
	}
	
	public void setController(Controller controller) {
		this.controller = controller;
	}

	public Controller getController() {
		return controller;
	}
	
	public RealSignal getOutput() {
		return deconvolvedImage;
	}

	public RealSignal getImage() {
		return image;
	}

	public RealSignal getPSF() {
		return psf;
	}

	public Features getDeconvolutionReports() {
		return report;
	}

	public String getName() {
		return name;
	}

	public Monitors getMonitors() {
		return controller.getMonitors();
	}

	public String getCommand() {
		return command;
	}

}
