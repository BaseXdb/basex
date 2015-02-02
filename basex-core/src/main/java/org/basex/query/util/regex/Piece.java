package org.basex.query.util.regex;

/**
 * An atom together with a quantifier.
 *
 * @author BaseX Team 2005-15, BSD License
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
  StringBuilder toRegEx(final StringBuilder sb) {
    return quant.toRegEx(atom.toRegEx(sb));
  }

}
