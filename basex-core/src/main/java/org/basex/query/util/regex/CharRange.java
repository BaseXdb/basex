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
    sb.append(Escape.escape(left)).append('-').append(Escape.escape(right));
    if(insensitive) {
      for(final int v : CaseVariants.get(left, right)) sb.append(Escape.escape(v));
    }
  }
}
