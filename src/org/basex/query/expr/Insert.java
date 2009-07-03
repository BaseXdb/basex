package org.basex.query.expr;


/**
 * Insert expression.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-09, ISC License
 * @author Lukas Kircher
 */
public class Insert extends Arr {
  /** First flag. */
  final boolean first;
  /** Last flag. */
  final boolean last;
  /** After flag. */
  final boolean after;
  /** Before flag. */
  final boolean before;

  /**
   * Constructor.
   * @param src source expression
   * @param fi first flag
   * @param la last flag
   * @param af after flag
   * @param be before flag
   * @param trg target expression
   */
  public Insert(final Expr src, final boolean fi, final boolean la,
      final boolean af, final boolean be, final Expr trg) {
    super(src, trg);
    first = fi;
    last = la;
    after = af;
    before = be;
  }

  @Override
  public String toString() {
    return null;
  }

}
