package org.basex.gui.dialog;

import static org.basex.gui.layout.BaseXKeys.*;
import static javax.swing.JOptionPane.*;
import static org.basex.core.Text.*;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import javax.swing.JComponent;
import javax.swing.JDialog;
import org.basex.gui.GUI;
import org.basex.gui.GUIConstants.Fill;
import org.basex.gui.GUIConstants.Msg;
import org.basex.gui.layout.BaseXBack;
import org.basex.gui.layout.BaseXButton;
import org.basex.gui.layout.TableLayout;

/**
 * This class provides the architecture for consistent dialog windows.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-10, ISC License
 * @author Christian Gruen
 */
public abstract class Dialog extends JDialog {
  /** Reference to main window. */
  public final GUI gui;
  /** Reference to the root panel. */
  protected final BaseXBack panel;
  /** Remembers if the window was correctly closed. */
  protected boolean ok;
  /** Dialog position. */
  private int[] loc;

  /** Key listener. */
  protected final KeyAdapter keys = new KeyAdapter() {
    @Override
    public void keyReleased(final KeyEvent e) {
      if(!modifier(e)) action(pressed(ENTER, e) ? e.getSource() : null);
    }
  };

  /**
   * Default constructor.
   * @param main reference to main window
   * @param title dialog title
   */
  protected Dialog(final GUI main, final String title) {
    this(main, title, true);
  }

  /**
   * Default constructor.
   * @param main reference to the main window
   * @param title dialog title
   * @param modal modal flag
   */
  protected Dialog(final GUI main, final String title, final boolean modal) {
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
   * @param comp the action component
   */
  @SuppressWarnings("unused")
  public void action(final Object comp) { /* */}

  /**
   * Called when GUI design has changed.
   */
  public void refresh() { /* */ }

  /**
   * Cancels the dialog; can be overwritten.
   */
  public void cancel() {
    ok = false;
    dispose();
  }

  /**
   * Closes the dialog; can be overwritten and stores the dialog position if it
   * has been specified before.
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
      gui.prop.write();
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
   * Creates a OK and CANCEL button.
   * @param dialog reference to the component, reacting on button clicks
   * @return button list
   */
  protected static BaseXBack okCancel(final Dialog dialog) {
    return newButtons(dialog, BUTTONOK, BUTTONCANCEL);
  }

  /**
   * Creates a new button list.
   * @param dialog reference to the component, reacting on button clicks
   * @param buttons button names or objects
   * @return button list
   */
  protected static BaseXBack newButtons(final Dialog dialog,
      final Object... buttons) {

    // horizontal/vertical layout
    final BaseXBack panel = new BaseXBack(Fill.NONE);
    panel.setBorder(12, 0, 0, 0);
    panel.setLayout(new TableLayout(1, buttons.length, 8, 0));
    for(final Object b : buttons) {
      panel.add(b instanceof Component ?
        (Component) b : new BaseXButton(b.toString(), dialog));
    }

    final BaseXBack but = new BaseXBack(Fill.NONE);
    but.setLayout(new BorderLayout());
    but.add(panel, BorderLayout.EAST);
    return but;
  }

  /**
   * Enables/disables a button in the specified panel.
   * @param panel button panel
   * @param label button label
   * @param enabled enabled/disabled
   */
  protected static void enableOK(final JComponent panel, final String label,
      final boolean enabled) {
    for(final Component c : panel.getComponents()) {
      if(!(c instanceof JComponent)) {
        continue;
      } else if(c instanceof BaseXButton) {
        final BaseXButton b = (BaseXButton) c;
        if(b.getText().equals(label)) b.setEnabled(enabled);
      } else {
        enableOK((JComponent) c, label, enabled);
      }
    }
  }

  /**
   * Static yes/no dialog.
   * @param comp parent reference
   * @param text text
   * @return true if dialog was confirmed
   */
  public static boolean confirm(final Component comp, final String text) {
    return showConfirmDialog(comp, text, NAME,
        YES_NO_OPTION, WARNING_MESSAGE) == YES_OPTION;
  }

  /**
   * Static information dialog.
   * @param gui parent reference
   * @param text text
   */
  public static void info(final GUI gui, final String text) {
    new DialogMessage(gui, text.trim(), Msg.SUCCESS);
  }

  /**
   * Static information dialog.
   * @param gui parent reference
   * @param text text
   */
  public static void warn(final GUI gui, final String text) {
    new DialogMessage(gui, text.trim(), Msg.WARN);
  }

  /**
   * Static error dialog.
   * @param gui parent reference
   * @param text text
   */
  public static void error(final GUI gui, final String text) {
    new DialogMessage(gui, text.trim(), Msg.ERROR);
  }
}
