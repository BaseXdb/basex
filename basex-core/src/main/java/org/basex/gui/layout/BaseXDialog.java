package org.basex.gui.layout;

import static org.basex.core.Text.*;
import static org.basex.gui.layout.BaseXKeys.*;

import java.awt.*;
import java.awt.event.*;
import java.net.*;

import javax.swing.*;

import org.basex.core.*;
import org.basex.gui.*;
import org.basex.gui.GUIConstants.Msg;
import org.basex.gui.dialog.*;
import org.basex.util.*;

/**
 * This superclass in inherited by all dialog windows.
 *
 * @author BaseX Team 2005-15, BSD License
 * @author Christian Gruen
 */
public abstract class BaseXDialog extends JDialog {
  /** Reference to main window. */
  public GUI gui;
  /** Used mnemonics. */
  protected final StringBuilder mnem = new StringBuilder();
  /** Remembers if the window was correctly closed. */
  protected boolean ok;
  /** Reference to the root panel. */
  protected BaseXBack panel;

  /** Dialog position. */
  private int[] loc;

  /** Key listener, triggering an action with each click. */
  public final KeyAdapter keys = new KeyAdapter() {
    @Override
    public void keyReleased(final KeyEvent e) {
      // don't trigger any action for modifier keys
      if(!modifier(e) && e.getKeyChar() != KeyEvent.CHAR_UNDEFINED) action(e.getSource());
    }
  };

  /**
   * Constructor, called from a dialog window.
   * @param d calling dialog
   * @param title dialog title
   */
  protected BaseXDialog(final BaseXDialog d, final String title) {
    super(d, title, true);
    init(d.gui);
  }

  /**
   * Constructor, called from the main window.
   * @param main reference to main window
   * @param title dialog title
   */
  protected BaseXDialog(final GUI main, final String title) {
    this(main, title, true);
  }

  /**
   * Constructor, called from the main window.
   * @param main reference to the main window
   * @param title dialog title
   * @param modal modal flag
   */
  protected BaseXDialog(final GUI main, final String title, final boolean modal) {
    super(main, title, modal);
    init(main);
  }

  /**
   * Initializes the dialog.
   * @param main reference to the main window
   */
  private void init(final GUI main) {
    gui = main;
    panel = new BaseXBack(new BorderLayout()).border(10, 10, 10, 10);
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
    setMinimumSize(getPreferredSize());
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
   * @param source source
   */
  @SuppressWarnings("unused")
  public void action(final Object source) { }

  /**
   * Cancels the dialog; can be overwritten.
   */
  public void cancel() {
    ok = false;
    dispose();
  }

  /**
   * Closes the dialog and stores the location of the dialog window; can be overwritten.
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
    }
    super.dispose();
    gui.gopts.write();
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
   * @return button list
   */
  protected BaseXBack okCancel() {
    return newButtons(B_OK, CANCEL);
  }

  /**
   * Creates a new button list.
   * @param buttons button names or objects
   * @return button list
   */
  public BaseXBack newButtons(final Object... buttons) {
    // horizontal/vertical layout
    final BaseXBack pnl = new BaseXBack(false).
      border(12, 0, 0, 0).layout(new TableLayout(1, buttons.length, 8, 0));

    for(final Object obj : buttons) {
      final BaseXButton b;
      if(obj instanceof BaseXButton) {
        b = (BaseXButton) obj;
      } else {
        b = new BaseXButton(obj.toString(), this);
      }
      pnl.add(b);
    }

    final BaseXBack but = new BaseXBack(false).layout(new BorderLayout());
    but.add(pnl, BorderLayout.EAST);
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
      if(c instanceof BaseXButton) {
        final BaseXButton b = (BaseXButton) c;
        if(b.getText().equals(label)) b.setEnabled(enabled);
      } else if(c instanceof JComponent) {
        enableOK((JComponent) c, label, enabled);
      }
    }
  }

  /**
   * Static yes/no/cancel dialog. Returns {@code null} if the dialog was canceled.
   * @param gui parent reference
   * @param text text
   * @param buttons additional buttons
   * @return true if dialog was confirmed
   */
  public static String yesNoCancel(final GUI gui, final String text, final String... buttons) {
    return new DialogMessage(gui, text.trim(), Msg.YESNOCANCEL, buttons).action();
  }

  /**
   * Static yes/no dialog.
   * @param gui parent reference
   * @param text text
   * @return true if dialog was confirmed
   */
  public static boolean confirm(final GUI gui, final String text) {
    return Text.B_YES.equals(new DialogMessage(gui, text.trim(), Msg.QUESTION).action());
  }

  /**
   * Static error dialog.
   * @param gui parent reference
   * @param text text
   */
  public static void error(final GUI gui, final String text) {
    new DialogMessage(gui, text.trim(), Msg.ERROR);
  }

  /**
   * Browses the specified url.
   * @param gui parent reference
   * @param url url to be browsed
   */
  public static void browse(final GUI gui, final String url) {
    try {
      Desktop.getDesktop().browse(new URI(url));
    } catch(final Exception ex) {
      error(gui, Util.info(H_BROWSER_ERROR_X, Prop.URL));
    }
  }
}
