package org.basex.query.util.regex;

/**
 * Back-reference.
 *
 * @author BaseX Team, BSD License
 * @author Leo Woerteler
 */
public final class BackRef extends RegExp {
  /** Referenced group. */
  private final Group group;
  /** Flags reference to group in different branch. If true, backref must not be serialized. */
  private final boolean isDifferentBranch;
  /** Case-insensitive matching. */
  private final boolean insensitive;

  /**
   * Constructor.
   * @param group referenced group
   * @param isDifferentBranch different-branch flag
   * @param insensitive case-insensitive matching
   */
  public BackRef(final Group group, final boolean isDifferentBranch, final boolean insensitive) {
    this.group = group;
    this.isDifferentBranch = isDifferentBranch;
    this.insensitive = insensitive;
  }

  @Override
  void toRegEx(final StringBuilder sb) {
    if(!isDifferentBranch) group.backRef(sb, insensitive);
  }
}
