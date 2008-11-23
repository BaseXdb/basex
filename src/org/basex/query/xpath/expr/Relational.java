package org.basex.query.xpath.expr;

import org.basex.query.xpath.item.Comp;

/**
 * XPath Relational Expression. Can express: LESS (&lt;) LESS_OR_EQUALS (&lt;=)
 * GREATER (&gt;) GREATER_OR_EQUALS (&gt;=)
 * 
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Tim Petrowsky
 */
public final class Relational extends Cmp {
  /**
   * Constructor.
   * @param e1 first expression
   * @param e2 second expression to be compared with
   * @param t see class description
   */
  public Relational(final Expr e1, final Expr e2, final Comp t) {
    super(e1, e2);
    type = t;
  }

  @Override
  public boolean sameAs(final Expr cmp) {
    if(!(cmp instanceof Relational)) return false;
    final Relational ex = (Relational) cmp;
    return expr[0].sameAs(ex.expr[0]) && expr[1].sameAs(ex.expr[1]);
  }
}
