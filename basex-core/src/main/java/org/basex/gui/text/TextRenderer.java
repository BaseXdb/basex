package org.basex.gui.text;

import java.awt.*;

import org.basex.gui.*;
import org.basex.gui.layout.*;
import org.basex.gui.text.SearchBar.*;
import org.basex.util.*;
import org.basex.util.list.*;

/**
 * Text renderer, supporting syntax highlighting and highlighting of selected, erroneous
 * or linked text.
 *
 * @author BaseX Team 2005-23, BSD License
 * @author Christian Gruen
 */
final class TextRenderer extends BaseXBack {
  /** Reference to the main window. */
  private final GUI gui;
  /** Offset. */
  private static final int OFFSET = 5;

  /** Text editor. */
  private final TextEditor text;
  /** Vertical start position. */
  private final BaseXScrollBar scroll;
  /** Indicates if the text is edited. */
  private final boolean edit;
  /** Current brackets. */
  private final IntList parentheses = new IntList();

  /** Fonts (default, bold). */
  private TextFonts fonts;

  /** Font height. */
  private int fontHeight;
  /** Width of current string. */
  private int stringWidth;
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
  /** Anti-aliasing type. */
  private String antiAlias;

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
  /** String rendering cache. */
  private final TokenBuilder stringCache = new TokenBuilder(4);

  /** Cursor position. */
  private final int[] cursor = new int[2];

  /** Vertical start position. */
  private Syntax syntax = Syntax.SIMPLE;
  /** Visibility of text cursor. */
  private boolean caret;
  /** Color highlighting flag. */
  private boolean markNext;
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
    setFont(GUIConstants.dmfont);
  }

  @Override
  public void setFont(final Font f) {
    fonts = new TextFonts(f);
    if(gui == null) return;

    final GUIOptions gopts = gui.gopts;
    margin = gopts.get(GUIOptions.SHOWMARGIN) ? Math.max(gopts.get(GUIOptions.MARGIN), 1) : -1;
    showInvisible = gopts.get(GUIOptions.SHOWINVISIBLE);
    showNL = gopts.get(GUIOptions.SHOWNL);
    showLines = gopts.get(GUIOptions.SHOWLINES);
    markline = gopts.get(GUIOptions.MARKLINE);
    antiAlias = gopts.get(GUIOptions.ANTIALIAS);
    indent = Math.max(1, gopts.get(GUIOptions.INDENT));
    repaint();
  }

  @Override
  public void paintComponent(final Graphics g) {
    super.paintComponent(g);
    BaseXLayout.antiAlias(g, antiAlias);

    parentheses.reset();
    int oldL = 0;
    final TextIterator iter = init(g, false);
    while(more(iter, g) && y < height) {
      if(line != oldL && y >= 0) {
        drawLineNumber(g);
        oldL = line;
      }
      write(iter, g);
    }
    if(x == offset) markLine(g);
    if(line != oldL) drawLineNumber(g);

    stringWidth = 0;
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
      final String string = Integer.toString(line);
      drawString(string, offset - fonts.stringWidth(string) - (OFFSET << 1), y, g);
    }
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
        final int lx = offset + charWidth(' ') * margin;
        g.setColor(GUIConstants.lgray);
        g.drawLine(lx, 0, lx, height);
      }
    }
  }

  /**
   * Sets a new search context.
   * @param sc new search context
   * @param jump jump to next search result
   */
  void search(final SearchContext sc, final boolean jump) {
    text.search(sc, jump);
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

    final Graphics g = getGraphics();
    for(final TextIterator iter = init(g, true); more(iter, g) && iter.pos() < pos; next(iter));
    return y;
  }

  /**
   * Returns the line and column of the current caret position.
   * @return line/column
   */
  int[] pos() {
    final Graphics g = getGraphics();
    boolean more = true;
    int col = 1;
    final TextIterator iter = init(g, true);
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
    return new int[] { line, col };
  }

  /**
   * Sets a new font.
   * @param style font style (Font#PLAIN, Font#BOLD)
   */
  private void font(final int style) {
    fonts.assign(style);
    fontHeight = fonts.font().getSize() * 5 / 4;
  }

  /**
   * Initializes the renderer.
   * @param g graphics reference (can be {@code null})
   * @param start start at beginning of text or at current scroll position
   * @return text iterator
   */
  private TextIterator init(final Graphics g, final boolean start) {
    syntax.init(GUIConstants.TEXT);
    font(Font.PLAIN);

    offset = OFFSET;
    if(g != null) {
      fonts.init(this);
      if(edit && showLines) {
        final String string = Integer.toString(text.lines());
        g.setFont(fonts.font(string));
        offset += fonts.stringWidth(string) + (OFFSET << 1);
      }
    }
    x = offset;
    y = fontHeight - (start ? 0 : scroll.pos()) - 2;
    lineY = y - (fontHeight << 2) / 5;
    line = 1;
    link = false;

    final TextIterator iter = new TextIterator(text);
    lineC = edit && iter.caretLine(true);
    return iter;
  }

  /**
   * Computes the height of the text and updates the scroll bar.
   */
  void computeHeight() {
    width = getWidth() - (offset >> 1);

    final Graphics g = getGraphics();
    for(final TextIterator iter = init(g, true); more(iter, g); next(iter));
    height = getHeight() + fontHeight;
    scroll.height(y + OFFSET);
  }

  /**
   * Returns the current vertical cursor position.
   * @return new position
   */
  int cursorY() {
    final Graphics g = getGraphics();
    for(final TextIterator iter = init(g, true); more(iter, g) && !iter.edited(); next(iter));
    return y - fontHeight;
  }

  /**
   * Checks if the text has more words to print.
   * @param iter iterator
   * @param g graphics reference (can be {@code null})
   * @return {@code true}} if more strings exist
   */
  private boolean more(final TextIterator iter, final Graphics g) {
    // no valid graphics reference, no more words found: quit
    final int w = width;
    if(g == null || !iter.moreStrings(w)) return false;

    // calculate width of next string to be drawn
    final int p = iter.pos(), m = w - offset;
    int sw = 0;
    while(iter.more()) {
      final int ch = iter.next();
      // process control codes
      if(ch == TokenBuilder.BOLD) {
        font(Font.BOLD);
      } else if(ch == TokenBuilder.NORM) {
        font(Font.PLAIN);
      } else if(ch == TokenBuilder.ULINE) {
        link ^= true;
      } else {
        sw += charWidth(ch);
        // check if string width exceeds panel width
        if(sw > w - x) {
          if(sw > m) {
            iter.posEnd(iter.pos());
            break;
          }
          newline(fontHeight);
        }
      }
    }
    iter.pos(p);
    stringWidth = sw;
    return true;
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
   * Marks the current line as erroneous.
   * @param g graphics reference
   */
  private void markErrorLine(final Graphics g) {
    g.setColor(GUIConstants.colormark2A);
    g.fillRect(0, lineY, offset - OFFSET * 3 / 2, fontHeight);
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
    x += stringWidth;
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
    final Color color = isEnabled() ? markNext ? GUIConstants.GREEN : link ?
      GUIConstants.color4 : syntax.getColor(iter) : GUIConstants.gray;
    int cp = iter.curr();
    markNext = cp == TokenBuilder.MARK;

    // retrieve first character of current token
    final int pos = iter.pos(), cpos = iter.caret();

    // handle matching parentheses
    if(cp == '(' || cp == '[' || cp == '{') {
      parentheses.add(x).add(y).add(pos).add(cp);
    } else if((cp == ')' || cp == ']' || cp == '}') && !parentheses.isEmpty()) {
      final int open = cp == ')' ? '(' : cp == ']' ? '[' : '{';
      if(parentheses.peek() == open) {
        parentheses.pop();
        final int cr = parentheses.pop(), yy = parentheses.pop(), xx = parentheses.pop();
        if(cpos == pos || cpos == cr) {
          g.setColor(GUIConstants.color4);
          g.drawRect(xx, yy - (fontHeight << 2) / 5, charWidth(open), fontHeight);
          g.drawRect(x, lineY, charWidth(cp), fontHeight);
        }
      }
    }

    // check if text is visible
    if(y > 0) {
      // mark selected text
      if(iter.selectStart()) {
        int xx = x;
        while(!iter.inSelect() && iter.more()) xx += charWidth(iter.next());
        int cw = 0;
        while(iter.inSelect() && iter.more()) cw += charWidth(iter.next());
        g.setColor(GUIConstants.color2A);
        g.fillRect(xx, lineY, cw, fontHeight);
        iter.pos(pos);
      }

      // mark found text
      for(int xx = x; iter.more() && iter.searchStart();) {
        while(!iter.inSearch() && iter.more()) xx += charWidth(iter.next());
        int cw = 0;
        while(iter.inSearch() && iter.more()) cw += charWidth(iter.next());
        g.setColor(GUIConstants.color2A);
        g.fillRect(xx, lineY, cw, fontHeight);
        xx += cw;
      }
      iter.pos(pos);

      // retrieve first character of current token
      if(iter.erroneous()) drawError(g);

      if(showNL && cp == '\n') {
        // draw newline character
        g.setColor(GUIConstants.gray);
        drawString("\u00b6", x, y, g);
      } else if(showInvisible && cp == '\t') {
        // draw tab arrow
        final int lh = 1 + fontHeight / 12, xe = x + charWidth('\t') - lh;
        final int yy = y - fontHeight * 3 / 10, as = (lh << 1) - 1;
        g.setColor(GUIConstants.gray);
        g.drawLine(x + lh, yy, xe, yy);
        g.drawLine(xe - as, yy - as, xe, yy);
        g.drawLine(xe - as, yy + as, xe, yy);
      } else if(cp > ' ' && cp < TokenBuilder.PRIVATE_START || cp > TokenBuilder.PRIVATE_END) {
        if(showInvisible && Character.isSpaceChar(cp)) {
          // draw whitespace character
          final int s = fontHeight / 12 + 1;
          g.setColor(GUIConstants.gray);
          g.fillRect(x + (stringWidth >> 1), y - fontHeight * 3 / 10, s, s);
        } else {
          // draw non-whitespace string
          g.setColor(color);

          // draw character by character (required by Java 9), but combine zero-width characters
          cp = iter.next();
          int xx = x;
          for(int cw, xxx = xx + charWidth(cp); iter.more();) {
            stringCache.reset();
            do {
              stringCache.add(cp);
              cp = iter.next();
              cw = charWidth(cp);
            } while(cw <= 0 && iter.more());
            drawString(stringCache.toString(), xx, y, g);
            xx = xxx;
            xxx += cw;
          }
          drawString(Character.toString(cp), xx, y, g);
          iter.pos(pos);
        }
      }

      // underline linked text
      if(link) g.drawLine(x, y + 1, x + stringWidth, y + 1);

      // show cursor
      if(caret && iter.edited()) {
        for(int xx = x; iter.more(); xx += charWidth(iter.next())) {
          if(cpos == iter.pos()) {
            drawCaret(g, xx);
            break;
          }
        }
        iter.pos(pos);
      }
    }

    // finish step
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
    final int ww = stringWidth == 0 ? charWidth(' ') : stringWidth;
    final int s = Math.max(2, fontHeight / 6);
    g.setColor(GUIConstants.RED);
    for(int xp = x; xp < x + ww; xp += 2) g.drawLine(xp - 1, y + 2, xp, y + s + 1);
    if(edit) markErrorLine(g);
  }

  /**
   * Returns the width of the specified codepoint.
   * @param cp codepoint
   * @return width
   */
  private int charWidth(final int cp) {
    return cp == '\t' ? fonts.charWidth(' ') * indent :
      cp >= TokenBuilder.PRIVATE_START && cp <= TokenBuilder.PRIVATE_END ||
      cp >= 0xD800 && cp <= 0xDC00 ? 0 : fonts.charWidth(cp);
  }

  /**
   * Returns the width of the specified codepoint.
   * @param string string to be drawn
   * @param xx x position
   * @param yy y position
   * @param g graphics reference
   */
  private void drawString(final String string, final int xx, final int yy, final Graphics g) {
    g.setFont(fonts.font(string));
    g.drawString(string, xx, yy);
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
        if(xx > x && xx <= x + stringWidth && yy > y - fontHeight && yy <= y) {
          while(iter.more()) {
            final int ww = charWidth(iter.curr());
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
