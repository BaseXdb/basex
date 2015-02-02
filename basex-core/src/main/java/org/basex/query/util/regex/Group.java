package org.basex.query.util.regex;

/**
 * A parenthesized group.
 *
 * @author BaseX Team 2005-15, BSD License
 * @author Leo Woerteler
 */
public final class Group extends RegExp {
  /** Enclosed expression. */
  private final RegExp encl;
  /** Capture flag. */
  private final boolean capture;

  /**
   * Constructor.
   * @param encl enclosed expression
   * @param capture capture flag
   */
  public Group(final RegExp encl, final boolean capture) {
    this.encl = encl;
    this.capture = capture;
  }

  @Override
  void toRegEx(final StringBuilder sb) {
    sb.append(capture ? "(" : "(?:");
    encl.toRegEx(sb);
    sb.append(')');
  }
}
