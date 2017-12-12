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

package deconvolutionlab.module;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.File;
import java.io.IOException;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import signal.Constraint;
import bilib.component.GridPanel;
import bilib.tools.Files;
import deconvolution.Command;
import deconvolutionlab.Config;

public class ControllerModule extends AbstractModule implements ActionListener, ChangeListener, KeyListener {

	private JButton				bnBrowse;

	private JTextField			txtReference;
	private JTextField			txtResidu;
	private JTextField			txtTime;
	private JTextField			txtIterations;

	private JComboBox<String>	cmbConstraint;
	private JComboBox<String>	cmbStats;
	private JComboBox<String>	cmbMonitor;
	private JComboBox<String>	cmbVerbose;

	private JCheckBox			chkResidu;
	private JCheckBox			chkReference;
	private JCheckBox			chkTime;

	public ControllerModule() {
		super("Controller", "", "Default", "");
	}

	@Override
	public String getCommand() {
		String cmd = "";
		if (cmbMonitor.getSelectedIndex() != 0)
			cmd += "-monitor " + cmbMonitor.getSelectedItem() + " ";
		if (cmbVerbose.getSelectedIndex() != 0)
			cmd += "-verbose " + cmbVerbose.getSelectedItem() + " ";
		if (cmbStats.getSelectedIndex() != 0)
			cmd += "-stats " + cmbStats.getSelectedItem() + " ";
		if (cmbConstraint.getSelectedIndex() != 0)
			cmd += "-constraint " + cmbConstraint.getSelectedItem() + " ";
		if (chkReference.isSelected())
			cmd += "-reference " +  txtReference.getText() + " ";
		if (chkResidu.isSelected())
			cmd += "-residu " + txtResidu.getText() + " ";
		if (chkTime.isSelected())
			cmd += "-time " + txtTime.getText() + " ";
		return cmd;
	}

	@Override
	public JPanel buildExpandedPanel() {

		chkTime = new JCheckBox("");
		chkResidu = new JCheckBox("");
		chkReference = new JCheckBox("");

		bnBrowse = new JButton("Browse");
		txtReference = new JTextField("");
		txtResidu = new JTextField("0.01");
		txtTime = new JTextField("3600");
		txtIterations = new JTextField("Iteration max (mandatory)");
		txtIterations.setEditable(false);
			
		cmbMonitor = new JComboBox<String>(new String[] {"console table", "console", "table", "no" });
		cmbVerbose = new JComboBox<String>(new String[] {"log", "quiet", "mute", "prolix" });
		cmbConstraint = new JComboBox<String>(Constraint.getContraintsAsArray());
		cmbStats = new JComboBox<String>(new String[] {"no", "show", "show + save", "save"});
		txtReference.setPreferredSize(new Dimension(200, 20));

		GridPanel pn = new GridPanel(true, 2);

		pn.place(0, 0, "monitor");
		pn.place(0, 2, cmbMonitor);
		pn.place(0, 3, "Monitoring message");
		
		pn.place(1, 0, "verbose");
		pn.place(1, 2, cmbVerbose);
		pn.place(1, 3, "");

		pn.place(3, 0, "stats");
		pn.place(3, 2, cmbStats);
		pn.place(3, 3, "Signal's statistics");

		pn.place(4, 0, "constraint");
		pn.place(4, 2, cmbConstraint);
		pn.place(4, 3, "Additional constraint");

		pn.place(5, 0, "residu");
		pn.place(5, 1, chkResidu);
		pn.place(5, 2, txtResidu);
		pn.place(5, 3, "Additional stopping criteria");
		
		pn.place(6, 0, "time");
		pn.place(6, 1, chkTime);
		pn.place(6, 2, txtTime);
		pn.place(6, 3, "Additional stopping criteria");
		
		pn.place(7, 0, "reference");
		pn.place(7, 1, chkReference);
		pn.place(7, 2, 2, 1, txtReference);
		pn.place(8, 2, bnBrowse);
		pn.place(8, 3, 1, 1, "Ground-truth file");

		JScrollPane scroll = new JScrollPane(pn);
		scroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		scroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);

		JPanel panel = new JPanel(new BorderLayout());
		panel.add(scroll, BorderLayout.CENTER);

		// Add drop area
		txtReference.setDropTarget(new LocalDropTarget());
		getCollapsedPanel().setDropTarget(new LocalDropTarget());
		bnBrowse.setDropTarget(new LocalDropTarget());

		Config.register(getName(), "residu.enable", chkResidu, false);
		Config.register(getName(), "reference.enable", chkReference, false);
		Config.register(getName(), "time.enable", chkTime, false);
		
		Config.register(getName(), "reference.value", txtReference, "");
		Config.register(getName(), "residu.value", txtResidu, "0.01");
		Config.register(getName(), "time.value", txtTime, "3600");
		Config.register(getName(), "constraint", cmbConstraint, cmbConstraint.getItemAt(0));
		Config.register(getName(), "stats", cmbStats, cmbStats.getItemAt(0));
		Config.register(getName(), "monitor", cmbMonitor, cmbMonitor.getItemAt(0));
		Config.register(getName(), "verbose", cmbVerbose, cmbVerbose.getItemAt(0));
		
		bnBrowse.addActionListener(this);
		chkResidu.addChangeListener(this);
		chkReference.addChangeListener(this);
		chkTime.addChangeListener(this);

		txtResidu.addKeyListener(this);
		txtReference.addKeyListener(this);
		txtTime.addKeyListener(this);
		cmbConstraint.addActionListener(this);
		cmbMonitor.addActionListener(this);
		cmbVerbose.addActionListener(this);
		getAction1Button().addActionListener(this);

		return panel;
	}

	private void update() {
		
		setCommand(getCommand());
		int count = 0;
		count += (chkResidu.isSelected() ? 1 : 0);
		count += (chkTime.isSelected() ? 1 : 0);
		count += (chkReference.isSelected() ? 1 : 0);
		setSynopsis("" + count + " controls");
		
		Command.command();
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		super.actionPerformed(e);
		if (e.getSource() == bnBrowse) {
			File file = Files.browseFile(Command.getPath());
			if (file != null)
				txtReference.setText(file.getAbsolutePath());
		}
		if (e.getSource() == getAction1Button()) {
			chkResidu.removeChangeListener(this);
			chkReference.removeChangeListener(this);
			chkTime.removeChangeListener(this);
			
			chkResidu.setSelected(false);
			chkReference.setSelected(false);
			chkTime.setSelected(false);
			
			txtReference.setText("");
			txtResidu.setText("0.01");
			txtTime.setText("3600");
			cmbConstraint.setSelectedIndex(0);
			cmbStats.setSelectedIndex(0);
			cmbMonitor.setSelectedIndex(0);
			cmbVerbose.setSelectedIndex(0);
	
			chkResidu.addChangeListener(this);
			chkReference.addChangeListener(this);
			chkTime.addChangeListener(this);
	
		}
		update();
	}

	@Override
	public void stateChanged(ChangeEvent e) {
		update();
	}

	@Override
	public void keyTyped(KeyEvent e) {
	}

	@Override
	public void keyPressed(KeyEvent e) {
	}

	@Override
	public void keyReleased(KeyEvent e) {
		update();
	}
	
	@Override
	public void close() {
		bnBrowse.removeActionListener(this);
		chkReference.removeChangeListener(this);
		chkResidu.removeChangeListener(this);
		cmbVerbose.removeActionListener(this);
		cmbMonitor.removeActionListener(this);
		cmbConstraint.removeActionListener(this);
		chkTime.removeChangeListener(this);
		getAction1Button().removeChangeListener(this);
	}

	public class LocalDropTarget extends DropTarget {

		@Override
		public void drop(DropTargetDropEvent e) {
			e.acceptDrop(DnDConstants.ACTION_COPY);
			e.getTransferable().getTransferDataFlavors();
			Transferable transferable = e.getTransferable();
			DataFlavor[] flavors = transferable.getTransferDataFlavors();
			for (DataFlavor flavor : flavors) {
				if (flavor.isFlavorJavaFileListType()) {
					try {
						List<File> files = (List<File>) transferable.getTransferData(flavor);
						for (File file : files) {
							if (file.isFile()) {
								txtReference.setText(file.getAbsolutePath());
								chkReference.setSelected(true);
								update();
							}
						}
					}
					catch (UnsupportedFlavorException ex) {
						ex.printStackTrace();
					}
					catch (IOException ex) {
						ex.printStackTrace();
					}
				}
			}
			e.dropComplete(true);
			super.drop(e);
		}
	}
}
