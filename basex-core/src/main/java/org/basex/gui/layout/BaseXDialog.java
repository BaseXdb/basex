package org.basex.gui.layout;

import static org.basex.core.Text.*;
import static org.basex.gui.layout.BaseXKeys.*;

import java.awt.*;
import java.awt.event.*;
import java.net.*;

import javax.swing.*;

import org.basex.core.*;
import org.basex.gui.*;
import org.basex.gui.GUIConstants.*;
import org.basex.gui.dialog.*;
import org.basex.gui.listener.*;
import org.basex.util.*;

/**
 * This superclass in inherited by all dialog windows.
 *
 * @author BaseX Team 2005-21, BSD License
 * @author Christian Gruen
 */
public abstract class BaseXDialog extends JDialog implements BaseXWindow {
  /** Reference to the main window. */
  public GUI gui;
  /** Used mnemonics. */
  final StringBuilder mnem = new StringBuilder();

  /** Remembers if the window was correctly closed. */
  protected boolean ok;
  /** Reference to the root panel. */
  protected BaseXBack panel;

  /** Key listener, triggering an action with each click. */
  public final KeyListener keys = (KeyReleasedListener) e -> {
    // don't trigger any action for modifier keys
    if(!modifier(e) && e.getKeyChar() != KeyEvent.CHAR_UNDEFINED) action(e.getSource());
  };

  /**
   * Constructor, called from a dialog window.
   * @param dialog calling dialog
   * @param title dialog title
   */
  protected BaseXDialog(final BaseXDialog dialog, final String title) {
    super(dialog, title, true);
    gui = dialog.gui;
    init();
  }

  /**
   * Constructor, called from the main window.
   * @param gui reference to the main window
   * @param title dialog title
   */
  protected BaseXDialog(final GUI gui, final String title) {
    this(gui, title, true);
  }

  /**
   * Constructor, called from the main window.
   * @param gui reference to the main window
   * @param title dialog title
   * @param modal modal flag
   */
  protected BaseXDialog(final GUI gui, final String title, final boolean modal) {
    super(gui, title, modal);
    this.gui = gui;
    init();
  }

  /**
   * Initializes the dialog.
   */
  private void init() {
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
   */
  protected final void finish() {
    pack();
    setMinimumSize(getPreferredSize());
    setLocationRelativeTo(gui);
    setVisible(true);
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
    // modal dialog: save options, remove GUI reference
    if(gui != null && modal()) {
      gui.saveOptions();
      gui = null;
    }
    super.dispose();
  }

  /**
   * Indicates if this is a modal dialog.
   * @return result of check
   */
  public final boolean modal() {
    return getModalityType() != ModalityType.MODELESS;
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
  protected final BaseXBack okCancel() {
    return newButtons(B_OK, B_CANCEL);
  }

  /**
   * Creates a new button list.
   * @param buttons button names or objects
   * @return button list
   */
  public final BaseXBack newButtons(final Object... buttons) {
    // horizontal/vertical layout
    final BaseXBack pnl = new BaseXBack(false).
      border(12, 0, 0, 0).layout(new TableLayout(1, buttons.length, 8, 0));

    for(final Object obj : buttons) {
      pnl.add(obj instanceof BaseXButton ? (BaseXButton) obj :
        new BaseXButton(this, obj.toString()));
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
   * @return chosen action ({@link Text#B_YES}, {@link Text#B_NO}, {@link Text#B_CANCEL})
   */
  public static String yesNoCancel(final GUI gui, final String text, final String... buttons) {
    return new DialogMessage(gui, text.trim(), Msg.YESNOCANCEL, buttons).action();
  }

  /**
   * Static yes/no dialog.
   * @param gui parent reference
   * @param text text
   * @return {@code true} if dialog was confirmed
   */
  public static boolean confirm(final GUI gui, final String text) {
    return B_YES.equals(new DialogMessage(gui, text.trim(), Msg.QUESTION).action());
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
      Util.debug(ex);
      error(gui, Util.info(H_BROWSER_ERROR_X, PUBLIC_URL));
    }
  }

  @Override
  public GUI gui() {
    return gui;
  }

  @Override
  public BaseXDialog dialog() {
    return this;
  }

  @Override
  public BaseXDialog component() {
    return this;
  }
}
