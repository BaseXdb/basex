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

  /**
   * Indentation of a line of code.
   * @param extra additional levels this line is indented by
   * @param reference additional levels of the expression this line belongs to
   * @param type syntax-specific type of the line (XQuery: type of its FLWOR clause)
   * @param separates the separators of the line separate the operands of the enclosing list
   */
  record Indent(int extra, int reference, int type, boolean separates) {
    /** Line that is not indented. */
    static final Indent NONE = new Indent(0, 0, 0, true);
  }

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
   * Returns the mode before the last processed character.
   * @return mode
   */
  final int modeBefore() {
    return modeBefore;
  }

  /**
   * Returns the mode after the last processed character.
   * @return mode
   */
  final int modeAfter() {
    return modeAfter;
  }

  /**
   * Indicates if the last processed character was code.
   * @return result of check
   */
  final boolean code() {
    return codeBefore() && codeAfter();
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
   * Indicates if the last processed character is element content.
   * @return result of check
   */
  final boolean content() {
    return content(modeBefore) && content(modeAfter);
  }

  /**
   * Indicates if element content starts after the last processed character.
   * @return result of check
   */
  final boolean contentStart() {
    return !content(modeBefore) && content(modeAfter);
  }

  /**
   * Indicates if element content ends with the last processed character (which opens a tag or an
   * enclosed expression).
   * @return result of check
   */
  final boolean contentEnd() {
    return content(modeBefore) && !content(modeAfter);
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
   * Returns the indentation of a line, relative to the expression that encloses it.
   * @param text text
   * @param pos start of the line (first character that is no whitespace)
   * @param last position after the last character of the previous line ({@code -1}: none)
   * @param mode mode of that character ({@code -1}: none)
   * @param newlines number of line breaks between the two lines
   * @param previous indentation of the previous line
   * @return indentation
   */
  @SuppressWarnings("unused")
  Indent indent(final byte[] text, final int pos, final int last, final int mode,
      final int newlines, final Indent previous) {
    return Indent.NONE;
  }

  /**
   * Indicates if the boundary whitespace of element constructors may be indented.
   * @param text text
   * @return result of check
   */
  @SuppressWarnings("unused")
  boolean boundarySpace(final byte[] text) {
    return false;
  }

  /**
   * Indicates if the last processed character opened an element (the {@code >} of a start tag).
   * @param text text
   * @param pos position of the character
   * @return result of check
   */
  @SuppressWarnings("unused")
  boolean elementOpen(final byte[] text, final int pos) {
    return false;
  }

  /**
   * Indicates if the last processed character closed an element (the {@code <} of an end tag).
   * @return result of check
   */
  boolean elementClose() {
    return false;
  }

  /**
   * Indicates if the last processed character occurs in a tag.
   * @return result of check
   */
  boolean tag() {
    return false;
  }

  /**
   * Indicates if the specified mode is element content.
   * @param mode mode
   * @return result of check
   */
  @SuppressWarnings("unused")
  boolean content(final int mode) {
    return false;
  }

  /**
   * Indicates if the syntax can be formatted.
   * @return result of check
   */
  boolean formatted() {
    return !separators().isEmpty();
  }

  /**
   * Returns the characters that separate the operands of an expression.
   * @return separators (empty if the syntax has no code)
   */
  String separators() {
    return "";
  }

  /**
   * Returns the opening brackets of lists.
   * @return brackets (empty if the language has no lists)
   */
  String lists() {
    return "";
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
   * Returns a formatted version of the specified text.
   * @param text text to be formatted
   * @param spaces indentation of a single level
   * @param margin line margin ({@code 0}: expressions will not be wrapped)
   * @return formatted text
   */
  public final byte[] format(final byte[] text, final byte[] spaces, final int margin) {
    return formatted() ? new Formatter(this, spaces, margin).format(text) : text;
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
