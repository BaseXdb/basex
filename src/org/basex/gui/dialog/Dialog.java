package org.basex.gui.dialog;

import static org.basex.Text.*;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import org.basex.gui.GUI;
import org.basex.gui.GUIProp;
import org.basex.gui.layout.BaseXBack;

/**
 * This class provides the architecture for consistent dialog windows.
 * 
 * @author Workgroup DBIS, University of Konstanz 2005-09, ISC License
 * @author Christian Gruen
 */
public abstract class Dialog extends JDialog {
  /** Reference to main window. */
  protected final GUI gui;
  /** Remembers if the window was correctly closed. */
  protected boolean ok;
  /** Reference to the root panel. */
  protected BaseXBack panel;
  /** Dialog position. */
  private int[] loc;

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
      public void windowClosing(final WindowEvent e) { cancel(); }
    });
  }

  /**
   * Sets a component at the specified {@link BorderLayout} position.
   * @param comp component to be added
   * @param pos layout position
   */
  protected final void set(final Component comp, final String pos) {
    panel.add(comp, pos);
  }

  /**
   * Finalizes the dialog layout and sets it visible.
   * @param l optional dialog location, relative to main window
   */
  protected final void finish(final int[] l) {
    pack();
    if(l == null) setLocationRelativeTo(gui);
    else setLocation(gui.getX() + l[0], gui.getY() + l[1]);
    loc = l;
    setVisible(true);
  }

  @Override
  public void setLocation(final int x, final int y) {
    final Dimension scr = Toolkit.getDefaultToolkit().getScreenSize();
    final int xx = Math.max(0, Math.min(scr.width - getWidth(), x));
    final int yy = Math.max(0, Math.min(scr.height - getHeight(), y));
    super.setLocation(xx, yy);
  }

  /**
   * Reacts on user input; can be overwritten.
   * @param cmd the action command
   */
  @SuppressWarnings("unused")
  public void action(final String cmd) { }

  /**
   * Cancels the dialog; can be overwritten.
   */
  public void cancel() {
    ok = false;
    dispose();
  }

  /**
   * Closes the dialog; can be overwritten and stores the dialog position
   * if it has been specified before.
   */
  public void close() {
    ok = true;
    dispose();
  }

  @Override
  public void dispose() {
    if(loc != null) {
      final Container par = getParent();
      loc[0] = getX() - par.getX();
      loc[1] = getY() - par.getY();
      GUIProp.write();
    }
    super.dispose();
  }
  
  /**
   * States if the dialog window was confirmed or canceled.
   * @return true when dialog was confirmed
   */
  public final boolean ok() {
    return ok;
  }

  /**
   * Static yes/no dialog.
   * @param comp parent reference
   * @param text text
   * @return true if dialog was confirmed
   */
  public static boolean confirm(final Component comp, final String text) {
    return JOptionPane.showConfirmDialog(comp, text, DIALOGINFO,
        JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION;
  }

  /**
   * Static error dialog.
   * @param comp parent reference
   * @param text text
   */
  public static void error(final Component comp, final String text) {
    JOptionPane.showMessageDialog(comp, text, DIALOGINFO,
        JOptionPane.ERROR_MESSAGE);
  }

  /**
   * Static information dialog.
   * @param comp parent reference
   * @param text text
   */
  public static void info(final Component comp, final String text) {
    JOptionPane.showMessageDialog(comp, text, DIALOGINFO,
        JOptionPane.INFORMATION_MESSAGE);
  }
}
