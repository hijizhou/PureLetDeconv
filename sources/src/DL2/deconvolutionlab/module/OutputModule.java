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

package DL2.deconvolutionlab.module;

import DL2.bilib.table.CustomizedColumn;
import DL2.bilib.table.CustomizedTable;
import DL2.deconvolution.Command;
import DL2.deconvolutionlab.Config;
import DL2.deconvolutionlab.Constants;
import DL2.deconvolutionlab.Lab;
import DL2.deconvolutionlab.dialog.OutputDialog;
import DL2.deconvolutionlab.output.Output;
import DL2.deconvolutionlab.output.Output.View;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;

public class OutputModule extends AbstractModule implements ActionListener, MouseListener {

	private CustomizedTable	table;
	private JButton			bnStack;
	private JButton			bnSeries;
	private JButton			bnMIP;
	private JButton			bnOrtho;
	private JButton			bnPlanar;
	private JButton			bnFigure;
	private JCheckBox			chkDisplayFinal;
	
	public OutputModule() {
		super("Output", "", "Clear", "");
	}

	@Override
	public String getCommand() {
		String cmd = " ";
		if (table == null)
			return cmd;
		for (int i = 0; i < table.getRowCount(); i++) {
			String[] values = new String[table.getColumnCount()];
			for(int c=0; c<table.getColumnCount(); c++)
				values[c] = table.getCell(i, c) == null ? "" : table.getCell(i, c).trim();
			cmd += " -out " + values[0] + " " + values[1] + " " + values[2] + " " + values[3] + " " +  values[4];
			if (values[5].equals(""))
				cmd += " noshow";
			if (values[6].equals(""))
				cmd += " nosave";
		}
		if (!chkDisplayFinal.isSelected())
			cmd += " -display no";
		return cmd;
	}

	public void update() {
		setCommand(getCommand());
		int count = table.getRowCount() + (chkDisplayFinal.isSelected() ? 1 : 0);
		setSynopsis(count + " output" + (count > 1 ? "s" : ""));
		Command.command();
	}

	@Override
	public JPanel buildExpandedPanel() {
		chkDisplayFinal = new JCheckBox("Display the final output as 32-bit stack (default)");
		chkDisplayFinal.setSelected(true);
		
		String[] dynamics = { "intact", "rescaled", "normalized", "clipped" };
		String[] types = { "float", "short", "byte" };

		ArrayList<CustomizedColumn> columns = new ArrayList<CustomizedColumn>();
		columns.add(new CustomizedColumn("Mode", String.class, 80, false));
		columns.add(new CustomizedColumn("Name", String.class, Constants.widthGUI, true));
		columns.add(new CustomizedColumn("Dynamic", String.class, 100, dynamics, "Select the dynamic range"));
		columns.add(new CustomizedColumn("Type", String.class, 100, types, "Select the type"));
		columns.add(new CustomizedColumn("Origin", String.class, 120, false));
		columns.add(new CustomizedColumn("Show", String.class, 50, false));
		columns.add(new CustomizedColumn("Save", String.class, 50, false));
		columns.add(new CustomizedColumn("Del", String.class, 30, "\u232B", "Delete this image source"));
		table = new CustomizedTable(columns, true);
		table.getColumnModel().getColumn(5).setMaxWidth(50);
		table.getColumnModel().getColumn(6).setMaxWidth(50);
		table.getColumnModel().getColumn(7).setMaxWidth(30);
		table.getColumnModel().getColumn(0).setMaxWidth(100);
		table.getColumnModel().getColumn(2).setMaxWidth(100);
		table.getColumnModel().getColumn(3).setMaxWidth(100);

		table.addMouseListener(this);
		bnStack 	= new JButton("\u2295 stack");
		bnSeries 	= new JButton("\u2295 series");
		bnMIP 		= new JButton("\u2295 mip");
		bnOrtho		= new JButton("\u2295 ortho");
		bnPlanar 	= new JButton("\u2295 planar");
		bnFigure 	= new JButton("\u2295 figure");
		
		JToolBar pn = new JToolBar("Controls Image");
		pn.setBorder(BorderFactory.createEmptyBorder());
		pn.setLayout(new GridLayout(1, 6));
		pn.setFloatable(false);
		pn.add(bnStack);
		pn.add(bnSeries);
		pn.add(bnMIP);
		pn.add(bnOrtho);
		pn.add(bnPlanar);
		pn.add(bnFigure);
	
		JPanel panel = new JPanel();
		panel.setBorder(BorderFactory.createEtchedBorder());
		panel.setLayout(new BorderLayout());
		panel.add(chkDisplayFinal, BorderLayout.NORTH);
		panel.add(pn, BorderLayout.SOUTH);
		panel.add(table.getMinimumPane(100, 100), BorderLayout.CENTER);

		bnStack.addActionListener(this);
		bnSeries.addActionListener(this);
		bnMIP.addActionListener(this);
		bnOrtho.addActionListener(this);
		bnPlanar.addActionListener(this);
		bnFigure.addActionListener(this);		
		chkDisplayFinal.addActionListener(this);
		getAction1Button().addActionListener(this);
		Config.registerTable(getName(), "output", table);
		Config.register(getName(), "display", chkDisplayFinal, true);
	
		return panel;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		super.actionPerformed(e);
		View view = null;
		if (e.getSource() == bnStack)
			view = View.STACK;
		else if (e.getSource() == bnSeries)
			view = View.SERIES;
		else if (e.getSource() == bnMIP)
			view = View.MIP;
		else if (e.getSource() == bnOrtho)
			view = View.ORTHO;
		else if (e.getSource() == bnPlanar)
			view = View.PLANAR;
		else if (e.getSource() == bnFigure)
			view = View.FIGURE;
		
		if (view != null) {
			OutputDialog dlg = new OutputDialog(view);
			Lab.setVisible(dlg, true);
			if (dlg.wasCancel())
				return;
			
			Output out = dlg.getOut();
			if (out != null)
				table.insert(out.getAsString());
			update();
		}
		
		if (e.getSource() == getAction1Button()) {
			table.removeRows();
			chkDisplayFinal.setSelected(true);
		}
		if (e.getSource() == chkDisplayFinal) {
			update();
		}
	}

	@Override
	public void mouseClicked(MouseEvent e) {
		int row = table.getSelectedRow();
		if (table.getSelectedColumn() == 7) {
			table.removeRow(row);
			if (table.getRowCount() > 0)
				table.setRowSelectionInterval(0, 0);
		}
		update();
		Command.command();
	}

	@Override
	public void mousePressed(MouseEvent e) {
	}

	@Override
	public void mouseReleased(MouseEvent e) {
	}

	@Override
	public void mouseEntered(MouseEvent e) {
	}

	@Override
	public void mouseExited(MouseEvent e) {
	}

	@Override
	public void close() {
		chkDisplayFinal.removeActionListener(this);
		bnStack.removeActionListener(this);
		bnSeries.removeActionListener(this);
		bnMIP.removeActionListener(this);
		bnOrtho.removeActionListener(this);
		bnPlanar.removeActionListener(this);
		bnFigure.removeActionListener(this);
		getAction1Button().removeActionListener(this);
		getAction2Button().removeActionListener(this);
	}
}
