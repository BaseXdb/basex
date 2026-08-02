package org.basex.gui.text;

import static org.basex.util.Token.*;

import java.text.*;
import java.util.*;

import javax.swing.*;
import javax.swing.Timer;

import org.basex.gui.*;
import org.basex.gui.text.SearchBar.*;
import org.basex.util.*;
import org.basex.util.list.*;

/**
 * Provides methods for editing a text that is visualized by the {@link TextPanel}.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class TextEditor {
  /** Case conversions. */
  public enum Case {
    /** Lower case. */ LOWER,
    /** Upper case. */ UPPER,
    /** Title case. */ TITLE
  }

  /** Completion characters. */
  private static final char[] ALLOWED = { ':', '-' };
  /** Delay before a text change triggers a new search (ms). */
  private static final int SEARCH_DELAY = 100;

  /** Editor options. */
  private final EditorOptions opts;
  /** Collapses the searches of consecutive text changes into a single one. */
  private final Timer searchTimer;

  /** Start and end positions of search terms (initially empty). */
  private IntList[] searchResults = { new IntList(), new IntList() };
  /** Index of the current search hit ({@code -1} if none). */
  private int searchHit = -1;
  /** Indicates whether the current search hit was reached by a selecting jump. */
  private boolean searchSelected;
  /** Context of the current search results (can be {@code null}). */
  private SearchContext searchContext;
  /** Context of the most recently started search (can be {@code null}). */
  private SearchContext searchRequest;
  /** Search thread (can be {@code null}). */
  private Thread searchThread;
  /** Id of the most recent search. */
  private long searchId;
  /** Result of the last decoding (accessed by the search thread and the event dispatch thread). */
  private volatile Decoded decoded;

  /** Start position of a text selection ({@code -1} if no text is selected). */
  private int start = -1;
  /** End position of a text selection +1 ({@code -1} if no text is selected). */
  private int end = -1;
  /** Start position of an error highlighting ({@code -1} for no error). */
  private int error = -1;
  /** Text array to be written. */
  private byte[] text = EMPTY;
  /** Number of lines. Required for displaying line numbers. */
  private int lines = -1;
  /** Caret/edit position. */
  private int pos;
  /** Caret position at the end of a rendered row ({@code -1} if the caret is elsewhere). */
  private int rowEndPos = -1;

  /**
   * Constructor.
   * @param opts editor options
   */
  TextEditor(final EditorOptions opts) {
    this.opts = opts;
    searchTimer = new Timer(SEARCH_DELAY, e -> {
      if(searchRequest != null) startSearch(searchRequest, false);
    });
    searchTimer.setRepeats(false);
  }

  /**
   * Sets a new text.
   * @param txt new text
   * @return {@code true} if text has changed
   */
  boolean text(final byte[] txt) {
    if(eq(txt, text)) return false;
    final int tl = txt.length;
    text = txt;
    lines = -1;
    resetSelection();
    // repeat the last requested search, but not before the changes have come to a rest
    if(searchRequest != null) searchTimer.restart();
    if(pos > tl) pos = tl;
    return true;
  }

  /**
   * A decoded text and the byte array it was decoded from (compared by identity).
   * @param text text
   * @param string decoded text
   */
  private record Decoded(byte[] text, String string) { }

  /**
   * Returns the specified text as string; the result of the last call is cached.
   * Two threads may decode the same text at once, but none of them is ever blocked.
   * @param txt text
   * @return decoded text
   */
  private String decode(final byte[] txt) {
    final Decoded dec = decoded;
    if(dec != null && dec.text() == txt) return dec.string();
    final String str = string(txt);
    decoded = new Decoded(txt, str);
    return str;
  }

  /**
   * Sets a new search context and searches at once.
   * @param sc search context (can be {@code null})
   * @param jump jump to next search result
   */
  void search(final SearchContext sc, final boolean jump) {
    searchTimer.stop();
    if(sc != null) startSearch(sc, jump);
  }

  /**
   * Starts a search. It runs in a separate thread; its results are adopted, and accessed,
   * in the event dispatch thread.
   * @param sc search context
   * @param jump jump to next search result
   */
  private void startSearch(final SearchContext sc, final boolean jump) {
    searchRequest = sc;
    // interrupt old search thread
    final Thread old = searchThread;
    if(old != null) old.interrupt();

    // start new search on a snapshot of the current text
    final byte[] txt = text;
    final long id = ++searchId;
    final Thread t = new Thread(() -> {
      try {
        final IntList[] results = sc.search(txt, decode(txt));
        SwingUtilities.invokeLater(() -> {
          // discard the results of a search that has been superseded
          if(id != searchId) return;
          searchResults(results, sc);
          if(sc.bar != null) sc.bar.refresh(this, sc, jump);
        });
      } catch(final Exception ex) {
        // search was interrupted, or failed unexpectedly
        Util.debug(ex);
      }
    });
    t.setDaemon(true);
    t.start();
    searchThread = t;
  }

  /**
   * Adopts the results of a search.
   * @param results start and end positions of the hits (must not be modified)
   * @param sc search context (can be {@code null})
   */
  void searchResults(final IntList[] results, final SearchContext sc) {
    searchResults = results;
    searchHit = -1;
    searchContext = sc;
  }

  /**
   * Returns the context of the last search.
   * @return search context (can be {@code null})
   */
  SearchContext searchContext() {
    return searchContext;
  }

  /**
   * Returns the start and end positions of the search hits.
   * @return positions (must not be modified)
   */
  IntList[] searchResults() {
    return searchResults;
  }

  /**
   * Replaces all hits; a selection restricts the replacement.
   * @param rc replace context
   * @return new range of the selection, or {@code null} if the whole text was replaced
   */
  int[] replace(final ReplaceContext rc) {
    final int[] range = selectionRange();
    rc.caret = pos;
    final int[] rng = rc.replace(searchContext, text, decode(text), range != null ? range[0] : 0,
        range != null ? range[1] : size());
    return range != null ? rng : null;
  }

  /**
   * Returns the current text selection if it restricts a replacement.
   * A selected search hit does not: Replace All must not be reduced to a single hit.
   * @return offsets, or {@code null}
   */
  int[] selectionRange() {
    if(!isSelected()) return null;
    final int s = selMin(), e = selMax();
    final int hit = searchResults[0].sortedIndexOf(s);
    return hit >= 0 && searchResults[1].get(hit) == e ? null : new int[] { s, e };
  }

  /**
   * Replaces the search hit at the caret.
   * @param rc replace context
   * @return selection offsets, or {@code null} if there is no hit
   */
  int[] replaceHit(final ReplaceContext rc) {
    final int hit = caretHit();
    if(hit < 0) return null;
    searchHit = hit;
    return rc.replace(searchContext, text, decode(text), searchResults[0].get(hit),
        searchResults[1].get(hit));
  }

  /**
   * Returns the index of the hit that encloses the caret, or of the first hit after it (wrapping).
   * @return index, or {@code -1} if there are no hits
   */
  int caretHit() {
    final IntList starts = searchResults[0];
    final int sl = starts.size();
    if(sl == 0) return -1;
    final int s = starts.sortedIndexOf(pos);
    if(s >= 0) return s;
    final int i = -s - 1;
    // caret inside the preceding hit; otherwise, next hit, or first one
    return i > 0 && pos < searchResults[1].get(i - 1) ? i - 1 : i == sl ? 0 : i;
  }

  /**
   * Counts the number of lines in the text.
   * @return number of new lines in the text
   */
  int lines() {
    if(lines == -1) {
      int c = 1;
      for(final byte ch : text) {
        if(ch == '\n') ++c;
      }
      lines = c;
    }
    return lines;
  }

  /**
   * Moves one character forward.
   * @param select selection flag
   */
  private void forward(final boolean select) {
    if(select || !isSelected()) {
      next();
    } else {
      pos(selMax());
    }
  }

  /**
   * Moves one character forward.
   * @param select selection flag
   */
  void nextChar(final boolean select) {
    keepSelection(select);
    forward(select);
    if(select) endSelection();
  }

  /**
   * Moves one character back.
   * @param select selection flag
   */
  void prevChar(final boolean select) {
    keepSelection(select);
    back(select);
    if(select) endSelection();
  }

  /**
   * Moves one token forward.
   * @param select selection flag
   */
  void nextWord(final boolean select) {
    keepSelection(select);

    int ch = curr();
    forward(select);
    if(ch != '\n') {
      if(FTToken.lod(ch)) {
        while(FTToken.lod(ch)) ch = next();
      } else if(!FTToken.ws(ch)) {
        while(ch != '\n' && !FTToken.lod(ch) && !FTToken.ws(ch)) ch = next();
      }
      while(ch != '\n' && FTToken.ws(ch)) ch = next();
      if(pos != size()) prev();
    }
    if(select) endSelection();
  }

  /**
   * Moves one token back.
   * @param select selection flag
   */
  void prevWord(final boolean select) {
    keepSelection(select);

    int ch = back(select);
    if(ch != '\n') {
      if(FTToken.lod(ch)) {
        while(FTToken.lod(ch)) ch = prev();
      } else if(FTToken.ws(ch)) {
        while(ch != '\n' && FTToken.ws(ch)) ch = prev();
        while(FTToken.lod(ch)) ch = prev();
      } else {
        while(ch != '\n' && !FTToken.lod(ch) && !FTToken.ws(ch)) ch = prev();
      }
      if(pos != 0) next();
    }
    if(select) endSelection();
  }

  /**
   * Returns the position of first character of the current auto-completion input.
   * @return position
   */
  int completionStart() {
    int p = pos;
    while(p > 0 && completeMore(text[p - 1])) --p;
    // include a dollar sign or angle bracket, which introduce variable and element names
    if(p > 0 && (text[p - 1] == '$' || text[p - 1] == '<')) --p;
    return p;
  }

  /**
   * Returns the position after the last character of the current auto-completion input.
   * @return position
   */
  int completionEnd() {
    int p = pos;
    final int tl = text.length;
    while(p < tl && completeMore(text[p])) ++p;
    return p;
  }

  /**
   * Checks if the specified character is a completion character.
   * @param ch character
   * @return result of check
   */
  private static boolean completeMore(final byte ch) {
    if(letterOrDigit(ch)) return true;
    for(final char a : ALLOWED) {
      if(ch == a) return true;
    }
    return false;
  }

  /**
   * Jumps to the beginning of the text.
   * @param select selection flag
   */
  void textStart(final boolean select) {
    moveTo(0, select);
  }

  /**
   * Jumps to the end of the text.
   * @param select selection flag
   */
  void textEnd(final boolean select) {
    moveTo(size(), select);
  }

  /**
   * Returns the original text array.
   * @return text
   */
  public byte[] text() {
    return text;
  }

  // POSITION =====================================================================================

  /**
   * Moves to the beginning of the line.
   * @param select selection flag
   */
  private void startOfLine(final boolean select) {
    if(pos == 0) {
      if(!select) resetSelection();
      return;
    }
    while(back(select) != '\n');
    if(pos != 0 || curr() == '\n') forward(select);
  }

  /**
   * Returns the column of the caret.
   * @return number of characters that precede the caret in its line
   */
  private int column() {
    int p = pos;
    while(p > 0 && text[p - 1] != '\n') p--;
    final int ind = opts.indent();
    int c = 0;
    for(; p < pos; p += cl(text, p)) c += text[p] == '\t' ? ind : 1;
    return c;
  }

  /**
   * Moves to the beginning of a line.
   * @param select selection flag
   */
  void lineStart(final boolean select) {
    keepSelection(select);

    // find beginning of line
    final int p = pos;
    startOfLine(select);

    // move to first non-whitespace character, or back to line start if cursor was already there
    final int bol = pos;
    while(FTToken.ws(curr()) && curr() != '\n') forward(select);
    if(pos == p) pos = bol;

    if(select) endSelection();
  }

  /**
   * Moves to the end of a line.
   * @param select selection flag
   */
  void lineEnd(final boolean select) {
    startSelection(select);
    forwardTo(Integer.MAX_VALUE, select);
    if(select) endSelection();
  }

  /**
   * Moves one character back and returns the found character.
   * @param select selection flag
   * @return previous character
   */
  private int back(final boolean select) {
    if(select || !isSelected()) return prev();
    pos(selMin());
    return curr();
  }

  /**
   * Moves one character back and returns the found character. A newline character is
   * returned if the cursor is placed at the beginning of the text.
   * @return previous character, or newline
   */
  private int prev() {
    if(pos == 0) return '\n';
    while(--pos > 0 && text[pos] < -64);
    return curr();
  }

  /**
   * Moves to the specified column or the end of the line.
   * @param p column to move to
   * @param select selection flag
   */
  private void forwardTo(final int p, final boolean select) {
    final int ind = opts.indent();
    int nc = 0;
    while(curr() != '\n') {
      nc += curr() == '\t' ? ind : 1;
      if(nc >= p) return;
      forward(select);
    }
  }

  /**
   * Moves the cursor up or down. The current column position is remembered and, if possible,
   * restored.
   * @param l number of lines to move cursor (negative: upwards)
   * @param select selection flag
   */
  void lines(final int l, final boolean select) {
    startSelection(select);

    final int col = column();
    startOfLine(select);
    // skip upward movement at the beginning of the text
    if(l > 0 || pos() != 0) {
      for(int i = 0; i < -l; ++i) {
        back(select);
        startOfLine(select);
      }
      for(int i = 0; i < l; ++i) {
        forwardTo(Integer.MAX_VALUE, select);
        forward(select);
      }
      forwardTo(col, select);
    }
    if(select) endSelection();
  }

  /**
   * Moves to the beginning of a rendered row.
   * @param p first position of the row
   * @param select selection flag
   */
  void rowStart(final int p, final boolean select) {
    int t = p;
    // first row of a line: move to first non-whitespace character, or back to the row start
    if(p == 0 || text[p - 1] == '\n') {
      while(t < size() && text[t] != '\n' && FTToken.ws(text[t])) t++;
      if(t == pos) t = p;
    }
    moveTo(t, select);
  }

  /**
   * Moves to the end of a rendered row.
   * @param p last position of the row
   * @param select selection flag
   */
  void rowEnd(final int p, final boolean select) {
    int t = p;
    // wrapped row: skip trailing whitespace, as the position is shared with the next row
    if(p < size() && text[p] != '\n') {
      while(t > 0 && text[t - 1] != '\n' && FTToken.ws(text[t - 1])) t--;
    }
    moveTo(t, select);
  }

  /**
   * Indicates if the caret is placed at the end of a rendered row. A wrapped row shares this
   * position with the next one, which is where the caret would otherwise be rendered.
   * @return result of check
   */
  boolean atRowEnd() {
    return rowEndPos == pos;
  }

  /**
   * Remembers if the caret is placed at the end of a rendered row. As the position is recorded,
   * any subsequent caret movement discards the flag.
   * @param rowEnd flag
   */
  void atRowEnd(final boolean rowEnd) {
    rowEndPos = rowEnd ? pos : -1;
  }

  /**
   * Moves the cursor to the specified position.
   * @param p caret position
   * @param select selection flag
   */
  void moveTo(final int p, final boolean select) {
    startSelection(select);
    pos = p;
    if(select) endSelection();
  }

  /**
   * Adds a string at the current position.
   * @param str string
   */
  void insert(final String str) {
    final int cl = str.length();
    final TokenBuilder tb = new TokenBuilder(cl);
    for(int c = 0; c < cl; ++c) {
      // skip invalid characters
      int ch = str.charAt(c);
      if(ch == '\r' || ch < ' ' && !ws(ch)) continue;
      if(Character.isHighSurrogate((char) ch) && c + 1 < cl) {
        ch = Character.toCodePoint((char) ch, str.charAt(++c));
      }
      tb.add(ch);
    }
    insert(tb.finish(), pos, pos);
    pos += tb.size();
  }

  /**
   * (Un)comments highlighted text or line.
   * @param syntax syntax highlighter
   * @return {@code true} if text has changed
   */
  boolean comment(final Syntax syntax) {
    final byte[] st = syntax.commentOpen(), en = syntax.commentEnd();
    final byte[] ste = concat(st, cpToken(' ')), ene = concat(cpToken(' '), en);
    final int sl = st.length, el = en.length, sle = ste.length, ele = ene.length;
    final boolean sel = isSelected();
    final int caret = pos;

    if(!sel) {
      // no selection: select line
      start = pos;
      end = pos;
      while(start > 0 && text[start - 1] != '\n') --start;
      while(end < size() && text[end] != '\n') ++end;
    } else if(start > end) {
      // selection: start < end
      final int s = start;
      start = end;
      end = s;
    }

    // ignore whitespace
    while(start < end && ws(text[start])) ++start;
    while(end > start && ws(text[end - 1])) --end;

    final int min = start;
    int max = end;
    if(isSelected() && text[max - 1] == '\n') max--;

    // check for an existing comment, with or without spaces
    final int mx = Math.max(min + sl, max - el), mxe = Math.max(min + sle, max - ele);
    final boolean spaced = indexOf(text, ste, min) == min && indexOf(text, ene, mxe) == mxe;
    final boolean plain = !spaced && indexOf(text, st, min) == min && indexOf(text, en, mx) == mx;

    // create new text with or without comment
    final TokenBuilder tb = new TokenBuilder();
    final int off, open;
    if(spaced || plain) {
      // remove existing comment
      final int sc = spaced ? sle : sl, ec = spaced ? ele : el;
      tb.add(text, min + sc, max - ec);
      open = -sc;
      off = open - ec;
    } else {
      // add new comment
      tb.add(ste).add(text, min, max).add(ene);
      open = sle;
      off = open + ele;
    }
    final boolean added = insert(tb.finish(), min, max);
    // selected text is reselected; otherwise, the caret is shifted by the opening comment
    if(sel) select(min, max + off);
    else pos(caret < min ? caret : caret > max ? caret + off :
      Math.max(min, Math.min(caret + open, max + off)));
    return added;
  }

  /**
   * Inserts a string into the given text.
   * @param string new string
   * @param offset offset where to add the new string
   * @param rem offset of remaining text
   * @return {@code true} if text has changed
   */
  private boolean insert(final byte[] string, final int offset, final int rem) {
    final int ts = size();
    final ByteList bl = new ByteList(offset + string.length + ts - rem);
    bl.add(text, 0, offset).add(string).add(text, rem, ts);
    return text(bl.finish());
  }

  /**
   * Case conversion.
   * @param cs case type
   * @return {@code true} if text has changed
   */
  boolean toCase(final Case cs) {
    if(!isSelected()) return false;
    final int s = selMin(), e = selMax(), d = size() - e;
    final byte[] tmp = substring(text, s, e);

    final TokenBuilder tb = new TokenBuilder(size());
    tb.add(text, 0, s);
    tb.add(cs == Case.LOWER ? lc(tmp) : cs == Case.UPPER ? uc(tmp) : tc(tmp));
    tb.add(text, e, size());
    final boolean changed = text(tb.finish());

    select(s, size() - d);
    return changed;
  }

  /**
   * Jumps to a matching bracket. Brackets in strings, comments and element content are ignored.
   * @param syntax syntax highlighter
   * @return new caret position
   */
  int bracket(final Syntax syntax) {
    // the highlighter is a forward-only lexer: pair all brackets in a single pass
    syntax.reset();
    final IntList positions = new IntList();
    final int caret = pos(), tl = size();

    for(int p = 0; p < tl; p += cl(text, p)) {
      syntax.color(text, p, p + cl(text, p));
      final int cp = cp(text, p);
      final boolean code = syntax.code();
      final int opening = Syntax.OPENING.indexOf(cp), closing = Syntax.CLOSING.indexOf(cp);
      // the caret is on no bracket, or on one that is no code: jump to the enclosing bracket
      if(p == caret && (!code || opening == -1 && closing == -1)) break;
      if(!code) continue;

      if(opening != -1) {
        positions.add(p);
      } else if(closing != -1 && !positions.isEmpty() &&
          Syntax.OPENING.indexOf(cp(text, positions.peek())) == closing) {
        final int open = positions.pop();
        if(open == caret) return p;
        if(p == caret) return open;
      }
    }
    // the caret is on no bracket: jump to the enclosing one
    return positions.isEmpty() ? caret : positions.peek();
  }

  /**
   * Moves the current line or the selected lines up or down.
   * @param down down/up flag
   */
  void move(final boolean down) {
    if(!extend()) return;

    final int s = start, e = end, ts = size();
    final byte[] tmp = Arrays.copyOf(text, ts);
    if(down) {
      if(e == ts) return;
      pos = e;
      lineEnd(true);
      int c = s;
      for(int i = e; i < pos; i++) tmp[c++] = text[i];
      tmp[c++] = '\n';
      for(int i = s; i < e - 1; i++) tmp[c++] = text[i];
      text(tmp);
      select(s + pos - e + 1, Math.min(ts, pos + 1));
    } else {
      if(s == 0) return;
      pos = s - 1;
      startOfLine(true);
      int c = pos;
      for(int i = s; i < e; i++) tmp[c++] = text[i];
      if(tmp[c - 1] != '\n') tmp[c++] = '\n';
      for(int i = pos; i < s && c < ts; i++) tmp[c++] = text[i];
      text(tmp);
      select(pos, pos + e - s);
    }
  }

  /**
   * Inserts the specified value and updates the cursor position.
   * @param value value
   * @param p position to start completion from
   */
  void complete(final String value, final int p) {
    // remove first underscore, which indicates the new cursor position
    String v = value;
    final int car = v.indexOf('_');
    if(car != -1) v = v.substring(0, car) + v.substring(car + 1);
    // adopt current indentation
    final int ind = open();
    if(ind != 0) {
      v = new TokenBuilder().addAll(v.split("\n"), "\n" + " ".repeat(ind)).toString();
    }
    // delete old string, add new one
    replace(p, completionEnd(), v);
    // adjust cursor
    if(car != -1) pos = p + car;
  }

  /**
   * Formats the selected text.
   * @param syntax syntax highlighter
   * @return {@code true} if text has changed
   */
  boolean format(final Syntax syntax) {
    final boolean sel = isSelected();
    final int s = sel ? selMin() : 0;
    final int e = sel ? selMax() : size();
    final Anchor anchor = anchor(s);
    final byte[] format = syntax.format(Arrays.copyOfRange(text, s, e), opts.spaces(),
        opts.margin());
    final boolean changed = insert(format, s, e);
    // selected text is reselected; otherwise, the caret keeps its place in the text
    final int to = s + format.length;
    if(sel) select(s, to);
    else restore(anchor, to);
    return changed;
  }

  /**
   * Removes trailing whitespace and appends a final newline.
   * @param trim remove trailing whitespace
   * @param nl append a final newline
   * @return {@code true} if text has changed
   */
  boolean tidy(final boolean trim, final boolean nl) {
    // carriage returns have already been removed from the editor contents
    final int tl = size();
    final Anchor anchor = anchor(0);
    final byte[] tmp = new byte[tl + 1];
    // size of the new text, start of the current whitespace run ({@code -1}: no run)
    int size = 0, run = -1;
    for(int t = 0; t < tl; t++) {
      final byte b = text[t];
      if(trim && (b == ' ' || b == '\t')) {
        if(run == -1) run = size;
      } else {
        if(b == '\n' && run != -1) size = run;
        run = -1;
      }
      tmp[size++] = b;
    }
    if(run != -1) size = run;
    if(nl && size > 0 && tmp[size - 1] != '\n') tmp[size++] = '\n';

    if(!text(size == tmp.length ? tmp : Arrays.copyOf(tmp, size))) return false;
    restore(anchor, size);
    return true;
  }

  /**
   * Caret position in a text whose whitespace is about to be changed.
   * @param start start offset
   * @param count non-whitespace characters that precede the caret
   * @param broken line break separates the caret from the last of these characters
   */
  private record Anchor(int start, int count, boolean broken) { }

  /**
   * Anchors the caret in the non-whitespace characters that precede it.
   * @param s start offset
   * @return anchor
   */
  private Anchor anchor(final int s) {
    int count = 0;
    for(int p = s; p < pos; p++) {
      if(!ws(text[p])) count++;
    }
    // check if a line break separates the caret from the last of these characters
    boolean brk = false;
    for(int p = pos - 1; p >= s && ws(text[p]) && !brk; p--) {
      brk = text[p] == '\n';
    }
    return new Anchor(s, count, brk);
  }

  /**
   * Restores an anchored caret in a text in which only whitespace was changed.
   * @param anchor caret anchor
   * @param e end offset
   */
  private void restore(final Anchor anchor, final int e) {
    // skip the characters that precede the caret
    int c = 0, p = anchor.start();
    while(p < e && c < anchor.count()) {
      if(!ws(text[p])) c++;
      p++;
    }
    // the caret was placed in a new line: skip the whitespace that precedes the next character
    if(anchor.broken()) {
      while(p < e && ws(text[p])) p++;
    }
    pos(p);
  }

  /**
   * Sorts the selected text.
   * @return {@code true} if text has changed
   */
  boolean sort() {
    final boolean sel = isSelected();
    final int caret = pos;
    if(!sel) selectAll();
    if(!extend()) return false;

    // count lines
    int l = 1;
    final int s = start, e = end, ts = size();
    final byte[] tmp = Arrays.copyOf(text, ts);
    for(int i = s; i < e; i++) {
      if(tmp[i] == '\n') l++;
    }

    // collect lines to be sorted
    final TokenList tl = new TokenList(l);
    final ByteList bl = new ByteList();
    for(int i = s; i < e; i++) {
      final byte ch = tmp[i];
      if(ch == '\n') {
        tl.add(bl.next());
      } else {
        bl.add(ch);
      }
    }
    if(!bl.isEmpty()) tl.add(bl.finish());

    // line with the caret, and column in this line
    int cl = 0, cc = 0;
    if(!sel) {
      for(int i = s; i < caret; i++) {
        if(tmp[i] == '\n') {
          cl++;
          cc = 0;
        } else {
          cc++;
        }
      }
    }
    final byte[] caretLine = sel || cl >= tl.size() ? null : tl.get(cl);
    sort(tl);

    // copy lines back to text
    int i = s;
    for(final byte[] line : tl) {
      final int ll = line.length;
      Array.copyFromStart(line, ll, tmp, i);
      i += ll;
      if(i < e) tmp[i++] = '\n';
    }
    if(i < e) Array.copy(tmp, e, ts - e, tmp, i);
    final boolean changed = text(i == e ? tmp : Arrays.copyOf(tmp, ts - e + i));
    // selected text is reselected; otherwise, the caret follows its line
    if(sel) {
      select(s, i);
    } else {
      int p = i;
      if(caretLine != null) {
        p = s;
        for(final byte[] ln : tl) {
          if(eq(ln, caretLine)) break;
          p += ln.length + 1;
        }
        p = Math.min(p + cc, i);
      }
      pos(p);
    }
    return changed;
  }

  /**
   * Sorts the specified data.
   * @param tokens list of tokens
   */
  private void sort(final TokenList tokens) {
    final boolean unicode = opts.get(GUIOptions.UNICODE);
    final int column = opts.get(GUIOptions.COLUMN) - 1;

    // stable sort: before custom sort, apply default sort
    if(!unicode || column > 0) tokens.sort(true, true);

    // choose the cheapest comparator
    final Comparator<byte[]> cc;
    if(!unicode) {
      final Collator coll = Collator.getInstance();
      cc = (t1, t2) -> coll.compare(string(sub(t1, column)), string(sub(t2, column)));
    } else if(opts.get(GUIOptions.CASESORT)) {
      cc = (t1, t2) -> compare(sub(t1, column), sub(t2, column));
    } else {
      cc = (t1, t2) -> compare(lc(sub(t1, column)), lc(sub(t2, column)));
    }
    tokens.sort(cc, opts.get(GUIOptions.ASCSORT));

    // remove duplicates
    if(opts.get(GUIOptions.MERGEDUPL)) tokens.unique();
  }

  /**
   * Returns a substring.
   * @param token token
   * @param column column position
   * @return sub string
   */
  private static byte[] sub(final byte[] token, final int column) {
    final int tl = token.length;
    int t = 0;
    for(int c = 0; t < tl && c < column; t += cl(token, t), c++);
    return substring(token, t);
  }

  /**
   * Indents the current line or text.
   * @param sb typed in string
   * @param shift shift key
   * @return indentation flag
   */
  boolean indent(final StringBuilder sb, final boolean shift) {
    // no selection, shift pressed: select current character
    if(!isSelected() && shift && size() != 0) selectLine();

    // check if something is selected
    if(isSelected()) {
      indent(shift);
      sb.setLength(0);
      return isSelected();
    }

    if(shift) {
      sb.setLength(0);
    } else {
      final boolean c = pos > 0;
      for(int p = pos - 1; p >= 0 && c; p--) {
        final byte b = text[p];
        if(!ws(b)) return false;
        if(b == '\n') break;
      }
      sb.setLength(0);
      sb.append(string(opts.spaces()));
    }
    return false;
  }

  /**
   * Processes the enter key and checks for opening brackets.
   * @param sb typed in string
   * @return number of characters to move forward
   */
  int enter(final StringBuilder sb) {
    // indent after opening bracket
    final boolean opening = pos > 0 && Syntax.OPENING.indexOf(text[pos - 1]) != -1;
    final boolean closing = pos < size() && Syntax.CLOSING.indexOf(text[pos]) != -1;

    final int ind = opts.indent();
    int indent = open(), move = 0;
    if(opening) {
      if(closing) {
        sb.append(" ".repeat(Math.max(0, indent + ind)));
        move = indent + ind + 1;
        sb.append('\n');
      } else {
        indent += ind;
      }
    } else if(closing) {
      // unindent before closing bracket
      indent -= ind;
    }
    sb.append(" ".repeat(Math.max(0, indent)));
    add(sb, false);
    return move;
  }

  /**
   * Processes and adds the specified string.
   * @param sb string to be added
   * @param selected states if the text was selected
   * @return returns the number spaces to move forward
   */
  int add(final StringBuilder sb, final boolean selected) {
    if(sb.isEmpty()) return 0;

    int move = 0;
    if(!selected && opts.get(GUIOptions.AUTO)) {
      final char ch = sb.charAt(0);
      final int next = pos + 1 < size() ? text[pos + 1] : 0;
      final int curr = pos < size() ? text[pos] : 0;
      final int prev = pos > 0 ? text[pos - 1] : 0;
      final int pprv = pos > 1 ? text[pos - 2] : 0;
      final int opening = Syntax.OPENING.indexOf(ch);
      if(opening != -1) {
        // adds a closing to an opening bracket
        if(Syntax.CLOSING.indexOf(curr) != -1 || curr == 0 || ws(curr) || curr == '<') {
          sb.append(Syntax.CLOSING.charAt(opening));
          move = 1;
        }
      } else if(Syntax.CLOSING.indexOf(ch) != -1) {
        // closing bracket: ignore if it equals next character
        if(ch == curr) {
          sb.setLength(0);
          move = 1;
        }
        close();
      } else if(ch == '"' || ch == '\'' || ch == '`') {
        // quote: ignore if it equals next character
        if(ch == curr) sb.setLength(0);
        // add second quote
        else if(!XMLToken.isNCChar(prev) && !XMLToken.isNCChar(curr)) sb.append(ch);
        move = 1;
      } else if(ch == '>') {
        // closes an opening element
        closeElem(sb);
        move = 1;
      } else if(ch == ':') {
        // closes XQuery comments
        if(prev == '(') {
          sb.append(':');
          if(curr != ')') sb.append(')');
          move = 1;
        }
      } else if(ch == '~') {
        // closes XQuery comments
        if(prev == ':' && pprv == '(') {
          sb.append(" ");
          if(curr != ':') {
            sb.append(':');
            if(curr != ')') sb.append(')');
          } else if(next != ')') {
            sb.append(')');
          }
          move = 1;
        }
      } else if(ch == '-') {
        // closes XML comments
        if(prev == '-' && pprv == '!' && pos > 2 && text[pos - 3] == '<') {
          sb.append("  -->");
          move = 2;
        }
      } else if(ch == '?') {
        // closes XML processing instructions
        if(prev == '<') {
          sb.append(" ?>");
          move = 1;
        }
      }
    }
    insert(sb.toString());
    return move;
  }

  /**
   * Closes a bracket and unindents leading whitespace.
   */
  private void close() {
    int p = pos - 1;
    for(; p >= 0; p--) {
      final byte b = text[p];
      if(b == '\n') break;
      if(!ws(b)) return;
    }
    if(++p >= pos) return;
    start = Math.max(pos - opts.indent(), p);
    end = Math.max(pos, p);
    if(start != end) delete();
  }

  /**
   * Tries to close an opening tag.
   * @param sb string builder
   */
  private void closeElem(final StringBuilder sb) {
    int s = pos - 1;
    while(s >= 0 && text[s] != '<') s--;
    final int n = s + 1;
    if(s < 0 || n >= pos || text[n] == '/' || text[n] == '?' || text[n] == '!') return;
    int e = n;
    while(e < pos && !ws(text[e]) && text[e] != '/' && text[e] != '>') e++;
    if(e == n) return;

    int quote = 0;
    boolean slash = false;
    for(int q = e; q < pos; q++) {
      final int cp = text[q];
      if(quote != 0) {
        if(cp == quote) quote = 0;
      } else if(cp == '"' || cp == '\'') {
        quote = cp;
      } else if(cp == '>') {
        return;
      } else if(!ws(cp)) {
        slash = cp == '/';
      }
    }
    if(quote == 0 && !slash) sb.append("</").append(string(text, n, e - n)).append('>');
  }

  /**
   * Deletes the previous character or the current selection.
   */
  void deletePrev() {
    if(!isSelected()) {
      if(pos == 0) return;
      anchorSelection();
      final int curr = curr(), prev = prev();
      endSelection();

      if(opts.get(GUIOptions.AUTO)) {
        if(curr == prev && (curr == '"' || curr == '\'' || curr == '`')) {
          // remove closing quote
          start++;
        } else {
          // remove closing bracket
          final int open = Syntax.OPENING.indexOf(prev);
          if(open != -1 && Syntax.CLOSING.indexOf(curr) == open) start++;
        }
      }
    }
    deleteSelection();
  }

  /**
   * Deletes the current character or selection.
   * Assumes that the current position allows a deletion.
   */
  void delete() {
    if(!isSelected()) {
      if(pos == size()) return;
      start = pos;
      end = pos + cl(text, pos);
    }
    deleteSelection();
  }

  /**
   * Deletes the current selection.
   */
  private void deleteSelection() {
    final int s = selMin(), e = selMax(), ts = size();
    text(new ByteList(ts - e + s).add(text, 0, s).add(text, e, ts).finish());
    pos = s;
  }

  /**
   * Deletes lines.
   */
  void deleteLines() {
    extend();
    delete();
  }

  /**
   * Duplicate lines.
   */
  void duplLines() {
    final int p = pos, s = start, e = end;
    extend();
    final String selected = selected();
    if(selected.isEmpty()) return;

    final StringBuilder sb = new StringBuilder();
    if(end > 0 && text[end - 1] != '\n') sb.append('\n');
    pos = end;
    insert(sb.append(selected).toString());
    pos = p;
    start = s;
    end = e;
  }

  /**
   * Deletes the word or line following the cursor.
   * @param word word/line flag
   */
  void deleteNext(final boolean word) {
    delete(word, true);
  }

  /**
   * Deletes the word or line preceding the cursor.
   * @param word word/line flag
   */
  void deletePrev(final boolean word) {
    delete(word, false);
  }

  /**
   * Deletes the word or line that precedes or follows the cursor.
   * @param word word/line flag
   * @param next next/previous flag
   */
  private void delete(final boolean word, final boolean next) {
    if(!isSelected()) {
      if(pos == (next ? size() : 0)) return;
      anchorSelection();
      if(next) {
        if(word) nextWord(true);
        else lineEnd(true);
      } else {
        if(word) prevWord(true);
        else lineStart(true);
      }
      checkSelection();
      // the caret was already placed at the beginning of the line: delete the line break
      if(!next && !isSelected()) prev();
    }
    delete();
  }

  // PRIVATE METHODS ==============================================================================

  /**
   * Replaces the specified text area with another string.
   * @param s start
   * @param e end
   * @param value new value
   */
  private void replace(final int s, final int e, final String value) {
    select(s, e);
    // an empty area is no selection: the next character must not be deleted
    if(isSelected()) delete();
    insert(value);
  }

  /**
   * Extends the current selection to the beginning of the first and the end of the last line.
   * @return if text is selected
   */
  private boolean extend() {
    if(!isSelected()) {
      selectLine();
      if(!isSelected()) return false;
    }

    int s = selMin(), e = selMax();
    final int ts = size();
    while(s > 0 && text[s - 1] != '\n') s--;
    if(e > 0) while(e < ts && text[e - 1] != '\n') e++;
    start = s;
    end = e;
    return true;
  }

  /**
   * Indents lines.
   * @param shift shift flag
   */
  private void indent(final boolean shift) {
    if(!extend()) return;

    final int s = start, e = end, ind = opts.indent();
    final byte[] spaces = opts.spaces();

    // build new text
    final TokenBuilder tb = new TokenBuilder();
    for(int p = s; p < e; p++) {
      if(p == 0 || text[p - 1] == '\n') {
        // find leading whitespace
        int i = 0;
        do {
          final int cp = text[p];
          if(cp == '\t') {
            i += ind;
          } else if(cp == ' ') {
            i++;
          } else {
            break;
          }
        } while(++p < e);

        // calculate indentation, add indentation and remaining spaces
        i = shift ? Math.max(0, i - ind) : i + ind;
        for(int c = 0; c < i / ind; c++) tb.add(spaces);
        for(int c = 0; c < i % ind; c++) tb.add(' ');
      }
      if(p < e) tb.addByte(text[p]);
    }

    insert(tb.finish(), s, e);
    select(s, s + tb.size());
  }

  /**
   * Returns the indentation of the current line.
   * @return number of spaces to indent
   */
  private int open() {
    // adopt indentation from previous line
    final int ind = opts.indent();
    int indent = 0;
    for(int p = pos - 1; p >= 0; p--) {
      final byte b = text[p];
      if(b == '\n') break;
      if(b == '\t') {
        indent += ind;
      } else if(b == ' ') {
        indent++;
      } else {
        indent = 0;
      }
    }
    return indent;
  }

  // TEXT NAVIGATION AND SELECTION ================================================================

  /**
   * Returns the text size.
   * @return text size
   */
  int size() {
    return text.length;
  }

  /**
   * Returns the caret position.
   * @return caret position
   */
  int pos() {
    return pos;
  }

  /**
   * Sets the caret position and resets a selection.
   * @param p caret position
   */
  void pos(final int p) {
    pos = p;
    resetSelection();
  }

  /**
   * Resets the selection.
   */
  void resetSelection() {
    start = -1;
    end = -1;
  }

  /**
   * Places the caret at the specified position and starts a new selection.
   * @param p position
   */
  void selectFrom(final int p) {
    pos = p;
    anchorSelection();
  }

  /**
   * Places the caret at the specified position and extends the current selection.
   * @param p position
   */
  void selectTo(final int p) {
    pos = p;
    end = pos;
  }

  /**
   * Selects the whole text.
   */
  void selectAll() {
    select(0, size());
  }

  /**
   * Selects the specified area and places the caret at the end of the selection.
   * @param s start position
   * @param e end position
   */
  void select(final int s, final int e) {
    start = s;
    end = e;
    pos = e;
    checkSelection();
  }

  /**
   * Initializes the selection.
   * @param select select flag
   */
  void startSelection(final boolean select) {
    if(select) keepSelection(true);
    else resetSelection();
  }

  /**
   * Initializes a selection and preserves an existing one.
   * @param select select flag
   */
  private void keepSelection(final boolean select) {
    if(select && !isSelected()) anchorSelection();
  }

  /**
   * Finishes a text selection.
   */
  void endSelection() {
    end = pos;
    checkSelection();
  }

  /**
   * Checks the validity of the selection.
   */
  private void checkSelection() {
    if(start == end) resetSelection();
  }

  /**
   * Tests if text has been selected.
   * @return result of check
   */
  boolean isSelected() {
    return start != end;
  }

  /**
   * Returns the start position of a text selection.
   * @return position ({@code -1} if no text is selected)
   */
  int start() {
    return start;
  }

  /**
   * Returns the end position of a text selection.
   * @return position ({@code -1} if no text is selected)
   */
  int end() {
    return end;
  }

  /**
   * Returns the first offset of a text selection.
   * @return offset
   */
  private int selMin() {
    return Math.min(start, end);
  }

  /**
   * Returns the offset after a text selection.
   * @return offset
   */
  private int selMax() {
    return Math.max(start, end);
  }

  /**
   * Returns the selected string.
   * @return string
   */
  String selected() {
    final int e = selMax();
    int s = selMin();
    final TokenBuilder tb = new TokenBuilder(e - s);
    for(; s < e; s += cl(text, s)) {
      final int cp = cp(text, s);
      if(cp >= ' ' && cp < TokenBuilder.PRIVATE_START || cp == 0x0A || cp == 0x09 ||
         cp > TokenBuilder.PRIVATE_END) tb.add(cp);
    }
    return tb.toString();
  }

  /**
   * Selects the word at the cursor position.
   */
  void selectWord() {
    final boolean ch = FTToken.lod(curr());
    while(pos() > 0) {
      final int cp = back(true);
      if(cp == '\n' || ch != FTToken.lod(cp)) {
        forward(true);
        break;
      }
    }
    anchorSelection();
    while(pos() < size()) {
      final int cp = curr();
      if(cp == '\n' || ch != FTToken.lod(cp)) break;
      forward(true);
    }
    endSelection();
  }

  /**
   * Selects the word at the cursor position.
   */
  void selectLine() {
    startOfLine(false);
    anchorSelection();
    forwardTo(Integer.MAX_VALUE, true);
    next();
    endSelection();
  }

  /**
   * Returns the current character, or the newline character if the position is out of bounds.
   * @return current character, or newline
   */
  private int curr() {
    return pos < 0 || pos >= size() ? '\n' : cp(text, pos);
  }

  /**
   * Returns the current character and moves to the next character.
   * @return current character
   */
  private int next() {
    final int c = curr();
    if(pos < size()) pos += cl(text, pos);
    return c;
  }

  /**
   * Anchors a new text selection at the current position.
   */
  private void anchorSelection() {
    start = pos;
    end = pos;
  }

  // ERROR HIGHLIGHTING ===========================================================================

  /**
   * Sets the error position.
   * @param s start position
   */
  void error(final int s) {
    error = s;
  }

  /**
   * Returns the error position.
   * @return position ({@code -1} if there is no error)
   */
  int error() {
    return error;
  }

  // SEARCH HIGHLIGHTING ==========================================================================

  /**
   * Selects a search string.
   * @param dir search direction
   * @param select select hit
   * @return new cursor position, or {@code -1}
   */
  int jump(final SearchDir dir, final boolean select) {
    final int sl = searchResults[0].size();
    if(sl == 0) {
      searchHit = -1;
      if(select) resetSelection();
      return -1;
    }

    // a zero-width hit is no selection: treat a hit we jumped to as the current one
    final boolean current = isSelected() || searchSelected && searchHit >= 0 && searchHit < sl &&
        searchResults[0].get(searchHit) == pos;
    // a directional jump off a hit lowers the key: a hit at the caret still lies ahead of it
    final boolean back = select && !current && dir != SearchDir.CURRENT;
    int s = searchResults[0].sortedIndexOf(back ? pos - 1 : pos);
    s = switch(dir) {
      case CURRENT -> s < 0 ? -s - 1 : s;
      case FORWARD -> s < 0 ? -s - 1 : s + 1;
      case BACKWARD -> s < 0 ? -s - 2 : s - 1;
    };
    if(s < 0) s = sl - 1;
    else if(s == sl) s = 0;
    searchHit = s;
    searchSelected = select;
    final int p = searchResults[0].get(s);
    if(select) {
      start = searchResults[1].get(s);
      end = p;
    }
    pos = p;
    return p;
  }

  /**
   * Returns the index of the current search hit.
   * @return index, or {@code -1}
   */
  int searchIndex() {
    return searchHit;
  }

  /**
   * Returns the number of search hits.
   * @return number of hits
   */
  int searchSize() {
    return searchResults[0].size();
  }
}
