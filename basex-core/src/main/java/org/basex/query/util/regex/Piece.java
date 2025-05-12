package org.basex.query.util.regex;

/**
 * An atom together with a quantifier.
 *
 * @author BaseX Team, BSD License
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
    if(quant.getMin() == 0 && atom instanceof final Group group && group.hasBackRef()) {
      // #2240: replace an optional capturing group by a mandatory one, and wrap its content
      // into an optional non-capturing group
      sb.append("((?:");
      group.getEncl().toRegEx(sb);
      sb.append(')');
      quant.toRegEx(sb);
      sb.append(')');
    } else {
      atom.toRegEx(sb);
      quant.toRegEx(sb);
    }
  }
}
