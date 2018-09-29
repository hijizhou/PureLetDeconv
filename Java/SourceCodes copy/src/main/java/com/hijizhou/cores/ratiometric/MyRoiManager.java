package com.hijizhou.cores.ratiometric;

import ij.*;
import ij.gui.*;
import ij.io.*;
import ij.macro.Interpreter;
import ij.macro.MacroRunner;
import ij.measure.Calibration;
import ij.measure.ResultsTable;
import ij.plugin.Colors;
import ij.plugin.OverlayCommands;
import ij.plugin.OverlayLabels;
import ij.plugin.filter.Analyzer;
import ij.plugin.filter.Filler;
import ij.plugin.filter.ThresholdToSelection;
import ij.plugin.frame.PlugInFrame;
import ij.plugin.frame.Recorder;
import ij.process.ByteProcessor;
import ij.process.ImageProcessor;
import ij.process.ImageStatistics;
import ij.util.Tools;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.Rectangle2D;
import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Hashtable;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

public class MyRoiManager
        extends PlugInFrame
        implements ActionListener, ItemListener, MouseListener, MouseWheelListener, ListSelectionListener
{
    public static final String LOC_KEY = "manager.loc";
    private static final int BUTTONS = 11;
    private static final int DRAW = 0;
    private static final int FILL = 1;
    private static final int LABEL = 2;
    private static final int SHOW_ALL = 0;
    private static final int SHOW_NONE = 1;
    private static final int LABELS = 2;
    private static final int NO_LABELS = 3;
    private static final int MENU = 0;
    private static final int COMMAND = 1;
    private static final int IGNORE_POSITION = -999;
    private static final int CHANNEL = 0;
    private static final int SLICE = 1;
    private static final int FRAME = 2;
    private static final int SHOW_DIALOG = 3;
    private static int rows = 15;
    private static int lastNonShiftClick = -1;
    private static boolean allowMultipleSelections = true;
    private static String moreButtonLabel = "More ";
    private Panel panel;
    private static Frame instance;
    private static int colorIndex = 4;
    private JList list;
    private DefaultListModel listModel;
    private ArrayList rois = new ArrayList();
    private boolean canceled;
    private boolean macro;
    private boolean ignoreInterrupts;
    private PopupMenu pm;
    private Button moreButton;
    private Button colorButton;
    private Checkbox showAllCheckbox = new Checkbox("Show All", false);
    private Checkbox labelsCheckbox = new Checkbox("Labels", false);
    private static boolean measureAll = true;
    private static boolean onePerSlice = true;
    private static boolean restoreCentered;
    private int prevID;
    private boolean noUpdateMode;
    private int defaultLineWidth = 1;
    private Color defaultColor;
    private boolean firstTime = true;
    private int[] selectedIndexes;
    private boolean appendResults;
    private ResultsTable mmResults;
    private int imageID;
    private boolean allowRecording;
    private boolean recordShowAll = true;

    public MyRoiManager()
    {
        super("Analysis");
        if (instance != null)
        {
            WindowManager.toFront(instance);
            return;
        }
        if ((IJ.isMacro()) && (Interpreter.getBatchModeRoiManager() != null)) {
            return;
        }
        instance = this;
        this.list = new JList();
        showWindow();
    }

    public MyRoiManager(boolean b)
    {
        super("Analysis");
        this.list = new JList();
        this.listModel = new DefaultListModel();
        this.list.setModel(this.listModel);
    }

    void showWindow()
    {
        ImageJ ij = IJ.getInstance();
        addKeyListener(ij);
        addMouseListener(this);
        addMouseWheelListener(this);
        WindowManager.addWindow(this);

        setLayout(new BorderLayout());
        this.listModel = new DefaultListModel();
        this.list.setModel(this.listModel);
        this.list.setPrototypeCellValue("0000-0000-0000 ");
        this.list.addListSelectionListener(this);
        this.list.addKeyListener(ij);
        this.list.addMouseListener(this);
        this.list.addMouseWheelListener(this);
        if (IJ.isLinux()) {
            this.list.setBackground(Color.white);
        }
        JScrollPane scrollPane = new JScrollPane(this.list, 22, 31);
        add("Center", scrollPane);
        this.panel = new Panel();
        int nButtons = 11;
        this.panel.setLayout(new GridLayout(nButtons, 1, 5, 0));
        addButton("Add");
        addButton("Update");
        addButton("Delete");
        addButton("Deselect");
        addButton("Measure");
        addButton("Multi Measure");
        addButton("List");
        addButton("Plot Mean");
//        addButton(moreButtonLabel);
        this.showAllCheckbox.addItemListener(this);
        this.panel.add(this.showAllCheckbox);
        this.labelsCheckbox.addItemListener(this);
        this.panel.add(this.labelsCheckbox);
        add("East", this.panel);
        addPopupMenu();
        pack();
        Dimension size = getSize();
        if (size.width > 270) {
            setSize(size.width - 40, size.height);
        }
        this.list.remove(0);
        Point loc = Prefs.getLocation("manager.loc");
        if (loc != null) {
            setLocation(loc);
        } else {
            GUI.center(this);
        }
        show();
    }

    void addButton(String label)
    {
        Button b = new Button(label);
        b.addActionListener(this);
        b.addKeyListener(IJ.getInstance());
        b.addMouseListener(this);
        if (label.equals(moreButtonLabel)) {
            this.moreButton = b;
        }
        this.panel.add(b);
    }

    void addPopupMenu()
    {
        this.pm = new PopupMenu();

        addPopupItem("Open...");
        addPopupItem("Save...");
        addPopupItem("Fill");
        addPopupItem("Draw");
        addPopupItem("AND");
        addPopupItem("OR (Combine)");
        addPopupItem("XOR");
        addPopupItem("Split");
        addPopupItem("Add Particles");
        addPopupItem("Multi Measure");
        addPopupItem("Multi Plot");
        addPopupItem("Sort");
        addPopupItem("Specify...");
        addPopupItem("Remove Positions...");
        addPopupItem("Labels...");
        addPopupItem("List");
        addPopupItem("Interpolate ROIs");
        addPopupItem("Translate...");
        addPopupItem("Help");
        addPopupItem("Options...");
        add(this.pm);
    }

    void addPopupItem(String s)
    {
        MenuItem mi = new MenuItem(s);
        mi.addActionListener(this);
        this.pm.add(mi);
    }

    public void actionPerformed(ActionEvent e)
    {
        String label = e.getActionCommand();
        if (label == null) {
            return;
        }
        String command = label;
        this.allowRecording = true;
        if (command.equals("Add"))
        {
            runCommand("add");
        }
        else if (command.equals("Update"))
        {
            update(true);
        }
        else if (command.equals("Delete"))
        {
            delete(false);
        }
        else if (command.equals("Rename..."))
        {
            rename(null);
        }
        else if (command.equals("Properties..."))
        {
            setProperties(null, -1, null);
        }
        else if (command.equals("Flatten [F]"))
        {
            flatten();
        }
        else if (command.equals("Measure"))
        {
            measure(0);
        }
        else if (command.equals("Open..."))
        {
            open(null);
        }
        else if (command.equals("Save..."))
        {
            Thread t1 = new Thread(new Runnable()
            {
                public void run()
                {
                    MyRoiManager.this.save();
                }
            });
            t1.start();
        }
        else if (command.equals("Fill"))
        {
            drawOrFill(1);
        }
        else if (command.equals("Draw"))
        {
            drawOrFill(0);
        }
        else if (command.equals("Deselect"))
        {
            deselect();
        }
        else if (command.equals(moreButtonLabel))
        {
            Point ploc = this.panel.getLocation();
            Point bloc = this.moreButton.getLocation();
            this.pm.show(this, ploc.x, bloc.y);
        }
        else if (command.equals("OR (Combine)"))
        {
            combine();
        }
        else if (command.equals("Split"))
        {
            split();
        }
        else if (command.equals("AND"))
        {
            and();
        }
        else if (command.equals("XOR"))
        {
            xor();
        }
        else if (command.equals("Add Particles"))
        {
            addParticles();
        }
        else if (command.equals("Multi Measure"))
        {
            multiMeasure("");
        }
        else if (command.equals("Multi Plot"))
        {
            multiPlot();
        }
        else if (command.equals("Sort"))
        {
            sort();
        }
        else if (command.equals("Specify..."))
        {
            specify();
        }
        else if (command.equals("Remove Positions..."))
        {
            removePositions(3);
        }
        else if (command.equals("Labels..."))
        {
            labels();
        }
        else if (command.equals("List"))
        {
            listRois();
        }
        else if (command.equals("Interpolate ROIs"))
        {
            interpolateRois();
        }
        else if (command.equals("Translate..."))
        {
            translate();
        }
        else if (command.equals("Help"))
        {
            help();
        }
        else if (command.equals("Options..."))
        {
            options();
        }
        else if (command.equals("\"Show All\" Color..."))
        {
            setShowAllColor();
        }
        else if (command.equals("Plot Mean"))
        {
            PlotMean();
        }
        this.allowRecording = false;
    }

    private void interpolateRois()
    {
        IJ.runPlugIn("ij.plugin.RoiInterpolator", "");
        if (record()) {
            Recorder.record("roiManager", "Interpolate ROIs");
        }
    }

    public void itemStateChanged(ItemEvent e)
    {
        Object source = e.getSource();
        boolean showAllMode = this.showAllCheckbox.getState();
        if (source == this.showAllCheckbox)
        {
            if ((this.firstTime) && (okToSet())) {
                this.labelsCheckbox.setState(true);
            }
            showAll(this.showAllCheckbox.getState() ? 0 : 1);
            if ((Recorder.record) && (this.recordShowAll)) {
                if (showAllMode) {
                    Recorder.record("roiManager", "Show All");
                } else {
                    Recorder.record("roiManager", "Show None");
                }
            }
            this.recordShowAll = true;
            this.firstTime = false;
            return;
        }
        if (source == this.labelsCheckbox)
        {
            if ((this.firstTime) && (okToSet())) {
                this.showAllCheckbox.setState(true);
            }
            boolean editState = this.labelsCheckbox.getState();
            boolean showAllState = this.showAllCheckbox.getState();
            if ((!showAllState) && (!editState))
            {
                showAll(1);
            }
            else
            {
                showAll(editState ? 2 : 3);
                if (Recorder.record) {
                    if (editState) {
                        Recorder.record("roiManager", "Show All with labels");
                    } else if (showAllState) {
                        Recorder.record("roiManager", "Show All without labels");
                    }
                }
                if ((editState) && (!showAllState) && (okToSet()))
                {
                    this.showAllCheckbox.setState(true);
                    this.recordShowAll = false;
                }
            }
            this.firstTime = false;
            return;
        }
    }

    private boolean okToSet()
    {
        return (!IJ.isMacOSX()) || (!IJ.isJava18());
    }

    void add(boolean shiftKeyDown, boolean altKeyDown)
    {
        if (shiftKeyDown) {
            addAndDraw(altKeyDown);
        } else if (altKeyDown) {
            addRoi(true);
        } else {
            addRoi(false);
        }
    }

    public void addRoi(Roi roi)
    {
        addRoi(roi, false, null, -1);
    }

    boolean addRoi(boolean promptForName)
    {
        return addRoi(null, promptForName, null, 64537);
    }

    boolean addRoi(Roi roi, boolean promptForName, Color color, int lineWidth)
    {
        ImagePlus imp = roi == null ? getImage() : WindowManager.getCurrentImage();
        if (roi == null)
        {
            if (imp == null) {
                return false;
            }
            roi = imp.getRoi();
            if (roi == null)
            {
                error("The active image does not have a selection.");
                return false;
            }
        }
        if (((roi instanceof PolygonRoi)) && (((PolygonRoi)roi).getNCoordinates() == 0)) {
            return false;
        }
        if ((color == null) && (roi.getStrokeColor() != null)) {
            color = roi.getStrokeColor();
        } else if ((color == null) && (this.defaultColor != null)) {
            color = this.defaultColor;
        }
        boolean ignorePosition = false;
        if (lineWidth == 64537)
        {
            ignorePosition = true;
            lineWidth = -1;
        }
        if (lineWidth < 0)
        {
            int sw = (int)roi.getStrokeWidth();
            lineWidth = sw > 1 ? sw : this.defaultLineWidth;
        }
        if (lineWidth > 100) {
            lineWidth = 1;
        }
        int n = getCount();
        int position = (imp != null) && (!ignorePosition) ? roi.getPosition() : 0;
        int saveCurrentSlice = imp != null ? imp.getCurrentSlice() : 0;
        if ((position > 0) && (position != saveCurrentSlice)) {
            imp.setSliceWithoutUpdate(position);
        } else {
            position = 0;
        }
        if ((n > 0) && (!IJ.isMacro()) && (imp != null))
        {
            Roi roi2 = (Roi)this.rois.get(n - 1);
            if (roi2 != null)
            {
                String label = (String)this.listModel.getElementAt(n - 1);
                int slice2 = getSliceNumber(roi2, label);
                if ((roi.equals(roi2)) && ((slice2 == -1) || (slice2 == imp.getCurrentSlice())) && (imp.getID() == this.prevID) && (!Interpreter.isBatchMode()))
                {
                    if (position > 0) {
                        imp.setSliceWithoutUpdate(saveCurrentSlice);
                    }
                    return false;
                }
            }
        }
        this.prevID = (imp != null ? imp.getID() : 0);
        String name = roi.getName();
        if (isStandardName(name)) {
            name = null;
        }
        String label = name != null ? name : getLabel(imp, roi, -1);
        if (promptForName) {
            label = promptForName(label);
        }
        if (label == null)
        {
            if (position > 0) {
                imp.setSliceWithoutUpdate(saveCurrentSlice);
            }
            return false;
        }
        this.listModel.addElement(label);
        roi.setName(label);
        Roi roiCopy = (Roi)roi.clone();
        setRoiPosition(imp, roiCopy);
        if (lineWidth > 1) {
            roiCopy.setStrokeWidth(lineWidth);
        }
        if (color != null) {
            roiCopy.setStrokeColor(color);
        }
        this.rois.add(roiCopy);
        updateShowAll();
        if (record()) {
            recordAdd(this.defaultColor, this.defaultLineWidth);
        }
        if (position > 0) {
            imp.setSliceWithoutUpdate(saveCurrentSlice);
        }
        return true;
    }

    void recordAdd(Color color, int lineWidth)
    {
        if (Recorder.scriptMode()) {
            Recorder.recordCall("rm.addRoi(imp.getRoi());");
        } else if ((color != null) && (lineWidth == 1)) {
            Recorder.recordString("roiManager(\"Add\", \"" + getHex(color) + "\");\n");
        } else if (lineWidth > 1) {
            Recorder.recordString("roiManager(\"Add\", \"" + getHex(color) + "\", " + lineWidth + ");\n");
        } else {
            Recorder.record("roiManager", "Add");
        }
    }

    String getHex(Color color)
    {
        if (color == null) {
            color = ImageCanvas.getShowAllColor();
        }
        String hex = Integer.toHexString(color.getRGB());
        if (hex.length() == 8) {
            hex = hex.substring(2);
        }
        return hex;
    }

    public void add(ImagePlus imp, Roi roi, int n)
    {
        if ((IJ.debugMode) && (n < 3) && (roi != null)) {
            IJ.log("RoiManager.add: " + n + " " + roi.getName());
        }
        if (roi == null) {
            return;
        }
        String label = roi.getName();
        String label2 = label;
        if (label == null) {
            label = getLabel(imp, roi, n);
        } else {
            label = label + "-" + n;
        }
        if (label == null) {
            return;
        }
        this.listModel.addElement(label);
        if (label2 != null) {
            roi.setName(label2);
        } else {
            roi.setName(label);
        }
        this.rois.add((Roi)roi.clone());
    }

    boolean isStandardName(String name)
    {
        if (name == null) {
            return false;
        }
        boolean isStandard = false;
        int len = name.length();
        if ((len >= 14) && (name.charAt(4) == '-') && (name.charAt(9) == '-')) {
            isStandard = true;
        } else if ((len >= 17) && (name.charAt(5) == '-') && (name.charAt(11) == '-')) {
            isStandard = true;
        } else if ((len >= 9) && (name.charAt(4) == '-')) {
            isStandard = true;
        } else if ((len >= 11) && (name.charAt(5) == '-')) {
            isStandard = true;
        }
        return isStandard;
    }

    String getLabel(ImagePlus imp, Roi roi, int n)
    {
        Rectangle r = roi.getBounds();
        int xc = r.x + r.width / 2;
        int yc = r.y + r.height / 2;
        if (n >= 0)
        {
            xc = yc;yc = n;
        }
        if (xc < 0) {
            xc = 0;
        }
        if (yc < 0) {
            yc = 0;
        }
        int digits = 4;
        String xs = "" + xc;
        if (xs.length() > digits) {
            digits = xs.length();
        }
        String ys = "" + yc;
        if (ys.length() > digits) {
            digits = ys.length();
        }
        if ((digits == 4) && (imp != null) && (imp.getStackSize() >= 10000)) {
            digits = 5;
        }
        xs = "000000" + xc;
        ys = "000000" + yc;
        String label = ys.substring(ys.length() - digits) + "-" + xs.substring(xs.length() - digits);
        if ((imp != null) && (imp.getStackSize() > 1))
        {
            int slice = imp.getCurrentSlice();
            String zs = "000000" + slice;
            label = zs.substring(zs.length() - digits) + "-" + label;
        }
        return label;
    }

    void addAndDraw(boolean altKeyDown)
    {
        if (altKeyDown)
        {
            if (addRoi(true)) {}
        }
        else if (!addRoi(false)) {
            return;
        }
        ImagePlus imp = WindowManager.getCurrentImage();
        if (imp != null)
        {
            Undo.setup(4, imp);
            IJ.run(imp, "Draw", "slice");
            Undo.setup(5, imp);
        }
        if (record()) {
            Recorder.record("roiManager", "Add & Draw");
        }
    }

    boolean delete(boolean replacing)
    {
        int count = getCount();
        if (count == 0) {
            return error("The list is empty.");
        }
        int[] index = getSelectedIndexes();
        if ((index.length == 0) || ((replacing) && (count > 1)))
        {
            String msg = "Delete all items on the list?";
            if (replacing) {
                msg = "Replace items on the list?";
            }
            this.canceled = false;
            if ((!IJ.isMacro()) && (!this.macro))
            {
                YesNoCancelDialog d = new YesNoCancelDialog(this, "ROI Manager", msg);
                if (d.cancelPressed())
                {
                    this.canceled = true;return false;
                }
                if (!d.yesPressed()) {
                    return false;
                }
            }
            index = getAllIndexes();
        }
        if ((count == index.length) && (!replacing))
        {
            this.rois.clear();
            this.listModel.removeAllElements();
        }
        else
        {
            for (int i = count - 1; i >= 0; i--)
            {
                boolean delete = false;
                for (int j = 0; j < index.length; j++) {
                    if (index[j] == i) {
                        delete = true;
                    }
                }
                if (delete)
                {
                    this.rois.remove(i);
                    this.listModel.remove(i);
                }
            }
        }
        ImagePlus imp = WindowManager.getCurrentImage();
        if ((count > 1) && (index.length == 1) && (imp != null)) {
            imp.deleteRoi();
        }
        updateShowAll();
        if (record()) {
            Recorder.record("roiManager", "Delete");
        }
        return true;
    }

    boolean update(boolean clone)
    {
        ImagePlus imp = getImage();
        if (imp == null) {
            return false;
        }
        ImageCanvas ic = imp.getCanvas();
        boolean showingAll = (ic != null) && (ic.getShowAllROIs());
        Roi roi = imp.getRoi();
        if (roi == null)
        {
            error("The active image does not have a selection.");
            return false;
        }
        int index = this.list.getSelectedIndex();
        if ((index < 0) && (!showingAll)) {
            return error("Exactly one item in the list must be selected.");
        }
        if (index >= 0) {
            if (clone)
            {
                String name = (String)this.listModel.getElementAt(index);
                Roi roi2 = (Roi)roi.clone();
                setRoiPosition(imp, roi2);
                roi.setName(name);
                roi2.setName(name);
                this.rois.set(index, roi2);
            }
            else
            {
                this.rois.set(index, roi);
            }
        }
        if (record()) {
            Recorder.record("roiManager", "Update");
        }
        updateShowAll();
        return true;
    }

    boolean rename(String name2)
    {
        int index = this.list.getSelectedIndex();
        if (index < 0) {
            return error("Exactly one item in the list must be selected.");
        }
        String name = (String)this.listModel.getElementAt(index);
        if (name2 == null) {
            name2 = promptForName(name);
        }
        if (name2 == null) {
            return false;
        }
        if (name2.equals(name)) {
            return false;
        }
        Roi roi = (Roi)this.rois.get(index);
        roi.setName(name2);
        int position = getSliceNumber(name2);
        if ((position > 0) && (roi.getCPosition() == 0) && (roi.getZPosition() == 0) && (roi.getTPosition() == 0)) {
            roi.setPosition(position);
        }
        this.rois.set(index, roi);
        this.listModel.setElementAt(name2, index);
        this.list.setSelectedIndex(index);
        if ((Prefs.useNamesAsLabels) && (this.labelsCheckbox.getState()))
        {
            ImagePlus imp = WindowManager.getCurrentImage();
            if (imp != null) {
                imp.draw();
            }
        }
        if (record()) {
            Recorder.record("roiManager", "Rename", name2);
        }
        return true;
    }

    String promptForName(String name)
    {
        GenericDialog gd = new GenericDialog("ROI Manager");
        gd.addStringField("Rename As:", name, 20);
        gd.showDialog();
        if (gd.wasCanceled()) {
            return null;
        }
        return gd.getNextString();
    }

    boolean restore(ImagePlus imp, int index, boolean setSlice)
    {
        Roi roi = (Roi)this.rois.get(index);
        if ((imp == null) || (roi == null)) {
            return false;
        }
        if (setSlice)
        {
            int c = roi.getCPosition();
            int z = roi.getZPosition();
            int t = roi.getTPosition();
            boolean hyperstack = imp.isHyperStack();
            if ((hyperstack) && ((c > 0) || (z > 0) || (t > 0)))
            {
                imp.setPosition(c, z, t);
            }
            else
            {
                String label = (String)this.listModel.getElementAt(index);
                int n = getSliceNumber(roi, label);
                if ((n >= 1) && (n <= imp.getStackSize())) {
                    if (hyperstack)
                    {
                        if ((imp.getNSlices() > 1) && (n <= imp.getNSlices())) {
                            imp.setPosition(imp.getC(), n, imp.getT());
                        } else if ((imp.getNFrames() > 1) && (n <= imp.getNFrames())) {
                            imp.setPosition(imp.getC(), imp.getZ(), n);
                        } else {
                            imp.setPosition(n);
                        }
                    }
                    else {
                        imp.setSlice(n);
                    }
                }
            }
        }
        if ((this.showAllCheckbox.getState()) && (!restoreCentered) && (!this.noUpdateMode))
        {
            roi.setImage(null);
            imp.setRoi(roi);
            return true;
        }
        Roi roi2 = (Roi)roi.clone();
        Rectangle r = roi2.getBounds();
        int width = imp.getWidth();int height = imp.getHeight();
        if (restoreCentered)
        {
            ImageCanvas ic = imp.getCanvas();
            if (ic != null)
            {
                Rectangle r1 = ic.getSrcRect();
                Rectangle r2 = roi2.getBounds();
                roi2.setLocation(r1.x + r1.width / 2 - r2.width / 2, r1.y + r1.height / 2 - r2.height / 2);
            }
        }
        if ((r.x >= width) || (r.y >= height) || (r.x + r.width < 0) || (r.y + r.height < 0)) {
            roi2.setLocation((width - r.width) / 2, (height - r.height) / 2);
        }
        if (this.noUpdateMode)
        {
            imp.setRoi(roi2, false);
            this.noUpdateMode = false;
        }
        else
        {
            imp.setRoi(roi2, true);
        }
        return true;
    }

    private boolean restoreWithoutUpdate(ImagePlus imp, int index)
    {
        this.noUpdateMode = true;
        if (imp == null) {
            imp = getImage();
        }
        return restore(imp, index, false);
    }

    public int getSliceNumber(String label)
    {
        int slice = -1;
        if ((label.length() >= 14) && (label.charAt(4) == '-') && (label.charAt(9) == '-')) {
            slice = (int)Tools.parseDouble(label.substring(0, 4), -1.0D);
        } else if ((label.length() >= 17) && (label.charAt(5) == '-') && (label.charAt(11) == '-')) {
            slice = (int)Tools.parseDouble(label.substring(0, 5), -1.0D);
        } else if ((label.length() >= 20) && (label.charAt(6) == '-') && (label.charAt(13) == '-')) {
            slice = (int)Tools.parseDouble(label.substring(0, 6), -1.0D);
        }
        return slice;
    }

    int getSliceNumber(Roi roi, String label)
    {
        int slice = roi != null ? roi.getPosition() : -1;
        if (slice == 0) {
            slice = -1;
        }
        if (slice == -1) {
            slice = getSliceNumber(label);
        }
        return slice;
    }

    void open(String path)
    {
        Macro.setOptions(null);
        String name = null;
        if ((path == null) || (path.equals("")))
        {
            OpenDialog od = new OpenDialog("Open Selection(s)...", "");
            String directory = od.getDirectory();
            name = od.getFileName();
            if (name == null) {
                return;
            }
            path = directory + name;
        }
        if ((Recorder.record) && (!Recorder.scriptMode())) {
            Recorder.record("roiManager", "Open", path);
        }
        if (path.endsWith(".zip"))
        {
            openZip(path);
            return;
        }
        Opener o = new Opener();
        if (name == null) {
            name = o.getName(path);
        }
        Roi roi = o.openRoi(path);
        if (roi != null)
        {
            if (name.endsWith(".roi")) {
                name = name.substring(0, name.length() - 4);
            }
            this.listModel.addElement(name);
            this.rois.add(roi);
        }
        updateShowAll();
    }

    void openZip(String path)
    {
        ZipInputStream in = null;
        ByteArrayOutputStream out = null;
        int nRois = 0;
        try
        {
            in = new ZipInputStream(new FileInputStream(path));
            byte[] buf = new byte[1024];

            ZipEntry entry = in.getNextEntry();
            while (entry != null)
            {
                String name = entry.getName();
                if (name.endsWith(".roi"))
                {
                    out = new ByteArrayOutputStream();
                    int len;
                    while ((len = in.read(buf)) > 0) {
                        out.write(buf, 0, len);
                    }
                    out.close();
                    byte[] bytes = out.toByteArray();
                    RoiDecoder rd = new RoiDecoder(bytes, name);
                    Roi roi = rd.getRoi();
                    if (roi != null)
                    {
                        name = name.substring(0, name.length() - 4);
                        this.listModel.addElement(name);
                        this.rois.add(roi);
                        nRois++;
                    }
                }
                entry = in.getNextEntry();
            }
            in.close();
            if (in != null) {
                try
                {
                    in.close();
                }
                catch (IOException e) {}
            }
            if (out != null) {
                try
                {
                    out.close();
                }
                catch (IOException e) {}
            }

        }
        catch (IOException e)
        {
            error(e.toString());
        }
        finally
        {
            if (in != null) {
                try
                {
                    in.close();
                }
                catch (IOException e) {}
            }
            if (out != null) {
                try
                {
                    out.close();
                }
                catch (IOException e) {}
            }
        }
        error("This ZIP archive does not appear to contain \".roi\" files");
        label290:
        updateShowAll();
    }

    boolean save()
    {
        if (getCount() == 0) {
            return error("The selection list is empty.");
        }
        int[] indexes = getIndexes();
        if (indexes.length > 1) {
            return saveMultiple(indexes, null);
        }
        String name = (String)this.listModel.getElementAt(indexes[0]);
        Macro.setOptions(null);
        SaveDialog sd = new SaveDialog("Save Selection...", name, ".roi");
        String name2 = sd.getFileName();
        if (name2 == null) {
            return false;
        }
        String dir = sd.getDirectory();
        Roi roi = (Roi)this.rois.get(indexes[0]);
        if (!name2.endsWith(".roi")) {
            name2 = name2 + ".roi";
        }
        String newName = name2.substring(0, name2.length() - 4);
        this.rois.set(indexes[0], roi);
        roi.setName(newName);
        this.listModel.setElementAt(newName, indexes[0]);
        RoiEncoder re = new RoiEncoder(dir + name2);
        try
        {
            re.write(roi);
        }
        catch (IOException e)
        {
            IJ.error("ROI Manager", e.getMessage());
        }
        if (record())
        {
            String path = dir + name2;
            if (Recorder.scriptMode()) {
                Recorder.recordCall("IJ.saveAs(imp, \"Selection\", \"" + path + "\");");
            } else {
                Recorder.record("saveAs", "Selection", path);
            }
        }
        return true;
    }

    boolean saveMultiple(int[] indexes, String path)
    {
        Macro.setOptions(null);
        if (path == null)
        {
            SaveDialog sd = new SaveDialog("Save ROIs...", "RoiSet", ".zip");
            String name = sd.getFileName();
            if (name == null) {
                return false;
            }
            if ((!name.endsWith(".zip")) && (!name.endsWith(".ZIP"))) {
                name = name + ".zip";
            }
            String dir = sd.getDirectory();
            path = dir + name;
        }
        DataOutputStream out = null;
        IJ.showStatus("Saving " + indexes.length + " ROIs " + " to " + path);
        long t0 = System.currentTimeMillis();
        try
        {
            ZipOutputStream zos = new ZipOutputStream(new BufferedOutputStream(new FileOutputStream(path)));
            out = new DataOutputStream(new BufferedOutputStream(zos));
            RoiEncoder re = new RoiEncoder(out);
            for (int i = 0; i < indexes.length; i++)
            {
                IJ.showProgress(i, indexes.length);
                String label = (String)this.listModel.getElementAt(indexes[i]);
                Roi roi = (Roi)this.rois.get(indexes[i]);
                if (IJ.debugMode) {
                    IJ.log("saveMultiple: " + i + "  " + label + "  " + roi);
                }
                if (roi != null)
                {
                    if (!label.endsWith(".roi")) {
                        label = label + ".roi";
                    }
                    zos.putNextEntry(new ZipEntry(label));
                    re.write(roi);
                    out.flush();
                }
            }
            out.close();
            if (out != null) {
                try
                {
                    out.close();
                }
                catch (IOException e) {}
            }
        }
        catch (IOException e)
        {
            error("" + e);
            return false;
        }
        finally
        {
            if (out != null) {
                try
                {
                    out.close();
                }
                catch (IOException e) {}
            }
        }
        double time = (System.currentTimeMillis() - t0) / 1000.0D;
        IJ.showProgress(1.0D);
        IJ.showStatus(IJ.d2s(time, 3) + " seconds, " + indexes.length + " ROIs, " + path);
        if ((Recorder.record) && (!IJ.isMacro())) {
            Recorder.record("roiManager", "Save", path);
        }
        return true;
    }

    private void listRois()
    {
        Roi[] list = getRoisAsArray();
        OverlayCommands.listRois(list);
        if (record()) {
            Recorder.record("roiManager", "List");
        }
    }

    boolean measure(int mode)
    {
        ImagePlus imp = getImage();
        if (imp == null) {
            return false;
        }
        int[] indexes = getIndexes();
        if (indexes.length == 0) {
            return false;
        }
        boolean allSliceOne = true;
        for (int i = 0; i < indexes.length; i++)
        {
            Roi roi = (Roi)this.rois.get(indexes[i]);
            String label = (String)this.listModel.getElementAt(indexes[i]);
            if (getSliceNumber(roi, label) > 1) {
                allSliceOne = false;
            }
        }
        int measurements = Analyzer.getMeasurements();
        if (imp.getStackSize() > 1) {
            Analyzer.setMeasurements(measurements | 0x100000);
        }
        int currentSlice = imp.getCurrentSlice();
        Analyzer.setMeasurements(measurements & 0xFFBFFFFF);
        for (int i = 0; i < indexes.length; i++)
        {
            if (!restore(getImage(), indexes[i], !allSliceOne)) {
                break;
            }
            IJ.run("Measure");
        }
        Analyzer.setMeasurements(measurements);
        imp.setSlice(currentSlice);
        if (indexes.length > 1) {
            IJ.run("Select None");
        }
        if (record()) {
            Recorder.record("roiManager", "Measure");
        }
        return true;
    }

    public ResultsTable multiMeasure(ImagePlus imp)
    {
        ResultsTable rt = multiMeasure(imp, getIndexes(), imp.getStackSize(), false);
        imp.deleteRoi();
        return rt;
    }

    boolean multiMeasure(String cmd)
    {
        ImagePlus imp = getImage();
        if (imp == null) {
            return false;
        }
        int[] indexes = getIndexes();
        if (indexes.length == 0) {
            return false;
        }
        int measurements = Analyzer.getMeasurements();

        int nSlices = imp.getStackSize();
        if (cmd != null) {
            this.appendResults = (cmd.contains("append"));
        }
        if (IJ.isMacro())
        {
            if (cmd.startsWith("multi-measure"))
            {
                measureAll = (cmd.contains(" measure")) && (nSlices > 1);
                onePerSlice = cmd.contains(" one");
                this.appendResults = cmd.contains(" append");
            }
            else
            {
                if (nSlices > 1) {
                    measureAll = true;
                }
                onePerSlice = true;
            }
        }
        else
        {
            GenericDialog gd = new GenericDialog("Multi Measure");
            if (nSlices > 1) {
                gd.addCheckbox("Measure all " + nSlices + " slices", measureAll);
            }
            gd.addCheckbox("One row per slice", onePerSlice);
            gd.addCheckbox("Append results", this.appendResults);
            int columns = getColumnCount(imp, measurements) * indexes.length;
            String str = nSlices == 1 ? "this option" : "both options";
            gd.setInsets(10, 25, 0);
            gd.addMessage("Enabling " + str + " will result\n" + "in a table with " + columns + " columns.");

            gd.showDialog();
            if (gd.wasCanceled()) {
                return false;
            }
            if (nSlices > 1) {
                measureAll = gd.getNextBoolean();
            }
            onePerSlice = gd.getNextBoolean();
            this.appendResults = gd.getNextBoolean();
        }
        if (!measureAll) {
            nSlices = 1;
        }
        int currentSlice = imp.getCurrentSlice();
        if (!onePerSlice)
        {
            int measurements2 = nSlices > 1 ? measurements | 0x100000 : measurements;
            ResultsTable rt = new ResultsTable();
            Analyzer analyzer = new Analyzer(imp, measurements2, rt);
            for (int slice = 1; slice <= nSlices; slice++)
            {
                if (nSlices > 1) {
                    imp.setSliceWithoutUpdate(slice);
                }
                for (int i = 0; i < indexes.length; i++)
                {
                    if (!restoreWithoutUpdate(imp, indexes[i])) {
                        break;
                    }
                    analyzer.measure();
                }
            }
            rt.show("Results");
            if (nSlices > 1) {
                imp.setSlice(currentSlice);
            }
        }
        else
        {
            ResultsTable rtMulti = multiMeasure(imp, indexes, nSlices, this.appendResults);
            this.mmResults = ((ResultsTable)rtMulti.clone());
            rtMulti.show("Results");
            imp.setSlice(currentSlice);
            if (indexes.length > 1) {
                IJ.run("Select None");
            }
        }
        if (record()) {
            if (Recorder.scriptMode())
            {
                Recorder.recordCall("rt = rm.multiMeasure(imp);");
                Recorder.recordCall("rt.show(\"Results\");");
            }
            else if (((nSlices == 1) || (measureAll)) && (onePerSlice) && (!this.appendResults))
            {
                Recorder.record("roiManager", "Multi Measure");
            }
            else
            {
                String options = "";
                if (measureAll) {
                    options = options + " measure_all";
                }
                if (onePerSlice) {
                    options = options + " one";
                }
                if (this.appendResults) {
                    options = options + " append";
                }
                Recorder.record("roiManager", "multi-measure" + options);
            }
        }
        return true;
    }

    private ResultsTable multiMeasure(ImagePlus imp, int[] indexes, int nSlices, boolean appendResults)
    {
        Analyzer aSys = new Analyzer(imp);
        ResultsTable rtSys = Analyzer.getResultsTable();
        ResultsTable rtMulti = new ResultsTable();
        if ((appendResults) && (this.mmResults != null)) {
            rtMulti = this.mmResults;
        }
        rtSys.reset();
        int currentSlice = imp.getCurrentSlice();
        for (int slice = 1; slice <= nSlices; slice++)
        {
            int sliceUse = slice;
            if (nSlices == 1) {
                sliceUse = currentSlice;
            }
            imp.setSliceWithoutUpdate(sliceUse);
            rtMulti.incrementCounter();
            if ((Analyzer.getMeasurements() & 0x400) != 0) {
                rtMulti.addLabel("Label", imp.getTitle());
            }
            int roiIndex = 0;
            for (int i = 0; i < indexes.length; i++)
            {
                if (!restoreWithoutUpdate(imp, indexes[i])) {
                    break;
                }
                roiIndex++;
                aSys.measure();
                for (int j = 0; j <= rtSys.getLastColumn(); j++)
                {
                    float[] col = rtSys.getColumn(j);
                    String head = rtSys.getColumnHeading(j);
                    String suffix = "" + roiIndex;
                    Roi roi = imp.getRoi();
                    if (roi != null)
                    {
                        String name = roi.getName();
                        if ((name != null) && (name.length() > 0) && ((name.length() < 9) || (!Character.isDigit(name.charAt(0))))) {
                            suffix = "(" + name + ")";
                        }
                    }
                    if ((head != null) && (col != null) && (!head.equals("Slice"))) {
                        rtMulti.addValue(head + suffix, rtSys.getValue(j, rtSys.getCounter() - 1));
                    }
                }
            }
        }
        return rtMulti;
    }

    int getColumnCount(ImagePlus imp, int measurements)
    {
        ImageStatistics stats = imp.getStatistics(measurements);
        ResultsTable rt = new ResultsTable();
        Analyzer analyzer = new Analyzer(imp, measurements, rt);
        analyzer.saveResults(stats, null);
        int count = 0;
        for (int i = 0; i <= rt.getLastColumn(); i++)
        {
            float[] col = rt.getColumn(i);
            String head = rt.getColumnHeading(i);
            if ((head != null) && (col != null)) {
                count++;
            }
        }
        return count;
    }

    void PlotMean()
    {
        ImagePlus imp = getImage();
        ResultsTable rt = multiMeasure(imp, getIndexes(), imp.getStackSize(), false);

        int size = rt.getCounter();
        int[] indexes = getIndexes();
        int n = indexes.length;

        double[][] x = new double[n][];
        double[][] y = new double[n][];

        for (int j = 0; j<n; j++) {

            y[j] = rt.getColumnAsDoubles(j * 4+1);

            double[] xx = new double[y[j].length];
            for (int i = 0; i < size; i++) {
                xx[i] = i;
            }
            x[j] = xx;
        }

        Color[] colors = { Color.blue, Color.green, Color.magenta, Color.red, Color.cyan, Color.yellow };
        if (n > colors.length)
        {
            colors = new Color[n];
            double c = 0.0D;
            double inc = 150.0D / n;
            for (int i = 0; i < n; i++)
            {
                colors[i] = new Color((int)c, (int)c, (int)c);
                c += inc;
            }
        }

        PlotWindow.noGridLines = false; // draw grid lines
        Plot plot = new Plot("Mean Plot","Frame","Value", x[0], y[0]);

        for (int i = 0; i < n; i++)
        {
            plot.setColor(colors[i]);
            if (x[i] != null) {
                plot.addPoints(x[i], y[i], 2);
            }

        }

        plot.setLineWidth(2);
        plot.show();
        plot.draw();
    }

    void multiPlot()
    {
        ImagePlus imp = getImage();
        if (imp == null) {
            return;
        }
        int[] indexes = getIndexes();
        int n = indexes.length;
        if (n == 0) {
            return;
        }
        Color[] colors = { Color.blue, Color.green, Color.magenta, Color.red, Color.cyan, Color.yellow };
        if (n > colors.length)
        {
            colors = new Color[n];
            double c = 0.0D;
            double inc = 150.0D / n;
            for (int i = 0; i < n; i++)
            {
                colors[i] = new Color((int)c, (int)c, (int)c);
                c += inc;
            }
        }
        int currentSlice = imp.getCurrentSlice();
        double[][] x = new double[n][];
        double[][] y = new double[n][];
        double minY = Double.MAX_VALUE;
        double maxY = -1.7976931348623157E308D;
        double fixedMin = ProfilePlot.getFixedMin();
        double fixedMax = ProfilePlot.getFixedMax();
        boolean freeYScale = (fixedMin == 0.0D) && (fixedMax == 0.0D);
        if (!freeYScale)
        {
            minY = fixedMin;
            maxY = fixedMax;
        }
        int maxX = 0;
        Calibration cal = imp.getCalibration();
        double xinc = cal.pixelWidth;
        for (int i = 0; i < indexes.length; i++)
        {
            if (!restore(getImage(), indexes[i], true)) {
                break;
            }
            Roi roi = imp.getRoi();
            if (roi == null) {
                break;
            }
            if ((roi.isArea()) && (roi.getType() != 0)) {
                IJ.run(imp, "Area to Line", "");
            }
            ProfilePlot pp = new ProfilePlot(imp, (Prefs.verticalProfile) || (IJ.altKeyDown()));
            y[i] = pp.getProfile();
            if (y[i] == null) {
                break;
            }
            if (y[i].length > maxX) {
                maxX = y[i].length;
            }
            if (freeYScale)
            {
                double[] a = Tools.getMinMax(y[i]);
                if (a[0] < minY) {
                    minY = a[0];
                }
                if (a[1] > maxY) {
                    maxY = a[1];
                }
            }
            double[] xx = new double[y[i].length];
            for (int j = 0; j < xx.length; j++) {
                xx[j] = (j * xinc);
            }
            x[i] = xx;
        }
        String xlabel = "Distance (" + cal.getUnits() + ")";
        Plot plot = new Plot("Profiles", xlabel, "Value", x[0], y[0]);
        plot.setLimits(0.0D, maxX * xinc, minY, maxY);
        for (int i = 1; i < indexes.length; i++)
        {
            plot.setColor(colors[i]);
            if (x[i] != null) {
                plot.addPoints(x[i], y[i], 2);
            }
        }
        plot.setColor(colors[0]);
        if (x[0] != null) {
            plot.show();
        }
        imp.setSlice(currentSlice);
        if (indexes.length > 1) {
            IJ.run("Select None");
        }
        if (record()) {
            Recorder.record("roiManager", "Multi Plot");
        }
    }

    boolean drawOrFill(int mode)
    {
        int[] indexes = getIndexes();
        ImagePlus imp = WindowManager.getCurrentImage();
        imp.deleteRoi();
        ImageProcessor ip = imp.getProcessor();
        ip.setColor(Toolbar.getForegroundColor());
        ip.snapshot();
        Undo.setup(1, imp);
        Filler filler = mode == 2 ? new Filler() : null;
        int slice = imp.getCurrentSlice();
        for (int i = 0; i < indexes.length; i++)
        {
            Roi roi = (Roi)this.rois.get(indexes[i]);
            int type = roi.getType();
            if (roi != null)
            {
                if ((mode == 1) && ((type == 6) || (type == 7) || (type == 8))) {
                    mode = 0;
                }
                String name = (String)this.listModel.getElementAt(indexes[i]);
                int slice2 = getSliceNumber(roi, name);
                if ((slice2 >= 1) && (slice2 <= imp.getStackSize()))
                {
                    imp.setSlice(slice2);
                    ip = imp.getProcessor();
                    ip.setColor(Toolbar.getForegroundColor());
                    if (slice2 != slice) {
                        Undo.reset();
                    }
                }
                switch (mode)
                {
                    case 0:
                        roi.drawPixels(ip); break;
                    case 1:
                        ip.fill(roi); break;
                    case 2:
                        roi.drawPixels(ip);
                        filler.drawLabel(imp, ip, i + 1, roi.getBounds());
                }
            }
        }
        if ((record()) && ((mode == 0) || (mode == 1))) {
            Recorder.record("roiManager", mode == 0 ? "Draw" : "Fill");
        }
        if (this.showAllCheckbox.getState()) {
            runCommand("show none");
        }
        imp.updateAndDraw();
        return true;
    }

    void setProperties(Color color, int lineWidth, Color fillColor)
    {
        boolean showDialog = (color == null) && (lineWidth == -1) && (fillColor == null);
        int[] indexes = getIndexes();
        int n = indexes.length;
        if (n == 0) {
            return;
        }
        Roi rpRoi = null;
        String rpName = null;
        Font font = null;
        int justification = 0;
        double opacity = -1.0D;
        int position = -1;
        int cpos = -1;int zpos = -1;int tpos = -1;
        int pointType = -1;
        int pointSize = -1;
        if (showDialog)
        {
            rpRoi = (Roi)this.rois.get(indexes[0]);
            if (n == 1)
            {
                fillColor = rpRoi.getFillColor();
                rpName = rpRoi.getName();
            }
            if (rpRoi.getStrokeColor() == null) {
                rpRoi.setStrokeColor(Roi.getColor());
            }
            rpRoi = (Roi)rpRoi.clone();
            if (n > 1) {
                rpRoi.setName("range: " + (indexes[0] + 1) + "-" + (indexes[(n - 1)] + 1));
            }
            rpRoi.setFillColor(fillColor);
            RoiProperties rp = new RoiProperties("Properties", rpRoi);
            if (!rp.showDialog()) {
                return;
            }
            lineWidth = (int)rpRoi.getStrokeWidth();
            this.defaultLineWidth = lineWidth;
            color = rpRoi.getStrokeColor();
            fillColor = rpRoi.getFillColor();
            this.defaultColor = color;
            position = rpRoi.getPosition();
            cpos = rpRoi.getCPosition();
            zpos = rpRoi.getZPosition();
            tpos = rpRoi.getTPosition();
            if ((rpRoi instanceof TextRoi))
            {
                font = ((TextRoi)rpRoi).getCurrentFont();
                justification = ((TextRoi)rpRoi).getJustification();
            }
            if ((rpRoi instanceof ImageRoi)) {
                opacity = ((ImageRoi)rpRoi).getOpacity();
            }
            if ((rpRoi instanceof PointRoi))
            {
                pointType = ((PointRoi)rpRoi).getPointType();
                pointSize = ((PointRoi)rpRoi).getSize();
            }
        }
        ImagePlus imp = WindowManager.getCurrentImage();
        if ((n == getCount()) && (n > 1) && (!IJ.isMacro()))
        {
            GenericDialog gd = new GenericDialog("ROI Manager");
            gd.addMessage("Apply changes to all " + n + " selections?");
            gd.showDialog();
            if (gd.wasCanceled()) {
                return;
            }
        }
        for (int i = 0; i < n; i++)
        {
            Roi roi = (Roi)this.rois.get(indexes[i]);
            if (roi != null)
            {
                if (color != null) {
                    roi.setStrokeColor(color);
                }
                if (lineWidth >= 0) {
                    roi.setStrokeWidth(lineWidth);
                }
                roi.setFillColor(fillColor);
                if ((cpos > 0) || (zpos > 0) || (tpos > 0)) {
                    roi.setPosition(cpos, zpos, tpos);
                } else if (position != -1) {
                    roi.setPosition(position);
                }
                if ((roi instanceof TextRoi))
                {
                    roi.setImage(imp);
                    if (font != null) {
                        ((TextRoi)roi).setCurrentFont(font);
                    }
                    ((TextRoi)roi).setJustification(justification);
                    roi.setImage(null);
                }
                if (((roi instanceof ImageRoi)) && (opacity != -1.0D)) {
                    ((ImageRoi)roi).setOpacity(opacity);
                }
                if ((roi instanceof PointRoi))
                {
                    if (pointType != -1) {
                        ((PointRoi)roi).setPointType(pointType);
                    }
                    if (pointSize != -1) {
                        ((PointRoi)roi).setSize(pointSize);
                    }
                }
            }
        }
        if ((rpRoi != null) && (rpName != null) && (!rpRoi.getName().equals(rpName))) {
            rename(rpRoi.getName());
        }
        ImageCanvas ic = imp != null ? imp.getCanvas() : null;
        Roi roi = imp != null ? imp.getRoi() : null;
        boolean showingAll = (ic != null) && (ic.getShowAllROIs());
        if ((roi != null) && ((n == 1) || (!showingAll)))
        {
            if (lineWidth >= 0) {
                roi.setStrokeWidth(lineWidth);
            }
            if (color != null) {
                roi.setStrokeColor(color);
            }
            if (fillColor != null) {
                roi.setFillColor(fillColor);
            }
            if ((roi != null) && ((roi instanceof TextRoi)))
            {
                ((TextRoi)roi).setCurrentFont(font);
                ((TextRoi)roi).setJustification(justification);
            }
            if ((roi != null) && ((roi instanceof ImageRoi)) && (opacity != -1.0D)) {
                ((ImageRoi)roi).setOpacity(opacity);
            }
        }
        if ((lineWidth > 1) && (!showingAll) && (roi == null))
        {
            showAll(0);
            showingAll = true;
        }
        if (imp != null) {
            imp.draw();
        }
        if (record()) {
            if (fillColor != null)
            {
                Recorder.record("roiManager", "Set Fill Color", Colors.colorToString(fillColor));
            }
            else
            {
                Recorder.record("roiManager", "Set Color", Colors.colorToString(color != null ? color : Color.red));
                Recorder.record("roiManager", "Set Line Width", lineWidth);
            }
        }
    }

    void flatten()
    {
        ImagePlus imp = WindowManager.getCurrentImage();
        if (imp == null)
        {
            IJ.noImage();return;
        }
        ImageCanvas ic = imp.getCanvas();
        if ((ic != null) && (ic.getShowAllList() == null) && (imp.getOverlay() == null) && (imp.getRoi() == null)) {
            error("Image does not have an overlay or ROI");
        } else {
            IJ.doCommand("Flatten");
        }
    }

    public boolean getDrawLabels()
    {
        return this.labelsCheckbox.getState();
    }

    void combine()
    {
        ImagePlus imp = getImage();
        if (imp == null) {
            return;
        }
        int[] indexes = getSelectedIndexes();
        if (indexes.length == 1)
        {
            error("More than one item must be selected, or none");
            return;
        }
        if (indexes.length == 0) {
            indexes = getAllIndexes();
        }
        int nPointRois = 0;
        for (int i = 0; i < indexes.length; i++)
        {
            Roi roi = (Roi)this.rois.get(indexes[i]);
            if (roi.getType() != 10) {
                break;
            }
            nPointRois++;
        }
        if (nPointRois == indexes.length) {
            combinePoints(imp, indexes);
        } else {
            combineRois(imp, indexes);
        }
        if (record()) {
            Recorder.record("roiManager", "Combine");
        }
    }

    void combineRois(ImagePlus imp, int[] indexes)
    {
        ShapeRoi s1 = null;ShapeRoi s2 = null;
        ImageProcessor ip = null;
        for (int i = 0; i < indexes.length; i++)
        {
            Roi roi = (Roi)this.rois.get(indexes[i]);
            if (!roi.isArea())
            {
                if (ip == null) {
                    ip = new ByteProcessor(imp.getWidth(), imp.getHeight());
                }
                roi = convertLineToPolygon(roi, ip);
            }
            if (s1 == null)
            {
                if ((roi instanceof ShapeRoi)) {
                    s1 = (ShapeRoi)roi;
                } else {
                    s1 = new ShapeRoi(roi);
                }
                if (s1 != null) {}
            }
            else
            {
                if ((roi instanceof ShapeRoi)) {
                    s2 = (ShapeRoi)roi;
                } else {
                    s2 = new ShapeRoi(roi);
                }
                if (s2 != null) {
                    s1.or(s2);
                }
            }
        }
        if (s1 != null) {
            imp.setRoi(s1);
        }
    }

    Roi convertLineToPolygon(Roi roi, ImageProcessor ip)
    {
        if (roi == null) {
            return null;
        }
        ip.resetRoi();
        ip.setColor(0);
        ip.fill();
        ip.setColor(255);
        if ((roi.getType() == 5) && (roi.getStrokeWidth() > 1.0F)) {
            ip.fillPolygon(roi.getPolygon());
        } else {
            roi.drawPixels(ip);
        }
        ip.setThreshold(255.0D, 255.0D, 2);
        ThresholdToSelection tts = new ThresholdToSelection();
        return tts.convert(ip);
    }

    void combinePoints(ImagePlus imp, int[] indexes)
    {
        int n = indexes.length;
        Polygon[] p = new Polygon[n];
        int points = 0;
        for (int i = 0; i < n; i++)
        {
            Roi roi = (Roi)this.rois.get(indexes[i]);
            p[i] = roi.getPolygon();
            points += p[i].npoints;
        }
        if (points == 0) {
            return;
        }
        int[] xpoints = new int[points];
        int[] ypoints = new int[points];
        int index = 0;
        for (int i = 0; i < p.length; i++) {
            for (int j = 0; j < p[i].npoints; j++)
            {
                xpoints[index] = p[i].xpoints[j];
                ypoints[index] = p[i].ypoints[j];
                index++;
            }
        }
        imp.setRoi(new PointRoi(xpoints, ypoints, xpoints.length));
    }

    void and()
    {
        ImagePlus imp = getImage();
        if (imp == null) {
            return;
        }
        int[] indexes = getSelectedIndexes();
        if (indexes.length == 1)
        {
            error("More than one item must be selected, or none");
            return;
        }
        if (indexes.length == 0) {
            indexes = getAllIndexes();
        }
        ShapeRoi s1 = null;ShapeRoi s2 = null;
        for (int i = 0; i < indexes.length; i++)
        {
            Roi roi = (Roi)this.rois.get(indexes[i]);
            if ((roi != null) && (roi.isArea())) {
                if (s1 == null)
                {
                    if ((roi instanceof ShapeRoi)) {
                        s1 = (ShapeRoi)roi.clone();
                    } else {
                        s1 = new ShapeRoi(roi);
                    }
                    if (s1 != null) {}
                }
                else
                {
                    if ((roi instanceof ShapeRoi)) {
                        s2 = (ShapeRoi)roi.clone();
                    } else {
                        s2 = new ShapeRoi(roi);
                    }
                    if (s2 != null) {
                        s1.and(s2);
                    }
                }
            }
        }
        if (s1 != null) {
            imp.setRoi(s1);
        }
        if (record()) {
            Recorder.record("roiManager", "AND");
        }
    }

    void xor()
    {
        ImagePlus imp = getImage();
        if (imp == null) {
            return;
        }
        int[] indexes = getSelectedIndexes();
        if (indexes.length == 1)
        {
            error("More than one item must be selected, or none");
            return;
        }
        if (indexes.length == 0) {
            indexes = getAllIndexes();
        }
        ShapeRoi s1 = null;ShapeRoi s2 = null;
        for (int i = 0; i < indexes.length; i++)
        {
            Roi roi = (Roi)this.rois.get(indexes[i]);
            if (roi.isArea()) {
                if (s1 == null)
                {
                    if ((roi instanceof ShapeRoi)) {
                        s1 = (ShapeRoi)roi.clone();
                    } else {
                        s1 = new ShapeRoi(roi);
                    }
                    if (s1 != null) {}
                }
                else
                {
                    if ((roi instanceof ShapeRoi)) {
                        s2 = (ShapeRoi)roi.clone();
                    } else {
                        s2 = new ShapeRoi(roi);
                    }
                    if (s2 != null) {
                        s1.xor(s2);
                    }
                }
            }
        }
        if (s1 != null) {
            imp.setRoi(s1);
        }
        if (record()) {
            Recorder.record("roiManager", "XOR");
        }
    }

    void addParticles()
    {
        String err = IJ.runMacroFile("ij.jar:AddParticles", null);
        if ((err != null) && (err.length() > 0)) {
            error(err);
        }
    }

    void sort()
    {
        int n = this.listModel.size();
        if (n == 0) {
            return;
        }
        String[] labels = new String[n];
        for (int i = 0; i < n; i++) {
            labels[i] = ((String)this.listModel.get(i));
        }
        int[] indices = Tools.rank(labels);
        Roi[] rois2 = getRoisAsArray();
        this.listModel.removeAllElements();
        this.rois.clear();
        for (int i = 0; i < labels.length; i++)
        {
            this.listModel.addElement(labels[indices[i]]);
            this.rois.add(rois2[indices[i]]);
        }
        if (record()) {
            Recorder.record("roiManager", "Sort");
        }
    }

    void specify()
    {
        try
        {
            IJ.run("Specify...");
        }
        catch (Exception e)
        {
            return;
        }
        runCommand("add");
    }

    private static boolean channel = false;
    private static boolean slice = true;
    private static boolean frame = false;

    private void removePositions(int position)
    {
        int[] indexes = getIndexes();
        if (indexes.length == 0) {
            return;
        }
        boolean removeChannels = position == 0;
        boolean removeFrames = position == 2;
        boolean removeSlices = (!removeChannels) && (!removeFrames);
        if (position == 3)
        {
            ImagePlus imp = WindowManager.getCurrentImage();
            if ((imp != null) && (!imp.isHyperStack()))
            {
                channel = false;slice = true;frame = false;
            }
            Font font = new Font("SansSerif", 1, 12);
            GenericDialog gd = new GenericDialog("Remove");
            gd.setInsets(5, 15, 0);
            gd.addMessage("Remove positions for:      ", font);
            gd.setInsets(6, 25, 0);
            gd.addCheckbox("Channels:", channel);
            gd.setInsets(0, 25, 0);
            gd.addCheckbox("Slices:", slice);
            gd.setInsets(0, 25, 0);
            gd.addCheckbox("Frames:", frame);
            gd.showDialog();
            if (gd.wasCanceled()) {
                return;
            }
            removeChannels = gd.getNextBoolean();
            removeSlices = gd.getNextBoolean();
            removeFrames = gd.getNextBoolean();
            channel = removeChannels;
            slice = removeSlices;
            frame = removeFrames;
        }
        if ((!removeChannels) && (!removeSlices) && (!removeFrames))
        {
            slice = true;
            return;
        }
        for (int i = 0; i < indexes.length; i++)
        {
            int index = indexes[i];
            Roi roi = (Roi)this.rois.get(index);
            int c = roi.getCPosition();
            int z = roi.getZPosition();
            int t = roi.getTPosition();
            if ((c > 0) || (t > 0))
            {
                if (removeChannels) {
                    c = 0;
                }
                if (removeSlices) {
                    z = 0;
                }
                if (removeFrames) {
                    t = 0;
                }
                roi.setPosition(c, z, t);
            }
            else
            {
                String name = (String)this.listModel.getElementAt(index);
                int n = getSliceNumber(name);
                if (n == -1)
                {
                    roi.setPosition(0);
                }
                else
                {
                    String name2 = name.substring(5, name.length());
                    roi.setName(name2);
                    this.rois.set(index, roi);
                    this.listModel.setElementAt(name2, index);
                }
            }
        }
        if (record())
        {
            if (removeChannels) {
                Recorder.record("roiManager", "Remove Channel Info");
            }
            if (removeSlices) {
                Recorder.record("roiManager", "Remove Slice Info");
            }
            if (removeFrames) {
                Recorder.record("roiManager", "Remove Frame Info");
            }
        }
    }

    private void help()
    {
        String macro = "run('URL...', 'url=http://imagej.nih.gov/ij/docs/menus/analyze.html#manager');";
        new MacroRunner(macro);
    }

    private void labels()
    {
        ImagePlus imp = WindowManager.getCurrentImage();
        if (imp != null)
        {
            this.showAllCheckbox.setState(true);
            this.labelsCheckbox.setState(true);
            showAll(2);
        }
        try
        {
            IJ.run("Labels...");
        }
        catch (Exception e) {}
        Overlay defaultOverlay = OverlayLabels.createOverlay();
        Prefs.useNamesAsLabels = defaultOverlay.getDrawNames();
    }

    private void options()
    {
        Color c = ImageCanvas.getShowAllColor();
        GenericDialog gd = new GenericDialog("Options");

        gd.addCheckbox("Associate \"Show All\" ROIs with slices", Prefs.showAllSliceOnly);
        gd.addCheckbox("Restore ROIs centered", restoreCentered);
        gd.addCheckbox("Use ROI names as labels", Prefs.useNamesAsLabels);
        gd.showDialog();
        if (gd.wasCanceled())
        {
            if (c != ImageCanvas.getShowAllColor()) {
                ImageCanvas.setShowAllColor(c);
            }
            return;
        }
        Prefs.showAllSliceOnly = gd.getNextBoolean();
        restoreCentered = gd.getNextBoolean();
        Prefs.useNamesAsLabels = gd.getNextBoolean();
        ImagePlus imp = WindowManager.getCurrentImage();
        if (imp != null)
        {
            Overlay overlay = imp.getOverlay();
            if (overlay == null)
            {
                ImageCanvas ic = imp.getCanvas();
                if (ic != null) {
                    overlay = ic.getShowAllList();
                }
            }
            if (overlay != null)
            {
                overlay.drawNames(Prefs.useNamesAsLabels);
                setOverlay(imp, overlay);
            }
            else
            {
                imp.draw();
            }
        }
        if (record())
        {
            Recorder.record("roiManager", "Associate", Prefs.showAllSliceOnly ? "true" : "false");
            Recorder.record("roiManager", "Centered", restoreCentered ? "true" : "false");
            Recorder.record("roiManager", "UseNames", Prefs.useNamesAsLabels ? "true" : "false");
        }
    }

    Panel makeButtonPanel(GenericDialog gd)
    {
        Panel panel = new Panel();

        this.colorButton = new Button("\"Show All\" Color...");
        this.colorButton.addActionListener(this);
        panel.add(this.colorButton);
        return panel;
    }

    void setShowAllColor()
    {
        ColorChooser cc = new ColorChooser("\"Show All\" Color", ImageCanvas.getShowAllColor(), false);
        ImageCanvas.setShowAllColor(cc.getColor());
    }

    void split()
    {
        ImagePlus imp = getImage();
        if (imp == null) {
            return;
        }
        Roi roi = imp.getRoi();
        if ((roi == null) || (roi.getType() != 9))
        {
            error("Image with composite selection required");
            return;
        }
        boolean record = Recorder.record;
        Recorder.record = false;
        Roi[] rois = ((ShapeRoi)roi).getRois();
        for (int i = 0; i < rois.length; i++)
        {
            imp.setRoi(rois[i]);
            addRoi(false);
        }
        Recorder.record = record;
        if (record()) {
            Recorder.record("roiManager", "Split");
        }
    }

    void showAll(int mode)
    {
        ImagePlus imp = WindowManager.getCurrentImage();
        if (imp == null) {
            return;
        }
        boolean showAll = mode == 0;
        if (showAll) {
            this.imageID = imp.getID();
        }
        if ((mode == 2) || (mode == 3)) {
            showAll = true;
        }
        if (showAll) {
            imp.deleteRoi();
        }
        if (mode == 1)
        {
            removeOverlay(imp);
            this.imageID = 0;
        }
        else if (getCount() > 0)
        {
            Roi[] rois = getRoisAsArray();
            Overlay overlay = newOverlay();
            for (int i = 0; i < rois.length; i++) {
                overlay.add(rois[i]);
            }
            setOverlay(imp, overlay);
        }
    }

    void updateShowAll()
    {
        ImagePlus imp = WindowManager.getCurrentImage();
        if (imp == null) {
            return;
        }
        if (this.showAllCheckbox.getState())
        {
            if (getCount() > 0)
            {
                Roi[] rois = getRoisAsArray();
                Overlay overlay = newOverlay();
                for (int i = 0; i < rois.length; i++) {
                    overlay.add(rois[i]);
                }
                setOverlay(imp, overlay);
            }
            else
            {
                removeOverlay(imp);
            }
        }
        else {
            removeOverlay(imp);
        }
    }

    int[] getAllIndexes()
    {
        int count = getCount();
        int[] indexes = new int[count];
        for (int i = 0; i < count; i++) {
            indexes[i] = i;
        }
        return indexes;
    }

    ImagePlus getImage()
    {
        ImagePlus imp = WindowManager.getCurrentImage();
        if (imp == null)
        {
            error("There are no images open.");
            return null;
        }
        return imp;
    }

    boolean error(String msg)
    {
        new MessageDialog(this, "ROI Manager", msg);
        Macro.abort();
        return false;
    }

    public void processWindowEvent(WindowEvent e)
    {
        super.processWindowEvent(e);
        if (e.getID() == 201) {
            instance = null;
        }
        if (!IJ.isMacro()) {
            this.ignoreInterrupts = false;
        }
    }

    public static MyRoiManager getInstance()
    {
        return (MyRoiManager)instance;
    }

    public static MyRoiManager getInstance2()
    {
        MyRoiManager rm = getInstance();
//        if ((rm == null) && (IJ.isMacro())) {
//            rm = Interpreter.getBatchModeRoiManager();
//        }
        return rm;
    }

    /**
     * @deprecated
     */
    public Hashtable getROIs()
    {
        return null;
    }

    /**
     * @deprecated
     */
    public List getList()
    {
        List awtList = new List();
        for (int i = 0; i < getCount(); i++) {
            awtList.add((String)this.listModel.getElementAt(i));
        }
        int index = getSelectedIndex();
        if (index >= 0) {
            awtList.select(index);
        }
        return awtList;
    }

    public int getCount()
    {
        return this.listModel.getSize();
    }

    public int getRoiIndex(Roi roi)
    {
        int n = getCount();
        for (int i = 0; i < n; i++)
        {
            Roi roi2 = (Roi)this.rois.get(i);
            if (roi == roi2) {
                return i;
            }
        }
        return -1;
    }

    public int getSelectedIndex()
    {
        return this.list.getSelectedIndex();
    }

    public Roi getRoi(int index)
    {
        if ((index < 0) || (index >= getCount())) {
            return null;
        }
        return (Roi)this.rois.get(index);
    }

    public synchronized Roi[] getRoisAsArray()
    {
        Roi[] array = new Roi[this.rois.size()];
        return (Roi[])this.rois.toArray(array);
    }

    public Roi[] getSelectedRoisAsArray()
    {
        int[] indexes = getIndexes();
        int n = indexes.length;
        Roi[] array = new Roi[n];
        for (int i = 0; i < n; i++) {
            array[i] = ((Roi)this.rois.get(indexes[i]));
        }
        return array;
    }

    public String getName(int index)
    {
        if ((index >= 0) && (index < getCount())) {
            return (String)this.listModel.getElementAt(index);
        }
        return null;
    }

    public static String getName(String index)
    {
        int i = (int)Tools.parseDouble(index, -1.0D);
        MyRoiManager instance = getInstance2();
        if ((instance != null) && (i >= 0) && (i < instance.getCount())) {
            return (String)instance.listModel.getElementAt(i);
        }
        return "null";
    }

    public boolean runCommand(String cmd)
    {
        cmd = cmd.toLowerCase();
        this.macro = true;
        boolean ok = true;
        if (cmd.equals("add"))
        {
            boolean shift = IJ.shiftKeyDown();
            boolean alt = IJ.altKeyDown();
            if (Interpreter.isBatchMode())
            {
                shift = false;
                alt = false;
            }
            add(shift, alt);
            if ((IJ.isJava18()) && (IJ.isMacOSX())) {
                repaint();
            }
        }
        else if (cmd.equals("add & draw"))
        {
            addAndDraw(false);
        }
        else if (cmd.equals("update"))
        {
            update(true);
        }
        else if (cmd.equals("update2"))
        {
            update(false);
        }
        else if (cmd.equals("delete"))
        {
            delete(false);
        }
        else if (cmd.equals("measure"))
        {
            measure(1);
        }
        else if (cmd.equals("draw"))
        {
            drawOrFill(0);
        }
        else if (cmd.equals("fill"))
        {
            drawOrFill(1);
        }
        else if (cmd.equals("label"))
        {
            drawOrFill(2);
        }
        else if (cmd.equals("and"))
        {
            and();
        }
        else if ((cmd.equals("or")) || (cmd.equals("combine")))
        {
            combine();
        }
        else if (cmd.equals("xor"))
        {
            xor();
        }
        else if (cmd.equals("split"))
        {
            split();
        }
        else if (cmd.equals("sort"))
        {
            sort();
        }
        else if ((cmd.startsWith("multi measure")) || (cmd.startsWith("multi-measure")))
        {
            multiMeasure(cmd);
        }
        else if (cmd.equals("multi plot"))
        {
            multiPlot();
        }
        else if (cmd.equals("show all"))
        {
            if (WindowManager.getCurrentImage() != null)
            {
                showAll(0);
                this.showAllCheckbox.setState(true);
            }
        }
        else if (cmd.equals("show none"))
        {
            if (WindowManager.getCurrentImage() != null)
            {
                showAll(1);
                this.showAllCheckbox.setState(false);
            }
        }
        else if (cmd.equals("show all with labels"))
        {
            this.labelsCheckbox.setState(true);
            showAll(2);
            this.showAllCheckbox.setState(true);
            if (Interpreter.isBatchMode()) {
                IJ.wait(250);
            }
        }
        else if (cmd.equals("show all without labels"))
        {
            this.showAllCheckbox.setState(true);
            this.labelsCheckbox.setState(false);
            showAll(3);
            if (Interpreter.isBatchMode()) {
                IJ.wait(250);
            }
        }
        else if ((cmd.equals("deselect")) || (cmd.indexOf("all") != -1))
        {
            if (IJ.isMacOSX()) {
                this.ignoreInterrupts = true;
            }
            deselect();
            IJ.wait(50);
        }
        else if (cmd.equals("reset"))
        {
            reset();
        }
        else if (!cmd.equals("debug"))
        {
            if (cmd.equals("enable interrupts")) {
                this.ignoreInterrupts = false;
            } else if (cmd.equals("remove channel info")) {
                removePositions(0);
            } else if (cmd.equals("remove slice info")) {
                removePositions(1);
            } else if (cmd.equals("remove frame info")) {
                removePositions(2);
            } else if (cmd.equals("list")) {
                listRois();
            } else if (cmd.equals("interpolate rois")) {
                interpolateRois();
            } else {
                ok = false;
            }
        }
        this.macro = false;
        return ok;
    }

    public boolean runCommand(ImagePlus imp, String cmd)
    {
        WindowManager.setTempCurrentImage(imp);
        boolean ok = runCommand(cmd);
        WindowManager.setTempCurrentImage(null);
        return ok;
    }

    public boolean runCommand(String cmd, String name)
    {
        cmd = cmd.toLowerCase();
        this.macro = true;
        if (cmd.equals("open"))
        {
            open(name);
            this.macro = false;
            return true;
        }
        if (cmd.equals("save"))
        {
            save(name, false);
        }
        else if (cmd.equals("save selected"))
        {
            save(name, true);
        }
        else
        {
            if (cmd.equals("rename"))
            {
                rename(name);
                this.macro = false;
                return true;
            }
            if (cmd.equals("set color"))
            {
                Color color = Colors.decode(name, Color.cyan);
                setProperties(color, -1, null);
                this.macro = false;
                return true;
            }
            if (cmd.equals("set fill color"))
            {
                Color fillColor = Colors.decode(name, Color.cyan);
                setProperties(null, -1, fillColor);
                this.macro = false;
                return true;
            }
            if (cmd.equals("set line width"))
            {
                int lineWidth = (int)Tools.parseDouble(name, 0.0D);
                if (lineWidth >= 0) {
                    setProperties(null, lineWidth, null);
                }
                this.macro = false;
                return true;
            }
            if (cmd.equals("associate"))
            {
                Prefs.showAllSliceOnly = name.equals("true");
                this.macro = false;
                return true;
            }
            if (cmd.equals("centered"))
            {
                restoreCentered = name.equals("true");
                this.macro = false;
                return true;
            }
            if (cmd.equals("usenames"))
            {
                Prefs.useNamesAsLabels = name.equals("true");
                this.macro = false;
                if (this.labelsCheckbox.getState())
                {
                    ImagePlus imp = WindowManager.getCurrentImage();
                    if (imp != null) {
                        imp.draw();
                    }
                }
                return true;
            }
        }
        return false;
    }

    public void reset()
    {
        if ((IJ.isMacOSX()) && (IJ.isMacro())) {
            this.ignoreInterrupts = true;
        }
        this.listModel.removeAllElements();
        this.rois.clear();
        updateShowAll();
    }

    private void translate()
    {
        double dx = 10.0D;
        double dy = 10.0D;
        GenericDialog gd = new GenericDialog("Translate");
        gd.addNumericField("X offset (pixels): ", dx, 0);
        gd.addNumericField("Y offset (pixels): ", dy, 0);
        gd.showDialog();
        if (gd.wasCanceled()) {
            return;
        }
        dx = gd.getNextNumber();
        dy = gd.getNextNumber();
        translate(dx, dy);
        if (record()) {
            if (Recorder.scriptMode()) {
                Recorder.recordCall("rm.translate(" + dx + ", " + dy + ");");
            } else {
                Recorder.record("roiManager", "translate", (int)dx, (int)dy);
            }
        }
    }

    public void translate(double dx, double dy)
    {
        Roi[] rois = getSelectedRoisAsArray();
        for (int i = 0; i < rois.length; i++)
        {
            Roi roi = rois[i];
            Rectangle2D r = roi.getFloatBounds();
            roi.setLocation(r.getX() + dx, r.getY() + dy);
        }
        ImagePlus imp = WindowManager.getCurrentImage();
        if (imp != null)
        {
            Roi roi = imp.getRoi();
            if ((roi != null) && ((rois.length != 1) || (rois[0] != roi)))
            {
                Rectangle2D r = roi.getFloatBounds();
                roi.setLocation(r.getX() + dx, r.getY() + dy);
            }
            imp.draw();
        }
    }

    private boolean save(String name, boolean saveSelected)
    {
        if ((!name.endsWith(".zip")) && (!name.equals(""))) {
            return error("Name must end with '.zip'");
        }
        if (getCount() == 0) {
            return error("The selection list is empty.");
        }
        int[] indexes = null;
        if (saveSelected) {
            indexes = getIndexes();
        } else {
            indexes = getAllIndexes();
        }
        boolean ok = false;
        if (name.equals("")) {
            ok = saveMultiple(indexes, null);
        } else {
            ok = saveMultiple(indexes, name);
        }
        this.macro = false;
        return ok;
    }

    public boolean runCommand(String cmd, String hexColor, double lineWidth)
    {
        if ((hexColor == null) && (lineWidth == 1.0D) && (IJ.altKeyDown()) && (!Interpreter.isBatchMode()))
        {
            addRoi(true);
        }
        else
        {
            Color color = hexColor != null ? Colors.decode(hexColor, Color.cyan) : null;
            addRoi(null, false, color, (int)Math.round(lineWidth));
        }
        return true;
    }

    private void setRoiPosition(ImagePlus imp, Roi roi)
    {
        if ((imp == null) || (roi == null)) {
            return;
        }
        if (imp.isHyperStack()) {
            roi.setPosition(imp.getChannel(), imp.getSlice(), imp.getFrame());
        } else if (imp.getStackSize() > 1) {
            roi.setPosition(imp.getCurrentSlice());
        }
    }

    public void select(int index)
    {
        select(null, index);
    }

    public void select(ImagePlus imp, int index)
    {
        this.selectedIndexes = null;
        if (index < 0)
        {
            deselect();
            return;
        }
        int n = getCount();
        if (index >= n) {
            return;
        }
        boolean mm = this.list.getSelectionMode() == 2;
        if (mm) {
            this.list.setSelectionMode(0);
        }
        int delay = 1;
        long start = System.currentTimeMillis();
        while (!this.list.isSelectedIndex(index))
        {
            this.list.clearSelection();
            this.list.setSelectedIndex(index);
        }
        if (imp == null) {
            imp = WindowManager.getCurrentImage();
        }
        if (imp != null) {
            restore(imp, index, true);
        }
        if (mm) {
            this.list.setSelectionMode(2);
        }
    }

    public void selectAndMakeVisible(ImagePlus imp, int index)
    {
        select(imp, index);
        this.list.ensureIndexIsVisible(index);
    }

    public void select(int index, boolean shiftKeyDown, boolean altKeyDown)
    {
        if ((!shiftKeyDown) && (!altKeyDown)) {
            select(index);
        }
        ImagePlus imp = IJ.getImage();
        if (imp == null) {
            return;
        }
        Roi previousRoi = imp.getRoi();
        if (previousRoi == null)
        {
            select(index);
            return;
        }
        Roi.previousRoi = (Roi)previousRoi.clone();
        Roi roi = (Roi)this.rois.get(index);
        if (roi != null)
        {
            roi.setImage(imp);
            roi.update(shiftKeyDown, altKeyDown);
        }
    }

    public void deselect()
    {
        int n = getCount();
        for (int i = 0; i < n; i++) {
            this.list.clearSelection();
        }
        if (record()) {
            Recorder.record("roiManager", "Deselect");
        }
    }

    public void deselect(Roi roi)
    {
        int[] indexes = getSelectedIndexes();
        if (indexes.length == 1)
        {
            String label = (String)this.listModel.getElementAt(indexes[0]);
            if (label.equals(roi.getName()))
            {
                deselect();
                repaint();
            }
        }
    }

    public void setEditMode(ImagePlus imp, boolean editMode)
    {
        this.showAllCheckbox.setState(editMode);
        this.labelsCheckbox.setState(editMode);
        showAll(editMode ? 2 : 1);
    }

    public void close()
    {
        super.close();
        instance = null;
        Prefs.saveLocation("manager.loc", getLocation());
        if ((!this.showAllCheckbox.getState()) || (IJ.macroRunning())) {
            return;
        }
        int n = getCount();
        ImagePlus imp = WindowManager.getCurrentImage();
        if ((imp == null) || ((imp.getCanvas() != null) && (imp.getCanvas().getShowAllList() == null))) {
            return;
        }
        if (n > 0)
        {
            GenericDialog gd = new GenericDialog("ROI Manager");
            gd.addMessage("Save the " + n + " displayed ROIs as an overlay?");
            gd.setOKLabel("Discard");
            gd.setCancelLabel("Save as Overlay");
            gd.showDialog();
            if (gd.wasCanceled()) {
                moveRoisToOverlay(imp);
            } else {
                removeOverlay(imp);
            }
        }
        else
        {
            imp.draw();
        }
    }

    public void moveRoisToOverlay(ImagePlus imp)
    {
        if (imp == null) {
            return;
        }
        Roi[] rois = getRoisAsArray();
        int n = rois.length;
        Overlay overlay = imp.getOverlay();
        if (overlay == null) {
            overlay = newOverlay();
        }
        for (int i = 0; i < n; i++)
        {
            Roi roi = (Roi)rois[i].clone();
            if (!Prefs.showAllSliceOnly) {
                roi.setPosition(0);
            }
            if (roi.getStrokeWidth() == 1.0F) {
                roi.setStrokeWidth(0.0F);
            }
            overlay.add(roi);
        }
        imp.setOverlay(overlay);
        if (imp.getCanvas() != null) {
            setOverlay(imp, null);
        }
    }

    public void mousePressed(MouseEvent e)
    {
        int x = e.getX();int y = e.getY();
        if ((e.isPopupTrigger()) || (e.isMetaDown())) {
            this.pm.show(e.getComponent(), x, y);
        }
    }

    public void mouseWheelMoved(MouseWheelEvent event)
    {
        synchronized (this)
        {
            int index = this.list.getSelectedIndex();
            int rot = event.getWheelRotation();
            if (rot < -1) {
                rot = -1;
            }
            if (rot > 1) {
                rot = 1;
            }
            index += rot;
            if (index < 0) {
                index = 0;
            }
            if (index >= getCount()) {
                index = getCount();
            }
            select(index);
            if (IJ.isWindows()) {
                this.list.requestFocusInWindow();
            }
            if ((IJ.isJava18()) && (IJ.isMacOSX())) {
                repaint();
            }
        }
    }

    public void setSelectedIndexes(int[] indexes)
    {
        int count = getCount();
        if (count == 0) {
            return;
        }
        for (int i = 0; i < indexes.length; i++)
        {
            if (indexes[i] < 0) {
                indexes[i] = 0;
            }
            if (indexes[i] >= count) {
                indexes[i] = (count - 1);
            }
        }
        this.selectedIndexes = indexes;
        this.list.setSelectedIndices(indexes);
    }

    public int[] getSelectedIndexes()
    {
        if (this.selectedIndexes != null)
        {
            int[] indexes = this.selectedIndexes;
            this.selectedIndexes = null;
            return indexes;
        }
        return this.list.getSelectedIndices();
    }

    public int[] getIndexes()
    {
        int[] indexes = getSelectedIndexes();
        if (indexes.length == 0) {
            indexes = getAllIndexes();
        }
        return indexes;
    }

    public boolean isSelected(int index)
    {
        return (index >= 0) && (index < this.listModel.getSize()) && (this.list.isSelectedIndex(index));
    }

    private Overlay newOverlay()
    {
        Overlay overlay = OverlayLabels.createOverlay();
        overlay.drawLabels(this.labelsCheckbox.getState());
        if ((overlay.getLabelFont() == null) && (overlay.getLabelColor() == null))
        {
            overlay.setLabelColor(Color.white);
            overlay.drawBackgrounds(true);
        }
        overlay.drawNames(Prefs.useNamesAsLabels);
        return overlay;
    }

    private void removeOverlay(ImagePlus imp)
    {
        if ((imp != null) && (imp.getCanvas() != null)) {
            setOverlay(imp, null);
        }
    }

    private void setOverlay(ImagePlus imp, Overlay overlay)
    {
        if (imp == null) {
            return;
        }
        ImageCanvas ic = imp.getCanvas();
        if (ic == null)
        {
            imp.setOverlay(overlay);
            return;
        }
        ic.setShowAllList(overlay);
        imp.draw();
    }

    private boolean record()
    {
        return (Recorder.record) && (this.allowRecording) && (!IJ.isMacro());
    }

    private boolean recordInEvent()
    {
        return (Recorder.record) && (!IJ.isMacro());
    }

    public void allowRecording(boolean allow)
    {
        this.allowRecording = allow;
    }

    public void mouseReleased(MouseEvent e) {}

    public void mouseClicked(MouseEvent e) {}

    public void mouseEntered(MouseEvent e) {}

    public void mouseExited(MouseEvent e) {}

    public void valueChanged(ListSelectionEvent e)
    {
        if (e.getValueIsAdjusting()) {
            return;
        }
        if (getCount() == 0)
        {
            if (recordInEvent()) {
                Recorder.record("roiManager", "Deselect");
            }
            return;
        }
        int[] selected = this.list.getSelectedIndices();
        if (selected.length == 0)
        {
            this.imageID = 0;
            return;
        }
        if (WindowManager.getCurrentImage() != null)
        {
            if (selected.length == 1)
            {
                ImagePlus imp = getImage();
                if (imp != null)
                {
                    Roi roi = imp.getRoi();
                    if (roi != null) {
                        Roi.previousRoi = (Roi)roi.clone();
                    }
                }
                restore(imp, selected[0], true);
                this.imageID = (imp != null ? imp.getID() : 0);
            }
            if (recordInEvent())
            {
                String arg = Arrays.toString(selected);
                if ((!arg.startsWith("[")) || (!arg.endsWith("]"))) {
                    return;
                }
                arg = arg.substring(1, arg.length() - 1);
                arg = arg.replace(" ", "");
                if (Recorder.scriptMode())
                {
                    if (selected.length == 1) {
                        Recorder.recordCall("rm.select(" + arg + ");");
                    } else {
                        Recorder.recordCall("rm.setSelectedIndexes([" + arg + "]);");
                    }
                }
                else if (selected.length == 1) {
                    Recorder.recordString("roiManager(\"Select\", " + arg + ");\n");
                } else {
                    Recorder.recordString("roiManager(\"Select\", newArray(" + arg + "));\n");
                }
            }
        }
    }

    public void windowActivated(WindowEvent e)
    {
        super.windowActivated(e);
        ImagePlus imp = WindowManager.getCurrentImage();
        if ((imp != null) &&
                (this.imageID != 0) && (imp.getID() != this.imageID))
        {
            showAll(1);
            if (okToSet()) {
                this.showAllCheckbox.setState(false);
            }
            deselect();
            this.imageID = 0;
        }
    }
}
