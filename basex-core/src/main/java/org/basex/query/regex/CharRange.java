package org.basex.query.regex;

/**
 * A character range.
 *
 * @author BaseX Team 2005-14, BSD License
 * @author Leo Woerteler
 */
public class CharRange extends RegExp {
  /** Left character. */
  private final int left;
  /** Right character. */
  private final int right;

  /**
   * Constructor.
   * @param a left character
   * @param b right character
   */
  public CharRange(final int a, final int b) {
    left = a;
    right = b;
  }

  @Override
  StringBuilder toRegEx(final StringBuilder sb) {
    return sb.append(Escape.escape(left)).append('-').append(Escape.escape(right));
  }
}
