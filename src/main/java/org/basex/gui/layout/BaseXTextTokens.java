package org.basex.gui.layout;

import static org.basex.util.Token.*;

import java.util.*;

import org.basex.util.*;

/**
 * This class allows the iteration on tokens.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Christian Gruen
 */
public final class BaseXTextTokens {
  /** Tab width. */
  static final int TAB = 2;
  /** Text array to be written. */
  byte[] text = EMPTY;

  /** Text length. */
  private int size;
  /** Current start position. */
  private int ps;
  /** Current end position. */
  private int pe;
  /** Current cursor position. */
  private int pc;
  /** Start of a text mark. */
  private int ms = -1;
  /** End of a text mark. */
  private int me = -1;
  /** Start of an error mark. */
  private int es = -1;

  /**
   * Constructor.
   * @param t text
   */
  BaseXTextTokens(final byte[] t) {
    this(t, t.length);
  }

  /**
   * Constructor.
   * @param t text
   * @param s buffer size
   */
  BaseXTextTokens(final byte[] t, final int s) {
    text = t;
    size = s;
  }

  /**
   * Initializes the iterator.
   */
  void init() {
    ps = 0;
    pe = 0;
  }

  /**
   * Checks if the text contains more words.
   * @return result of check
   */
  boolean moreWords() {
    // quit if text has ended
    if(pe >= size) return false;
    ps = pe;

    // find next token boundary
    int ch = cp(text, ps);
    pe += cl(text, ps);
    if(!ftChar(ch)) return true;

    while(pe < size) {
      ch = cp(text, pe);
      if(!ftChar(ch)) break;
      pe += cl(text, pe);
    }
    return true;
  }

  /**
   * Returns the token as word.
   * @return word
   */
  public String nextWord() {
    return string(text, ps, pe - ps);
  }

  /**
   * Returns the length of the current word.
   * @return length
   */
  int length() {
    return pe - ps;
  }

  /**
   * Moves one character forward.
   * @param mark mark flag
   * @return character
   */
  int next(final boolean mark) {
    return noMark(mark, true) ? curr() : next();
  }

  /**
   * Moves one token forward.
   * @param mark mark flag
   */
  void nextToken(final boolean mark) {
    int ch = next(mark);
    if(ch == '\n') return;
    if(Character.isLetterOrDigit(ch)) {
      while(Character.isLetterOrDigit(ch)) ch = next();
      while(ch != '\n' && Character.isWhitespace(ch)) ch = next();
    } else if(Character.isWhitespace(ch)) {
      while(ch != '\n' && Character.isWhitespace(ch)) ch = next();
    } else {
      while(ch != '\n' && !Character.isLetterOrDigit(ch) &&
          !Character.isWhitespace(ch)) ch = next();
      while(ch != '\n' && Character.isWhitespace(ch)) ch = next();
    }
    if(ps != size) prev();
  }

  /**
   * Moves one token back.
   * @param mark mark flag
   */
  void prevToken(final boolean mark) {
    int ch = prev(mark);
    if(ch == '\n') return;
    if(Character.isLetterOrDigit(ch)) {
      while(Character.isLetterOrDigit(ch)) ch = prev();
    } else if(Character.isWhitespace(ch)) {
      while(ch != '\n' && Character.isWhitespace(ch)) ch = prev();
      while(Character.isLetterOrDigit(ch)) ch = prev();
    } else {
      while(ch != '\n' && !Character.isLetterOrDigit(ch) &&
          !Character.isWhitespace(ch)) ch = prev();
    }
    if(ps != 0) next();
  }

  /**
   * Checks if the character position equals the word end.
   * @return result of check
   */
  boolean more() {
    return ps < pe;
  }

  /**
   * Returns the current character.
   * @return current character
   */
  public int curr() {
    return ps >= size ? '\n' : cp(text, ps);
  }

  /**
   * Moves one character forward.
   * @return character
   */
  int next() {
    final int c = curr();
    if(ps < size) ps += cl(text, ps);
    return c;
  }

  /**
   * Sets the iterator position.
   * @param p iterator position
   */
  void pos(final int p) {
    ps = p;
  }

  /**
   * Returns the iterator position.
   * @return iterator position
   */
  int pos() {
    return ps;
  }

  /**
   * Returns the byte array, chopping the unused bytes.
   * @return character array
   */
  byte[] toArray() {
    return text.length == size ? text : Arrays.copyOf(text, size);
  }

  // POSITION =================================================================

  /**
   * Moves to the beginning of the line.
   * @param mark mark flag
   * @return number of moved characters
   */
  int bol(final boolean mark) {
    int c = 0;
    if(ps == 0) return 0;
    do c += curr() == '\t' ? TAB : 1; while(prev(mark) != '\n');
    if(ps != 0 || curr() == '\n') next(mark);
    return c;
  }

  /**
   * Moves to the end of the line.
   * @param mark mark flag
   */
  void eol(final boolean mark) {
    forward(Integer.MAX_VALUE, mark);
  }

  /**
   * Moves one character back and returns the found character.
   * @param mark mark flag
   * @return character
   */
  int prev(final boolean mark) {
    return noMark(mark, false) ? curr() : prev();
  }

  /**
   * Moves one character back and returns the found character.
   * @return character
   */
  int prev() {
    if(ps == 0) return '\n';
    final int p = ps;
    ps = Math.max(0, ps - 5);
    while(ps < p && ps + cl(text, ps) < p) ++ps;
    return curr();
  }

  /**
   * Moves to the specified position of to the of the line.
   * @param p position to move to
   * @param mark mark flag
   */
  void forward(final int p, final boolean mark) {
    int nc = 0;
    while(curr() != '\n') {
      if((nc += curr() == '\t' ? TAB : 1) >= p) return;
      next(mark);
    }
  }

  /**
   * Adds a string at the current position.
   * @param str string
   */
  void add(final String str) {
    final TokenBuilder tb = new TokenBuilder();
    tb.add(text, 0, ps);
    int cc = 0;
    final int cl = str.length();
    for(int c = 0; c < cl; ++c) {
      // ignore invalid characters
      int ch = str.charAt(c);
      if(ch == '\r') continue;
      if(ch < ' ' && !ws(ch)) ch = '\n';
      tb.add(ch);
      ++cc;
    }
    tb.add(text, ps, size);
    text = tb.finish();
    size = tb.size();
    for(int c = 0; c < cc; ++c) next();
  }

  /**
   * Indents lines.
   * @param s start position
   * @param e end position
   * @param sh shift flag
   */
  void indent(final int s, final int e, final boolean sh) {
    // extend selection to match whole lines
    pos(s);
    bol(true);
    startMark();
    pos(e);
    forward(Integer.MAX_VALUE, true);
    next(true);
    endMark();

    // decide if to use tab or spaces
    boolean tab = false;
    for(int p = 0; p < size; ++p) tab |= text[p] == '\t';
    byte[] add = { '\t' };
    if(!tab) {
      add = new byte[TAB];
      for(int a = 0; a < TAB; a++) add[a] = ' ';
    }

    // build new text
    final TokenBuilder tb = new TokenBuilder();
    tb.add(text, 0, ms);
    for(int p = ms; p < ps; p += cl(text, p)) {
      if(p == 0 || text[p - 1] == '\n') {
        if(sh) {
          // remove indentation
          if(text[p] == '\t') {
            me--;
            continue;
          }
          if(text[p] == ' ') {
            me--;
            for(int i = 1; i < TAB && p + i < size && text[p + i] == ' '; i++) {
              me--;
              p++;
            }
            continue;
          }
        } else {
          // add new indentation
          tb.add(add);
          me += add.length;
        }
      }
      tb.add(cp(text, p));
    }
    tb.add(text, ps, size);
    ps = me;
    text = tb.finish();
    size = tb.size();
  }


  /**
   * (Un)comments highlighted text or line.
   * @param syntax syntax highlighter
   */
  void comment(final BaseXSyntax syntax) {
    final byte[] start = syntax.commentOpen();
    final byte[] end = syntax.commentEnd();
    boolean add = true;
    int min = ps;
    int max = ps;

    if(marked()) {
      min = ps < ms ? ps : ms;
      max = ps > ms ? ps : ms;
      // marked
      final int mn = Math.max(min + start.length, max - end.length);
      if(indexOf(text, start, min) == min &&
         indexOf(text, end, mn) == mn) {
        final TokenBuilder tb = new TokenBuilder();
        tb.add(text, 0, min);
        tb.add(text, min + start.length, max - end.length);
        tb.add(text, max, size);
        text = tb.finish();
        size = tb.size();
        ms = min;
        me = max - start.length - end.length;
        ps = me;
        add = false;
      }
    } else {
      while(min > 0 && text[min - 1] != '\n') --min;
      while(max < size() && text[max] != '\n') ++max;
    }

    if(add) {
      pos(max);
      add(string(end));
      pos(min);
      add(string(start));
      ms = min;
      me = max + start.length + end.length;
      ps = me;
    }
  }

  /**
   * Deletes the current character.
   * Assumes that the current position allows a deletion.
   */
  void delete() {
    if(size == 0) return;
    final TokenBuilder tb = new TokenBuilder();
    final int s = marked() ? Math.min(ms, me) : ps;
    final int e = marked() ? Math.max(ms, me) : ps + cl(text, ps);
    tb.add(text, 0, s);
    if(e < size) tb.add(text, e, size);
    text = tb.finish();
    size = tb.size();
    ps = s;
    noMark();
  }

  /**
   * Deletes the current line.
   */
  void deleteLine() {
    bol(false);
    startMark();
    eol(true);
    next(true);
    endMark();
    delete();
  }

  // TEXT MARKING =============================================================

  /**
   * Jumps to the maximum/minimum position and resets the selection.
   * @param mark marking flag
   * @param max maximum/minimum flag
   * @return true if mark was reset
   */
  private boolean noMark(final boolean mark, final boolean max) {
    final boolean rs = !mark && marked();
    if(rs) {
      ps = max ^ ms < me ? ms : me;
      noMark();
    }
    return rs;
  }

  /**
   * Resets the selection.
   */
  void noMark() {
    ms = -1;
    me = -1;
  }

  /**
   * Sets the start of a text mark.
   */
  void startMark() {
    ms = ps;
    me = ps;
  }

  /**
   * Sets the end of a text mark.
   */
  void endMark() {
    me = ps;
  }

  /**
   * Returns the start of the text mark. The value is {@code -1} if no
   * text is selected.
   * @return start mark
   */
  int start() {
    return ms;
  }

  /**
   * Tests if some text is marked.
   * @return result of check
   */
  boolean marked() {
    return ms != -1;
  }

  /**
   * Checks the validity of the mark.
   */
  void checkMark() {
    if(ms == me) noMark();
  }

  /**
   * Tests if the current position is marked.
   * @return result of check
   */
  boolean markStart() {
    return marked() &&
        (inMark() || (ms < me ? ms >= ps && ms < pe : me >= ps && me < pe));
  }

  /**
   * Tests if the current position is marked.
   * @return result of check
   */
  boolean inMark() {
    return ms < me ? ps >= ms && ps < me : ps >= me && ps < ms;
  }

  /**
   * Returns the marked substring.
   * @return substring
   */
  String copy() {
    if(!marked()) return "";
    final TokenBuilder tb = new TokenBuilder();
    final int e = ms < me ? me : ms;
    for(int s = ms < me ? ms : me; s < e; s += cl(text, s)) {
      final int t = cp(text, s);
      if(t < 0 || t >= ' ' || t == 0x0A || t == 0x09) tb.add(t);
    }
    return tb.toString();
  }

  /**
   * Selects the word at the cursor position.
   */
  void selectWord() {
    pos(cursor());
    final boolean ch = ftChar(prev(true));
    while(pos() > 0) {
      final int c = prev(true);
      if(c == '\n' || ch != ftChar(c)) break;
    }
    if(pos() != 0) next(true);
    startMark();
    while(pos() < size()) {
      final int c = curr();
      if(c == '\n' || ch != ftChar(c)) break;
      next(true);
    }
    endMark();
  }

  /**
   * Selects the word at the cursor position.
   */
  void selectLine() {
    pos(cursor());
    bol(true);
    startMark();
    eol(true);
    endMark();
  }

  /**
   * Selects the whole text.
   */
  void selectAll() {
    pos(0);
    startMark();
    pos(size());
    endMark();
  }

  // ERROR MARK ===============================================================

  /**
   * Tests if the current token is erroneous.
   * @return result of check
   */
  boolean error() {
    return es >= ps && es <= pe;
  }

  /**
   * Tests if the cursor moves over the current token.
   * @param s start
   */
  void error(final int s) {
    es = s;
  }

  // CURSOR ===================================================================

  /**
   * Checks if the cursor moves over the current token.
   * @return result of check
   */
  boolean edited() {
    return pc >= ps && pc <= pe;
  }

  /**
   * Sets the caret position to the specified counter.
   * @param c caret position
   */
  void setCaret(final int c) {
    pc = c;
  }

  /**
   * Sets the caret position to the current counter.
   */
  void setCaret() {
    pc = ps;
  }

  /**
   * Returns the cursor position.
   * @return cursor position
   */
  int cursor() {
    return pc;
  }

  /**
   * Returns the text size.
   * @return text size
   */
  int size() {
    return size;
  }

  @Override
  public String toString() {
    return copy();
  }
}
