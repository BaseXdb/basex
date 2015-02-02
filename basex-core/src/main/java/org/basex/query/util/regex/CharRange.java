package org.basex.query.util.regex;

/**
 * A character range.
 *
 * @author BaseX Team 2005-15, BSD License
 * @author Leo Woerteler
 */
public final class CharRange extends RegExp {
  /** Left character. */
  private final int left;
  /** Right character. */
  private final int right;

  /**
   * Constructor.
   * @param left left character
   * @param right right character
   */
  public CharRange(final int left, final int right) {
    this.left = left;
    this.right = right;
  }

  @Override
  void toRegEx(final StringBuilder sb) {
    sb.append(Escape.escape(left)).append('-').append(Escape.escape(right));
  }
}
