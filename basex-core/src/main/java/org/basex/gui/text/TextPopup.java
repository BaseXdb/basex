package org.basex.gui.text;

import java.awt.*;

import javax.swing.*;

/**
 * Popup window that is displayed below the caret of a text panel.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
abstract class TextPopup {
  /** Popup delay (ms). */
  private static final int DELAY = 250;
  /** Horizontal margin of the popup content. */
  static final int MARGIN = 4;

  /** Text panel. */
  final TextPanel panel;
  /** Timer for showing the popup after a delay. */
  private final Timer timer;
  /** Popup window (can be {@code null}: popup is hidden). */
  private JWindow window;

  /**
   * Constructor.
   * @param panel text panel
   */
  TextPopup(final TextPanel panel) {
    this.panel = panel;
    timer = new Timer(DELAY, e -> timeout());
    timer.setRepeats(false);
  }

  /**
   * Called when the timer has expired.
   */
  abstract void timeout();

  /**
   * Indicates if the popup is visible.
   * @return result of check
   */
  final boolean visible() {
    return window != null && window.isVisible();
  }

  /**
   * Indicates if the popup is visible or scheduled.
   * @return result of check
   */
  final boolean active() {
    return timer.isRunning() || visible();
  }

  /**
   * Restarts the timer for showing the popup after a delay.
   */
  final void schedule() {
    timer.restart();
  }

  /**
   * Adopts the size of the popup window to its content.
   */
  final void pack() {
    if(window != null) window.pack();
  }

  /**
   * Returns the content of a new popup window.
   * @return content
   */
  abstract Component content();

  /**
   * Shows the popup window at the specified position, creating it if necessary.
   * @param point position, relative to the text panel
   */
  final void open(final Point point) {
    // the panel may have been hidden before the popup was displayed
    if(!panel.isShowing()) return;

    if(window == null) {
      // the popup must not take away the focus from the text panel
      window = new JWindow(SwingUtilities.getWindowAncestor(panel));
      window.setFocusableWindowState(false);
      window.add(content());
    }
    window.pack();
    final Point screen = panel.getLocationOnScreen();
    window.setLocation(screen.x + point.x, screen.y + point.y);
    window.setVisible(true);
  }

  /**
   * Hides the popup, discards its window and stops the timer.
   */
  final void hide() {
    timer.stop();
    if(window == null) return;
    window.dispose();
    window = null;
  }
}
