package org.basex.gui.view.table;

import static org.basex.gui.layout.BaseXKeys.*;

import java.awt.*;
import java.awt.event.*;
import java.util.*;

import org.basex.gui.*;
import org.basex.gui.layout.*;

/**
 * This class allows simple text input for the table headers.
 *
 * @author BaseX Team 2005-21, BSD License
 * @author Christian Gruen
 */
final class TableInput {
  /** Panel reference. */
  private final BaseXPanel panel;
  /** Timer. */
  private final Timer timer;

  /** Input text. */
  private String text;
  /** Flashing cursor. */
  private boolean flashing;
  /** Flashing cursor. */
  private int pos;

  /**
   * Constructor.
   * @param panel panel reference
   * @param text text
   */
  TableInput(final BaseXPanel panel, final String text) {
    this.panel = panel;
    this.text = text;
    pos = text.length();

    timer = new Timer(true);
    timer.scheduleAtFixedRate(new TimerTask() {
      @Override
      public void run() {
        flashing ^= true;
        panel.repaint();
      }
    }, 0, 500);
  }

  /**
   * Stops the box input.
   */
  void stop() {
    timer.cancel();
    flashing = false;
    panel.repaint();
  }

  /**
   * Paints the input box.
   * @param g graphics reference
   * @param x x position
   * @param y y position
   * @param w width
   * @param h height
   */
  void paint(final Graphics g, final int x, final int y, final int w, final int h) {
    g.setColor(GUIConstants.color4);
    g.drawRect(x, y - 1, w - 1, h);
    g.setColor(GUIConstants.TEXT);
    g.setFont(GUIConstants.font);
    g.drawString(text, x + 5, y + h - 7);
    final int xx = x + BaseXLayout.width(g, text.substring(0, pos)) + 5;
    if(flashing) g.drawLine(xx, y + 1, xx, y + h - 5);
  }

  /**
   * Reacts on key codes.
   * @param e key event
   */
  void code(final KeyEvent e) {
    if(ENTER.is(e) || text == null) stop();

    flashing = true;
    if(LINESTART.is(e)) {
      pos = 0;
    } else if(LINEEND.is(e)) {
      pos = text.length();
    } else if(PREVCHAR.is(e)) {
      pos = Math.max(0, pos - 1);
    } else if(NEXTCHAR.is(e)) {
      pos = Math.min(text.length(), pos + 1);
    } else if(DELPREV.is(e)) {
      if(pos > 0) text = text.substring(0, pos - 1) + text.substring(pos--);
    } else if(DELNEXT.is(e)) {
      if(pos < text.length()) {
        text = text.substring(0, pos) + text.substring(pos + 1);
      }
    }
  }

  /**
   * Adds a character.
   * @param e key event
   * @return true if input was modified
   */
  boolean add(final KeyEvent e) {
    // backspace/delete...
    if(DELNEXT.is(e) || DELPREV.is(e)) return true;
    // skip other control chars
    final char ch = e.getKeyChar();
    if(ch < ' ') return false;
    flashing = true;
    text = text.substring(0, pos) + ch + text.substring(pos);
    ++pos;
    return true;
  }

  /**
   * Returns the entered text.
   * @return text
   */
  String text() {
    return text;
  }
}
