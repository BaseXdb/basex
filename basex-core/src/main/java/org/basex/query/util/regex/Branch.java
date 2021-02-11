package org.basex.query.util.regex;

/**
 * A branch of a regular expression.
 *
 * @author BaseX Team 2005-21, BSD License
 * @author Leo Woerteler
 */
public final class Branch extends RegExp {
  /** Pieces of the regex. */
  private final RegExp[] pieces;

  /**
   * Constructor.
   * @param pieces pieces
   */
  public Branch(final RegExp[] pieces) {
    this.pieces = pieces;
  }

  @Override
  void toRegEx(final StringBuilder sb) {
    for(final RegExp piece : pieces) piece.toRegEx(sb);
  }
}
