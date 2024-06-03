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
 * @author BaseX Team 2005-24, BSD License
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
  private TextFont font;
  /** Font height. */
  private int fontHeight;
  /** Width of current string. */
  private int stringWidth;
  /** Current string. */
  private String currString;
  /** Show invisible characters. */
  private boolean showInvisible;
  /** Show newlines. */
  private boolean showNL;
  /** Line margin. */
  private int margin;
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
    super.setFont(f);
    if(gui == null) return;

    final GUIOptions gopts = gui.gopts;
    margin = gopts.get(GUIOptions.SHOWMARGIN) ? Math.max(gopts.get(GUIOptions.MARGIN), 1) : -1;
    showInvisible = gopts.get(GUIOptions.SHOWINVISIBLE);
    showNL = gopts.get(GUIOptions.SHOWNL);
    showLines = gopts.get(GUIOptions.SHOWLINES);
    markline = gopts.get(GUIOptions.MARKLINE);
    antiAlias = gopts.get(GUIOptions.ANTIALIAS);
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
    if(s == iter.errorPos()) drawError(g);

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
      drawString(string, offset - font.stringWidth(string) - (OFFSET << 1), y, g);
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
        final int lx = offset + font.charWidth(' ') * margin;
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
   * @return line and column
   */
  int[] caretPos() {
    final Graphics g = getGraphics();
    int col = 1;
    boolean more = true;
    for(final TextIterator iter = init(g, true); more(iter, g);) {
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
   * Sets a new font style.
   * @param style font style ({@link Font#PLAIN}, {@link Font#BOLD})
   */
  private void setStyle(final int style) {
    font.style(style);
    fontHeight = font.size() * 5 / 4;
  }

  @Override
  public Dimension getPreferredSize() {
    // calculate size required for the currently rendered text
    final Graphics g = getGraphics();
    width = Integer.MAX_VALUE;
    height = Integer.MAX_VALUE;
    int maxX = 0;
    for(final TextIterator iter = init(g, true); more(iter, g); next(iter)) {
      if(iter.curr() == TokenBuilder.NLINE) maxX = Math.max(x, maxX);
    }
    return new Dimension(Math.max(x, maxX) + font.charWidth(' '), y + fontHeight);
  }

  /**
   * Initializes the renderer.
   * @param g graphics reference (can be {@code null})
   * @param start start at beginning of text or at current scroll position
   * @return text iterator
   */
  private TextIterator init(final Graphics g, final boolean start) {
    final int indent = gui != null ? Math.max(1, gui.gopts.get(GUIOptions.INDENT)) :
      GUIOptions.INDENT.value();
    font = new TextFont(getFont(), indent, this);
    setStyle(Font.PLAIN);
    syntax.init(GUIConstants.TEXT);

    offset = OFFSET;
    if(g != null && edit && showLines) {
      offset += font.stringWidth(Integer.toString(text.lines())) + (OFFSET << 1);
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
    final int w = width, maxWidth = w - offset;
    if(g == null || maxWidth <= 0 || !iter.moreStrings(w >> 2)) return false;

    String s = iter.currString();
    int sw = 0;

    if(s.isEmpty()) return false;
    final int cp = s.codePointAt(0);
    if(cp == TokenBuilder.BOLD) {
      setStyle(Font.BOLD);
    } else if(cp == TokenBuilder.NORM) {
      setStyle(Font.PLAIN);
    } else if(cp == TokenBuilder.ULINE) {
      link ^= true;
    } else {
      // compute string width, shorten if it exceeds panel width
      sw = font.stringWidth(s);
      if(sw > maxWidth) {
        if(x != offset) newline(true);

        final TokenBuilder tb = new TokenBuilder();
        sw = 0;
        for(final int scp : s.codePoints().toArray()) {
          if(sw >= maxWidth) break;
          tb.add(scp);
          sw += font.charWidth(scp);
          if(sw > maxWidth) sw = font.stringWidth(tb.toString());
        }
        s = tb.removeLast().toString();
        if(s.isEmpty()) return false;
        sw = font.stringWidth(s);
        iter.posEnd(iter.pos() + tb.size());
      }
    }
    // no space left: move current string into next line
    if(sw < maxWidth && sw > w - x) newline(true);

    currString = s;
    stringWidth = sw;
    return true;
  }

  /**
   * Jumps to the next line.
   * @param full add full line height
   */
  private void newline(final boolean full) {
    final int h = fontHeight >> (full ? 0 : 1);
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
      newline(ch == TokenBuilder.NLINE);
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
    final int cp = iter.curr();
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
          g.drawRect(xx, yy - (fontHeight << 2) / 5, font.charWidth(open), fontHeight);
          g.drawRect(x, lineY, font.charWidth(cp), fontHeight);
        }
      }
    }

    // check if text is visible
    if(y > 0) {
      // mark selected and found text
      mark(iter.selection(), iter, g);
      for(final int[] sr : iter.searchResults()) mark(sr, iter, g);
      //for(int[] sr; (sr = iter.searchResult()) != null;) mark(sr, iter, g);

      // retrieve first character of current token
      if(iter.error()) drawError(g);

      if(showNL && cp == TokenBuilder.NLINE) {
        // draw newline character
        g.setColor(GUIConstants.gray);
        drawString("\u00b6", x, y, g);
      } else if(showInvisible && cp == '\t') {
        // draw tab arrow
        final int lh = 1 + fontHeight / 12, xe = x + font.charWidth('\t') - lh;
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
          drawString(currString, x, y, g);
        }
      }
      // underline linked text
      if(link) g.drawLine(x, y + 1, x + stringWidth, y + 1);
      // show cursor
      if(caret && iter.edited()) drawCaret(g, x + font.stringWidth(iter.substring(pos, cpos)));
    }

    // finish step
    next(iter);
  }

  /**
   * Highlights text.
   * @param range start/end of mark
   * @param iter iterator
   * @param g graphics reference
   */
  private void mark(final int[] range, final TextIterator iter, final Graphics g) {
    if(range != null) {
      final int pos = iter.pos(), posEnd = iter.posEnd();
      final int ss = Math.max(pos, range[0]), se = Math.min(posEnd, range[1]);
      final int xs = font.stringWidth(iter.substring(pos, ss));
      final int cw = font.stringWidth(iter.substring(ss, se));
      g.setColor(GUIConstants.color2A);
      g.fillRect(x + xs, lineY, cw, fontHeight);
    }
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
    final int ww = stringWidth == 0 ? font.charWidth(' ') : stringWidth;
    final int s = Math.max(2, fontHeight / 6);
    g.setColor(GUIConstants.RED);
    for(int xp = x; xp < x + ww; xp += 2) g.drawLine(xp - 1, y + 2, xp, y + s + 1);
    if(edit) markErrorLine(g);
  }

  /**
   * Returns the width of the specified codepoint.
   * @param string string to be drawn
   * @param xx x position
   * @param yy y position
   * @param g graphics reference
   */
  private void drawString(final String string, final int xx, final int yy, final Graphics g) {
    g.setFont(font.font(string));
    g.drawString(string, xx, yy);
  }

  /**
   * Jumps to the text at the specified position.
   * @param pos mouse position
   * @return text iterator
   */
  TextIterator jump(final Point pos) {
    final Graphics g = getGraphics();
    final TextIterator iter = init(g, false);

    for(final int xPos = pos.x, yPos = pos.y - fontHeight / 5;
        yPos >= y - fontHeight && more(iter, g); next(iter)) {
      // skip line
      if(yPos >= y) continue;
      // beginning of line
      if(xPos < x) break;
      // token found
      if(xPos < x + stringWidth) {
        final int p = iter.pos(), sw = xPos - x;
        for(int caretP, oldFsw = 0; iter.more();) {
          caretP = iter.pos();
          iter.next();
          final int fsw = font.stringWidth(iter.substring(p, iter.pos()));
          if(sw < fsw) {
            if(sw < oldFsw + (fsw - oldFsw) / 2) iter.pos(caretP);
            break;
          }
          oldFsw = fsw;
        }
        break;
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
