package org.basex.gui.view.query;

import static org.basex.Text.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import org.basex.data.Nodes;
import org.basex.gui.GUI;
import org.basex.gui.GUICommands;
import org.basex.gui.GUIProp;
import org.basex.gui.GUIToolBar;
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
  /** Filter button. */
  BaseXButton filter;
  /** Last Query. */
  String last = "";

  /** Initializes the panel. */
  abstract void init();
    /** Closes the panel. */
  abstract void finish();
  /** Reacts on the GUI termination. */
  abstract void quit();

  /** Refreshes the panel. */
  void refresh() {
    BaseXLayout.enable(go, !GUIProp.execrt);
    final Nodes marked = main.gui.context.marked();
    if(marked == null) return;
    BaseXLayout.enable(filter, !GUIProp.filterrt && marked.size() != 0);
  }

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
        main.gui.stop();
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
    
    filter = GUIToolBar.newButton(GUICommands.FILTER, main.gui);
    filter.addKeyListener(main);
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
