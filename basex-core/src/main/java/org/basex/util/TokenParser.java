package org.basex.util;

import static org.basex.util.Token.*;

/**
 * This class can be used to iterate through all codepoints of a token.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public class TokenParser {
  /** Token to be parsed. */
  protected final byte[] token;
  /** Token length. */
  private final int size;
  /** Current position. */
  private int pos;

  /**
   * Constructor.
   * @param token token
   */
  public TokenParser(final byte[] token) {
    this.token = token;
    size = token.length;
  }

  /**
   * Resets the cursor position.
   */
  public final void reset() {
    pos = 0;
  }

  /**
   * Checks if the parser will return more codepoints.
   * @return result of check
   */
  public final boolean more() {
    return pos < size;
  }

  /**
   * Returns the current codepoint and advances the cursor.
   * @return current codepoint, or {@code -1}
   */
  public final int next() {
    final int p = pos;
    if(p < size) {
      pos += cl(token, p);
      return cp(token, p);
    }
    return -1;
  }

  /**
   * Tries to consume the specified codepoint.
   * @param cp codepoint to be consumed
   * @return indicates if the codepoint was consumed
   */
  public final boolean consume(final int cp) {
    final int p = pos;
    if(p >= size || cp(token, p) != cp) return false;
    pos += cl(token, p);
    return true;
  }

  @Override
  public String toString() {
    return string(token);
  }
}
