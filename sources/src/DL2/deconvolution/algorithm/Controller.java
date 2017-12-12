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

package DL2.deconvolution.algorithm;

import DL2.bilib.tools.Files;
import DL2.bilib.tools.NumFormat;
import DL2.deconvolution.Deconvolution;
import DL2.deconvolution.Stats;
import DL2.deconvolutionlab.Constants;
import DL2.deconvolutionlab.monitor.*;
import DL2.deconvolutionlab.output.Output;
import DL2.deconvolutionlab.system.SystemUsage;
import DL2.fft.AbstractFFT;
import DL2.fft.FFT;
import DL2.signal.Assessment;
import DL2.signal.ComplexSignal;
import DL2.signal.Constraint;
import DL2.signal.RealSignal;
import DL2.signal.apodization.Apodization;
import DL2.signal.padding.Padding;

import java.io.File;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

/**
 * This is an important class to manage all the common task of the algorithm.
 * The method start() is called before at the starting of the algorithm. The
 * method ends() is called at the end of every iterations for the iterative
 * algorithm. It returns true if one the stopping criteria is true. The method
 * finish() is called when the algorithm is completely terminated.
 * 
 * A timer is started to get the peak memory. 
 * 
 * @author Daniel Sage
 *
 */
public class Controller {

	private String				path;
	private boolean				system;
	private boolean				multithreading;
	private boolean				displayFinal;
	private double				normalizationPSF;
	private double				epsilon;
	
	private Padding				padding;
	private Apodization			apodization;
	private ArrayList<Output>	outs;
	private Stats				stats;
	private Constraint.Mode		constraintMode;
	private double				residuMin;
	private double				timeLimit;
	private String				referenceName;
	private Monitors			monitors;
	private Verbose				verbose;
	private AbstractFFT			fft;

	private int					iterationsMax	= 100;

	private boolean				doResidu		= false;
	private boolean				doTime			= false;
	private boolean				doReference		= false;
	private boolean				doConstraint	= false;
	private boolean				abort			= false;

	private double				timeStarting	= 0;
	private double				memoryStarting	= 0;
	private double				residu			= Double.MAX_VALUE;
	private int					iterations		= 0;
	private double				memoryPeak		= 0;
	private double				snr				= 0;
	private double				psnr			= 0;

	private RealSignal			refImage;
	private RealSignal			prevImage;
	private RealSignal			x;
	
	private Timer				timer;
	
	private String				algoName = "";

	/**
	 * Constructor.
	 * 
	 * One controller is always instantiated for every run of a algorithm.
	 */
	public Controller() {
		doResidu = false;
		doTime = false;
		doReference = false;
		doConstraint = false;
		timeStarting = System.nanoTime();
		
		setPath(Files.getWorkingDirectory());
		setSystem(true);
		setMultithreading(true);
		setDisplayFinal(true);
		setFFT(FFT.getFastestFFT().getDefaultFFT());
		setNormalizationPSF(1);
		setEpsilon(1e-6);
		setPadding(new Padding());
		setApodization(new Apodization());

		monitors = new Monitors();
		monitors.add(new ConsoleMonitor());
		monitors.add(new TableMonitor(Constants.widthGUI, 240));

		setVerbose(Verbose.Log);
		setStats(new Stats(Stats.Mode.NO));
		setConstraint(Constraint.Mode.NO);
		setResiduMin(-1);
		setTimeLimit(-1);
		setReference(null);
		setOuts(new ArrayList<Output>());
	}

	public void setAlgoName(String algoName) {
		this.algoName = algoName;
	}
	
	public void setFFT(AbstractFFT fft) {
		this.fft = fft;
	}

	public void abort() {
		this.abort = true;
	}

	public void setIterationsMax(int iterationsMax) {
		this.iterationsMax = iterationsMax;
	}

	public boolean needSpatialComputation() {
		return doConstraint || doResidu || doReference;
	}

	/**
	 * Call one time at the beginning of the algorithms
	 * 
	 * @param x
	 *            the input signal
	 */
	public void start(RealSignal x) {
		this.x = x;	
		
		stats.show();
		stats.addInput(x);
		
		iterations = 0;
		timer = new Timer();
		timer.schedule(new Updater(), 0, 100);
		timeStarting = System.nanoTime();
		memoryStarting = SystemUsage.getHeapUsed();

		if (doConstraint && x != null)
			Constraint.setModel(x);

		if (doReference && refImage == null) {
			refImage = new Deconvolution("Reference", "-image file " + referenceName).openImage();
			if (refImage == null)
				monitors.error("Impossible to load the reference image " + referenceName);
			else
				monitors.log("Reference image loaded");
		}
		for (Output out : outs)
			out.executeStarting(monitors, x, this);

		this.prevImage = x;
	}

	public boolean ends(ComplexSignal X) {

		boolean out = false;
		for (Output output : outs)
			out = out | output.is(iterations);

		if (doConstraint || doResidu || doReference || out) {
			if (fft == null)
				fft = FFT.createDefaultFFT(monitors, X.nx, X.ny, X.nz);
			x = fft.inverse(X, x);
			return ends(x);
		}

		return ends((RealSignal) null);
	}

	public boolean ends(RealSignal x) {
		this.x = x;

		if (doConstraint || doResidu || doReference)
			compute(iterations, x, doConstraint, doResidu, doReference);

		for (Output out : outs)
			out.executeIterative(monitors, x, this, iterations);

		iterations++;
		double p = iterations * 100.0 / iterationsMax;
		monitors.progress("Iterative " + iterations + "/" + iterationsMax, p);
		double timeElapsed = getTimeSecond();
		boolean stopIter = (iterations >= iterationsMax);
		boolean stopTime = doTime && (timeElapsed >= timeLimit);
		boolean stopResd = doResidu && (residu <= residuMin);
		monitors.log("@" + iterations + " Time: " + NumFormat.seconds(timeElapsed*1e9));

		String pnsrText = doReference ? "" + psnr : "n/a";
		String snrText = doReference ? "" + snr : "n/a";
		String residuText = doResidu ? "" + residu : "n/a";
		stats.add(x, iterations, NumFormat.seconds(getTimeNano()), pnsrText, snrText, residuText);
		
		String prefix = "Stopped>> by ";
		if (abort)
			monitors.log(prefix + "abort");
		if (stopIter)
			monitors.log(prefix + "iteration " + iterations + " > " + iterationsMax);
		if (stopTime)
			monitors.log(prefix + "time " + timeElapsed + " > " + timeLimit);
		if (stopResd)
			monitors.log(prefix + "residu " + NumFormat.nice(residu) + " < " + NumFormat.nice(residuMin));

		return abort | stopIter | stopTime | stopResd;
	}

	public void finish(RealSignal x) {
		this.x = x;

		boolean ref = doReference;
		boolean con = doConstraint;
		boolean res = doResidu;
		if (con || res || ref)
			compute(iterations, x, con, res, ref);

		String pnsrText = doReference ? ""+psnr : "n/a";
		String snrText = doReference ? ""+snr : "n/a";
		String residuText = doResidu ? "" + residu : "n/a";
		stats.addOutput(x, algoName, NumFormat.seconds(getTimeNano()), pnsrText, snrText, residuText);
		
		stats.save(monitors, path);
		
		for (Output out : outs)
			out.executeFinal(monitors, x, this);

		monitors.log("Time: " + NumFormat.seconds(getTimeNano()) + " Peak:" + getMemoryAsString());
		if (timer != null)
			timer.cancel();
	}

	private void compute(int iterations, RealSignal x, boolean con, boolean res, boolean ref) {
		if (x == null)
			return;

		if (con && constraintMode != null)
			new Constraint(monitors).apply(x, constraintMode);

		if (ref && refImage != null) {
			String s = "";
			psnr = Assessment.psnr(x, refImage);
			snr = Assessment.snr(x, refImage);
			s += " PSNR: " + NumFormat.nice(psnr);
			s += " SNR: " + NumFormat.nice(snr);
			monitors.log("@" + iterations + " " + s);
		}

		residu = Double.MAX_VALUE;
		if (res && prevImage != null) {
			residu = Assessment.relativeResidu(x, prevImage);
			prevImage = x.duplicate();
			monitors.log("@" + iterations + " Residu: " + NumFormat.nice(residu));
		}
	}

	public double getTimeNano() {
		return (System.nanoTime() - timeStarting);
	}
	
	public double getTimeSecond() {
		return (System.nanoTime() - timeStarting) * 1e-9;
	}

	public String getConstraintAsString() {
		if (!doConstraint)
			return "no";
		if (constraintMode == null)
			return "null";
		return constraintMode.name().toLowerCase();
	}

	public String getStoppingCriteriaAsString(Algorithm algo) {
		String stop = algo.isIterative() ? "iterations limit=" + algo.getIterationsMax() + ", " : "direct, ";
		stop += doTime ? ", time limit=" + NumFormat.nice(timeLimit * 1e-9) : " no time limit" + ", ";
		stop += doResidu ? ", residu limit=" + NumFormat.nice(residuMin) : " no residu limit";
		return stop;
	}

	public double getMemory() {
		return memoryPeak - memoryStarting;
	}

	public String getMemoryAsString() {
		return NumFormat.bytes(getMemory());
	}

	public int getIterations() {
		return iterations;
	}
	
	public double getSNR() {
		return snr;
	}

	public double getPSNR() {
		return psnr;
	}

	public double getResidu() {
		return residu;
	}

	private void update() {
		memoryPeak = Math.max(memoryPeak, SystemUsage.getHeapUsed());
	}

	public AbstractFFT getFFT() {
		return fft;
	}

	/**
	 * @return the path
	 */
	public String getPath() {
		return path;
	}

	/**
	 * @param path
	 *            the path to set
	 */
	public void setPath(String path) {
		this.path = path;
	}

	/**
	 * @return the system
	 */
	public boolean isSystem() {
		return system;
	}

	/**
	 * @param system
	 *            the system to set
	 */
	public void setSystem(boolean system) {
		this.system = system;
	}

	/**
	 * @return the multithreading
	 */
	public boolean isMultithreading() {
		return multithreading;
	}

	/**
	 * @param multithreading
	 *            the multithreading to set
	 */
	public void setMultithreading(boolean multithreading) {
		this.multithreading = multithreading;
	}

	/**
	 * @return the displayFinal
	 */
	public boolean isDisplayFinal() {
		return displayFinal;
	}

	/**
	 * @param displayFinal
	 *            the displayFinal to set
	 */
	public void setDisplayFinal(boolean displayFinal) {
		this.displayFinal = displayFinal;
	}

	/**
	 * @return the normalizationPSF
	 */
	public double getNormalizationPSF() {
		return normalizationPSF;
	}

	/**
	 * @param normalizationPSF
	 *            the normalizationPSF to set
	 */
	public void setNormalizationPSF(double normalizationPSF) {
		this.normalizationPSF = normalizationPSF;
	}

	/**
	 * @return the epsilon
	 */
	public double getEpsilon() {
		return epsilon;
	}

	/**
	 * @param epsilon
	 *            the epsilon to set
	 */
	public void setEpsilon(double epsilon) {
		this.epsilon = epsilon;
	}

	/**
	 * @return the padding
	 */
	public Padding getPadding() {
		return padding;
	}

	/**
	 * @param padding
	 *            the padding to set
	 */
	public void setPadding(Padding padding) {
		this.padding = padding;
	}

	/**
	 * @return the apodization
	 */
	public Apodization getApodization() {
		return apodization;
	}

	/**
	 * @param apodization
	 *            the apodization to set
	 */
	public void setApodization(Apodization apodization) {
		this.apodization = apodization;
	}

	/**
	 * @return the monitors
	 */
	public Monitors getMonitors() {
		if (monitors == null)
			return Monitors.createDefaultMonitor();
		return monitors;
	}

	/**
	 * @param monitors
	 *            the monitors to set
	 */
	public void setMonitors(Monitors monitors) {
		this.monitors = monitors;
	}

	/**
	 * @return the verbose
	 */
	public Verbose getVerbose() {
		return verbose;
	}

	/**
	 * @param verbose
	 *            the verbose to set
	 */
	public void setVerbose(Verbose verbose) {
		this.verbose = verbose;
	}

	public Constraint.Mode getConstraint() {
		return constraintMode;
	}

	public void setConstraint(Constraint.Mode constraintMode) {
		doConstraint = constraintMode != Constraint.Mode.NO;
		this.constraintMode = constraintMode;
	}

	/**
	 * @return the stats
	 */
	public Stats getStats() {
		return stats;
	}

	/**
	 * @param stats
	 *            the stats to set
	 */
	public void setStats(Stats stats) {
		this.stats = stats;
	}

	/**
	 * @return the residuMin
	 */
	public double getResiduMin() {
		return residuMin;
	}

	/**
	 * @param residuMin
	 *            the residuMin to set
	 */
	public void setResiduMin(double residuMin) {
		doResidu = residuMin > 0;
		this.residuMin = residuMin;
	}

	/**
	 * @return the timeLimit
	 */
	public double getTimeLimit() {
		return timeLimit;
	}

	/**
	 * @param timeLimit
	 *            the timeLimit to set
	 */
	public void setTimeLimit(double timeLimit) {
		doTime = timeLimit > 0;
		this.timeLimit = timeLimit;
	}

	/**
	 * @return the reference
	 */
	public String getReferenceName() {
		return referenceName;
	}

	/**
	 * @param reference
	 *            the reference to set
	 */
	public void setReferenceName(String referenceName) {
		doReference = false;
		if (referenceName == null)
			return;
		if (referenceName.equals(""))
			return;
		doReference = true;
		this.referenceName = referenceName;
	}

	/**
	 * @return the reference
	 */
	public RealSignal getReference() {
		return refImage;
	}

	/**
	 * @param reference
	 *            the reference to set
	 */
	public void setReference(RealSignal refImage) {
		doReference = false;
		if (refImage == null)
			return;
		doReference = true;
		this.refImage = refImage;
	}

	/**
	 * @return the outs
	 */
	public ArrayList<Output> getOuts() {
		return outs;
	}

	/**
	 * @param outs
	 *            the outs to set
	 */
	public void setOuts(ArrayList<Output> outs) {
		this.outs = outs;
	}

	public void addOutput(Output out) {
		this.outs.add(out);
	}

	public String toStringMonitor() {
		String s = "[" + verbose.name().toLowerCase() + "] ";
		for (AbstractMonitor monitor : monitors) {
			s += "" + monitor.getName() + " ";
		}
		return s;
	}

	public Stats.Mode getStatsMode() {
		return stats.getMode();
	}

	public void setStatsMode(Stats.Mode mode) {
		this.stats = new Stats(mode);
	}

	public String toStringPath() {
		File dir = new File(path);
		if (dir.exists()) {
			if (dir.isDirectory()) {
				if (dir.canWrite())
					return path + " (writable)";
				else
					return path + " (non-writable)";
			}
			else {
				return path + " (non-directory)";
			}
		}
		else {
			return path + " (not-valid)";
		}
	}

	private class Updater extends TimerTask {
		@Override
		public void run() {
			update();
		}
	}
}
