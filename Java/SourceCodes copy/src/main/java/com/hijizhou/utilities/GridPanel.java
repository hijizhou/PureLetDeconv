package com.hijizhou.utilities;

import javax.swing.*;
import java.awt.*;

public class GridPanel
  extends JPanel
{
  private GridBagLayout layout = new GridBagLayout();
  private GridBagConstraints constraint = new GridBagConstraints();
  private int defaultSpace = 3;
  
  public GridPanel()
  {
    setLayout(this.layout);
    setBorder(BorderFactory.createEtchedBorder());
  }
  
  public GridPanel(int defaultSpace)
  {
    setLayout(this.layout);
    this.defaultSpace = defaultSpace;
    setBorder(BorderFactory.createEtchedBorder());
  }
  
  public GridPanel(boolean border)
  {
    setLayout(this.layout);
    if (border) {
      setBorder(BorderFactory.createEtchedBorder());
    }
  }
  
  public GridPanel(String title)
  {
    setLayout(this.layout);
    setBorder(BorderFactory.createTitledBorder(title));
  }
  
  public GridPanel(boolean border, int defaultSpace)
  {
    setLayout(this.layout);
    this.defaultSpace = defaultSpace;
    if (border) {
      setBorder(BorderFactory.createEtchedBorder());
    }
  }
  
  public GridPanel(String title, int defaultSpace)
  {
    setLayout(this.layout);
    this.defaultSpace = defaultSpace;
    setBorder(BorderFactory.createTitledBorder(title));
  }
  
  public void setSpace(int defaultSpace)
  {
    this.defaultSpace = defaultSpace;
  }
  
  public void place(int row, int col, JComponent comp)
  {
    place(row, col, 1, 1, this.defaultSpace, comp);
  }
  
  public void place(int row, int col, int space, JComponent comp)
  {
    place(row, col, 1, 1, space, comp);
  }
  
  public void place(int row, int col, int width, int height, JComponent comp)
  {
    place(row, col, width, height, this.defaultSpace, comp);
  }
  
  public void place(int row, int col, int width, int height, int space, JComponent comp)
  {
    this.constraint.gridx = col;
    this.constraint.gridy = row;
    this.constraint.gridwidth = width;
    this.constraint.gridheight = height;
    this.constraint.anchor = 18;
    this.constraint.insets = new Insets(space, space, space, space);
    this.constraint.fill = 2;
    this.layout.setConstraints(comp, this.constraint);
    add(comp);
  }
}
