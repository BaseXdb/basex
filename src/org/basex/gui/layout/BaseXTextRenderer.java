package org.basex.gui.layout;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Point;

import org.basex.gui.GUIConstants;

/**
 * Efficient Text Editor and Renderer, supporting syntax highlighting and
 * text selections.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Christian Gruen
 */
public final class BaseXTextRenderer extends BaseXBack {
  /** Font. */
  private Font font;
  /** Font height. */
  private int fontH;
  /** Character widths. */
  private int[] fwidth = GUIConstants.mfwidth;

  /** Vertical start position. */
  private BaseXBar bar;
  /** Width of current word. */
  private int wordW;
  /** Character widths. */
  private String word;
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
  private BaseXTextTokens text;
  /** Vertical start position. */
  private BaseXSyntax syntax = BaseXSyntax.SIMPLE;
  /** Visibility of cursor. */
  private boolean cursor;

  /**
   * Constructor.
   * @param t text to be drawn
   * @param b scrollbar reference
   */
  BaseXTextRenderer(final BaseXTextTokens t, final BaseXBar b) {
    setMode(GUIConstants.Fill.NONE);
    setText(t);
    setFont(GUIConstants.dfont);
    bar = b;
  }

  /**
   * Initializes the constructor.
   * @param t text to be drawn
   */
  void setText(final BaseXTextTokens t) {
    text = t;
  }

  @Override
  public void setFont(final Font f) {
    font = f;
    off = f.getSize() >> 2;
    fontH = f.getSize() + off;
    fwidth = GUIConstants.fontWidths(f);
  }

  @Override
  public void paintComponent(final Graphics g) {
    super.paintComponent(g);
    write(g, bar.pos());
  }

  /**
   * Initializes the renderer.
   * @param g graphics reference
   * @param pos current text position
   */
  private void init(final Graphics g, final int pos) {
    if(g != null) g.setFont(font);
    syntax.init();
    text.init();
    x = off;
    y = off + fontH - pos - 2;
  }

  /**
   * Calculates the text height.
   */
  void calc() {
    w = getWidth() - (off >> 1);
    h = Integer.MAX_VALUE;
    final Graphics g = getGraphics();
    init(g, 0);
    while(more(g, false)) next();
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
    init(g, 0);
    while(more(g, false) && !text.edited()) next();
    h = hh;
    return y - fontH;
  }

  /**
   * Writes the text.
   * @param g graphics reference
   * @param pos current text position
   */
  void write(final Graphics g, final int pos) {
    BaseXLayout.antiAlias(g);

    init(g, pos);
    while(more(g, true)) write(g);

    if(cursor && text.cursor() == text.size) {
      g.setColor(Color.black);
      g.drawLine(x, y - fontH + 4, x, y + 3);
    }
  }

  /**
   * Checks if the text has more words to print.
   * @param g graphics reference
   * @param write flag for real text writing
   * @return true if the text has more words
   */
  private boolean more(final Graphics g, final boolean write) {
    // no more words found; quit
    if(!text.moreWords()) return false;

    wordW = 0;
    word = text.nextWord(write);

    // calculate word width
    for(int c = 0; c < word.length(); c++) {
      final char ch = word.charAt(c);
      // internal special codes...
      if(ch == 0x02) {
        setFont(GUIConstants.bfont);
      } else if(ch == 0x03) {
        setFont(GUIConstants.font);
      } else {
        wordW += charW(g, ch);
      }
    }

    // jump to new line
    if(x + wordW > w) {
      x = off;
      y += fontH;
    }

    // check if word has been found, and word is still visible
    return y < h;
  }


  /**
   * Finishes the current token.
   */
  private void next() {
    x += wordW;
    final int ch = word.length() > 0 ? word.charAt(0) : 0;
    if(ch == 0x0A || ch == 0x0B) {
      x = off;
      y += fontH >> (ch == 0x0A ? 0 : 1);
    }
  }
  
  /**
   * Writes the current string to the graphics reference.
   * @param g graphics reference
   */
  private void write(final Graphics g) {
    Color col = isEnabled() ? syntax.getColor(word) : Color.gray;
    if (col == Color.black) col = text.getColor();

    // return if current text is not visible.
    if(y > 0 && y < h) {
      // mark error
      if(text.error()) {
        g.setColor(GUIConstants.COLORERRHIGH);
        g.fillRect(x, y - fontH + 4, wordW, fontH);
      }
  
      // mark text
      int xx = x;
      if(text.markStart()) {
        final int p = text.pos();
        for(int c = 0; c < word.length(); c++) {
          final int cw = charW(g, word.charAt(c));
          if(text.marked()) {
            g.setColor(GUIConstants.COLORS[3]);
            g.fillRect(xx, y - fontH + 4, cw, fontH);
          }
          xx += cw;
          text.next();
        }
        text.pos(p);
      }

      // don't write whitespaces
      final int ch = word.length() > 0 ? word.charAt(0) : 0;
      if(ch > ' ') {
        g.setColor(col);
        g.drawString(word, x, y);
      } else if(ch < 0x04) {
        g.setFont(font);
      }
  
      // show cursor
      if(cursor && text.edited()) {
        xx = x;
        for(int c = 0; c < word.length(); c++) {
          if(text.cursor() == text.pos() + c) {
            g.setColor(Color.black);
            g.drawLine(xx, y - fontH + 4, xx, y + 3);
            break;
          }
          xx += charW(g, word.charAt(c));
        }
      }
    }
    next();
  }

  /**
   * Selects the text at the specified position.
   * @param pos current text position
   * @param p mouse position
   * @param finish states if selection is in progress
   */
  void select(final int pos, final Point p, final boolean finish) {
    if(!finish) text.noMark();
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
        if(!more(g, true)) {
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
      };

      if(!finish) text.startMark();
      else text.endMark();
      text.setCaret();
    }
    repaint();
  }
  
  /**
   * Returns the width of the specified character.
   * @param g graphics reference
   * @param ch character
   * @return width
   */
  private int charW(final Graphics g, final int ch) {
    if(ch == '\t') return fwidth[' '] * BaseXTextTokens.TAB;
    if(ch < 32) return 0;
    if(ch > 255 && g != null) return g.getFontMetrics().charWidth(ch);
    return fwidth[ch & 0xFF];
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
   * @param s syntax reference
   */
  void setSyntax(final BaseXSyntax s) {
    syntax = s;
  }
}
