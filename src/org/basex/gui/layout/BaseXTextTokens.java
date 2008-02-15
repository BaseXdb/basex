package org.basex.gui.layout;

import org.basex.util.Array;
import org.basex.util.Token;
import org.basex.util.TokenBuilder;

/**
 * This class allows the iteration on tokens.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Christian Gruen
 */
public final class BaseXTextTokens {
  /** Tab width. */
  static final int TAB = 2;
  /** Text array to be written. */
  byte[] text = Token.EMPTY;
  /** Text length. */
  int size;

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
  public BaseXTextTokens(final byte[] t) {
    this(t, t.length);
  }

  /**
   * Constructor.
   * @param t text
   * @param s buffer size
   */
  public BaseXTextTokens(final byte[] t, final int s) {
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

    // parse next token boundaries; quit if text reference has been reset
    if(sep(text[ps])) ++pe;
    else while(++pe < size && !sep(text[pe]));
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
   * @param c character to be checked
   * @return true for a delimiter character
   */
  private boolean sep(final int c) {
    return !Token.letterOrDigit(c);
  }

  /**
   * Returns the the byte array, chopping the unused bytes.
   * @return character array
   */
  public byte[] finish() {
    return text.length == size ? text : Array.finish(text, size);
  }

  // POSITION =================================================================

  /**
   * Moves to the beginning of the line.
   * @param mark mark flag
   * @return number of moved characters
   */
  public int home(final boolean mark) {
    int c = 0;
    do c += curr() == '\t' ? TAB : 1; while(prev(mark) != '\n');
    if(ps != 0) next(mark);
    return c;
  }

  /**
   * Moves one character back and returns the found character.
   * @param mark mark flag
   * @return character
   */
  public int prev(final boolean mark) {
    if(mark || ms == -1) return prev();
    ps = Math.min(ms, me);
    noMark();
    return curr();
  }
  
  /**
   * Moves one character back and returns the found character.
   * @return character
   */
  public int prev() {
    if(ps == 0) return '\n';
    final int p = ps--;
    while(ps > 0 && ps + Token.cl(text[ps]) > p) ps--;
    return curr();
  }

  /**
   * Moves to the specified position of to the of the line.
   * @param p position to move to
   * @param mark mark flag
   */
  public void end(final int p, final boolean mark) {
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
  public int next(final boolean mark) {
    if(mark || ms == -1) return next();
    ps = Math.max(ms, me);
    noMark();
    return curr();
  }

  /**
   * Moves one character forward.
   * @return character
   */
  public int next() {
    final int c = curr();
    if(ps < size) ps += Token.cl(text[ps]);
    return c;
  }

  /**
   * Returns the current character.
   * @return current character
   */
  public int curr() {
    return ps == size ? '\n' : Token.cp(text, ps);
  }

  /**
   * Checks if the character position equals the word end.
   * @return result of check
   */
  public boolean more() {
    return ps < pe;
  }

  /**
   * Sets the iterator position.
   * @param p iterator position
   */
  public void pos(final int p) {
    ps = p;
  }

  /**
   * Returns the iterator position.
   * @return iterator position
   */
  public int pos() {
    return ps;
  }

  /**
   * Adds a character array at the current position.
   * @param ch char array
   */
  public void add(final char[] ch) {
    // <CG> Add Text in Text Field: use same array if some space is left 
    final TokenBuilder tb = new TokenBuilder();
    tb.add(text, 0, ps);
    for(final char c : ch) tb.addUTF(c);
    tb.add(text, ps, size);
    text = tb.finish();
    size = tb.size;
    for(final char c : ch) ps += Token.cl(c);
  }

  /**
   * Deletes the current character.
   * Assumes that the current position allows a deletion.
   */
  public void delete() {
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
  public void noMark() {
    ms = -1;
    me = -1;
  }

  /**
   * Sets the start of a text mark.
   */
  public void startMark() {
    ms = ps;
    me = ps;
  }

  /**
   * Sets the end of a text mark.
   */
  public void endMark() {
    me = ps;
  }

  /**
   * Returns the start of the text mark.
   * @return start mark
   */
  public int start() {
    return ms;
  }

  /**
   * Returns if the current position is marked.
   * @return result of check
   */
  public boolean markStart() {
    if(ms == -1) return false;
    return ms < me ? (ms >= ps && ms < pe || ps >= ms && ps < me ||
        ms >= ps && ms < pe) : (me >= ps && me < pe || ps >= me && ps < ms ||
            me >= ps && me < pe);
  }

  /**
   * Returns if the current position is marked.
   * @return result of check
   */
  public boolean marked() {
    if(ms == -1) return false;
    return ms < me ? ps >= ms && ps < me : ps >= me && ps < ms;
  }

  /**
   * Returns the marked substring.
   * @return substring
   */
  public String copy() {
    if(ms == -1) return "";
    return Token.string(text, ms < me ? ms : me, ms < me ? me - ms : ms - me);
  }

  // ERROR MARK ===============================================================

  /**
   * Returns if the current token is erroneous.
   * @return result of check
   */
  public boolean error() {
    return es >= ps && es <= pe;
  }
  
  /**
   * Returns if the cursor moves over the current token.
   * @param s start
   */
  public void error(final int s) {
    es = s;
  }

  // CURSOR ===================================================================

  /**
   * Returns if the cursor moves over the current token.
   * @return result of check
   */
  public boolean edited() {
    return pc >= ps && pc <= pe;
  }

  /**
   * Sets the cursor position.
   */
  public void setCursor() {
    pc = ps;
  }

  /**
   * Returns the cursor position.
   * @return cursor position
   */
  public int cursor() {
    return pc;
  }
}
