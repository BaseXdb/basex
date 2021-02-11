package org.basex.query.util.regex;

/**
 * A disjunction of branches.
 *
 * @author BaseX Team 2005-21, BSD License
 * @author Leo Woerteler
 */
public final class Disjunction extends RegExp {
  /** Branches. */
  private final RegExp[] branches;

  /**
   * Constructor.
   * @param branches branches
   */
  public Disjunction(final RegExp[] branches) {
    this.branches = branches;
  }

  @Override
  void toRegEx(final StringBuilder sb) {
    branches[0].toRegEx(sb);
    final int bl = branches.length;
    for(int b = 1; b < bl; b++) branches[b].toRegEx(sb.append('|'));
  }
}
