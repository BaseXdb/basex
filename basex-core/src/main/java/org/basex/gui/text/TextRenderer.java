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
  /** Graphics configuration of the font (can be {@code null}). */
  private GraphicsConfiguration fontConfig;
  /** Font height. */
  private int fontHeight;
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
  /** Area to be repainted. */
  private Rectangle clip = new Rectangle();

  /** Border offset. */
  private int offset;
  /** Width of total text area. */
  private int width;
  /** Height of total text area. */
  private int height;
  /** Indicates if the last scan stopped at the end of a row. */
  private boolean rowEnd;

  /** Line-offset cache (maps document-space y or text position to a line). */
  private final TextLineCache cache = new TextLineCache();
  /** Cursor position. */
  private final int[] cursor = new int[2];

  /** Vertical start position. */
  private Syntax syntax = Syntax.SIMPLE;
  /** Visibility of text cursor. */
  private boolean caret;

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

    // the font and its caches are reused until the font, the indentation or the screen changes
    font(f);
    // text that cannot be edited is always wrapped: it has no horizontal scrolling
    wrap = !edit || opts.get(GUIOptions.WORDWRAP);
    if(wrap) hscroll.pos(0);
    // editing aids are cleared for text that cannot be edited
    margin = edit ? opts.margin() : 0;
    showInvisible = edit && opts.get(GUIOptions.SHOWINVISIBLE);
    showNL = edit && opts.get(GUIOptions.SHOWNL);
    showLines = edit && opts.get(GUIOptions.SHOWLINES);
    markline = edit && opts.get(GUIOptions.MARKLINE);
    antiAlias = opts.get(GUIOptions.ANTIALIAS);
    repaint();
  }

  /**
   * Creates the text font.
   * @param f font
   */
  private void font(final Font f) {
    font = new TextFont(f, opts.indent(), this);
    fontConfig = getGraphicsConfiguration();
  }

  @Override
  public void paintComponent(final Graphics g) {
    super.paintComponent(g);
    BaseXLayout.antiAlias(g, antiAlias);
    // rows outside the requested area are laid out, but not drawn
    final Rectangle bounds = g.getClipBounds();
    clip = bounds != null ? bounds : new Rectangle(getWidth(), getHeight());

    parentheses.reset();
    final Layout layout = init(true, false);
    final TextIterator iter = layout.iter;
    clipText(g);
    skip(layout);
    int oldL = layout.line - 1;
    while(more(layout) && layout.y < height) {
      if(layout.line != oldL && layout.y >= 0) {
        drawLineNumber(layout, g);
        oldL = layout.line;
      }
      write(layout, g);
    }
    if(rowStart(layout)) markLine(layout, g);
    if(layout.line != oldL) drawLineNumber(layout, g);

    layout.stringWidth = 0;
    final int s = iter.pos();
    if(caret && s == iter.caret()) drawCaret(g, layout.x, layout.lineY);
    if(s == iter.errorPos()) drawError(layout, g);

    clipAll(g);
    drawLinesSep(g);
  }

  /**
   * Restricts the graphics to the text area, right of the line numbers.
   * @param g graphics reference
   */
  private void clipText(final Graphics g) {
    clipAll(g);
    g.clipRect(Math.max(0, sepX() + 1), 0, getWidth(), getHeight());
  }

  /**
   * Extends the graphics to the area that was requested to be repainted.
   * @param g graphics reference
   */
  private void clipAll(final Graphics g) {
    g.setClip(clip);
  }

  /**
   * Indicates if the current row intersects the area to be repainted.
   * @param layout layout
   * @return result of check
   */
  private boolean rowVisible(final Layout layout) {
    return layout.lineY < clip.y + clip.height && layout.lineY + fontHeight > clip.y;
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
   * @param layout layout
   * @return result of check
   */
  private boolean rowStart(final Layout layout) {
    return layout.x == layout.startX;
  }

  /**
   * Renders the current line number.
   * @param layout layout
   * @param g graphics reference
   */
  private void drawLineNumber(final Layout layout, final Graphics g) {
    if(showLines && rowVisible(layout)) {
      g.setColor(GUIConstants.gray);
      final String string = Integer.toString(layout.line);
      clipAll(g);
      font.draw(g, string, offset - font.stringWidth(string) - (OFFSET << 1), layout.y);
      clipText(g);
    }
  }

  /**
   * Draws the line number separator.
   * @param g graphics reference
   */
  private void drawLinesSep(final Graphics g) {
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

    final Layout layout = init(renderable(), true);
    final int idx = lineIndex(pos);
    if(idx >= 0) position(layout, idx, 0);
    for(; more(layout) && layout.iter.pos() < pos; next(layout));
    return layout.y;
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
    final int pad = Math.max(OFFSET, font.charWidth(' '));
    // the line cache holds the size of the rendered text (wrapped text: only its height)
    if(!wrap && cache.valid(text.size(), cacheWidth())) {
      return new Dimension(offset + cache.maxWidth() + pad, cache.endY() + fontHeight);
    }

    // calculate size required for the currently rendered text
    final int w = width, h = height;
    width = Integer.MAX_VALUE;
    height = Integer.MAX_VALUE;
    try {
      int maxX = 0;
      final Layout layout = init(renderable(), true);
      for(; more(layout); next(layout)) {
        if(layout.iter.curr() == TokenBuilder.NLINE) maxX = Math.max(layout.x, maxX);
      }
      return new Dimension(Math.max(layout.x, maxX) + pad, layout.y + fontHeight);
    } finally {
      // the dimensions of the panel must not be invalidated by the calculation
      width = w;
      height = h;
    }
  }

  /**
   * Initializes the renderer.
   * @param render indicates if the text can be laid out
   * @param start start at beginning of text or at current scroll position
   * @return layout
   */
  private Layout init(final boolean render, final boolean start) {
    // character widths depend on the screen: refresh the font if the panel was shown or moved
    final GraphicsConfiguration gc = getGraphicsConfiguration();
    if(gc != null && gc != fontConfig) {
      font(getFont());
      cache.reset();
    }
    setStyle(Font.PLAIN);
    syntax.init(GUIConstants.textColor);

    offset = OFFSET;
    if(render && showLines) {
      offset += font.stringWidth(Integer.toString(text.lines())) + (OFFSET << 1);
    }
    final Layout layout = new Layout(new TextIterator(text), render);
    layout.startX = offset - (start ? 0 : hscroll.pos());
    layout.x = layout.startX;
    layout.y = fontHeight - (start ? 0 : scroll.pos()) - 2;
    layout.lineY = layout.y - (fontHeight << 2) / 5;
    return layout;
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

    // text that cannot be laid out: the cache must not be updated
    if(!renderable()) return;

    final byte[] txt = text.text();
    final Layout layout = init(true, true);
    final TextIterator iter = layout.iter;
    // try to resume from the edited line
    final int r0 = cache.beginUpdate(txt, cacheWidth(), offset);
    int endY;
    if(r0 < 0) {
      cache.reset();
      cache.add(layout.y, 0, syntax.state());
      while(more(layout)) {
        // advance the highlighter state so it can be restored when rendering resumes mid-document
        syntax.getColor(iter);
        if(next(layout)) {
          // the line that was left behind ends at the remembered row position
          cache.lineWidth(layout.rowX - offset);
          cache.add(layout.y, iter.posEnd(), syntax.state());
        }
      }
      cache.lineWidth(layout.x - offset);
      endY = layout.y;
    } else {
      // resume at the first changed line
      final int sp = cache.startPos();
      final int[] st = cache.startState();
      layout.y = cache.startY();
      layout.lineY = layout.y - (fontHeight << 2) / 5;
      layout.line = r0 + 1;
      iter.pos(sp);
      iter.posEnd(sp);
      syntax.state(st);
      cache.add(layout.y, sp, st);

      endY = -1;
      while(more(layout)) {
        syntax.getColor(iter);
        if(next(layout)) {
          final int p = iter.posEnd();
          final int[] state = syntax.state();
          cache.lineWidth(layout.rowX - offset);
          // stop as soon as the layout re-converges with the unchanged tail
          if(cache.splice(p, layout.y, state)) { endY = cache.endY(); break; }
          cache.add(layout.y, p, state);
        }
      }
      // no convergence: the edit reached the end of the document
      if(endY < 0) {
        cache.lineWidth(layout.x - offset);
        endY = layout.y;
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
   * @param layout layout
   */
  private void skip(final Layout layout) {
    if(!cache.positionable(cacheWidth())) return;
    final int top = scroll.pos();
    final int idx = cache.indexByY(top);
    final int p = cache.pos(idx);
    // trust a stale cache only if the pending edit (at the caret) is not above this line
    if(p > layout.iter.caret() && !cache.valid(text.size(), cacheWidth())) return;
    position(layout, idx, -top);
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
   * Positions the iterator and the layout at the start of the specified cached line.
   * @param layout layout
   * @param idx cached line index
   * @param dy vertical offset added to the line's document-space y (e.g. {@code -scroll})
   */
  private void position(final Layout layout, final int idx, final int dy) {
    layout.line = idx + 1;
    layout.y = cache.y(idx) + dy;
    layout.lineY = layout.y - (fontHeight << 2) / 5;
    layout.x = layout.startX;
    final int p = cache.pos(idx);
    layout.iter.pos(p);
    layout.iter.posEnd(p);
  }

  /**
   * Returns the text position of the topmost visible line.
   * @return text position, or {@code -1} if the line cache cannot be used
   */
  int topPos() {
    return cache.positionable(cacheWidth()) ?
      cache.pos(cache.indexByY(scroll.pos() + fontHeight)) : -1;
  }

  /**
   * Returns the scroll position that moves the specified line to the top of the viewport.
   * @param pos text position ({@code -1} for none)
   * @return scroll position, or the current one if the line cache cannot be used
   */
  int topY(final int pos) {
    return pos >= 0 && cache.positionable(cacheWidth()) ? lineTop(cache.indexByPos(pos)) :
      scroll.pos();
  }

  /**
   * Returns the current vertical cursor position.
   * @return new position
   */
  int cursorY() {
    final Layout layout = init(renderable(), true);
    toCaretRow(layout);
    return layout.y - fontHeight;
  }

  /**
   * Returns the vertical position below the rendered row with the caret.
   * @return position, relative to the text panel, or {@code -1} if the text has not been rendered
   */
  int cursorBottom() {
    final Layout layout = caretLayout();
    return layout == null ? -1 : layout.lineY + fontHeight - scroll.pos();
  }

  /**
   * Moves the iterator to the rendered row with the caret.
   * @param layout layout
   */
  private void toCaretRow(final Layout layout) {
    final TextIterator iter = layout.iter;
    final int idx = lineIndex(iter.caret());
    if(idx >= 0) position(layout, idx, 0);
    for(; more(layout) && !iter.edited(); next(layout));
    // the caret is rendered at the end of the previous row: adopt that row
    if(atRowEnd(layout)) {
      layout.x = layout.rowX;
      layout.y = layout.rowY;
      layout.lineY = layout.rowLineY;
    }
  }

  /**
   * Checks if the text has more words to print.
   * @param layout layout
   * @return {@code true}} if more strings exist
   */
  private boolean more(final Layout layout) {
    final TextIterator iter = layout.iter;
    layout.wrapped = false;
    // text that cannot be laid out, no more words found: quit
    final int w = width, maxWidth = w - offset;
    if(!layout.renderable || maxWidth <= 0 || !iter.moreStrings(w >> 2)) return false;

    final int oldY = layout.y;
    int sw = 0;

    final int cp = iter.curr();
    if(cp == TokenBuilder.BOLD) {
      setStyle(Font.BOLD);
    } else if(cp == TokenBuilder.NORM) {
      setStyle(Font.PLAIN);
    } else if(cp == TokenBuilder.ULINE) {
      layout.link ^= true;
    } else {
      // compute string width, shorten if it exceeds panel width
      sw = font.stringWidth(iter.text(), iter.pos(), iter.posEnd());
      if(wrap && sw > maxWidth) {
        if(!rowStart(layout)) newline(layout, true);

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
    if(wrap && sw < maxWidth && sw > w - layout.x) newline(layout, true);

    layout.wrapped = layout.y != oldY;
    layout.stringWidth = sw;
    return true;
  }

  /**
   * Jumps to the next line.
   * @param layout layout
   * @param full add full line height
   */
  private void newline(final Layout layout, final boolean full) {
    final int h = fontHeight >> (full ? 0 : 1);
    // remember the end of the row that is left behind
    layout.rowX = layout.x;
    layout.rowY = layout.y;
    layout.rowLineY = layout.lineY;
    layout.x = layout.startX;
    layout.y += h;
    layout.lineY += h;
  }

  /**
   * Marks the current line if it contains the cursor.
   * @param layout layout
   * @param g graphics reference
   */
  private void markLine(final Layout layout, final Graphics g) {
    if(!markline) return;
    final TextIterator iter = layout.iter;
    if(layout.caretStart == -1) {
      // locate the boundaries of the line with the cursor
      final byte[] txt = iter.text();
      final int tl = txt.length;
      int s = iter.caret(), e = s;
      while(s > 0 && txt[s - 1] != '\n') s--;
      while(e < tl && txt[e] != '\n') e++;
      layout.caretStart = s;
      layout.caretEnd = e;
    }
    final int pos = iter.pos();
    if(pos >= layout.caretStart && pos <= layout.caretEnd) {
      g.setColor(GUIConstants.color3A);
      clipAll(g);
      g.fillRect(0, layout.lineY, getWidth(), fontHeight);
      clipText(g);
    }
  }

  /**
   * Marks the current line as erroneous.
   * @param layout layout
   * @param g graphics reference
   */
  private void markErrorLine(final Layout layout, final Graphics g) {
    g.setColor(GUIConstants.colormark2A);
    clipAll(g);
    g.fillRect(0, layout.lineY, sepX(), fontHeight);
    clipText(g);
  }

  /**
   * Finishes the current token.
   * @param layout layout
   * @return new line
   */
  private boolean next(final Layout layout) {
    final int ch = layout.iter.curr();
    if(ch == TokenBuilder.NLINE || ch == TokenBuilder.HLINE) {
      newline(layout, ch == TokenBuilder.NLINE);
      layout.line++;
      return true;
    }
    layout.x += layout.stringWidth;
    return false;
  }

  /**
   * Writes the current string to the graphics reference.
   * @param layout layout
   * @param g graphics reference
   */
  private void write(final Layout layout, final Graphics g) {
    final TextIterator iter = layout.iter;
    if(rowStart(layout)) markLine(layout, g);

    // advance the highlighter, and choose color for enabled text, depending on highlighting or link
    final Color syntaxColor = syntax.getColor(iter);
    final Color color = isEnabled() ? layout.markNext ? GUIConstants.green : layout.link ?
      GUIConstants.color4 : syntaxColor : GUIConstants.gray;
    final int cp = iter.curr();
    layout.markNext = cp == TokenBuilder.MARK;

    // retrieve first character of current token
    final int pos = iter.pos(), cpos = iter.caret();
    // position of the current token: it is advanced by the finishing step
    final int x = layout.x, y = layout.y, lineY = layout.lineY, sw = layout.stringWidth;

    // pair brackets in editable code, but not in strings, comments or element content
    final boolean code = edit && (syntax.codeBefore() || syntax.codeAfter());
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
    if(y > 0 && rowVisible(layout) && x <= width && x + sw >= offset) {
      // mark repeated, selected and found text
      if(edit) {
        for(final int[] oc : iter.occurrences()) mark(oc, layout, g, GUIConstants.color3A);
      }
      mark(iter.selection(), layout, g, GUIConstants.color2A);
      for(final int[] sr : iter.searchResults()) mark(sr, layout, g, GUIConstants.color2A);

      // retrieve first character of current token
      if(iter.error()) drawError(layout, g);

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
      } else if(TextFont.control(cp)) {
        // draw control picture
        g.setColor(GUIConstants.gray);
        font.draw(g, String.valueOf((char) TextFont.picture(cp)), x, y);
      } else if(cp > ' ' && cp < TokenBuilder.PRIVATE_START || cp > TokenBuilder.PRIVATE_END) {
        if(showInvisible && Character.isSpaceChar(cp)) {
          // draw whitespace character
          final int s = fontHeight / 12 + 1;
          g.setColor(GUIConstants.gray);
          g.fillRect(x + (sw >> 1), y - fontHeight * 3 / 10, s, s);
        } else {
          // draw non-whitespace string
          g.setColor(color);
          font.draw(g, iter.text(), pos, iter.posEnd(), x, y);
        }
      }
      // underline linked text
      if(layout.link) g.drawLine(x, y + 1, x + sw, y + 1);
      // show cursor: a wrapped token shares its first position with the end of the previous row
      if(caret && iter.edited()) {
        if(atRowEnd(layout)) drawCaret(g, layout.rowX, layout.rowLineY);
        else drawCaret(g, x + font.stringWidth(iter.text(), pos, cpos), lineY);
      }
    }

    // finish step
    next(layout);
  }

  /**
   * Highlights text.
   * @param range start/end of mark (can be {@code null})
   * @param layout layout
   * @param g graphics reference
   * @param color color of the highlighting
   */
  private void mark(final int[] range, final Layout layout, final Graphics g,
      final Color color) {
    if(range != null) {
      final TextIterator iter = layout.iter;
      final int pos = iter.pos(), posEnd = iter.posEnd();
      final int ss = Math.max(pos, range[0]), se = Math.min(posEnd, range[1]);
      final int xs = font.stringWidth(iter.text(), pos, ss);
      final int cw = ss == pos && se == posEnd ? layout.stringWidth :
        font.stringWidth(iter.text(), ss, se);
      g.setColor(color);
      g.fillRect(layout.x + xs, layout.lineY, cw, fontHeight);
    }
  }

  /**
   * Indicates if the caret is to be rendered at the end of the previous row.
   * @param layout layout
   * @return result of check
   */
  private boolean atRowEnd(final Layout layout) {
    final TextIterator iter = layout.iter;
    return layout.wrapped && iter.rowEnd() && iter.pos() == iter.caret();
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
   * @param layout layout
   * @param g graphics reference
   */
  private void drawError(final Layout layout, final Graphics g) {
    final int x = layout.x, y = layout.y, sw = layout.stringWidth;
    final int ww = sw == 0 ? font.charWidth(' ') : sw;
    final int s = Math.max(2, fontHeight / 6);
    g.setColor(GUIConstants.red);
    for(int xp = x; xp < x + ww; xp += 2) g.drawLine(xp - 1, y + 2, xp, y + s + 1);
    if(edit) markErrorLine(layout, g);
  }

  /**
   * Jumps to the text at the specified position.
   * @param pos mouse position
   * @return text iterator
   */
  TextIterator jump(final Point pos) {
    final Layout layout = init(renderable(), false);
    scan(layout, pos.x, pos.y - fontHeight / 5);
    final TextIterator iter = layout.iter;
    iter.link(layout.link);
    return iter;
  }

  /**
   * Moves the iterator to the text at the specified coordinates.
   * @param layout layout
   * @param xPos x position
   * @param yPos y position (top of the rendered row)
   */
  private void scan(final Layout layout, final int xPos, final int yPos) {
    final TextIterator iter = layout.iter;
    for(; yPos >= layout.y - fontHeight && more(layout); next(layout)) {
      // skip row
      if(yPos >= layout.y) continue;
      // beginning of row
      if(xPos < layout.x) break;
      // token found
      if(xPos < layout.x + layout.stringWidth) {
        final int p = iter.pos(), sw = xPos - layout.x;
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
    rowEnd = layout.y - fontHeight > yPos;
  }

  /**
   * Returns the caret position that is the specified number of rendered rows away from the
   * current one.
   * @param count number of rows (negative: upwards)
   * @param lastX preferred x position, or {@code -1}
   * @return caret and x position, or {@code null} if the text has not been rendered yet
   */
  int[] caretRows(final int count, final int lastX) {
    final Layout layout = caretLayout();
    if(layout == null) return null;

    // x position of the caret, and top of the target row
    final TextIterator iter = layout.iter;
    final int cpos = iter.caret();
    final int xPos = lastX != -1 ? lastX :
      layout.x + font.stringWidth(iter.text(), iter.pos(), cpos);
    final int yPos = layout.y - fontHeight + count * fontHeight;
    return new int[] { scan(xPos, yPos).iter.pos(), xPos };
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
    final Layout layout = caretLayout();
    if(layout == null) return;
    // horizontal position of the cursor in the text (the rendering starts at the left border)
    final TextIterator iter = layout.iter;
    final int cx = layout.x + font.stringWidth(iter.text(), iter.pos(), iter.caret());
    hscroll.pos(Math.min(Math.max(hscroll.pos(), cx - width), cx - offset));
  }

  /**
   * Returns the caret position at the beginning or end of the rendered row with the caret.
   * @param end end of row
   * @return caret position, or {@code -1} if the text has not been rendered yet
   */
  int caretRow(final boolean end) {
    final Layout layout = caretLayout();
    if(layout == null) return -1;
    final int yPos = layout.y - fontHeight;
    return scan(end ? Integer.MAX_VALUE : 0, yPos).iter.pos();
  }

  /**
   * Initializes the renderer and moves the iterator to the rendered row with the caret.
   * @return layout, or {@code null} if the text has not been rendered yet
   */
  private Layout caretLayout() {
    if(!renderable()) return null;
    final Layout layout = init(true, true);
    if(width - offset <= 0) return null;
    toCaretRow(layout);
    return layout;
  }

  /**
   * Initializes the renderer and moves the iterator to the text at the specified coordinates.
   * @param xPos x position
   * @param yPos y position (top of the rendered row)
   * @return layout
   */
  private Layout scan(final int xPos, final int yPos) {
    final Layout layout = init(true, true);
    if(cache.valid(text.size(), cacheWidth())) position(layout, cache.indexByY(yPos), 0);
    scan(layout, xPos, yPos);
    return layout;
  }

  /**
   * Indicates if the last scan stopped at the end of a rendered row.
   * @return result of check
   */
  boolean rowEnd() {
    return rowEnd;
  }

  /**
   * Checks if the text can be laid out, i.e. if the panel provides a graphics reference.
   * @return result of check
   */
  private boolean renderable() {
    final Graphics g = getGraphics();
    if(g == null) return false;
    g.dispose();
    return true;
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
   * Toggles the visibility of the text cursor.
   */
  void blink() {
    caret ^= true;
    // the cursor is repainted at the position it was last rendered at
    repaint(cursor[0], cursor[1] - fontHeight, 2, fontHeight);
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

  /** Position and state of a single traversal of the text. */
  private static final class Layout {
    /** Text iterator. */
    private final TextIterator iter;
    /** Indicates if the text can be laid out. */
    private final boolean renderable;
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
    /** Current line number. */
    private int line = 1;
    /** Width of current string. */
    private int stringWidth;
    /** Indicates if the current token is part of a link. */
    private boolean link;
    /** Color highlighting flag. */
    private boolean markNext;
    /** Start of the line with the cursor ({@code -1}: not computed yet). */
    private int caretStart = -1;
    /** End of the line with the cursor. */
    private int caretEnd;

    /**
     * Constructor.
     * @param iter text iterator
     * @param renderable indicates if the text can be laid out
     */
    private Layout(final TextIterator iter, final boolean renderable) {
      this.iter = iter;
      this.renderable = renderable;
    }
  }
}
