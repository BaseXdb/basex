package org.basex.util;

import static org.basex.core.Text.*;
import static org.basex.util.Token.*;

/**
 * Abstract class for parsing various inputs, such as database commands or queries.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public class InputParser {
  /** Parsing exception. */
  private static final String FOUND = ", found '%'";

  /** Input string as codepoints. */
  public final int[] input;
  /** Input length. */
  public final int length;

  /** Input path. */
  public String path;
  /** Current input position. */
  public int pos;
  /** Marked input position. */
  public int mark;

  /**
   * Constructor.
   * @param input input
   */
  public InputParser(final String input) {
    this.input = input.replaceAll("\r\n?", "\n").codePoints().toArray();
    length = this.input.length;
  }

  /**
   * Checks if more characters are found.
   * @return current character
   */
  public final boolean more() {
    return pos < length;
  }

  /**
   * Returns the current character.
   * @return current character
   */
  public final int current() {
    final int i = pos;
    return i < length ? input[i] : 0;
  }

  /**
   * Checks if the current character equals the specified one.
   * @param cp character to be checked
   * @return result of check
   */
  public final boolean current(final int cp) {
    final int i = pos;
    return i < length && cp == input[i];
  }

  /**
   * Remembers the current position.
   */
  protected final void mark() {
    mark = pos;
  }

  /**
   * Returns the next character.
   * @return next character, or {@code 0} if string is exhausted
   */
  protected final int next() {
    final int i = pos + 1;
    return i < length ? input[i] : 0;
  }

  /**
   * Consumes the next character.
   * @return next character, or {@code 0} if string is exhausted
   */
  public final int consume() {
    return pos < length ? input[pos++] : 0;
  }

  /**
   * Peeks forward and consumes the character if it equals the specified one.
   * @param cp character to consume
   * @return true if character was found
   */
  public final boolean consume(final int cp) {
    final int i = pos;
    if(i >= length || cp != input[i]) return false;
    ++pos;
    return true;
  }

  /**
   * Checks if the specified character is a quote.
   * @param cp character to be checked
   * @return result
   */
  protected static boolean quote(final int cp) {
    return cp == '"' || cp == '\'';
  }

  /**
   * Peeks forward and consumes the string if it equals the specified one.
   * @param string string to consume
   * @return true if string was found
   */
  public final boolean consume(final String string) {
    int i = pos;
    final int l = string.length();
    if(i + l > length) return false;
    for(int s = 0; s < l; ++s) {
      if(input[i++] != string.charAt(s)) return false;
    }
    pos = i;
    return true;
  }

  /**
   * Returns a "found" string, containing the current character.
   * @return completion
   */
  protected final byte[] found() {
    return current() == 0 ? EMPTY : Util.inf(FOUND, currentAsString());
  }

  /**
   * Returns a maximum of 15 remaining characters that have not yet been parsed.
   * @return query substring
   */
  protected final String remaining() {
    final TokenBuilder tb = new TokenBuilder();
    final int pl = Math.min(length, pos + 15);
    int p = pos;
    for(; p < pl; p++) {
      final int cp = input[p];
      if(cp == '\n') break;
      tb.add(cp);
    }
    return tb + (pl == length ? "" : DOTS);
  }

  /**
   * Returns an input substring.
   * @param s start index
   * @param e end index
   * @return substring
   */
  public final TokenBuilder substring(final int s, final int e) {
    final TokenBuilder tb = new TokenBuilder();
    for(int i = s; i < e; i++) tb.add(input[i]);
    return tb;
  }

  /**
   * Returns the current character as string.
   * @return current character
   */
  protected final String currentAsString() {
    final int cp = current();
    return cp == 0 ? "END OF INPUT" : !XMLToken.valid(cp) || Character.isSpaceChar(cp) ?
      Character.getName(cp) :
      Character.toString(cp);
  }

  /**
   * Creates input information.
   * @return input info
   */
  public InputInfo info() {
    return new InputInfo(this);
  }
}
