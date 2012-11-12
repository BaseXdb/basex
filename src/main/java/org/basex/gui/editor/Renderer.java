package org.basex.gui.editor;

import java.awt.*;

import org.basex.gui.*;
import org.basex.gui.GUIConstants.Fill;
import org.basex.gui.editor.Editor.SearchDir;
import org.basex.gui.layout.*;
import org.basex.util.*;

/**
 * Efficient Text Editor and Renderer, supporting syntax highlighting and
 * text selections.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Christian Gruen
 */
public final class Renderer extends BaseXBack {
  /** Vertical start position. */
  private final BaseXBar bar;

  /** Font. */
  private Font font;
  /** Default font. */
  private Font dfont;
  /** Bold font. */
  private Font bfont;
  /** Font height. */
  private int fontH;
  /** Character widths. */
  private int[] fwidth = GUIConstants.mfwidth;
  /** Color. */
  private Color color;
  /** Color highlighting flag. */
  private boolean high;

  /** Width of current word. */
  private int wordW;

  /** Border offset. */
  private int off;
  /** Current x coordinate. */
  private int x;
  /** Current y coordinate. */
  private int y;
  /** Current width. */
  private int w;
  /** Current height. */
  private int h;

  /** Text array to be written. */
  private final EditorText text;
  /** Vertical start position. */
  private transient Syntax syntax = Syntax.SIMPLE;
  /** Visibility of cursor. */
  private boolean cursor;

  /**
   * Constructor.
   * @param t text to be drawn
   * @param b scrollbar reference
   */
  Renderer(final EditorText t, final BaseXBar b) {
    mode(Fill.NONE);
    text = t;
    bar = b;
  }

  @Override
  public void setFont(final Font f) {
    dfont = f;
    bfont = f.deriveFont(Font.BOLD);
    font(f);
  }

  @Override
  public void paintComponent(final Graphics g) {
    super.paintComponent(g);
    init(g, bar.pos());
    while(more(g)) write(g);
    wordW = 0;
    final int s = text.size();
    if(cursor && s == text.cursor()) drawCursor(g, x);
    if(s == text.error()) drawError(g);
  }

  /**
   * Sets a new search context.
   * @param sc new search context
   */
  void search(final SearchContext sc) {
    text.search(sc);
  }

  /**
   * Replaces the text.
   * @param rc replace context
   * @return selection offsets
   */
  int[] replace(final ReplaceContext rc) {
    return text.replace(rc);
  }

  /**
   * Jumps to a search string.
   * @param dir search direction
   * @param select select hit
   * @return new vertical position, or {@code -1}
   */
  int jump(final SearchDir dir, final boolean select) {
    final int pos = text.jump(dir, select);
    if(pos == -1) return -1;

    final int hh = h;
    h = Integer.MAX_VALUE;
    final Graphics g = getGraphics();
    for(init(g); more(g) && text.pos() < pos; next());
    h = hh;
    return y;
  }

  /**
   * Returns the cursor coordinates.
   * @return line/column
   */
  int[] pos() {
    final int hh = h;
    h = Integer.MAX_VALUE;
    final Graphics g = getGraphics();
    int col = 1;
    int line = 1;
    init(g);
    boolean more = true;
    while(more(g)) {
      final int p = text.pos();
      while(text.more()) {
        more = text.pos() < text.cursor();
        if(!more) break;
        text.next();
        col++;
      }
      if(!more) break;
      text.pos(p);
      if(next()) {
        line++;
        col = 1;
      }
    }
    h = hh;
    return new int[] { line, col };
  }

  /**
   * Sets the current font.
   * @param f font
   */
  private void font(final Font f) {
    font = f;
    off = f.getSize() + 1 >> 2;
    fontH = f.getSize() + off;
    fwidth = GUIConstants.fontWidths(f);
  }

  @Override
  public Dimension getPreferredSize() {
    final Graphics g = getGraphics();
    w = Integer.MAX_VALUE;
    h = Integer.MAX_VALUE;
    init(g);
    int max = 0;
    while(more(g)) {
      if(text.curr() == 0x0A) max = Math.max(x, max);
      next();
    }
    return new Dimension(Math.max(x, max) + fwidth[' '], y + fontH);
  }

  /**
   * Initializes the renderer.
   * @param g graphics reference
   */
  private void init(final Graphics g) {
    init(g, 0);
  }

  /**
   * Initializes the renderer.
   * @param g graphics reference
   * @param pos current text position
   */
  private void init(final Graphics g, final int pos) {
    font = dfont;
    color = Color.black;
    syntax.init();
    text.init();
    x = off;
    y = off + fontH - pos - 2;
    if(g != null) g.setFont(font);
  }

  /**
   * Calculates the text height.
   */
  void calc() {
    w = getWidth() - (off >> 1);
    h = Integer.MAX_VALUE;
    final Graphics g = getGraphics();
    init(g);
    while(more(g)) next();
    h = getHeight() + fontH;
    bar.height(y + off);
  }

  /**
   * Returns the current vertical cursor position.
   * @return new position
   */
  int cursorY() {
    final int hh = h;
    h = Integer.MAX_VALUE;
    final Graphics g = getGraphics();
    init(g);
    while(more(g) && !text.edited()) next();
    h = hh;
    return y - fontH;
  }

  /**
   * Checks if the text has more words to print.
   * @param g graphics reference
   * @return true if the text has more words
   */
  private boolean more(final Graphics g) {
    // no more words found; quit
    if(!text.moreTokens()) return false;

    // calculate word width
    int ww = 0;
    final int p = text.pos();
    while(text.more()) {
      final int ch = text.next();
      // internal special codes...
      if(ch == 0x02) {
        font(bfont);
      } else if(ch == 0x03) {
        font(dfont);
      } else {
        ww += charW(g, ch);
      }
    }
    text.pos(p);

    // jump to new line
    if(x + ww > w) {
      x = off;
      y += fontH;
    }
    wordW = ww;

    // check if word has been found, and word is still visible
    return y < h;
  }

  /**
   * Finishes the current token.
   * @return true for new line
   */
  private boolean next() {
    final int ch = text.curr();
    if(ch == TokenBuilder.NLINE || ch == TokenBuilder.HLINE) {
      x = off;
      y += fontH >> (ch == TokenBuilder.NLINE ? 0 : 1);
      return true;
    }
    x += wordW;
    return false;
  }

  /**
   * Writes the current string to the graphics reference.
   * @param g graphics reference
   */
  private void write(final Graphics g) {
    if(high) {
      high = false;
    } else {
      color = isEnabled() ? syntax.getColor(text) : Color.gray;
    }

    final int ch = text.curr();
    if(y > 0 && y < h) {
      if(ch == TokenBuilder.MARK) {
        color = GUIConstants.GREEN;
        high = true;
      }

      // mark selected text
      final int cp = text.pos();
      if(text.selectStart()) {
        int xx = x, cw = 0;
        while(!text.inSelect() && text.more()) xx += charW(g, text.next());
        while(text.inSelect() && text.more()) cw += charW(g, text.next());
        g.setColor(GUIConstants.color(3));
        g.fillRect(xx, y - fontH * 4 / 5, cw, fontH);
        text.pos(cp);
      }

      // mark found text
      int xx = x;
      while(text.more() && text.searchStart()) {
        int cw = 0;
        while(!text.inSearch() && text.more()) xx += charW(g, text.next());
        while(text.inSearch() && text.more()) cw += charW(g, text.next());
        g.setColor(GUIConstants.color2A);
        g.fillRect(xx, y - fontH * 4 / 5, cw, fontH);
        xx += cw;
      }
      text.pos(cp);

      if(text.erroneous()) drawError(g);

      // don't write whitespaces
      if(ch > ' ') {
        g.setColor(color);
        String n = text.nextString();
        int ww = w - x;
        if(x + wordW > ww) {
          // shorten string if it cannot be completely shown (saves memory)
          int c = 0;
          for(final int nl = n.length(); c < nl && ww > 0; c++) {
            ww -= charW(g, n.charAt(c));
          }
          n = n.substring(0, c);
        }
        g.drawString(n, x, y);
      } else if(ch <= TokenBuilder.MARK) {
        g.setFont(font);
      }

      // show cursor
      if(cursor && text.edited()) {
        xx = x;
        while(text.more()) {
          if(text.cursor() == text.pos()) {
            drawCursor(g, xx);
            break;
          }
          xx += charW(g, text.next());
        }
        text.pos(cp);
      }
    }
    next();
  }

  /**
   * Paints the text cursor.
   * @param g graphics reference
   * @param xx x position
   */
  private void drawCursor(final Graphics g, final int xx) {
    g.setColor(GUIConstants.DGRAY);
    g.fillRect(xx, y - fontH * 4 / 5, 2, fontH);
  }

  /**
   * Paints the error marker.
   * @param g graphics reference
   */
  private void drawError(final Graphics g) {
    final int ww = wordW != 0 ? wordW : charW(g, ' ');
    final int s = Math.max(1, fontH / 8);
    g.setColor(GUIConstants.LRED);
    g.fillRect(x, y + 2, ww, s);
    g.setColor(GUIConstants.RED);
    for(int xp = x; xp < x + ww; xp++) {
      if((xp & 1) == 0) g.drawLine(xp, y + 2, xp, y + s + 1);
    }
  }

  /**
   * Finishes the selection.
   */
  void stopSelect() {
    text.checkSelect();
  }

  /**
   * Selects the text at the specified position.
   * @param pos current text position
   * @param p mouse position
   * @param finish states if selection is in progress
   */
  void select(final int pos, final Point p, final boolean finish) {
    if(!finish) text.noSelect();
    p.y -= 3;

    final Graphics g = getGraphics();
    init(g, pos);
    if(p.y > y - fontH) {
      int s = text.pos();
      while(true) {
        // end of line
        if(p.x > x && p.y < y - fontH) {
          text.pos(s);
          break;
        }
        // end of text - skip last characters
        if(!more(g)) {
          while(text.more()) text.next();
          break;
        }
        // beginning of line
        if(p.x <= x && p.y < y) break;
        // middle of line
        if(p.x > x && p.x <= x + wordW && p.y > y - fontH && p.y <= y) {
          while(text.more()) {
            final int ww = charW(g, text.curr());
            if(p.x < x + ww) break;
            x += ww;
            text.next();
          }
          break;
        }
        s = text.pos();
        next();
      }

      if(!finish) text.startSelect();
      else text.extendSelect();
      text.setCursor();
    }
    repaint();
  }

  /**
   * Returns the width of the specified codepoint.
   * @param g graphics reference
   * @param cp character
   * @return width
   */
  private int charW(final Graphics g, final int cp) {
    return cp < ' ' || g == null ?  cp == '\t' ?
      fwidth[' '] * EditorText.TAB : 0 : cp < 256 ? fwidth[cp] :
      cp >= 0xD800 && cp <= 0xDC00 ? 0 : g.getFontMetrics().charWidth(cp);
  }

  /**
   * Returns the font height.
   * @return font height
   */
  int fontH() {
    return fontH;
  }

  /**
   * Sets the cursor flag.
   * @param c cursor flag
   */
  void cursor(final boolean c) {
    cursor = c;
  }

  /**
   * Returns the cursor flag.
   * @return cursor flag
   */
  boolean cursor() {
    return cursor;
  }

  /**
   * Sets a syntax highlighter.
   * @param s syntax highlighter
   */
  void setSyntax(final Syntax s) {
    syntax = s;
  }

  /**
   * Returns the syntax highlighter.
   * @return syntax highlighter
   */
  Syntax getSyntax() {
    return syntax;
  }
}
