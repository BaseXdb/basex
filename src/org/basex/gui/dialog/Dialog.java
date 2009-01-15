package org.basex.gui.dialog;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import javax.swing.JDialog;
import org.basex.gui.GUI;
import org.basex.gui.layout.BaseXBack;

/**
 * This class provides the architecture for consistent dialog windows.
 * 
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Christian Gruen
 */
public abstract class Dialog extends JDialog {
  /** Reference to main window. */
  protected final GUI gui;
  /** Remembers if the window was correctly closed. */
  protected boolean ok;
  /** Reference to the root panel. */
  private BaseXBack panel;

  /**
   * Default Constructor.
   * @param main reference to main window
   * @param title dialog title
   */
  public Dialog(final GUI main, final String title) {
    this(main, title, true);
  }
  
  /**
   * Default Constructor.
   * @param main reference to the main window
   * @param title dialog title
   * @param modal modal flag
   */
  public Dialog(final GUI main, final String title, final boolean modal) {
    super(main, title, modal);
    gui = main;
    panel = new BaseXBack();
    panel.setBorder(10, 10, 10, 10);
    panel.setLayout(new BorderLayout());
    add(panel, BorderLayout.CENTER);
    setResizable(false);
    
    addWindowListener(new WindowAdapter() {
      @Override
      public void windowClosing(final WindowEvent e) {
        cancel();
      }
    });
  }

  /**
   * Adds a component.
   * @param comp component to be added
   * @param pos layout position
   */
  public final void set(final Component comp, final String pos) {
    panel.add(comp, pos);
  }

  /**
   * Finishes dialog layout.
   */
  public final void finish() {
    finish(null);
  }

  /**
   * Finishes dialog layout and sets the dialog to the specified
   * position relative to the main window.
   * @param loc dialog location
   */
  public final void finish(final int[] loc) {
    pack();
    if(loc == null) setLocationRelativeTo(gui);
    else setLocation(gui.getX() + loc[0], gui.getY() + loc[1]);
    setVisible(true);
  }

  /**
   * Default action for canceling the dialog.
   */
  public void cancel() {
    ok = false;
    dispose();
  }

  /**
   * Default action for closing the dialog.
   */
  public void close() {
    ok = true;
    dispose();
  }

  /**
   * Reacts on user input.
   * @param cmd the action command
   */
  @SuppressWarnings("unused")
  public void action(final String cmd) { }
  
  /**
   * Stores the dialog location in the specified array (relative to main window)
   * and closes the window.
   * @param loc location array
   */
  protected final void close(final int[] loc) {
    final Container parent = getParent();
    loc[0] = Math.max(0, getX() - parent.getX());
    loc[1] = Math.max(0, getY() - parent.getY());
    dispose();
  }

  /**
   * States if the dialog window was confirmed or canceled.
   * @return true when dialog was confirmed
   */
  public final boolean ok() {
    return ok;
  }
}
