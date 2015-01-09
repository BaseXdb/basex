package org.basex.util;

import static org.basex.util.Token.*;

import org.basex.util.list.*;

/**
 * <p>This class can be used to iterate through all codepoints of a token.</p>
 *
 * @author BaseX Team 2005-15, BSD License
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
   * Checks if the parser will return more codepoints.
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

  /**
   * Returns a list with all codepoints.
   * @return array
   */
  public final IntList toList() {
    final IntList il = new IntList();
    while(more()) il.add(next());
    return il;
  }

  @Override
  public String toString() {
    return string(token);
  }
}
