package org.basex.query.regex;

/**
 * Character group.
 *
 * @author BaseX Team 2005-13, BSD License
 * @author Leo Woerteler
 */
public class CharGroup extends RegExp {
  /** Negation flag, set after construction. */
  public boolean negative;
  /** Sub-ranges. */
  private final RegExp[] subs;
  /**
   * Constructor.
   * @param sub sub-ranges
   */
  public CharGroup(final RegExp[] sub) {
    subs = sub;
  }

  @Override
  StringBuilder toRegEx(final StringBuilder sb) {
    sb.append(negative ? "^" : "");
    for(final RegExp sub : subs) sub.toRegEx(sb);
    return sb;
  }
}
