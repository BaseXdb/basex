package org.basex.util;

import static org.basex.core.Text.*;
import static org.basex.util.Token.*;

/**
 * Abstract class for parsing various inputs, such as database commands or queries.
 *
 * @author BaseX Team 2005-14, BSD License
 * @author Christian Gruen
 */
public class InputParser {
  /** Parsing exception. */
  private static final String FOUND = ", found '%'";

  /** Input to be parsed. */
  public final String input;
  /** Query length. */
  public final int length;

  /** File reference. */
  public String file;
  /** Current input position. */
  public int pos;
  /** Marked input position. */
  public int mark;

  /**
   * Constructor.
   * @param input input
   */
  public InputParser(final String input) {
    this.input = input;
    length = input.length();
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
  public final char curr() {
    final int i = pos;
    return i < length ? input.charAt(i) : 0;
  }

  /**
   * Checks if the current character equals the specified one.
   * @param ch character to be checked
   * @return result of check
   */
  public final boolean curr(final int ch) {
    final int i = pos;
    return i < length && ch == input.charAt(i);
  }

  /**
   * Remembers the current position.
   */
  protected final void mark() {
    mark = pos;
  }

  /**
   * Returns the next character.
   * @return result of check
   */
  protected final char next() {
    final int i = pos + 1;
    return i < length ? input.charAt(i) : 0;
  }

  /**
   * Returns next character.
   * @return next character
   */
  public final char consume() {
    return pos < length ? input.charAt(pos++) : 0;
  }

  /**
   * Peeks forward and consumes the character if it equals the specified one.
   * @param ch character to consume
   * @return true if character was found
   */
  public final boolean consume(final int ch) {
    final int i = pos;
    if(i >= length || ch != input.charAt(i)) return false;
    ++pos;
    return true;
  }

  /**
   * Checks if the specified character is a quote.
   * @param ch character to be checked
   * @return result
   */
  protected static boolean quote(final int ch) {
    return ch == '"' || ch == '\'';
  }

  /**
   * Peeks forward and consumes the string if it equals the specified one.
   * @param str string to consume
   * @return true if string was found
   */
  public final boolean consume(final String str) {
    int i = pos;
    final int l = str.length();
    if(i + l > length) return false;
    for(int s = 0; s < l; ++s) {
      if(input.charAt(i++) != str.charAt(s)) return false;
    }
    pos = i;
    return true;
  }

  /**
   * Returns a "found" string, containing the current character.
   * @return completion
   */
  protected final byte[] found() {
    return curr() == 0 ? EMPTY : Util.inf(FOUND, curr());
  }

  /**
   * Returns the remaining, unscanned query substring.
   * @return query substring
   */
  protected final String rest() {
    final int ie = Math.min(length, pos + 15);
    return input.substring(pos, ie) + (ie == length ? "" : DOTS);
  }

  /**
   * Creates input information.
   * @return input information
   */
  public final InputInfo info() {
    return new InputInfo(this);
  }
}
