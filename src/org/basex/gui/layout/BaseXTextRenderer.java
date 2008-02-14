package org.basex.gui.layout;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Point;
import javax.swing.UIManager;
import org.basex.gui.GUIConstants;
import org.basex.gui.GUIConstants.FILL;

/**
 * Efficient Text Editor and Renderer, supporting syntax highlighting and
 * text selections.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Christian Gruen
 */
public final class BaseXTextRenderer extends BaseXBack {
  /** Selection background. */
  private static final Color COLORSELBACK =
    UIManager.getColor("TextPane.selectionBackground");
  /** Selection foreground. */
  private static final Color COLORSELFORE =
    UIManager.getColor("TextPane.selectionForeground");

  /** Font height. */
  private int fontH;
  /** Character widths. */
  private int[] fwidth = GUIConstants.mfwidth;

  /** Vertical start position. */
  private BaseXBar bar;
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
  private BaseXTextTokens text;
  /** Vertical start position. */
  private BaseXSyntax syntax = new BaseXSyntax();
  /** Visibility of cursor. */
  private boolean cursor;

  /**
   * Constructor.
   * @param t text to be drawn
   * @param b scrollbar reference
   */
  BaseXTextRenderer(final BaseXTextTokens t, final BaseXBar b) {
    setMode(FILL.NONE);
    setCursor(GUIConstants.CURSORTEXT);
    setText(t);
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
    super.setFont(f);
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
    if(g != null) g.setFont(getFont());
    syntax.init();
    text.init();
    x = off;
    y = off + fontH - pos - 2;
  }

  /**
   * Calculates the text height.
   */
  void calc() {
    w = getWidth() - off / 2;
    h = Integer.MAX_VALUE;
    final Graphics g = getGraphics();
    init(g, 0);
    while(more(g)) next();
    h = getHeight() + fontH;
    bar.height(y + off);
  }

  /**
   * Returns the current vertical cursor position.
   * @return new position
   */
  int cursorY() {
    int hh = h;
    h = Integer.MAX_VALUE;
    final Graphics g = getGraphics();
    init(g, 0);
    while(more(g) && !text.edited()) next();
    next();
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
    while(more(g)) write(g);

    if(cursor && text.cursor() == text.size) {
      g.setColor(Color.black);
      g.drawLine(x, y - fontH + 3, x, y + 1);
    }
  }

  /**
   * Checks if the text has more words to print.
   * @param g graphics reference
   * @return true if the text has more words
   */
  private boolean more(final Graphics g) {
    final boolean more = text.moreWords();

    // calculate word width
    wordW = 0;
    final int ps = text.pos();
    while(text.more()) wordW += charW(g, text.next());
    text.pos(ps);
    if(x + wordW > w) { x = off; y += fontH; }

    return more && (y < h || text.start() != -1 && text.pos() < text.start());
  }


  /**
   * Moves to the next token.
   */
  private void next() {
    // add word width and check newline at beginning/end of text
    x += wordW;
    final boolean nl = text.pos() != text.size && text.curr() == '\n';
    if(nl) { x = off; y += fontH; }
  }
  
  /**
   * Writes the current string to the graphics reference.
   * @param g graphics reference
   */
  private void write(final Graphics g) {
    // choose color (later: use variable syntax highlighter)
    final int ch = text.curr();
    Color color = syntax.getColor(text);

    // return if current text is not visible.
    if(y > 0 && y < h) {
      // mark error
      if(text.error()) {
        g.setColor(GUIConstants.COLORMARK);
        g.fillRect(x, y - fontH + 3, wordW, fontH);
      }
  
      // mark text
      int ps = text.pos();
      int xx = x;
      // whitespace flag
      final boolean vis = ch < 0 || ch > ' ';

      if(text.markStart()) {
        while(text.more()) {
          final boolean m = text.marked();
          final int c = text.next();
          final int cw = charW(g, c);
          if(m) {
            g.setColor(COLORSELBACK);
            g.fillRect(xx, y - fontH + 3, cw, fontH);
            g.setColor(COLORSELFORE);
          } else {
            g.setColor(color);
          }
          if(c < 0 || c > ' ') {
            g.drawChars(new char[] { (char) c }, 0, 1, xx, y);
          }
          xx += cw;
        }
        text.pos(ps);
      } else if(vis) {
        g.setColor(color);
        g.drawString(text.nextWord(), x, y);
      }
  
      // show cursor
      if(cursor && text.edited()) {
        g.setColor(Color.black);
        xx = x;
        ps = text.pos();
        while(text.more()) {
          if(text.cursor() == text.pos()) {
            g.drawLine(xx, y - fontH + 3, xx, y + 2);
            break;
          }
          xx += charW(g, text.next());
        }
        text.pos(ps);
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
    if(p.y < y - fontH) return;

    while(more(g)) {
      if(p.x > x && p.x <= x + wordW && p.y > y - fontH && p.y <= y ||
          p.x > x && p.y < y - fontH || p.x <= x && p.y < y) {
        while(text.more()) {
          final int ww = charW(g, text.curr());
          if(p.x < x + ww) break;
          x += ww;
          text.next();
        }
        break;
      }
      next();
    };
    if(!finish) text.startMark();
    else text.endMark();
    text.setCursor();
  }
  
  /**
   * Returns the character width.
   * @param g graphics reference
   * @param ch character
   * @return width
   */
  private int charW(final Graphics g, final int ch) {
    return ch > 255 ? g.getFontMetrics().charWidth(ch) :
      ch == '\t' ? fwidth[' '] * BaseXTextTokens.TAB : fwidth[ch & 0xFF];
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
