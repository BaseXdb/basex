package org.basex.gui.view.query;

import static org.basex.Text.*;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import org.basex.gui.GUI;
import org.basex.gui.layout.BaseXButton;
import org.basex.gui.layout.BaseXLayout;

/**
 * This interface defines methods for search panel implementations.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Christian Gruen
 */
abstract class QueryPanel {
  /** Main panel. */
  QueryView main;
  /** Execute button. */
  BaseXButton go;
  /** Execute Button. */
  BaseXButton stop;
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

  /**
   * Initializes components which are equal in all panels.
   */
  void initPanel() {
    stop = new BaseXButton(GUI.icon("cmd-stop"), HELPSTOP);
    stop.trim();
    stop.addKeyListener(main);
    stop.addActionListener(new ActionListener() {
      public void actionPerformed(final ActionEvent e) {
        BaseXLayout.enable(stop, false);
        GUI.get().stop();
      }
    });
    BaseXLayout.enable(stop, false);

    go = new BaseXButton(GUI.icon("cmd-go"), HELPGO);
    go.trim();
    go.addKeyListener(main);
    go.addActionListener(new ActionListener() {
      public void actionPerformed(final ActionEvent e) {
        query(true);
      }
    });
  }

  /**
   * Runs a query.
   * @param force force the execution of a new query.
   */
  abstract void query(boolean force);
  
  /**
   * Handles info messages resulting from a query execution.
   * @param info info message
   * @param ok true if query was successful
   * @return true if info was evaluated
   */
  abstract boolean info(final String info, final boolean ok);
}
