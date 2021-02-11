package org.basex.query.util.regex;

/**
 * Back-reference.
 *
 * @author BaseX Team 2005-21, BSD License
 * @author Leo Woerteler
 */
public final class BackRef extends RegExp {
  /** Capture group number. */
  private final int num;

  /**
   * Constructor.
   * @param num capture group number
   */
  public BackRef(final int num) {
    this.num = num;
  }

  @Override
  void toRegEx(final StringBuilder sb) {
    sb.append('\\').append(num);
  }
}
