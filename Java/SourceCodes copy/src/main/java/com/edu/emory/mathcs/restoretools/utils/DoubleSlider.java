/* Evolvo - Image Generator
 * Copyright (C) 2000 Andrew Molloy
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.

 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.

 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */

/**
 *  $Id: DoubleSlider.java,v 1.1.1.1 2002/10/04 15:39:33 maloi Exp $
 */

package com.edu.emory.mathcs.restoretools.utils;

import com.edu.emory.mathcs.restoretools.utils.DoubleBoundedRangeModel;

import java.util.Dictionary;
import java.util.Enumeration;
import java.util.Hashtable;

import javax.swing.JSlider;

/**
 * Creates a slider which operates on a range of doubles instead of ints.
 * 
 * This actually uses a little trickery so that the JSlider class didn't have to
 * be rewritten. All values are actually stored as integers, and multiplied or
 * divided by a multiplier to translate from the underlaying Slider's methods
 * that deal with integers and this objects methods that deal with doubles. The
 * multiplier is a power of 10, and determines the accuracy of the slider.
 */
public class DoubleSlider extends JSlider {
    private static final long serialVersionUID = -1027614687975303073L;

    /** Class constructor. */
    public DoubleSlider(DoubleBoundedRangeModel dbrm) {
        super(dbrm);
    }

    /**
     * Default constructor. Creates a JDoubleSlider with some default
     * parameters.
     */
    public DoubleSlider() {
        // Defaults to min=0.0, max=1.0, extent=0.0, value=0.0, precision=2
        this(0.0, 0.0, 0.0, 1.0, 2);
    }

    /**
     * Class constructor that takes all parameters but precision, defaulting to
     * a precision of 2 decimal places.
     */
    public DoubleSlider(double v, double e, double minimum, double maximum) {
        // Default precision of 2 decimal points
        this(v, e, minimum, maximum, 2);
    }

    /** Class constructor that takes all parameters. */
    public DoubleSlider(double v, double e, double minimum, double maximum, int precision) {
        super();
        DoubleBoundedRangeModel dbrm = new DoubleBoundedRangeModel(v, e, minimum, maximum, precision);

        setModel(dbrm);
    }

    /** Returns the double value of the slider. */
    public double getDoubleValue() {
        DoubleBoundedRangeModel dbrm = (DoubleBoundedRangeModel) this.getModel();
        return dbrm.getDoubleValue();
    }

    /** Sets the double value of the slider. */
    public void setValue(double v) {
        DoubleBoundedRangeModel dbrm = (DoubleBoundedRangeModel) this.getModel();
        dbrm.setDoubleValue(v);
    }

    /** Sets the major tick spacing for the slider from a double. */
    public void setMajorTickSpacing(double ts) {
        DoubleBoundedRangeModel dbrm = (DoubleBoundedRangeModel) this.getModel();
        super.setMajorTickSpacing((int) (ts * dbrm.getMultiplier()));
    }

    /** Returns the major tick spacing for the slider as a double. */
    public double getDoubleMajorTickSpacing() {
        DoubleBoundedRangeModel dbrm = (DoubleBoundedRangeModel) this.getModel();
        return (double) super.getMajorTickSpacing() / (double) dbrm.getMultiplier();
    }

    /** Sets the minor tick spacing from a double. */
    public void setMinorTickSpacing(double ts) {
        DoubleBoundedRangeModel dbrm = (DoubleBoundedRangeModel) this.getModel();
        super.setMinorTickSpacing((int) (ts * dbrm.getMultiplier()));
    }

    /** Returns the minor tick spacing as a double. */
    public double getDoubleMinorTickSpacing() {
        DoubleBoundedRangeModel dbrm = (DoubleBoundedRangeModel) this.getModel();
        return (double) super.getMinorTickSpacing() / (double) dbrm.getMultiplier();
    }

    /** Sets the extend from a double. */
    public void setExtent(double e) {
        DoubleBoundedRangeModel dbrm = (DoubleBoundedRangeModel) this.getModel();
        super.setExtent((int) (e * dbrm.getMultiplier()));
    }

    /** Returns the extent as a double. */
    public double getDoubleExtent() {
        DoubleBoundedRangeModel dbrm = (DoubleBoundedRangeModel) this.getModel();
        return (double) super.getExtent() / (double) dbrm.getMultiplier();
    }

    public void setLabelTable(Dictionary labels) {
        Dictionary translatedLabels = new Hashtable();
        Double oldValue;
        Integer newValue;

        for (Enumeration e = labels.keys(); e.hasMoreElements();) {
            oldValue = (Double) e.nextElement();
            newValue = new Integer((int) (oldValue.doubleValue() * ((DoubleBoundedRangeModel) (this.getModel())).getMultiplier()));
            translatedLabels.put(newValue, labels.get(oldValue));
        }

        super.setLabelTable(translatedLabels);
    }
}
