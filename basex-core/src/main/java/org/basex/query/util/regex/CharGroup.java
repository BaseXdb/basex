package org.basex.query.util.regex;

/**
 * Character group.
 *
 * @author BaseX Team 2005-21, BSD License
 * @author Leo Woerteler
 */
public final class CharGroup extends RegExp {
  /** Negation flag, set after construction. */
  public boolean negative;
  /** Sub-ranges. */
  private final RegExp[] subs;
  /**
   * Constructor.
   * @param subs sub-ranges
   */
  public CharGroup(final RegExp[] subs) {
    this.subs = subs;
  }

  @Override
  void toRegEx(final StringBuilder sb) {
    sb.append(negative ? "^" : "");
    for(final RegExp sub : subs) sub.toRegEx(sb);
  }
}
