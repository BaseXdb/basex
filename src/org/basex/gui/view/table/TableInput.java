package org.basex.gui.view.table;

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
 * @author Workgroup DBIS, University of Konstanz 2005-09, ISC License
 * @author Christian Gruen
 */
final class TableInput implements Runnable {
  /** Panel reference. */
  final BaseXPanel panel;
  /** Flashing cursor. */
  boolean flashing;
  /** Input text. */
  String text;
  /** Flashing cursor. */
  int pos;

  /**
   * Constructor.
   * @param p panel reference
   * @param t text
   */
  public TableInput(final BaseXPanel p, final String t) {
    panel = p;
    text = t;
    pos = text.length();
    new Thread(this).start();
  }

  /**
   * Stops the box input.
   */
  public void stop() {
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
  public void paint(final Graphics g, final int x, final int y, final int w,
      final int h) {
    g.setColor(GUIConstants.color6);
    g.drawRect(x, y - 1, w - 1, h);
    g.setColor(Color.black);
    g.setFont(GUIConstants.font);
    g.drawString(text, x + 5, y + h - 7);
    final int xx = x + BaseXLayout.width(g, text.substring(0, pos)) + 5;
    if(flashing) g.drawLine(xx, y + 1, xx, y + h - 5);
  }

  /**
   * Adds a character.
   * @param c character to be added
   * @return true if input was modified
   */
  public boolean add(final char c) {
    // backspace/delete...
    if(c == 8 || c == 127) return true;
    // skip other control chars
    if(c < ' ') return false;
    flashing = true;
    text = text.substring(0, pos) + c + text.substring(pos);
    pos++;
    return true;
  }

  /**
   * Reacts on key codes.
   * @param c code to be evaluated
   */
  public void code(final int c) {
    if(c == KeyEvent.VK_ENTER || text == null) stop();

    flashing = true;
    if(c == KeyEvent.VK_HOME) {
      pos = 0;
    } else if(c == KeyEvent.VK_END) {
      pos = text.length();
    } else if(c == KeyEvent.VK_LEFT) {
      pos = Math.max(0, pos - 1);
    } else if(c == KeyEvent.VK_RIGHT) {
      pos = Math.min(text.length(), pos + 1);
    } else if(c == KeyEvent.VK_BACK_SPACE) {
      if(pos > 0) text = text.substring(0, pos - 1) + text.substring(pos--);
    } else if(c == KeyEvent.VK_DELETE) {
      if(pos < text.length()) {
        text = text.substring(0, pos) + text.substring(pos + 1);
      }
    }
  }

  /**
   * Thread for cursor flashing.
   */
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
