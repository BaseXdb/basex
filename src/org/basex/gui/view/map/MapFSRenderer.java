package org.basex.gui.view.map;

import static org.basex.gui.GUIConstants.*;
import static org.basex.util.Token.*;

import java.awt.Color;
import java.awt.Graphics;
import java.util.ArrayList;

import org.basex.core.Prop;
import org.basex.gui.layout.BaseXLayout;
import org.basex.util.Tokenizer;

/**
 * This class assembles utility methods for painting filesystem specific
 * rectangle contents.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-09, ISC License
 * @author Bastian Lemke
 */
final class MapFSRenderer {
  
  // [BL] filesystem specific thumbnails

  /** Private constructor. */
  private MapFSRenderer() { }

  /**
   * Calculates the height of the specified text.
   * @param g graphics reference
   * @param r rectangle
   * @param s text to be drawn
   * @param fs font size
   * @return last height that was occupied
   */
  static int calcHeight(final Graphics g, final MapRect r, final byte[] s,
      final int fs) {
    return drawText(g, r, s, false, fs);
  }

  /**
   * Draws a text.
   * @param g graphics reference
   * @param r rectangle
   * @param s text to be drawn
   * @param fs font size
   * @return last height that was occupied
   */
  static int drawText(final Graphics g, final MapRect r, final byte[] s,
      final int fs) {
    return drawText(g, r, s, true, fs);
  }
  
  /**
   * Wraps the given lines to fit into a rectangle with the given width.
   * @param g graphics reference
   * @param text text to wrap
   * @param width width of the rectangle
   * @return wrapped text
   */
  private static String[] wrapText(final Graphics g, final String[] text,
      final int width) {
    final int[] cw = fontWidths(g.getFont());
    final ArrayList<String> wrappedLines = new ArrayList<String>(text.length);
    
    for(final String line : text) {
      if(line.length() == 0) wrappedLines.add("");
      int lineWidth = 0;
      int linePos = 0;
      int startIndex = 0;
      for(final char ch : line.toCharArray()) {
        final int charWidth = BaseXLayout.width(g, cw, ch);
        if(lineWidth + charWidth > width) {
          wrappedLines.add(line.substring(startIndex, linePos));
          startIndex = linePos;
          lineWidth = 0;
        }
        lineWidth += charWidth;
        linePos++;
      }
      if(startIndex < linePos - 1)
        wrappedLines.add(line.substring(startIndex, linePos));
    }
    return wrappedLines.toArray(new String[wrappedLines.size()]);
  }

  /**
   * Draws a text.
   * @param g graphics reference
   * @param r rectangle
   * @param s text to be drawn
   * @param draw draw text (otherwise: just calculate space)
   * @param fs font size
   * @return height of the text
   */
  private static int drawText(final Graphics g, final MapRect r,
      final byte[] s, final boolean draw, final int fs) {

    final int fh = (int) (1.2 * fs);
    final Color textc = g.getColor();
    int yy = r.y + fh;
    
    // [BL] find more efficient way to get and highlight the full-text strings
    final ArrayList<String> ftStrings = new ArrayList<String>();
    if(r.pos != null) {
      final Tokenizer tok = new Tokenizer(s, null);
      int count = 0;
      while(tok.more()) {
        if(r.pos.contains(count++)) ftStrings.add(string(tok.orig()));
      }
    }

    final String[] lines = wrapText(g, string(s).split(Prop.NL), r.w);
    for(final String line : lines) {
      if(draw) {
        boolean ft = false;
        if(ftStrings.size() != 0) {
          for(final String str : 
              ftStrings.toArray(new String[ftStrings.size()])) {
            if(line.contains(str)) {
              ft = true;
              break;
            }
          }
        }
        // [BL] highlight only the word, not the complete line
        g.setColor(ft ? COLORFT : textc);
        g.drawString(line, r.x, yy);
      }
      yy += fh;
    }
    return yy - r.y;
  }
}
