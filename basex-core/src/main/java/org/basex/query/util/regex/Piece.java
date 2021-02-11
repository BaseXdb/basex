package org.basex.query.util.regex;

/**
 * An atom together with a quantifier.
 *
 * @author BaseX Team 2005-21, BSD License
 * @author Leo Woerteler
 */
public final class Piece extends RegExp {
  /** Wrapped atom. */
  private final RegExp atom;
  /** Quantifier. */
  private final Quantifier quant;

  /**
   * Constructor.
   * @param atom atom
   * @param quant quantifier
   */
  public Piece(final RegExp atom, final Quantifier quant) {
    this.atom = atom;
    this.quant = quant;
  }

  @Override
  void toRegEx(final StringBuilder sb) {
    atom.toRegEx(sb);
    quant.toRegEx(sb);
  }
}
