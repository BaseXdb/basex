package org.basex.query.xquery.expr;

import org.basex.query.xquery.XQContext;
import org.basex.query.xquery.item.Dbl;
import org.basex.query.xquery.iter.Iter;

/**
 * FTMildnot expression.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Christian Gruen
 */
public final class FTMildNot extends Arr {
  /**
   * Constructor.
   * @param l expression list
   */
  public FTMildNot(final Expr[] l) {
    super(l);
  }

  @Override
  public Iter iter(final XQContext ctx) {
    // <CG> XQuery/FTMildNot: not working yet
    return Dbl.iter(0);
  }

  @Override
  public String toString() {
    return toString(" not in ");
  }
}
