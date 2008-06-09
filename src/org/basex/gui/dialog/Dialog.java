package org.basex.gui.dialog;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import org.basex.gui.layout.BaseXBack;

/**
 * This class provides the architecture for consistent dialog windows.
 * 
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Christian Gruen
 */
public abstract class Dialog extends JDialog {
  /** Remembers if the window was correctly closed. */
  protected boolean ok;
  /** Reference to the root panel. */
  private BaseXBack panel;

  /**
   * Default Constructor.
   * @param parent parent frame
   * @param title dialog title
   */
  public Dialog(final JFrame parent, final String title) {
    this(parent, title, true);
  }
  
  /**
   * Default Constructor.
   * @param parent parent frame
   * @param title dialog title
   * @param modal modal flag
   */
  public Dialog(final JFrame parent, final String title, final boolean modal) {
    super(parent, title, modal);
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
   * @param constr layout constraints
   */
  public final void set(final Component comp, final Object constr) {
    panel.add(comp, constr);
  }

  /**
   * Finishes dialog layout.
   * @param parent parent frame
   */
  public final void finish(final JFrame parent) {
    finish(parent, null);
  }

  /**
   * Finishes dialog layout and sets the dialog to the specified
   * position relative to the main window.
   * @param parent parent frame
   * @param loc dialog location
   */
  public final void finish(final JFrame parent, final int[] loc) {
    pack();
    if(loc == null) setLocationRelativeTo(parent);
    else setLocation(parent.getX() + loc[0], parent.getY() + loc[1]);
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
