package org.basex.gui.text;

import static org.basex.util.Token.*;

import java.awt.*;
import java.util.*;

import org.basex.util.*;

/**
 * Framework for syntax highlighting: assigns a stackable mode to every character.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
abstract class Syntax {
  /** Empty highlighter state. */
  private static final int[] NO_STATE = {};

  /** Opening brackets. */
  static final String OPENING = "{([";
  /** Closing brackets. */
  static final String CLOSING = "})]";

  /** State index: current mode. */
  static final int MODE = 0;
  /** State index: characters to be skipped (rest of a multi-character delimiter). */
  static final int SKIP = 1;
  /** State index: mode to be entered after the skipped characters (mode + 1, 0 for none). */
  static final int PENDING = 2;
  /** State index: stack pointer. */
  static final int SP = 3;
  /** State index: first stack entry (8 bits per enclosing mode; the stack grows on demand). */
  static final int STACK = 4;

  /** Standard color. */
  Color plain;
  /** Highlighter state; allows resuming mid-document (see {@link TextLineCache}). */
  int[] state = NO_STATE;
  /** Mode before the last processed character. */
  private int modeBefore;
  /** Mode after the last processed character. */
  private int modeAfter;

  /** Simple syntax: no highlighting, no state. */
  static final Syntax SIMPLE = new Syntax() {
    @Override
    void reset() {
      state = NO_STATE;
    }
  };

  /**
   * Initializes the highlighter.
   * @param color default color
   */
  public final void init(final Color color) {
    plain = color;
    reset();
  }

  /**
   * Resets the highlighter state.
   */
  void reset() {
    state = new int[STACK + 1];
    state[MODE] = initialMode();
  }

  /**
   * Returns the color for the current token.
   * @param iter iterator
   * @return color
   */
  public final Color getColor(final TextIterator iter) {
    return color(iter.text(), iter.pos(), iter.posEnd());
  }

  /**
   * Returns the color of the token at the specified position and advances the state.
   * @param text text
   * @param pos start position of the token
   * @param end end position of the token
   * @return color
   */
  final Color color(final byte[] text, final int pos, final int end) {
    // stateless highlighter: everything is code
    if(state.length == 0) return plain;

    final int mode = state[MODE];
    final Color color;
    if(state[SKIP] > 0) {
      // remaining characters of a delimiter: keep the current color
      state[SKIP] = Math.max(0, state[SKIP] - (end - pos));
      if(state[SKIP] == 0 && state[PENDING] != 0) {
        state[MODE] = state[PENDING] - 1;
        state[PENDING] = 0;
      }
      color = color(mode);
    } else {
      color = mode(text, pos, end, cp(text, pos), mode);
    }
    modeBefore = mode;
    modeAfter = state[MODE];
    return color;
  }

  /**
   * Indicates if code precedes the last processed character (it may close an enclosed expression).
   * @return result of check
   */
  final boolean codeBefore() {
    return code(modeBefore);
  }

  /**
   * Indicates if code follows the last processed character (it may open an enclosed expression).
   * @return result of check
   */
  final boolean codeAfter() {
    return code(modeAfter);
  }

  /**
   * Returns a snapshot of the current highlighting state.
   * @return state (empty if the highlighter is stateless)
   */
  public final int[] state() {
    return state.clone();
  }

  /**
   * Restores a highlighting state previously returned by {@link #state()}.
   * @param st state to restore
   */
  public final void state(final int[] st) {
    state = st.clone();
  }

  // MODE STACK ===================================================================================

  /**
   * Enters a mode, and skips the remaining characters of its opening delimiter.
   * @param mode mode to be entered
   * @param skip characters to be skipped
   */
  final void enter(final int mode, final int skip) {
    push(state[MODE]);
    state[MODE] = mode;
    state[SKIP] = skip;
  }

  /**
   * Returns to the enclosing mode, and skips the remaining characters of the closing delimiter.
   * @param skip characters to be skipped
   */
  final void close(final int skip) {
    final int mode = pop();
    if(skip == 0) {
      state[MODE] = mode;
    } else {
      state[SKIP] = skip;
      state[PENDING] = mode + 1;
    }
  }

  /**
   * Pushes the enclosing mode to the stack.
   * @param mode mode
   */
  final void push(final int mode) {
    final int sp = state[SP], i = STACK + (sp >> 2), shift = (sp & 3) << 3;
    if(i >= state.length) state = Arrays.copyOf(state, i + 1);
    state[i] = state[i] & ~(0xFF << shift) | mode << shift;
    state[SP] = sp + 1;
  }

  /**
   * Pops the enclosing mode from the stack.
   * @return mode
   */
  final int pop() {
    final int sp = state[SP];
    if(sp == 0) return initialMode();
    state[SP] = sp - 1;
    return state[STACK + (sp - 1 >> 2)] >> ((sp - 1 & 3) << 3) & 0xFF;
  }

  // OVERRIDABLE METHODS ==========================================================================

  /**
   * Returns the mode a document starts with.
   * @return mode
   */
  int initialMode() {
    return 0;
  }

  /**
   * Determines the color of a token and advances the state.
   * @param text text
   * @param pos start position of the token
   * @param end end position of the token
   * @param ch current character
   * @param mode current mode
   * @return color
   */
  @SuppressWarnings("unused")
  Color mode(final byte[] text, final int pos, final int end, final int ch, final int mode) {
    return plain;
  }

  /**
   * Returns the color of a mode.
   * @param mode mode
   * @return color
   */
  @SuppressWarnings("unused")
  Color color(final int mode) {
    return plain;
  }

  /**
   * Indicates if the specified mode is code. Brackets are only paired in code (see
   * {@link TextRenderer} and {@link TextEditor#bracket(Syntax)}); a syntax with strings, comments
   * or literal text must override this method, or their brackets will be paired as well.
   * @param mode mode
   * @return result of check
   */
  @SuppressWarnings("unused")
  boolean code(final int mode) {
    return true;
  }

  /**
   * Returns the start of a comment.
   * @return comment start
   */
  public byte[] commentOpen() {
    return EMPTY;
  }

  /**
   * Returns the end of a comment.
   * @return comment end
   */
  public byte[] commentEnd() {
    return EMPTY;
  }

  /**
   * Returns a formatted version of a string.
   * @param string string to be formatted
   * @param spaces spaces
   * @return formatted string
   */
  @SuppressWarnings("unused")
  public byte[] format(final byte[] string, final byte[] spaces) {
    return string;
  }

  // OPERANDS =====================================================================================

  /**
   * Checks if the text before the specified position ends an operand. It decides if a character
   * is an operator or if it starts an expression (XQuery: {@code <}, Javascript: {@code /}).
   * @param text text
   * @param pos position of the character
   * @return result of check
   */
  final boolean operand(final byte[] text, final int pos) {
    final int p = skipWsBack(text, pos);
    if(p < 0) return false;
    final int ch = cp(text, p);
    if(")]}\"'`*".indexOf(ch) != -1) return true;
    return XMLToken.isNCChar(ch) && operandName(text, p);
  }

  /**
   * Checks if the name that ends at the specified position ends an operand. Keywords that are
   * followed by an expression do not (XQuery: {@code return}, Javascript: {@code typeof}).
   * @param text text
   * @param pos position of the last character of the name
   * @return result of check
   */
  @SuppressWarnings("unused")
  boolean operandName(final byte[] text, final int pos) {
    return true;
  }

  // CHARACTERS ===================================================================================

  /**
   * Returns the position of the first non-whitespace character at or after the specified position.
   * @param text text
   * @param pos position
   * @return position
   */
  static int skipWs(final byte[] text, final int pos) {
    int p = pos;
    while(ws(cp(text, p))) p += cl(text, p);
    return p;
  }

  /**
   * Returns the position of the first non-whitespace character before the specified position.
   * @param text text
   * @param pos position
   * @return position ({@code -1} if there is none)
   */
  static int skipWsBack(final byte[] text, final int pos) {
    int p = back(text, pos);
    while(p >= 0 && ws(cp(text, p))) p = back(text, p);
    return p;
  }

  /**
   * Returns the start of the sequence of name characters that ends at the specified position.
   * @param text text
   * @param pos position
   * @return start position
   */
  static int nameStart(final byte[] text, final int pos) {
    int start = pos;
    for(int p; (p = back(text, start)) >= 0 && XMLToken.isNCChar(cp(text, p)); start = p);
    return start;
  }

  /**
   * Checks if the text starts with the specified string at the given position.
   * @param text text
   * @param pos position
   * @param string string
   * @return result of check
   */
  static boolean startsWith(final byte[] text, final int pos, final String string) {
    final int sl = string.length();
    if(pos + sl > text.length) return false;
    for(int s = 0; s < sl; s++) {
      if(text[pos + s] != string.charAt(s)) return false;
    }
    return true;
  }

  /**
   * Returns the character at the specified position.
   * @param text text
   * @param pos position
   * @return character, or {@code 0} if the position is invalid
   */
  static int cp(final byte[] text, final int pos) {
    return pos >= 0 && pos < text.length ? Token.cp(text, pos) : 0;
  }

  /**
   * Returns the character before the specified position.
   * @param text text
   * @param pos position
   * @return character, or {@code 0} if there is none
   */
  static int prev(final byte[] text, final int pos) {
    return cp(text, back(text, pos));
  }

  /**
   * Returns the position of the character before the specified position.
   * @param text text
   * @param pos position
   * @return position, or {@code -1} if there is none
   */
  static int back(final byte[] text, final int pos) {
    int p = pos - 1;
    while(p >= 0 && (text[p] & 0xC0) == 0x80) p--;
    return p;
  }
}
