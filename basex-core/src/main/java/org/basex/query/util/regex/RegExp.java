package org.basex.query.util.regex;

/**
 * A node of the regular expression AST.
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
   * Recursive {@link RegExp#toString()} helper.
   * @param sb string builder
   */
  abstract void toRegEx(StringBuilder sb);
}
