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
 *  $Id: DoubleBoundedRangeModel.java,v 1.2 2002/10/07 15:20:22 maloi Exp $
 */

package com.edu.emory.mathcs.restoretools.utils;

import javax.swing.DefaultBoundedRangeModel;

/**
 * This class is an extension to DefaultBoundedRangeModel which allows the range
 * to be over double-sized floating point numbers. In order to maintain the
 * standard interface of BoundedRangeModel, this class stores numbers only to a
 * desired precision. When one of the methods defined by the BoundedRangeModel
 * interface are called, they return their expected value multiplied by
 * 10^precision. There are also accessor methods to set and retrieve the actual
 * floating point numbers. I chose to extend DefaultBoundedRangeModel rather
 * than implementing BoundedRangeModel to avoid rewriting code for the event
 * handlers. No need to reinvent the wheel...
 * 
 */
class DoubleBoundedRangeModel extends DefaultBoundedRangeModel {
    private static final long serialVersionUID = -2439392833440892942L;

    /** The actual lower boundary. */
    private double dblMinimum;

    /** The actual upper boundary. */
    private double dblMaximum;

    /** The actual extent. */
    private double dblExtent;

    /**
     * The multiplier used to convert between our actual (double) space and the
     * integer space.
     */
    private int multiplier;

    /** Number of decimal places to keep precision to. */
    private int precision;

    /**
     * Default constructor. Initializes the model with a min of 0, max of 1,
     * value and extent of 0, and a precision of 10 decimal points.
     */
    public DoubleBoundedRangeModel() {
        this(0.0, 0.0, 0.0, 1.0, 10);
    }

    /**
     * Constructor that takes all parameters except precision, which defaults to
     * 10 decimal places.
     */
    public DoubleBoundedRangeModel(double v, double e, double minimum, double maximum) {
        this(v, e, minimum, maximum, 10);

    }

    /** Constructor for which all values have been provided by the user. */
    public DoubleBoundedRangeModel(double v, double e, double minimum, double maximum, int precision) {
        doSetRangeProps(v, e, minimum, maximum, precision);
    }

    /**
     * Changes the Set Range Properties. This involves ensuring that all ranges
     * are valid, and that maximum >= minimum, minimum <= maximum, and minimum
     * <= value <= (maximum - extent)
     */
    private void doSetRangeProps(double v, double e, double minimum, double maximum, int p) {
        double dblValue;

        if (minimum > maximum) {
            maximum = minimum;
        }
        if (maximum < minimum) {
            minimum = maximum;
        }
        if ((v + e) > maximum) {
            v = maximum - e;
        }
        if (v < minimum) {
            v = minimum;
        }

        precision = p;
        multiplier = new Double(Math.pow(10, p)).intValue();

        dblMinimum = minimum;
        setMinimum((int) (dblMinimum * multiplier));
        dblMaximum = maximum;
        setMaximum((int) (dblMaximum * multiplier));
        dblValue = v;
        setValue((int) (dblValue * multiplier));
        dblExtent = e;
        setExtent((int) (dblExtent * multiplier));
    }

    /**
     * Changes the Set Range Properties, and fires events to alert listeners of
     * the change.
     */
    public void setRangeProperties(double v, double e, double minimum, double maximum, int precision, boolean newValueIsAdjusting) {
        doSetRangeProps(v, e, minimum, maximum, precision);
        setValueIsAdjusting(newValueIsAdjusting);
        fireStateChanged();
    }

    /** Creates a human readable String describing the range model. */
    public String toString() {
        StringBuffer sb = new StringBuffer("DoubleBoundedRangeModel[value=");
        sb.append(new Double((double) getValue() / (double) multiplier).toString());
        sb.append(", extent=");
        sb.append(new Double(dblExtent).toString());
        sb.append(", minimum=");
        sb.append(new Double(dblMinimum).toString());
        sb.append(", maximum=");
        sb.append(new Double(dblMaximum).toString());
        sb.append(", precision=");
        sb.append(new Integer(precision).toString());
        sb.append(", multiplier=");
        sb.append(new Integer(multiplier).toString());
        sb.append(", adj=");
        sb.append(new Boolean(getValueIsAdjusting()).toString());
        sb.append("]");

        return sb.toString();
    }

    /** Returns the currently stored value as a double. */
    public double getDoubleValue() {
        return (double) getValue() / (double) multiplier;
    }

    /** Sets the value from a double. */
    public void setDoubleValue(double v) {
        if ((v + dblExtent) > dblMaximum) {
            v = dblMaximum - dblExtent;
        }
        if (v < dblMinimum) {
            v = dblMinimum;
        }
        double dblValue = v;
        setValue((int) (dblValue * multiplier));
    }

    /** Returns the multiplier. */
    public int getMultiplier() {
        return multiplier;
    }

    public void setPrecision(int p) {
        doSetRangeProps(getDoubleValue(), dblExtent, dblMinimum, dblMaximum, p);
    }

    public int getPrecision() {
        return precision;
    }
}
