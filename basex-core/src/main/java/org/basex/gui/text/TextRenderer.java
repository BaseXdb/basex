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
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
final class TextRenderer extends BaseXBack {
  /** Editor options. */
  private final EditorOptions opts;
  /** Offset. */
  private static final int OFFSET = 5;

  /** Text editor. */
  private final TextEditor text;
  /** Vertical start position. */
  private final BaseXScrollBar scroll;
  /** Horizontal start position (no word wrap). */
  private final BaseXScrollBar hscroll;
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
  /** Show invisible characters. */
  private boolean showInvisible;
  /** Show newlines. */
  private boolean showNL;
  /** Wrap long lines. */
  private boolean wrap;
  /** Line margin ({@code 0} if no margin is shown). */
  private int margin;
  /** Show line numbers. */
  private boolean showLines;
  /** Mark current line. */
  private boolean markline;
  /** Antialiasing type. */
  private String antiAlias;

  /** Border offset. */
  private int offset;
  /** Width of total text area. */
  private int width;
  /** Height of total text area. */
  private int height;

  /** x position at the beginning of a row. */
  private int startX;
  /** Current x position. */
  private int x;
  /** Current y position. */
  private int y;
  /** Current y position of rendered line. */
  private int lineY;
  /** Current x position of the row that was left by the last line break. */
  private int rowX;
  /** Current y position of the row that was left by the last line break. */
  private int rowY;
  /** Current y position of the rendered line that was left by the last line break. */
  private int rowLineY;
  /** Indicates if the current token was moved to a new row. */
  private boolean wrapped;
  /** Indicates if the last scan stopped at the end of a row. */
  private boolean rowEnd;
  /** Current line number. */
  private int line;
  /** Start of the line with the cursor ({@code -1}: not computed yet). */
  private int caretStart;
  /** End of the line with the cursor. */
  private int caretEnd;

  /** Line-offset cache (maps document-space y or text position to a line). */
  private final TextLineCache cache = new TextLineCache();
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
   * @param scroll vertical scrollbar reference
   * @param hscroll horizontal scrollbar reference
   * @param edit editable flag
   * @param opts editor options
   */
  TextRenderer(final TextEditor text, final BaseXScrollBar scroll, final BaseXScrollBar hscroll,
      final boolean edit, final EditorOptions opts) {

    setOpaque(false);
    this.text = text;
    this.scroll = scroll;
    this.hscroll = hscroll;
    this.edit = edit;
    this.opts = opts;
    setFont(GUIConstants.dmfont);
  }

  @Override
  public void setFont(final Font f) {
    super.setFont(f);
    // the superclass constructor assigns a font before the options are available
    if(opts == null) return;
    cache.reset();

    margin = opts.margin();
    // text that cannot be edited is always wrapped: it has no horizontal scrolling
    wrap = !edit || opts.get(GUIOptions.WORDWRAP);
    if(wrap) hscroll.pos(0);
    showInvisible = opts.get(GUIOptions.SHOWINVISIBLE);
    showNL = opts.get(GUIOptions.SHOWNL);
    showLines = opts.get(GUIOptions.SHOWLINES);
    markline = opts.get(GUIOptions.MARKLINE);
    antiAlias = opts.get(GUIOptions.ANTIALIAS);
    repaint();
  }

  @Override
  public void paintComponent(final Graphics g) {
    super.paintComponent(g);
    BaseXLayout.antiAlias(g, antiAlias);

    parentheses.reset();
    final TextIterator iter = init(g, false);
    clipText(g);
    skip(iter);
    int oldL = line - 1;
    while(more(iter, g) && y < height) {
      if(line != oldL && y >= 0) {
        drawLineNumber(g);
        oldL = line;
      }
      write(iter, g);
    }
    if(rowStart()) markLine(iter, g);
    if(line != oldL) drawLineNumber(g);

    stringWidth = 0;
    final int s = iter.pos();
    if(caret && s == iter.caret()) drawCaret(g, x, lineY);
    if(s == iter.errorPos()) drawError(g);

    clipAll(g);
    drawLinesSep(g);
  }

  /**
   * Restricts the graphics to the text area, right of the line numbers.
   * @param g graphics reference
   */
  private void clipText(final Graphics g) {
    final int cx = Math.max(0, sepX() + 1);
    g.setClip(cx, 0, getWidth() - cx, getHeight());
  }

  /**
   * Extends the graphics to the whole panel, including the line numbers.
   * @param g graphics reference
   */
  private void clipAll(final Graphics g) {
    g.setClip(0, 0, getWidth(), getHeight());
  }

  /**
   * Returns the x position of the line number separator.
   * @return position
   */
  private int sepX() {
    return offset - OFFSET * 3 / 2;
  }

  /**
   * Indicates if the current position is at the beginning of a row.
   * @return result of check
   */
  private boolean rowStart() {
    return x == startX;
  }

  /**
   * Renders the current line number.
   * @param g graphics reference
   */
  private void drawLineNumber(final Graphics g) {
    if(edit && showLines) {
      g.setColor(GUIConstants.gray);
      final String string = Integer.toString(line);
      clipAll(g);
      font.draw(g, string, offset - font.stringWidth(string) - (OFFSET << 1), y);
      clipText(g);
    }
  }

  /**
   * Draws the line number separator.
   * @param g graphics reference
   */
  private void drawLinesSep(final Graphics g) {
    if(edit) {
      final int sx = sepX();
      if(showLines) {
        g.setColor(GUIConstants.lightGray);
        g.drawLine(sx, 0, sx, height);
      }
      if(margin > 0) {
        // line margin
        final int lx = offset - hscroll.pos() + font.charWidth(' ') * margin;
        if(lx > sx) {
          g.setColor(GUIConstants.lightGray);
          g.drawLine(lx, 0, lx, height);
        }
      }
    }
  }

  /**
   * Returns the cursor coordinates.
   * @return coordinates
   */
  int[] cursor() {
    return cursor;
  }

  /**
   * Returns the horizontal position of the specified text position.
   * @param pos text position
   * @return position, relative to the text panel
   */
  int x(final int pos) {
    final byte[] txt = text.text();
    int start = pos;
    while(start > 0 && txt[start - 1] != '\n') start--;
    return Math.max(0, offset - hscroll.pos() + width(start, pos));
  }

  /**
   * Returns the pixel width of the specified text range.
   * @param start start position
   * @param end end position
   * @return width
   */
  int width(final int start, final int end) {
    return font.stringWidth(text.text(), start, end);
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
    final TextIterator iter = init(g, true);
    final int idx = lineIndex(pos);
    if(idx >= 0) position(iter, idx, 0);
    for(; more(iter, g) && iter.pos() < pos; next(iter));
    return y;
  }

  /**
   * Returns the line and column of the current caret position.
   * @return line and column
   */
  int[] caretPos() {
    computeHeight();

    final TextIterator iter = new TextIterator(text);
    final int c = iter.caret(), idx = lineIndex(c);
    int ln = Math.max(idx, 0) + 1, col = 1;
    if(idx >= 0) iter.pos(cache.pos(idx));
    while(iter.pos() < c) {
      if(iter.next() == '\n') { ln++; col = 1; }
      else col++;
    }
    return new int[] { ln, col };
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
    return new Dimension(Math.max(x, maxX) + Math.max(OFFSET, font.charWidth(' ')), y + fontHeight);
  }

  /**
   * Initializes the renderer.
   * @param g graphics reference (can be {@code null})
   * @param start start at beginning of text or at current scroll position
   * @return text iterator
   */
  private TextIterator init(final Graphics g, final boolean start) {
    font = new TextFont(getFont(), opts.indent(), this);
    setStyle(Font.PLAIN);
    syntax.init(GUIConstants.textColor);

    offset = OFFSET;
    if(g != null && edit && showLines) {
      offset += font.stringWidth(Integer.toString(text.lines())) + (OFFSET << 1);
    }
    startX = offset - (start ? 0 : hscroll.pos());
    x = startX;
    y = fontHeight - (start ? 0 : scroll.pos()) - 2;
    lineY = y - (fontHeight << 2) / 5;
    line = 1;
    link = false;
    caretStart = -1;

    return new TextIterator(text);
  }

  /**
   * Computes the height of the text and updates the scroll bar.
   */
  void computeHeight() {
    width = getWidth() - OFFSET;
    // text and width unchanged: only refresh the derived height and scroll extent
    if(cache.built(text.text(), cacheWidth())) {
      height = getHeight() + fontHeight;
      scroll.extent(cache.endY() + OFFSET);
      hscroll.extent(textWidth());
      return;
    }

    // no graphics reference: the layout cannot be computed, and the cache must not be updated
    final Graphics g = getGraphics();
    if(g == null) return;

    final byte[] txt = text.text();
    final TextIterator iter = init(g, true);
    // try to resume from the edited line
    final int r0 = cache.beginUpdate(txt, cacheWidth(), offset);
    int endY;
    if(r0 < 0) {
      cache.reset();
      cache.add(y, 0, syntax.state());
      while(more(iter, g)) {
        // advance the highlighter state so it can be restored when rendering resumes mid-document
        syntax.getColor(iter);
        if(next(iter)) {
          // the line that was left behind ends at the remembered row position
          cache.lineWidth(rowX - offset);
          cache.add(y, iter.posEnd(), syntax.state());
        }
      }
      cache.lineWidth(x - offset);
      endY = y;
    } else {
      // resume at the first changed line
      final int sp = cache.startPos();
      final int[] st = cache.startState();
      y = cache.startY();
      lineY = y - (fontHeight << 2) / 5;
      line = r0 + 1;
      iter.pos(sp);
      iter.posEnd(sp);
      syntax.state(st);
      cache.add(y, sp, st);

      endY = -1;
      while(more(iter, g)) {
        syntax.getColor(iter);
        if(next(iter)) {
          final int p = iter.posEnd();
          final int[] state = syntax.state();
          cache.lineWidth(rowX - offset);
          // stop as soon as the layout re-converges with the unchanged tail
          if(cache.splice(p, y, state)) { endY = cache.endY(); break; }
          cache.add(y, p, state);
        }
      }
      // no convergence: the edit reached the end of the document
      if(endY < 0) {
        cache.lineWidth(x - offset);
        endY = y;
      }
    }
    cache.finish(txt, cacheWidth(), offset, endY);
    height = getHeight() + fontHeight;
    scroll.extent(endY + OFFSET);
    hscroll.extent(textWidth());
    marks();
  }

  /**
   * Returns the total width of the rendered text, including the borders.
   * @return width, or {@code 0} if long lines are wrapped
   */
  private int textWidth() {
    return wrap ? 0 : offset + cache.maxWidth() + OFFSET;
  }

  /**
   * Indicates if long lines are wrapped.
   * @return result of check
   */
  boolean wrap() {
    return wrap;
  }

  /**
   * Returns the width the line cache is built for.
   * @return width, or {@code -1} if the layout is independent of the width of the panel
   */
  private int cacheWidth() {
    return wrap ? width : -1;
  }

  /**
   * Assigns the positions of the search hits to the scroll bar.
   * The hits are mapped to the document-space y of their line, the axis of the slider.
   */
  void marks() {
    final IntList starts = text.searchResults()[0], ys = new IntList();
    // a stale line cache yields no positions; the next layout will assign them
    if(cache.valid(text.size(), cacheWidth())) {
      final int ss = starts.size(), cs = cache.size();
      // one marker per line: the number of hits in a line is unbounded
      for(int s = 0; s < ss;) {
        final int idx = cache.indexByPos(starts.get(s));
        final boolean last = idx + 1 == cs;
        // a wrapped line extends over several rows
        final int top = lineTop(idx), end = last ? top + fontHeight : lineTop(idx + 1);
        for(int y2 = top; y2 < end; y2 += fontHeight) ys.add(y2);
        if(last) break;
        // continue with the first hit of the next line
        final int next = starts.sortedIndexOf(cache.pos(idx + 1));
        s = next < 0 ? -next - 1 : next;
      }
    }
    scroll.marks(ys);
  }

  /**
   * Returns the document-space y of the first rendered row of the specified line.
   * @param idx line index
   * @return y
   */
  private int lineTop(final int idx) {
    return Math.max(0, cache.y(idx) - fontHeight);
  }

  /**
   * Positions the iterator at the first text line at or above the viewport, using the
   * line-offset cache, so only the visible region is rendered.
   * @param iter text iterator
   */
  private void skip(final TextIterator iter) {
    if(!cache.positionable(cacheWidth())) return;
    final int top = scroll.pos();
    final int idx = cache.indexByY(top);
    final int p = cache.pos(idx);
    // trust a stale cache only if the pending edit (at the caret) is not above this line
    if(p > iter.caret() && !cache.valid(text.size(), cacheWidth())) return;
    position(iter, idx, -top);
    // restore the highlighter state captured for this line so colors resume correctly
    syntax.state(cache.state(idx));
  }

  /**
   * Returns the index of the cached line containing the specified text position, or {@code -1}
   * if the cache is missing or stale.
   * @param pos text position
   * @return line index, or {@code -1}
   */
  private int lineIndex(final int pos) {
    return cache.valid(text.size(), cacheWidth()) ? cache.indexByPos(pos) : -1;
  }

  /**
   * Positions the iterator and renderer at the start of the specified cached line.
   * @param iter text iterator
   * @param idx cached line index
   * @param dy vertical offset added to the line's document-space y (e.g. {@code -scroll})
   */
  private void position(final TextIterator iter, final int idx, final int dy) {
    line = idx + 1;
    y = cache.y(idx) + dy;
    lineY = y - (fontHeight << 2) / 5;
    x = startX;
    final int p = cache.pos(idx);
    iter.pos(p);
    iter.posEnd(p);
  }

  /**
   * Returns the current vertical cursor position.
   * @return new position
   */
  int cursorY() {
    final Graphics g = getGraphics();
    final TextIterator iter = init(g, true);
    toCaretRow(iter, g);
    return y - fontHeight;
  }

  /**
   * Moves the iterator to the rendered row with the caret.
   * @param iter text iterator
   * @param g graphics reference (can be {@code null})
   */
  private void toCaretRow(final TextIterator iter, final Graphics g) {
    final int idx = lineIndex(iter.caret());
    if(idx >= 0) position(iter, idx, 0);
    for(; more(iter, g) && !iter.edited(); next(iter));
    // the caret is rendered at the end of the previous row: adopt that row
    if(atRowEnd(iter)) {
      x = rowX;
      y = rowY;
      lineY = rowLineY;
    }
  }

  /**
   * Checks if the text has more words to print.
   * @param iter iterator
   * @param g graphics reference (can be {@code null})
   * @return {@code true}} if more strings exist
   */
  private boolean more(final TextIterator iter, final Graphics g) {
    wrapped = false;
    // no valid graphics reference, no more words found: quit
    final int w = width, maxWidth = w - offset;
    if(g == null || maxWidth <= 0 || !iter.moreStrings(w >> 2)) return false;

    final int oldY = y;
    int sw = 0;

    final int cp = iter.curr();
    if(cp == TokenBuilder.BOLD) {
      setStyle(Font.BOLD);
    } else if(cp == TokenBuilder.NORM) {
      setStyle(Font.PLAIN);
    } else if(cp == TokenBuilder.ULINE) {
      link ^= true;
    } else {
      // compute string width, shorten if it exceeds panel width
      sw = font.stringWidth(iter.text(), iter.pos(), iter.posEnd());
      if(wrap && sw > maxWidth) {
        if(!rowStart()) newline(true);

        // keep the longest prefix of the token that fits into the row
        final byte[] txt = iter.text();
        final int start = iter.pos(), end = iter.posEnd();
        int p = start;
        sw = 0;
        for(; p < end; p += Token.cl(txt, p)) {
          final int cw = font.charWidth(Token.cp(txt, p));
          if(sw + cw >= maxWidth) break;
          sw += cw;
        }
        if(p == start) return false;
        iter.posEnd(p);
      }
    }
    // no space left: move current string into next line
    if(wrap && sw < maxWidth && sw > w - x) newline(true);

    wrapped = y != oldY;
    stringWidth = sw;
    return true;
  }

  /**
   * Jumps to the next line.
   * @param full add full line height
   */
  private void newline(final boolean full) {
    final int h = fontHeight >> (full ? 0 : 1);
    // remember the end of the row that is left behind
    rowX = x;
    rowY = y;
    rowLineY = lineY;
    x = startX;
    y += h;
    lineY += h;
  }

  /**
   * Marks the current line if it contains the cursor.
   * @param iter iterator
   * @param g graphics reference
   */
  private void markLine(final TextIterator iter, final Graphics g) {
    if(!edit || !markline) return;
    if(caretStart == -1) {
      // locate the boundaries of the line with the cursor
      final byte[] txt = iter.text();
      final int tl = txt.length;
      int s = iter.caret(), e = s;
      while(s > 0 && txt[s - 1] != '\n') s--;
      while(e < tl && txt[e] != '\n') e++;
      caretStart = s;
      caretEnd = e;
    }
    final int pos = iter.pos();
    if(pos >= caretStart && pos <= caretEnd) {
      g.setColor(GUIConstants.color3A);
      clipAll(g);
      g.fillRect(0, lineY, getWidth(), fontHeight);
      clipText(g);
    }
  }

  /**
   * Marks the current line as erroneous.
   * @param g graphics reference
   */
  private void markErrorLine(final Graphics g) {
    g.setColor(GUIConstants.colormark2A);
    clipAll(g);
    g.fillRect(0, lineY, sepX(), fontHeight);
    clipText(g);
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
    if(rowStart()) markLine(iter, g);

    // advance the highlighter, and choose color for enabled text, depending on highlighting or link
    final Color syntaxColor = syntax.getColor(iter);
    final Color color = isEnabled() ? markNext ? GUIConstants.green : link ?
      GUIConstants.color4 : syntaxColor : GUIConstants.gray;
    final int cp = iter.curr();
    markNext = cp == TokenBuilder.MARK;

    // retrieve first character of current token
    final int pos = iter.pos(), cpos = iter.caret();

    // handle matching parentheses; ignore brackets in strings, comments and element content
    final boolean code = syntax.codeBefore() || syntax.codeAfter();
    final int opening = code ? Syntax.OPENING.indexOf(cp) : -1;
    final int closing = code ? Syntax.CLOSING.indexOf(cp) : -1;
    if(opening != -1 || closing != -1) {
      // a bracket at the caret is highlighted even if its counterpart is not rendered
      final boolean marked = cpos == pos || cpos == pos + 1;
      if(marked) drawBracket(g, x, lineY, cp);
      if(opening != -1) {
        parentheses.add(x).add(lineY).add(pos).add(cp);
      } else if(!parentheses.isEmpty() && parentheses.peek() == Syntax.OPENING.charAt(closing)) {
        final int open = parentheses.pop();
        final int cr = parentheses.pop(), yy = parentheses.pop(), xx = parentheses.pop();
        // highlight the counterpart of the bracket at the caret
        if(marked) drawBracket(g, xx, yy, open);
        else if(cpos == cr || cpos == cr + 1) drawBracket(g, x, lineY, cp);
      }
    }

    // check if text is visible
    if(y > 0 && x <= width && x + stringWidth >= offset) {
      // mark repeated, selected and found text
      for(final int[] oc : iter.occurrences()) mark(oc, iter, g, GUIConstants.color3A);
      mark(iter.selection(), iter, g, GUIConstants.color2A);
      for(final int[] sr : iter.searchResults()) mark(sr, iter, g, GUIConstants.color2A);

      // retrieve first character of current token
      if(iter.error()) drawError(g);

      if(showNL && cp == TokenBuilder.NLINE) {
        // draw newline character
        g.setColor(GUIConstants.gray);
        font.draw(g, "\u00b6", x, y);
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
          font.draw(g, iter.currString(), x, y);
        }
      }
      // underline linked text
      if(link) g.drawLine(x, y + 1, x + stringWidth, y + 1);
      // show cursor: a wrapped token shares its first position with the end of the previous row
      if(caret && iter.edited()) {
        if(atRowEnd(iter)) drawCaret(g, rowX, rowLineY);
        else drawCaret(g, x + font.stringWidth(iter.text(), pos, cpos), lineY);
      }
    }

    // finish step
    next(iter);
  }

  /**
   * Highlights text.
   * @param range start/end of mark
   * @param iter iterator
   * @param g graphics reference
   * @param color color of the highlighting
   */
  private void mark(final int[] range, final TextIterator iter, final Graphics g,
      final Color color) {
    if(range != null) {
      final int pos = iter.pos(), posEnd = iter.posEnd();
      final int ss = Math.max(pos, range[0]), se = Math.min(posEnd, range[1]);
      final int xs = font.stringWidth(iter.text(), pos, ss);
      final int cw = font.stringWidth(iter.text(), ss, se);
      g.setColor(color);
      g.fillRect(x + xs, lineY, cw, fontHeight);
    }
  }

  /**
   * Indicates if the caret is to be rendered at the end of the previous row.
   * @param iter text iterator
   * @return result of check
   */
  private boolean atRowEnd(final TextIterator iter) {
    return wrapped && iter.rowEnd() && iter.pos() == iter.caret();
  }

  /**
   * Highlights a bracket.
   * @param g graphics reference
   * @param xx x position
   * @param yy y position
   * @param bracket bracket character
   */
  private void drawBracket(final Graphics g, final int xx, final int yy, final int bracket) {
    g.setColor(GUIConstants.color4);
    g.drawRect(xx, yy, font.charWidth(bracket), fontHeight);
  }

  /**
   * Paints the text cursor.
   * @param g graphics reference
   * @param xx x position
   * @param yy y position
   */
  private void drawCaret(final Graphics g, final int xx, final int yy) {
    g.setColor(GUIConstants.darkGray);
    g.fillRect(xx, yy, 2, fontHeight);
    cursor[0] = xx;
    cursor[1] = yy + fontHeight;
  }

  /**
   * Draws an error marker.
   * @param g graphics reference
   */
  private void drawError(final Graphics g) {
    final int ww = stringWidth == 0 ? font.charWidth(' ') : stringWidth;
    final int s = Math.max(2, fontHeight / 6);
    g.setColor(GUIConstants.red);
    for(int xp = x; xp < x + ww; xp += 2) g.drawLine(xp - 1, y + 2, xp, y + s + 1);
    if(edit) markErrorLine(g);
  }

  /**
   * Jumps to the text at the specified position.
   * @param pos mouse position
   * @return text iterator
   */
  TextIterator jump(final Point pos) {
    final Graphics g = getGraphics();
    final TextIterator iter = init(g, false);
    scan(iter, g, pos.x, pos.y - fontHeight / 5);
    iter.link(link);
    return iter;
  }

  /**
   * Moves the iterator to the text at the specified coordinates.
   * @param iter text iterator
   * @param g graphics reference (can be {@code null})
   * @param xPos x position
   * @param yPos y position (top of the rendered row)
   */
  private void scan(final TextIterator iter, final Graphics g, final int xPos, final int yPos) {
    for(; yPos >= y - fontHeight && more(iter, g); next(iter)) {
      // skip row
      if(yPos >= y) continue;
      // beginning of row
      if(xPos < x) break;
      // token found
      if(xPos < x + stringWidth) {
        final int p = iter.pos(), sw = xPos - x;
        for(int caretP, oldFsw = 0; iter.more();) {
          caretP = iter.pos();
          iter.next();
          final int fsw = font.stringWidth(iter.text(), p, iter.pos());
          if(sw < fsw) {
            if(sw < oldFsw + (fsw - oldFsw) / 2) iter.pos(caretP);
            break;
          }
          oldFsw = fsw;
        }
        break;
      }
    }
    // the scan walked past the target row: its last position is shared with the next row
    rowEnd = y - fontHeight > yPos;
  }

  /**
   * Returns the caret position that is the specified number of rendered rows away from the
   * current one.
   * @param count number of rows (negative: upwards)
   * @param lastX preferred x position, or {@code -1}
   * @return caret and x position, or {@code null} if the text has not been rendered yet
   */
  int[] caretRows(final int count, final int lastX) {
    final Graphics g = getGraphics();
    final TextIterator iter = caretIter(g);
    if(iter == null) return null;

    // x position of the caret, and top of the target row
    final int cpos = iter.caret();
    final int xPos = lastX != -1 ? lastX : x + font.stringWidth(iter.text(), iter.pos(), cpos);
    final int yPos = y - fontHeight + count * fontHeight;
    return new int[] { scan(g, xPos, yPos).pos(), xPos };
  }

  /**
   * Moves the text horizontally.
   * @param dx pixels to move (negative: to the left)
   */
  void moveX(final int dx) {
    if(wrap) return;
    hscroll.pos(hscroll.pos() + dx);
    repaint();
  }

  /**
   * Moves the text horizontally to make the cursor visible.
   */
  void scrollX() {
    if(wrap) return;
    final Graphics g = getGraphics();
    final TextIterator iter = caretIter(g);
    if(iter == null) return;
    // horizontal position of the cursor in the text (the rendering starts at the left border)
    final int cx = x + font.stringWidth(iter.text(), iter.pos(), iter.caret());
    hscroll.pos(Math.min(Math.max(hscroll.pos(), cx - width), cx - offset));
  }

  /**
   * Returns the caret position at the beginning or end of the rendered row with the caret.
   * @param end end of row
   * @return caret position, or {@code -1} if the text has not been rendered yet
   */
  int caretRow(final boolean end) {
    final Graphics g = getGraphics();
    if(caretIter(g) == null) return -1;
    final int yPos = y - fontHeight;
    return scan(g, end ? Integer.MAX_VALUE : 0, yPos).pos();
  }

  /**
   * Initializes the renderer and moves the iterator to the rendered row with the caret.
   * @param g graphics reference (can be {@code null})
   * @return text iterator, or {@code null} if the text has not been rendered yet
   */
  private TextIterator caretIter(final Graphics g) {
    if(g == null) return null;
    final TextIterator iter = init(g, true);
    if(width - offset <= 0) return null;
    toCaretRow(iter, g);
    return iter;
  }

  /**
   * Initializes the renderer and moves the iterator to the text at the specified coordinates.
   * @param g graphics reference
   * @param xPos x position
   * @param yPos y position (top of the rendered row)
   * @return text iterator
   */
  private TextIterator scan(final Graphics g, final int xPos, final int yPos) {
    final TextIterator iter = init(g, true);
    if(cache.valid(text.size(), cacheWidth())) position(iter, cache.indexByY(yPos), 0);
    scan(iter, g, xPos, yPos);
    return iter;
  }

  /**
   * Indicates if the last scan stopped at the end of a rendered row.
   * @return result of check
   */
  boolean rowEnd() {
    return rowEnd;
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
  void syntax(final Syntax s) {
    if(syntax != s) cache.reset();
    syntax = s;
  }

  /**
   * Returns the syntax highlighter.
   * @return syntax highlighter
   */
  Syntax syntax() {
    return syntax;
  }
}
