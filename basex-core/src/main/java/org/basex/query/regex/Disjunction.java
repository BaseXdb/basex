package org.basex.query.regex;

/**
 * A disjunction of branches.
 *
 * @author BaseX Team 2005-13, BSD License
 * @author Leo Woerteler
 */
public class Disjunction extends RegExp {
  /** Branches. */
  private final RegExp[] branches;

  /**
   * Constructor.
   * @param br branches
   */
  public Disjunction(final RegExp[] br) {
    branches = br;
  }

  @Override
  StringBuilder toRegEx(final StringBuilder sb) {
    branches[0].toRegEx(sb);
    for(int i = 1; i < branches.length; i++) branches[i].toRegEx(sb.append('|'));
    return sb;
  }
}
