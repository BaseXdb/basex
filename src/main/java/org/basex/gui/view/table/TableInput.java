package org.basex.gui.view.table;

import static org.basex.gui.layout.BaseXKeys.*;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.event.KeyEvent;
import org.basex.gui.GUIConstants;
import org.basex.gui.layout.BaseXLayout;
import org.basex.gui.layout.BaseXPanel;
import org.basex.util.Performance;

/**
 * This class allows simple text input for the table headers.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Christian Gruen
 */
final class TableInput implements Runnable {
  /** Input text. */
  String text;
  /** Panel reference. */
  private final BaseXPanel panel;
  /** Flashing cursor. */
  private boolean flashing;
  /** Flashing cursor. */
  private int pos;

  /**
   * Constructor.
   * @param p panel reference
   * @param t text
   */
  TableInput(final BaseXPanel p, final String t) {
    panel = p;
    text = t;
    pos = text.length();
    new Thread(this).start();
  }

  /**
   * Stops the box input.
   */
  void stop() {
    text = null;
  }

  /**
   * Paints the input box.
   * @param g graphics reference
   * @param x x position
   * @param y y position
   * @param w width
   * @param h height
   */
  void paint(final Graphics g, final int x, final int y, final int w,
      final int h) {
    g.setColor(GUIConstants.color4);
    g.drawRect(x, y - 1, w - 1, h);
    g.setColor(Color.black);
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
    } else if(PREV.is(e)) {
      pos = Math.max(0, pos - 1);
    } else if(NEXT.is(e)) {
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

  @Override
  public void run() {
    while(text != null) {
      flashing ^= true;
      panel.repaint();
      Performance.sleep(500);
    }
    flashing = false;
    panel.repaint();
  }
}
