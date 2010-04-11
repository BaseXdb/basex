package org.basex.query.expr;

import org.basex.query.QueryContext;
import org.basex.query.QueryException;
import org.basex.query.item.Nod;
import org.basex.query.item.SeqType;

/**
 * Fragment constructor.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-10, ISC License
 * @author Christian Gruen
 */
public abstract class CFrag extends Arr {
  /**
   * Constructor.
   * @param n name
   */
  protected CFrag(final Expr... n) {
    super(n);
  }

  @Override
  public abstract Nod atomic(final QueryContext ctx) throws QueryException;

  @Override
  public final boolean uses(final Use u, final QueryContext ctx) {
    return u == Use.FRG || super.uses(u, ctx);
  }

  @Override
  public SeqType returned(final QueryContext ctx) {
    return SeqType.NOD;
  }

  /**
   * Returns a string info for the expression.
   * @param pre info prefix
   * @return string
   */
  protected final String info(final String pre) {
    return pre + " constructor";
  }

  /**
   * Returns a string representation of the expression.
   * @param pre expression prefix
   * @return string
   */
  protected final String toString(final String pre) {
    return pre + " { " + super.toString(", ") + " }";
  }
}
