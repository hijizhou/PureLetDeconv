package com.hijizhou.cores.ratiometric;

/*
 * IJ BAR: https://github.com/tferr/Scripts
 *
 * This program is free software; you can redistribute it and/or modify it under the terms of the
 * GNU General Public License as published by the Free Software Foundation
 * (http://www.gnu.org/licenses/gpl.txt).
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 */

import ij.IJ;
import ij.ImagePlus;
import ij.Prefs;
import ij.WindowManager;
import ij.gui.GenericDialog;
import ij.gui.ImageWindow;
import ij.gui.PlotWindow;
import ij.io.OpenDialog;
import ij.measure.ResultsTable;
import ij.plugin.PlugIn;
import ij.text.TextWindow;

import java.awt.*;
import java.awt.event.WindowListener;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Random;

/**
 * A collection of utilities to interact with BAR, including scripting aids for
 * ImageJ. Most methods are designed so that they can be called by the IJ macro
 * language (BTW, Note that the IJ1 macro interpreter converts all returned
 * objects into their String values).
 */
public class ResultsTableUtils implements PlugIn {

    /** The URL to BAR's wiki page */
    static final String DOC_URL = "http://imagej.net/BAR";

    /** The URL to BAR's GitHub repository */
    static final String SRC_URL = "https://github.com/tferr/Scripts";

    /** The URL to BAR's GitHub repository */
    static final String API_URL = "http://tferr.github.io/Scripts/apidocs/";

    /** The absolute path to the /BAR directory */
    private static final String BAR_DIR = Prefs.getImageJDir() + "scripts" + File.separator + "BAR" + File.separator;

    /** The absolute path to the /BAR/lib/ directory */
    static final String LIB_DIR = BAR_DIR + "lib" + File.separator;

    /** Checks if table in the "Results" window contains valid data */
    private static boolean validResultsTable() {
        final ResultsTable rt = ResultsTable.getResultsTable();
        return (ResultsTable.getResultsWindow() != null && rt != null && rt.getCounter() != 0);
    }

    /**
     * Returns a reference to the default IJ Results table. It the Results table
     * is not displayed or is empty, prompts the user for data to populate it
     * (by using {@link #getTable(boolean, WindowListener)} to display data in
     * the "Results" window). This method is thought for IJ macros, since the IJ
     * macro language can only interact with the "Results" window. Note that any
     * previous data in the "Results" window will be lost.
     *
     * @return A reference to the populated {@code ResultsTable} in the
     *         "Results" window or {@code null} if chosen source did not contain
     *         valid data. Note that the IJ1 macro interpreter converts all
     *         returned objects into their String values
     *
     * @see #getTable()
     */
    public static ResultsTable getResultsTable() {
        try {
            ResultsTable table = ResultsTable.getResultsTable();
            if (table == null || table.getCounter() == 0)
                table = getTable(true, null);
            return table;
        } catch (final Exception ignored) { // useful for IJM calls
            return null;
        }
    }

    /**
     * Variant of {@link #getTable(boolean, WindowListener)} that ignores
     * WindowListeners.
     *
     * @return A reference to the chosen {@code ResultsTable} or {@code null} if
     *         chosen source did not contain valid data
     *
     * @see #getTable(boolean, WindowListener)
     * @see #getResultsTable()
     */
    public static ResultsTable getTable() {
        return getTable(false, null);
    }

    /**
     * Returns the ResultsTable associated with the specified TextWindow.
     *
     * @param textWindowTitle
     *            The title of the {@code TextWindow} holding the
     *            {@code TextPanel} and associated {@code ResultsTable}
     *
     * @return The {@code ResultsTable} associated with the specified TextWindow
     *         title or an empty {@code ResultsTable} if no table exists.
     *
     * @see #getTable(boolean, WindowListener)
     * @see #getResultsTable()
     */
    public static ResultsTable getTable(final String textWindowTitle) {
        ResultsTable rt = null;
        final TextWindow window = (TextWindow) WindowManager.getFrame(textWindowTitle);
        if (window != null)
            rt = window.getTextPanel().getResultsTable();
        if (rt == null)
            rt = new ResultsTable();
        return rt;
    }

    /**
     * Prompts the user for tabular data, retrieved from several sources
     * including 1) Importing a new text/csv file; 2) Trying to import data from
     * the system clipboard; 3) Importing a demo dataset populated by random
     * (Gaussian) values; 4) the {@link ResultsTable} of any {@link TextWindow}
     * or a {@link PlotWindow} currently being displayed in ImageJ. For 1) and
     * 2) data is displayed in a new TextWindow.
     *
     * @param displayInResults
     *            If {@code true} chosen data is displayed in the "Results"
     *            window. Useful, since macros (and several plugins) can only
     *            work with the "Results" window. Note that any previous data in
     *            the "Results" window will be lost. If {@code false} chosen
     *            data is displayed on a dedicated window.
     *
     * @param listener
     *            The {@link WindowListener} to be added to the window
     *            containing data (if retrieval was successful. It is ignored
     *            when null. Note that the window containing the data can either
     *            be a {@link TextWindow} or a {@link PlotWindow}
     *
     * @return A reference to the chosen {@code ResultsTable} or {@code null} if
     *         chosen source did not contain valid data
     *
     * @see #getResultsTable()
     * @see #getTable()
     */
    public static ResultsTable getTable(final boolean displayInResults, final WindowListener listener) {
        return getTable(null, displayInResults, true, listener);
    }

    public static ResultsTable getTable(final Component relativeComponent, final boolean displayInResults,
                                        final boolean offerSampleChoice, final WindowListener listener) {

        ResultsTable rt = null;
        final ArrayList<ResultsTable> tables = new ArrayList<>();
        final ArrayList<String> tableTitles = new ArrayList<>();

        // Retrieve tables from all available TextWindows
        final Frame[] windows = WindowManager.getNonImageWindows();
        for (final Frame w : windows) {
            if (w == null)
                continue;
            if (w instanceof TextWindow) {
                final TextWindow rtWindow = (TextWindow) w;
                rt = ((TextWindow) w).getTextPanel().getResultsTable();
                if (rt != null) {
                    if (displayInResults && rt == ResultsTable.getResultsTable())
                        continue;
                    tables.add(rt);
                    tableTitles.add(rtWindow.getTitle());
                }
            }
        }

        // Retrieve tables from all available PlotWindows
        final int[] ids = WindowManager.getIDList();
        if (ids != null) {
            for (final int id : ids) {
                final ImagePlus pImp = WindowManager.getImage(id);
                if (pImp == null)
                    continue;
                final ImageWindow pWin = pImp.getWindow();
                if (pWin == null)
                    continue;
                if (pWin instanceof PlotWindow) {
                    rt = ((PlotWindow) pWin).getResultsTable();
                    if (rt != null) {
                        tables.add(rt);
                        tableTitles.add(pWin.getTitle());
                    }
                }
            }
        }
        final boolean noTablesOpened = tableTitles.isEmpty();

        // Append options for external sources
        tableTitles.add("External file...");
        tableTitles.add("Clipboard");
        if (offerSampleChoice)
            tableTitles.add("Demo sample of Gaussian values");

        // Make prompt as intuitive as possible
        String gdTitle = "Choose Data Source";
        String subtitle = "Use tabular data from:";
        if (displayInResults) {
            if (validResultsTable()) {
                gdTitle = "Transfer Data to Results Table";
            } else {
                gdTitle = "No Data in Results Table";
            }
            subtitle = "Replace values in \"Results\" table with data from:";
        }

        // Build prompt
        final GenericDialog gd = new GenericDialog(gdTitle);
        final int cols = (tableTitles.size() < 18) ? 1 : 2;
        final int rows = (tableTitles.size() % cols > 0) ? tableTitles.size() / cols + 1 : tableTitles.size() / cols;
        gd.addRadioButtonGroup(subtitle, tableTitles.toArray(new String[tableTitles.size()]), rows, cols,
                tableTitles.get(0));
        // gd.hideCancelButton();
        if (relativeComponent != null)
            gd.setLocationRelativeTo(relativeComponent);
        gd.showDialog();
        if (gd.wasCanceled())
            return null;

        if (gd.wasOKed()) {

            final String choice = gd.getNextRadioButton();
            String rtTitle;
            Window win = null;

            if (choice.equals("External file...")) {

                rtTitle = (displayInResults) ? "Results" : null;
                try {
                    return openAndDisplayTable("", rtTitle, listener);
                } catch (final IOException exc) {
                    IJ.error(exc.getMessage());
                    return null;
                }

                // Clipboard
            } else if (choice.equals("Clipboard")) {
//                final String clipboard = getClipboardText();
//                final String error = "Clipboard does not seem to contain valid data";
//                if (clipboard == null || clipboard.isEmpty()) {
//                    IJ.error(error);
//                    return null;
//                }
//                try {
//                    final File temp = File.createTempFile("BARclipboard", ".txt");
//                    temp.deleteOnExit();
//                    try (PrintStream out = new PrintStream(temp.getAbsolutePath())) {
//                        out.println(clipboard);
//                        out.close();
//                    } catch (final Exception exc) {
//                        IJ.error("Could not extract data from clipboard.");
//                        return null;
//                    }
//                    rtTitle = (displayInResults) ? "Results" : "Clipboard Data";
//                    rt = openAndDisplayTable(temp.getAbsolutePath(), rtTitle, listener, true);
//                    if (rt == null) {
//                        IJ.error(error);
//                        return null;
//                    }
//                } catch (final IOException exc) {
//                    IJ.error("Could not extract data from clipboard.");
//                    return null;
//                }

            } else if (choice.equals("Demo sample of Gaussian values")) {

                rt = generateGaussianData();
                if (rt != null) {
                    rtTitle = (displayInResults) ? "Results" : WindowManager.makeUniqueName("Gaussian Data");
                    rt.show(rtTitle);
                    win = WindowManager.getFrame(rtTitle);
                }

                // Any other ResultsTable in available TextWindows/PlotWindows
            } else if (!noTablesOpened) {

                rt = tables.get(tableTitles.indexOf(choice));
                if (displayInResults) {
                    rt.show("Results");
                    win = ResultsTable.getResultsWindow();
                } else {
                    win = WindowManager.getFrame(choice);
                }

                // ??
            } else
                return null;

            if (win != null && listener != null)
                win.addWindowListener(listener);
        }

        // Ensure nothing went awry when overriding the "Results" window
        if (displayInResults && !validResultsTable())
            rt = null;

        return rt;

    }

    /**
     * Opens a tab or comma delimited text file.
     *
     * @param path
     *            The absolute pathname string of the file. A file open dialog
     *            is displayed if path is {@code null} or an empty string.
     * @param title
     *            The title of the window in which data is displayed. The
     *            filename is used if title is null or an empty string. To avoid
     *            windows with duplicated titles, title is made unique by
     *            {@link WindowManager}.
     * @param listener
     *            The {@link WindowListener} to be added to the window
     *            containing data if retrieval was successful. It is ignored
     *            when {@code null}.
     * @param silent
     *            If {@code true} I/O exceptions are silently ignored.
     *
     * @return A reference to the opened {link ResultsTable} or {@code null} if
     *         file could not be open.
     *
     * @see #getTable()
     * @see ij.io.Opener#openTable(String)
     */
    public static ResultsTable openAndDisplayTable(final String path, final String title, final WindowListener listener,
                                                   final boolean silent) {
        try {
            return openAndDisplayTable(path, title, listener);
        } catch (final IOException exc) {
            if (!silent)
                IJ.handleException(exc);
            return null;
        }

    }

    /**
     * Opens a tab or comma delimited text file.
     *
     * @param path
     *            The absolute pathname string of the file. A file open dialog
     *            is displayed if path is {@code null} or an empty string.
     * @param title
     *            The title of the window in which data is displayed. The
     *            filename is used if title is null or an empty string. To avoid
     *            windows with duplicated titles, title is made unique by
     *            {@link WindowManager} .
     * @param listener
     *            The {@link WindowListener} to be added to the window
     *            containing data if retrieval was successful. It is ignored
     *            when {@code null}.
     * @throws IOException
     *             if file could not be opened
     * @return A reference to the opened {link ResultsTable} or {@code null} if
     *         table was empty.
     *
     * @see #getTable()
     * @see ij.io.Opener#openTable(String)
     */
    public static ResultsTable openAndDisplayTable(final String path, final String title, final WindowListener listener)
            throws IOException {
        ResultsTable rt = null;
        rt = ResultsTable.open(path);
        if (rt == null || rt.getCounter() == 0) // nothing to be displayed
            return null;
        rt.showRowNumbers(false);
        String rtTitle = (title != null && !title.isEmpty()) ? title : OpenDialog.getLastName();
        rtTitle = WindowManager.makeUniqueName(rtTitle);
        rt.show(rtTitle);
        final TextWindow rtWindow = (TextWindow) WindowManager.getFrame(rtTitle);
        if (rtWindow != null && listener != null)
            rtWindow.addWindowListener(listener);
        return rt;
    }

    /**
     * Returns a {@link ResultsTable} containing Gaussian ("normally")
     * distributed values without displaying it.
     *
     * @return the {link ResultsTable} containing the demo data
     *
     * @see #getTable(boolean, WindowListener)
     * @see #getTable()
     * @see #getResultsTable()
     */
    public static ResultsTable generateGaussianData() {
        final ResultsTable rt = new ResultsTable();
        final double[] m1MeanSD = { 200, 50 };
        final double[] m2MeanSD = { 250, 50 };
        final double[] a1MeanSD = { 350, 100 };
        final double[] a2MeanSD = { 300, 100 };
        final double[] xyMeanSD = { 500, 100 };
        double value;
        for (int i = 0; i < 50; i++) {
            rt.incrementCounter();
            value = new Random().nextGaussian();
            rt.setLabel("Type I", i);
            rt.setValue("Mean", i, m1MeanSD[0] + value * m1MeanSD[1]);
            rt.setValue("Area", i, a1MeanSD[0] + value * a1MeanSD[1]);
            rt.setValue("X", i, xyMeanSD[0] + new Random().nextGaussian() * xyMeanSD[1]);
            rt.setValue("Y", i, xyMeanSD[0] + new Random().nextGaussian() * xyMeanSD[1]);
        }
        for (int i = 50; i < 100; i++) {
            rt.incrementCounter();
            value = new Random().nextGaussian();
            rt.setLabel("Type II", i);
            rt.setValue("Mean", i, m2MeanSD[0] + value * m2MeanSD[1]);
            rt.setValue("Area", i, a2MeanSD[0] + value * a2MeanSD[1]);
            rt.setValue("X", i, xyMeanSD[0] + new Random().nextGaussian() * xyMeanSD[1]);
            rt.setValue("Y", i, xyMeanSD[0] + new Random().nextGaussian() * xyMeanSD[1]);
        }
        return rt;
    }

    public static URL getBARresource(final String resourcePath) {
        final ClassLoader loader = Thread.currentThread().getContextClassLoader();
        URL resource = null;
        try {
            final Enumeration<URL> resources = loader.getResources(resourcePath);
            while (resources.hasMoreElements()) {
                resource = resources.nextElement();
                final String path = urlPath(resource);
                if (path == null)
                    continue;
                if (path.contains("BAR"))
                    return resource;
            }
        } catch (final IOException exc) {
            // proceed with return null;
        }
        return resource;
    }

    private static String urlPath(final URL url) {
        try {
            return url.toURI().toString();
        } catch (final URISyntaxException exc) {
            return null;
        }
    }

    @Override
    public void run(String s) {

    }
}
