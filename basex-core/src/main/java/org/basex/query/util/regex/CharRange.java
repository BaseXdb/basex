package org.basex.query.util.regex;

/**
 * A character range.
 *
 * @author BaseX Team, BSD License
 * @author Leo Woerteler
 */
public final class CharRange extends RegExp {
  /** Left character. */
  private final int left;
  /** Right character. */
  private final int right;
  /** Case-insensitive matching. */
  private final boolean insensitive;

  /**
   * Constructor.
   * @param left left character
   * @param right right character
   */
  public CharRange(final int left, final int right) {
    this(left, right, false);
  }

  /**
   * Constructor.
   * @param left left character
   * @param right right character
   * @param insensitive case-insensitive matching
   */
  public CharRange(final int left, final int right, final boolean insensitive) {
    this.left = left;
    this.right = right;
    this.insensitive = insensitive;
  }

  @Override
  void toRegEx(final StringBuilder sb) {
    append(sb, left, right);
    if(insensitive) {
      final int ll = Character.toLowerCase(left), lr = Character.toLowerCase(right);
      if((ll != left || lr != right) && ll <= lr) append(sb, ll, lr);
      final int ul = Character.toUpperCase(left), ur = Character.toUpperCase(right);
      if((ul != left || ur != right) && ul <= ur) append(sb, ul, ur);
    }
  }

  /**
   * Appends an escaped range to the string builder.
   * @param sb string builder
   * @param lo lower bound
   * @param hi upper bound
   */
  private static void append(final StringBuilder sb, final int lo, final int hi) {
    sb.append(Escape.escape(lo)).append('-').append(Escape.escape(hi));
  }
}
