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

package DL2.deconvolution.capsule;

import DL2.bilib.component.PanelImage;
import DL2.bilib.table.CustomizedTable;
import DL2.bilib.tools.NumFormat;
import DL2.deconvolution.Deconvolution;
import DL2.deconvolution.Features;
import DL2.signal.RealSignal;
import DL2.signal.SignalCollector;
import DL2.signal.apodization.Apodization;
import DL2.signal.padding.Padding;

import javax.swing.*;
import java.awt.*;

/**
 * This class is a information module for the image source.
 * 
 * @author Daniel Sage
 *
 */
public class ImageCapsule extends AbstractCapsule implements Runnable {

	private PanelImage pnImage;
	private CustomizedTable table;
	
	public ImageCapsule(Deconvolution deconvolution) {
		super(deconvolution);
		pnImage = new PanelImage();
		table = new CustomizedTable(new String[] { "Features", "Values" }, false);
		pnImage.setPreferredSize(new Dimension(300, 300));
		split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, table.getPane(300, 300), pnImage);
	}
	
	@Override
	public void update() {
		split.setDividerLocation(300);
		if (pnImage == null)
			return;
		if (table == null)
			return;
		
		table.removeRows();
		table.append(new String[] {"Image", "Waiting for loading ..."});
		Thread thread = new Thread(this);
		thread.setPriority(Thread.MIN_PRIORITY);
		thread.start();
	}

	@Override
	public String getID() {
		return "Image";
	}
	
	@Override
	public void run() {
		Features features = new Features();
	
		if (deconvolution.image == null) {
			startAsynchronousTimer("Open image", 200);
			deconvolution.image = deconvolution.openImage();
			stopAsynchronousTimer();
		}
		
		if (deconvolution.image == null) {
			features.add("Image", "No valid input image");
			return;
		}
		
		if (deconvolution.getController().getPadding() == null) {
			features.add("Padding", "No valid padding");
			return;
		}
		
		if (deconvolution.getController().getApodization() == null) {
			features.add("Apodization", "No valid apodization");
			return;
		}

		startAsynchronousTimer("Open image", 200);

		float stati[] = deconvolution.image.getStats();
		int sizi = deconvolution.image.nx * deconvolution.image.ny * deconvolution.image.nz;
		float totali = stati[0] * sizi;
		features.add("<html><b>Orignal Image</b></html>", "");
		features.add("Size", deconvolution.image.dimAsString() + " " + NumFormat.bytes(sizi*4));
		features.add("Mean (stdev)", NumFormat.nice(stati[0])  + " (" + NumFormat.nice(stati[3]) + ")");
		features.add("Extrema (min, max)", NumFormat.nice(stati[1]) + ", " + NumFormat.nice(stati[2]));
		features.add("Energy (integral)", NumFormat.nice(stati[5])  + " (" + NumFormat.nice(totali) + ")");
		table.removeRows();
		for (String[] feature : features)
			table.append(feature);
		Padding pad = deconvolution.getController().getPadding();
		Apodization apo = deconvolution.getController().getApodization();
		RealSignal signal = pad.pad(deconvolution.getMonitors(), deconvolution.image);
		apo.apodize(deconvolution.getMonitors(), signal);
		float stats[] = signal.getStats();
		int sizs = signal.nx * signal.ny * signal.nz;
		float totals = stats[0] * sizs;

		double incpad = (double)((sizs-sizi)/sizi*100.0);
		features.add("<html><b>Working Image</b></html>", "");
		features.add("Size", signal.dimAsString() + " " + NumFormat.bytes(sizs*4));
		features.add("Mean (stdev)", NumFormat.nice(stats[0])  + " (" + NumFormat.nice(stats[3]) + ")");
		features.add("Extrema (min, max)", NumFormat.nice(stats[1]) + ", " + NumFormat.nice(stats[2]));
		features.add("Energy (integral)", NumFormat.nice(stats[5])  + " (" + NumFormat.nice(totals) + ")");
		
		features.add("<html><b>Information</b></html>", "");
		features.add("Size increase (pad.)", NumFormat.nice(incpad) + "%");
		features.add("Energy lost (apo.)", NumFormat.nice((stats[5]-stati[5])/stati[5]*100));
		SignalCollector.free(signal);
		table.removeRows();
		for (String[] feature : features)
			table.append(feature);
		
		pnImage.setImage(deconvolution.image.preview());
		stopAsynchronousTimer();

	}	
}