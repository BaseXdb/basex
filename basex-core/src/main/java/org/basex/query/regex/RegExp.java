package org.basex.query.regex;

/**
 * A node of the regular expression AST.
 * @author Leo Woerteler
 */
public abstract class RegExp {
  @Override
  public String toString() {
    return toRegEx(new StringBuilder()).toString();
  }

  /**
   * Recursive {@link RegExp#toString()} helper.
   * @param sb string builder
   * @return the string builder for convenience
   */
  abstract StringBuilder toRegEx(StringBuilder sb);
}
