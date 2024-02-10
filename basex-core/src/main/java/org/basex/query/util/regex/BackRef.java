package org.basex.query.util.regex;

/**
 * Back-reference.
 *
 * @author BaseX Team 2005-24, BSD License
 * @author Leo Woerteler
 */
public final class BackRef extends RegExp {
  /** Capture group number. */
  private final int num;
  /** Flags reference to group in different branch. If true, backref must not be serialized. */
  private final boolean isDifferentBranch;

  /**
   * Constructor.
   * @param num capture group number
   * @param isDifferentBranch the different-branch flag
   */
  public BackRef(final int num, final boolean isDifferentBranch) {
    this.num = num;
    this.isDifferentBranch = isDifferentBranch;
  }

  @Override
  void toRegEx(final StringBuilder sb) {
    if(!isDifferentBranch) sb.append('\\').append(num);
  }
}
