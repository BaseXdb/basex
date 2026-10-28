package org.basex.query.util.regex;

/**
 * A branch of a regular expression.
 *
 * @author BaseX Team, BSD License
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
  int minLength() {
    long min = 0;
    for(final RegExp piece : pieces) min += piece.minLength();
    return (int) Math.min(Integer.MAX_VALUE, min);
  }

  @Override
  RegExp[] pieces() {
    return pieces;
  }

  @Override
  void toRegEx(final StringBuilder sb) {
    for(final RegExp piece : pieces) piece.toRegEx(sb);
  }
}
