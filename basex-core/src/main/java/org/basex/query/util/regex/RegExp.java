package org.basex.query.util.regex;

/**
 * A node of the regular expression AST.
 *
 * @author BaseX Team, BSD License
 * @author Leo Woerteler
 */
public abstract class RegExp {
  @Override
  public String toString() {
    final StringBuilder sb = new StringBuilder();
    toRegEx(sb);
    return sb.toString();
  }

  /**
   * Returns the minimum number of code points matched by this expression.
   * @return minimum length
   */
  int minLength() {
    return 0;
  }

  /**
   * Returns the pieces that are matched in sequence by this expression.
   * @return pieces
   */
  RegExp[] pieces() {
    return new RegExp[] { this };
  }

  /**
   * Returns the code point that is matched case-sensitively by this expression.
   * @return code point, or {@code -1}
   */
  int literal() {
    return -1;
  }

  /**
   * Recursive {@link RegExp#toString()} helper.
   * @param sb string builder
   */
  abstract void toRegEx(StringBuilder sb);
}
