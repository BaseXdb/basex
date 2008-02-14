package org.basex.gui.view.text;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import org.basex.gui.GUIConstants;
import org.basex.gui.GUIProp;
import org.basex.gui.layout.BaseXLayout;
import org.basex.util.Token;
import org.basex.util.TokenIterator;

/**
 * Prettttty Efficient Text Renderer, supporting highlighting and
 * copy and paste operations.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Christian Gruen
 */
final class TextRenderer {
  /** Current text color. */
  private Color color;
  /** Secondary text color. */
  private Color color2;
  /** Graphics reference. */
  private Graphics2D g;

  /** Font. */
  private Font font = GUIConstants.mfont;
  /** Font height. */
  private int fontH = font.getSize();
  /** Character widths. */
  private int[] fwidth = GUIConstants.mfwidth;

  /** Border offset. */
  private int offset;
  /** Current word size. */
  private int wordW;
  /** Current x coordinate. */
  private int x;
  /** Current y coordinate. */
  private int y;
  /** Current width. */
  private int w;
  /** Current height. */
  private int h;
  /** Current text position. */
  private int posW = -1;
  /** Flag for a current text quotation. */
  private byte quote;
  /** Error flag. */
  private boolean error;
  /** Info flag. */
  private boolean info;

  /** Text array to be written. */
  private TokenIterator text;

  /** Text header. */
  final String header;
  /** Marked term. */
  private byte[] mark = {};
  /** Start of a text mark. */
  int startW = -1;
  /** End of a text mark. */
  int endW = -1;

  /**
   * Constructor.
   * @param head text header
   */
  TextRenderer(final String head) {
    header = head;
    init(mark);
  }

  /**
   * Initializes the font.
   * @param f font
   */
  void init(final Font f) {
    font = f;
    fontH = font.getSize();
    fwidth = GUIConstants.fontWidths(f);
  }

  /**
   * Initializes the constructor.
   * @param t text to be drawn
   */
  void init(final byte[] t) {
    text = new TokenIterator(t);
    offset = GUIProp.fontsize / 2 + 4;
  }

  /**
   * Sets the error flag.
   * @param err error flag
   */
  void error(final boolean err) {
    error = err;
  }

  /**
   * Sets the info flag.
   * @param inf info flag
   */
  void info(final boolean inf) {
    info = inf;
  }

  /**
   * Initializes the renderer.
   * @param pos current text position
   */
  private void init(final int pos) {
    text.init();
    g.setFont(font);

    wordW = 0;
    posW = 0;
    quote = 0;
    color = Color.black;
    x = offset;
    y = offset + (fontH >> 1) - pos;
    y += GUIConstants.lfont.getSize() + 15;
  }

  /**
   * Calculates the text height.
   * @param gg graphics reference
   * @param ww width of the text area
   * @param hh height of the text area
   * @return text height
   */
  int calc(final Graphics gg, final int ww, final int hh) {
    g = (Graphics2D) gg;
    w = ww - offset / 2;
    h = Integer.MAX_VALUE;
    init(0);
    startW = -1;
    endW = -1;
    while(more());
    final int ys = GUIConstants.lfont.getSize();

    h = hh + fontH + ys;
    return y + offset;
  }

  /**
   * Finds the current search term.
   * @param m term to be found
   * @return vertical position
   */
  int find(final byte[] m) {
    mark = Token.lc(m);
    startW = -1;
    endW = -1;
    if(m.length == 0) return -1;

    final int hh = h;
    h = Integer.MAX_VALUE;
    init(0);
    while(more()) {
      // mark typed in term
      if(text.contains(mark)) {
        h = hh;
        return Math.max(0, y);
      }
    }
    h = hh;
    return -1;
  }

  /**
   * Writes the text.
   * @param gg graphics reference
   * @param pos current text position
   */
  void write(final Graphics gg, final int pos) {
    g = (Graphics2D) gg;
    BaseXLayout.antiAlias(gg);

    final int ys = GUIConstants.lfont.getSize();
    g.setColor(GUIConstants.COLORS[16]);
    g.setFont(GUIConstants.lfont);
    g.drawString(header, offset, ys - pos + 6);

    init(pos);
    while(more()) write();
  }

  /**
   * Gets the current y value of the written text.
   * @return current y value
   */
  int getY() {
    return y;
  }

  /**
   * Checks if the text has more words to print.
   * @return true if the text has more words
   */
  private boolean more() {
    boolean nl = text.get(-1) == '\n';
    if(x != offset || !nl || text.get(0) != ' ') x += wordW;

    if(!text.more()) return false;
    posW++;

    // new line?
    wordW = 0;
    nl = text.get(0) == '\n';
    if(!nl) {
      final int s = text.size();
      for(int i = 0; i < s;) {
        final int c = text.cp(i);
        wordW += c > 255 ? g.getFontMetrics().charWidth(c) : fwidth[c & 0xFF];
        i += text.len(i);
      }
    }
    if(nl || x + wordW > w) { x = offset; y += fontH; }

    return startW == -1 ? y < h : y < h || posW < startW;
  }

  /**
   * Writes the current string to the graphics reference.
   */
  private void write() {
    final byte ch = text.get(0);
    if(quote != 0) {
      if(ch == quote) color = color2;
    } else {
      if(ch == '<') color = GUIConstants.COLORQUOTE;
    }

    // print visible text
    if(y >= 0 && y < h) {
      Color col = error ? GUIConstants.COLORERROR : info ? Color.black : color;
      if(startW >= 0 && endW >= 0 &&
          (posW >= startW && posW <= endW || posW >= endW && posW <= startW)) {
        g.setColor(GUIConstants.color6);
        g.fillRect(x, y - fontH + 3, wordW, fontH);
        col = Color.white;
      }
      // ignore whitespaces and draw string
      if(text.get(0) < 0 || text.get(0) > ' ') {
        // mark typed in term
        if(text.contains(mark)) {
          g.setColor(GUIConstants.color5);
          g.fillRect(x, y - fontH + 2, wordW, fontH);
        }
        g.setColor(col);
        g.drawString(text.next(), x, y);
      }
    }

    if(quote == 0) {
      if(color == GUIConstants.COLORQUOTE && (ch == '"' || ch == '\'')) {
        // colorize quotes
        color2 = color;
        color = GUIConstants.COLORERROR;
        quote = ch;
      } else if(ch == '>') {
        color = Color.black;
      }
    } else if(quote == ch) {
      quote = 0;
    }
  }

  /**
   * Selects the text at the specified position.
   * @param pos current text position
   * @param p mouse position
   * @param finish states if selection is in progress
   */
  void select(final int pos, final Point p, final boolean finish) {
    if(!finish) {
      startW = -1;
      endW = -1;
    }
    p.y -= 3;
    
    init(pos);
    if(p.y < y - fontH) return;
    
    while(more()) {
      if(p.x >= x && p.x < x + wordW && p.y > y - fontH && p.y <= y ||
          p.x > x && p.y < y - fontH || p.x < x && p.y < y) {
        posW--;
        break;
      }
    }
    if(!finish) startW = posW + 1;
    endW = posW + 1;
  }

  /**
   * Returns the selected text.
   * @param pos current text position
   * @return selected text
   */
  String copy(final int pos) {
    init(pos);

    while(more()) {
      if(posW >= startW && posW <= endW || posW >= endW && posW <= startW) {
        text.mark();
      }
    }
    final String nxt = text.marked();
    return nxt.equals("\n") ? "" : nxt;
  }
}
