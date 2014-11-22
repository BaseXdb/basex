package org.basex.query.util.regex;

/**
 * A disjunction of branches.
 *
 * @author BaseX Team 2005-14, BSD License
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
    final int bl = branches.length;
    for(int b = 1; b < bl; b++) branches[b].toRegEx(sb.append('|'));
    return sb;
  }
}
