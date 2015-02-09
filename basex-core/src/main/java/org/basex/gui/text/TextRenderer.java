package org.basex.gui.text;

import java.awt.*;

import org.basex.gui.*;
import org.basex.gui.layout.*;
import org.basex.gui.text.SearchBar.SearchDir;
import org.basex.util.*;
import org.basex.util.list.*;

/**
 * Text renderer, supporting syntax highlighting and highlighting of selected, erroneous
 * or linked text.
 *
 * @author BaseX Team 2005-15, BSD License
 * @author Christian Gruen
 */
final class TextRenderer extends BaseXBack {
  /** Reference to the main window. */
  private final GUI gui;
  /** Offset. */
  private static final int OFFSET = 5;

  /** Text array to be written. */
  private final TextEditor text;
  /** Vertical start position. */
  private final BaseXScrollBar scroll;
  /** Indicates if the text is edited. */
  private final boolean edit;
  /** Current brackets. */
  private final IntList pars = new IntList();

  /** Font. */
  private Font font;
  /** Default font. */
  private Font defaultFont;
  /** Bold font. */
  private Font boldFont;
  /** Font height. */
  private int fontHeight;
  /** Character widths. */
  private int[] charWidths;
  /** Width of current word. */
  private int wordWidth;
  /** Show invisible characters. */
  private boolean showInvisible;
  /** Show newlines. */
  private boolean showNL;
  /** Line margin. */
  private int margin;
  /** Tab indentation. */
  private int indent;
  /** Show line numbers. */
  private boolean showLines;
  /** Mark current line. */
  private boolean markline;

  /** Border offset. */
  private int offset;
  /** Width of total text area. */
  private int width;
  /** Height of total text area. */
  private int height;

  /** Current x position. */
  private int x;
  /** Current y position. */
  private int y;
  /** Current y position of rendered line. */
  private int lineY;
  /** Current line number. */
  private int line;
  /** Indicates if the cursor is located in the current line. */
  private boolean lineC;

  /** Cursor position. */
  private final int[] cursor = new int[2];

  /** Vertical start position. */
  private Syntax syntax = Syntax.SIMPLE;
  /** Visibility of text cursor. */
  private boolean caret;
  /** Color highlighting flag. */
  private boolean highlighted;
  /** Indicates if the current token is part of a link. */
  private boolean link;

  /**
   * Constructor.
   * @param text text to be drawn
   * @param scroll scrollbar reference
   * @param edit editable flag
   * @param gui reference to the main window
   */
  TextRenderer(final TextEditor text, final BaseXScrollBar scroll, final boolean edit,
      final GUI gui) {

    setOpaque(false);
    this.text = text;
    this.scroll = scroll;
    this.edit = edit;
    this.gui = gui;
    font(GUIConstants.dmfont);
  }

  @Override
  public void setFont(final Font f) {
    defaultFont = f;
    boldFont = f.deriveFont(Font.BOLD);
    font(f);
    if(gui != null) {
      margin = gui.gopts.get(GUIOptions.SHOWMARGIN)
          ? Math.max(gui.gopts.get(GUIOptions.MARGIN), 1) : -1;
      showInvisible = gui.gopts.get(GUIOptions.SHOWINVISIBLE);
      showNL = gui.gopts.get(GUIOptions.SHOWNL);
      showLines = gui.gopts.get(GUIOptions.SHOWLINES);
      markline = gui.gopts.get(GUIOptions.MARKLINE);
      indent = Math.max(1, gui.gopts.get(GUIOptions.INDENT));
      repaint();
    }
  }

  @Override
  public void paintComponent(final Graphics g) {
    super.paintComponent(g);
    pars.reset();
    final TextIterator iter = init(g, false);
    int oldL = 0;
    while(more(iter, g)) {
      if(line != oldL && y >= 0) {
        drawLineNumber(g);
        oldL = line;
      }
      write(iter, g);
    }
    if(x == offset) markLine(g);
    if(line != oldL) drawLineNumber(g);

    wordWidth = 0;
    final int s = iter.pos();
    if(caret && s == iter.caret()) drawCaret(g, x);
    if(s == iter.error()) drawError(g);

    drawLinesSep(g);
  }

  /**
   * Renders the current line number.
   * @param g graphics reference
   */
  private void drawLineNumber(final Graphics g) {
    if(edit && showLines) {
      g.setColor(GUIConstants.gray);
      final String s = Integer.toString(line);
      g.drawString(s, offset - fontWidth(g, s) - OFFSET * 2, y);
    }
  }

  /**
   * Marks the current line as erroneous.
   * @param g graphics reference
   */
  private void drawErrorLine(final Graphics g) {
    g.setColor(GUIConstants.colormark2A);
    g.fillRect(0, lineY, offset - OFFSET * 3 / 2, fontHeight);
  }

  /**
   * Draws the line number separator.
   * @param g graphics reference
   */
  private void drawLinesSep(final Graphics g) {
    if(edit) {
      if(showLines) {
        final int lx = offset - OFFSET * 3 / 2;
        g.setColor(GUIConstants.lgray);
        g.drawLine(lx, 0, lx, height);
      }
      if(margin != -1) {
        // line margin
        final int lx = offset + fontWidth(g, ' ') * margin;
        g.setColor(GUIConstants.lgray);
        g.drawLine(lx, 0, lx, height);
      }
    }
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
   * Returns the cursor coordinates.
   * @return coordinates
   */
  int[] cursor() {
    return cursor;
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

    final int hh = height;
    height = Integer.MAX_VALUE;
    final Graphics g = getGraphics();
    for(final TextIterator iter = init(g, true); more(iter, g) && iter.pos() < pos; next(iter));
    height = hh;
    return y;
  }

  /**
   * Returns the line and column of the current caret position.
   * @return line/column
   */
  int[] pos() {
    final int hh = height;
    height = Integer.MAX_VALUE;
    final Graphics g = getGraphics();
    final TextIterator iter = init(g, true);
    boolean more = true;
    int col = 1;
    while(more(iter, g)) {
      final int p = iter.pos();
      while(iter.more()) {
        more = iter.pos() < iter.caret();
        if(!more) break;
        iter.next();
        col++;
      }
      if(!more) break;
      iter.pos(p);
      if(next(iter)) col = 1;
    }
    height = hh;
    return new int[] { line, col };
  }

  /**
   * Sets the current font.
   * @param f font
   */
  private void font(final Font f) {
    font = f;
    fontHeight = f.getSize() * 5 / 4;
    charWidths = GUIConstants.fontWidths(f);
  }

  @Override
  public Dimension getPreferredSize() {
    // calculate size required for the currently rendered text
    final Graphics g = getGraphics();
    width = Integer.MAX_VALUE;
    height = Integer.MAX_VALUE;
    final TextIterator iter = init(g, true);
    int maxX = 0;
    while(more(iter, g)) {
      if(iter.curr() == '\n') maxX = Math.max(x, maxX);
      next(iter);
    }
    return new Dimension(Math.max(x, maxX) + charWidths[' '], y + fontHeight);
  }

  /**
   * Initializes the renderer.
   * @param g graphics reference
   * @param start start at beginning of text or at current scroll position
   * @return iterator
   */
  private TextIterator init(final Graphics g, final boolean start) {
    font = defaultFont;
    syntax.init(GUIConstants.TEXT);

    final TextIterator iter = new TextIterator(text);
    link = false;
    offset = OFFSET;
    if(edit && showLines) offset += fontWidth(g, Integer.toString(text.lines())) + OFFSET * 2;
    x = offset;
    y = fontHeight - (start ? 0 : scroll.pos()) - 2;
    lineY = y - fontHeight * 4 / 5;
    line = 1;
    lineC = edit && iter.caretLine(true);
    if(g != null) g.setFont(font);
    return iter;
  }

  /**
   * Updates the scroll bar.
   */
  void updateScrollbar() {
    width = getWidth() - (offset >> 1);
    height = Integer.MAX_VALUE;
    final Graphics g = getGraphics();
    final TextIterator iter = init(g, true);
    while(more(iter, g)) next(iter);
    height = getHeight() + fontHeight;
    scroll.height(y + OFFSET);
  }

  /**
   * Returns the current vertical cursor position.
   * @return new position
   */
  int cursorY() {
    final int hh = height;
    height = Integer.MAX_VALUE;
    final Graphics g = getGraphics();
    final TextIterator iter = init(g, true);
    while(more(iter, g) && !iter.edited()) next(iter);
    height = hh;
    return y - fontHeight;
  }

  /**
   * Checks if the text has more words to print.
   * @param iter iterator
   * @param g graphics reference
   * @return true if the text has more words
   */
  private boolean more(final TextIterator iter, final Graphics g) {
    // no more words found; quit
    if(!iter.moreTokens()) return false;

    // calculate word width
    int ww = 0;
    final int p = iter.pos();
    while(iter.more()) {
      final int ch = iter.next();
      // internal special codes...
      if(ch == TokenBuilder.BOLD) {
        font(boldFont);
      } else if(ch == TokenBuilder.NORM) {
        font(defaultFont);
      } else if(ch == TokenBuilder.ULINE) {
        link ^= true;
      } else {
        ww += fontWidth(g, ch);
      }
    }
    iter.pos(p);

    if(x + ww > width) newline(fontHeight);
    wordWidth = ww;

    // check if word has been found, and word is still visible
    return y < height;
  }

  /**
   * Jumps to the next line.
   * @param h line height
   */
  private void newline(final int h) {
    x = offset;
    y += h;
    lineY += h;
  }

  /**
   * Marks the current line.
   * @param g graphics reference
   */
  private void markLine(final Graphics g) {
    if(lineC && markline) {
      g.setColor(GUIConstants.color3A);
      g.fillRect(0, lineY, width + offset, fontHeight);
    }
  }

  /**
   * Finishes the current token.
   * @param iter iterator
   * @return new line
   */
  private boolean next(final TextIterator iter) {
    final int ch = iter.curr();
    if(ch == TokenBuilder.NLINE || ch == TokenBuilder.HLINE) {
      newline(fontHeight >> (ch == TokenBuilder.NLINE ? 0 : 1));
      line++;
      lineC = edit && iter.caretLine(false);
      return true;
    }
    x += wordWidth;
    return false;
  }

  /**
   * Writes the current string to the graphics reference.
   * @param iter iterator
   * @param g graphics reference
   */
  private void write(final TextIterator iter, final Graphics g) {
    if(x == offset) markLine(g);

    // choose color for enabled text, depending on highlighting, link, or current syntax
    final Color color = isEnabled() ? highlighted ? GUIConstants.GREEN : link ?
      GUIConstants.color4 : syntax.getColor(iter) : GUIConstants.gray;

    // retrieve first character of current token
    final int ch = iter.curr();
    highlighted = ch == TokenBuilder.MARK;

    final int cp = iter.pos();
    final int cc = iter.caret();
    if(y > 0 && y < height) {
      // mark selected text
      if(iter.selectStart()) {
        int xx = x;
        while(!iter.inSelect() && iter.more()) xx += fontWidth(g, iter.next());
        int cw = 0;
        while(iter.inSelect() && iter.more()) cw += fontWidth(g, iter.next());
        g.setColor(GUIConstants.color2A);
        g.fillRect(xx, lineY, cw, fontHeight);
        iter.pos(cp);
      }

      // mark found text
      int xx = x;
      while(iter.more() && iter.searchStart()) {
        while(!iter.inSearch() && iter.more()) xx += fontWidth(g, iter.next());
        int cw = 0;
        while(iter.inSearch() && iter.more()) cw += fontWidth(g, iter.next());
        g.setColor(GUIConstants.color2A);
        g.fillRect(xx, lineY, cw, fontHeight);
        xx += cw;
      }
      iter.pos(cp);

      // retrieve first character of current token
      if(iter.erroneous()) drawError(g);

      // don't write whitespaces
      if(ch == '\n' && showNL) {
        g.setColor(GUIConstants.gray);
        g.drawString("\u00b6", x, y);
      } else if((ch == '\u00a0' || ch >= 0x2000 && ch <= 0x200A) && showInvisible) {
        final int s = fontHeight / 12 + 1;
        g.setColor(GUIConstants.gray);
        g.fillRect(x + (wordWidth >> 1), y - fontHeight * 3 / 10, s, s);
      } else if(ch == '\t' && showInvisible) {
        final int yy = y - fontHeight * 3 / 10;
        final int s = 1 + fontHeight / 12;
        final int xe = x + fontWidth(g, '\t') - s;
        final int as = s * 2 - 1;
        g.setColor(GUIConstants.gray);
        g.drawLine(x + s, yy, xe, yy);
        g.drawLine(xe - as, yy - as, xe, yy);
        g.drawLine(xe - as, yy + as, xe, yy);
      } else if(ch > ' ') {
        g.setColor(color);
        String n = iter.nextString();
        int ww = width - x;
        if(x + wordWidth > ww) {
          // shorten string if it cannot be completely shown (saves memory)
          int c = 0;
          for(final int nl = n.length(); c < nl && ww > 0; c++) {
            ww -= fontWidth(g, n.charAt(c));
          }
          n = n.substring(0, c);
        }
        g.drawString(n, x, y);
      } else if(ch <= TokenBuilder.ULINE) {
        g.setFont(font);
      }

      // underline linked text
      if(link) g.drawLine(x, y + 1, x + wordWidth, y + 1);

      // show cursor
      if(caret && iter.edited()) {
        xx = x;
        while(iter.more()) {
          if(cc == iter.pos()) {
            drawCaret(g, xx);
            break;
          }
          xx += fontWidth(g, iter.next());
        }
        iter.pos(cp);
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
         g.setColor(GUIConstants.color4);
          g.drawRect(xx, yy - fontHeight * 4 / 5, fontWidth(g, open), fontHeight);
          g.drawRect(x, lineY, fontWidth(g, ch), fontHeight);
        }
      }
    }
    next(iter);
  }

  /**
   * Paints the text cursor.
   * @param g graphics reference
   * @param xx x position
   */
  private void drawCaret(final Graphics g, final int xx) {
    g.setColor(GUIConstants.dgray);
    g.fillRect(xx, lineY, 2, fontHeight);
    cursor[0] = xx;
    cursor[1] = lineY + fontHeight;
  }

  /**
   * Draws an error marker.
   * @param g graphics reference
   */
  private void drawError(final Graphics g) {
    final int ww = wordWidth == 0 ? fontWidth(g, ' ') : wordWidth;
    final int s = Math.max(1, fontHeight / 8);
    g.setColor(GUIConstants.LRED);
    g.fillRect(x, y + 2, ww, s);
    g.setColor(GUIConstants.RED);
    for(int xp = x; xp < x + ww; xp++) {
      if((xp & 1) == 0) g.drawLine(xp, y + 2, xp, y + s + 1);
    }
    if(edit) drawErrorLine(g);
  }

  /**
   * Returns the width of the specified codepoint.
   * @param g graphics reference
   * @param cp character
   * @return width
   */
  private int fontWidth(final Graphics g, final int cp) {
    return cp < ' ' || g == null ? cp == '\t' ?
      charWidths[' '] * indent : 0 : cp < 256 ? charWidths[cp] :
      cp >= 0xD800 && cp <= 0xDC00 ? 0 : g.getFontMetrics().charWidth(cp);
  }

  /**
   * Returns the width of the specified string.
   * @param g graphics reference
   * @param string string
   * @return width
   */
  private int fontWidth(final Graphics g, final String string) {
    final int cl = string.length();
    int w = 0;
    for(int c = 0; c < cl; c++) w += fontWidth(g, string.charAt(c));
    return w;
  }

  /**
   * Selects the text at the specified position.
   * @param p mouse position
   * @return text iterator
   */
  TextIterator jump(final Point p) {
    final int xx = p.x;
    final int yy = p.y - fontHeight / 5;

    final Graphics g = getGraphics();
    final TextIterator iter = init(g, false);
    if(yy > y - fontHeight) {
      int s = iter.pos();
      while(true) {
        // end of line
        if(xx > x && yy < y - fontHeight) {
          iter.pos(s);
          break;
        }
        // end of text - skip last characters
        if(!more(iter, g)) {
          while(iter.more()) iter.next();
          break;
        }
        // beginning of line
        if(xx <= x && yy < y) break;
        // middle of line
        if(xx > x && xx <= x + wordWidth && yy > y - fontHeight && yy <= y) {
          while(iter.more()) {
            final int ww = fontWidth(g, iter.curr());
            if(xx < x + ww) break;
            x += ww;
            iter.next();
          }
          break;
        }
        s = iter.pos();
        next(iter);
      }
    }
    iter.link(link);
    return iter;
  }

  /**
   * Returns the font height.
   * @return font height
   */
  int fontHeight() {
    return fontHeight;
  }

  /**
   * Sets the cursor flag and repaints the panel.
   * @param c cursor flag
   */
  void caret(final boolean c) {
    caret = c;
    repaint();
  }

  /**
   * Returns the cursor flag.
   * @return cursor flag
   */
  boolean caret() {
    return caret;
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
