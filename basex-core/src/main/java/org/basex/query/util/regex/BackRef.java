package org.basex.query.util.regex;

/**
 * Back-reference.
 *
 * @author BaseX Team 2005-15, BSD License
 * @author Leo Woerteler
 */
public final class BackRef extends RegExp {
  /** Capture group number. */
  private final int num;

  /**
   * Constructor.
   * @param n capture group number
   */
  public BackRef(final int n) {
    num = n;
  }

  @Override
  StringBuilder toRegEx(final StringBuilder sb) {
    return sb.append('\\').append(num);
  }
}
