package org.basex.gui.text;

import static org.basex.gui.GUIConstants.*;

import java.awt.*;

import javax.swing.*;

import org.basex.gui.layout.*;

/**
 * Popup window with the signature of the function that is currently called.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
final class SignaturePopup {
  /** Popup delay (ms). */
  private static final int DELAY = 250;
  /** Horizontal margin of the signature. */
  private static final int MARGIN = 4;

  /** Text panel. */
  private final TextPanel panel;
  /** Displayed signature. */
  private final BaseXBack content = new BaseXBack().layout(new ColumnLayout());
  /** Timer for showing the popup after a delay. */
  private final Timer timer;

  /** Popup window (can be {@code null}: popup is hidden). */
  private JWindow window;
  /** Position of the popup, relative to the text panel (can be {@code null}). */
  private Point point;

  /**
   * Constructor.
   * @param panel text panel
   */
  SignaturePopup(final TextPanel panel) {
    this.panel = panel;
    content.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(lightGray),
      BaseXLayout.border(2, MARGIN, 2, MARGIN)));
    content.setBackground(backColor);
    timer = new Timer(DELAY, e -> display());
    timer.setRepeats(false);
  }

  /**
   * Indicates if the popup is visible or scheduled.
   * @return result of check
   */
  boolean active() {
    return timer.isRunning() || window != null && window.isVisible();
  }

  /**
   * Shows a signature below the current line.
   * @param signature signature
   * @param name function name, as it was specified in the call
   * @param index index of the argument at the caret ({@code -1}: no argument is emphasized)
   * @param pnt position of the popup, relative to the text panel
   */
  void show(final Signature signature, final String name, final int index, final Point pnt) {
    point = pnt;
    content.removeAll();
    // the name of the argument at the caret is emphasized
    final String args = signature.args();
    final int al = args.length();
    final int s = index == -1 ? al : signature.starts()[index];
    final int e = index == -1 ? al : signature.ends()[index];
    add(name + args.substring(0, s), false);
    add(args.substring(s, e), true);
    add(args.substring(e), false);

    // a visible popup is updated at once, a new one is displayed after a delay
    if(window != null) display();
    else timer.restart();
  }

  /**
   * Adds a part of the signature.
   * @param string string
   * @param bold emphasize the string
   */
  private void add(final String string, final boolean bold) {
    if(string.isEmpty()) return;
    // the signature is rendered like the candidates of the code completion
    final JLabel label = new JLabel(string);
    final Font f = label.getFont().deriveFont((float) popupFontSize);
    label.setFont(bold ? f.deriveFont(Font.BOLD) : f);
    label.setForeground(textColor);
    content.add(label);
  }

  /**
   * Displays the popup at the position of the last request.
   */
  private void display() {
    // the panel may have been hidden before the popup was displayed
    if(!panel.isShowing()) return;

    if(window == null) {
      // the popup must not take away the focus from the text panel
      window = new JWindow(SwingUtilities.getWindowAncestor(panel));
      window.setFocusableWindowState(false);
      window.add(content);
    }
    window.pack();

    final Point screen = panel.getLocationOnScreen();
    window.setLocation(screen.x + point.x, screen.y + point.y);
    window.setVisible(true);
  }

  /**
   * Hides the popup, discards its window and stops the timer.
   */
  void hide() {
    timer.stop();
    if(window == null) return;
    window.dispose();
    window = null;
  }
}
