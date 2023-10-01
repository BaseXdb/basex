package org.basex.query.util.regex;

/**
 * An atom together with a quantifier.
 *
 * @author BaseX Team 2005-23, BSD License
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
    if(quant.getMin() == 0 && atom instanceof Group && ((Group) atom).hasBackRef()) {
      // #2240: replace an optional capturing group by a mandatory one, and wrap its content
      // into an optional non-capturing group
      sb.append("((?:");
      ((Group) atom).getEncl().toRegEx(sb);
      sb.append(')');
      quant.toRegEx(sb);
      sb.append(')');
    } else {
      atom.toRegEx(sb);
      quant.toRegEx(sb);
    }
  }
}
