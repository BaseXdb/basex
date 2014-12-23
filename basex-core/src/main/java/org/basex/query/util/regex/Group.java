package org.basex.query.util.regex;

/**
 * A parenthesized group.
 *
 * @author BaseX Team 2005-14, BSD License
 * @author Leo Woerteler
 */
public final class Group extends RegExp {
  /** Enclosed expression. */
  private final RegExp encl;
  /** Capture flag. */
  private final boolean capture;

  /**
   * Constructor.
   * @param sub enclosed expression
   * @param capt capture flag
   */
  public Group(final RegExp sub, final boolean capt) {
    encl = sub;
    capture = capt;
  }

  @Override
  StringBuilder toRegEx(final StringBuilder sb) {
    return encl.toRegEx(sb.append(capture ? "(" : "(?:")).append(')');
  }
}
