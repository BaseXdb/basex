package org.basex.gui.layout;

import org.basex.util.Array;
import org.basex.util.Token;
import org.basex.util.TokenBuilder;

/**
 * This class allows the iteration on tokens.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-09, ISC License
 * @author Christian Gruen
 */
final class BaseXTextTokens {
  /** Tab width. */
  static final int TAB = 2;
  /** Text array to be written. */
  byte[] text = Token.EMPTY;

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
   * Checks if the text has more words to print.
   * @return true if the text has more words
   */
  boolean moreWords() {
    // quit if text has ended
    if(pe >= size) return false;
    ps = pe;

    // find next token boundary
    int ch = Token.cp(text, ps);
    pe += Token.cl(text[ps]);
    if(sep(ch)) return true;

    while(pe < size) {
      ch = Token.cp(text, pe);
      if(sep(ch)) break;
      pe += Token.cl(text[pe]);
    };
    return true;
  }

  /**
   * Returns the next token.
   * @return next token
   */
  String nextWord() {
    return Token.string(text, ps, pe - ps);
  }

  /**
   * Returns the current character type.
   * @param c char
   * @return true for a delimiter character
   */
  private boolean sep(final int c) {
    return !Token.letterOrDigit(c);
  }

  /**
   * Returns the the byte array, chopping the unused bytes.
   * @return character array
   */
  byte[] finish() {
    return text.length == size ? text : Array.finish(text, size);
  }

  // POSITION =================================================================

  /**
   * Moves to the beginning of the line.
   * @param mark mark flag
   * @return number of moved characters
   */
  int home(final boolean mark) {
    int c = 0;
    if(ps == 0) return 0;
    do c += curr() == '\t' ? TAB : 1; while(prev(mark) != '\n');
    if(ps != 0 || curr() == '\n') next(mark);
    return c;
  }

  /**
   * Moves one character back and returns the found character.
   * @param mark mark flag
   * @return character
   */
  int prev(final boolean mark) {
    if(mark || ms == -1) return prev();
    ps = Math.min(ms, me);
    noMark();
    return curr();
  }

  /**
   * Moves one character back and returns the found character.
   * @return character
   */
  int prev() {
    if(ps == 0) return '\n';
    final int p = ps;
    ps = Math.max(0, ps - 5);
    while(ps < p && ps + Token.cl(text[ps]) < p) ps++;
    return curr();
  }

  /**
   * Moves to the specified position of to the of the line.
   * @param p position to move to
   * @param mark mark flag
   */
  void end(final int p, final boolean mark) {
    int nc = 0;
    while(curr() != '\n') {
      if((nc += curr() == '\t' ? TAB : 1) >= p) return;
      next(mark);
    }
  }

  /**
   * Moves one character forward.
   * @param mark mark flag
   * @return character
   */
  int next(final boolean mark) {
    if(mark || ms == -1) return next();
    ps = Math.max(ms, me);
    noMark();
    return curr();
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
  int curr() {
    return ps >= size ? '\n' : Token.cp(text, ps);
  }

  /**
   * Moves one character forward.
   * @return character
   */
  int next() {
    final int c = curr();
    if(ps < size) ps += Token.cl(text[ps]);
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
   * Adds a character array at the current position.
   * @param ch char array
   */
  void add(final char[] ch) {
    final TokenBuilder tb = new TokenBuilder();
    tb.add(text, 0, ps);
    for(final char c : ch) tb.addUTF(c);
    tb.add(text, ps, size);
    text = tb.finish();
    size = tb.size;
    for(int c = 0; c < ch.length; c++) next();
  }

  /**
   * Deletes the current character.
   * Assumes that the current position allows a deletion.
   */
  void delete() {
    if(size == 0) return;
    final TokenBuilder tb = new TokenBuilder();
    final int s = ms != -1 ? Math.min(ms, me) : ps;
    final int e = ms != -1 ? Math.max(ms, me) : ps + Token.cl(text[ps]);
    tb.add(text, 0, s);
    if(e < size) tb.add(text, e, size);
    text = tb.finish();
    size = tb.size;
    ps = s;
    noMark();
  }

  // TEXT MARKING =============================================================

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
   * Returns the start of the text mark.
   * @return start mark
   */
  int start() {
    return ms;
  }

  /**
   * Returns if the current position is marked.
   * @return result of check
   */
  boolean markStart() {
    if(ms == -1) return false;
    return marked() || (ms < me ? ms >= ps && ms < pe : me >= ps && me < pe);
  }

  /**
   * Returns if the current position is marked.
   * @return result of check
   */
  boolean marked() {
    return ms < me ? ps >= ms && ps < me : ps >= me && ps < ms;
  }

  /**
   * Returns the marked substring.
   * @return substring
   */
  String copy() {
    if(ms == -1) return "";
    final TokenBuilder tb = new TokenBuilder();
    for(int s = ms < me ? ms : me, e = ms < me ? me : ms; s < e; s++) {
      final byte t = text[s];
      if(t < 0 || t >= ' ' || t == 0x0A || t == 0x09) tb.add(t);
    }
    return tb.toString();
  }

  // ERROR MARK ===============================================================

  /**
   * Returns if the current token is erroneous.
   * @return result of check
   */
  boolean error() {
    return es >= ps && es <= pe;
  }

  /**
   * Returns if the cursor moves over the current token.
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
}
