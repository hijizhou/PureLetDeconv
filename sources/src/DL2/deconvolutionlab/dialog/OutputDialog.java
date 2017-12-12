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

package DL2.deconvolutionlab.dialog;

import DL2.bilib.component.GridPanel;
import DL2.bilib.component.HTMLPane;
import DL2.bilib.component.SpinnerRangeInteger;
import DL2.deconvolutionlab.Imager;
import DL2.deconvolutionlab.output.Output;
import DL2.deconvolutionlab.output.Output.Action;
import DL2.deconvolutionlab.output.Output.Dynamic;
import DL2.deconvolutionlab.output.Output.View;
import ij.gui.GUI;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class OutputDialog extends JDialog implements ActionListener, ChangeListener {

	private JComboBox<String>	cmbDynamic	= new JComboBox<String>(new String[] { "intact", "rescaled", "normalized", "clipped" });
	private JComboBox<String>	cmbType		= new JComboBox<String>(new String[] { "float", "short", "byte" });

	private JCheckBox			chkSave		= new JCheckBox("Save output", true);
	private JCheckBox			chkShow		= new JCheckBox("Show output", true);

	private SpinnerRangeInteger	snpSnapshot	= new SpinnerRangeInteger(0, 0, 99999, 1);
	private JComboBox<String>	cmbSnapshot	= new JComboBox<String>(new String[] { "Final Output", "Specify Iterations..." });

	private SpinnerRangeInteger	spnX		= new SpinnerRangeInteger(128, 0, 99999, 1);
	private SpinnerRangeInteger	spnY		= new SpinnerRangeInteger(128, 0, 99999, 1);
	private SpinnerRangeInteger	spnZ		= new SpinnerRangeInteger(32, 0, 99999, 1);
	private JTextField			txtName		= new JTextField("Noname", 18);

	private JCheckBox			chkCenter	= new JCheckBox("Center of the volume", true);
	private JButton				bnOK		= new JButton("OK");
	private JButton				bnCancel	= new JButton("Cancel");
	private boolean				cancel		= false;
	private JLabel				lblBit		= new JLabel("32-bit");
	private JLabel				lblIter		= new JLabel("iterations");
	private JLabel				lblSnapshot	= new JLabel("Iterations");
	private Output				out;
	private View				view;
	private GridPanel			pnOrtho;
	private HTMLPane			info = new HTMLPane(200, 200);
	
	private static int count	= 1;
	
	public OutputDialog(View view) {
		super(new JFrame(), "Create a new output");
		this.view = view;
		lblBit.setBorder(BorderFactory.createEtchedBorder());
		lblIter.setBorder(BorderFactory.createEtchedBorder());

		txtName.setText(view.name().substring(0, 2) + (count++));
		
		GridPanel pn = new GridPanel(true);
		pn.place(0, 0, "Name");
		pn.place(0, 1, txtName);
		pn.place(1, 0, "Dynamic");
		pn.place(1, 1, cmbDynamic);
		pn.place(2, 0, "Type");
		pn.place(2, 1, cmbType);
		pn.place(3, 1, lblBit);
		
		if (view != View.SERIES && view != View.STACK) {
			pn.place(4, 0,  "Snapshot");
			pn.place(4, 1,  cmbSnapshot);
			pn.place(5, 0, lblSnapshot);
			pn.place(5, 1, snpSnapshot);
			pn.place(6, 1, lblIter);
		}
		pn.place(7, 1, 3, 1, chkShow);
		pn.place(8, 1, 3, 1, chkSave);

		GridPanel main = new GridPanel(false);
		main.place(1, 0, 2, 1, pn);
	
		if (view == View.ORTHO || view == View.FIGURE) {
			pn.place(9, 1, 3, 1, chkCenter);
			pnOrtho = new GridPanel("Keypoint");
			pnOrtho.place(4, 0, "Position in X");
			pnOrtho.place(4, 1, spnX);
			pnOrtho.place(4, 2, "[pixel]");

			pnOrtho.place(5, 0, "Position in Y");
			pnOrtho.place(5, 1, spnY);
			pnOrtho.place(5, 2, "[pixel]");

			pnOrtho.place(6, 0, "Position in Z");
			pnOrtho.place(6, 1, spnZ);
			pnOrtho.place(5, 2, "[pixel]");
			main.place(2, 0, 2, 1, pnOrtho);
		}

		pn.place(10, 0, 2, 1, info.getPane());
		
		main.place(3, 0, bnCancel);
		main.place(3, 1, bnOK);

		info();
		cmbSnapshot.addActionListener(this);
		snpSnapshot.addChangeListener(this);
		chkCenter.addActionListener(this);
		cmbType.addActionListener(this);
		bnOK.addActionListener(this);
		bnCancel.addActionListener(this);
		add(main);
		update();
		pack();
		GUI.center(this);
		setModal(true);
	}
	
	@Override
	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == chkCenter) {
			update();
		}
		else if (e.getSource() == cmbSnapshot) {
			update();
		}
		else if (e.getSource() == cmbType) {
			if (cmbType.getSelectedIndex() == 0)
				lblBit.setText("32-bits");
			if (cmbType.getSelectedIndex() == 1)
				lblBit.setText("16-bits");
			if (cmbType.getSelectedIndex() == 2)
				lblBit.setText("8-bits");
		}
		else if (e.getSource() == bnCancel) {
			dispose();
			cancel = true;
			return;
		}
		else if (e.getSource() == bnOK) {
			Action action = Action.SHOW;
			if (chkShow.isSelected() && chkSave.isSelected())
				action = Action.SHOWSAVE;
			if (!chkShow.isSelected() && chkSave.isSelected())
				action = Action.SAVE;
			String name = txtName.getText();
			out = new Output(view, action, name).frequency(snpSnapshot.get());
			Dynamic dynamic = Output.Dynamic.values()[cmbDynamic.getSelectedIndex()];
			if (dynamic == Dynamic.RESCALED)
				out.rescale();
			if (dynamic == Dynamic.CLIPPED)
				out.clip();
			if (dynamic == Dynamic.NORMALIZED)
				out.normalize();
			Imager.Type type = Imager.Type.values()[cmbType.getSelectedIndex()];
			if (type == Imager.Type.BYTE)
				out.toByte();
			if (type == Imager.Type.SHORT)
				out.toShort();
			if (type == Imager.Type.FLOAT)
				out.toFloat();	
			if (!chkCenter.isSelected()) 
				out.origin(spnX.get(), spnY.get(), spnZ.get());
			cancel = false;
			dispose();
		}
	}

	private void update() {
		
		if (cmbSnapshot.getSelectedIndex() == 0) {
			snpSnapshot.set(0);
			lblSnapshot.setEnabled(false);
			lblIter.setEnabled(false);
			lblSnapshot.setEnabled(false);
		}
		else {
			lblSnapshot.setEnabled(true);
			lblIter.setEnabled(true);
			lblSnapshot.setEnabled(true);
		}
		if (snpSnapshot.get() == 0)
			lblIter.setText("at the end (default)");
		else
			lblIter.setText("every " + snpSnapshot.get() + " iterations");

		if (snpSnapshot.get() == 0)
			lblIter.setText("at the end (default)");
		else
			lblIter.setText("every " + snpSnapshot.get() + " iterations");

		boolean b = !chkCenter.isSelected();
		if (pnOrtho != null) {
			pnOrtho.setEnabled(b);
			for (Component c : pnOrtho.getComponents())
				c.setEnabled(b);
		}
		pack();
	}

	@Override
	public void stateChanged(ChangeEvent e) {
		if (e.getSource() == snpSnapshot)
			update();
	}

	public Output getOut() {
		return out;
	}

	public boolean wasCancel() {
		return cancel;
	}
	
	private void info() {
		if (view == View.FIGURE) {
			info.append("h1", "figure");
			info.append("h2", "Create a view with 2 panels (XY) and (YZ) with a border.");
		}
		if (view == View.MIP) {
			info.append("h1", "mip");
			info.append("h2", "Create a view 3 orthogonal projections.");
		}
		if (view == View.ORTHO) {
			info.append("h1", "ortho");
			info.append("h2", "Create a view 3 orthogonal section centered on the origin.");
		}
		if (view == View.PLANAR) {
			info.append("h1", "ortho");
			info.append("h2", "Create a montage of all Z-slice in one large flatten plane.");
		}
		if (view == View.STACK) {
			info.append("h1", "stack");
			info.append("h2", "Create a z-stack of image.");
		}
		if (view == View.SERIES) {
			info.append("h1", "series");
			info.append("h2", "Create a series of z-slices.");
		}

		info.append("p", "<b>Name:</b> This string will be used as title of the window in <i>show</i> mode or a filename in <i>save</i> mode.");
		info.append("p", "<b>Dynamic:</b> Select the dynamic range used for the display. The default value is <i>intact</i> which preserves the true values.");
		info.append("p", "<b>Type:</b> Select the data type. The default value is <i>float</i> which preserves the true values without loosing precision.");
		info.append("p", "<b>Snapshot:</b> The output is usually shown (or saved) at the end of the processing, optionally it is possible to specify to show (or save) every N iterations.");
	}

}
