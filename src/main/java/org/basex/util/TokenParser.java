package org.basex.util;

import static org.basex.util.Token.*;

/**
 * <p>This class can be used to iterate through all codepoints of a token.</p>
 *
 * @author BaseX Team 2005-13, BSD License
 * @author Christian Gruen
 */
public final class TokenParser {
  /** Token to be parsed. */
  private final byte[] token;
  /** Token length. */
  private final int size;
  /** Current position. */
  private int pos;

  /**
   * Constructor.
   * @param tok token
   */
  public TokenParser(final byte[] tok) {
    token = tok;
    size = token.length;
  }

  /**
   * Resets the cursor position.
   */
  public void reset() {
    pos = 0;
  }

  /**
   * Returns if the parser can return more codepoints.
   * @return result of check
   */
  public boolean more() {
    return pos < size;
  }

  /**
   * Returns the current codepoint and advances the cursor.
   * @return current codepoint, or {@code -1}
   */
  public int next() {
    final int p = pos;
    if(p < size) {
      pos += cl(token, p);
      return cp(token, p);
    }
    return -1;
  }

  /**
   * Tries to consume the specified codepoint.
   * @param ch codepoint to be consumed
   * @return indicates if the codepoint was consumed
   */
  public boolean consume(final int ch) {
    final int p = pos;
    if(p >= size || cp(token, p) != ch) return false;
    pos += cl(token, p);
    return true;
  }

  @Override
  public String toString() {
    return string(token);
  }
}
