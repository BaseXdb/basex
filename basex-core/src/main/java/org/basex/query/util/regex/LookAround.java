package org.basex.query.util.regex;

/**
 * Lookahaead or lookbehind assertion.
 *
 * @author BaseX Team, BSD License
 * @author Gunther Rademacher
 */
public final class LookAround extends RegExp {
  /** Behind flag. */
  private final boolean behind;
  /** Positive flag. */
  private final boolean positive;
  /** Regular expression. */
  private final RegExp regExp;

  /**
   * Constructor.
   * @param behind 'behind' flag
   * @param positive 'positive' flag
   * @param regExp regular expression
   */
  private LookAround(final boolean behind, final boolean positive, final RegExp regExp) {
    this.behind = behind;
    this.positive = positive;
    this.regExp = regExp;
  }

  /**
   * Creates a regular expression from the given lookahead or lookbehind assertion.
   * @param behind 'behind' flag
   * @param positive 'positive' flag
   * @param regExp regular expression
   * @return regular expression
   */
  public static LookAround get(final boolean behind, final boolean positive, final RegExp regExp) {
    return new LookAround(behind, positive, regExp);
  }

  @Override
  void toRegEx(final StringBuilder sb) {
    sb.append("(?");
    if(behind) sb.append("<");
    sb.append(positive ? '=' : '!');
    regExp.toRegEx(sb);
    sb.append(')');
  }
}
