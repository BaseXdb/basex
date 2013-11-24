package org.basex.gui.editor;

import java.awt.*;
import org.basex.gui.*;
import org.basex.gui.GUIConstants.Fill;
import org.basex.gui.editor.Editor.SearchDir;
import org.basex.gui.layout.*;
import org.basex.util.*;
import org.basex.util.list.*;

/**
 * Efficient Text Editor and Renderer, supporting syntax highlighting and
 * text selections.
 *
 * @author BaseX Team 2005-13, BSD License
 * @author Christian Gruen
 */
final class Renderer extends BaseXBack {
  /** Vertical start position. */
  private final BaseXBar bar;
  /** Vertical line number bar. */
  private final LineNumberBar lineNumberBar;

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
  /** Current link. */
  private boolean link;

  /** Width of current word. */
  private int wordW;

  /** Border offset. */
  private int off;
  /** Offset from the line number bar. */
  private int lineNumberOffset;
  /** Current x coordinate. */
  private int x;
  /** Current y coordinate. */
  private int y;
  /** Current width. */
  private int w;
  /** Current height. */
  private int h;

  /** Current brackets. */
  private final IntList pars = new IntList();

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
   * @param l line number bar
   */
  Renderer(final EditorText t, final BaseXBar b, final LineNumberBar l) {
    mode(Fill.NONE);
    text = t;
    bar = b;
    lineNumberBar = l;
  }

  @Override
  public void setFont(final Font f) {
    dfont = f;
    bfont = f.deriveFont(Font.BOLD);
    font(f);
  }

  @Override
  public void paintComponent(final Graphics g) {
    if(lineNumberBar != null) {
      // the line number bar goes over the text, so we need to offset the text
      lineNumberBar.setLastLineNumber(text.lineNumbers());
      lineNumberOffset = lineNumberBar.getWidth() - 8;
    }

    super.paintComponent(g);

    pars.reset();
    init(g, bar.pos());
    if(lineNumberBar != null) {
      lineNumberBar.resetLineNumbers();
      // there is always at least one line in the editor
      lineNumberBar.addRenderedTextLine(y);
    }
    int oldY;
    while(more(g)) {
      oldY = y;
      write(g);
      if(lineNumberBar != null && y != oldY) lineNumberBar.addRenderedTextLine(y);
    }
    wordW = 0;
    final int s = text.size();
    if(cursor && s == text.getCaret()) drawCursor(g, x);
    if(s == text.error()) drawError(g);

    if(lineNumberBar != null) lineNumberBar.repaint();
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
    init(g);
    boolean more = true;
    int line = 1;
    int col = 1;
    while(more(g)) {
      final int p = text.pos();
      while(text.more()) {
        more = text.pos() < text.getCaret();
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
    link = false;
    x = off + lineNumberOffset;
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
      if(ch == TokenBuilder.BOLD) {
        font(bfont);
      } else if(ch == TokenBuilder.NORM) {
        font(dfont);
      } else if(ch == TokenBuilder.ULINE) {
        link ^= true;
      } else {
        ww += charW(g, ch);
      }
    }
    text.pos(p);

    // jump to new line
    if(x + ww > w) {
      x = off + lineNumberOffset;
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
      x = off + lineNumberOffset;
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
    // choose color for enabled text, depending on highlighting, link, or current syntax
    color = isEnabled() ?
      high ? GUIConstants.GREEN : link ? GUIConstants.color4 : syntax.getColor(text) :
      Color.gray;
    high = false;

    // retrieve first character of current token
    final int ch = text.curr();
    if(ch == TokenBuilder.MARK) high = true;

    final int cp = text.pos();
    final int cc = text.getCaret();
    if(y > 0 && y < h) {
      // mark selected text
      if(text.selectStart()) {
        int xx = x;
        while(!text.inSelect() && text.more()) xx += charW(g, text.next());
        int cw = 0;
        while(text.inSelect() && text.more()) cw += charW(g, text.next());
        g.setColor(GUIConstants.color(3));
        g.fillRect(xx, y - fontH * 4 / 5, cw, fontH);
        text.pos(cp);
      }

      // mark found text
      int xx = x;
      while(text.more() && text.searchStart()) {
        while(!text.inSearch() && text.more()) xx += charW(g, text.next());
        int cw = 0;
        while(text.inSearch() && text.more()) cw += charW(g, text.next());
        g.setColor(GUIConstants.color2A);
        g.fillRect(xx, y - fontH * 4 / 5, cw, fontH);
        xx += cw;
      }
      text.pos(cp);

      // retrieve first character of current token
      if(text.erroneous()) drawError(g);

      // don't write whitespaces
      if(ch == '\u00a0') {
        final int s = fontH / 12;
        g.setColor(GUIConstants.GRAY);
        g.fillRect(x + (wordW >> 1), y - fontH * 3 / 10, s, s);
      } else if(ch == '\t') {
        final int yy = y - fontH * 3 / 10;
        final int s = 1 + fontH / 12;
        final int xe = x + charW(g, '\t') - s;
        final int as = s * 2 - 1;
        g.setColor(GUIConstants.GRAY);
        g.drawLine(x + s, yy, xe, yy);
        g.drawLine(xe - as, yy - as, xe, yy);
        g.drawLine(xe - as, yy + as, xe, yy);
      } else if(ch >= ' ') {
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
        if(ch != ' ') g.drawString(n, x, y);
      } else if(ch <= TokenBuilder.ULINE) {
        g.setFont(font);
      }

      // underline linked text
      if(link) g.drawLine(x, y + 1, x + wordW, y + 1);

      // show cursor
      if(cursor && text.edited()) {
        xx = x;
        while(text.more()) {
          if(cc == text.pos()) {
            drawCursor(g, xx);
            break;
          }
          xx += charW(g, text.next());
        }
        text.pos(cp);
      }
    }

    // handle matching parentheses
    if(ch == '(' || ch == '[' || ch == '{') {
      pars.add(x);
      pars.add(y);
      pars.add(cp);
      pars.add(ch);
    } else if((ch == ')' || ch == ']' || ch == '}') && !pars.isEmpty()) {
      final int open = ch == ')' ? '(' : ch == ']' ? '[' : '{';
      if(pars.peek() == open) {
        pars.pop();
        final int cr = pars.pop();
        final int yy = pars.pop();
        final int xx = pars.pop();
        if(cc == cp || cc == cr) {
          g.setColor(GUIConstants.color3);
          g.drawRect(xx, yy - fontH * 4 / 5, charW(g, open), fontH);
          g.drawRect(x, y - fontH * 4 / 5, charW(g, ch), fontH);
        }
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
   * Draws an error marker.
   * @param g graphics reference
   */
  private void drawError(final Graphics g) {
    final int ww = wordW == 0 ? charW(g, ' ') : wordW;
    final int s = Math.max(1, fontH / 8);
    g.setColor(GUIConstants.LRED);
    g.fillRect(x, y + 2, ww, s);
    g.setColor(GUIConstants.RED);
    for(int xp = x; xp < x + ww; xp++) {
      if((xp & 1) == 0) g.drawLine(xp, y + 2, xp, y + s + 1);
    }
  }

  /**
   * Selects the text at the specified position.
   * @param pos current text position
   * @param p mouse position
   * @return {@code true} if mouse is placed over a link
   */
  boolean link(final int pos, final Point p) {
    final int xx = p.x;
    final int yy = p.y - fontH / 5;

    final Graphics g = getGraphics();
    init(g, pos);
    if(yy > y - fontH) {
      int s = text.pos();
      while(true) {
        // end of line
        if(xx > x && yy < y - fontH) {
          text.pos(s);
          break;
        }
        // end of text - skip last characters
        if(!more(g)) {
          while(text.more()) text.next();
          break;
        }
        // beginning of line
        if(xx <= x && yy < y) break;
        // middle of line
        if(xx > x && xx <= x + wordW && yy > y - fontH && yy <= y) {
          while(text.more()) {
            final int ww = charW(g, text.curr());
            if(xx < x + ww) break;
            x += ww;
            text.next();
          }
          break;
        }
        s = text.pos();
        next();
      }
    }
    return link;
  }

  /**
   * Selects the text at the specified position.
   *
   * @param pos current text position
   * @param p mouse position
   * @param start states if selection has just been started
   */
  void select(final int pos, final Point p, final boolean start) {
    if(start) text.noSelect();
    link(pos, p);

    if(start) text.startSelect();
    else text.extendSelect();
    text.setCaret();
    repaint();
  }

  /**
   * Retrieves the current link found via {@link #link(int, Point)} or
   * {@link #select(int, Point, boolean)}.
   * @return clicked link
   */
  String link() {
    // find beginning and end of link
    final int s = text.size();
    final byte[] txt = text.text();
    int ls = text.pos(), le = ls;
    while(ls > 0 && txt[ls - 1] != TokenBuilder.ULINE) ls--;
    while(le < s && txt[le] != TokenBuilder.ULINE) le++;
    return Token.string(txt, ls, le - ls);
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
   * Sets the cursor flag and repaints the panel.
   * @param c cursor flag
   */
  void cursor(final boolean c) {
    cursor = c;
    repaint();
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
