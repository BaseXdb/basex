package org.basex.gui.view.query;

/**
 * This interface defines methods for search panel implementations.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Christian Gruen
 */
abstract class QueryPanel {
  /** XPath Mode. */
  static final int XPATH = 0; 
  /** XQuery Mode. */
  static final int XQUERY = 1; 
  /** Simple query Mode. */
  static final int SIMPLE = 2; 
  /** Last Query. */
  String last = "";

  /** Initializes the panel. */
  abstract void init();
  /** Refreshes the panel. */
  abstract void refresh();
    /** Closes the panel. */
  abstract void finish();
  /** Reacts on the GUI termination. */
  abstract void quit();
  /** Runs a query.
   * @param force force the execution of a new query.
   */
  abstract void query(boolean force);
  
  /**
   * Handles info messages from the main window.
   * @param info info message
   * @param ok ok flag
   */
  abstract void info(final String info, final boolean ok);
}
