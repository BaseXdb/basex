package org.basex.query.util.regex;

/**
 * A branch of a regular expression.
 *
 * @author BaseX Team 2005-15, BSD License
 * @author Leo Woerteler
 */
public final class Branch extends RegExp {
  /** Pieces of the regex. */
  private final RegExp[] pieces;

  /**
   * Constructor.
   * @param pcs pieces
   */
  public Branch(final RegExp[] pcs) {
    pieces = pcs;
  }

  @Override
  StringBuilder toRegEx(final StringBuilder sb) {
    for(final RegExp piece : pieces) piece.toRegEx(sb);
    return sb;
  }
}
